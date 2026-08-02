# Política de privacidad de Nolvida

> **Contenido funcional aprobado y página web publicada. Antes de introducir
> esta política o enviar declaraciones en Google Play deben sustituirse en la
> web todos los campos `[PENDIENTE]` indicados en este maestro. Esos datos
> públicos no se guardarán en el repositorio de Nolvida.**

Versión del documento: 1.0-borrador  
Última actualización: 2 de agosto de 2026

## Resumen de privacidad

- Nolvida no requiere una cuenta.
- Las notas y preferencias se guardan únicamente en el dispositivo.
- El reconocimiento de voz se realiza en el dispositivo y el audio no se
  almacena.
- Nolvida no tiene backend y no incluye publicidad, analítica ni seguimiento.
- Pramos no recibe, vende ni comparte las notas, las transcripciones o el audio.
- El usuario controla y elimina sus notas directamente desde la aplicación.

Este resumen facilita la lectura, pero la información completa se encuentra en
los apartados siguientes.

## 1. ¿Quién es responsable de Nolvida?

Nolvida es una aplicación publicada bajo el nombre comercial **Pramos**.

Antes de publicar esta política deben completarse los siguientes datos en la
página web, sin guardarlos en el repositorio de Nolvida:

- **Responsable legal:** `[PENDIENTE: NOMBRE O RAZÓN SOCIAL]`
- **Nombre comercial:** Pramos
- **Dirección postal de contacto:** `[PENDIENTE: DIRECCIÓN PÚBLICA]`
- **Correo de privacidad y soporte:** nolvida@pramos.dev

## 2. ¿A qué se aplica esta política?

Esta política explica el funcionamiento de la aplicación Android **Nolvida** y
distingue entre:

1. la información que Nolvida procesa localmente en el dispositivo;
2. la información que una persona decide enviar al contactar por correo; y
3. los datos técnicos necesarios para consultar esta política en la web.

Nolvida no ofrece cuentas, sincronización, copias en la nube ni servicios
online propios.

## 3. ¿Qué información utiliza Nolvida en el dispositivo?

### Notas y transcripciones

Nolvida guarda en su base de datos local:

- el texto introducido manualmente por el usuario;
- el texto resultante de una captura por voz cuando el usuario decide guardarlo;
  y
- la fecha interna necesaria para ordenar e identificar cada nota.

El contenido de una nota lo decide exclusivamente el usuario y podría incluir
información personal o sensible. Pramos no puede ver ni recuperar ese contenido.

Nolvida utiliza el texto para mostrar la nota en la pantalla principal y en una
notificación de Android. Según la configuración elegida por el usuario, Android
también puede mostrar ese texto en la pantalla de bloqueo.

### Micrófono y reconocimiento de voz

Nolvida solicita acceso al micrófono únicamente cuando el usuario inicia una
captura por voz desde la aplicación o pulsa el widget.

La aplicación usa la modalidad de reconocimiento en el dispositivo ofrecida por
Android. El audio se procesa mediante el servicio local de reconocimiento
disponible en el teléfono para producir una transcripción:

- Nolvida no crea ni conserva un archivo de audio;
- el audio no se envía a Pramos;
- no se integra ningún servicio de transcripción en la nube; y
- si no está disponible el reconocimiento local, Nolvida no utiliza un respaldo
  online y permite recurrir a la entrada manual.

La captura iniciada desde el widget puede ejecutarse aunque la pantalla
principal de Nolvida no esté abierta. Siempre comienza por una acción del
usuario y Android muestra una notificación visible mientras el micrófono está
activo.

### Preferencias

Nolvida guarda localmente si se ha completado la introducción inicial y cuál de
las paletas de color disponibles ha elegido el usuario. Estas preferencias no se
envían a Pramos.

## 4. ¿Para qué se utiliza esta información?

La información local se utiliza exclusivamente para ofrecer las funciones que
el usuario solicita:

- crear y mostrar notas pendientes;
- transcribir una captura de voz;
- mantener una notificación por cada nota activa;
- completar y restaurar brevemente una nota; y
- conservar la apariencia seleccionada.

Nolvida no utiliza la información para publicidad, elaboración de perfiles,
medición de audiencia, entrenamiento de sistemas de inteligencia artificial o
decisiones automatizadas.

Cuando resulte aplicable el Reglamento General de Protección de Datos, el
tratamiento local necesario para proporcionar estas funciones se fundamenta en
la ejecución del servicio solicitado por el usuario. Los permisos concedidos en
Android son controles técnicos del sistema y no se utilizan como autorización
para ninguna finalidad adicional.

## 5. ¿Qué permisos y funciones de Android utiliza Nolvida?

- **Micrófono:** para convertir en texto una nota de voz iniciada por el usuario.
- **Notificaciones:** para mantener visibles las notas pendientes y ofrecer la
  acción `Hecho`.
- **Servicio de micrófono en primer plano:** para completar la captura iniciada
  desde el widget, con una notificación visible y cancelable.
- **Inicio del dispositivo:** para reconstruir las notificaciones de las notas
  pendientes después de reiniciar el teléfono.
- **Trabajo periódico local:** para comprobar en segundo plano que cada nota
  activa conserva su notificación.

El permiso del micrófono puede rechazarse o revocarse desde los ajustes de
Android; la entrada manual seguirá disponible. Las notificaciones y la
visibilidad de su contenido en la pantalla de bloqueo también pueden gestionarse
desde los ajustes del sistema.

Estas capacidades no se utilizan para enviar información fuera del dispositivo.
La aplicación no solicita el permiso de acceso a Internet.

## 6. ¿Se comparten o venden datos?

