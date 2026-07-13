# Pensieve

Pensieve es una aplicación Android nativa para capturar pensamientos o recordatorios con la mínima fricción y mantenerlos visibles hasta completarlos.

La idea central es sencilla: crear una nota por texto o voz, verla como recordatorio activo y eliminarla al pulsar **Hecho**. No pretende ser un gestor de tareas completo.

## Principios del producto

- Sin cuentas, backend, sincronización en la nube, publicidad ni analítica.
- Las notas se guardarán únicamente en el dispositivo.
- Sin categorías, prioridades, etiquetas, calendario ni alarmas.
- Sin almacenar el audio de las capturas por voz.
- Una única pantalla principal y pocos controles.

## Estado actual

La aplicación se encuentra en la **Fase 3 — Persistencia local**. Actualmente permite:

- Crear una nota manual.
- Completar una nota con una animación breve.
- Reordenar la lista automáticamente.
- Deshacer una finalización mediante un snackbar.
- Guardar y observar las notas con Room en la base de datos privada de la aplicación.
- Recuperar las notas almacenadas al volver a abrir la aplicación.

Las notificaciones persistentes, la captura por voz y el widget pertenecen a las siguientes fases.

## Tecnología

- Kotlin.
- Jetpack Compose y Material 3.
- Android nativo, `minSdk 31`.
- Un único módulo Gradle: `app`.
- Una única actividad: `MainActivity`.

Las siguientes fases incorporarán Room, DataStore, WorkManager, `SpeechRecognizer` y un App Widget, sin añadir backend ni conexión a Internet.

## Ejecutar el proyecto

1. Abre la carpeta `android/` con Android Studio.
2. Sincroniza Gradle y selecciona un emulador o dispositivo con Android 12 (API 31) o superior.
3. Ejecuta la configuración `app`.

También puedes comprobar el proyecto desde PowerShell:

```powershell
cd android
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

## Estructura relevante

```text
android/app/src/main/java/com/mvppostit/pensieve/
├── MainActivity.kt
├── PensieveApplication.kt
├── AppContainer.kt
├── data/
│   ├── local/
│   └── repository/
├── reminders/
└── ui/home/
```

Las referencias visuales y de flujo están en `diseño/`. Son material de consulta y no deben modificarse.

## Desarrollo

Las decisiones de alcance, privacidad, arquitectura y validación están documentadas en [AGENTS.md](AGENTS.md).
