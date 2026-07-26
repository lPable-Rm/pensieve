# Fase 9.4 — Lista y tarjetas

## Objetivo

Pulir la lista de recordatorios y comunicar con claridad que no hay nada
pendiente cuando Room está vacío.

## Inspeccionar

```text
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeScreen.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeUiState.kt
android/app/src/main/res/values*/strings.xml
localizacion/TRADUCCIONES_FASE_9.csv
diseño/02_estado_vacio.png
diseño/05_snackbar_completado.png
diseño/06_animacion_salida_tarjeta.png
```

## Implementación

1. Extraer `ReminderCard` a `ui/home/components/ReminderCard.kt`.
2. Mantener texto, fecha y una sola acción de completar.
3. Usar superficie blanca, radio coherente, sombra baja o borde sutil y un
   mínimo táctil de `48dp`.
4. No añadir punto decorativo, prioridad, menú, selección o gesto oculto.
5. Conservar orden reciente, claves estables, salida animada y snackbar con
   Deshacer.
6. Mostrar un estado vacío sencillo cuando no haya recordatorios y no exista
   una entrada activa:

```text
Título: Nada pendiente
Cuerpo: Cuando guardes un recado, aparecerá aquí y en tus notificaciones.
```

La tarjeta `Toca para hablar` del estado vacío es una acción real, aparece solo
en ese estado y reutiliza el mismo callback de voz que el botón inferior. No
añade estado ni lógica de captura propios.

## Calidad

- Fecha formateada con el locale del sistema.
- Texto largo ajustable sin truncar la acción.
- Descripción de completar conserva el contenido del recordatorio.
- Si se crea `EmptyReminderState.kt`, no crear además una capa o modelo.

## Comprobaciones

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Añadir previews de lista vacía y texto largo. Detenerse antes de 9.5.
