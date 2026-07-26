# AGENTS.md

## Proyecto

Este repositorio contiene **Nolvida**, una aplicación Android nativa para capturar pensamientos o recordatorios con la mínima fricción y mantenerlos visibles mediante notificaciones persistentes hasta que el usuario los complete.

Nombre comercial definitivo del producto: **Nolvida**.
Paquete Android actual: `com.mvppostit.pensieve` (identificador técnico; no
determina el nombre comercial).

La aplicación se publicará inicialmente en Google Play como una app de pago único, sin publicidad ni suscripciones.

## Objetivo principal

El flujo principal debe ser extremadamente simple:

1. El usuario captura una nota por voz o texto.
2. La nota se guarda solo en el dispositivo.
3. Se publica una notificación visible.
4. La nota permanece activa hasta que el usuario pulse **Hecho**.
5. Al completarla, se elimina la nota y su notificación.

La prioridad del producto es:

**mínima fricción + visibilidad persistente**

No convertir la aplicación en un gestor de tareas completo.

## Principios de producto

Mantener siempre estas restricciones:

- Sin cuentas, inicio de sesión ni perfiles online.
- Sin backend.
- Sin sincronización en la nube.
- Sin publicidad.
- Sin analítica.
- Sin historial de notas completadas.
- Sin categorías, carpetas, prioridades ni etiquetas.
- Sin calendario, fechas programadas ni alarmas.
- Sin inteligencia artificial generativa.
- Sin guardar el audio capturado.
- Sin conexión a Internet requerida.
- Las notas se almacenan únicamente en la base de datos local privada de la aplicación.
- Completar y eliminar una nota son la misma acción en el MVP.
- La interfaz debe tener una única pantalla principal y muy pocos controles.

Antes de proponer una función nueva, comprobar que refuerza el objetivo principal. Si añade complejidad sin reducir fricción o mejorar visibilidad, no añadirla.

## Plataforma y tecnología

- Android nativo.
- Kotlin.
- Jetpack Compose.
- Material 3.
- `minSdk 31`.
- Un único módulo Gradle: `app`.
- Una única actividad principal: `MainActivity`.
- Room para persistencia local.
- DataStore para preferencias locales.
- WorkManager para reconciliación periódica de notificaciones.
- `SpeechRecognizer` con reconocimiento en el dispositivo cuando esté disponible.
- App Widget para la entrada rápida por voz.

No introducir Hilt, una capa de dominio, casos de uso, múltiples módulos o Navigation Compose salvo que exista una necesidad clara y se apruebe expresamente.

## Arquitectura prevista

Estructura orientativa:

```text
app/src/main/java/com/mvppostit/pensieve/
├── MainActivity.kt
├── ui/
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   ├── HomeUiState.kt
│   │   └── components/
│   ├── appearance/
│   └── theme/
├── data/
│   ├── local/
│   ├── preferences/
│   └── repository/
├── voice/
├── notifications/
├── widget/
└── recovery/
```

Esta estructura es una guía. No crear archivos vacíos ni capas innecesarias por adelantado.

## Experiencia de usuario

### Pantalla principal

- Una única pantalla.
- Fondo claro y limpio.
- Cabecera con identidad de Nolvida.
- Notas ordenadas de la más reciente a la más antigua.
- Cada nota aparece en una tarjeta sencilla.
- Cada tarjeta tiene una única acción circular para marcarla como completada.
- No utilizar iconos decorativos que no aporten una acción real.

Al completar una nota:

1. Mostrar una animación breve.
2. Eliminar la nota de la interfaz.
3. Eliminarla de la base de datos.
4. Retirar su notificación.
5. Recolocar las notas restantes.
6. Mostrar un snackbar: `Recordatorio completado · Deshacer`.
7. Permitir restaurar brevemente la nota y su notificación.

### Entrada manual

En estado normal no debe existir un campo de texto vacío permanente.

Flujo:

1. Pulsar `Nueva nota`.
2. Abrir el teclado.
3. Mostrar un campo compacto con forma de burbuja encima del teclado o la lista.
4. Escribir la nota.
5. Confirmar mediante el botón de la interfaz o la acción del teclado.
6. Guardar localmente.
7. Publicar la notificación.
8. Ocultar el campo.

### Entrada por voz desde la aplicación

Flujo previsto:

1. Pulsar el botón de micrófono.
2. Mostrar una superficie visible y compacta de grabación.
3. Reproducir un bip.
4. Mostrar la transcripción parcial en directo.
5. Detener al pulsar o detectar el final del habla.
6. Transformar la misma barra en modo de revisión.
7. Confirmar.
8. Guardar la nota y publicar la notificación.

