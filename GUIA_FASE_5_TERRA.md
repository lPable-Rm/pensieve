# Guía de implementación — Fase 5 con ChatGPT 5.6 Terra

## Propósito

Esta guía sirve para que el chat de implementación construya la Fase 5
—captura por voz— mientras el chat habitual de Pensieve se reserva para
auditar el resultado y proponer correcciones.

La implementación debe realizarse por bloques pequeños, compilables y fáciles
de revisar. No se debe avanzar hacia el widget ni hacia la reconciliación de
notificaciones.

## Lectura obligatoria antes de editar

Terra debe leer completos, en este orden:

1. `AGENTS.md`.
2. `CONTEXTO_MANANA.txt`.
3. Esta guía.
4. `diseño/flujodevoz.png`.
5. Los archivos actuales relacionados:
   - `android/app/src/main/AndroidManifest.xml`
   - `android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeUiState.kt`
   - `android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeViewModel.kt`
   - `android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeRoute.kt`
   - `android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeScreen.kt`
   - `android/app/src/main/java/com/mvppostit/pensieve/reminders/ReminderManager.kt`
   - `android/app/src/main/res/values/strings.xml`
   - pruebas actuales relacionadas.

También debe ejecutar `git status --short` antes de editar y conservar cualquier
cambio previo del usuario. No debe modificar ningún archivo de `diseño/`.

## Decisiones ya aprobadas

- El reconocimiento será exclusivamente local.
- Usar `SpeechRecognizer.isOnDeviceRecognitionAvailable()` y
  `SpeechRecognizer.createOnDeviceSpeechRecognizer()`.
- No usar `createSpeechRecognizer()` como respaldo.
- No confiar en `EXTRA_PREFER_OFFLINE` como garantía de privacidad.
- Si no existe motor o modelo local, informar y mantener disponible la entrada
  manual.
- No añadir `INTERNET`.
- No guardar audio, buffers ni archivos temporales.
- No registrar transcripciones, texto de notas ni objetos `Bundle`.
- Solicitar `RECORD_AUDIO` únicamente al pulsar el micrófono.
- Mantener una sola `MainActivity` y una sola pantalla.
- Mostrar grabación, procesamiento, revisión y errores en una barra compacta
  dentro de `HomeScreen`.
- No guardar automáticamente al finalizar el habla: siempre habrá revisión.
- Reutilizar `ReminderManager.createReminder()` para Room y notificaciones.
- No añadir dependencias, Hilt, capa de dominio, casos de uso, servicios,
  módulos, Navigation Compose ni otro ViewModel.

## Arquitectura acordada

```text
Botón de micrófono
        │
        ▼
HomeRoute
  permiso + ciclo de vida
        │
        ▼
OnDeviceVoiceRecognizer
        │ callbacks
        ▼
HomeViewModel ── VoiceInputState
        │
        ▼
HomeScreen ── VoiceInputBar
        │ confirmar
        ▼
ReminderManager ── Room + notificación
```

Responsabilidades:

- `HomeRoute`: permisos, ciclo de vida y coordinación con APIs Android.
- `OnDeviceVoiceRecognizer`: envolver únicamente `SpeechRecognizer`.
- `HomeViewModel`: estado, transcripción editable y guardado.
- `HomeScreen` y `VoiceInputBar`: interfaz pura mediante estado y callbacks.
- `ReminderManager`: conservar el flujo existente de persistencia y
  notificaciones.

No introducir el reconocedor en `HomeViewModel`, `AppContainer` o
`MainActivity`.

## Estado mínimo

Usar un estado cerrado para impedir combinaciones imposibles:

```text
Hidden
  → Listening
  → Processing
  → Review
  → Guardando
  → Hidden

Listening / Processing
  → Error
  → Reintentar o cancelar
```

Estados:

- `Hidden`: no hay barra de voz.
- `Listening`: muestra escucha activa, tiempo, indicador visual, parcial si
  existe y acción `Detener`.
