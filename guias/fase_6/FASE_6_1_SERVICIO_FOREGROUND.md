# Fase 6.1 — Servicio foreground de micrófono

## Objetivo

Crear la infraestructura mínima para ejecutar una sesión visible de micrófono en segundo plano. Esta subfase no reconoce voz, no guarda notas y todavía no crea el widget.

Al terminar debe existir un servicio válido que:

- se promociona inmediatamente a foreground;
- muestra `Escuchando…`;
- permite cancelar;
- termina limpiamente;
- nunca se reinicia solo.

## Leer antes de editar

```text
android/app/src/main/AndroidManifest.xml
android/app/src/main/java/com/mvppostit/pensieve/PensieveApplication.kt
android/app/src/main/java/com/mvppostit/pensieve/notifications/ReminderNotificationChannel.kt
android/app/src/main/res/values/strings.xml
android/app/build.gradle.kts
```

Revisar `build.gradle.kts` únicamente para conocer las APIs disponibles. No modificarlo.

## Cambios previstos

Crear:

```text
voice/VoiceCaptureService.kt
notifications/VoiceCaptureNotificationChannel.kt
```

Modificar:

```text
AndroidManifest.xml
PensieveApplication.kt
strings.xml
```

## Implementación

1. Añadir sin eliminar permisos existentes:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
```

2. Declarar `VoiceCaptureService`:

- `android:exported="false"`;
- `android:foregroundServiceType="microphone"`.

3. Crear un canal de baja importancia separado del canal de recordatorios.

4. Crear el canal desde `PensieveApplication.onCreate()`.

5. Definir dos acciones internas descriptivas:

- `ACTION_START_CAPTURE`;
- `ACTION_CANCEL_CAPTURE`.

6. Ante `ACTION_START_CAPTURE`:

- publicar inmediatamente la notificación foreground;
- usar el tipo de servicio `microphone`;
- no iniciar todavía `SpeechRecognizer`;
- ignorar una segunda orden si el servicio ya está activo.

7. La notificación debe:

- indicar `Escuchando…`;
- ser `ongoing`;
- no mostrar datos sensibles;
- incluir `Cancelar` mediante un `PendingIntent.getService(...)` explícito e inmutable.

8. Ante cancelación o destrucción:

- retirar la notificación;
- ejecutar una limpieza idempotente;
- llamar a `stopSelf()` cuando corresponda.

9. Devolver `START_NOT_STICKY`.

## No implementar todavía

- `OnDeviceVoiceRecognizer`.
- Corrutinas de guardado.
- `ReminderManager` o Room.
- Widget y `AppWidgetProvider`.
- Apertura de `MainActivity`.
- Timeout de reconocimiento.

## Criterios de aceptación

- [ ] Manifest contiene los dos permisos de foreground service.
- [ ] El servicio es no exportado y de tipo `microphone`.
- [ ] El canal temporal se crea al arrancar la aplicación.
- [ ] La notificación no contiene transcripciones.
- [ ] `Cancelar` apunta explícitamente al servicio.
- [ ] Una segunda orden no duplica la sesión.
- [ ] El servicio usa `START_NOT_STICKY`.
- [ ] No hay dependencias ni cambios ajenos.

## Comprobaciones

```powershell
cd android
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Detenerse después de entregar el diff y los resultados. No comenzar 6.2.
