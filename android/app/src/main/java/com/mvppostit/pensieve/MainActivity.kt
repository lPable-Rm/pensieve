package com.mvppostit.pensieve

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mvppostit.pensieve.ui.appearance.AppearancePaletteSheet
import com.mvppostit.pensieve.ui.home.HomeRoute
import com.mvppostit.pensieve.ui.onboarding.OnboardingScreen
import com.mvppostit.pensieve.ui.theme.PaletteId
import com.mvppostit.pensieve.ui.theme.PensieveTheme
import com.mvppostit.pensieve.widget.VoiceCaptureWidgetProvider
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private companion object {
        /** Clave del único dato visual que necesitamos conservar al recrear la actividad. */
        const val STATE_OPENED_FROM_WIDGET = "opened_from_widget"
    }

    /**
     * La actividad y HomeRoute comparten el mismo coordinador del contenedor.
     * `lazy` evita construirlo hasta que realmente se necesita.
     */
    private val reminderManager by lazy {
        (application as PensieveApplication).appContainer.reminderManager
    }

    /** Preferencias compartidas que ya viven en el contenedor de la aplicación. */
    private val appPreferences by lazy {
        (application as PensieveApplication).appContainer.appPreferences
    }

    /**
     * Conservamos el trabajo para que dos llamadas consecutivas a `onStart`
     * no lancen dos reconciliaciones simultáneas sobre las mismas notificaciones.
     */
    private var notificationReconciliationJob: Job? = null

    // Es un contador y no un booleano: dos pulsaciones consecutivas generan
    // dos eventos distinguibles aunque la composición aún esté recomponiendo.
    private var widgetVoiceRequestToken by mutableIntStateOf(0)

    /** El alias privado del widget tiene prioridad sobre el onboarding. */
    private var openedFromWidgetEntry by mutableStateOf(false)

    /** Evita escribir dos veces la misma finalización por pulsaciones rápidas. */
    private var onboardingCompletionStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // El Intent puede consumirse después de la primera creación. Guardamos
        // solo esta señal privada para que una recreación no devuelva al usuario
        // al onboarding antes de iniciar el fallback de voz del widget.
        openedFromWidgetEntry = savedInstanceState?.getBoolean(
            STATE_OPENED_FROM_WIDGET,
            false,
        ) == true

        // Se procesa antes de setContent para no perder el evento si Android
        // crea la actividad directamente desde el PendingIntent del widget.
        handleWidgetIntent(intent)

        enableEdgeToEdge()

        // La Application conserva las dependencias durante todo el proceso.
        // MainActivity solo entrega a la ruta la dependencia que esta necesita.
        setContent {
            // Un valor nullable distingue «todavía no leído» de Lavanda. Así una
            // paleta guardada no muestra primero un destello de la predeterminada.
            val selectedPaletteFlow: Flow<PaletteId?> = remember(appPreferences) {
                appPreferences.colorPalette.map { paletteId -> paletteId }
            }
            val selectedPalette by selectedPaletteFlow
                .collectAsStateWithLifecycle(initialValue = null)
            val loadedPalette = selectedPalette ?: return@setContent

            PensieveTheme(paletteId = loadedPalette) {
                // El Flow se convierte en nullable para distinguir "aún no
                // cargado" de false y evitar un parpadeo del onboarding.
                val onboardingStateFlow: Flow<Boolean?> = remember(appPreferences) {
                    appPreferences.onboardingCompleted.map { completed -> completed }
                }
                val onboardingCompleted by onboardingStateFlow
                    .collectAsStateWithLifecycle(initialValue = null)

                when {
                    openedFromWidgetEntry || onboardingCompleted == true -> {
                        // El widget debe entrar directamente en Home para
                        // conservar su flujo de permisos y captura de voz.
                        val snackbarHostState = remember { SnackbarHostState() }
                        var isAppearanceSheetVisible by rememberSaveable {
                            mutableStateOf(false)
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            snackbarHost = {
                                SnackbarHost(hostState = snackbarHostState)
                            },
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .consumeWindowInsets(innerPadding),
                            ) {
                                HomeRoute(
                                    reminderManager = reminderManager,
                                    snackbarHostState = snackbarHostState,
                                    widgetVoiceRequestToken = widgetVoiceRequestToken,
                                    onWidgetVoiceRequestConsumed = ::consumeWidgetVoiceRequest,
                                    onAppearanceClick = {
                                        isAppearanceSheetVisible = true
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )

                                if (isAppearanceSheetVisible) {
                                    AppearancePaletteSheet(
                                        selectedPalette = loadedPalette,
                                        onPaletteSelected = { paletteId ->
                                            isAppearanceSheetVisible = false
                                            lifecycleScope.launch {
                                                try {
                                                    // DataStore confirma primero la
                                                    // escritura; después app y widget
                                                    // comparten la misma fuente de verdad.
                                                    appPreferences.setColorPalette(paletteId)
                                                    VoiceCaptureWidgetProvider.updateAll(
                                                        this@MainActivity,
                                                        paletteId,
                                                    )
                                                } catch (_: IOException) {
                                                    // Si el almacenamiento local falla,
                                                    // reabrimos el selector sin aplicar
                                                    // al widget un valor no persistido.
                                                    isAppearanceSheetVisible = true
                                                }
                                            }
                                        },
                                        onDismissRequest = {
                                            isAppearanceSheetVisible = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    onboardingCompleted == false -> {
                        OnboardingScreen(
                            modifier = Modifier.fillMaxSize(),
                            canAddWidget = canRequestPinAppWidget(),
                            onNext = {},
                            onSkip = ::completeOnboarding,
                            onStart = ::completeOnboarding,
                            onAddWidget = ::requestWidgetAndCompleteOnboarding,
                        )
                    }

                    else -> {
                        // Mientras DataStore entrega su primer valor dejamos
                        // una superficie vacía, sin mostrar una ruta incorrecta.
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // El estado se limita al origen privado del widget; las preferencias de
        // producto siguen teniendo DataStore como única fuente persistente.
        outState.putBoolean(STATE_OPENED_FROM_WIDGET, openedFromWidgetEntry)
        super.onSaveInstanceState(outState)
    }

    /** Android puede llamar a este método al volver a primer plano. */
    override fun onStart() {
        super.onStart()

        if (notificationReconciliationJob?.isActive == true) return

        notificationReconciliationJob = lifecycleScope.launch {
            try {
                // Room decide qué recordatorios deben existir; Android solo se
                // ajusta a esa instantánea sin mostrar mensajes a la persona.
                reminderManager.reconcileNotifications()
            } catch (exception: Exception) {
                // Una cancelación pertenece al ciclo de vida y debe propagarse.
                // Los demás fallos no deben cerrar la pantalla principal.
                if (exception is CancellationException) throw exception
            }
        }
    }

    /** Android llama aquí cuando el PendingIntent reutiliza esta actividad. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    /** Convierte solo la acción del widget en una señal consumible por Compose. */
    private fun handleWidgetIntent(intent: Intent?) {
        if (
            intent?.action == VoiceCaptureWidgetProvider.ACTION_PREPARE_WIDGET_VOICE &&
            intent.component == ComponentName(
                packageName,
                VoiceCaptureWidgetProvider.WIDGET_ENTRY_CLASS_NAME,
            )
        ) {
            openedFromWidgetEntry = true
            widgetVoiceRequestToken += 1
            // Evita repetir la misma orden si Android recrea la actividad con
            // el Intent que acaba de entregar.
            intent.action = null
        }
    }

    /** Consume el token solo si sigue siendo el evento que se entregó. */
    private fun consumeWidgetVoiceRequest(token: Int) {
        if (widgetVoiceRequestToken == token) {
            widgetVoiceRequestToken = 0
        }
    }

    /** Guarda la finalización una sola vez; el Flow hará cambiar la ruta a Home. */
    private fun completeOnboarding() {
        if (onboardingCompletionStarted) return

        onboardingCompletionStarted = true
        lifecycleScope.launch {
            try {
                appPreferences.setOnboardingCompleted(true)
            } catch (exception: Exception) {
                if (exception is CancellationException) throw exception
                // Permite reintentar si Android no pudo escribir la preferencia.
                onboardingCompletionStarted = false
            }
        }
    }

    /** Solicita el widget solo en launchers que anuncian soporte para fijarlo. */
    private fun requestWidgetAndCompleteOnboarding() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(
                ComponentName(this, VoiceCaptureWidgetProvider::class.java),
                null,
                null,
            )
        }

        // La confirmación nativa queda bajo control del launcher; la app no
        // muestra un error ni mantiene bloqueada la entrada principal.
        completeOnboarding()
    }

    private fun canRequestPinAppWidget(): Boolean =
        AppWidgetManager.getInstance(this).isRequestPinAppWidgetSupported
}
