# Fase 8.2 — Alemán y francés

## Objetivo

Añadir alemán y francés reutilizando exactamente la estructura validada en
8.1.

## Archivos

```text
localizacion/TRADUCCIONES_FASE_8.csv
android/app/src/main/res/values-de/strings.xml
android/app/src/main/res/values-fr/strings.xml
```

## Implementación

1. Crear `values-de/strings.xml` desde la columna `aleman`.
2. Crear `values-fr/strings.xml` desde la columna `frances`.
3. Incluir en cada archivo los 36 `string` traducibles.
4. En alemán, declarar `voice_elapsed_seconds` con `one` y `other`.
5. En francés, declarar `voice_elapsed_seconds` con `one`, `many` y `other`.
6. No incluir `app_name`: debe resolverse desde el recurso predeterminado.
7. Conservar exactamente `%1$s`, `%1$d`, signos, acentos y elipsis.

No corregir las traducciones sobre la marcha. Si aparece una contradicción
real con el CSV, detenerse y documentarla antes de editarlo.

## No incluir

Italiano, portugués, Kotlin, interfaz, Gradle o configuración de idioma.

## Comprobaciones

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Detenerse antes de 8.3.
