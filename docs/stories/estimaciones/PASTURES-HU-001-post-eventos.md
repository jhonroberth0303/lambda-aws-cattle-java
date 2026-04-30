# PASTURES-HU-001: POST de Eventos de Potreros

**ID**: PASTURES-HU-001  
**Tipo**: Estimación / diseño técnico histórico  
**Estado actual**: Revisión documental parcial - Abril 2026

## Objetivo

Registrar eventos operativos de potreros para producir transiciones de estado controladas en el dominio de pastures.

## Lectura vigente

Este artefacto debe leerse hoy como una propuesta histórica de backlog. No describe con precisión garantizada el contrato real vigente del backend.

## Evidencia y desalineaciones detectadas

En su versión previa, este documento afirmaba como verificados varios elementos que hoy requieren corrección o matiz:

- usaba `PasturesController`, mientras el controlador real vigente es `PastureController`
- asumía un endpoint POST de eventos como si ya existiera en el contrato actual
- refería `TABLE_EVENTS=cattle-events-dev` como si fuera infraestructura cerrada
- enlazaba documentación con rutas históricas o reubicadas

## Lo que sí está confirmado en código cercano

- existe `PastureStatusEngine`
- existe `EntityPatch`
- existe `PatchApplier`
- existe `PastureRepository`
- existe el endpoint `GET /farms/{farmId}/pastures`

## Lo que esta HU no debe afirmar sin refinamiento nuevo

- que ya existe `POST /farms/{farmId}/pastures/{pastureId}/events`
- que existe una `TABLE_EVENTS` operativa asociada a este flujo
- que los nombres de clases y controladores históricos sigan vigentes tal cual

## Uso recomendado

Si esta HU vuelve a priorizarse para desarrollo:

1. rehacer el refinamiento técnico contra el código actual
2. confirmar controlador, processor y contrato HTTP reales
3. validar si el histórico de eventos se resolverá con `Event`, con auditoría específica o con otro patrón

## Referencias vigentes

- `docs/arquitectura/architecture-cattle-lambda-function.md`
- `docs/arquitectura/eventos/events-overview.md`
- `docs/modelo-negocio/pastures/events-architecture.md`