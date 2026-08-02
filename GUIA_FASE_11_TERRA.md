# Fase 11 — Guía de publicación comercial

Estado: **en curso; 11.1, 11.2 y 11.3 completadas, y 11.0 pendiente de verificación
externa**.

## Objetivo

Convertir el MVP validado de Nolvida en una aplicación comercial firmada,
declarada correctamente y publicada en Google Play. Esta fase no añade nuevas
funciones de producto: prepara y valida el artefacto `release`, la información
legal, la ficha y los canales de prueba exigidos por Google.

El ciclo de trabajo será:

```text
preparar -> comprobar -> registrar evidencia -> avanzar
```

## Decisiones confirmadas

| Elemento | Decisión |
|---|---|
| Nombre comercial | `Nolvida` |
| Desarrollador público | `Pramos` |
| Tipo de cuenta | Personal y nueva |
| Estado de la cuenta | Registrada y pendiente de verificación de Google |
| Correo público | `nolvida@pramos.dev` |
| Identificador Android | `com.mvppostit.pensieve` |
| Modelo comercial | Pago único, sin anuncios ni suscripciones |
| API mínima | 31 |
| API objetivo | 36 |

El correo propietario de Play Console, las credenciales, los documentos de
identidad, las claves y las direcciones privadas no se escribirán en el
repositorio.

## Subfases

| Subfase | Alcance | Guía |
|---|---|---|
| 11.0 | Cuenta, identidad y decisiones irreversibles | [FASE_11_0_CUENTA_IDENTIDAD.md](guias/fase_11/FASE_11_0_CUENTA_IDENTIDAD.md) |
| 11.1 | Auditoría técnica de release | [FASE_11_1_AUDITORIA_RELEASE.md](guias/fase_11/FASE_11_1_AUDITORIA_RELEASE.md) |
| 11.2 | Política de privacidad y acceso desde la app | [FASE_11_2_PRIVACIDAD.md](guias/fase_11/FASE_11_2_PRIVACIDAD.md) |
| 11.3 | Optimización y ofuscación con R8 | [FASE_11_3_R8.md](guias/fase_11/FASE_11_3_R8.md) |
| 11.4 | Clave de subida, firma y AAB | [FASE_11_4_FIRMA_AAB.md](guias/fase_11/FASE_11_4_FIRMA_AAB.md) |
| 11.5 | Ficha de Google Play | [FASE_11_5_FICHA_PLAY.md](guias/fase_11/FASE_11_5_FICHA_PLAY.md) |
| 11.6 | Data Safety y declaraciones de Play | [FASE_11_6_DECLARACIONES.md](guias/fase_11/FASE_11_6_DECLARACIONES.md) |
| 11.7 | Prueba interna desde Google Play | [FASE_11_7_PRUEBA_INTERNA.md](guias/fase_11/FASE_11_7_PRUEBA_INTERNA.md) |
| 11.8 | Prueba cerrada y acceso a producción | [FASE_11_8_PRUEBA_CERRADA.md](guias/fase_11/FASE_11_8_PRUEBA_CERRADA.md) |
| 11.9 | Precio, producción y cierre | [FASE_11_9_PUBLICACION.md](guias/fase_11/FASE_11_9_PUBLICACION.md) |

No iniciar una subfase hasta cerrar la anterior, salvo la verificación externa
de 11.0. Mientras Google responde se puede avanzar con el trabajo local de
11.1 a 11.6, pero no subir artefactos ni enviar declaraciones fuera del orden
acordado.

## Responsabilidades

- El usuario controla Play Console, pagos, identidad, claves y dispositivos.
- El agente puede revisar o modificar el repositorio dentro de la subfase
  autorizada, pero nunca solicita ni registra secretos.
- El usuario genera y custodia la clave de subida siguiendo la guía.
- El usuario instala y valida la versión distribuida por Google Play en el
  dispositivo físico.
- Ningún agente hace commit, push o publicación sin petición explícita.

## Límites

- No añadir funciones de V2, backend, cuentas, analítica o publicidad.
- No añadir `INTERNET` mientras Nolvida siga siendo completamente local.
- No introducir Play Integrity, Firebase, Billing ni SDK de telemetría.
- No cambiar `com.mvppostit.pensieve`.
- No actualizar Gradle, Kotlin, Compose, SDK o dependencias por advertencias.
- No modificar `diseño/`; los materiales de Play se preparan por separado.
- No guardar `.jks`, contraseñas, documentos o archivos de firma en Git.
- No prometer en la ficha funciones que Nolvida no ofrece.

## Prompt base para otro agente

```text
Trabaja exclusivamente en la subfase [SUBFASE] de la fase 11 de Nolvida.

Lee completamente AGENTS.md, CONTEXTO_MANANA.txt, GUIA_FASE_11_TERRA.md y
[SUBGUIA]. Revisa git status y el código real antes de actuar. Conserva todos
los cambios existentes del usuario.

No avances a otra subfase, no añadas funciones de V2, no actualices versiones,
no modifiques diseño/ y no hagas commit ni push. No solicites, muestres ni
guardes credenciales, documentos de identidad, direcciones privadas,
contraseñas o claves de firma.

Ejecuta solo las comprobaciones indicadas. Si una acción corresponde a Play
Console, firma o dispositivo físico, guía al usuario y espera su resultado sin
inventar evidencia. Informa de archivos, comandos y resultados exactos.
```

## Referencias oficiales

- <https://developer.android.com/topic/performance/app-optimization/enable-app-optimization>
- <https://developer.android.com/studio/publish/app-signing>
- <https://developer.android.com/studio/publish/upload-bundle>
- <https://support.google.com/googleplay/android-developer/answer/14151465>
- <https://support.google.com/googleplay/android-developer/answer/10787469>
- <https://support.google.com/googleplay/android-developer/answer/13392821>
- <https://support.google.com/googleplay/android-developer/answer/9866151>

## Criterio de cierre

La fase 11 se cierra únicamente cuando:

- la cuenta y el paquete están verificados;
- la versión release está optimizada, firmada y probada;
- el AAB válido está conservado fuera del repositorio;
- la política de privacidad es pública y accesible desde la aplicación;
- Data Safety y las declaraciones coinciden con el comportamiento real;
- la prueba interna y la prueba cerrada exigida están completadas;
- Google concede acceso a producción;
- Nolvida se publica como aplicación de pago y se instala desde Google Play;
- la prueba de humo final termina sin fallos bloqueantes;
- AGENTS.md y CONTEXTO_MANANA.txt registran el resultado real.
