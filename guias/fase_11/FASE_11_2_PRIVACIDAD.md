# Fase 11.2 — Política de privacidad

Estado: **completada y validada manualmente**.

## Objetivo

Publicar una política coherente con el comportamiento local de Nolvida y hacerla
accesible permanentemente desde la aplicación sin añadir navegación compleja.

## Contenido mínimo

- Identidad legal del responsable en la página pública, sin guardarla en este
  repositorio.
- Nombre comercial del desarrollador: `Pramos`.
- Contacto: `nolvida@pramos.dev`.
- Las notas y preferencias permanecen en el dispositivo.
- El audio se procesa localmente y no se almacena.
- No hay cuentas, backend, publicidad, analítica ni intercambio de datos.
- Finalidad de micrófono, notificaciones y reinicio del dispositivo.
- Eliminación de datos al completar notas, borrar datos o desinstalar.
- Fecha de vigencia y mecanismo de contacto.

## Implementación mínima

1. Usar la URL HTTPS estable
   `https://www.pramos.dev/nolvida/privacy`.
2. Publicar una página web accesible, no un PDF ni un documento restringido.
3. Inventariar los textos nuevos en un CSV de localización.
4. Añadir un acceso visible y permanente desde la interfaz existente.
5. Abrir la política en el navegador mediante un `Intent` implícito
   `ACTION_VIEW` limitado a esa URL HTTPS; esto no requiere añadir el permiso
   `INTERNET` a Nolvida.
6. No crear Navigation Compose ni una pantalla principal adicional.

Los seis idiomas deben mostrar un nombre y descripción accesibles. No se
incluirán direcciones privadas o datos de verificación dentro del repositorio.

## Orden de trabajo acordado

1. Redactar y aprobar el maestro español en
   `legal/POLITICA_PRIVACIDAD_NOLVIDA_ES.md`.
2. Trabajar únicamente en el repositorio del portfolio para crear y publicar
   `/nolvida/privacy` mediante Vercel.
3. Comprobar la URL pública desde una ventana privada y cerrar ese bloque.
4. Regresar al repositorio de Nolvida para inventariar y traducir los textos del
   enlace.
5. Añadir el acceso dentro de la app y ejecutar sus comprobaciones.

No modificar los dos repositorios dentro del mismo bloque de trabajo.

## Resultado implementado

- La política está publicada en
  `https://www.pramos.dev/nolvida/privacy` y respondió correctamente mediante
  HTTPS durante la revisión.
- El contenido funcional coincide con el comportamiento local de Nolvida y
  declara Gmail para el soporte y Vercel para el alojamiento de la página.
- `localizacion/TRADUCCIONES_FASE_11.csv` inventaría el menú, la acción de
  privacidad, su error y la URL estable.
- La cabecera conserva una sola acción visible. Su menú ofrece `Apariencia` y
  `Política de privacidad` en los seis idiomas compatibles.
- `HomeRoute` abre exclusivamente la URL HTTPS mediante `ACTION_VIEW` y
  `CATEGORY_BROWSABLE`. Si no existe una aplicación compatible, muestra un
  snackbar localizado en lugar de cerrar Nolvida.
- No se añadió Navigation Compose, una pantalla adicional, una dependencia o
  el permiso `INTERNET`.
- Una prueba semántica abre el menú y demuestra que ambas opciones ejecutan el
  callback correspondiente.

## Comprobaciones automáticas

- `:app:testDebugUnitTest`: 17 tests, 0 fallos, 0 errores y 0 omitidos.
- `:app:compileDebugAndroidTestKotlin`: 31 tests instrumentados compilados.
- `:app:assembleDebug`: correcto.
- `lintDebug`: 0 errores y las 11 advertencias históricas de versiones.
- Manifest principal y fusionado debug: sin permiso `INTERNET`.
- `git diff --check`: sin errores; solo avisos locales LF/CRLF.

## Validación manual

El usuario confirmó con la aplicación instalada que `Más opciones` >
`Política de privacidad` abre correctamente la URL publicada.

## Tarea comercial aplazada

Antes de introducir la política o enviar declaraciones en Google Play, completar
en la web la identidad legal y la dirección pública de contacto indicadas en el
maestro español. Estos datos no se copiarán a este repositorio. El usuario ha
decidido aplazar este punto y conservarlo como requisito previo a 11.6.

## Cierre

La URL es pública, el acceso permanece disponible después del onboarding y la
integración coincide con el comportamiento real de Nolvida. La subfase 11.2 se
cierra el 2 de agosto de 2026. El dato legal aplazado continúa siendo obligatorio
antes de 11.6 y de cualquier envío a Google Play.
