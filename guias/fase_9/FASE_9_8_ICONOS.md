# Fase 9.8 — Iconos y superficies del sistema

## Objetivo

Unificar la identidad gráfica de Nolvida en launcher, aplicación, widget y
notificaciones sin introducir iconos decorativos.

## Punto de aprobación obligatorio

Antes de reemplazar el launcher, presentar al usuario propuestas simples del
símbolo de Nolvida y esperar su elección. Si no existe una propuesta aprobada,
no inventar una como cambio definitivo.

El símbolo debe:

- ser original y reconocible a tamaño pequeño;
- funcionar en color y monocromo;
- evitar referencias protegidas a Harry Potter;
- comunicar recuerdo, descarga mental o visibilidad sin parecer calendario;
- conservar una silueta clara sobre distintos fondos.

## Inspeccionar

```text
android/app/src/main/res/drawable/ic_*.xml
android/app/src/main/res/drawable/ic_launcher_*.xml
android/app/src/main/res/mipmap-*/
android/app/src/main/res/mipmap-anydpi/
android/app/src/main/AndroidManifest.xml
android/app/src/main/java/com/mvppostit/pensieve/notifications/
android/app/src/main/java/com/mvppostit/pensieve/voice/VoiceCaptureService.kt
android/app/src/main/res/xml/voice_capture_widget_info.xml
```

## Implementación tras aprobación

1. Crear un icono adaptativo con fondo de marca y foreground seguro dentro de
   la zona visible.
2. Proporcionar icono redondo y variante monocroma cuando corresponda.
3. Mantener el icono pequeño de notificación como silueta blanca, sin color
   incrustado ni detalles finos.
4. Revisar `ic_add`, `ic_mic`, completar, detener, apariencia y onda para que
   compartan grosor y tamaño óptico.
5. Añadir una vista previa del widget en reposo para el selector de Android.
6. Mantener las notificaciones nativas: no crear layouts personalizados.

## Límites

- No usar paquetes de iconos o imágenes con licencia dudosa.
- No añadir dependencias.
- No modificar `diseño/`.
- No convertir cada elemento visual en un icono.
- No cambiar canales, PendingIntent o contenido de las notificaciones.

## Comprobaciones

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Revisar recursos adaptativos, monocromos y small icon antes de 9.9.
