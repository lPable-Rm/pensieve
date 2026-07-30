# Fase 10 — Matriz de pruebas

Estado: **pendiente de ejecución**.

## Leyenda

- `PENDIENTE`: todavía no ejecutada.
- `OK`: resultado esperado confirmado.
- `FALLO`: resultado diferente y reproducible.
- `BLOQUEADA`: falta una condición externa.
- `NO_APLICA`: justificar en notas.

No escribir `OK` por inferencia, compilación o resultado de otro dispositivo.

## Línea base 10.0

| Dato | Resultado |
|---|---|
| Fecha de medición | 2026-07-30 |
| Rama | `main` |
| Variante debug | `versionName 1.0`, `versionCode 1` |
| SDK | `minSdk 31`, `targetSdk 36`, `compileSdk 36` |
| Tests unitarios | 15 ejecutados, 0 fallos, 0 errores, 0 omitidos |
| Tests instrumentados | 29 métodos compilados; 0 ejecutados en dispositivo |
| APK debug | Ensamblado correctamente |
| Lint | 0 errores, 19 advertencias |

Comandos ejecutados:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Los 15 tests unitarios proceden de los informes XML de
`app/build/test-results/testDebugUnitTest/`: `ReminderManagerTest` (9),
`ColorContrastTest` (3) y `PaletteIdTest` (3). La compilación instrumentada
terminó correctamente y el recuento de 29 métodos `@Test` corresponde al
conjunto `app/src/androidTest`; no se ejecutó `connectedDebugAndroidTest`.

Clasificación exacta de lint:

| Clasificación | Recuento | Detalle |
|---|---:|---|
| Plantilla | 6 | `UnusedResources` en `res/values/colors.xml` (`purple_200`, `purple_700`, `teal_200`, `teal_700`, `black`, `white`) |
| Launcher | 2 | `IconXmlAndPng` para `ic_launcher.webp` e `ic_launcher_round.webp` |
| Versiones | 11 | `OldTargetApi` (1), `AndroidGradlePluginVersion` (2), `GradleDependency` (7) y `NewerVersionAvailable` (1) |
| Advertencias inesperadas | 0 | Ninguna |
| Total | 19 | Todas con severidad `Warning`; 0 errores |

Las 11 advertencias de versiones se conservan sin actualizar dependencias,
Gradle, Kotlin ni SDK, conforme al alcance de 10.0.

## Resultado 10.1

La limpieza segura se verificó el 2026-07-30. Se retiraron únicamente los seis
colores heredados sin consumidores y los 10 WebP raster duplicados de las cinco
densidades. El Manifest y las variantes adaptive del launcher permanecen sin
cambios.

`assembleDebug` terminó correctamente. `lintDebug` terminó con 0 errores y 11
advertencias, todas de versiones: `OldTargetApi` (1),
`AndroidGradlePluginVersion` (2), `GradleDependency` (7) y
`NewerVersionAvailable` (1). No quedan advertencias de plantilla, launcher ni
inesperadas. Las advertencias de versiones siguen aceptadas y no se modifican
herramientas, SDK ni dependencias.

## Resultado 10.2

Se añadieron dos tests deterministas en `ReminderManagerTest`: completar con
reconciliación y restaurar con reconciliación. Ambos bloquean la primera
operación dentro del fake mediante `CompletableDeferred`, lanzan la segunda y
verifican que no alcanza el fake hasta liberar la primera. No usan esperas
temporales ni exponen el `Mutex` de producción.

El 2026-07-30, `:app:testDebugUnitTest` terminó con 17 tests, 0 fallos,
0 errores y 0 omitidos. `ReminderManagerTest` contiene 11 tests; los otros
tests locales son `ColorContrastTest` (3) y `PaletteIdTest` (3).
`:app:compileDebugKotlin` también terminó correctamente. No se modificó
producción ni se añadieron dependencias.

## Entornos

| Entorno | Dispositivo | API | Launcher/navegación | Estado |
|---|---|---:|---|---|
| Emulador mínimo | No seleccionado en 10.0 | 31 | No verificado | PENDIENTE |
| Emulador actual | No seleccionado en 10.0 | 36 | No verificado | PENDIENTE |
| Teléfono físico | No conectado en 10.0 | Pendiente | No verificado | PENDIENTE |

## Evidencia automática

