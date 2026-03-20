# Análisis: modelado DynamoDB y estructura del proyecto (front + lambda)

Fecha: 2026-01-08

Propósito: documentar hallazgos sobre el modelado actual (single-table `TABLE_CATTLE`), cómo se usa desde el backend (lambdas/servicios) y el front, y recomendaciones prácticas (PK/SK, `entityType`, GSIs, migración).

1) Resumen del proyecto
- Frontend: carpeta `cattle-front/` (React + Vite). Interactúa con APIs y muestra dashboards (componentes en `src/components/*`).
- Backend (lambdas): carpeta `cattle-lambda-function/` (Java + AWS Lambda + DynamoDB). Código relevante:
  - Repositorios y entidades en `src/main/java/com/cattle/repository/` y `entities/`.
  - Servicios en `src/main/java/com/cattle/services/` (p. ej. `MilkingService.java`).
  - Procesadores en `processor/` (p. ej. `MilkingProcessor.java`).
  - Documentación existente en `docs/` y `docs/architecture/`.

2) Hallazgos sobre `TABLE_CATTLE` y modelado actual
- La tabla `TABLE_CATTLE` se usa como tabla principal para bovinos (PK: `BOVINE#<id>`). Ver: `BovineRepository.java` y la documentación en `docs/project-explored.md`.
- El proyecto usa `sk` polimórfico para múltiples tipos de item: `PROFILE`, `LACTANCIA#...`, `MILKING#...`, `PREG#...`. Hay referencias también a una tabla separada `TABLE_FARM_MILKING` en la documentación (revisar `docs/architecture/flujo-registro-ordeno.md`).
- Ventaja actual: fácil obtener todo lo relativo a un bovino con `PK = BOVINE#id` en una sola consulta (Query por PK + begins_with(SK,...)).
- Riesgos detectados:
  - Mezcla de entidades con distintos patrones de acceso y retención en la misma partición (`BOVINE#id`) puede generar hot-partitions y sobrecarga si hay muchos registros por animal.
  - `sk` polimórfico sin un `entityType` claro complica consultas, proyecciones y mantenimiento del esquema.
  - Consultas globales (p.ej. listar ordeños por fecha, listar vacas por estado) requieren GSIs que actualmente no siempre están definidos o proyectados correctamente.

3) Recomendaciones resumidas (prioritarias)
- Normalizar la convención de claves y añadir `entityType`:
  - `pk = BOVINE#<id>`
  - `sk` ejemplos:
    - `PROFILE#META`
    - `LACTATION#<lactationId>`
    - `MILKING#YYYY-MM-DD#AM` (o `MILKING#YYYY-MM-DD#HHMM#SEQ`)
    - `PREGNANCY#ACTIVE#<timestamp>`
  - Añadir atributo `entityType` con valores explícitos (`PROFILE`, `MILKING`, `LACTATION`, `PREGNANCY`).
- Añadir atributos auxiliares que faciliten GSIs: `farmId`, `date`, `status`, `cowTag`, `createdAt`, `ttl` (si aplica).
- Diseñar GSIs según patrones de acceso (ejemplos):
  - GSI1 (por fecha de ordeño): `GSI1PK = MILKING#YYYY-MM-DD`, `GSI1SK = BOVINE#<id>` ⇒ listar ordeños por fecha.
  - GSI2 (por finca y estado): `GSI2PK = FARM#<farmId>`, `GSI2SK = entityType#status#BOVINE#<id>` ⇒ listar vacas por estado en una finca.
  - GSI sparce para preñez: indexar sólo items con `entityType = PREGNANCY`.
- Proyecciones: proyectar sólo atributos necesarios en cada GSI para minimizar RCU.

4) Cuándo separar tablas
- Mantener single-table si:
  - La mayoría de accesos son por bovino (`PK = BOVINE#id`) y necesitas juntar información relacionada frecuentemente.
- Separar tablas si:
  - Diferentes políticas de retención o escalado (p. ej. ordeños con TTL y alta tasa de escritura).
  - Necesitas aislar throughput o responsabilidades entre equipos.
  - Los patrones de consulta cruzados son costosos y constantes (mejor una tabla especializada `TABLE_FARM_MILKING`).

5) Pasos sugeridos para migración/implementación
1. Definir convención final (`pk`, `sk`, `entityType`, atributos). Documentarlo en `docs/` (esta entrada).
2. Añadir GSIs minimalistas para consultas críticas (monitorizar costo y hot-partitions).
3. Escribir script de migración que lea items antiguos y reescriba con las nuevas claves/atributos (puede hacerse en batches). Guardar mapping y logs.
4. Actualizar repositorios Java (`BovineRepository`, `MilkingRepository`) para leer y escribir según la convención nueva. Añadir tests de integración para queries comunes.
5. Desplegar GSIs y cambios en etapas; si se opta por separar tablas, planificar backfill hacia `TABLE_FARM_MILKING`.

6) Observaciones específicas en código
- `MilkingService.java` y `MilkingProcessor.java` ya usan prefijos (`BOVINE#`, `MILKING#`/`LACTANCIA#`). Se recomienda unificar nomenclatura (usar `MILKING#` o `LACTATION#` consistentemente) y añadir `entityType` en `FarmMilking` y `Bovine` entities.
- Revisar `docs/architecture/` donde aparecen referencias a `TABLE_FARM_MILKING` y `TABLE_CATTLE`: armonizar documentación con el diseño elegido.

7) Próximos pasos opcionales (si quieres que lo haga)
- Generar POJOs de ejemplo actualizados para `Bovine` y `FarmMilking` (con `entityType` y atributos de GSI).
- Crear scripts de migración o un pequeño lambda de backfill.
- Implementar y testear 2 GSIs propuestos.

---
Archivo generado automáticamente: `docs/analysis-table-design.md` (resumen y recomendaciones). Si quieres, puedo actualizar `docs/project-explored.md` integrando estas recomendaciones directamente en la sección de `TABLE_CATTLE`.
