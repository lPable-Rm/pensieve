# Fase 7.4 — Reinicio del dispositivo

## Objetivo

Encolar una reconciliación al terminar el arranque normal del dispositivo.

## Archivos

```text
AndroidManifest.xml
recovery/ReminderReconcileWorker.kt
recovery/BootCompletedReceiver.kt
```

## Implementación

1. Declarar `RECEIVE_BOOT_COMPLETED`.
2. Crear un receiver exportado con filtro para `BOOT_COMPLETED`.
3. Validar exactamente esa acción en `onReceive()`.
4. Añadir al worker un método que encole una ejecución única con nombre estable
   y `ExistingWorkPolicy.KEEP`.
5. El receiver llama a ese método y termina inmediatamente.

El receiver no consulta Room. El worker reutiliza la lógica y el contenedor
existentes.

## No incluir

`LOCKED_BOOT_COMPLETED`, `goAsync()`, corrutinas en el receiver, AlarmManager,
servicios o apertura de `MainActivity`.

Tras una detención forzada, Android puede suspender trabajos y broadcasts hasta
que el usuario vuelva a abrir la aplicación.

## Comprobaciones

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Revisar el Manifest fusionado: receiver y permiso correctos, sin `INTERNET`.
Detenerse antes de 7.5.
