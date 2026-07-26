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

El estado vacío no añade otro botón: las acciones inferiores ya permiten crear
por texto o voz.

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
