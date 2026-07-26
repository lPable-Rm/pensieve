# Fase 9.10 — Correcciones de la auditoría

Estado: **completada y re-auditada en sus cuatro hallazgos medios**. La deuda
baja de los bloques 9.10C y 9.10D queda documentada para la fase 10.

## Motivo

La auditoría 9.9 terminó sin hallazgos altos y con cuatro hallazgos medios. La
fase 9 debe permanecer abierta hasta corregirlos y verificar que el pulido
visual no ha alterado los flujos funcionales de las fases 1 a 8.

Esta subfase es un bloque de corrección. No debe incorporar funciones nuevas,
rediseñar los flujos aprobados ni convertirse en una refactorización general.

## Resultado bloqueante alcanzado

1. Las seis paletas cumplen contraste en todos los componentes reales que usan
   sus colores.
2. El contenido del onboarding respeta barras del sistema y recortes sin perder
   el fondo edge-to-edge ni su desplazamiento.
3. El estado vacío permanece centrado cuando cabe y puede desplazarse con poco
   alto o texto grande.
4. La revisión de voz protege editor y acciones frente al IME y permite
   desplazarlos sin alterar su máquina de estados.

## Hallazgos medios que bloquean el cierre

| Bloque | Hallazgo | Evidencia principal |
|---|---|---|
| 9.10A | Contraste insuficiente en varias paletas | Acciones, iconos y textos usan `primary` como color de contenido sobre blanco o `primaryContainer` |
| 9.10B | El onboarding no protege su contenido frente a los insets | `enableEdgeToEdge()` está activo, pero el contenido no aplica `safeDrawing` |
| 9.10B | El estado vacío puede desbordarse | No tiene desplazamiento ni variante adaptativa para poco alto o texto grande |
| 9.10B | La revisión de voz puede quedar tapada por el teclado | La barra editable no dispone de protección específica frente al IME |

No cerrar la fase compensando un hallazgo con una excepción documental. Cada
uno debe corregirse en código o recursos y quedar cubierto por una comprobación
repetible.

## Antes de modificar

Leer:

- `AGENTS.md`;
- `GUIA_FASE_9_TERRA.md`;
- `guias/fase_9/FASE_9_9_AUDITORIA.md`;
- las guías 9.1, 9.2 y 9.3–9.8 relacionadas con el bloque que se vaya a
  corregir;
- los archivos de producción y prueba citados en cada apartado.

Revisar también `git status --short`. El árbol puede contener trabajo previo
del usuario: no descartarlo, no reformatear archivos ajenos y no modificar
ningún archivo de `diseño/`.

Archivos principales que deben inspeccionarse:

```text
android/app/src/main/java/com/mvppostit/pensieve/MainActivity.kt
android/app/src/main/java/com/mvppostit/pensieve/data/preferences/AppPreferences.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/appearance/AppearancePaletteSheet.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeScreen.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/components/
android/app/src/main/java/com/mvppostit/pensieve/ui/onboarding/
android/app/src/main/java/com/mvppostit/pensieve/ui/theme/
android/app/src/main/java/com/mvppostit/pensieve/widget/VoiceCaptureWidgetProvider.kt
android/app/src/main/res/layout/widget_voice_capture.xml
android/app/src/test/java/com/mvppostit/pensieve/ui/theme/
android/app/src/androidTest/java/com/mvppostit/pensieve/ui/home/
```

Implementar y verificar un bloque cada vez, en el orden siguiente.

## 9.10A — Contraste y roles de color

### Problema confirmado

`primary` representa el acento de marca, pero se está utilizando también como
si fuera siempre un color de contenido accesible. La combinación falla para
texto normal en varias paletas:

| Paleta | `primary` / blanco | `primary` / `primaryContainer` |
|---|---:|---:|
| Lavanda | 5,70:1 | 4,80:1 |
| Verde petróleo | 3,74:1 | 3,36:1 |
| Azul profundo | 5,17:1 | 4,51:1 |
| Coral suave | 2,78:1 | 2,52:1 |
| Escala de grises | 4,83:1 | 4,39:1 |
| Turquesa | 2,43:1 | 2,18:1 |

