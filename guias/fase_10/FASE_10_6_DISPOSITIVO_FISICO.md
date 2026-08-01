# Fase 10.6 — Dispositivo físico

## Objetivo

Aceptar la experiencia real de Nolvida en un teléfono conectado a Android
Studio. Esta subfase es obligatoria para cerrar la fase 10.

## Preparación

El usuario conecta y selecciona el teléfono. Registrar:

- fabricante y modelo;
- versión y nivel API;
- launcher y cuadrícula;
- navegación por gestos o botones;
- idioma, escala de fuente y paleta inicial.

No registrar identificadores personales. Usar notas ficticias y evitar
capturas con información privada.

Ejecutar desde Android Studio sobre el teléfono seleccionado. Como alternativa,
si es el único destino que Gradle debe probar:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

## Matriz física

Repetir la columna física de los flujos `CORE` y los casos `PHY`, especialmente:

- micrófono y reconocimiento local sin red disponible;
- widget en reposo, grabando, cancelando y guardando;
- launcher, padding y tamaño real del widget;
- notificaciones y acción `Hecho` del fabricante;
- teclado, insets, rotación y segundo plano;
- reinicio y recuperación;
- TalkBack y texto grande;
- fluidez perceptible, cierres y posibles ANR.

No añadir herramientas de rendimiento. Si se observa un bloqueo, consumo o
jank reproducible, medir entonces con las herramientas de Android Studio.

## Criterio de cierre

- Tests instrumentados del teléfono registrados.
- Flujos críticos y casos físicos completados.
- Ningún problema visual se corrige sin una reproducción anotada.
- Los fallos quedan preparados para 10.7 con pasos y severidad provisional.
