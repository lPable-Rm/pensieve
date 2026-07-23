# Fase 7.1 — Motor de reconciliación

## Objetivo

Comparar Room con las notificaciones visibles y reparar sus diferencias.

## Archivos

```text
reminders/ReminderManager.kt
notifications/ReminderNotificationPublisher.kt
data/repository/ReminderRepository.kt
ReminderManagerTest.kt
```

Modificar todos salvo `ReminderRepository`, que solo debe inspeccionarse.

## Implementación

1. Añadir a `ReminderNotifier`:

```kotlin
fun activeReminderIds(): Set<Long>
```

2. Obtener esos ids desde `NotificationManager.activeNotifications`, filtrando:

   - `ReminderNotificationChannel.CHANNEL_ID`;
   - etiqueta nula;
   - id positivo.

3. Añadir `ReminderManager.reconcileNotifications()`:

   - tomar el primer valor del `Flow` existente;
   - publicar notas cuyos ids no estén activos;
   - cancelar ids activos que no existan en Room;
   - ignorar coincidencias.

4. Proteger crear, completar, restaurar y reconciliar con un único `Mutex`
   privado. No capturar los fallos dentro del gestor.

5. Probar como mínimo: notificación ausente, notificación huérfana,
   coincidencia completa y varias diferencias. Los tests anteriores deben
   seguir pasando.

## No incluir

MainActivity, WorkManager, Manifest, DAO nuevo o capas adicionales.

## Comprobaciones

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat lintDebug
```

Detenerse antes de 7.2.
