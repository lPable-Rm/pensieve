# Fase 9.1 — Marca visible y onboarding

## Objetivo

Establecer la base visual de Nolvida y explicar su uso en dos pantallas que
solo aparecen la primera vez.

## Inspeccionar

```text
android/gradle/libs.versions.toml
android/app/build.gradle.kts
android/app/src/main/java/com/mvppostit/pensieve/AppContainer.kt
android/app/src/main/java/com/mvppostit/pensieve/MainActivity.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/theme/
android/app/src/main/res/values*/strings.xml
localizacion/TRADUCCIONES_FASE_8.csv
```

## Crear

```text
localizacion/TRADUCCIONES_FASE_9.csv
android/app/src/main/java/com/mvppostit/pensieve/data/preferences/AppPreferences.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/onboarding/OnboardingScreen.kt
```

## Implementación

1. Añadir únicamente DataStore Preferences `1.2.1`.
2. Crear una única instancia de preferencias con la clave booleana
   `onboarding_completed`; el valor ausente significa `false`.
3. Exponerla desde `AppContainer`, sin ViewModel o repositorio adicional.
4. Actualizar `app_name` a `Nolvida` y sustituir la marca anterior en todos los
   textos localizados. No renombrar identificadores internos.
5. Definir en el tema la paleta lavanda y los colores compartidos de la guía.
   Desactivar color dinámico y mantener un diseño claro coherente.
6. Crear un onboarding Compose de dos páginas, indicadores simples y botones.
   No usar Navigation Compose ni abrir una actividad nueva.

El CSV debe inventariar, como mínimo:

```text
onboarding_capture_title
onboarding_capture_body
onboarding_widget_title
onboarding_widget_body
onboarding_next
onboarding_skip
onboarding_start
onboarding_add_widget
widget_tap_to_speak
widget_recording
widget_stop_recording
empty_reminders_title
empty_reminders_body
appearance_title
appearance_open
palette_lavender
palette_petrol
palette_deep_blue
palette_soft_coral
palette_grayscale
palette_turquoise
```

También incluye `app_name` y cada recurso existente cuyo texto de producto
deba sustituir Pensieve por Nolvida. Los nombres son orientativos, pero deben
quedar estables una vez aprobadas las traducciones.

Contenido:

```text
Página 1
Título: Sácalo de tu cabeza
Cuerpo: Escribe o dicta un recado. Nolvida lo mantendrá visible hasta que
        pulses Hecho.

Página 2
Título: Habla sin abrir Nolvida
Cuerpo: Añade el widget y guarda un recado con un solo toque.
```

Acciones:

- `Siguiente` en la primera página;
- `Omitir` completa el onboarding y abre la pantalla principal;
- `Empezar` completa el onboarding;
- `Añadir widget` usa `requestPinAppWidget()` solo si el launcher lo admite.

Si el launcher no permite fijarlo, ocultar esa acción. No mostrar un error ni
crear una pantalla de configuración.

## Reglas de entrada

- Mientras DataStore entrega el primer valor, no mostrar brevemente el
  onboarding a una persona que ya lo completó.
- Un intent válido del alias privado `WidgetVoiceEntry` abre `HomeRoute`
  directamente y conserva el flujo de permisos de fase 6.
- El onboarding no solicita `RECORD_AUDIO` ni `POST_NOTIFICATIONS`.
- El CSV de fase 9 incluye todos los textos nuevos previstos por la guía en los
  seis idiomas antes de cargarlos en recursos.

## Comprobaciones

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Confirmar que no quedan usos visibles de Pensieve y detenerse antes de 9.2.
