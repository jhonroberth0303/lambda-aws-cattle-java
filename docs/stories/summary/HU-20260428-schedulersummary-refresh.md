# HU-20260428: Scheduler Diario para Refresh de Summary

**ID**: HU-20260428-schedulersummary-refresh  
**Tipo**: Feature / Operación programada  
**Prioridad**: Alta  
**Fecha**: 2026-04-28  
**Estado actual**: En progreso - refinamiento técnico, implementación y documentación operativa completados; validación ejecutable en curso

## Registro de cambios

| Fecha | Versión | Descripción | Resultado |
|------|---------|-------------|-----------|
| 2026-04-28 | 1.0 | Creación de HU para automatizar el refresh diario de `summary` | HU abierta |
| 2026-04-28 | 1.1 | Refinamiento técnico de la solución preferida con EventBridge Scheduler y Lambda Java dedicada | Diseño listo para desarrollo |
| 2026-04-28 | 1.2 | Implementación de handler programado, recursos SAM y documentación operativa | Lista para validación |

## Contexto de negocio

El frontend principal consume la proyección `SUMMARY` para listar bovinos. Hoy esa proyección puede quedar desactualizada si no se ejecuta una regeneración batch después de cambios acumulados en perfiles o estados productivos.

Se requiere una ejecución automática diaria a las `3:00 AM` para recalcular y persistir los summaries sin intervención manual, reduciendo deriva entre datos base y vista consolidada.

## Objetivo

Implementar una automatización diaria que dispare el refresh batch de `summary` a las `3:00 AM`, con observabilidad básica, reintentos controlados y sin depender de una llamada HTTP pública innecesaria entre servicios propios.

## Evidencia revisada

- `template.yml` expone actualmente una única Lambda Java detrás de API Gateway
- `src/main/java/com/cattle/controller/BovinesSummaryController.java` ya publica `POST /summary/refresh`
- `src/main/java/com/cattle/services/BovineSummaryService.java` ya contiene la lógica batch en `refreshAllSummaries()`
- `docs/arquitectura/componente-summary-bovinos.md` documenta el flujo batch vigente del componente `summary`

## Problema actual

- la proyección `SUMMARY` depende de una regeneración explícita
- no existe en la infraestructura actual un scheduler diario para esa regeneración
- crear una Lambda Node solo para llamar un endpoint propio agrega un salto operativo adicional: scheduler -> Lambda Node -> API Gateway -> Lambda Java

## Decisión técnica propuesta

La historia tomará como camino preferido un scheduler nativo de AWS que invoque directamente una capacidad interna del backend Java, evitando depender del endpoint público `POST /summary/refresh` como mecanismo primario de automatización.

Arquitectura objetivo:

```text
EventBridge Scheduler (3:00 AM, zona horaria explícita)
    -> Lambda Java dedicada para scheduler
    -> BovineSummaryService.refreshAllSummaries()
    -> logs / métricas / manejo de error
```

## Refinamiento técnico ejecutado

### Diseño final adoptado

- nueva Lambda dedicada `cattle-summary-refresh-scheduler`
- nuevo handler `SummaryRefreshSchedulerHandler`
- recurso `AWS::Scheduler::Schedule` para cron diario con zona horaria explícita
- DLQ SQS y política de reintentos mínimos del scheduler
- reutilización de `BovineSummaryService.refreshAllSummaries()` como fuente única de verdad del batch

### Motivo de la decisión

Se descartó una Lambda Node puente porque agregaba un salto operativo innecesario y convertía una automatización interna en una llamada HTTP sobre infraestructura propia. La nueva función programada mantiene el disparo dentro del stack Java actual y permite aumentar el `timeout` del batch sin afectar la Lambda HTTP principal.

### Componentes impactados

- `template.yml`
- `src/main/java/com/cattle/SummaryRefreshSchedulerHandler.java`
- `src/test/java/com/cattle/SummaryRefreshSchedulerHandlerTest.java`
- documentación de arquitectura y operación del backend

## Alcance

Incluye:

- definir el mecanismo programado diario en AWS
- conectar el disparo con la lógica existente de `refreshAllSummaries()`
- configurar zona horaria explícita para la ejecución
- registrar logs suficientes para saber si la corrida inició, cuántos summaries actualizó y si falló
- definir estrategia mínima de reintentos y manejo de error operativo
- actualizar documentación técnica y operativa relevante

