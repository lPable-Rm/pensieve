# Fase 8.5 — Auditoría final

## Objetivo

Auditar la fase completa sin modificar código. Los hallazgos se documentarán
para un bloque de correcciones posterior.

## Revisar

- `values/strings.xml` es inglés, completo y funciona como fallback.
- Existen `values-es`, `values-de`, `values-fr`, `values-it` y
  `values-pt-rPT`.
- Cada archivo localizado contiene los 36 `string` traducibles.
- Inglés y alemán incluyen `one` y `other`; español, francés, italiano y
  portugués incluyen también `many`.
- `app_name` solo está en el archivo predeterminado y no es traducible.
- Todos los nombres coinciden con el CSV y con el archivo predeterminado.
- `%1$s`, `%1$d`, las cantidades aplicables, puntuación y elipsis están
  intactos.
- No hay textos visibles escritos directamente en Kotlin o XML.
- Compose, notificaciones, canales, servicio y widget resuelven recursos.
- La voz conserva `Locale.getDefault()` y el reconocimiento local.
- No existe selector, almacenamiento de idioma, `localeConfig` o capa nueva.
- No hay dependencias, cambios de versiones, `INTERNET` ni regresiones en las
  fases 4 a 7.

## Comprobaciones automáticas

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Registrar el número exacto de tests, errores y advertencias. No gestionar el
emulador.

## Matriz manual

Para cada idioma compatible:

- abrir la pantalla principal;
- crear, completar y restaurar una nota;
- revisar entrada manual, voz, errores y accesibilidad;
- revisar la notificación y su acción;
- revisar el nombre y la descripción del widget;
- confirmar que el reconocimiento solicita el idioma general de Android.

Usar también un idioma no compatible para comprobar el fallback inglés. Las
pruebas amplias en API 31, API 36 y dispositivo físico pueden aplazarse a las
fases 10 y 11 si el propietario lo aprueba expresamente.

## Cierre

La fase puede cerrarse cuando no queden hallazgos altos o medios y las pruebas
manuales se completen o se aplacen expresamente. Después se actualizarán
`AGENTS.md`, `CONTEXTO_MANANA.txt` y el estado de esta guía.
