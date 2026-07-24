# Fase 8.4 — Integración con Android y captura por voz

## Objetivo

Comprobar que todas las superficies usan los recursos localizados y que la voz
sigue el idioma general de Android.

## Inspeccionar

```text
ui/home/HomeRoute.kt
ui/home/HomeScreen.kt
ui/home/components/VoiceInputBar.kt
notifications/ReminderNotificationChannel.kt
notifications/VoiceCaptureNotificationChannel.kt
notifications/ReminderNotificationPublisher.kt
voice/VoiceCaptureService.kt
voice/OnDeviceVoiceRecognizer.kt
widget/VoiceCaptureWidgetProvider.kt
res/layout/widget_voice_capture.xml
res/xml/voice_capture_widget_info.xml
AndroidManifest.xml
```

## Validación

1. Confirmar que todo texto visible procede de `R.string`, `stringResource`,
   `pluralStringResource` o `@string/...`.
2. Mantener las notas introducidas por la persona sin traducir: son contenido,
   no interfaz.
3. Confirmar que notificaciones, canales y servicio obtienen sus textos con el
   `Context` correspondiente.
4. Confirmar que el nombre y la descripción del widget usan recursos.
5. Conservar en `OnDeviceVoiceRecognizer`:

```kotlin
Locale.getDefault().toLanguageTag()
```

6. No crear un mapa entre idioma y región ni modificar el reconocimiento local.
7. No activar `generateLocaleConfig`, añadir `android:localeConfig` ni usar
   APIs para fijar un idioma distinto al general del sistema.

El código actual ya utiliza recursos y `Locale.getDefault()`. Si la inspección
lo confirma, no modificar Kotlin solo para producir un diff.

## Correcciones permitidas

Únicamente sustituir un texto visible escrito directamente en Kotlin o XML por
uno de los recursos ya presentes en el CSV. Si hiciera falta un texto nuevo,
detenerse: primero debe traducirse y añadirse al CSV.

## Comprobaciones

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Detenerse antes de 8.5.
