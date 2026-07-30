# Fase 10.1 — Limpieza segura

## Objetivo

Eliminar únicamente recursos heredados confirmados, sin refactorizar el MVP.

## Alcance autorizado

1. Confirmar con búsquedas que nadie consume:
   - `purple_200`, `purple_700`;
   - `teal_200`, `teal_700`;
   - `black`, `white`.
2. Confirmar que Manifest usa `@mipmap/ic_launcher` y que las variantes
   `mipmap-anydpi` contienen el icono adaptativo de Nolvida.
3. Retirar los WebP antiguos `ic_launcher.webp` e
   `ic_launcher_round.webp` de las cinco densidades.
4. No tocar los colores actuales del launcher, widget o paletas.

No ampliar este bloque a una limpieza general. Si se encuentra otra deuda baja,
documentarla para decidirla por separado.

## Comprobaciones

Desde `android/`:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Desde la raíz:

```powershell
git diff --check
git status --short
```

## Criterio de cierre

- APK debug correcto.
- Lint con `0` errores.
- Desaparecen las ocho advertencias de recursos.
- Quedan aproximadamente once advertencias de versiones, aceptadas sin
  actualizar herramientas o dependencias.

