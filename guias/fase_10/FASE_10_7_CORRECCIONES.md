# Fase 10.7 — Correcciones demostradas

## Objetivo

Corregir únicamente los fallos reproducibles registrados en la matriz, con un
cambio pequeño y verificable por incidencia.

## Proceso

1. Ordenar las filas `FALLO` por severidad e impacto.
2. Corregir un problema cada vez.
3. Mantener el comportamiento no relacionado.
4. Repetir inmediatamente el caso y sus regresiones cercanas.
5. Conservar la evidencia original y añadir el resultado de la repetición.

No aprovechar una corrección para rediseñar, refactorizar ampliamente o añadir
funciones. Un hallazgo bajo puede aplazarse solo con riesgo, motivo, prueba
pendiente y destino concreto. Ningún hallazgo alto o medio se cierra mediante
documentación.

## Comprobaciones

Ejecutar las comprobaciones proporcionales a cada cambio según `AGENTS.md`.
Después de terminar todas las correcciones, desde `android/`:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Repetir los tests instrumentados únicamente en los entornos afectados. Desde
la raíz:

```powershell
git diff --check
git status --short
```

## Criterio de cierre

- No quedan fallos altos o medios abiertos.
- Cada corrección tiene prueba de reproducción y repetición.
- La matriz refleja el resultado final sin borrar la evidencia previa.
- Las comprobaciones automáticas terminan correctamente.

