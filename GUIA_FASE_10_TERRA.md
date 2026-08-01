# Fase 10 — Guía de calidad con ChatGPT 5.6 Terra

Estado: **completada y auditada; subfases 10.0 a 10.9 cerradas**.

## Objetivo

Convertir el MVP funcional de Nolvida en una versión estable, comprobada y
preparada para iniciar su publicación. Esta fase no añade funciones: elimina
deuda segura, amplía únicamente la cobertura crítica y valida el producto en
emuladores y en un dispositivo físico.

El ciclo de trabajo será siempre:

```text
preparar -> probar -> registrar evidencia -> corregir lo mínimo -> repetir
```

## Línea base

Antes de comenzar se esperan:

- `15` tests unitarios correctos;
- `29` tests instrumentados que compilan;
- Kotlin y APK debug correctos;
- lint con `0` errores y `19` advertencias conocidas;
- `6` advertencias por colores de plantilla;
- `2` advertencias por launchers raster antiguos;
- `11` advertencias de versiones que no autorizan actualizaciones.

La subfase 10.0 debe volver a medir esta línea base. No se copiarán estas cifras
sin comprobar los informes reales.

## Subfases

| Subfase | Alcance | Guía |
|---|---|---|
| 10.0 | Línea base y matriz | [FASE_10_0_LINEA_BASE.md](guias/fase_10/FASE_10_0_LINEA_BASE.md) |
| 10.1 | Limpieza segura | [FASE_10_1_LIMPIEZA.md](guias/fase_10/FASE_10_1_LIMPIEZA.md) |
| 10.2 | Tests automáticos críticos | [FASE_10_2_TESTS_AUTOMATICOS.md](guias/fase_10/FASE_10_2_TESTS_AUTOMATICOS.md) |
| 10.3 | Compatibilidad mínima en API 31 | [FASE_10_3_API_31.md](guias/fase_10/FASE_10_3_API_31.md) |
| 10.4 | Compatibilidad actual en API 36 | [FASE_10_4_API_36.md](guias/fase_10/FASE_10_4_API_36.md) |
| 10.5 | Accesibilidad, idiomas y paletas | [FASE_10_5_ACCESIBILIDAD_IDIOMAS.md](guias/fase_10/FASE_10_5_ACCESIBILIDAD_IDIOMAS.md) |
| 10.6 | Dispositivo físico | [FASE_10_6_DISPOSITIVO_FISICO.md](guias/fase_10/FASE_10_6_DISPOSITIVO_FISICO.md) |
| 10.7 | Correcciones demostradas | [FASE_10_7_CORRECCIONES.md](guias/fase_10/FASE_10_7_CORRECCIONES.md) |
| 10.8 | Auditoría y cierre | [FASE_10_8_AUDITORIA_FINAL.md](guias/fase_10/FASE_10_8_AUDITORIA_FINAL.md) |
| 10.9 | Correcciones bajas de auditoría | [FASE_10_9_CORRECCIONES_AUDITORIA.md](guias/fase_10/FASE_10_9_CORRECCIONES_AUDITORIA.md) |

Todos los resultados manuales se registran en
[MATRIZ_PRUEBAS_MANUALES.md](guias/fase_10/MATRIZ_PRUEBAS_MANUALES.md).
Completar una sola subfase antes de iniciar la siguiente.

Las pruebas automáticas y manuales de 10.0 a 10.7 ya están registradas. La
auditoría integral 10.8 terminó sin hallazgos altos o medios y registró siete
hallazgos bajos. La subfase 10.9 corrigió cuatro y aceptó expresamente tres
como deuda no bloqueante. La fase 10 queda cerrada.

## Entornos y responsabilidades

- El usuario crea, inicia y selecciona los emuladores en Android Studio.
- El usuario conecta y controla el dispositivo físico desde Android Studio.
- El agente no crea, inicia, reinicia ni borra dispositivos.
- Una limpieza de datos de la app debug requiere confirmación del usuario.
- Los tests instrumentados se lanzan desde Android Studio sobre el destino
  seleccionado o con `connectedDebugAndroidTest` cuando solo estén disponibles
  para Gradle los dispositivos que el usuario quiera probar.

