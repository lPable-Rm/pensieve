# Fase 9.9 — Auditoría

## Objetivo

Auditar la fase 9 completa sin corregir hallazgos durante la misma revisión.

## Revisar

### Producto

- Nolvida es la única marca visible.
- El onboarding tiene dos páginas, aparece una vez y no solicita permisos.
- El fallback del widget no queda bloqueado por el onboarding.
- No se han añadido buscador, perfil, categorías o funciones ajenas.

### Widget

- Tamaño `2 × 1` y `RemoteViews`.
- Estados reposo y grabando.
- Segunda pulsación cancela solo antes de guardar.
- Sin actualizaciones continuas, Glance o onda real.
- Sin regresiones en servicio foreground, privacidad o guardado único.

### Interfaz

- Home sigue siendo la única pantalla principal.
- Texto, voz, completar, deshacer y errores conservan su comportamiento.
- HomeScreen coordina y los componentes extraídos tienen una responsabilidad.
- Paletas, contraste, texto grande, objetivos táctiles y TalkBack son
  coherentes.

### Preferencias y localización

- Una única instancia de DataStore.
- Solo guarda onboarding y id de paleta.
- Valor inválido vuelve a Lavanda.
- Los seis idiomas contienen los mismos recursos y marcadores.
- No existe texto visible hardcodeado.

### Seguridad y alcance

- Sin `INTERNET`, analítica, audio o logs sensibles.
- Sin cambios en paquete, Room, base de datos o flujos de recuperación.
- Solo se añadió DataStore Preferences `1.2.1`.
- No hay código muerto, imports innecesarios o comentarios desactualizados.

## Comprobaciones

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
git diff --check
```

Registrar número de tests, errores y advertencias. No ocultar resultados
reutilizados por Gradle.

## Pruebas manuales aplazables

Documentar para fases 10 y 11:

- primer inicio, omitir y no repetición;
- solicitud de fijar widget en launchers compatibles;
- widget reposo, grabación, cancelación y guardado;
- seis paletas tras cerrar y abrir;
- seis idiomas y fallback inglés;
- texto grande, TalkBack, API 31, API 36 y dispositivo físico.

## Cierre

Si no existen hallazgos altos o medios, actualizar `AGENTS.md` y
`CONTEXTO_MANANA.txt` con el cierre. Si hay correcciones, crear una subguía
9.10 específica y mantener la fase abierta.
