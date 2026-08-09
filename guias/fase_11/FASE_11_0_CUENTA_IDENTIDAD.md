# Fase 11.0 — Cuenta e identidad

Estado: **completada**.

## Objetivo

Fijar la identidad comercial y completar la cuenta antes de crear o subir el
primer artefacto de Nolvida.

## Decisiones cerradas

- Cuenta personal nueva de Google Play Console.
- Nombre público del desarrollador: `Pramos`.
- Correo público de soporte: `nolvida@pramos.dev`.
- Nombre de la aplicación: `Nolvida`.
- Identificador definitivo: `com.mvppostit.pensieve`.
- Aplicación de pago único.
- Cuenta registrada y tasa abonada.
- Identidad de la cuenta verificada por Google.

El correo propietario y los datos legales permanecen fuera del repositorio.

## Resultado

- La identidad de la cuenta personal está verificada.
- Play Console no mostró una tarea pendiente de verificación del dispositivo
  al crear la aplicación. Si Google la solicita más adelante, deberá
  completarse antes de distribuirla.
- El perfil público muestra `Pramos`.
- Play Console aceptó `com.mvppostit.pensieve` durante la creación.
- El borrador de `Nolvida` está creado como aplicación de pago, con inglés de
  Estados Unidos como idioma predeterminado.
- Se aceptaron las declaraciones de políticas, Play App Signing y legislación
  de exportación.
- No se ha subido todavía ningún AAB.

Google intentará registrar automáticamente el paquete para la verificación de
desarrolladores de Android. Su estado formal se volverá a comprobar después de
la primera subida, sin iniciar ahora un registro manual paralelo.

Al crear el borrador se seleccionará aplicación, no juego, y el modelo de pago
acordado. No se elegirá `Gratis` por error: una aplicación que ya se ha ofrecido
gratis no puede volver a convertirse en aplicación de pago con el mismo
paquete.

## Condición de la cuenta personal nueva

Antes de solicitar producción será necesaria una prueba cerrada con al menos
12 participantes inscritos de forma continua durante 14 días. La prueba
interna no sustituye este requisito.

## Cierre

La subfase queda cerrada: la cuenta está verificada, no existe una solicitud
visible de dispositivo, el paquete fue aceptado y el borrador de Nolvida existe.
Solo se han registrado estados no sensibles.