No. Nolvida no transmite a Pramos ni a terceros las notas, transcripciones,
audio o preferencias. No contiene SDK propio de publicidad, analítica,
telemetría, redes sociales o informes remotos de errores.

Los servicios locales de Android que permiten mostrar notificaciones, alojar el
widget, reconocer voz en el dispositivo y ejecutar tareas programadas forman
parte del funcionamiento del sistema. Nolvida no los utiliza para crear un
perfil del usuario ni para compartir información con Pramos.

La compra y distribución de Nolvida se gestionan mediante Google Play. La
aplicación no recibe datos de tarjetas ni integra un sistema propio de pagos. El
tratamiento realizado directamente por Google se rige por sus propias
condiciones y políticas.

Si el usuario ha aceptado en Android compartir diagnósticos y uso con Google,
Google Play puede recopilar información técnica sobre estabilidad y rendimiento
y mostrar a Pramos estadísticas o informes de Android vitals. Este mecanismo
pertenece a Google Play, no incorpora las notas o el audio por decisión de
Nolvida y no sustituye a un SDK de analítica dentro de la aplicación.

No se realizan transferencias internacionales de las notas, transcripciones,
audio o preferencias porque esta información no sale del dispositivo.

## 7. ¿Durante cuánto tiempo se conserva la información?

Una nota permanece en la base de datos local hasta que el usuario pulsa
`Hecho`. En ese momento se elimina de la base de datos y se retira su
notificación.

Durante el breve periodo en el que aparece la acción `Deshacer`, la nota puede
permanecer temporalmente en la memoria de la aplicación para permitir su
restauración. Nolvida no conserva un historial de notas completadas.

Las preferencias permanecen en el dispositivo hasta que el usuario borra los
datos de Nolvida o desinstala la aplicación. Nolvida tiene desactivada la copia
de seguridad de la aplicación y no ofrece un mecanismo remoto de recuperación.

Como Pramos no recibe el contenido local, no puede acceder a él, restaurarlo o
eliminarlo a distancia.

## 8. ¿Cómo se protege la información local?

Las notas y preferencias se almacenan en el espacio privado asignado por Android
a Nolvida. La aplicación se apoya en el aislamiento de aplicaciones, los
permisos y las medidas de seguridad del dispositivo.

Nolvida no afirma que las notas tengan cifrado o contraseña propios. Para
reducir el acceso no autorizado se recomienda proteger el teléfono con PIN,
contraseña o biometría y configurar la privacidad de las notificaciones en la
pantalla de bloqueo.

## 9. ¿Qué ocurre si se contacta con Pramos?

Si una persona escribe voluntariamente a **nolvida@pramos.dev**, Pramos recibirá
la dirección de correo, el contenido del mensaje y cualquier información que la
persona decida incluir. Estos datos se utilizarán únicamente para gestionar y
responder la consulta.

La base jurídica será atender la solicitud de la persona y, cuando corresponda,
el interés legítimo en prestar soporte y conservar evidencia de su resolución.
Los mensajes se conservarán durante el tiempo necesario para resolver la
consulta y, como máximo, durante 12 meses desde su resolución, salvo que una
obligación legal o la gestión de una reclamación exija un periodo diferente.

El servicio de correo utilizado es **Gmail**, ofrecido por Google. Los mensajes
pueden almacenarse y procesarse en la infraestructura de Google de acuerdo con
sus condiciones y su
[política de privacidad](https://policies.google.com/privacy?hl=es). Google
puede tratar información en los países donde mantiene infraestructura aplicando
los mecanismos de protección descritos en dicha política.

Se recomienda no incluir notas, transcripciones u otra información sensible en
los mensajes de soporte.

## 10. ¿Qué ocurre al consultar esta política en la web?

El enlace incluido en Nolvida abre
`https://www.pramos.dev/nolvida/privacy` en el navegador elegido por el usuario.
Nolvida no adjunta ni envía notas, transcripciones, audio, preferencias o
identificadores al abrirlo.

La página se aloja mediante **Vercel**. Para servir y proteger la web, el
proveedor puede procesar datos técnicos como la dirección IP, fecha y hora,
información básica de la solicitud, navegador y sistema operativo. Vercel actúa
como proveedor de alojamiento conforme a sus condiciones y su
[aviso de privacidad](https://vercel.com/legal/privacy-notice).

Antes de publicar esta política se auditará el proyecto del portfolio para
confirmar si la página incorpora cookies, analítica u otros servicios. El
resultado real deberá añadirse aquí: `[PENDIENTE: AUDITORÍA WEB]`.

## 11. ¿Qué derechos tiene el usuario?

Las notas y preferencias permanecen exclusivamente en el dispositivo. El
usuario puede consultarlas y eliminarlas directamente desde Nolvida, borrar
todos los datos desde los ajustes de Android o desinstalar la aplicación.

Respecto a información que sí haya enviado por correo o que se trate al visitar
la página web, la persona puede solicitar, cuando resulte aplicable, acceso,
rectificación, supresión, limitación, oposición o portabilidad escribiendo a
**nolvida@pramos.dev**.

También puede presentar una reclamación ante la
[Agencia Española de Protección de Datos](https://www.aepd.es/) si considera que
el tratamiento de sus datos no ha sido adecuado.

## 12. Cambios en esta política

La política se actualizará cuando cambie el funcionamiento de Nolvida o resulte
necesario adaptar la información. La versión vigente permanecerá disponible en
la misma URL e indicará su fecha de última actualización.

Si una modificación introduce una nueva recogida, transmisión o finalidad
relevante, se informará de forma adecuada antes de aplicarla.

## 13. Contacto

Para consultas sobre privacidad o sobre Nolvida:

**Pramos**  
**Correo electrónico:** nolvida@pramos.dev
