# Fase 10.4 — Compatibilidad actual en API 36

## Objetivo

Repetir la validación funcional en Android API 36 y detectar diferencias con
la versión mínima sin alterar la evidencia obtenida en 10.3.

## Preparación

El usuario inicia y selecciona únicamente el emulador API 36 desde Android
Studio. Registrar en la matriz resolución, navegación y launcher. Borrar los
datos de la app debug solo cuando el usuario lo autorice.

Ejecutar los tests desde Android Studio sobre ese emulador. Como alternativa,
si es el único destino disponible para Gradle:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Registrar tests, fallos, errores y omitidos desde el informe real.

## Pruebas manuales

Completar solo la columna `API 36` de todos los casos `CORE`. Prestar especial
atención a:

- permisos de notificaciones y micrófono;
- servicio foreground iniciado por el widget;
- insets, teclado y barras del sistema;
- reinicio y reconciliación;
- posibles diferencias frente a API 31.

No copiar resultados de 10.3. Cada `OK` debe corresponder a una ejecución
real en API 36.

## Regla ante fallos

Marcar `FALLO`, anotar si también sucede en API 31 y conservar los pasos de
reproducción. No corregir durante esta subfase.

## Criterio de cierre

- Tests instrumentados API 36 registrados.
- Toda la columna obligatoria tiene un resultado real.
- Las diferencias entre API 31 y API 36 están documentadas.
