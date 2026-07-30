# Fase 10.3 — Compatibilidad mínima en API 31

## Objetivo

Ejecutar los tests instrumentados y los flujos críticos en la versión mínima
compatible, sin mezclar resultados con otros dispositivos.

## Preparación

El usuario inicia y selecciona únicamente el emulador API 31 desde Android
Studio. Registrar en la matriz resolución, navegación y launcher. Borrar los
datos de la app debug solo cuando el usuario lo autorice para probar onboarding.

Ejecutar los tests desde Android Studio sobre ese emulador. Como alternativa,
si es el único destino disponible para Gradle:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Registrar tests, fallos, errores y omitidos desde el informe real.

## Pruebas manuales

Completar solo la columna `API 31` de los casos `CORE`:

- instalación limpia y onboarding;
- texto, completar y deshacer;
- notificaciones y reconciliación;
- voz dentro de la aplicación;
- widget directo, cancelación y fallback de permisos;
- reinicio, permisos y canal;
- pulsaciones rápidas y ausencia de duplicados.

Usar textos de prueba neutros. Una nota guardada debe producir exactamente una
fila activa y una notificación normal. WorkManager puede ejecutarse después de
los 15 minutos mínimos; registrar el tiempo observado.

## Regla ante fallos

Marcar `FALLO` con pasos y resultado observado. Se puede diagnosticar en solo
lectura, pero no corregir durante esta subfase.

## Criterio de cierre

- Tests instrumentados API 31 registrados.
- Toda la columna obligatoria tiene un resultado real.
- Ningún fallo se convierte en `OK` sin repetir el caso.