No abrir una pantalla adicional solo para confirmar el texto.

### Entrada rápida desde el widget

Flujo implementado:

1. Pulsar el widget `1 × 1`.
2. Iniciar directamente un foreground service de tipo `microphone`, sin abrir
   `MainActivity` durante el uso normal.
3. Mostrar inmediatamente la notificación temporal `Escuchando…` con la acción
   `Cancelar` mientras el micrófono está activo.
4. Reconocer la voz exclusivamente en el dispositivo.
5. Guardar automáticamente un único resultado final mediante
   `ReminderManager`, manteniendo Room como fuente de verdad.
6. Publicar la notificación normal del recordatorio y finalizar el servicio.

El widget no muestra parciales ni una revisión. Si faltan permisos, abre el
flujo visible de `MainActivity` mediante una entrada privada para que Android
pueda solicitarlos con una actividad en primer plano.

### Notificaciones

Cada nota activa tiene una notificación nativa mínima con:

- Icono de la aplicación.
- Nombre de la aplicación.
- Texto del recordatorio.
- Acción `Hecho`.
- Pulsar el cuerpo abre la aplicación.
- Pulsar `Hecho` completa y elimina la nota sin abrir la aplicación.

Si el usuario descarta una notificación pero la nota continúa activa, la aplicación debe reconciliarla y volver a publicarla silenciosamente. La reconciliación se ejecutará al abrir la app, tras reiniciar el dispositivo y periódicamente con WorkManager.

### Paletas

La primera versión tendrá varias paletas predefinidas almacenadas localmente:

- Lavanda.
- Verde petróleo.
- Azul profundo.
- Coral suave.
- Escala de grises.
- Turquesa.

No añadir un selector libre de colores en el MVP.

### Idiomas

- Nolvida seguirá automáticamente el idioma configurado en Android.
- La primera localización incluye inglés como fallback, español, alemán,
  francés, italiano y portugués de Portugal.
- No añadir un selector de idioma dentro de la aplicación.
- La interfaz, la voz, las notificaciones y el widget deben usar el mismo
  idioma del sistema.
- La estructura de recursos debe permitir añadir traducciones futuras sin
  cambiar la arquitectura.

## Seguridad y privacidad

La privacidad es parte central del producto.

- No añadir el permiso `INTERNET` mientras la aplicación sea completamente local.
- Permisos previstos: `RECORD_AUDIO`, `POST_NOTIFICATIONS`,
  `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE` y
  `RECEIVE_BOOT_COMPLETED`.
- Solicitar permisos solo cuando sean necesarios y explicar su propósito.
- No registrar en logs el texto de notas, transcripciones ni datos sensibles.
- No almacenar audio.
- Usar almacenamiento privado de la aplicación.
- Marcar correctamente componentes exportados y no exportados en el Manifest.
- Usar `PendingIntent` explícitos e inmutables cuando corresponda.
- No introducir Firebase, SDK de analítica, publicidad, seguimiento o telemetría.
- No añadir secretos, claves, keystores ni archivos de configuración privada al repositorio.
- No añadir dependencias de producción sin explicar el motivo y pedir aprobación.

## Forma de trabajar

El desarrollador está aprendiendo Android y Kotlin. Trabajar de manera didáctica.

- Responder y explicar en español.
- Hacer cambios pequeños y verificables.
- Explicar brevemente qué se ha cambiado y por qué.
- Añadir comentarios útiles al código nuevo cuando ayuden a aprender.
- No comentar cada línea obvia.
- Evitar reemplazar archivos completos si basta con modificar una sección.
- Mantener las funciones pequeñas y con una sola responsabilidad.
- Preferir nombres descriptivos.
- Priorizar código simple y legible frente a abstracciones prematuras.
- Inspeccionar siempre el código existente antes de editar.
- No borrar cambios del usuario sin autorización.
- No reformatear archivos no relacionados con la tarea.
- No realizar refactorizaciones amplias durante una corrección pequeña.
- No crear funciones o características no solicitadas.
- Ante una ambigüedad importante, preguntar antes de tomar una decisión irreversible.

## Reglas para Codex

Cuando se solicite un cambio:

1. Leer primero los archivos relacionados.
2. Resumir el problema encontrado.
3. Modificar únicamente lo necesario.
4. Mantener el comportamiento existente no relacionado.
5. Ejecutar la comprobación adecuada.
6. Informar de los archivos modificados y del resultado de la comprobación.

