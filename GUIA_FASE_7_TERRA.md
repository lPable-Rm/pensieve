# Fase 7 — Guía de implementación con ChatGPT 5.6 Terra

Estado: **completada y auditada**. La matriz manual queda aplazada a las fases
10 y 11 por decisión de producto.

## Objetivo

Mantener sincronizadas las notas activas de Room y sus notificaciones:

```text
Nota en Room sin notificación -> publicarla de nuevo
Notificación sin nota en Room -> cancelarla
Ambas coinciden              -> no hacer nada
```

Room continúa siendo la única fuente de verdad. La reconciliación se ejecutará:

1. al abrir o volver a Pensieve;
2. periódicamente con WorkManager;
3. después de reiniciar el dispositivo.

WorkManager no mantiene un bucle activo. El intervalo será de 15 minutos, pero
Android puede retrasar cada ejecución para optimizar la batería.

## Subfases

| Subfase | Resultado | Guía |
|---|---|---|
| 7.1 | Motor de reconciliación | [FASE_7_1_RECONCILIACION.md](guias/fase_7/FASE_7_1_RECONCILIACION.md) |
| 7.2 | Ejecución al entrar en la app | [FASE_7_2_PRIMER_PLANO.md](guias/fase_7/FASE_7_2_PRIMER_PLANO.md) |
| 7.3 | WorkManager periódico | [FASE_7_3_WORKMANAGER.md](guias/fase_7/FASE_7_3_WORKMANAGER.md) |
| 7.4 | Recuperación tras reinicio | [FASE_7_4_REINICIO.md](guias/fase_7/FASE_7_4_REINICIO.md) |
| 7.5 | Auditoría final | [FASE_7_5_AUDITORIA.md](guias/fase_7/FASE_7_5_AUDITORIA.md) |

Implementar y revisar una sola subfase cada vez.

## Arquitectura acordada

```text
MainActivity.onStart() --------.
WorkManager periódico ---------|-> ReminderManager.reconcileNotifications()
BOOT_COMPLETED -> worker único-'          |
                                           |-> Room
                                           `-> NotificationManager
```

- `ReminderManager` contiene la única lógica de reconciliación.
- `ReminderNotifier` también expone los ids de recordatorios visibles.
- `ReminderReconcileWorker` reutiliza el `AppContainer`.
- `BootCompletedReceiver` solo encola el worker.

No crear un reconciliador, caso de uso, DAO, repositorio o capa de dominio
adicional.

## Decisiones aprobadas

- Añadir únicamente `androidx.work:work-runtime:2.11.2`.
- No añadir `work-runtime-ktx` ni `work-testing`.
- Obtener la instantánea de Room desde el `Flow` existente.
- Consultar `NotificationManager.activeNotifications`.
- Filtrar el canal de recordatorios, etiquetas nulas e ids positivos.
- Usar un solo `Mutex` en `ReminderManager` para crear, completar, restaurar y
  reconciliar sin carreras.
- Programar un trabajo periódico único de 15 minutos con política `KEEP`.
- No añadir restricciones de red, carga o batería.
- Programarlo desde `PensieveApplication`.
- En `BOOT_COMPLETED`, encolar una ejecución única del mismo worker.
- No usar `LOCKED_BOOT_COMPLETED`.

## Límites

- Sin cambios de interfaz ni del esquema de Room.
- Sin servicios permanentes, alarmas exactas o un worker por nota.
- Sin `INTERNET`, backend, telemetría o datos sensibles en logs.
- Sin dependencias o cambios de versiones adicionales.
- Sin modificar `diseño/`, gestionar el emulador, hacer commit o push.
- `AGENTS.md` y `CONTEXTO_MANANA.txt` se actualizan al cerrar la auditoría.

## Comprobaciones comunes

Ejecutar las que correspondan al cambio:

```powershell
cd android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

## Prompt para Terra

```text
Implementa exclusivamente la subfase [SUBFASE] de Pensieve.

Lee AGENTS.md, CONTEXTO_MANANA.txt, GUIA_FASE_7_TERRA.md, [SUBGUIA] y los
archivos que esta indique. Revisa git status antes de editar.

Mantén Room como fuente de verdad y aplica solo los cambios descritos. La única
dependencia aprobada es androidx.work:work-runtime:2.11.2 y solo corresponde a
7.3. No añadas capas innecesarias, no gestiones el emulador y no hagas commit
ni push.

Ejecuta las comprobaciones de la subguía. Entrega el problema resuelto, archivos
modificados, resultados exactos y riesgos pendientes. Después detente.
```

## Referencias oficiales

- <https://developer.android.com/jetpack/androidx/releases/work>
- <https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work>
- <https://developer.android.com/reference/android/app/NotificationManager#getActiveNotifications()>
- <https://developer.android.com/develop/background-work/background-tasks/broadcasts/broadcast-exceptions>
