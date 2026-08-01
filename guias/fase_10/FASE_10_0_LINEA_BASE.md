# Fase 10.0 — Línea base y matriz

## Objetivo

Medir el estado real antes de cambiar código y preparar una única matriz
compartida. Esta subfase es de solo lectura y documentación.

## Pasos

1. Leer `AGENTS.md`, `CONTEXTO_MANANA.txt`, la guía principal y la auditoría
   final de fase 9.
2. Revisar `git status --short` sin descartar trabajo existente.
3. Ejecutar desde `android/`:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

4. Obtener de los informes el número real de tests, fallos, errores y omitidos.
5. Clasificar las advertencias de lint en:
   - plantilla y launcher;
   - versiones;
   - cualquier advertencia inesperada.
6. Completar en `MATRIZ_PRUEBAS_MANUALES.md` los datos de los tres entornos,
   dejando sus pruebas como `PENDIENTE`.

No ejecutar todavía `connectedDebugAndroidTest` ni pruebas manuales.

## Resultado esperado

- Línea base reproducible.
- Ningún archivo de producción modificado.
- Matriz preparada sin resultados inventados.
- Cualquier diferencia con las cifras previstas queda explicada antes de 10.1.

## Cierre

Registrar la fecha, rama, versión debug, tests y resumen de lint. Ejecutar:

```powershell
git diff --check
git status --short
```
