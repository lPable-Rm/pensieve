# Fase 11.4 — Firma y Android App Bundle

Estado: **completada**.

## Objetivo

Crear una clave de subida segura, activar Play App Signing y generar el primer
AAB firmado sin introducir secretos en el repositorio.

## Responsabilidad del usuario

El usuario realizará el asistente `Generate Signed Bundle / APK` de Android
Studio. Nunca compartirá contraseñas, clave privada, códigos de recuperación ni
el contenido del `.jks` con un agente.

## Pasos

1. Crear una clave de subida con validez amplia y alias descriptivo.
2. Guardar el `.jks` fuera de `pensieve/`.
3. Conservar al menos dos copias seguras en ubicaciones diferentes.
4. No guardar contraseñas dentro de Gradle, Git o capturas.
5. Generar un AAB release firmado.
6. Activar Play App Signing al efectuar la primera subida.
7. Verificar firma, `applicationId`, `versionCode` y `versionName`.
8. Generar también un APK release firmado para la prueba física local.

Cada AAB nuevo subido a Google Play necesita un `versionCode` superior. No se
incrementará por ensayos locales que no se suban.

## Prueba de humo release

Instalar el APK firmado en un dispositivo físico limpio y comprobar onboarding,
texto, voz, widget, notificaciones, `Hecho`, deshacer y reinicio.

## Resultado

El usuario creó la clave de subida mediante Android Studio, la mantiene fuera
del repositorio y confirmó que dispone de una copia segura adicional. Las
contraseñas y la clave privada permanecen exclusivamente bajo su control.

Los artefactos finales de la versión `1.0` se conservan fuera de `pensieve/` y
están separados por formato:

```text
NolvidaRelease/1.0/
├── aab/release/
│   ├── app-release.aab
│   └── mapping.txt
└── apk/release/
    └── app-release.apk
```

Comprobaciones del AAB:

- tamaño: 4.307.531 bytes (4,11 MiB);
- SHA-256:
  `5E3DA5E31580E7F38C8BC67CC31B186FE26F20367F8A77E0CDFEC9AFD8793BCC`;
- firma JAR válida;
- `mapping.txt` conservado, con SHA-256
  `2A82BAAAD23519BDE78970873A2C0A5FAB6828B4F69C4612AE9C80929E483350`.

Comprobaciones del APK:

- tamaño: 3.362.202 bytes (3,21 MiB);
- SHA-256:
  `F6FC8383A056611A8F4AB373FEEBE3DA9DC479FCBB426396EAFC5F530DCE0290`;
- firma APK V2 válida y un único firmante;
- paquete `com.mvppostit.pensieve`, `versionCode` 1, `versionName` 1.0 y
  `targetSdk` 36.

El certificado público SHA-256 coincide en el AAB y el APK:

```text
922edaf4f433db6b73d61f8d1a4dc31b7e867bfcafd0fe16fd2afb3388a41b16
```

El APK release se instaló correctamente en un Vivo V2130 y un Pixel 10. El
usuario confirmó que los flujos principales funcionan correctamente en ambos
dispositivos.

La revisión del repositorio no encontró claves, almacenes, contraseñas, APK ni
AAB dentro de Git o del árbol fuente. Play App Signing se activará con la
primera subida a Google Play, tal como indica el paso 6; todavía no se ha subido
ningún artefacto.

## Cierre

La subfase queda cerrada: la clave y sus copias están bajo control del usuario,
el AAB es válido, el APK firmado superó la prueba de humo física y ningún
secreto aparece en Git. La siguiente subfase local es 11.5.
