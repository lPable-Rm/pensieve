package com.mvppostit.pensieve.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mvppostit.pensieve.PensieveApplication
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.data.local.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderNotificationPublisherTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var publisher: ReminderNotificationPublisher

    @Before
    fun prepareNotifications() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        context = instrumentation.targetContext

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }

        notificationManager = context.getSystemService(NotificationManager::class.java)
        // Los canales son inmutables después de crearse; se elimina el canal
        // para que el caso intente partir de la configuración inicial.
        notificationManager.deleteNotificationChannel(ReminderNotificationChannel.CHANNEL_ID)
        ReminderNotificationChannel.create(context)
        publisher = ReminderNotificationPublisher(context)
        cancelTestNotifications()
    }

    @After
    fun cancelNotifications() {
        cancelTestNotifications()
    }

    @Test
    fun createChannel_usesTheStableDefaultSilentConfiguration() {
        val channel = requireNotNull(
            notificationManager.getNotificationChannel(ReminderNotificationChannel.CHANNEL_ID),
        )

        assertTrue(
            "El canal debe ser visible y no puede quedarse en IMPORTANCE_LOW",
            channel.importance >= NotificationManager.IMPORTANCE_DEFAULT,
        )
        assertNull(channel.sound)
        assertFalse(channel.shouldVibrate())
        assertEquals(
            context.getString(R.string.reminder_notification_channel_name),
            channel.name.toString(),
        )
        assertEquals(
            context.getString(R.string.reminder_notification_channel_description),
            channel.description,
        )
    }

    @Test
    fun publish_buildsTheReminderContract() {
        val reminder = testReminder(FIRST_TEST_ID, "Comprar fruta")

        publisher.publish(reminder)

        val postedNotification = awaitPostedNotification(reminder.id)
        val notification = postedNotification.notification
        assertEquals(ReminderNotificationChannel.CHANNEL_ID, notification.channelId)
        assertEquals(Notification.CATEGORY_REMINDER, notification.category)
        assertEquals(Notification.VISIBILITY_PRIVATE, notification.visibility)
        assertNull(notification.extras.getCharSequence(Notification.EXTRA_TITLE))
        assertEquals(
            reminder.text,
            notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString(),
        )
        assertEquals(
            reminder.createdAtMillis,
            notification.`when`,
        )
        assertEquals(
            reminder.text,
            notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString(),
        )
        assertNotNull(notification.contentIntent)
        assertTrue(notification.contentIntent.isActivity)
        assertTrue(notification.contentIntent.isImmutable)
        assertEquals(1, notification.actions.size)
        assertEquals(
            context.getString(R.string.complete_reminder_action),
            notification.actions.single().title.toString(),
        )
        assertTrue(notification.actions.single().actionIntent.isBroadcast)
        assertTrue(notification.actions.single().actionIntent.isImmutable)
    }

    @Test
    fun completeAction_removesAnOrphanNotification() {
        val reminder = testReminder(SECOND_TEST_ID, "Notificación huérfana")
        publisher.publish(reminder)
        val postedNotification = awaitPostedNotification(reminder.id)

        postedNotification.notification.actions.single().actionIntent.send()

        awaitRemovedNotification(reminder.id)
    }

    @Test
    fun completeAction_deletesOnlyItsReminderEndToEnd() = runBlocking {
        val application = context.applicationContext as PensieveApplication
        val manager = application.appContainer.reminderManager
        var firstReminder: ReminderEntity? = null
        var secondReminder: ReminderEntity? = null

        try {
            val first = manager.createReminder("Primera nota de prueba")
            val second = manager.createReminder("Segunda nota de prueba")
            firstReminder = first
            secondReminder = second
            val firstNotification = awaitPostedNotification(first.id)
            awaitPostedNotification(second.id)

            firstNotification.notification.actions.single().actionIntent.send()

            val remainingReminders = withTimeout(ASYNC_OPERATION_TIMEOUT_MILLIS) {
                manager.observeReminders().first { reminders ->
                    reminders.none { it.id == first.id } &&
                        reminders.any { it.id == second.id }
                }
            }
            awaitRemovedNotification(first.id)

            assertFalse(remainingReminders.any { it.id == first.id })
            assertTrue(remainingReminders.any { it.id == second.id })
            assertNotNull(findNotification(second.id))
        } finally {
            firstReminder?.let { manager.completeReminder(it.id) }
            secondReminder?.let { manager.completeReminder(it.id) }
        }
    }

    private fun awaitPostedNotification(reminderId: Long): StatusBarNotification {
        repeat(MAX_STATUS_CHECKS) {
            findNotification(reminderId)?.let { return it }
            SystemClock.sleep(STATUS_CHECK_INTERVAL_MILLIS)
        }

        throw AssertionError("No se publicó la notificación de prueba")
    }

    private fun awaitRemovedNotification(reminderId: Long) {
        repeat(MAX_STATUS_CHECKS) {
            if (findNotification(reminderId) == null) return
            SystemClock.sleep(STATUS_CHECK_INTERVAL_MILLIS)
        }

        throw AssertionError("No se retiró la notificación de prueba")
    }

    private fun findNotification(reminderId: Long): StatusBarNotification? =
        notificationManager.activeNotifications.firstOrNull { notification ->
            // Android puede crear un resumen de grupo con el mismo id, pero
            // siempre le asigna una etiqueta interna; Pensieve publica sin tag.
            notification.id == reminderId.toInt() && notification.tag == null
        }

    private fun cancelTestNotifications() {
        notificationManager.cancel(FIRST_TEST_ID.toInt())
        notificationManager.cancel(SECOND_TEST_ID.toInt())
    }

    private fun testReminder(id: Long, text: String): ReminderEntity =
        ReminderEntity(
            id = id,
            text = text,
            createdAtMillis = 100L,
        )

    private companion object {
        const val FIRST_TEST_ID = 9_000_001L
        const val SECOND_TEST_ID = 9_000_002L
        const val MAX_STATUS_CHECKS = 100
        const val STATUS_CHECK_INTERVAL_MILLIS = 50L
        const val ASYNC_OPERATION_TIMEOUT_MILLIS = 5_000L
    }
}
