# Fase 11.2 — Política de privacidad

## Objetivo

Publicar una política coherente con el comportamiento local de Nolvida y hacerla
accesible permanentemente desde la aplicación sin añadir navegación compleja.

## Contenido mínimo

- Responsable o nombre del desarrollador: `Pramos`.
- Contacto: `nolvida@pramos.dev`.
- Las notas y preferencias permanecen en el dispositivo.
- El audio se procesa localmente y no se almacena.
- No hay cuentas, backend, publicidad, analítica ni intercambio de datos.
- Finalidad de micrófono, notificaciones y reinicio del dispositivo.
- Eliminación de datos al completar notas, borrar datos o desinstalar.
- Fecha de vigencia y mecanismo de contacto.

## Implementación mínima

1. Acordar una URL HTTPS estable bajo `pramos.dev`.
2. Publicar una página web accesible, no un PDF ni un documento restringido.
3. Inventariar los textos nuevos en un CSV de localización.
4. Añadir un acceso visible y permanente desde la interfaz existente.
5. Abrir la política en el navegador mediante un `Intent` explícito adecuado;
   esto no requiere añadir el permiso `INTERNET` a Nolvida.
6. No crear Navigation Compose ni una pantalla principal adicional.

Los seis idiomas deben mostrar un nombre y descripción accesibles. No se
incluirán direcciones privadas o datos de verificación dentro del repositorio.

## Comprobaciones

Ejecutar tests afectados, compilación Kotlin, `assembleDebug`, lint y una prueba
manual del enlace con la aplicación instalada.

## Cierre

La URL debe ser pública, la política debe coincidir con Data Safety y el acceso
debe permanecer disponible después del onboarding.
