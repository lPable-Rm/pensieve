# Fase 6.3 — Widget y acceso directo

## Objetivo

Crear el widget `1 × 1` y conectar su pulsación directamente con `VoiceCaptureService` cuando los requisitos ya estén disponibles.

## Leer antes de editar

```text
AndroidManifest.xml
voice/VoiceCaptureService.kt
notifications/ReminderNotificationAvailability.kt
notifications/VoiceCaptureNotificationChannel.kt
res/drawable/
res/layout/
res/values/strings.xml
res/xml/
```

Consultar las imágenes de `diseño/` solo como referencia visual. No editarlas y no copiar de ellas el flujo técnico antiguo.

## Diseño aprobado

- Widget clásico con `AppWidgetProvider` y `RemoteViews`.
- Tamaño inicial `1 × 1`.
- Fondo lavanda o morado coherente con Pensieve.
- Esquinas redondeadas.
- Icono de micrófono.
- Toda la superficie es pulsable.
- Descripción accesible `Grabar recordatorio`.
- Sin configuración, texto pequeño, contador o animaciones.
- Sin Glance ni dependencias nuevas.

El feedback durante la escucha procede de la notificación foreground y del indicador de privacidad de Android.

## Cambios previstos

Crear algo equivalente a:

```text
widget/VoiceCaptureWidgetProvider.kt
res/layout/widget_voice_capture.xml
res/drawable/widget_voice_background.xml
res/xml/voice_capture_widget_info.xml
```

Modificar Manifest y recursos de texto cuando sea necesario.

## Implementación

1. Declarar el receiver y los metadatos del widget de forma segura.

2. El proveedor debe limitarse a:

- construir `RemoteViews`;
- decidir el destino del clic;
- actualizar las instancias instaladas.

No debe reconocer voz, acceder a Room ni guardar notas.

3. Si están disponibles:

- `RECORD_AUDIO`;
- notificaciones de la app;
- canal de recordatorios;
- canal temporal de captura;

crear un `PendingIntent.getForegroundService(...)` explícito e inmutable dirigido a `VoiceCaptureService` con `ACTION_START_CAPTURE`.

4. Si falta un requisito, crear provisionalmente un `PendingIntent.getActivity(...)` explícito hacia `MainActivity` con `ACTION_PREPARE_WIDGET_VOICE`.

La acción se implementará completamente en 6.4. En esta subfase basta con que abra la pantalla principal sin provocar un crash.

5. Añadir una función pequeña `updateAll(context)` que tolere cero, una o varias instancias.

6. Usar request codes estables y flags correctas para evitar `PendingIntent` duplicados o mutables.

7. No usar un broadcast como trampolín para iniciar el servicio.

## Criterios de aceptación

- [ ] El widget aparece en el selector del launcher.
- [ ] Tiene tamaño, etiqueta e icono adecuados.
- [ ] Toda la superficie es accesible y pulsable.
- [ ] Con requisitos preparados, el clic apunta al servicio y no a la actividad.
- [ ] Sin requisitos, el clic abre la actividad sin fallar.
- [ ] El provider no conoce Room ni `ReminderManager`.
- [ ] Varias instancias se actualizan correctamente.
- [ ] No se añadió Glance ni otra dependencia.

## Comprobaciones

```powershell
cd android
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Pruebas manuales del widget las realiza el desarrollador. Terra no debe iniciar ni gestionar el emulador.

Detenerse y auditar antes de comenzar 6.4.
