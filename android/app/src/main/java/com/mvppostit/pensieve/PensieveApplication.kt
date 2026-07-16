package com.mvppostit.pensieve

import android.app.Application

import com.mvppostit.pensieve.notifications.ReminderNotificationChannel

/**
 * Punto de inicio del proceso de Pensieve.
 *
 * Android crea esta clase antes que la actividad. El contenedor se inicializa
 * solo cuando alguien lo necesita, evitando trabajo innecesario al arrancar.
 */
class PensieveApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // El canal debe existir antes de publicar el primer recordatorio.
        ReminderNotificationChannel.create(this)
    }

    val appContainer: AppContainer by lazy {
        AppContainer(applicationContext)
    }
}