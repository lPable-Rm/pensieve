# Fase 6.4 — Permisos y recuperación

## Objetivo

Completar el camino excepcional del widget cuando Android no permite iniciar la captura directamente y reforzar los cambios de permisos entre la actualización y la pulsación.

El camino normal debe seguir sin abrir la aplicación.

## Leer antes de editar

```text
MainActivity.kt
ui/home/HomeRoute.kt
ui/home/HomeScreen.kt
voice/VoiceCaptureService.kt
widget/VoiceCaptureWidgetProvider.kt
notifications/ReminderNotificationAvailability.kt
notifications/VoiceCaptureNotificationChannel.kt
```

## Flujo excepcional

```text
Widget
  -> falta RECORD_AUDIO o una notificación necesaria
  -> MainActivity con ACTION_PREPARE_WIDGET_VOICE
  -> flujo visible existente de fase 5
  -> solicitud o explicación del permiso
  -> actualización de widgets instalados
```

## Implementación

1. Manejar `ACTION_PREPARE_WIDGET_VOICE` tanto en:

- `MainActivity.onCreate()`;
- `MainActivity.onNewIntent()`.

2. Transmitir la orden a `HomeRoute` con el mecanismo efímero más pequeño posible.

3. Consumir la orden una sola vez y reutilizar `startVoiceInput()`.

4. Mantener el flujo de fase 5:

- permiso solicitado desde una actividad visible;
- escucha visible;
- revisión y confirmación dentro de la app.

El guardado automático pertenece únicamente al camino directo del widget.

5. Actualizar todas las instancias del widget:

- después del resultado de `RECORD_AUDIO`;
- al volver a primer plano cuando sea útil;
- después de detectar un requisito revocado.

6. El servicio debe volver a comprobar los requisitos después de promocionarse a foreground y antes de acceder al micrófono. El estado del `PendingIntent` puede haberse quedado antiguo.

7. Si falta un requisito en ese momento:

- no iniciar el reconocedor;
- no crear una nota;
- limpiar el servicio;
- actualizar el widget para el siguiente toque;
- mostrar una explicación genérica solo si Android lo permite.

8. Si el canal de captura está bloqueado, no comenzar una escucha invisible. Si el canal de recordatorios está bloqueado, no iniciar un flujo que normalmente terminaría en una nota invisible.

## Mantenerlo simple

No crear:

- otra actividad de permisos;
- Navigation Compose;
- DataStore para esta señal;
- un bus global de eventos;
- un coordinador de permisos genérico;
- reintentos en segundo plano.

Una acción de intent y un evento consumible son suficientes.

## Casos que deben cubrirse

- Permiso nunca solicitado.
- Permiso rechazado.
- Permiso revocado después de instalar el widget.
- Permiso temporal caducado.
- Notificaciones de la app desactivadas.
- Canal de captura bloqueado.
- Canal de recordatorios bloqueado.
- Motor local no disponible.
- `MainActivity` ya abierta al pulsar el fallback.

## Criterios de aceptación

- [ ] El camino preparado no abre `MainActivity`.
- [ ] El fallback abre una actividad visible y solicita permisos correctamente.
- [ ] La acción se consume una sola vez.
- [ ] No se crea otra pantalla ni otra arquitectura.
- [ ] El servicio revalida permisos antes del micrófono.
- [ ] Un requisito revocado no provoca crash ni nota vacía.
- [ ] Los widgets se actualizan después de cambios relevantes.
- [ ] La voz dentro de la app conserva revisión y confirmación.

## Comprobaciones

```powershell
cd android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Detenerse y auditar antes de comenzar 6.5.
