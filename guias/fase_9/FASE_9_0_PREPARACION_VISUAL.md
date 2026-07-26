# Fase 9.0 — Preparación visual

Estado: **completada**.

## Objetivo

Consolidar las referencias definitivas de Nolvida antes de modificar recursos
o código Android. Esta subfase no implementa interfaz ni cambia los flujos
funcionales auditados.

## Referencias actuales

```text
diseño/onboarding.png
diseño/01_selector_paletas.png
diseño/02_estado_vacio.png
diseño/03_entrada_manual.png
diseño/04_flujo_voz.png
diseño/05_snackbar_completado.png
diseño/06_animacion_salida_tarjeta.png
diseño/07_notificacion_nativa.png
diseño/08_widget_reposo_grabando.png
```

Los archivos de `diseño/diseñoAntiguo/` quedan archivados como contexto
histórico. No son referencias para implementar la fase 9.

## Recursos maestros

```text
diseño/logo.png
diseño/logo_nolvida.svg
diseño/logo_nolvida_monocromo.svg
diseño/logo_nolvida_preview.png
diseño/ilustracion_onboarding.png
```

- Los dos SVG usan una única tinta y fondo transparente.
- Compose y el widget aplicarán el color de la paleta activa sobre el símbolo.
- El launcher mantendrá una identidad fija; no cambiará con la preferencia
  interna.
- Android controla el color final del icono pequeño de notificación.
- `ilustracion_onboarding.png` es el maestro de `1024 × 1024`, transparente y
  sin texto. En 9.1A se importará como recurso Android optimizado sin añadir
  una dependencia de imágenes.

## Empaquetado Android

Google Play no recibe la carpeta del repositorio. Recibe el Android App Bundle
generado por el único módulo `android/app`. Solo los recursos situados dentro
de su source set se incluyen en la aplicación:

```text
android/app/src/main/
├── AndroidManifest.xml
├── java/
└── res/
    ├── drawable/
    ├── drawable-nodpi/
    ├── layout/
    ├── mipmap-*/
    ├── values*/
    └── xml/
```

Mapa previsto para 9.1A:

```text
diseño/logo_nolvida.svg
    -> android/app/src/main/res/drawable/ic_nolvida.xml

diseño/ilustracion_onboarding.png
    -> android/app/src/main/res/drawable-nodpi/illustration_onboarding.png
       o illustration_onboarding.webp si puede optimizarse sin degradarla
```

- Android no carga el SVG maestro directamente: se convierte a
  `VectorDrawable`.
- `drawable-nodpi` evita que Android aplique un escalado automático por
  densidad al único raster de ilustración; Compose define su tamaño.
- No se copian `onboarding.png`, los ocho bocetos, `logo.png` ni las
  previsualizaciones al módulo.
- El launcher se prepara en 9.8 dentro de `drawable/` y `mipmap-*`.
- El icono pequeño de notificación sigue siendo un `VectorDrawable` en
  `drawable/`.
- El widget reutiliza el vector desde su layout `RemoteViews`; no necesita otra
  copia bitmap.

El icono de la ficha de Google Play, las capturas y el feature graphic no
forman parte del AAB. Se prepararán en fase 11 y se subirán por separado a Play
Console.

## Decisiones de implementación

- Los bocetos completos no se empaquetan dentro de la aplicación.
- Los textos, botones, widget y notificación dibujados en ellos se recrean con
  Compose, `RemoteViews` o APIs nativas y recursos localizados.
- El onboarding mantiene dos páginas, botones de color plano e indicadores de
  puntos. No implementa los cuadrados decorativos `1` y `2`.
- El onboarding no solicita permisos. `Añadir widget` usa la confirmación
  nativa de Android cuando el launcher la admite.
- El texto del widget habla de la **pantalla de inicio**, no de cualquier
  pantalla.
- La tarjeta `Toca para hablar` del estado vacío es una acción real y reutiliza
  exactamente el mismo callback de voz que el botón de micrófono.
- La línea de voz es una representación estática, no mide niveles de audio.
- La notificación del boceto es una referencia de contenido; se conserva el
  layout nativo de Android.
- La animación al completar es breve y termina con el snackbar y su acción
  `Deshacer`.

## Localización

`localizacion/TRADUCCIONES_FASE_9.csv` inventaría antes del código:

- los textos nuevos de onboarding, estado vacío, widget y apariencia;
- los textos de voz modificados por el nuevo diseño;
- los recursos existentes que sustituyen Pensieve por Nolvida;
- inglés como fallback, español, alemán, francés, italiano y portugués de
  Portugal.

No se deben copiar literalmente textos visibles desde los PNG.

## Criterio de cierre

- Las referencias nuevas tienen nombres estables.
- Las guías enlazan únicamente recursos actuales.
- El logo vectorial es monocromo y no contiene colores de una paleta.
- El CSV de fase 9 existe antes de cargar textos en Android.
- No se ha modificado código, Manifest, Gradle ni recursos de producción.
