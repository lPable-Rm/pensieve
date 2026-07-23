# Fase 7.2 — Entrada en primer plano

## Objetivo

Reconciliar al abrir Pensieve y cada vez que `MainActivity` vuelva a primer
plano.

## Archivos

```text
MainActivity.kt
PensieveApplication.kt
AppContainer.kt
ReminderManager.kt
```

Modificar únicamente `MainActivity.kt`.

## Implementación

1. Convertir el acceso actual a `ReminderManager` en una propiedad `lazy`
   privada y reutilizarla en `HomeRoute`.
2. En `onStart()`, ejecutar `reconcileNotifications()` con `lifecycleScope`.
3. Conservar el `Job` y no iniciar otro mientras siga activo.
4. Contener un fallo para que la aplicación no se cierre. La recuperación es
   silenciosa y no necesita snackbar.
5. No cancelar el trabajo solo porque la actividad pase a segundo plano.

Usar `onStart`, no `onCreate`, permite detectar también el regreso desde otra
aplicación.

## Comprobaciones

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat lintDebug
```

Detenerse antes de 7.3.
