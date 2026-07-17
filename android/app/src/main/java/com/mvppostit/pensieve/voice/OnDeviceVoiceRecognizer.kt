package com.mvppostit.pensieve.voice

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Envuelve la API de Android para reconocer una única captura de voz local.
 *
 * Esta clase no guarda notas ni conoce Compose. Solo traduce los callbacks de
 * [SpeechRecognizer] a eventos pequeños para que HomeRoute los entregue al
 * ViewModel. Cada sesión se invalida al cancelarla, evitando que un callback
 * tardío cambie la interfaz después de cerrar la barra de voz.
 */
class OnDeviceVoiceRecognizer(
    private val context: Context,
    private val onEvent: (VoiceRecognitionEvent) -> Unit,
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var activeSessionId: Long? = null
    private var nextSessionId = 0L

    /**
     * Comprueba el soporte necesario sin abrir el micrófono ni pedir permisos.
     *
     * HomeRoute usa esta consulta antes de solicitar RECORD_AUDIO. [start]
     * repite la comprobación por si la disponibilidad cambia entre ambos pasos.
     */
    fun isAvailable(): Boolean {
        requireMainThread()
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    }

    /**
     * Inicia una sesión nueva si el dispositivo tiene micrófono y un motor local.
     *
     * HomeRoute debe haber comprobado antes el permiso RECORD_AUDIO. No existe
     * respaldo online: si falta el motor local emitimos un error visible.
     */
    fun start() {
        requireMainThread()
        cancelActiveSession()

        if (!isAvailable()) {
            onEvent(VoiceRecognitionEvent.Failure(VoiceRecognitionFailure.RecognizerUnavailable))
            return
        }

        val sessionId = ++nextSessionId
        val recognizer = try {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } catch (_: UnsupportedOperationException) {
            onEvent(VoiceRecognitionEvent.Failure(VoiceRecognitionFailure.RecognizerUnavailable))
            return
        }

        activeSessionId = sessionId
        speechRecognizer = recognizer
        recognizer.setRecognitionListener(createRecognitionListener(sessionId))

        // La superficie de grabación puede mostrarse antes de que Android envíe
        // onReadyForSpeech. Así el bip no es la única señal de que se ha iniciado.
        onEvent(VoiceRecognitionEvent.ListeningStarted)
        playStartTone()

        try {
            recognizer.startListening(createRecognitionIntent())
        } catch (_: SecurityException) {
            finishSession(sessionId)
            onEvent(VoiceRecognitionEvent.Failure(VoiceRecognitionFailure.PermissionDenied))
        } catch (_: RuntimeException) {
            finishSession(sessionId)
            onEvent(VoiceRecognitionEvent.Failure(VoiceRecognitionFailure.TemporaryFailure))
        }
    }

    /** Detiene la escucha y espera el resultado final o el error del proveedor. */
    fun stop() {
        requireMainThread()
        val sessionId = activeSessionId ?: return
        val recognizer = speechRecognizer ?: return

        onEvent(VoiceRecognitionEvent.Processing)
        try {
            recognizer.stopListening()
        } catch (_: SecurityException) {
            finishSession(sessionId)
            onEvent(VoiceRecognitionEvent.Failure(VoiceRecognitionFailure.PermissionDenied))
        } catch (_: RuntimeException) {
            finishSession(sessionId)
            onEvent(VoiceRecognitionEvent.Failure(VoiceRecognitionFailure.TemporaryFailure))
        }
    }

    /** Cancela una captura en curso y descarta sus callbacks posteriores. */
    fun cancel() {
        requireMainThread()
        cancelActiveSession()
    }

    /** Libera el reconocedor al abandonar la pantalla o destruir la composición. */
    fun destroy() {
        requireMainThread()
        cancelActiveSession()
    }

    private fun createRecognitionIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // Es una defensa adicional. La garantía de privacidad la aporta el
            // reconocedor creado específicamente para funcionar en el dispositivo.
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

    private fun createRecognitionListener(sessionId: Long): RecognitionListener =
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) {
                // Nunca almacenamos ni procesamos audio: Pensieve solo conserva texto.
            }

            override fun onEndOfSpeech() {
                if (isActiveSession(sessionId)) {
                    onEvent(VoiceRecognitionEvent.Processing)
                }
            }

            override fun onError(error: Int) {
                if (!isActiveSession(sessionId)) return

                finishSession(sessionId)
                onEvent(VoiceRecognitionEvent.Failure(mapFailure(error)))
            }

            override fun onResults(results: Bundle?) {
                if (!isActiveSession(sessionId)) return

                val finalText = firstRecognitionResult(results)
                finishSession(sessionId)

                if (finalText == null) {
                    onEvent(VoiceRecognitionEvent.Failure(VoiceRecognitionFailure.NoSpeech))
                } else {
                    onEvent(VoiceRecognitionEvent.FinalResult(finalText))
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                if (!isActiveSession(sessionId)) return

                firstRecognitionResult(partialResults)?.let { partialText ->
                    onEvent(VoiceRecognitionEvent.PartialResult(partialText))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }

    private fun firstRecognitionResult(results: Bundle?): String? =
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.trim()
            ?.takeIf(String::isNotEmpty)

    private fun mapFailure(error: Int): VoiceRecognitionFailure =
        when (error) {
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
            SpeechRecognizer.ERROR_NO_MATCH,
            -> VoiceRecognitionFailure.NoSpeech

            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
            SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
            -> VoiceRecognitionFailure.LanguageUnavailable

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                VoiceRecognitionFailure.PermissionDenied

            else -> VoiceRecognitionFailure.TemporaryFailure
        }

    private fun isActiveSession(sessionId: Long): Boolean = activeSessionId == sessionId

    /** Destruye una sesión que ya entregó resultado o error; no debe cancelarse otra vez. */
    private fun finishSession(sessionId: Long) {
        if (!isActiveSession(sessionId)) return

        val recognizer = speechRecognizer
        speechRecognizer = null
        activeSessionId = null
        recognizer?.destroy()
    }

    /** Invalida primero la sesión para ignorar el onError que puede causar cancel(). */
    private fun cancelActiveSession() {
        val recognizer = speechRecognizer
        speechRecognizer = null
        activeSessionId = null

        recognizer?.cancel()
        recognizer?.destroy()
    }

    private fun playStartTone() {
        val toneGenerator = try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, START_TONE_VOLUME_PERCENT)
        } catch (_: RuntimeException) {
            // Si el sistema no puede reproducir el bip, la captura sigue siendo visible.
            return
        }

        if (toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, START_TONE_DURATION_MILLIS)) {
            mainHandler.postDelayed(toneGenerator::release, START_TONE_DURATION_MILLIS.toLong())
        } else {
            toneGenerator.release()
        }
    }

    private fun requireMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "OnDeviceVoiceRecognizer debe usarse desde el hilo principal."
        }
    }

    private companion object {
        const val START_TONE_VOLUME_PERCENT = 55
        const val START_TONE_DURATION_MILLIS = 180
    }
}

/** Eventos del reconocedor que HomeRoute traduce a las acciones del ViewModel. */
sealed interface VoiceRecognitionEvent {
    data object ListeningStarted : VoiceRecognitionEvent
    data object Processing : VoiceRecognitionEvent
    data class PartialResult(val text: String) : VoiceRecognitionEvent
    data class FinalResult(val text: String) : VoiceRecognitionEvent
    data class Failure(val reason: VoiceRecognitionFailure) : VoiceRecognitionEvent
}

/** Errores de plataforma reducidos a los mensajes que podrá entender la interfaz. */
enum class VoiceRecognitionFailure {
    NoSpeech,
    LanguageUnavailable,
    RecognizerUnavailable,
    PermissionDenied,
    TemporaryFailure,
}