- `Processing`: muestra `Transcribiendo…` y espera el resultado final.
- `Review`: muestra el texto editable y acciones `Cancelar` y `Guardar`.
- `Error`: muestra mensaje y acción contextual.
- El guardado puede reutilizar `isCreatingReminder`; no crear otro sistema
  paralelo si no es necesario.

Errores visibles, agrupados por la acción que permiten:

- `NoSpeech`: silencio o texto no reconocido; permite reintentar.
- `LanguageUnavailable`: idioma o modelo local no disponible.
- `RecognizerUnavailable`: falta micrófono o reconocimiento local.
- `PermissionDenied`: permite abrir los ajustes del micrófono.
- `TemporaryFailure`: permite reintentar.

No reproducir en la interfaz todos los códigos internos de Android.

## Archivos previstos

Crear:

- `android/app/src/main/java/com/mvppostit/pensieve/voice/OnDeviceVoiceRecognizer.kt`
- `android/app/src/main/java/com/mvppostit/pensieve/ui/home/components/VoiceInputBar.kt`
- las pruebas mínimas que se utilicen realmente.

Modificar:

- `android/app/src/main/AndroidManifest.xml`
- `HomeUiState.kt`
- `HomeViewModel.kt`
- `HomeRoute.kt`
- `HomeScreen.kt`
- `strings.xml`
- pruebas existentes cuando su construcción cambie.

No deberían cambiar:

- `MainActivity.kt`
- `AppContainer.kt`
- `ReminderManager.kt`
- repositorio, DAO, entidad o base de datos
- archivos Gradle y catálogo de versiones
- implementación de notificaciones
- archivos de `diseño/`

Si Terra detecta una necesidad real de modificar uno de estos archivos, debe
explicarla antes de hacerlo y elegir primero la alternativa más pequeña.

## Bloque 5.1 — Plataforma y controlador local

### Manifest

Añadir:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<uses-feature
    android:name="android.hardware.microphone"
    android:required="false" />

<queries>
    <intent>
        <action android:name="android.speech.RecognitionService" />
    </intent>
</queries>
```

Antes de pedir permiso, comprobar también que el dispositivo dispone de
micrófono. La entrada por texto debe seguir funcionando si no lo tiene.

### Controlador

`OnDeviceVoiceRecognizer` debe:

- ejecutarse y llamar a `SpeechRecognizer` desde el hilo principal;
- comprobar disponibilidad local antes de crear el reconocedor;
- instalar `RecognitionListener` antes de iniciar;
- usar `ACTION_RECOGNIZE_SPEECH`;
- usar lenguaje de texto libre;
- solicitar resultados parciales;
- limitarse al primer resultado;
- respetar el idioma predeterminado del dispositivo;
- exponer operaciones pequeñas: `start`, `stop`, `cancel` y `destroy`;
- llamar siempre a `destroy()` al terminar;
- ignorar callbacks tardíos después de cancelar;
- no usar ni almacenar `onBufferReceived`;
- tratar `onEndOfSpeech` como paso a `Processing`, no como resultado final;
- aceptar que los resultados parciales pueden no llegar;
- transformar errores Android en los pocos errores visibles acordados.

El bip inicial debe producirlo Pensieve con un `ToneGenerator` corto. Debe
respetar el volumen del dispositivo, liberarse correctamente y no ser la única
señal de que la captura ha empezado.

### Comprobación del bloque

Desde `android/`:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Después, revisar el diff en el chat de auditoría antes de continuar.

## Bloque 5.2 — Estado, permisos y ciclo de vida

### HomeUiState y HomeViewModel

- Añadir el estado de voz a `HomeUiState`.
- Mantener manual y voz como entradas mutuamente excluyentes.
- Conservar el parcial más reciente mientras se escucha.
- Reemplazar el parcial por el resultado final.
- Permitir editar la revisión.
- No permitir confirmaciones dobles.
- Conservar la revisión si Room falla.
- Limpiar el estado solo después de un guardado correcto o una cancelación
  explícita.

### HomeRoute

- Conectar el callback `onVoiceNoteClick` ya existente.
- Solicitar `RECORD_AUDIO` solo tras esa pulsación.
- Crear el controlador en el contexto de la pantalla, no en el contenedor
  global.
- Cancelar la escucha cuando la aplicación pasa a segundo plano.
- Destruir el controlador al abandonar la composición.
- Hacer que Atrás cancele primero la captura o revisión activa.
- Ignorar callbacks pertenecientes a una sesión ya cancelada.
- Si se deniega el permiso, mantener la entrada manual operativa.
- Si el permiso queda bloqueado, ofrecer `Abrir ajustes`.

Al confirmar una transcripción, reutilizar la comprobación actual de
notificaciones. Si el permiso o el canal están bloqueados, conservar el texto
de revisión.

### Comprobación del bloque

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
```

