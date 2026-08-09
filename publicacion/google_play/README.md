# Ficha de Google Play de Nolvida

Estado: preparación local de la versión 1.0.

Esta carpeta contiene únicamente materiales de publicación. No forma parte del
AAB y no debe contener credenciales, claves, notas personales ni capturas con
datos reales.

## Decisiones confirmadas

- Nombre: `Nolvida`.
- Tipo: aplicación.
- Modelo: pago único.
- Idioma predeterminado: inglés de Estados Unidos.
- Categoría propuesta: Productividad.
- Correo de soporte: `nolvida@pramos.dev`.
- Política de privacidad: `https://www.pramos.dev/nolvida/privacy`.

## Descubrimiento

Google Play no ofrece un campo independiente de palabras clave. La ficha usa
descriptores reales y localizados en el título, la descripción corta y las
primeras líneas, sin listas repetitivas.

Conceptos principales en inglés:

- `reminders` y `reminder app`;
- `voice` y `text capture`;
- `notes`, `tasks` y `errands`;
- `notifications` y `notification shade`;
- `home screen widget`;
- `private`, `local` y `offline`.

El título de Play puede añadir un descriptor localizado a la marca, mientras
el nombre instalado de la aplicación continúa siendo simplemente `Nolvida`.

## Inventario honesto del producto

La ficha puede afirmar que Nolvida:

- captura recordatorios mediante texto o reconocimiento de voz en el
  dispositivo cuando está disponible;
- inicia la captura por voz desde el widget sin abrir la aplicación en el flujo
  normal;
- mantiene una notificación por recordatorio activo;
- permite completar desde la aplicación o la notificación y deshacer durante
  unos segundos;
- guarda las notas solo en el almacenamiento privado local;
- no incluye cuentas, anuncios, analítica, sincronización, audio almacenado ni
  permiso de Internet;
- ofrece seis paletas y seis idiomas de interfaz.

La ficha no debe prometer alarmas programadas, calendario, nube, historial,
edición de notas, inteligencia artificial, compatibilidad universal de voz ni
ninguna función anotada para V2.

## Estructura prevista

```text
google_play/
├── FICHA_GOOGLE_PLAY.csv
├── textos/
├── graficos/
└── capturas/
```

Las carpetas gráficas se crearán cuando exista un recurso final aprobado. Los
maestros de `diseño/` se consultan, pero nunca se modifican.
