# Fase 6.6 — Correcciones de la auditoría

## Objetivo

Corregir únicamente los problemas confirmados durante la auditoría independiente de fase 6. No añadir funciones nuevas ni rehacer la arquitectura.

La implementación se divide en cuatro bloques. Terra debe completar un solo bloque, ejecutar sus comprobaciones y detenerse para auditoría antes de continuar.

## Lectura obligatoria

```text
AGENTS.md
CONTEXTO_MANANA.txt
GUIA_FASE_6_TERRA.md
guias/fase_6/FASE_6_6_CORRECCIONES_AUDITORIA.md

android/app/src/main/AndroidManifest.xml
android/app/src/main/java/com/mvppostit/pensieve/MainActivity.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeRoute.kt
android/app/src/main/java/com/mvppostit/pensieve/voice/VoiceCaptureService.kt
android/app/src/main/java/com/mvppostit/pensieve/widget/VoiceCaptureWidgetProvider.kt
android/app/src/main/java/com/mvppostit/pensieve/notifications/ReminderNotificationPublisher.kt
android/app/src/main/res/layout/widget_voice_capture.xml
android/app/src/main/res/xml/voice_capture_widget_info.xml
```

Revisar primero `git status --short` y preservar todos los cambios existentes.

## Bloque 6.6.1 — Notificaciones seguras

### Problemas

- Android puede retrasar la notificación `Escuchando…`.
- Los IDs temporales `2001` y `2003` pueden colisionar con IDs positivos de Room.

### Cambios

1. En la notificación foreground, añadir:

```kotlin
.setForegroundServiceBehavior(
    Notification.FOREGROUND_SERVICE_IMMEDIATE,
)
```

El proyecto tiene `minSdk 31`, por lo que no necesita una comprobación de versión.

2. Reservar IDs negativos para las notificaciones internas del servicio. Mantener diferentes el ID de captura y el de error.

3. No cambiar los IDs de las notificaciones normales ni el esquema de Room.

### Aceptación

- [ ] La notificación de captura solicita visibilidad inmediata.
- [ ] Los IDs internos no pueden coincidir con IDs normales positivos.
- [ ] No se modifican canales, importancia o contenido sensible.

Después, compilar, ejecutar lint y detenerse.

## Bloque 6.6.2 — Guardado no cancelable por el usuario

### Problema

La acción `Cancelar` puede llegar mientras `ReminderManager.createReminder()` está entre la escritura en Room y la publicación de su notificación.

### Cambios

1. Mientras el servicio escucha, la notificación conserva `Cancelar`.

2. Al aceptar un resultado final y entrar en `Guardando…`:

- reconstruir la notificación sin la acción `Cancelar`;
- marcar el guardado antes de lanzar la corrutina;
- cancelar el timeout y destruir el reconocedor para liberar el micrófono antes de esperar a Room;
- dejar que `ReminderManager.createReminder()` termine.

3. Si llega un `ACTION_CANCEL_CAPTURE` antiguo cuando el guardado ya comenzó, ignorarlo. Retirar el botón no basta porque su `PendingIntent` podría haberse pulsado justo antes de actualizar la notificación.

4. La cancelación normal durante la escucha continúa deteniendo el reconocedor sin crear una nota.

5. No envolver todo el guardado en `NonCancellable` ni crear una transacción artificial entre Room y Android. La fase 7 seguirá resolviendo interrupciones externas o muerte de proceso.

### Aceptación

- [ ] Cancelar durante la escucha no guarda.
- [ ] Después del resultado final, una acción antigua no cancela `saveJob`.
- [ ] `Guardando…` no muestra `Cancelar`.
- [ ] El reconocedor y el micrófono se liberan antes de esperar el guardado.
- [ ] El servicio se limpia cuando termina el guardado.
- [ ] Sigue siendo imposible guardar dos veces.

Después, ejecutar tests, compilación y lint; detenerse.

## Bloque 6.6.3 — Fallback privado

### Problema

`MainActivity` está exportada como launcher y acepta `ACTION_PREPARE_WIDGET_VOICE` solo por su nombre. Otra aplicación puede imitar esa acción.

### Solución aprobada

No usar un token aleatorio: obligaría a persistirlo, renovarlo y coordinarlo entre varias instancias del widget.

Usar un `activity-alias` privado que apunte a la misma `MainActivity`:

```xml
<activity-alias
    android:name=".WidgetVoiceEntry"
    android:exported="false"
    android:targetActivity=".MainActivity" />
```