Después, llevar el diff y el resultado al chat de auditoría.

## Bloque 5.3 — Barra compacta y accesibilidad

`VoiceInputBar` debe aparecer entre la cabecera y la lista. La lista permanece
visible y no se abre ninguna pantalla o diálogo adicional.

### Escuchando

- Texto `Escuchando…` hasta que exista parcial.
- Parcial visible cuando el proveedor lo entregue.
- Tiempo transcurrido.
- Indicador animado sencillo y decorativo.
- Acción clara `Detener grabación`.

No construir una forma de onda real ni depender de `onRmsChanged` para que el
flujo funcione.

### Procesando

- Texto `Transcribiendo…`.
- Progreso sencillo.
- Posibilidad de cancelar para no dejar a la persona atrapada.

### Revisión

- Texto editable, con varias líneas.
- No abrir el teclado automáticamente.
- Acción `Cancelar`.
- Acción `Guardar recordatorio`.
- Estado `Guardando…` con controles bloqueados.

### Error

- Mensaje corto.
- Acción `Reintentar` cuando corresponda.
- Acción `Abrir ajustes` para permiso denegado.
- Posibilidad de cerrar la barra y usar texto.

### Accesibilidad

- Áreas táctiles mínimas de 48 dp.
- Descripciones completas de todas las acciones.
- El indicador visual no se anuncia como contenido.
- El tiempo se anuncia como segundos, no solo `0:15`.
- No anunciar cada parcial con TalkBack.
- El bip nunca es la única señal.
- Comprobar texto grande y varias líneas.

No copiar del boceto el buscador, perfil, selección múltiple ni otros elementos
que no existen en el producto actual.

### Comprobación del bloque

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
```

Revisar visualmente y volver al chat de auditoría.

## Bloque 5.4 — Guardado, errores y pruebas

El guardado por voz debe terminar en el mismo:

```text
ReminderManager.createReminder(texto)
  → Room
  → notificación
