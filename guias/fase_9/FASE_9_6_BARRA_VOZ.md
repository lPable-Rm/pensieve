# Fase 9.6 — Barra de voz

## Objetivo

Dar una representación profesional y coherente a todos los estados de voz
dentro de la pantalla principal sin cambiar su máquina de estados.

## Inspeccionar

```text
android/app/src/main/java/com/mvppostit/pensieve/ui/home/components/VoiceInputBar.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeUiState.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeViewModel.kt
diseño/04_flujo_voz.png
```

## Estados

- `Listening`: indicador rojo, tiempo, parcial visible y control de detener.
- `Processing`: actividad breve y acción Cancelar.
- `Review`: texto editable, Cancelar y Guardar.
- `Saving`: indicador y texto estable.
- `Error`: mensaje y únicamente las acciones resolubles actuales.

## Implementación

1. Mantener una única superficie compacta de radio y elevación coherentes con
   las tarjetas.
2. Sustituir el progreso genérico de escucha por:
   - punto rojo o pulso breve;
   - línea o barras visuales que no pretendan medir el audio;
   - control rojo con símbolo de detener.
3. El texto parcial sigue siendo contenido visual no anunciado continuamente
   por TalkBack.
4. Conservar el contador accesible y los `liveRegion` solo para estados
   estables.
5. Usar `MaterialTheme`; el rojo de grabación no cambia con la paleta.
6. Mantener funciones pequeñas y un composable por estado. No mover permisos,
   SpeechRecognizer o persistencia a este archivo.

## Límites

- Sin pantalla completa de voz.
- Sin niveles reales de audio, almacenamiento o visualizador complejo.
- Sin cambios en transiciones, reintentos, revisión o guardado.
- Sin animación infinita salvo un pulso ligero que respete accesibilidad.

## Comprobaciones

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Conservar o ampliar los tests instrumentados existentes de `VoiceInputBar`.
Detenerse antes de 9.7.