El mínimo es `4.5:1` para texto normal, `3:1` para texto grande y `3:1` para
iconos o límites esenciales de controles.

### Corregir

- Inventariar las parejas reales de primer plano y fondo de:
  - botones tonales y `TextButton`;
  - acción de voz del estado vacío;
  - selector de paletas y sus radios;
  - iconos accionables de cabecera y tarjetas;
  - entrada manual y barra de voz;
  - snackbar y hoja de apariencia;
  - widget en reposo y grabando.
- Mantener los seis colores de marca aprobados. No alterar sus hexadecimales
  solo para convertir `primary` en color de texto.
- Asignar roles semánticos accesibles en `ColorScheme`, por ejemplo
  `onPrimary`, `onPrimaryContainer`, `onSurface`, `outline` o el rol equivalente
  adecuado a cada superficie.
- Hacer que los componentes consuman esos roles. No introducir condiciones por
  id de paleta dentro de cada componente.
- Definir también los tokens que consumen realmente snackbar y hoja inferior,
  como `inversePrimary` o los `surfaceContainer*` necesarios, para que ninguna
  paleta herede accidentalmente el morado predeterminado de Material.
- Conservar la combinación accesible predeterminada de Material para errores y
  reservar el rojo de grabación para el estado aprobado.
- Añadir pruebas unitarias que recorran las seis paletas y validen, como mínimo:
  - texto normal: `>= 4.5:1`;
  - contenido no textual esencial: `>= 3:1`;
  - todos los pares expuestos por los componentes anteriores.

No basta con probar los colores aislados si la interfaz usa después una pareja
distinta.

### Verificar antes de continuar

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
```

Revisar las previews de las seis paletas y registrar cualquier pareja que se
haya excluido de las pruebas, junto con el motivo.

## 9.10B — Insets, poco alto, texto grande e IME

### Onboarding

- Mantener el fondo a pantalla completa.
- Proteger títulos, ilustración, indicador y acciones con
  `WindowInsets.safeDrawing` o una solución Compose equivalente.
- Evitar aplicar el mismo inset dos veces.
- Conservar el desplazamiento vertical y el reinicio de posición entre páginas.
- Comprobar las dos páginas con navegación por gestos y por tres botones.

### Estado vacío

- Convertir el contenido en desplazable o adaptativo cuando el alto disponible
  no permita mostrar icono, textos y botón.
- Mantener el objetivo táctil mínimo de `48dp`.
- No sustituir el callback real de voz por uno decorativo.
- Añadir previews del estado vacío con:
  - escala de fuente `1.8`;
  - orientación horizontal o un alto equivalente reducido.

### Revisión de voz

- Proteger el editor y sus acciones frente al teclado con `imePadding`,
  desplazamiento o la combinación mínima necesaria.
- Mantener visibles y alcanzables `Cancelar` y `Guardar`.
- No cambiar la máquina de estados de escucha, revisión, guardado o error.
- Evitar duplicar el padding del IME que ya utiliza la entrada manual.

### Semántica

Si la corrección cambia la jerarquía, marcar el título principal del
onboarding, el título del estado vacío y el encabezado de apariencia como
encabezados semánticos. No añadir descripciones redundantes a textos que
TalkBack ya anuncia correctamente.

### Verificar antes de continuar

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:compileDebugAndroidTestKotlin
```

Las pruebas instrumentadas pueden compilarse ahora y ejecutarse en la matriz
manual de fase 10, pero las previews y tests existentes no deben quedar rotos.

## 9.10C — Deuda baja del widget para fase 10

Estas observaciones son bajas, pero están ligadas directamente a paletas y
onboarding y deben resolverse cuando exista una corrección pequeña y clara.

### Actualizaciones de paleta

- Evitar que una lectura asíncrona antigua de DataStore se aplique después de
  una selección nueva.
- Serializar la obtención y aplicación del estado visual, o utilizar una
  generación equivalente que descarte resultados obsoletos.
