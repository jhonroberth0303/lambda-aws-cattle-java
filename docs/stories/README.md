# Historias de Usuario - lambda-aws-cattle-java

Este índice organiza los artefactos de `docs/stories/` y deja explícito qué superficies ya fueron alineadas con el código actual y cuáles siguen siendo principalmente históricas o candidatas a revisión adicional.

## Estructura vigente

```text
stories/
├── README.md
├── bedrock/
├── bugs-deuda-tecnica/
├── buscador/
├── cattle-pastures-milkingProd/
├── configuracion/
├── estimaciones/
├── eventos-bovines/
├── milkingProd/
├── notificaciones/
├── settings/
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

La carpeta `milkingProd/` contiene historias específicas del dominio de lactancia. En la revisión rápida de esta pasada no apareció la misma deriva sistemática observada en Bedrock, pero no se ha hecho todavía una normalización completa de esos artefactos.

Historias disponibles:

- `HU-001-consulta-lactancias.md`
- `HU-20260429-flujo-registro-lactancias.md`

### Cattle Pastures Milking

La carpeta `cattle-pastures-milkingProd/` concentra deuda técnica transversal de pruebas unitarias y cobertura para componentes backend de bovinos, potreros y lactancia cuando el alcance cruza más de un dominio y conviene gestionar la remediación como una única HU técnica.

Historias disponibles:

- `HU-20260428-deuda-tecnica-pu-cattle-pastures-milkingProd.md`

### Summary

La carpeta `summary/` concentra historias vivas del slice de resumen consolidado de bovinos en backend. Debe usarse para registrar deuda técnica de pruebas, cobertura y consistencia entre controller, processor, service, repository, mapper, DTOs y entidades asociadas al flujo `SUMMARY`.

Historias disponibles:

- `HU-20260428-deuda-tecnica-summary.md`
- `HU-20260428-schedulersummary-refresh.md`

### Bugs y deuda técnica

La carpeta `bugs-deuda-tecnica/` registra hallazgos transversales detectados durante pruebas o revisiones que no encajan en una sola superficie de dominio y aún no tienen historia de remediación.

Artefactos disponibles:

- `DT-20260909-eventos-salida-no-proyectan-estado-ni-summary.md` — los eventos MUERTE/VENTA no cambian el `LifecycleStatus` ni se reflejan en `GET /summary`; además el estado productivo/alertas y `farmId` quedan inconsistentes para bovinos inactivos, y el guard `isBaja` del front no filtra estados del backend (hallazgo D resuelto; A/B/C abiertos). Detectado probando la Fase 2 de `configuracion/EP-20260909`.
- `DT-20260910-epica-configuracion-cierre-diferidos.md` — backlog de seguimiento al cierre de `configuracion/EP-20260909`: i18n con librería, `openapi-typescript`, `species` enum, selector de finca, test anti-deriva bundle↔YAML, `PRE_ENTRY_ITEMS` como catálogo, bug ARIA de `SearchBar`. Nada bloquea; la deriva activa ya está eliminada.

### Eventos Bovinos

La carpeta `eventos-bovines/` existe en la estructura del repositorio y debe tratarse como superficie documental separada cuando se revisen historias del dominio bovino orientadas a eventos.

### Configuración

La carpeta `configuracion/` concentra el análisis y diseño para eliminar los datos quemados del frontend (catálogos, i18n, configuración de negocio) y para el módulo de configuración por sitio. Separa por naturaleza del dato: catálogos de dominio (fuente única en backend), textos de UI (i18n del front), configuración de negocio (`SiteSettingItem` versionado) y mocks (a eliminar).

Artefactos disponibles:

- `EP-20260909-configuracion-catalogos-i18n.md` — épica transversal en 5 fases (consolidación en front, endpoint de catálogos, formularios schema-driven, configuración de negocio en `SiteSetting`, pruebas unitarias del front).

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