Entornos obligatorios:

1. emulador Android API 31;
2. emulador Android API 36;
3. al menos un teléfono físico compatible.

Los emuladores validan compatibilidad. El teléfono decide la aceptación real
de micrófono, widget, notificaciones, teclado, TalkBack y apariencia. La fase
11 repetirá únicamente una prueba de humo sobre la versión release.

## Política de pruebas

- La prueba de concurrencia pendiente de fase 7 se añadió en 10.2.
- Preferir tests locales deterministas.
- Usar tests instrumentados solo cuando se necesiten APIs o interfaz Android.
- No usar esperas temporales para simular concurrencia.
- No introducir Robolectric, snapshots, Orchestrator ni dependencias nuevas.
- No intentar cubrir cada línea: proteger contratos y regresiones de riesgo.
- No crear capas de producción únicamente para facilitar una prueba.

## Límites

No incorporar durante la fase 10:

- nuevas funciones de producto;
- widget `4 × 1` o widget grande de recordatorios;
- cambios de arquitectura;
- actualizaciones de Gradle, Kotlin, Compose, SDK o dependencias;
- Hilt, Glance, Navigation Compose o módulos adicionales;
- modo oscuro, colores dinámicos o rediseños no justificados por una prueba;
- permiso `INTERNET`, telemetría, analítica o almacenamiento de audio;
- modificaciones dentro de `diseño/`.

Las correcciones visuales deben responder a un fallo reproducible registrado
en la matriz. No se rediseñará por intuición durante una prueba.

## Comprobaciones comunes

Desde `android/`, según indique cada subguía:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

Desde la raíz:

```powershell
git diff --check
git status --short
```

Registrar siempre tareas ejecutadas, tests, fallos, omitidos y advertencias.
No ocultar resultados `UP-TO-DATE`; son válidos si corresponden al estado
actual.

## Prompt base para Terra

```text
Trabaja exclusivamente en la subfase [SUBFASE] de la fase 10 de Nolvida.

Lee completamente AGENTS.md, CONTEXTO_MANANA.txt, GUIA_FASE_10_TERRA.md,
[SUBGUIA] y la matriz manual. Revisa git status y el código real antes de
actuar. Conserva los cambios existentes del usuario.

No añadas funciones, dependencias ni actualices versiones. No modifiques
diseño/, no gestiones emuladores o dispositivos y no hagas commit ni push.
Si la subfase es manual, espera a que el usuario confirme el entorno y registra
sus resultados sin inventar evidencia. Si aparece un fallo, documéntalo y
detente antes de corregirlo salvo que esta subfase autorice la corrección.

Ejecuta únicamente las comprobaciones indicadas, informa de archivos y
resultados exactos y detente antes de la siguiente subfase.
```

## Referencias oficiales

- <https://developer.android.com/studio/test/command-line>
- <https://developer.android.com/training/testing/instrumented-tests>
- <https://developer.android.com/develop/ui/compose/accessibility/testing>
- <https://developer.android.com/codelabs/jetpack-compose-accessibility>
- <https://developer.android.com/docs/quality-guidelines/core-app-quality>
- <https://developer.android.com/topic/performance/vitals/>

## Criterio de cierre

La fase 10 solo se cierra cuando:

- no quedan hallazgos altos o medios;
- los tests unitarios e instrumentados obligatorios terminan correctamente;
- las matrices API 31, API 36 y física están ejecutadas;
- TalkBack, texto grande, idiomas y paletas tienen evidencia;
- no existen cierres, ANR o bloqueos reproducibles en los flujos críticos;
- lint tiene `0` errores y solo advertencias aceptadas y documentadas;
- Manifest fuente y fusionado siguen sin `INTERNET`;
- la deuda aplazada tiene riesgo, motivo y destino en fase 11 o versión futura;
- `AGENTS.md` y `CONTEXTO_MANANA.txt` reflejan el resultado final.