No incluye:

- rediseñar el modelo `SUMMARY`
- rehacer la API pública de `summary`
- introducir una Lambda Node intermedia salvo que el refinamiento técnico encuentre un bloqueo real para el enfoque preferido

## Criterios de aceptación

| ID | Criterio |
|----|----------|
| CA-001 | Existe una ejecución automática diaria configurada para las `3:00 AM` con zona horaria explícita y verificable |
| CA-002 | La ejecución programada invoca la lógica batch de refresh de summaries sin depender como camino principal de una llamada HTTP pública al propio API |
| CA-003 | El proceso registra evidencia operativa mínima: inicio, fin, cantidad de summaries actualizados y detalle del error cuando falle |
| CA-004 | La solución contempla reintentos automáticos o estrategia equivalente de resiliencia operativa |
| CA-005 | La infraestructura y el código asociados quedan versionados en el repositorio |
| CA-006 | La documentación resultante deja claro cómo funciona el scheduler, qué componente ejecuta el batch y cómo validar una corrida |

## Supuestos iniciales

1. La ventana de ejecución de las `3:00 AM` es aceptable para negocio y operación.
2. La frecuencia requerida es diaria, no varias veces por día.
3. La lógica existente `refreshAllSummaries()` es la fuente de verdad del proceso batch.
4. El despliegue seguirá realizándose sobre AWS SAM / CloudFormation del backend actual.

## Estimación técnica

```text
5 Story Points
24 horas estimadas
Complejidad Media
Riesgo Medio
```

| Fase | Alcance | SP | Horas |
|------|---------|----|-------|
| F1 | Refinamiento SAM + estrategia de invocación | 1 | 4h |
| F2 | Implementación handler y recursos AWS | 2 | 8h |
| F3 | Pruebas focalizadas y validación | 1 | 4h |
| F4 | Documentación operativa y cierre | 1 | 8h |
| | **TOTAL** | **5 SP** | **24h** |

## Tareas ejecutables

1. Agregar handler Java dedicado para el job programado.
2. Declarar Lambda programada, scheduler, DLQ y permisos en `template.yml`.
3. Propagar `APP_TIMEZONE` al stack.
4. Agregar pruebas unitarias del nuevo handler.
5. Documentar arquitectura resultante, despliegue y validación operativa.
6. Ejecutar validación puntual de tests y plantilla.

## Riesgos y decisiones a validar en refinamiento

- definir si el scheduler invocará la misma Lambda con un evento dedicado o una función/handler separado dentro del mismo proyecto
- confirmar la zona horaria oficial del negocio para evitar desalineación con UTC
- decidir si el endpoint `POST /summary/refresh` queda solo para uso manual/operativo o si requiere endurecimiento adicional
- validar límites de timeout y memoria del batch si el volumen de bovinos crece

## Propuesta de validación

- prueba unitaria o de integración acotada del handler/evento programado
- validación de plantilla SAM para asegurar que el scheduler y permisos queden declarados
- evidencia de ejecución exitosa en logs o entorno de prueba después del despliegue

## Implementación realizada

- se agregó el handler `SummaryRefreshSchedulerHandler` para invocación programada
- se agregó la función `SummaryRefreshSchedulerFunction` en SAM con `timeout` independiente de la API principal
- se agregó `AWS::Scheduler::Schedule` con cron diario y `ScheduleExpressionTimezone`
- se agregó DLQ `SQS` y política de reintentos del scheduler
- se documentó el componente operativo del scheduler en arquitectura

## Estado y siguiente paso

Estado actual: implementación terminada y pendiente de validación final en entorno de build/despliegue.

Siguiente paso esperado:

1. Ejecutar tests focalizados del nuevo handler y validación de compilación.
2. Validar el template SAM con las credenciales y tooling del entorno.
3. Desplegar en ambiente controlado y verificar logs de una ejecución manual.

## Artefacto vivo

Esta HU debe mantenerse como artefacto vivo durante refinamiento, implementación, validación y cierre. Cuando se ejecute la solución, este mismo archivo debe ampliarse con evidencia de pruebas, cambios realizados y decisión final de despliegue.