# Scheduler de Refresh de Summary

## Contexto

El componente `summary` expone una proyección derivada que el frontend usa como vista principal de bovinos. Para reducir deriva entre los perfiles base y la proyección `SUMMARY`, el backend incorpora una ejecución programada diaria a las `3:00 AM`.

## Evidencia

- `template.yml`
- `src/main/java/com/cattle/SummaryRefreshSchedulerHandler.java`
- `src/main/java/com/cattle/services/BovineSummaryService.java`
- `src/main/java/com/cattle/controller/BovinesSummaryController.java`

## Arquitectura resultante

```text
EventBridge Scheduler
  -> Lambda SummaryRefreshSchedulerFunction
  -> SummaryRefreshSchedulerHandler
  -> BovineSummaryService.refreshAllSummaries()
  -> DynamoDB summary projection
  -> CloudWatch Logs / DLQ SQS
```

## Decisiones operativas

- se usa `AWS::Scheduler::Schedule` para poder declarar zona horaria explícita
- el scheduler no llama el endpoint público `POST /summary/refresh`
- la ejecución usa una Lambda dedicada con mayor `timeout` que la Lambda HTTP principal
- la resiliencia mínima se cubre con reintentos del scheduler y una DLQ SQS para eventos agotados

## Configuración declarada

- horario por defecto: `cron(0 3 * * ? *)`
- zona horaria por defecto: `America/Bogota`
- timeout de la Lambda programada: `300` segundos
- reintentos del scheduler: `2`
- edad máxima del evento para reintentos: `3600` segundos

## Validación operativa recomendada

1. desplegar el stack actualizado
2. verificar que exista la función `cattle-summary-refresh-scheduler`
3. verificar que exista el recurso `SummaryRefreshDailySchedule`
4. revisar CloudWatch Logs de la función tras una ejecución manual o programada
5. confirmar que el log incluya inicio, fin y cantidad de summaries actualizados
6. revisar la cola DLQ si una ejecución falla repetidamente

## Ejecución manual sugerida

Ejemplo con AWS CLI:

```bash
aws lambda invoke \
  --function-name cattle-summary-refresh-scheduler \
  --payload '{"source":"manual-validation"}' \
  response.json
```

La respuesta esperada incluye `message`, `count`, `requestId` y `trigger`.

## Riesgos vigentes

- si el volumen de bovinos crece por encima de lo esperado, el `timeout` de `300` segundos puede requerir ajuste
- la Lambda programada levanta el contexto Spring completo, por lo que el cold start sigue siendo un costo operativo
- la calidad del proceso batch sigue dependiendo de que `refreshAllSummaries()` mantenga tolerancia a fallos parciales por bovino