```

Requisitos:

- recortar espacios;
- no guardar texto vacío;
- impedir doble pulsación;
- conservar el texto si falla Room;
- conservarlo si no pueden publicarse notificaciones;
- ocultar la barra solo después del éxito;
- no modificar el flujo manual no relacionado.

Pruebas automatizadas mínimas:

- parcial → final → revisión;
- detener → procesamiento;
- resultado vacío → error sin voz;
- error → reintento;
- edición de transcripción;
- confirmación correcta;
- doble confirmación bloqueada;
- fallo de Room conserva la revisión;
- permiso denegado mantiene disponible la entrada manual;
- estados visuales principales de `VoiceInputBar`;
- cancelación al abandonar la pantalla.

No intentar validar reconocimiento real reproduciendo audio en una prueba
instrumentada: depende del dispositivo, proveedor y modelos instalados. Simular
los eventos controlables y reservar el audio real para la matriz manual.

## Bloque 5.5 — Validación final

Ejecutar:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Matriz manual obligatoria:

- API 31 y API 36.
- Al menos un dispositivo físico.
- Permiso concedido.
- Permiso denegado.
- Permiso concedido solo una vez.
- Dispositivo sin motor local disponible.
- Modelo del idioma disponible y no disponible.
- Modo avión.
- Silencio inicial.
- Detención manual.
- Final automático del habla.
- Parciales disponibles y ausencia de parciales.
- Edición antes de guardar.
- Salir a segundo plano durante la escucha.
- Confirmación que crea fila Room y notificación.
- Notificaciones bloqueadas conservando la revisión.
- Verificación de que no se crea ningún archivo de audio.

Al terminar:

1. Revisar `git diff --check`.
2. Revisar `git status --short`.
3. Enumerar archivos modificados.
4. Informar de todas las comprobaciones y advertencias.
5. No hacer commit ni push sin petición explícita.
6. No marcar la Fase 5 como terminada hasta superar la matriz manual y la
   auditoría final.

## Criterios de aceptación

La Fase 5 solo puede considerarse terminada cuando:

- el permiso de micrófono se solicita en contexto;
- la captura es exclusivamente local;
- la aplicación funciona por texto sin micrófono o reconocimiento local;
- se muestra una barra compacta dentro de la pantalla principal;
- el parcial aparece cuando el proveedor lo entrega;
- detener o finalizar el habla conduce a revisión;
- el texto puede editarse antes de guardar;
- guardar utiliza Room y las notificaciones actuales;
- los borradores sobreviven a errores de persistencia o permisos;
- la escucha se cancela al ir a segundo plano;
- el reconocedor se destruye siempre;
- no se guarda audio ni se registran transcripciones;
- las pruebas automáticas y la matriz manual terminan correctamente;
- el chat de auditoría aprueba el bloque.

## Prompt maestro para Terra

Copiar y pegar este texto en el chat de implementación:

```text
Actúa como implementador principal de la Fase 5 de Pensieve.

Antes de editar, lee completos AGENTS.md, CONTEXTO_MANANA.txt y
GUIA_FASE_5_TERRA.md. Después inspecciona los archivos Android relacionados y
ejecuta git status --short. Conserva cualquier cambio previo del usuario.

Implementa la Fase 5 siguiendo estrictamente GUIA_FASE_5_TERRA.md y sus bloques
5.1 a 5.5. La voz debe ser exclusivamente local mediante
createOnDeviceSpeechRecognizer; no uses respaldo online, no añadas INTERNET, no
guardes audio y no registres notas o transcripciones.

Mantén la arquitectura simple: un único controlador Android de voz, un único
VoiceInputBar, estado en HomeViewModel, permisos y ciclo de vida en HomeRoute,
y guardado mediante ReminderManager. No añadas Hilt, dominio, casos de uso,
repositorio de voz, otra actividad, otro ViewModel, servicios, módulos,
Navigation Compose ni dependencias nuevas.

Trabaja por bloques pequeños. Antes de cada bloque lee los archivos afectados,
resume brevemente el problema y modifica solo lo necesario. Ejecuta las
comprobaciones indicadas después de cada bloque. No ocultes errores.

Al completar cada punto de auditoría, detente y entrega:
- resumen de lo implementado;
- archivos modificados;
- decisiones relevantes;
- comandos ejecutados y resultados;
- riesgos o validaciones manuales pendientes;
- git status --short.

No hagas commit ni push. No modifiques diseño/. Si encuentras una necesidad que
contradice la guía o exige una dependencia, detente y pide autorización.

Empieza por el Bloque 5.1 y deja el resultado listo para auditar.
```

## Uso recomendado entre chats

1. Terra implementa un bloque.
2. Copiar su resumen y traer el estado del repositorio a este chat.
3. Este chat audita el diff, la simplicidad, privacidad y pruebas.
4. Si se aprueba, pedir a Terra el siguiente bloque.
5. Tras el bloque 5.5, realizar aquí la auditoría final antes de actualizar el
   estado de la fase.