Declarar el alias después de `MainActivity`. Como no existe una clase Kotlin para el alias, construir su intent con un `ComponentName` explícito basado en `com.mvppostit.pensieve.WidgetVoiceEntry`.

Reglas:

1. Mantener `MainActivity` como launcher exportada.

2. El fallback del widget debe crear un `PendingIntent.getActivity(...)` explícito e inmutable dirigido a `.WidgetVoiceEntry`, no directamente a `MainActivity`.

3. `MainActivity` debe aceptar la acción únicamente cuando coincidan:

- `ACTION_PREPARE_WIDGET_VOICE`;
- el componente privado `.WidgetVoiceEntry`.

4. Si otra aplicación abre directamente `MainActivity` con la misma acción, Pensieve puede abrirse normalmente, pero no debe iniciar la voz.

5. No crear una segunda clase `Activity`, tokens, DataStore, receivers intermedios ni permisos personalizados.

6. Verificar manualmente en API 31 y 36 que el `PendingIntent` creado por Pensieve puede abrir el alias privado. Un `PendingIntent` se ejecuta con la identidad de su creador, pero este camino debe probarse con el launcher real.

### Aceptación

- [ ] El fallback real del widget abre el flujo visible de permisos.
- [ ] Una intent externa equivalente no inicia el micrófono.
- [ ] No existe estado persistente nuevo.
- [ ] Sigue habiendo una única clase de actividad.

Después, compilar, ensamblar, ejecutar lint y detenerse.

## Bloque 6.6.4 — Tamaño y limpieza

### Widget `1 × 1`

Añadir a `voice_capture_widget_info.xml`:

```xml
android:targetCellWidth="1"
android:targetCellHeight="1"
```

Reducir `minWidth` y `minHeight` a un valor compatible, aproximadamente `40–48dp`. Revisar conjuntamente el layout: el icono y el padding deben caber en la celda. Como referencia simple, usar un icono de `28–32dp` y padding cercano a `8dp`.

No añadir redimensionado, variantes de layout ni lógica adaptativa mientras no sean necesarias.

### Compose y comentarios

- Mover `modifier` para que sea el primer parámetro opcional de `HomeRoute`.
- No convertir el token y el callback en obligatorios solo para silenciar lint.
- Cambiar los comentarios que aún hablan de “lo que hará 6.4” para describir el comportamiento presente.

### Cobertura mínima

Añadir pruebas solo cuando puedan cubrir decisiones estables sin crear una arquitectura de mocks:

- una acción tardía no cancela el guardado;
- un segundo resultado no vuelve a guardar;
- un timeout no guarda;
- los intents del camino directo y del fallback apuntan al componente correcto.

Si una prueba depende del motor real, permisos o launcher, dejarla en la matriz manual. No extraer una capa de dominio únicamente para probar booleanos privados.

### Aceptación

- [ ] El widget solicita `1 × 1` y su contenido cabe sin recortes.
- [ ] Desaparece la advertencia nueva `ModifierParameter`.
- [ ] Los comentarios no mencionan subfases futuras ya completadas.
- [ ] La cobertura añadida no complica producción.

## Comprobación final

Después de completar los cuatro bloques:

```powershell
cd android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

El desarrollador debe comprobar manualmente:

- aparición inmediata de `Escuchando…`;
- cancelación normal durante la escucha;
- pulsación de un `Cancelar` tardío durante `Guardando…`;
- creación de una sola nota y su notificación;
- fallback real desde el widget;
- intento externo que no inicia voz;
- tamaño `1 × 1` después de retirar y volver a añadir el widget;
- varias instancias del widget.

No cerrar la fase 6 ni actualizar `AGENTS.md` o `CONTEXTO_MANANA.txt` hasta que estas correcciones sean reauditadas.

## Prompt para Terra

```text
Implementa exclusivamente el bloque 6.6.1 de Pensieve.

Lee AGENTS.md, CONTEXTO_MANANA.txt, GUIA_FASE_6_TERRA.md y
guias/fase_6/FASE_6_6_CORRECCIONES_AUDITORIA.md. Inspecciona el código actual
y git status antes de editar.

No implementes otros bloques, no añadas dependencias, no gestiones el
emulador y no hagas commit ni push. Mantén la solución mínima y conserva el
comportamiento no relacionado.

Ejecuta las comprobaciones indicadas, presenta el diff y detente para auditoría.
```
