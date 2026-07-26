# Fase 9.5 — Entrada manual

## Objetivo

Pulir la entrada manual y las dos acciones inferiores sin convertir el campo
en un elemento permanente.

## Inspeccionar

```text
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeScreen.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeUiState.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeViewModel.kt
android/app/src/main/res/drawable/ic_add.xml
android/app/src/main/res/drawable/ic_mic.xml
diseño/03_entrada_manual.png
```

## Implementación

1. Extraer `ManualReminderInput` a su propio archivo de componentes.
2. Mantener la burbuja compacta sobre el teclado o la lista:
   - foco y teclado automáticos;
   - hasta tres líneas visibles;
   - `Cancelar` y `Guardar`;
   - acción IME `Done`;
   - guardado deshabilitado para texto vacío.
3. Usar colores, tipografía y formas del tema; no duplicar hexadecimales.
4. Extraer `HomeActions` solo si deja `HomeScreen` como coordinador legible.
5. Mantener:
   - botón tonal `Nueva nota`;
   - botón circular de micrófono como acción principal rápida;
   - estados deshabilitados actuales.
6. El campo desaparece después de guardar o cancelar y nunca ocupa espacio en
   reposo.

## Límites

- Sin fecha, prioridad, categoría, título o campos adicionales.
- Sin autoguardado, borradores persistentes o nueva validación.
- Sin cambios en `HomeViewModel` salvo los necesarios para conservar el mismo
  contrato al extraer componentes.

## Comprobaciones

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Conservar foco, teclado, IME y protección frente a doble guardado. Detenerse
antes de 9.6.
