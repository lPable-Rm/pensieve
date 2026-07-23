# Fase 6.5 — Pruebas y auditoría independiente

## Objetivo

Validar el flujo completo sin modificar código. Si aparecen fallos, se documentan y se corrigen posteriormente en la subfase 6.6.

## Auditoría de código

Revisar:

- ciclo de vida completo de `VoiceCaptureService`;
- promoción inmediata a foreground;
- `START_NOT_STICKY`;
- limpieza idempotente y liberación del micrófono;
- protección frente a doble guardado y pulsaciones repetidas;
- servicio no exportado;
- receiver y `PendingIntent` explícitos e inmutables;
- ausencia de audio, logs sensibles e `INTERNET`;
- Room como fuente de verdad;
- uso del `ReminderManager` existente;
- accesibilidad del widget;
- ausencia de dependencias y capas innecesarias;
- regresiones en el flujo de fase 5.

## Pruebas automáticas

Ejecutar:

```powershell
cd android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Ejecutar `connectedDebugAndroidTest` únicamente si existen pruebas instrumentadas relevantes y el desarrollador ya ha proporcionado un dispositivo disponible. Terra no gestiona el emulador.

## Matriz manual para el desarrollador

### Widget

- Añadirlo desde el launcher.
- Comprobar tamaño, icono, etiqueta y área pulsable.
- Probar una y varias instancias.

### Camino normal

- Con permisos preparados y la app fuera de pantalla, pulsar el widget.
- Confirmar que `MainActivity` no aparece.
- Confirmar bip, indicador de micrófono y notificación `Escuchando…`.
- Hablar y esperar el final del habla.
- Abrir la app después y comprobar una única nota.
- Confirmar una única notificación normal y la retirada de la temporal.

### Permisos

- Primer uso sin `RECORD_AUDIO`.
- Rechazar y conceder el permiso.
- Revocarlo después de instalar el widget.
- Bloquear notificaciones, canal de captura y canal de recordatorios.
- Confirmar que el fallback abre la app solo en estos casos.

### Errores y robustez

- Pulsar rápidamente varias veces.
- Cancelar antes de hablar.
- No hablar hasta el timeout.
- Probar en modo avión.
- Probar sin motor local disponible.
- Interrumpir el micrófono con otra aplicación o llamada.
- Detener el servicio desde Android.
- Matar el proceso normalmente y volver a usar el widget.
- Reiniciar el dispositivo.

`Forzar detención` no equivale a cerrar la app: Android bloquea sus componentes hasta que el usuario la abre de nuevo.

### Privacidad

- Revisar Logcat: no debe contener notas ni transcripciones.
- Comprobar que no aparecen archivos de audio.
- Comprobar que la notificación temporal no muestra lo pronunciado.
- Confirmar que Manifest no incluye `INTERNET`.

## Compatibilidad y publicación

- Comprobar API 36 y al menos API 31.
- Dejar para las fases 10 y 11 la matriz amplia de dispositivos físicos.
- Anotar para la fase 11 la declaración del foreground service de micrófono en Play Console y la demostración del flujo requerida por Google Play.

## Criterios de cierre

- [ ] El camino normal no abre la app.
- [ ] La escucha siempre es visible y cancelable; el guardado ya aceptado es visible y no se interrumpe.
- [ ] El reconocimiento es local.
- [ ] Cada pulsación válida crea como máximo una nota.
- [ ] Cancelación, error, vacío y timeout no guardan.
- [ ] Todos los caminos liberan el micrófono.
- [ ] Los permisos ausentes usan el fallback visible.
- [ ] No hay regresiones en fases anteriores.
- [ ] Tests, compilación, ensamblado y lint son correctos.
- [ ] Las pruebas manuales acordadas son satisfactorias.
- [ ] El código sigue siendo simple y didáctico.

La auditoría encontró problemas que deben resolverse siguiendo `FASE_6_6_CORRECCIONES_AUDITORIA.md`. No actualizar todavía `AGENTS.md` ni `CONTEXTO_MANANA.txt`.