No hacer `git commit`, `git push`, crear ramas, cambiar el historial ni modificar remotos salvo petición explícita.

No instalar dependencias ni modificar versiones de Gradle, Kotlin, Compose o Android SDK salvo petición explícita.

No modificar archivos dentro de `diseño/`.

## Comprobaciones

Después de modificar código Kotlin o Compose, ejecutar desde `android/`:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Cuando el cambio afecte recursos, Manifest, Gradle o integración general, ejecutar:

```powershell
.\gradlew.bat :app:assembleDebug
```

Antes de considerar terminado un bloque funcional, revisar también:

```powershell
.\gradlew.bat lintDebug
```

Si un comando falla:

- No ocultar el error.
- Identificar la causa.
- Corregir solo si está dentro del alcance de la tarea.
- Explicar claramente cualquier problema que quede pendiente.

## Git

El repositorio Git está en la carpeta superior `pensieve/`.

Rama principal: `main`.

Flujo acordado:

1. Modificar.
2. Ejecutar y verificar.
3. Revisar `git status`.
4. Crear un commit pequeño por bloque funcional.
5. Hacer push.

La rama `main` debe mantenerse compilable.

## Fases del MVP

### Fase 0 — Base del proyecto

- Proyecto Compose.
- Repositorio Git.
- GitHub privado.
- Estructura inicial.

Estado: completada.

### Fase 1 — Pantalla principal estática

- Cabecera.
- Lista visual con datos de prueba.
- Tarjetas de recordatorio.
- Botones inferiores.
- Diseño basado en las referencias de `diseño/`.

Estado: completada.

### Fase 2 — Interacciones en memoria

- Crear una nota manual.
- Completar una nota.
- Reordenar la lista.
- Snackbar con deshacer.

Estado: completada.

### Fase 3 — Persistencia local

- Room.
- Entidad y DAO.
- Repositorio.
- Restauración de notas al reiniciar.

Estado: completada.

### Fase 4 — Notificaciones

- Canal de notificaciones.
- Una notificación por nota.
- Acción `Hecho`.
- Abrir la app al pulsar el cuerpo.

Estado: completada.

### Fase 5 — Captura por voz

- Permiso de micrófono.
- Reconocimiento local.
- Transcripción parcial.
- Revisión y confirmación compacta.
- Manejo de errores.

Estado: completada y auditada.

### Fase 6 — Widget

- Widget de acceso rápido `1 × 1`.
- Captura local directa mediante foreground service de micrófono.
- Guardado automático del resultado final.
- Fallback visible y privado para preparar permisos.

Estado: completada y auditada. Las pruebas manuales amplias se
realizarán en las fases 10 y 11.

### Fase 7 — Recuperación

- Reconciliación de notas y notificaciones.
- Inicio de la app.
- Reinicio del dispositivo.
- WorkManager periódico.

Estado: completada y auditada. Las pruebas manuales de recuperación se
realizarán en las fases 10 y 11.

### Fase 8 — Idiomas

- Inglés completo como fallback.
- Recursos localizados en español, alemán, francés, italiano y portugués de
  Portugal.
- Selección automática según el idioma del sistema Android.
- Coherencia de idioma en interfaz, voz, notificaciones y widget.
- Base preparada para añadir más traducciones.

Estado: completada y auditada. La matriz manual de idiomas se realizará en las
fases 10 y 11.

### Fase 9 — Apariencia y pulido visual

- Paletas predefinidas.
- Persistencia con DataStore.
- Mejora de la interfaz y revisión del diseño de los iconos.

### Fase 10 — Calidad

- Pruebas.
- Prueba específica de concurrencia entre completar o restaurar y reconciliar.
- Matriz manual pendiente de las fases 6, 7 y 8 en API 31 y API 36.
- Accesibilidad.
- Estados vacíos y errores.
- Rendimiento.
- Pulido visual.

### Fase 11 — Publicación comercial

- Revisión de seguridad.
- Revisión del Manifest.
- Release firmado.
- Android App Bundle.
- Política de privacidad.
- Declaración Data Safety.
- Pruebas en dispositivos físicos.
- Preparación de Google Play.

## Estado actual relevante

La aplicación compila y se ha ejecutado en un emulador Android API 36.1.
Las fases 1 a 8 están completadas y auditadas. La pantalla, la captura manual,
la captura local por voz y la acción nativa `Hecho` coordinan Room con una
notificación por nota mediante `ReminderManager` y el contenedor de
dependencias manual.

