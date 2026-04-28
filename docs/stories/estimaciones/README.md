# Estimaciones Técnicas - Pastures y Backlog Relacionado

Esta carpeta reúne artefactos de estimación y planeación técnica. No debe asumirse como fuente arquitectónica principal sin contrastar con el código y con `docs/arquitectura/`.

## Estado documental actual

Los documentos de esta carpeta mezclan:

- estimaciones históricas
- propuestas de implementación
- decisiones ya implementadas
- referencias técnicas que pueden haber derivado con el tiempo

Por eso deben leerse como material de backlog y planeación, no como descripción canónica del sistema actual.

## Reglas de uso

1. Validar cualquier endpoint, controlador, comando o tabla contra el código vigente.
2. Usar `docs/arquitectura/` como fuente principal para arquitectura base.
3. Tratar las estimaciones numéricas como históricas si no fueron recalculadas recientemente.

## Prioridad de revisión dentro de la carpeta

Los artefactos con mayor deriva detectada y ya normalizados fueron:

- `PASTURES-HU-001-post-eventos.md`
- `PASTURES-HU-008-tests-engine.md`
- `PASTURES-HU-009-tests-eta.md`
- `PASTURES-HU-012-historial-eventos.md`
- `PASTURES-HU-017-openapi-swagger.md`
- `PASTURES-HU-018-sns-sqs.md`
- `PASTURES-HU-023-caching-redis.md`

Esos documentos ya quedaron normalizados para distinguir mejor entre intención de backlog, estado real y gaps abiertos.

## Resto de la carpeta

Los demás documentos siguen disponibles como insumo de planeación, pero todavía pueden contener referencias desalineadas a:

- nombres de controladores o rutas antiguas
- comandos de testing basados en Maven
- supuestos de infraestructura no verificados
- enlaces documentales previos a la reorganización de `docs/`

## Siguiente uso recomendado

Cuando se retome una HU de esta carpeta:

1. Leer primero el artefacto de estimación.
2. Contrastar con el código real.
3. Si pasa a implementación, promover la decisión vigente a una historia o refinamiento técnico actualizado.