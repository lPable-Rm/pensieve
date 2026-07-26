# Fase 9 — Índice de implementación con ChatGPT 5.6 Terra

Estado: **diseñada y pendiente de implementación**.

## Objetivo

Convertir la interfaz funcional actual en una versión visualmente coherente y
comercial de **Nolvida**, sin ampliar el producto ni modificar sus flujos de
negocio.

La fase debe mejorar:

- identidad visual;
- onboarding del primer inicio;
- widget de captura directa;
- pantalla principal y componentes existentes;
- paletas predefinidas;
- iconos, accesibilidad y consistencia.

Después del onboarding, Nolvida continúa teniendo una sola pantalla principal.

## Subfases

| Subfase | Elemento | Guía |
|---|---|---|
| 9.1 | Marca visible, sistema base y onboarding | [FASE_9_1_MARCA_ONBOARDING.md](guias/fase_9/FASE_9_1_MARCA_ONBOARDING.md) |
| 9.2 | Widget rectangular en reposo y grabando | [FASE_9_2_WIDGET.md](guias/fase_9/FASE_9_2_WIDGET.md) |
| 9.3 | Estructura y cabecera de la pantalla principal | [FASE_9_3_PANTALLA_PRINCIPAL.md](guias/fase_9/FASE_9_3_PANTALLA_PRINCIPAL.md) |
| 9.4 | Lista, tarjetas y estado vacío | [FASE_9_4_TARJETAS_LISTA.md](guias/fase_9/FASE_9_4_TARJETAS_LISTA.md) |
| 9.5 | Entrada manual y acciones inferiores | [FASE_9_5_ENTRADA_MANUAL.md](guias/fase_9/FASE_9_5_ENTRADA_MANUAL.md) |
| 9.6 | Barra de captura y revisión por voz | [FASE_9_6_BARRA_VOZ.md](guias/fase_9/FASE_9_6_BARRA_VOZ.md) |
| 9.7 | Selector de paletas y persistencia | [FASE_9_7_PALETAS.md](guias/fase_9/FASE_9_7_PALETAS.md) |
| 9.8 | Iconos, launcher y superficies del sistema | [FASE_9_8_ICONOS.md](guias/fase_9/FASE_9_8_ICONOS.md) |
| 9.9 | Auditoría visual y técnica | [FASE_9_9_AUDITORIA.md](guias/fase_9/FASE_9_9_AUDITORIA.md) |

Implementar, comprobar y revisar una sola subfase cada vez.

## Dirección visual aprobada

- Producto calmado, cotidiano y directo; no debe parecer un gestor de tareas.
- Fondo claro, superficies limpias y sombras discretas.
- Tipografía del sistema mediante Material 3; no descargar fuentes.
- Espaciados regulares, componentes con una única responsabilidad y objetivos
  táctiles de al menos `48dp`.
- El rojo `#E5484D` se reserva para grabación, detención, error o peligro.
- Sin buscador, perfil, categorías, selección múltiple ni controles
  decorativos de los bocetos antiguos.
- Sin degradados o animaciones complejas si un color plano y una transición
  breve comunican lo mismo.

Las imágenes de `diseño/` son referencias de jerarquía y color, no
especificaciones literales. No deben modificarse.

## Paletas aprobadas

| Paleta | Principal | Contenedor claro | Contenido sobre principal |
|---|---|---|---|
| Lavanda | `#7C3AED` | `#EDE9FF` | `#FFFFFF` |
| Verde petróleo | `#0D9488` | `#E6F6F3` | `#111827` |
| Azul profundo | `#2563EB` | `#E7F0FF` | `#FFFFFF` |
| Coral suave | `#FF6B6B` | `#FFF1EF` | `#172033` |
| Escala de grises | `#6B7280` | `#F3F4F6` | `#FFFFFF` |
| Turquesa | `#06B6D4` | `#E0F7FA` | `#172033` |

Colores compartidos:

```text
Fondo general:      #FAF9FF
Superficie:         #FFFFFF
Texto principal:    #172033
Texto secundario:   #667085
Grabación/peligro:  #E5484D
```

Lavanda es la paleta inicial. Cada combinación de texto y fondo debe validarse
de nuevo en lint y accesibilidad; el color principal no implica que todo el
texto situado sobre él sea blanco.

## Arquitectura acordada

```text
AppPreferences (DataStore Preferences)
        |-> onboarding completado
        `-> identificador de paleta
                    |
MainActivity -------|-> onboarding o HomeRoute
                    `-> tema Compose
                              |
                              `-> widget RemoteViews
