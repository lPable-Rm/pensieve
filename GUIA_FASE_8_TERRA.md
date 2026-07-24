# Fase 8 — Índice de implementación con ChatGPT 5.6 Terra

Estado: **diseñada y pendiente de implementación**.

## Objetivo

Localizar Pensieve mediante los recursos nativos de Android para que siga
automáticamente el idioma general del sistema.

Idiomas aprobados:

- inglés como idioma predeterminado y fallback;
- español;
- alemán;
- francés;
- italiano;
- portugués de Portugal.

Este alcance amplía únicamente la mención provisional a español e inglés de
`AGENTS.md` y `CONTEXTO_MANANA.txt`. El CSV y esta guía recogen la decisión de
producto más reciente; ambos documentos generales se consolidarán al cerrar
la auditoría de la fase.

La localización debe cubrir la interfaz, los mensajes de error, las
notificaciones, el widget y la captura por voz. No se añadirá un selector de
idioma dentro de Pensieve ni una preferencia propia.

## Fuente de las traducciones

Usar exclusivamente:

```text
localizacion/TRADUCCIONES_FASE_8.csv
```

El CSV contiene 37 recursos `string` y tres filas para el único `plural`:
`one`, `other` y la forma `many` que solo requieren español, francés, italiano
y portugués. También identifica `app_name` como no traducible. No volver a
traducir, resumir ni reinterpretar los textos durante la implementación.

## Subfases

| Subfase | Resultado | Guía |
|---|---|---|
| 8.1 | Inglés predeterminado y recursos españoles | [FASE_8_1_BASE_INGLES_ESPANOL.md](guias/fase_8/FASE_8_1_BASE_INGLES_ESPANOL.md) |
| 8.2 | Recursos alemanes y franceses | [FASE_8_2_ALEMAN_FRANCES.md](guias/fase_8/FASE_8_2_ALEMAN_FRANCES.md) |
| 8.3 | Recursos italianos y portugueses | [FASE_8_3_ITALIANO_PORTUGUES.md](guias/fase_8/FASE_8_3_ITALIANO_PORTUGUES.md) |
| 8.4 | Integración con Android y captura por voz | [FASE_8_4_INTEGRACION_SISTEMA.md](guias/fase_8/FASE_8_4_INTEGRACION_SISTEMA.md) |
| 8.5 | Auditoría final | [FASE_8_5_AUDITORIA.md](guias/fase_8/FASE_8_5_AUDITORIA.md) |

Implementar, comprobar y revisar una sola subfase cada vez.

## Estructura acordada

```text
res/
├── values/strings.xml          # Inglés completo y fallback
├── values-es/strings.xml       # Español
├── values-de/strings.xml       # Alemán
├── values-fr/strings.xml       # Francés
├── values-it/strings.xml       # Italiano
└── values-pt-rPT/strings.xml   # Portugués de Portugal
```

Android seleccionará automáticamente el recurso adecuado:

```text
Idioma general de Android
        |
        |-> Compose: stringResource / pluralStringResource
        |-> notificaciones y servicio: Context.getString
        |-> Manifest y widget: @string/...
        `-> voz: Locale.getDefault() -> SpeechRecognizer local
```

Si Android usa un idioma sin traducción, la interfaz recurrirá al inglés
completo de `values/strings.xml`.

## Decisiones aprobadas

- Mantener completo el archivo predeterminado `values/strings.xml`.
- Marcar `app_name` con `translatable="false"` y conservar `Pensieve`.
- No duplicar `app_name` en los archivos localizados.
- Conservar exactamente `%1$s`, `%1$d`, las categorías `one` y `other`, la
  categoría `many` en los cuatro idiomas indicados y el carácter de elipsis
  `…`.
- Usar `values-pt-rPT`; no crear `values-pt` ni recursos brasileños.
- Mantener `Locale.getDefault().toLanguageTag()` para el reconocimiento local.
- No crear cargadores de traducciones, mapas de idiomas o clases nuevas.
- No activar `generateLocaleConfig` ni añadir `android:localeConfig`: Pensieve
  seguirá el idioma general de Android y no tendrá un idioma independiente.
- No añadir DataStore, AppCompat, dependencias ni cambios de versiones.
- No rediseñar la interfaz para acomodar textos largos durante esta fase. Los
  posibles ajustes visuales pertenecen a las fases 9 y 10.

## Límites

- Sin cambios funcionales en Room, notificaciones, voz, widget o recuperación.
- Sin permiso `INTERNET`, reconocimiento online ni telemetría.
- Sin selector de idioma ni persistencia de una preferencia lingüística.
- Sin modificar `diseño/`, gestionar el emulador, hacer commit o push.
- Los comentarios del código pueden permanecer en español: no son texto de
  producto y no deben convertirse en recursos.
- `AGENTS.md` y `CONTEXTO_MANANA.txt` se actualizarán al cerrar la auditoría.

## Comprobaciones comunes

Ejecutar desde `android/` las que indique cada subguía:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

La fase no debe introducir errores ni advertencias nuevas de lint.

## Prompt base para Terra

Sustituir `[SUBFASE]` y `[SUBGUIA]` antes de enviarlo:

```text
Implementa exclusivamente la subfase [SUBFASE] de la fase 8 de Pensieve.

Lee completamente AGENTS.md, CONTEXTO_MANANA.txt, GUIA_FASE_8_TERRA.md,
[SUBGUIA] y localizacion/TRADUCCIONES_FASE_8.csv. Después inspecciona solo los
archivos indicados por la subguía y revisa git status.

Usa el CSV como fuente literal de las traducciones. Mantén el inglés completo
en values/ como fallback, no añadas un selector de idioma, localeConfig,
dependencias ni abstracciones de localización. No gestiones el emulador y no
hagas commit ni push.

Implementa únicamente el alcance de [SUBFASE], ejecuta sus comprobaciones y
después detente. Entrega los archivos modificados, los resultados exactos y
cualquier riesgo o prueba manual pendiente. No continúes con la subfase
siguiente.
```

## Referencias oficiales

- <https://developer.android.com/guide/topics/resources/localization>
- <https://developer.android.com/guide/topics/resources/providing-resources>
- <https://developer.android.com/guide/topics/resources/app-languages>
- <https://developer.android.com/reference/android/speech/RecognizerIntent>
