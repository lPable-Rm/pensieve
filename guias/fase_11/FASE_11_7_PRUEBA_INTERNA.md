# Fase 11.7 — Prueba interna

## Objetivo

Validar el AAB real mediante la distribución de Google Play antes de iniciar el
periodo cerrado obligatorio.

## Pasos

1. Crear una versión en el canal de prueba interna.
2. Subir el AAB firmado y revisar los avisos automáticos.
3. Esperar el procesamiento y el informe previo al lanzamiento.
4. Instalar Nolvida desde el enlace de Google Play en un dispositivo físico.
5. Confirmar que la firma de Play no altera ningún flujo.

## Prueba de humo

- Instalación limpia y onboarding.
- Entrada manual y voz dentro de la app.
- Widget directo y fallback de permisos.
- Notificaciones, pantalla de bloqueo y acción `Hecho`.
- Deshacer, reconciliación y reinicio.
- Cambio de idioma del sistema y una paleta alternativa.
- Ausencia de cierres, ANR o errores del informe previo.

Si se corrige algo después de subir el AAB, incrementar `versionCode`, repetir
las comprobaciones release y subir un artefacto nuevo. No reutilizar un código
de versión ya consumido.

## Cierre

Cerrar cuando la instalación procedente de Play supere la prueba de humo y el
informe previo no contenga un bloqueo alto o medio sin resolver.
