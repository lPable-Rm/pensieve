# Fase 11.6 — Declaraciones de Google Play

## Objetivo

Completar los formularios de Play Console de forma idéntica al comportamiento
real de Nolvida y conservar evidencia no sensible de las respuestas.

## Formularios previstos

- Data Safety: no se recopilan ni comparten datos fuera del dispositivo.
- Política de privacidad: URL pública aprobada en 11.2.
- Anuncios: no contiene anuncios.
- Acceso a la aplicación: todas las funciones están disponibles sin cuenta.
- Público objetivo y contenido: responder según la audiencia comercial real.
- Clasificación de contenido.
- Categoría y datos de contacto.
- Registro del paquete exigido por la verificación de desarrolladores.

El acceso y procesamiento exclusivamente local no se declara como recopilación
remota. Deben revisarse también las dependencias reales para confirmar que
ningún SDK transmite datos.

## Foreground service de micrófono

Declarar el tipo `microphone` e indicar:

- la captura comienza por una pulsación explícita del usuario;
- necesita iniciarse inmediatamente para no perder la voz;
- si se interrumpe, no se guarda un resultado parcial;
- el audio no se almacena ni se envía;
- existe una notificación foreground con acción de cancelación.

Preparar un vídeo breve que muestre el widget, el comienzo visible de la
captura, la notificación `Escuchando…`, la cancelación y el resultado normal.
El enlace debe ser accesible para la revisión de Google.

## Cierre

Cerrar cuando Play Console no muestre tareas de contenido pendientes y todas
las respuestas hayan sido contrastadas con Manifest, dependencias y política.
