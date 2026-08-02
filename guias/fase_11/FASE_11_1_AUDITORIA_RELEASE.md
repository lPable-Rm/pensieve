# Fase 11.1 — Auditoría técnica de release

Estado: **completada y auditada el 2 de agosto de 2026**.

## Objetivo

Comprobar la configuración real antes de activar R8, firmar o subir archivos.
Esta subfase es principalmente de lectura y no corrige hallazgos inesperados.

## Revisiones

1. Revisar `build.gradle.kts`, catálogo de versiones y wrapper.
2. Confirmar `applicationId`, `namespace`, API 31, target 36 y versión 1.0.
3. Revisar Manifest fuente y fusionado de release.
4. Inventariar permisos, componentes exportados y tipos de foreground service.
5. Confirmar ausencia de `INTERNET`, secretos, telemetría y logs sensibles.
6. Revisar dependencias incluidas en runtime.
7. Confirmar que tests, documentación y `diseño/` no forman parte del paquete.
8. Generar una línea base release todavía no subida a Play Console.

## Comprobaciones

Desde `android/`:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:bundleRelease
.\gradlew.bat lintDebug
```

Revisar también `git diff --check` y `git status --short` desde la raíz.

## Cierre

Documentar tests, lint, permisos, dependencias, tamaño del AAB no firmado y
cualquier diferencia. Un hallazgo alto o medio detiene el avance antes de 11.2.

## Resultado real

La auditoría termina con **0 hallazgos altos y 0 medios**. La configuración
actual es una base válida para preparar el release comercial en las siguientes
subfases. No se modificó código de producción, el Manifest ni las versiones.

### Configuración validada

- `namespace` y `applicationId`: `com.mvppostit.pensieve`.
- Versión inicial: `versionCode 1` y `versionName "1.0"`.
- `minSdk 31`, `targetSdk 36` y `compileSdk 36.1`.
- Un único módulo Android: `app`.
- La variante `release` todavía tiene `optimization.enable = false`.
- La configuración de firma de `release` está vacía.

Los dos últimos puntos son el estado previo deliberado: R8 se activará en 11.3
y la firma se configurará en 11.4. No son fallos de esta auditoría.

### Seguridad, privacidad y paquete

- El Manifest fusionado de release no contiene `INTERNET`.
- Conserva únicamente los permisos funcionales de Nolvida y los permisos
  normales `WAKE_LOCK` y `ACCESS_NETWORK_STATE` incorporados por WorkManager.
- `VoiceCaptureService`, `CompleteReminderReceiver` y el alias privado del
  widget no están exportados.
- `MainActivity`, el proveedor del widget y el receiver de reinicio se exportan
  únicamente porque sus entradas del sistema lo requieren.
- Los componentes exportados aportados por AndroidX están protegidos por
  permisos del sistema cuando corresponde.
- No se encontraron Firebase, analítica, publicidad, telemetría, clientes HTTP,
  URLs de producción, logs con notas ni audio almacenado.
- No se encontraron keystores, certificados, archivos `.env`, credenciales ni
  otros secretos en el repositorio.
- El runtime contiene las dependencias AndroidX, Kotlin, Compose, Room,
  WorkManager y DataStore previstas; no se añadió ninguna dependencia.
- El AAB no incluye fuentes Kotlin/Java, tests ni los directorios `diseño/`,
  `guias/` o `localizacion/`. Sí incluye archivos `LICENSE.txt` de AndroidX,
  que son metadatos normales de sus dependencias.

### Comprobaciones ejecutadas

Desde `android/` se ejecutaron conjuntamente:

```powershell
.\gradlew.bat :app:testDebugUnitTest `
  :app:compileDebugAndroidTestKotlin `
  :app:assembleDebug `
  :app:bundleRelease `
  lintDebug
```

Resultados:

- Gradle: `BUILD SUCCESSFUL`.
- Tests unitarios: 17 ejecutados, 0 fallos, 0 errores y 0 omitidos.
- Tests instrumentados: 30 métodos compilados; no se ejecutaron en dispositivo
  porque esta subfase solo establece la línea base de build.
- APK debug: generado correctamente, 33,60 MB.
- AAB release: generado correctamente, 8,71 MB.
- Lint: 0 errores y 11 advertencias de versiones ya aceptadas.
- `lintVitalRelease`: correcto.
- Dependencias de runtime y `signingReport`: correctos.

El archivo generado en
`android/app/build/outputs/bundle/release/app-release.aab` está sin firmar. Es
solo evidencia local y deberá sustituirse por el AAB optimizado y firmado de
11.3–11.4; no debe subirse a Google Play.

### Observación baja aceptada

Gradle no pudo retirar símbolos de dos bibliotecas nativas transitivas de
AndroidX (`libandroidx.graphics.path.so` y
`libdatastore_shared_counter.so`) y las empaquetó sin cambios. No afecta al
funcionamiento, privacidad ni firma; su posible impacto se limita al tamaño.
Se volverá a observar al generar el AAB optimizado, sin añadir reglas o
dependencias preventivas.

### Decisión para 11.3

El proyecto ya usa el DSL de optimización de AGP 9.2. La corrección mínima será
cambiar `optimization.enable` de `false` a `true` y utilizar el archivo
`src/main/keepRules/rules.keep` existente únicamente si aparece una necesidad
reproducible. No hace falta introducir por adelantado la configuración antigua
de ProGuard.
