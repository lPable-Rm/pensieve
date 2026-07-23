package com.mvppostit.pensieve

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.mvppostit.pensieve.ui.home.HomeRoute
import com.mvppostit.pensieve.ui.theme.PensieveTheme
import com.mvppostit.pensieve.widget.VoiceCaptureWidgetProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /**
     * La actividad y HomeRoute comparten el mismo coordinador del contenedor.
     * `lazy` evita construirlo hasta que realmente se necesita.
     */
    private val reminderManager by lazy {
        (application as PensieveApplication).appContainer.reminderManager
    }

    /**
     * Conservamos el trabajo para que dos llamadas consecutivas a `onStart`
     * no lancen dos reconciliaciones simultáneas sobre las mismas notificaciones.
     */
    private var notificationReconciliationJob: Job? = null

    // Es un contador y no un booleano: dos pulsaciones consecutivas generan
    // dos eventos distinguibles aunque la composición aún esté recomponiendo.
    private var widgetVoiceRequestToken by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Se procesa antes de setContent para no perder el evento si Android
        // crea la actividad directamente desde el PendingIntent del widget.
        handleWidgetIntent(intent)

        enableEdgeToEdge()

        // La Application conserva las dependencias durante todo el proceso.
        // MainActivity solo entrega a la ruta la dependencia que esta necesita.
        setContent {
            PensieveTheme {
                // Estado compartido entre el Scaffold, que dibuja el snackbar,
                // y HomeScreen, que más adelante pedirá mostrar mensajes.
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    // Scaffold coloca el snackbar sobre el contenido y lo mantiene
                    // visible durante el tiempo que indique SnackbarHostState.
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                ) { innerPadding ->
                    HomeRoute(
                        reminderManager = reminderManager,
                        snackbarHostState = snackbarHostState,
                        widgetVoiceRequestToken = widgetVoiceRequestToken,
                        onWidgetVoiceRequestConsumed = ::consumeWidgetVoiceRequest,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
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
}
