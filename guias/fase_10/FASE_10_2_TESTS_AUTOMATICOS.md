# Fase 10.2 — Tests automáticos críticos

## Objetivo

Proteger el `Mutex` compartido de `ReminderManager` frente a regresiones de
concurrencia, sin modificar producción si el contrato actual ya es correcto.

## Implementación mínima

Añadir en `ReminderManagerTest.kt` dos pruebas deterministas:

1. completar y reconciliar no entran simultáneamente en la sección crítica;
2. restaurar y reconciliar no entran simultáneamente en la sección crítica.

Usar operaciones suspendidas controladas, como `CompletableDeferred`, para:

- detener la primera operación dentro del fake;
- lanzar la segunda;
- demostrar que la segunda todavía no alcanzó el fake;
- liberar la primera;
- confirmar el orden final y el estado coherente.

No usar `Thread.sleep`, retrasos arbitrarios ni depender del planificador. No
exponer el `Mutex`, cambiar su visibilidad o crear una abstracción de producción
solo para la prueba.

Las pruebas de servicio, reconocimiento y widget que necesiten Android real
permanecen en las subfases manuales. No añadir dependencias.

## Comprobaciones

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
```

## Criterio de cierre

- Las dos carreras están cubiertas.
- Todos los tests locales terminan sin fallos ni omitidos inesperados.
- Producción queda intacta o cualquier corrección necesaria se explica y
  audita como un cambio separado.

