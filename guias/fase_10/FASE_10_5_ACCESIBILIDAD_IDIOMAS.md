# Fase 10.5 — Accesibilidad, idiomas y paletas

## Objetivo

Validar que el mismo MVP continúa siendo legible y operable con configuraciones
reales, sin multiplicar innecesariamente todas las combinaciones.

## Accesibilidad

Probar y registrar:

- TalkBack: orden, encabezados, tarjetas y nombres de acciones;
- fuente cercana a `1.8` y tamaño de pantalla aumentado;
- poco alto y orientación horizontal;
- teclado abierto en entrada manual y revisión de voz;
- objetivos táctiles y foco visible;
- estado vacío, snackbar y hoja de paletas desplazables.

TalkBack manual es obligatorio; los tests semánticos automáticos no lo
sustituyen. Accessibility Scanner puede aportar señales, pero un resultado
limpio tampoco reemplaza la revisión humana.

## Idiomas

Cambiar el idioma general de Android, no una preferencia de Nolvida:

1. inglés;
2. español;
3. alemán;
4. francés;
5. italiano;
6. portugués de Portugal;
7. un idioma no soportado para comprobar el fallback inglés.

Crear una nota nueva después de cada cambio cuando se comprueben notificaciones
o widget. Se acepta que una notificación publicada antes del cambio conserve
parte del idioma anterior.

## Paletas

Recorrer las seis paletas y comprobar Home, estado vacío, tarjetas, entrada,
voz, snackbar, selector y widget. Repetir texto grande en una paleta clara y
otra oscura; no cruzar seis idiomas por seis paletas.

## Criterio de cierre

- Todos los casos `A11Y`, `LOC` y `PAL` tienen evidencia.
- No hay contenido inaccesible, cortado o imposible de pulsar.
- Cualquier excepción aceptada está documentada y no oculta un fallo.

