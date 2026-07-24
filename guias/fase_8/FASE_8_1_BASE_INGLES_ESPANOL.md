# Fase 8.1 — Inglés predeterminado y español

## Objetivo

Crear la base completa de localización sin cambiar el comportamiento de la
aplicación.

## Archivos

```text
localizacion/TRADUCCIONES_FASE_8.csv
android/app/src/main/res/values/strings.xml
android/app/src/main/res/values-es/strings.xml
```

El CSV solo se consulta. No debe modificarse en esta subfase.

## Implementación

1. Sustituir en `values/strings.xml` los textos españoles por la columna
   `ingles` del CSV.
2. Mantener en ese archivo todos los recursos: 37 elementos `string` y un
   `plurals` con `one` y `other`. El inglés no utiliza `many`.
3. Marcar únicamente:

```xml
<string name="app_name" translatable="false">Pensieve</string>
```

4. Crear `values-es/strings.xml` con los 36 `string` traducibles y las formas
   `one`, `many` y `other` del plural de la columna `espanol`.
5. Omitir `app_name` del archivo español para reutilizar la marca del fallback.
6. Preservar nombres, orden lógico, `%1$s`, `%1$d`, puntuación y elipsis.

## No incluir

Otros idiomas, Kotlin, Manifest, Gradle, `localeConfig` o pruebas nuevas.

## Comprobaciones

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Confirmar que `values/strings.xml` sigue siendo completo y detenerse antes de
8.2.
