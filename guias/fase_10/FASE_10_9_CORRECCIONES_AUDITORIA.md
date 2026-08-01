# Fase 10.9 — Correcciones bajas de la auditoría integral

Estado: **completada y revisada; 0 hallazgos altos o medios**.

## Objetivo

Resolver cuatro hallazgos bajos, pequeños y verificables antes de cerrar la
fase 10. No ampliar el producto, rediseñar la interfaz ni introducir nuevas
capas o dependencias.

Trabajar en orden y detenerse al terminar cada bloque.

## 10.9A — Deshabilitar `Nueva nota` durante la edición

Hallazgo: **B-01**.

En `HomeActions`, `Nueva nota` continúa habilitado cuando el editor manual ya
está abierto, aunque `HomeViewModel.showManualInput()` convierta la pulsación
en un no-op.

Corrección mínima:

- incluir `!isManualInputVisible` en `canCreateManualReminder`;
- conservar sin cambios la protección del ViewModel;
- añadir a `HomeScreenAccessibilityTest` un caso que muestre el editor manual
  y compruebe que `Nueva nota` se anuncia deshabilitado;
- no ocultar el botón ni cambiar el diseño de `HomeActions`.

## 10.9B — Fecha estable en las tarjetas

Hallazgo: **B-03**.

`Ahora` solo cambia cuando existe otra recomposición y la condición actual
acepta cualquier timestamp futuro.

Usar la solución más simple: mostrar siempre la fecha y hora absolutas mediante
el `DateFormat` localizado que ya existe.

- retirar de Home la rama relativa a `System.currentTimeMillis()`;
- simplificar `formatCreatedAtLabel` para que solo reciba `createdAtMillis`;
- conservar `created_just_now`, porque el onboarding lo utiliza en su maqueta;
- no añadir temporizadores, coroutines, estado reactivo ni dependencias.

## 10.9C — Tono informal coherente en portugués de Portugal

Hallazgo: **B-06**.

La aplicación usa mayoritariamente el tratamiento informal. Unificar solo los
imperativos que todavía son formales:

| Recurso | Texto aprobado |
|---|---|
| `new_reminder_placeholder` | `Escreve um lembrete` |
| `notification_permission_denied` | `Permite as notificações para manter este lembrete visível mesmo depois de fechar a Nolvida.` |
| `voice_review_placeholder` | `Revê o teu lembrete` |
| `voice_permission_denied` | `Permite o acesso ao microfone para criar uma nota por voz.` |
| `voice_temporary_failure` | `Não foi possível transcrever agora. Tenta novamente.` |

Actualizar los mismos valores en `localizacion/TRADUCCIONES_FASE_8.csv`. No
cambiar otros idiomas ni reformular textos neutrales.

## 10.9D — Documentación coherente

Hallazgo: **B-07**.

- cambiar en el flujo actual de `AGENTS.md` el tamaño antiguo `1 × 1` por
  `3 × 1`;
- presentar la referencia de fase 6 como tamaño histórico posteriormente
  sustituido en fase 9, sin reescribir la historia del proyecto;
- actualizar el comentario de `WidgetPreview`: es una maqueta estática del
  onboarding y el widget funcional ya está implementado;
- contrastar los recursos modificados con los CSV sin cambiar traducciones
  ajenas al hallazgo;
- retirar exclusivamente las líneas vacías finales que `git diff --check`
  señala en las guías 10.0 a 10.6.

## Deuda aceptada y fuera de alcance

| Hallazgo | Decisión |
|---|---|
| B-02 | Aplazar la recuperación visual del widget tras muerte abrupta. No compromete captura ni datos. |
| B-04 | Aceptar la conversión `Long` a `Int`; alcanzar una colisión no es realista para el producto. |
| B-05 | Mantener como deuda de cobertura los contratos directos de voz, widget y alias. La matriz actual es suficiente para el MVP. |

No modificar `VoiceCaptureWidgetProvider`, la asignación de IDs,
`OnDeviceVoiceRecognizer`, el Manifest ni los `PendingIntent` en esta subfase.

## Comprobaciones

Desde `android/`:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

No ejecutar `connectedDebugAndroidTest`. Desde la raíz:

```powershell
git diff --check
git status --short
```

## Resultado

- B-01, B-03, B-06 y B-07 quedaron corregidos y revisados.
- B-02, B-04 y B-05 permanecen como deuda baja aceptada.
- Los 17 tests unitarios terminaron sin fallos.
- Los 30 tests instrumentados actuales compilan; no se repitió su ejecución
  conectada, conforme al alcance de esta subfase.
- Kotlin, APK debug y lint terminaron correctamente.
- Lint conserva 0 errores y 11 advertencias de versiones conocidas.

## Criterio de cierre

- B-01, B-03, B-06 y B-07 están corregidos sin regresiones.
- B-02, B-04 y B-05 quedan documentados como deuda baja aceptada.
- Kotlin, APK, tests locales, compilación instrumentada y lint son correctos.
- Lint conserva 0 errores y solo las 11 advertencias de versiones aceptadas.
- `git diff --check` no informa de errores de espacios o líneas finales.
- No se ha añadido ninguna función, dependencia ni complejidad arquitectónica.
