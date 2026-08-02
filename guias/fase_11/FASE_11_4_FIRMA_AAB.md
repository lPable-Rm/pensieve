# Fase 11.4 — Firma y Android App Bundle

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

## Cierre

Cerrar cuando clave y copias estén bajo control del usuario, el AAB sea válido,
el APK firmado supere la prueba de humo y ningún secreto aparezca en Git.
