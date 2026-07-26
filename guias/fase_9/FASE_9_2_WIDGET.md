# Fase 9.2 — Widget rectangular

Estado: **completada y auditada**. No quedan hallazgos altos o medios.

## Objetivo

Transformar el widget `1 × 1` en un control `3 × 1` que comunique claramente si
está en reposo o grabando.

## Inspeccionar

```text
android/app/src/main/java/com/mvppostit/pensieve/widget/VoiceCaptureWidgetProvider.kt
android/app/src/main/java/com/mvppostit/pensieve/voice/VoiceCaptureService.kt
android/app/src/main/res/layout/widget_voice_capture.xml
android/app/src/main/res/xml/voice_capture_widget_info.xml
android/app/src/main/res/drawable/
android/app/src/main/res/values*/strings.xml
localizacion/TRADUCCIONES_FASE_9.csv
diseño/08_widget_reposo_grabando.png
```

## Estados visuales

### Reposo

- Micrófono dentro de una superficie circular.
- Marca `Nolvida`.
- Texto `Toca para hablar`.
- El widget completo inicia el flujo existente.

### Grabando

- Indicador y control rojo `#E5484D`.
- Texto `Grabando…`.
- Línea de voz estática; no representa niveles reales.
- Símbolo de detener.
- El widget completo envía `ACTION_CANCEL_CAPTURE`.

La descripción accesible debe comunicar `Grabando. Toca para detener`.

## Implementación

1. Usar un único layout `RemoteViews` con dos contenedores y alternar
   `VISIBLE/GONE`.
2. Cambiar metadatos a `targetCellWidth="3"` y `targetCellHeight="1"`, tamaño
   mínimo aproximado `180dp × 48dp`, sin redimensionado.
3. Añadir `previewLayout` con el estado de reposo si no introduce otro layout.
4. Mantener en memoria de proceso solo el estado efímero de la sesión. El
   servicio lo activa tras iniciar foreground y lo limpia en la salida
   idempotente.
5. Actualizar todas las instancias al comenzar y finalizar. No actualizar por
   segundo ni desde WorkManager.
6. Mantener el estado activo durante el breve guardado para impedir que un
   toque parezca iniciar otra captura; `Cancelar` sigue ignorándose cuando
   `saveStarted` ya es verdadero.
7. En esta subfase se usa lavanda. La paleta elegida se conectará en 9.7.

## Conservar

- Servicio foreground no exportado.
- Reconocimiento exclusivamente local.
- Timeout de 60 segundos.
- Fallback mediante alias privado.
- Una única escritura y notificación por resultado.
- Notificación foreground con acción Cancelar.

No añadir Glance, animación real, audio, transcripción parcial ni otro
servicio.

## Comprobaciones

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Revisar en XML y código los dos estados y detenerse antes de 9.3.
