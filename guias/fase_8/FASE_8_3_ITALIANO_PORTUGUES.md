# Fase 8.3 — Italiano y portugués de Portugal

## Objetivo

Completar los idiomas aprobados con italiano y portugués europeo.

## Archivos

```text
localizacion/TRADUCCIONES_FASE_8.csv
android/app/src/main/res/values-it/strings.xml
android/app/src/main/res/values-pt-rPT/strings.xml
```

## Implementación

1. Crear `values-it/strings.xml` desde la columna `italiano`.
2. Crear `values-pt-rPT/strings.xml` desde `portugues_portugal`.
3. Incluir en cada archivo los 36 `string` traducibles y
   `voice_elapsed_seconds` con `one`, `many` y `other`.
4. Omitir `app_name` y conservar los marcadores y la puntuación del CSV.
5. Mantener expresiones europeas como `lembrete`, `Definições`, `detetada` y
   las construcciones `A ouvir…`, `A transcrever…` y `A guardar…`.

El directorio debe llamarse exactamente `values-pt-rPT`. No crear
`values-pt`, `values-pt-rBR` ni una segunda variante portuguesa.

## No incluir

Cambios de Kotlin, interfaz, Gradle, Manifest o traducciones adicionales.

## Comprobaciones

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Detenerse antes de 8.4.
