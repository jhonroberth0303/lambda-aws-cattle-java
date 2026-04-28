# Historias de Usuario - lambda-aws-cattle-java

Este índice organiza los artefactos de `docs/stories/` y deja explícito qué superficies ya fueron alineadas con el código actual y cuáles siguen siendo principalmente históricas o candidatas a revisión adicional.

## Estructura vigente

```text
stories/
├── README.md
├── bedrock/
├── estimaciones/
├── eventos-bovines/
├── milking/
└── summary/
```

## Estado por carpeta

### Bedrock

La carpeta `bedrock/` ya fue revisada y homogeneizada como conjunto de historias vivas. Sus artefactos principales están alineados con la arquitectura actual integrada en `lambda-aws-cattle-java`.

Historias disponibles:

- `HU-BEDROCK-001-IMPLEMENTACION.md`
- `HU-BEDROCK-002-TESTING.md`
- `HU-BEDROCK-003-SEGURIDAD.md`
- `HU-BEDROCK-004-DOCUMENTACION.md`
- `HU-BEDROCK-AGENT-001-knowledge-base.md`
- `HU-ASEGURAMIENTO-CALIDAD-001.md`

### Milking

La carpeta `milking/` contiene historias específicas del dominio de lactancia. En la revisión rápida de esta pasada no apareció la misma deriva sistemática observada en Bedrock, pero no se ha hecho todavía una normalización completa de esos artefactos.

### Summary

La carpeta `summary/` concentra historias vivas del slice de resumen consolidado de bovinos en backend. Debe usarse para registrar deuda técnica de pruebas, cobertura y consistencia entre controller, processor, service, repository, mapper, DTOs y entidades asociadas al flujo `SUMMARY`.

Historias disponibles:

- `HU-20260428-deuda-tecnica-summary.md`

### Eventos Bovinos

La carpeta `eventos-bovines/` existe en la estructura del repositorio y debe tratarse como superficie documental separada cuando se revisen historias del dominio bovino orientadas a eventos.

### Estimaciones

La carpeta `estimaciones/` concentra artefactos de planeación y backlog técnico. En esta revisión aparecieron varias señales de deriva documental, por ejemplo:

- referencias a rutas o nombres de controladores antiguos
- comandos de testing heredados
- enlaces documentales que ya no coinciden con la estructura actual
- supuestos operativos sobre tablas o endpoints que conviene revalidar

Por eso debe leerse como material de planificación histórica o de trabajo, no como fuente arquitectónica principal sin contraste con el código actual.

## Orden de lectura recomendado

1. Si el tema es chatbot o Bedrock, empezar por `bedrock/`.
2. Si el tema es summary de bovinos, revisar `summary/` y contrastar el estado real con `src/main/java/com/cattle/*Summary*` y `src/test/java/**/*Summary*.java`.
3. Si el tema es arquitectura o contratos reales, contrastar siempre con `docs/arquitectura/`.
4. Si el tema cae en `estimaciones/`, validar primero que el artefacto siga siendo coherente con el código antes de reutilizarlo para decisiones técnicas.

## Criterio de mantenimiento

- Las historias deben mantenerse como artefactos vivos con trazabilidad.
- Cuando una historia mezcle contexto vigente con topología o endpoints legacy, debe corregirse o marcarse explícitamente como histórica.
- `docs/arquitectura/` sigue siendo la fuente principal para arquitectura base; `docs/stories/` complementa esa visión desde el backlog y la entrega.