| Comprobación | Resultado | Tests/advertencias | Notas |
|---|---|---|---|
| Tests unitarios | OK | 17 tests; 0 fallos, 0 errores, 0 omitidos | `:app:testDebugUnitTest`; informes XML generados |
| Tests instrumentados API 31 | PENDIENTE | 29 métodos compilados; ejecución no realizada | `:app:compileDebugAndroidTestKotlin`; sin emulador seleccionado |
| Tests instrumentados API 36 | PENDIENTE | 29 métodos compilados; ejecución no realizada | `:app:compileDebugAndroidTestKotlin`; sin emulador seleccionado |
| Tests instrumentados físicos | PENDIENTE | 29 métodos compilados; ejecución no realizada | `:app:compileDebugAndroidTestKotlin`; sin dispositivo conectado |
| APK debug | OK | Ensamblado correcto | `:app:assembleDebug` |
| Lint | OK | 0 errores, 11 advertencias | `lintDebug`: 0 plantilla, 0 launcher, 11 versiones, 0 inesperadas |

## Flujos críticos

| ID | Escenario y resultado esperado | API 31 | API 36 | Físico | Notas |
|---|---|---|---|---|---|
| CORE-01 | Inicio limpio: onboarding de dos páginas sin pedir permisos | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-02 | Completar/saltar onboarding: no reaparece al reiniciar | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-03 | Crear por texto: una fila y una notificación | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-04 | Completar en app y deshacer: Room, lista y notificación coherentes | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-05 | Pulsar cuerpo de notificación: abre Nolvida sin completar | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-06 | Pulsar Hecho: completa sin abrir Nolvida | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-07 | Descartar notificación y abrir app: se reconcilia silenciosamente | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-08 | Voz en app: permiso, parciales, revisión y un guardado | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-09 | Voz en app: cancelar y error conservan alternativa manual | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-10 | Widget preparado: graba sin abrir la actividad y guarda una nota | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-11 | Widget: segunda pulsación cancela antes del guardado | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-12 | Widget sin permisos: fallback privado abre el flujo visible | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-13 | Pulsaciones rápidas: no duplican sesión, nota o notificación | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-14 | Cerrar y reabrir: notas, onboarding y paleta persisten | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-15 | Reiniciar dispositivo: recupera notificaciones sin abrir la app | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-16 | Trabajo periódico: recupera una ausente sin duplicar trabajos | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-17 | Revocar permiso o bloquear canal: falla de forma controlada | PENDIENTE | PENDIENTE | PENDIENTE | |
| CORE-18 | Varias notas y texto largo: orden, scroll y acciones correctos | PENDIENTE | PENDIENTE | PENDIENTE | |

## Accesibilidad, idiomas y paletas

| ID | Escenario | Entorno | Estado | Notas |
|---|---|---|---|---|
| A11Y-01 | TalkBack anuncia jerarquía y acciones sin duplicados | Físico | PENDIENTE | |
| A11Y-02 | Fuente grande y tamaño de pantalla aumentado | API 36 y físico | PENDIENTE | |
| A11Y-03 | Poco alto/horizontal mantiene contenido alcanzable | API 31 y API 36 | PENDIENTE | |
| A11Y-04 | IME no tapa entrada manual ni revisión de voz | API 31, API 36 y físico | PENDIENTE | |
| LOC-01 | Inglés completo | API 36 | PENDIENTE | |
| LOC-02 | Español completo | API 36 | PENDIENTE | |
| LOC-03 | Alemán completo | API 36 | PENDIENTE | |
| LOC-04 | Francés completo | API 36 | PENDIENTE | |
| LOC-05 | Italiano completo | API 36 | PENDIENTE | |
| LOC-06 | Portugués de Portugal completo | API 36 | PENDIENTE | |
| LOC-07 | Idioma no soportado usa fallback inglés | API 36 | PENDIENTE | |
| PAL-01 | Las seis paletas cubren app, snackbar, hoja y widget | API 36 y físico | PENDIENTE | |
| PAL-02 | Paleta persiste tras recreación y reinicio | API 36 y físico | PENDIENTE | |

## Dispositivo físico

| ID | Escenario | Estado | Notas |
|---|---|---|---|
| PHY-01 | Launcher blanco/negro se ve completo con la máscara real | PENDIENTE | |
| PHY-02 | Widget mantiene proporciones, padding y estados | PENDIENTE | |
| PHY-03 | Reconocimiento local funciona sin red disponible | PENDIENTE | |
| PHY-04 | Notificaciones del fabricante conservan texto y Hecho | PENDIENTE | |
| PHY-05 | Teclado, barras del sistema y rotación no ocultan acciones | PENDIENTE | |
| PHY-06 | Uso repetido no produce cierre, ANR ni bloqueo perceptible | PENDIENTE | |

## Incidencias

| ID | Severidad provisional | Entorno | Pasos | Esperado | Observado | Estado |
|---|---|---|---|---|---|---|
| — | — | — | — | — | — | — |
