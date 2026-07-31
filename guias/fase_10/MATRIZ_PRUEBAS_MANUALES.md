# Fase 10 — Matriz de pruebas

Estado: **subfases 10.0 a 10.7 completadas; auditoría independiente 10.8 pendiente**.

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

## Resultados 10.3 a 10.7

- **10.3, API 31:** Pixel 6 AVD con Android 12. Los 29 tests
  instrumentados terminaron sin fallos ni omitidos y el usuario validó los
  flujos `CORE` sin encontrar errores.
- **10.4, API 36:** Pixel 8 AVD con Android 16. Los 29 tests
  instrumentados terminaron sin fallos ni omitidos y el usuario repitió los
  flujos `CORE` sin encontrar diferencias funcionales.
- **10.5:** accesibilidad, texto grande, orientación, teclado, los seis idiomas,
  fallback inglés y las seis paletas fueron comprobados y aprobados por el
  usuario.
- **10.6:** Vivo V2130 con Android 14. Los 29 tests instrumentados terminaron
  sin fallos ni omitidos. Los flujos funcionales fueron correctos y la prueba
  física produjo las cuatro incidencias visuales o de notificación `F10-01` a
  `F10-04`.
- **10.7:** las cuatro incidencias se corrigieron y el usuario aprobó cada
  repetición. El pulido adicional del widget se conserva como mejora futura y
  no bloquea esta versión.

## Entornos

| Entorno | Dispositivo | API | Launcher/navegación | Estado |
|---|---|---:|---|---|
| Emulador mínimo | Pixel 6 AVD, Android 12 | 31 | Pixel Launcher; navegación no registrada | OK |
| Emulador actual | Pixel 8 AVD, Android 16 | 36 | Pixel Launcher; navegación no registrada | OK |
| Teléfono físico | Vivo V2130, Android 14 | 34 | Launcher del fabricante; navegación no registrada | OK |

## Evidencia automática

| Comprobación | Resultado | Tests/advertencias | Notas |
|---|---|---|---|
| Tests unitarios | OK | 17 tests; 0 fallos, 0 errores, 0 omitidos | `:app:testDebugUnitTest`; informes XML generados |
| Tests instrumentados API 31 | OK | 29 ejecutados; 0 fallos, 0 errores, 0 omitidos | Pixel 6 AVD, Android 12 |
| Tests instrumentados API 36 | OK | 29 ejecutados; 0 fallos, 0 errores, 0 omitidos | Pixel 8 AVD, Android 16 |
| Tests instrumentados físicos | OK | 29 ejecutados; 0 fallos, 0 errores, 0 omitidos | Vivo V2130, Android 14 |
| APK debug | OK | Ensamblado correcto | `:app:assembleDebug` |
| Lint | OK | 0 errores, 11 advertencias | `lintDebug`: 0 plantilla, 0 launcher, 11 versiones, 0 inesperadas |

## Flujos críticos

| ID | Escenario y resultado esperado | API 31 | API 36 | Físico | Notas |
|---|---|---|---|---|---|
| CORE-01 | Inicio limpio: onboarding de dos páginas sin pedir permisos | OK | OK | OK | Validación manual confirmada |
| CORE-02 | Completar/saltar onboarding: no reaparece al reiniciar | OK | OK | OK | Validación manual confirmada |
| CORE-03 | Crear por texto: una fila y una notificación | OK | OK | OK | Validación manual confirmada |
| CORE-04 | Completar en app y deshacer: Room, lista y notificación coherentes | OK | OK | OK | Validación manual confirmada |
| CORE-05 | Pulsar cuerpo de notificación: abre Nolvida sin completar | OK | OK | OK | Validación manual confirmada |
| CORE-06 | Pulsar Hecho: completa sin abrir Nolvida | OK | OK | OK | Validación manual confirmada |
| CORE-07 | Descartar notificación y abrir app: se reconcilia silenciosamente | OK | OK | OK | Validación manual confirmada |
| CORE-08 | Voz en app: permiso, parciales, revisión y un guardado | OK | OK | OK | Validación manual confirmada |
| CORE-09 | Voz en app: cancelar y error conservan alternativa manual | OK | OK | OK | Validación manual confirmada |
| CORE-10 | Widget preparado: graba sin abrir la actividad y guarda una nota | OK | OK | OK | Validación manual confirmada |
| CORE-11 | Widget: segunda pulsación cancela antes del guardado | OK | OK | OK | Validación manual confirmada |
| CORE-12 | Widget sin permisos: fallback privado abre el flujo visible | OK | OK | OK | Validación manual confirmada |
| CORE-13 | Pulsaciones rápidas: no duplican sesión, nota o notificación | OK | OK | OK | Validación manual confirmada |
| CORE-14 | Cerrar y reabrir: notas, onboarding y paleta persisten | OK | OK | OK | Validación manual confirmada |
| CORE-15 | Reiniciar dispositivo: recupera notificaciones sin abrir la app | OK | OK | OK | Validación manual confirmada |
| CORE-16 | Trabajo periódico: recupera una ausente sin duplicar trabajos | OK | OK | OK | Validación manual confirmada |
| CORE-17 | Revocar permiso o bloquear canal: falla de forma controlada | OK | OK | OK | Validación manual confirmada |
| CORE-18 | Varias notas y texto largo: orden, scroll y acciones correctos | OK | OK | OK | Validación manual confirmada |

