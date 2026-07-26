# Fase 9.7 — Paletas

## Objetivo

Permitir elegir una de las seis paletas aprobadas, guardarla localmente y
aplicarla de inmediato a Compose y al widget.

## Inspeccionar

```text
android/app/src/main/java/com/mvppostit/pensieve/data/preferences/AppPreferences.kt
android/app/src/main/java/com/mvppostit/pensieve/MainActivity.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/theme/
android/app/src/main/java/com/mvppostit/pensieve/ui/home/components/NolvidaHeader.kt
android/app/src/main/java/com/mvppostit/pensieve/widget/VoiceCaptureWidgetProvider.kt
android/app/src/main/res/values*/strings.xml
localizacion/TRADUCCIONES_FASE_9.csv
```

## Crear

```text
android/app/src/main/java/com/mvppostit/pensieve/ui/appearance/AppearancePaletteSheet.kt
```

## Implementación

1. Representar las seis opciones con un enum o value class pequeña y un id
   estable:
   `lavender`, `petrol`, `deep_blue`, `soft_coral`, `grayscale`, `turquoise`.
2. Añadir a `AppPreferences` la clave `color_palette`; un valor ausente o
   desconocido usa Lavanda.
3. Construir los `ColorScheme` desde una única función exhaustiva, usando la
   tabla de la guía principal.
4. Recoger la preferencia en la raíz y aplicarla sin recrear Room, ViewModel o
   actividad.
5. Añadir a la cabecera una acción real de apariencia y abrir un
   `ModalBottomSheet` sobre la pantalla principal.
6. Mostrar seis filas con muestra de color, nombre y estado seleccionado. El
   cambio se guarda al pulsar y se aplica inmediatamente.
7. Actualizar todas las instancias del widget con la misma paleta. Sus estados
   de reposo y grabación no cambian de comportamiento.

## Límites

- No crear una pantalla de ajustes ni Navigation Compose.
- No permitir colores libres.
- No guardar colores hexadecimales: guardar solo el id.
- No añadir selector de modo claro/oscuro/automático.
- No volver a añadir color dinámico.
- No usar DataStore para notas o contenido sensible.

## Comprobaciones

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Comprobar fallback a Lavanda y que el widget no use rojo como color de paleta.
Detenerse antes de 9.8.
