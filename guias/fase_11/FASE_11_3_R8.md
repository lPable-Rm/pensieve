# Fase 11.3 — Optimización y ofuscación con R8

## Objetivo

Optimizar únicamente la variante `release`, reducir código y recursos no usados
y elevar el coste de ingeniería inversa sin presentar la ofuscación como una
barrera de seguridad absoluta.

## Cambio previsto

El proyecto usa el DSL de optimización de AGP 9.2. En `release`, cambiar solo:

```kotlin
optimization {
    enable = true
}
```

Esta propiedad activa la optimización de código y recursos mediante R8. No
crear un `proguard-rules.pro` paralelo ni añadir reglas preventivas. Si un fallo
release reproducible demuestra que hace falta conservar una clase, usar el
archivo existente `app/src/main/keepRules/rules.keep` y documentar el motivo.

## Comprobaciones

1. Ejecutar tests unitarios y compilación de tests instrumentados.
2. Generar `bundleRelease` con R8.
3. Ejecutar lint de release o la variante disponible equivalente.
4. Revisar advertencias y el archivo `mapping.txt` generado.
5. Confirmar que debug mantiene su comportamiento y no queda ofuscado.

Referencia del DSL usado por el proyecto:
<https://developer.android.com/reference/tools/gradle-api/9.2/com/android/build/api/dsl/Optimization>.

El `mapping.txt` es un artefacto privado de cada versión. Se conserva junto al
release correspondiente y no se publica como código fuente.

## Cierre

Cerrar cuando R8 termine sin reglas injustificadas, no existan advertencias
bloqueantes y el AAB optimizado se genere correctamente. La prueba física de la
versión firmada se realizará en 11.4 y 11.7.