## Accesibilidad, idiomas y paletas

| ID | Escenario | Entorno | Estado | Notas |
|---|---|---|---|---|
| A11Y-01 | TalkBack anuncia jerarquía y acciones sin duplicados | Físico | OK | Validado en 10.5 |
| A11Y-02 | Fuente grande y tamaño de pantalla aumentado | API 36 y físico | OK | Validado en 10.5 |
| A11Y-03 | Poco alto/horizontal mantiene contenido alcanzable | API 31 y API 36 | OK | Validado en 10.5 |
| A11Y-04 | IME no tapa entrada manual ni revisión de voz | API 31, API 36 y físico | OK | Validado en 10.5 |
| LOC-01 | Inglés completo | API 36 | OK | Validado en 10.5 |
| LOC-02 | Español completo | API 36 | OK | Validado en 10.5 |
| LOC-03 | Alemán completo | API 36 | OK | Validado en 10.5 |
| LOC-04 | Francés completo | API 36 | OK | Validado en 10.5 |
| LOC-05 | Italiano completo | API 36 | OK | Validado en 10.5 |
| LOC-06 | Portugués de Portugal completo | API 36 | OK | Validado en 10.5 |
| LOC-07 | Idioma no soportado usa fallback inglés | API 36 | OK | Validado en 10.5 |
| PAL-01 | Las seis paletas cubren app, snackbar, hoja y widget | API 36 y físico | OK | Validado en 10.5 |
| PAL-02 | Paleta persiste tras recreación y reinicio | API 36 y físico | OK | Validado en 10.5 |

## Dispositivo físico

| ID | Escenario | Estado | Notas |
|---|---|---|---|
| PHY-01 | Launcher blanco/negro se ve completo con la máscara real | OK | Validación física confirmada |
| PHY-02 | Widget mantiene proporciones, padding y estados | OK | Ajuste 10.7C aprobado; pulido futuro no bloqueante |
| PHY-03 | Reconocimiento local funciona sin red disponible | OK | Validación física confirmada |
| PHY-04 | Notificaciones del fabricante conservan texto y Hecho | OK | Corregido y repetido en 10.7A y 10.7B |
| PHY-05 | Teclado, barras del sistema y rotación no ocultan acciones | OK | Validación física confirmada |
| PHY-06 | Uso repetido no produce cierre, ANR ni bloqueo perceptible | OK | Validación física confirmada |

## Incidencias

| ID | Severidad provisional | Entorno | Pasos | Esperado | Observado | Estado |
|---|---|---|---|---|---|---|
| F10-01 | Media | Vivo V2130, Android 14 | Crear una nota con el canal silencioso y bloquear el teléfono | La notificación permanece visible sin sonido ni vibración | Aparecía en el panel, pero no en el bloqueo; al activar sonido sí aparecía | CORREGIDA Y VALIDADA 10.7A |
| F10-02 | Baja | Vivo V2130, Android 14 | Revisar una notificación nativa | Símbolo reconocible y jerarquía sin información repetida | El diseño inicial repetía `Nolvida`; la primera corrección repitió el recordatorio como título y cuerpo | CORREGIDA Y VALIDADA 10.7B |
| F10-03 | Baja | Vivo V2130, Android 14 | Añadir el widget `3 x 1` y revisar reposo y grabación | Micrófono, detener y logo legibles y equilibrados | Los iconos se percibían demasiado pequeños para el espacio disponible | CORREGIDA Y ACEPTADA 10.7C |
| F10-04 | Baja | Vivo V2130, Android 14 | Abrir Home y observar la cabecera | `Nolvida` centrado entre logo y paleta | El bloque logo-título quedaba desplazado hacia la izquierda | CORREGIDA Y VALIDADA 10.7D |
