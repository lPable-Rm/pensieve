# Fase 9.3 — Pantalla principal

## Objetivo

Aplicar a la única pantalla principal la jerarquía visual de Nolvida sin
añadir controles o comportamientos.

## Inspeccionar

```text
android/app/src/main/java/com/mvppostit/pensieve/MainActivity.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/home/HomeScreen.kt
android/app/src/main/java/com/mvppostit/pensieve/ui/theme/
diseño/02_estado_vacio.png
diseño/03_entrada_manual.png
diseño/logo_nolvida.svg
```

Las imágenes solo orientan sobre proporciones, tarjetas y jerarquía.

## Implementación

1. Sustituir colores locales de `HomeScreen.kt` por
   `MaterialTheme.colorScheme`.
2. Usar estilos de `MaterialTheme.typography` en vez de repetir tamaños y
   pesos arbitrarios.
3. Mantener fondo claro, margen horizontal coherente y contenido edge-to-edge
   sin tapar barras del sistema.
4. Extraer la cabecera a
   `ui/home/components/NolvidaHeader.kt`. Es un elemento independiente y deja
   `HomeScreen.kt` centrado en coordinar la pantalla.
5. Cabecera:
   - símbolo aprobado tintado con la paleta activa;
   - nombre `Nolvida` como heading;
   - una única acción real de apariencia.
6. Mantener la barra de voz sobre la lista y las acciones de creación en la
   parte inferior.

## Límites

- No cambiar callbacks, `HomeUiState`, ViewModel, Room o Scaffold.
- No crear una barra superior genérica ni Navigation Compose.
- No añadir controles decorativos ni otra acción a la cabecera.
- No introducir dimensiones globales si solo se usan en un componente.

## Comprobaciones

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

Conservar los previews normal y con `fontScale = 1.8f`. Detenerse antes de 9.4.