```

- Un único `AppPreferences` almacena dos valores pequeños.
- `MainActivity` elige onboarding o pantalla principal sin Navigation Compose.
- `HomeViewModel` conserva únicamente el estado funcional de las notas y la
  captura; no recibe preferencias visuales.
- Room sigue siendo la fuente de verdad de los recordatorios.
- El widget continúa siendo clásico con `RemoteViews`.
- No crear una capa de dominio, un gestor de temas genérico ni un módulo nuevo.

## Nombre comercial y nombre técnico

- Todo texto visible debe usar **Nolvida**.
- `app_name` pasa a ser `Nolvida` y continúa sin traducirse.
- Se actualizan las frases localizadas que todavía mencionen Pensieve.
- Se mantienen `com.mvppostit.pensieve`, `PensieveApplication`,
  `PensieveDatabase`, `pensieve.db` y los nombres internos existentes.
- No renombrar paquetes, base de datos o clases solo para hacer coincidir la
  marca: no aporta valor al usuario y aumenta el riesgo.

## Onboarding aprobado

Solo se muestra en un inicio ordinario cuando todavía no se ha completado:

1. **Sácalo de tu cabeza**: voz o texto y notificación persistente.
2. **Habla sin abrir Nolvida**: explica el widget y permite solicitar que
   Android lo añada.

No solicita permisos. El fallback privado iniciado desde el widget tiene
prioridad sobre el onboarding para poder preparar micrófono y notificaciones.

## Widget aprobado

- Tamaño fijo `2 × 1`.
- Reposo: micrófono, `Nolvida` y `Toca para hablar`.
- Grabando: indicador rojo, `Grabando…`, línea de voz estática y control de
  detención.
- Una segunda pulsación cancela mientras la escucha pueda cancelarse.
- Solo se actualiza al entrar o salir de la sesión; no hay onda real ni
  actualizaciones por segundo.

Esta decisión sustituye únicamente la presentación `1 × 1` de la fase 6. Se
conservan el servicio foreground, reconocimiento local, privacidad y guardado
automático ya auditados.

## Localización

Antes de añadir textos de fase 9, la subfase 9.1 crea:

```text
localizacion/TRADUCCIONES_FASE_9.csv
```

Debe contener todos los recursos nuevos o modificados en inglés, español,
alemán, francés, italiano y portugués de Portugal. Cada subfase consume solo
sus filas. No introducir texto visible directamente en Kotlin o XML.

## Dependencia aprobada

La única dependencia nueva de producción permitida es:

```text
androidx.datastore:datastore-preferences:1.2.1
```

Se añade en 9.1 y se reutiliza en 9.7. No añadir Navigation Compose, Glance,
bibliotecas de iconos, fuentes, animación, imágenes o preferencias.

## Fuera de alcance

- Cambios en Room, esquema, repositorio o `ReminderManager`.
- Cambios en reconocimiento de voz o almacenamiento de audio.
- Categorías, buscador, perfil, historial o selección múltiple.
- Selector libre de color.
- Selector de modo claro, oscuro o automático.
- Tema dinámico de Android: la marca debe conservar sus paletas.
- Refactorizaciones amplias o renombrado del paquete.
- Permiso `INTERNET`, telemetría o nuevos datos personales.
- Modificar `diseño/`, gestionar el emulador, hacer commit o push.

La interfaz de esta fase es clara y coherente en modo claro. Un diseño oscuro
completo necesita paletas propias y queda fuera del MVP.

## Comprobaciones comunes

Ejecutar desde `android/` las indicadas por cada subguía:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

No aceptar errores ni advertencias nuevas propias de fase 9. Las pruebas
manuales extensas siguen programadas para las fases 10 y 11.

## Prompt base para Terra

Sustituir `[SUBFASE]` y `[SUBGUIA]`:

```text
Implementa exclusivamente la subfase [SUBFASE] de la fase 9 de Nolvida.

Lee completamente AGENTS.md, CONTEXTO_MANANA.txt, GUIA_FASE_9_TERRA.md y
[SUBGUIA]. Revisa git status e inspecciona los archivos reales antes de editar.

Mantén la arquitectura simple: una actividad, HomeRoute, HomeViewModel,
AppPreferences con DataStore y el widget clásico con RemoteViews. No cambies
Room, ReminderManager, reconocimiento de voz ni flujos ya auditados. No añadas
funciones de producto, navegación, Glance o dependencias no aprobadas.

Usa los recursos localizados de los seis idiomas, no gestiones el emulador y no
hagas commit ni push. Ejecuta las comprobaciones de la subguía, informa de
archivos y resultados exactos y detente antes de la siguiente subfase.
```

## Referencias oficiales

- <https://developer.android.com/jetpack/androidx/releases/datastore>
- <https://developer.android.com/topic/libraries/architecture/datastore>
- <https://developer.android.com/develop/ui/views/appwidgets>
- <https://developer.android.com/develop/ui/views/appwidgets/advanced>
- <https://developer.android.com/develop/ui/views/appwidgets/discoverability>
- <https://developer.android.com/develop/ui/compose/accessibility>

## Criterio de cierre

La fase 9 solo puede cerrarse cuando:

- Nolvida es la única marca visible;
- el onboarding no reaparece después de completarlo;
- el widget `2 × 1` refleja reposo y grabación sin abrir la aplicación;
- los componentes mantienen todos los flujos anteriores;
- las seis paletas se guardan y se aplican a app y widget;
- los iconos han sido aprobados;
- los seis idiomas, accesibilidad, compilación, ensamblado y lint están
  auditados;
- `AGENTS.md` y `CONTEXTO_MANANA.txt` documentan el resultado final.
