# Fase 10.7 — Correcciones demostradas

Estado: **completada y validada en el Vivo V2130**.

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

## 10.7A — Notificación visible y silenciosa en el bloqueo

Severidad: **media**.

En el Vivo V2130 con Android 14, el canal `IMPORTANCE_LOW` muestra el
recordatorio en el panel, pero no en la pantalla bloqueada. Al convertirlo
manualmente en un canal con sonido sí aparece. El teléfono muestra otras
notificaciones silenciosas en el bloqueo, por lo que no es una limitación
general del dispositivo.

Aplicar la corrección mínima:

1. Crear el canal de recordatorios con `IMPORTANCE_DEFAULT`.
2. Desactivar explícitamente su sonido y vibración predeterminados.
3. Marcar cada notificación como `CATEGORY_REMINDER`.
4. Declarar `VISIBILITY_PRIVATE` para respetar la privacidad configurada por
   la persona sin ocultar la existencia del recordatorio.
5. Mantener el identificador estable y no modificar el canal temporal de voz.

Android conserva el comportamiento de un canal ya creado. Para validar el
nuevo valor inicial en desarrollo, eliminar la instalación debug o borrar el
canal antes de repetir la prueba. La aplicación no debe reemplazar después las
preferencias que haya elegido la persona.

Actualizar el test del canal y el contrato de la notificación. Repetir en el
Vivo creación, reconciliación y reinicio: el recordatorio debe aparecer en el
bloqueo sin sonido ni vibración y `Hecho` debe continuar funcionando.

## 10.7B — Identidad y jerarquía de la notificación

Severidad: **baja**.

La prueba física muestra un símbolo demasiado pequeño y el nombre `Nolvida`
repetido en la cabecera del sistema y en el título del contenido.

Aplicar:

- ampliar el dibujo dentro de `ic_reminder_notification.xml` sin incumplir la
  silueta monocroma exigida para el icono pequeño;
- usar el texto del recordatorio como cuerpo mediante `setContentText` y no
  definir un título de contenido;
- conservar `createdAtMillis` mediante `setWhen` para que Android muestre la
  fecha u hora original en su ubicación nativa cuando corresponda;
- usar `Notification.BigTextStyle` para que un texto largo pueda expandirse;
- conservar la acción `Hecho`, el `PendingIntent` inmutable y la privacidad.

La cabecera nativa de Android ya muestra `Nolvida`. No repetir el nombre de la
aplicación ni el texto del recordatorio en el título de contenido: al expandir
`BigTextStyle`, Android mostraría el título y el cuerpo a la vez. Tampoco usar
la fecha como título, porque podría interpretarse como una fecha límite que el
producto no ofrece. Al reconciliar una notificación, su timestamp debe seguir
siendo el de creación y no el momento en que se volvió a publicar.

No usar `largeIcon` para repetir el logo, `MessagingStyle`, una notificación
personalizada ni recursos de `diseño/`. Comprobar en API 31, API 36 y el Vivo
que el símbolo no queda recortado y que el texto corto y largo se leen bien.

## 10.7C — Iconos más legibles en el widget

Severidad: **baja**.

El widget `3 x 1` dispone de espacio, pero el micrófono, el logo y el control de
detener se perciben pequeños en el dispositivo físico.

Ajuste que debe validarse visualmente:

| Elemento | Ajuste |
|---|---:|
| Círculo de micrófono | 40 dp con 4 dp de padding |
| Micrófono visible | ocupa el espacio interior de 32 dp |
| Logo | variante compacta dentro de una vista de 36 dp |
| Círculo de detener | 40 dp con 4 dp de padding |
| Cuadrado de detener | trazado ampliado dentro del vector |

No basta con aumentar el `ImageView`: el vector general de Nolvida contiene
margen interno y el micrófono solo ocupa una parte de su viewport. Usar una
variante compacta exclusiva del widget y conservar intacto el recurso general.

Actualizar los estados de reposo y grabación y también
`widget_voice_capture_preview.xml`. Mantener toda la superficie pulsable, el
tamaño `3 x 1`, los dos estados, el flujo directo y la adaptación a las seis
paletas. Confirmar que los textos de alemán y portugués no sufren un recorte
inaceptable.

## 10.7D — Título centrado en la cabecera

Severidad: **baja**.

La cabecera agrupa actualmente logo y título a la izquierda, por lo que
`Nolvida` no queda centrado respecto a la pantalla.

Usar una distribución sencilla de tres posiciones:

```text
[logo]                 Nolvida                 [paleta]
```

El logo debe alinearse al inicio, el título al centro y la paleta al final.
Conservar los extremos de 48 dp, las semánticas de heading y el área táctil.
No usar offsets ni márgenes calculados para un dispositivo concreto. Validar
ancho reducido y fuente `1.8` para evitar solapamientos.

## Fuera de alcance

- No cambiar el tamaño `3 x 1` del widget.
- No añadir opciones visuales ni ajustes de notificaciones dentro de Nolvida.
- No crear sonidos, alarmas, canales por nota o diseños de notificación
  personalizados.
- No modificar `diseño/`, dependencias, versiones ni arquitectura.

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
- La notificación aparece en el bloqueo del Vivo sin sonido ni vibración.
- Notificación, widget y cabecera quedan legibles sin recortes ni solapamientos.
- La matriz refleja el resultado final sin borrar la evidencia previa.
- Las comprobaciones automáticas terminan correctamente.
