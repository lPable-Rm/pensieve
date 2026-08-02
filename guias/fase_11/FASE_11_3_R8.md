# Fase 11.3 — Optimización y ofuscación con R8

Estado: **completada**.

## Objetivo

Optimizar únicamente la variante `release`, reducir código y recursos no usados
y elevar el coste de ingeniería inversa sin presentar la ofuscación como una
barrera de seguridad absoluta.

## Implementación aplicada

El proyecto usa AGP 9.2.1. Para esta versión se ha empleado el DSL estable de
`buildTypes.release`:

```kotlin
isMinifyEnabled = true
isShrinkResources = true
proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
```

El DSL gradual `optimization { enable = true }` requiere activar la opción
experimental global `android.r8.gradual.support` en AGP 9.2. No se ha añadido
esa opción porque la configuración estable anterior resuelve el objetivo sin
introducir una función experimental.

R8 no ha comunicado clases ausentes ni ha requerido excepciones. Por ello, no
se han añadido reglas preventivas y el archivo existente
`app/src/main/keepRules/rules.keep` permanece sin reglas específicas.

## Resultado de las comprobaciones

- `:app:testDebugUnitTest`: 17 tests, 0 fallos.
- `:app:compileDebugAndroidTestKotlin`: correcto; 31 tests instrumentados
  continúan compilando.
- `:app:bundleRelease`: correcto, incluida la tarea `minifyReleaseWithR8`.
- `:app:lintRelease`: 0 errores y las 11 advertencias de versiones ya
  aceptadas.
- `:app:assembleDebug`: correcto; la optimización continúa limitada a
  `release`.
- Manifest release: no contiene el permiso `INTERNET`.
- R8: sin archivos de reglas ausentes ni advertencias bloqueantes.

El AAB optimizado se generó en
`app/build/outputs/bundle/release/app-release.aab`:

- tamaño anterior: 9.136.201 bytes (8,71 MiB);
- tamaño optimizado: 4.299.190 bytes (4,10 MiB);
- reducción: 52,94 %;
- SHA-256 de esta referencia:
  `47E7892B14CB144D7321DFA5E0942D54C48ACD5DD4A11DBCD516411142B22027`.

El archivo `app/build/outputs/mapping/release/mapping.txt` existe, no está vacío
y demuestra que R8 ha renombrado clases del proyecto. Este mapa y el AAB son
artefactos generados dentro de `build/`, no documentación que deba añadirse al
repositorio.

El AAB de esta subfase sigue **sin firmar**, de forma intencionada. La clave de
subida y el AAB publicable corresponden a 11.4. Al generar allí el artefacto
definitivo habrá que conservar juntos ese AAB y su nuevo `mapping.txt`.

Referencia oficial para habilitar R8 y la reducción de recursos:
<https://developer.android.com/topic/performance/app-optimization/enable-app-optimization>.

El `mapping.txt` es un artefacto privado de cada versión. Se conserva junto al
release correspondiente y no se publica como código fuente.

## Cierre

La subfase queda completada: R8 termina sin reglas injustificadas, no existen
advertencias bloqueantes y el AAB optimizado se genera correctamente. La firma
y la prueba de humo del artefacto firmado se realizarán en 11.4; la instalación
distribuida desde Google Play se comprobará en 11.7.
