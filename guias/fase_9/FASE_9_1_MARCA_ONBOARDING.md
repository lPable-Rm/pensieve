# Fase 9.1 — Marca visible y onboarding

Estado: **completada y auditada**. No quedan hallazgos altos o medios.

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
localizacion/TRADUCCIONES_FASE_9.csv
diseño/onboarding.png
diseño/logo_nolvida.svg
diseño/logo_nolvida_monocromo.svg
diseño/ilustracion_onboarding.png
```

## Crear

```text
android/app/src/main/java/com/mvppostit/pensieve/data/preferences/AppPreferences.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/onboarding/OnboardingScreen.kt
```

## Bloque 9.1A — Base visual y preferencias

1. Añadir únicamente DataStore Preferences `1.2.1`.
2. Crear una única instancia de preferencias con la clave booleana
   `onboarding_completed`; el valor ausente significa `false`.
3. Exponerla desde `AppContainer`, sin ViewModel o repositorio adicional.
4. Actualizar `app_name` a `Nolvida` y sustituir la marca anterior en todos los
   textos localizados. No renombrar identificadores internos.
5. Definir en el tema la paleta lavanda y los colores compartidos de la guía.
   Desactivar color dinámico y mantener un diseño claro coherente.
6. Convertir el símbolo aprobado a
   `res/drawable/ic_nolvida.xml`, como `VectorDrawable` monocromo, y tintarlo
   desde el tema.
7. Crear `res/drawable-nodpi/` e importar la ilustración sin texto como
   `illustration_onboarding.png`. Usar WebP solo si las herramientas disponibles
   permiten convertirla sin instalar dependencias ni degradarla.
8. No copiar al módulo los bocetos completos, el PNG original del logo ni las
   previsualizaciones.

Comprobar y detenerse antes de 9.1B.

## Bloque 9.1B — Onboarding visual

1. Crear un onboarding Compose de dos páginas, indicadores simples y botones.
2. No usar Navigation Compose ni abrir una actividad nueva.
3. Recrear con Compose los textos, la tarjeta informativa, el widget y la
   notificación de ejemplo. No incrustarlos dentro de una imagen.
4. Usar botones de color plano y únicamente puntos como indicador de página.
5. Adaptar el contenido a texto grande sin alturas rígidas; permitir scroll si
   el espacio vertical no es suficiente.

Comprobar y detenerse antes de 9.1C.

## Bloque 9.1C — Entrada y finalización

1. Conectar `Siguiente`, `Omitir`, `Empezar` y `Añadir widget`.
2. Guardar la finalización una sola vez en DataStore.
3. Mantener la prioridad del alias privado `WidgetVoiceEntry`.
4. Usar `requestPinAppWidget()` solo si el launcher lo admite.

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
Cuerpo: Usa el widget desde la pantalla de inicio y guarda un recado con un
        solo toque.
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