- Construir cada actualización a partir de una sola instantánea coherente de
  paleta y estado.
- No añadir otra preferencia, dependencia ni trabajo periódico.

### Jerarquía visual

- Evitar teñir con el mismo color el fondo completo, el círculo del micrófono y
  su contorno.
- Mantener una superficie neutra compartida y usar la paleta activa como
  acento, con texto e iconos que cumplan contraste.
- Reutilizar un único layout y tintes en tiempo de ejecución. No crear seis
  copias de drawables o layouts.

### Ciclo de vida

- Corregir el comentario que afirma que una muerte abrupta del proceso devuelve
  automáticamente el widget a reposo: `RemoteViews` puede conservar el último
  estado.
- No persistir una falsa sesión de grabación ni añadir reconciliación periódica
  solo para ocultar ese caso. Si no existe una solución determinista y pequeña,
  documentarlo como prueba de recuperación para fase 10.
- Impedir solicitudes duplicadas de fijado del widget durante una misma
  finalización del onboarding.
- Conservar de forma segura, durante una recreación, la intención privada que
  prepara la captura del widget; no permitir que una acción equivalente enviada
  directamente a la actividad exportada la active.

Después de cada cambio, confirmar que siguen intactos:

- servicio foreground de tipo micrófono;
- reconocimiento exclusivamente local;
- cancelación solo antes de aceptar el guardado;
- una única escritura mediante `ReminderManager`;
- `PendingIntent` explícitos e inmutables;
- alias privado como único fallback de permisos.

## 9.10D — Limpieza acotada para fase 10

### Limpieza segura

Revisar y retirar, solo después de confirmar que no tienen consumidores:

- el import no usado de `height` en `OnboardingPages.kt`;
- los seis colores de plantilla de `res/values/colors.xml`;
- los iconos WebP antiguos del robot que lint marca como duplicados de los
  launchers adaptativos;
- comentarios de plantilla de `Type.kt`;
- referencias a bloques futuros ya implementados en onboarding;
- el parámetro `onNext` si continúa siendo una API muerta;
- los `map { it }` sin transformación de `MainActivity`;
- la lectura duplicada de DataStore en `AppPreferences`;
- las actualizaciones completas duplicadas de widget en
  `VoiceCaptureService`, solo si `finishCapture()` ya garantiza la transición;
- la supresión `UnusedResources` de `ic_nolvida.xml`, que ya tiene consumidores;
- `res/values/keep.xml` y los comentarios de plantilla de
  `main/keepRules/rules.keep`, cuando las referencias directas hagan
  innecesaria la conservación adicional.

No borrar recursos basándose solo en su nombre. Comprobar referencias, el
Manifest fusionado y el resultado de `assembleDebug`.

### Pruebas

- Sustituir el texto español hardcodeado de
  `HomeScreenAccessibilityTest.kt` por el recurso localizado.
- Mantener las pruebas de ids de paleta e incorporar las comprobaciones de
  contraste de 9.10A.
- Añadir pruebas pequeñas para la normalización de preferencias y para las
  transiciones del widget solo si pueden escribirse con las dependencias
  actuales.
- No introducir Robolectric, librerías de snapshots ni nuevas dependencias de
  producción o prueba en esta subfase.

### Deuda baja que puede aplazarse

Si una corrección exige temporizadores permanentes o una arquitectura nueva,
documentarla para fase 10 en lugar de inflar 9.10. En concreto:

- actualización exacta de `Ahora` al cruzar el primer minuto sin que exista
  otra recomposición;
- recuperación visual inmediata del widget después de matar abruptamente todo
  el proceso;
- conversión opcional de la ilustración PNG a WebP sin pérdida;
- ejecución completa de TalkBack y de la matriz física.

El aplazamiento debe quedar escrito con su riesgo y una prueba manual concreta;
no debe ocultarse como si estuviera corregido.

## Límites

No modificar en 9.10:

- `diseño/`;
- Room, esquema, DAO o `ReminderManager`;
- comportamiento del reconocedor local;
- permisos, paquete o componentes exportados del Manifest salvo que una
  verificación de seguridad demuestre una regresión;
- versiones de Gradle, Kotlin, Compose o SDK;
- dependencias;
- Navigation Compose, Hilt, Glance, modo oscuro o colores dinámicos;
- flujos de producto, textos nuevos no inventariados o funciones de fase 10.

No añadir `INTERNET`, telemetría, logs con notas o transcripciones, persistencia
de audio ni estado histórico de recordatorios.

No gestionar emuladores o dispositivos, ni hacer commit, push, ramas o cambios
de remotos salvo petición explícita del usuario.

## Comprobación final

Desde `android/`:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Desde la raíz del repositorio:

```powershell
git diff --check
git status --short
```

Registrar:

- número de tests ejecutados, fallidos y omitidos;
- errores y advertencias de lint;
- advertencias reutilizadas o tareas `UP-TO-DATE`;
- diferencias manuales aún aplazadas.

El objetivo bloqueante es `0` errores de lint y ninguna advertencia nueva. Las
ocho advertencias ya documentadas de recursos antiguos se trasladan a fase 10;
las once advertencias de versiones disponibles no autorizan actualizar
dependencias.

Repetir también las comprobaciones estáticas de la auditoría:

- Manifest fuente y fusionado sin `INTERNET`;
- ausencia de Firebase, analítica, publicidad y telemetría;
- ausencia de secretos y logs sensibles;
- `PendingIntent` explícitos e inmutables;
- ninguna referencia visible a `Pensieve`.

## Matriz manual para fases 10 y 11

Dejar enumeradas, aunque no se ejecuten todavía:

- onboarding en API 31 y API 36, ambas páginas, gestos y tres botones;
- estado vacío con fuente `1.8` y horizontal;
- revisión de voz con teclado abierto;
- seis paletas en Home, hoja, snackbar y widget;
- TalkBack sobre encabezados, tarjetas y acciones;
- widget en reposo, grabando, cancelando, guardando, recreación de actividad y
  muerte abrupta del proceso;
- seis idiomas y fallback inglés.

## Resultado de implementación

- Los componentes consumen `onPrimary`, `onPrimaryContainer`, `onSurface`,
  `outline` y los roles inversos según su superficie, sin modificar ningún
  hexadecimal de marca.
- `ColorContrastTest` recorre las seis paletas y valida texto, controles,
  snackbar, widget y grabación.
- El onboarding aplica `safeDrawingPadding()` dentro de su fondo edge-to-edge.
- Home consume una sola vez los insets del `Scaffold`, aplica un único
  `imePadding()` compartido y hace desplazable únicamente la revisión de voz.
- El estado vacío vive en un `LazyColumn` centrado y desplazable.
- Se añadieron variantes compactas y de texto grande y dos pruebas
  instrumentadas de geometría reducida; compilan, pero su ejecución en
  dispositivo continúa en la matriz de fase 10.
- Resultado automático final:
  - `15` tests unitarios, `0` fallos, `0` errores y `0` omitidos;
  - `29` tests instrumentados compilados;
  - compilación Kotlin y ensamblado debug correctos;
  - lint con `0` errores y las `19` advertencias ya registradas;
  - segunda auditoría: `0` hallazgos altos y `0` medios.

## Criterio de cierre

La subfase 9.10 puede darse por completada únicamente cuando:

- los cuatro hallazgos medios tienen corrección y evidencia;
- no queda ningún hallazgo alto o medio nuevo;
- las seis paletas cumplen los umbrales definidos;
- compilación, tests, ensamblado y lint terminan correctamente;
- texto, voz, completar, deshacer, notificaciones y widget mantienen sus
  contratos;
- toda deuda baja aplazada está identificada para fase 10.

La segunda pasada independiente con la guía 9.9 confirmó que no quedan
hallazgos altos o medios. `AGENTS.md` y `CONTEXTO_MANANA.txt` registran por ello
el cierre de la fase 9 y trasladan la matriz manual y la deuda baja a fase 10.
