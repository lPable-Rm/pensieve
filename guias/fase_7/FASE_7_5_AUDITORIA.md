# Fase 7.5 — Auditoría final

Estado: **superada sin hallazgos altos o medios**. La matriz manual queda
aplazada a las fases 10 y 11.

## Objetivo

Auditar la fase completa sin modificar código. Los fallos encontrados se
documentarán para un bloque posterior.

## Revisar

- Room determina las notificaciones esperadas.
- Se publican ausentes, se cancelan huérfanas y se ignoran coincidencias.
- Las notificaciones negativas del servicio de voz quedan excluidas.
- El `Mutex` evita que una reconciliación resucite una nota completada.
- `MainActivity` no solapa trabajos.
- Worker y receiver reutilizan `ReminderManager`.
- Los trabajos periódico y de reinicio son únicos.
- El receiver solo encola trabajo tras `BOOT_COMPLETED`.
- No hay `INTERNET`, datos sensibles, servicios nuevos ni capas innecesarias.
- No existen regresiones en las fases 4, 5 y 6.

## Comprobaciones automáticas

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

No gestionar el emulador.

## Matriz manual

- Descartar una notificación y volver a la app: debe reaparecer.
- Repetir con varias notas: no debe haber duplicados.
- Completar y deshacer: Room y notificación deben coincidir.
- Bloquear permiso o canal: las notas deben conservarse sin cierres.
- Reactivarlos y entrar en la app: deben recuperarse las notificaciones.
- Descartar una notificación y esperar al worker, recordando que puede tardar
  más de 15 minutos.
- Reiniciar y desbloquear: debe recuperarse sin abrir `MainActivity`.
- Verificar que abrir varias veces la app no crea trabajos duplicados.

Las pruebas amplias en API 31, API 36 y dispositivo físico pueden aplazarse a
las fases 10 y 11 si el propietario lo decide expresamente.

## Cierre

La fase puede cerrarse si las comprobaciones son correctas, no quedan hallazgos
altos o medios y las pruebas manuales se realizan o aplazan expresamente.
Después se actualizarán `AGENTS.md` y `CONTEXTO_MANANA.txt`.