La captura por voz solicita `RECORD_AUDIO` únicamente al iniciarse, usa
`SpeechRecognizer` en el dispositivo sin respaldo online, muestra una barra
compacta de escucha y revisión y conserva la entrada manual como alternativa
cuando el permiso, el motor o el modelo local no están disponibles. No se
almacena audio ni se registran transcripciones.

El widget inicia `VoiceCaptureService` sin abrir la actividad cuando los
permisos están preparados. El servicio muestra inmediatamente una notificación
foreground cancelable, reutiliza `OnDeviceVoiceRecognizer`, ignora parciales y
guarda una sola transcripción final mediante `ReminderManager`. Durante
`Guardando…` libera el micrófono y no permite cancelar una escritura ya
aceptada. Sus notificaciones internas usan IDs negativos para no colisionar con
Room.

Si falta un permiso, un `PendingIntent` inmutable abre el alias privado
`WidgetVoiceEntry`, que apunta a la única `MainActivity`. La actividad rechaza
la misma acción si llega directamente a su componente exportado. La auditoría
automática de fase 6 finalizó sin errores; la matriz manual se aplaza por
decisión de producto a las fases 10 y 11.

La fase 7 reconcilia las notas activas de Room con las notificaciones visibles:
publica las ausentes, cancela las huérfanas e ignora las coincidencias. Un único
`Mutex` protege crear, completar, restaurar y reconciliar. La reconciliación se
ejecuta al entrar en primer plano, cada 15 minutos con WorkManager y después de
`BOOT_COMPLETED`, usando trabajos únicos con política `KEEP`. La auditoría
terminó sin hallazgos altos o medios. La prueba específica de concurrencia y la
matriz manual de recuperación se aplazan por decisión de producto a las fases
10 y 11.

La fase 8 usa los recursos nativos de Android con inglés completo en
`values/strings.xml` y traducciones en español, alemán, francés, italiano y
portugués de Portugal. La interfaz, las notificaciones, el widget y los
mensajes de voz resuelven `R.string`; el reconocedor conserva
`Locale.getDefault().toLanguageTag()`. No existe selector, `localeConfig`,
almacenamiento propio del idioma ni permiso `INTERNET`.

La auditoría de fase 8 terminó sin errores y las cuatro advertencias nuevas de
plurales se corrigieron con `many` en los idiomas que lo requieren. Se aceptan
como observaciones no bloqueantes los recursos regionales aportados por las
dependencias y que una notificación ya publicada pueda conservar su acción en
el idioma anterior si Android cambia de idioma en ese momento. No justifican
añadir complejidad al MVP. La matriz manual se aplaza a las fases 10 y 11.

Archivos principales del bloque actual:

```text
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeRoute.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeScreen.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeViewModel.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/components/VoiceInputBar.kt
android/app/src/main/java/com/mvppostit/pensieve/voice/OnDeviceVoiceRecognizer.kt
android/app/src/main/java/com/mvppostit/pensieve/voice/VoiceCaptureService.kt
android/app/src/main/java/com/mvppostit/pensieve/widget/VoiceCaptureWidgetProvider.kt
android/app/src/main/java/com/mvppostit/pensieve/data/local/
android/app/src/main/java/com/mvppostit/pensieve/data/repository/
android/app/src/main/java/com/mvppostit/pensieve/reminders/ReminderManager.kt
android/app/src/main/java/com/mvppostit/pensieve/notifications/
android/app/src/main/java/com/mvppostit/pensieve/recovery/
android/app/src/main/res/values/strings.xml
android/app/src/main/res/values-es/strings.xml
android/app/src/main/res/values-de/strings.xml
android/app/src/main/res/values-fr/strings.xml
android/app/src/main/res/values-it/strings.xml
android/app/src/main/res/values-pt-rPT/strings.xml
localizacion/TRADUCCIONES_FASE_8.csv
```

El siguiente bloque previsto es la fase 9, apariencia y pulido visual:
paletas predefinidas, persistencia local mediante DataStore, mejora de la
interfaz y revisión del diseño de los iconos. Debe diseñarse antes de modificar
recursos o código.

## Referencias de diseño

Las referencias visuales están en:

```text
diseño/
```

Archivos conocidos:

- `MVP_POSTIT_flujo_tecnico.png`
- `PantallaApp.png`
- `bocetografico.png`
- `coloresBoceto.png`
- `diseñoNotificacion.png`
- `flujodevoz.png`
- `paletaDeColores.png`
- `pantallaApp1.1.png`

Usarlas como guía visual. No editarlas ni eliminarlas.
