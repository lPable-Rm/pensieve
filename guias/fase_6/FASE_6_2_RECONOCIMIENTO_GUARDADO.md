# Fase 6.2 — Reconocimiento local y guardado

## Objetivo

Completar `VoiceCaptureService` para reconocer voz localmente y guardar automáticamente un único resultado final mediante la arquitectura existente.

Todavía no se implementa el widget.

## Leer antes de editar

```text
voice/VoiceCaptureService.kt
voice/OnDeviceVoiceRecognizer.kt
AppContainer.kt
PensieveApplication.kt
reminders/ReminderManager.kt
data/repository/ReminderRepository.kt
notifications/ReminderNotificationPublisher.kt
notifications/ReminderNotificationAvailability.kt
```

## Flujo de esta subfase

```text
VoiceCaptureService
  -> OnDeviceVoiceRecognizer
  -> resultado final
  -> ReminderManager.createReminder(text)
  -> Room
  -> notificación normal del recordatorio
  -> limpieza y stopSelf()
```

## Implementación

1. Reutilizar `OnDeviceVoiceRecognizer`; no copiar su lógica ni usar `SpeechRecognizer` directamente desde una segunda clase.

2. El servicio posee su propia instancia del reconocedor y la destruye al terminar.

3. Antes de escuchar, comprobar:

- `RECORD_AUDIO` concedido;
- reconocimiento en el dispositivo disponible;
- notificaciones de recordatorios disponibles mediante la ayuda existente.

4. Ejecutar las operaciones del reconocedor en el hilo principal.

5. Tratar los eventos así:

- `PartialResult`: ignorar;
- `FinalResult`: guardar una sola vez si no está vacío;
- `Failure`: mostrar un error genérico cuando sea posible y terminar sin guardar.

6. Obtener `ReminderManager` desde:

```kotlin
(application as PensieveApplication).appContainer
```

No crear otro repositorio ni otro contenedor.

7. Usar un `CoroutineScope` pequeño propiedad del servicio. Cancelarlo en `onDestroy()`.

8. Añadir un timeout máximo de 60 segundos. El timeout no guarda parciales.

9. Centralizar la salida en una limpieza idempotente que:

- impida un segundo guardado;
- cancele y destruya el reconocedor;
- cancele trabajos pendientes;
- retire el foreground service;
- libere el micrófono;
- detenga el servicio.

10. Si Room guarda la nota y después cambia el permiso de notificaciones, conservar la nota. No simular una transacción con Android; la fase 7 reconciliará la notificación.

## No implementar todavía

- Widget.
- Fallback mediante `MainActivity`.
- Transcripción parcial visible.
- Revisión o edición desde el launcher.
- Audio persistido, logs sensibles o respaldo online.
- Abstracciones grandes creadas únicamente para hacer mocks.

## Pruebas razonables

Probar decisiones puras o estados internos si pueden extraerse sin complicar el diseño:

- solo se acepta un guardado;
- cancelar no guarda;
- texto vacío no guarda;
- fallo no guarda;
- segunda orden no inicia otra escucha;
- timeout termina la sesión.

El motor de reconocimiento real se comprobará manualmente; no crear una infraestructura compleja para simular Android.

## Criterios de aceptación

- [ ] Se reutiliza `OnDeviceVoiceRecognizer`.
- [ ] Solo se guarda el resultado final.
- [ ] `ReminderManager` crea la nota y su notificación.
- [ ] No hay doble guardado.
- [ ] Todos los caminos liberan el micrófono.
- [ ] No se almacenan audio ni transcripciones.
- [ ] No se usa Internet.
- [ ] El servicio no se reinicia automáticamente.

## Comprobaciones

```powershell
cd android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Detenerse y auditar antes de comenzar 6.3.
