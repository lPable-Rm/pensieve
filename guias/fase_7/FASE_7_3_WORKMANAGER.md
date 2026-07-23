# Fase 7.3 — WorkManager periódico

## Objetivo

Ejecutar periódicamente la misma reconciliación sin mantener un proceso activo.

## Archivos

```text
android/gradle/libs.versions.toml
android/app/build.gradle.kts
PensieveApplication.kt
AppContainer.kt
recovery/ReminderReconcileWorker.kt
```

Crear el worker y modificar los tres primeros archivos. `AppContainer` solo se
inspecciona.

## Implementación

1. Declarar mediante el catálogo la única dependencia aprobada:

```text
androidx.work:work-runtime:2.11.2
```

2. Crear `ReminderReconcileWorker` como `CoroutineWorker`.
3. Obtener `ReminderManager` desde:

```text
applicationContext -> PensieveApplication -> appContainer
```

4. En `doWork()`, reconciliar una vez, devolver `success()` al terminar y
   `retry()` ante un fallo inesperado. No registrar contenido sensible.
5. Centralizar en el `companion object` la programación periódica:

   - intervalo de 15 minutos;
   - nombre único estable;
   - `ExistingPeriodicWorkPolicy.KEEP`;
   - sin restricciones, foreground ni datos de entrada.

6. Solicitar la programación desde `PensieveApplication.onCreate()`.

Los 15 minutos son un mínimo aproximado; Android decide la hora real.

## No incluir

`work-runtime-ktx`, `work-testing`, worker por nota, Hilt o una clase scheduler
separada.

## Comprobaciones

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Detenerse antes de 7.4.
