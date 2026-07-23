# Fase 6 — Índice de implementación con ChatGPT 5.6 Terra

Estado: **completada y auditada**. La matriz manual queda aplazada
a las fases 10 y 11 por decisión de producto.

## Objetivo

Implementar el widget de captura rápida por voz de Pensieve mediante cambios pequeños y auditables.

Flujo definitivo:

```text
Widget
  -> foreground service de micrófono
  -> reconocimiento local
  -> guardado automático con ReminderManager
  -> Room y notificación normal del recordatorio
```

En el uso normal, pulsar el widget **no abre `MainActivity`**. La aplicación solo puede abrirse cuando Android exige una actividad visible para solicitar o recuperar permisos.

Esta decisión sustituye únicamente las frases antiguas de `AGENTS.md`, `CONTEXTO_MANANA.txt` y los bocetos que indicaban que el widget siempre abriría la aplicación. El botón de voz dentro de la app conserva la revisión y confirmación de la fase 5.

## Cómo utilizar estas guías

La fase se divide en seis entregas. La 6.6 se añadió después de la auditoría independiente:

| Subfase | Resultado | Guía |
|---|---|---|
| 6.1 | Foreground service y notificación de escucha | [FASE_6_1_SERVICIO_FOREGROUND.md](guias/fase_6/FASE_6_1_SERVICIO_FOREGROUND.md) |
| 6.2 | Reconocimiento local y guardado automático | [FASE_6_2_RECONOCIMIENTO_GUARDADO.md](guias/fase_6/FASE_6_2_RECONOCIMIENTO_GUARDADO.md) |
| 6.3 | Widget `1 × 1` y acceso directo | [FASE_6_3_WIDGET.md](guias/fase_6/FASE_6_3_WIDGET.md) |
| 6.4 | Preparación de permisos y recuperación | [FASE_6_4_PERMISOS.md](guias/fase_6/FASE_6_4_PERMISOS.md) |
| 6.5 | Pruebas y auditoría independiente | [FASE_6_5_AUDITORIA.md](guias/fase_6/FASE_6_5_AUDITORIA.md) |
| 6.6 | Correcciones justificadas por la auditoría | [FASE_6_6_CORRECCIONES_AUDITORIA.md](guias/fase_6/FASE_6_6_CORRECCIONES_AUDITORIA.md) |

Reglas de trabajo:

1. Implementar una única subfase.
2. Ejecutar sus comprobaciones.
3. Detenerse y entregar el diff para auditoría.
4. No iniciar la siguiente hasta recibir aprobación.
5. No marcar la fase 6 como terminada hasta implementar y verificar la subfase 6.6.

Terra debe leer únicamente:

- `AGENTS.md`;
- `CONTEXTO_MANANA.txt`;
- este índice;
- la guía de la subfase activa;
- los archivos de código indicados en esa guía.

No necesita cargar las otras subfases por adelantado.

## Arquitectura común

```text
VoiceCaptureWidgetProvider
       |
       |-- requisitos disponibles
       |       -> PendingIntent.getForegroundService(...)
       |       -> VoiceCaptureService
       |       -> OnDeviceVoiceRecognizer
       |       -> ReminderManager.createReminder(text)
       |       -> Room + notificación del recordatorio
       |
       `-- falta un permiso
               -> MainActivity
               -> flujo visible ya existente
```

Responsabilidades:

- El widget solo presenta la acción y crea el `PendingIntent`.
- El servicio controla una única sesión de escucha.
- `OnDeviceVoiceRecognizer` sigue siendo el único adaptador de `SpeechRecognizer`.
- `ReminderManager` sigue coordinando Room y las notificaciones.
- `MainActivity` solo interviene en el camino excepcional de permisos.

## Restricciones comunes

- Sin permiso `INTERNET` ni reconocimiento online de respaldo.
- Sin guardar audio, resultados parciales o transcripciones en logs.
- Sin Glance, Hilt, Navigation Compose, WorkManager ni capas nuevas.
- Sin una segunda actividad, overlays o servicios exportados.
- Sin cambios de Gradle, Kotlin, Compose o Android SDK.
- Sin dependencias nuevas.
- Sin cambios en el esquema de Room.
- Sin modificar `diseño/`.
- Sin gestionar el emulador.
- Sin commit ni push.
- Mantener el flujo de voz de fase 5 sin regresiones.
- Añadir comentarios didácticos únicamente para explicar decisiones no obvias de permisos, seguridad o ciclo de vida.

## Decisiones ya aprobadas

- Widget clásico con `AppWidgetProvider` y `RemoteViews`.
- Tamaño inicial `1 × 1`, sin configuración ni animaciones.
- Inicio mediante `PendingIntent.getForegroundService(...)` explícito e inmutable.
- Foreground service no exportado con tipo `microphone`.
- Notificación temporal `Escuchando…` con acción `Cancelar`.
- Reconocimiento exclusivamente en el dispositivo.
- Los parciales se ignoran en el flujo del widget.
- El resultado final válido se guarda automáticamente una sola vez.
- `START_NOT_STICKY`: nunca reiniciar una escucha sin otra pulsación.
- Timeout prudente de 60 segundos.
- Room continúa siendo la fuente de verdad.
- Si cambia un permiso justo después de guardar en Room, no borrar la nota: la fase 7 reconciliará la notificación.

## Comprobaciones comunes

Después de Kotlin o Compose:

```powershell
cd android
.\gradlew.bat :app:compileDebugKotlin
```

Cuando cambien recursos o Manifest:

```powershell
.\gradlew.bat :app:assembleDebug
```

Al terminar cada subfase:

```powershell
.\gradlew.bat lintDebug
```

No ocultar errores. Corregir únicamente lo que pertenezca a la subfase actual.

## Prompt base para Terra

Sustituir `[SUBFASE]` y `[ARCHIVO]` antes de enviarlo:

```text
Vamos a implementar exclusivamente la subfase [SUBFASE] de Pensieve.

Lee completamente AGENTS.md, CONTEXTO_MANANA.txt,
GUIA_FASE_6_TERRA.md y [ARCHIVO]. Después inspecciona solo los archivos de
código que indica esa subguía y revisa git status.

La decisión definitiva de fase 6 es que el widget inicia directamente un
foreground service de micrófono y no abre MainActivity durante el uso normal.
MainActivity se reserva para preparar permisos. Esta decisión sustituye solo
las descripciones antiguas de fase 6; no cambies el flujo auditado de fase 5.

Implementa únicamente el alcance de [SUBFASE]. Mantén el código simple, no
añadas dependencias, no gestiones el emulador y no hagas commit ni push.

Ejecuta las comprobaciones indicadas en la subguía. Después detente y entrega:
- problema resuelto;
- archivos modificados;
- decisiones técnicas;
- resultados exactos de compilación, tests y lint;
- riesgos o pruebas manuales pendientes.

No continúes con la siguiente subfase.
```

## Cierre de la fase

La subfase 6.6 superó la reauditoría estática y automática. `AGENTS.md` y
`CONTEXTO_MANANA.txt` reflejan ya la arquitectura definitiva. Las pruebas
manuales pendientes se conservan como trabajo de calidad y publicación en las
fases 10 y 11.
