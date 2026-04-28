# PASTURES-HU-020: Soft Delete de Potreros

**ID**: PASTURES-HU-020  
**Tipo**: Estimación / backlog funcional  
**Estado actual**: Revisión documental parcial - Abril 2026

## Objetivo

Evaluar un mecanismo de eliminación lógica para potreros que preserve histórico y permita excluir registros de listados operativos sin borrado físico inmediato.

## Evidencia confirmada hoy en el repositorio

- `PastureController` expone actualmente `GET /farms/{farmId}/pastures` para semáforo de rotación
- no se confirmó endpoint `DELETE /farms/{farmId}/pastures/{pastureId}` en el controlador real
- no se confirmaron campos `deletedAt` o `deletedBy` en la entidad de pastures revisada por esta pasada
- no se confirmó estado `REMOVED` como parte operativa del contrato actual de pastures
- no se confirmó integración SNS/EventPublisher operativa asociada a eliminación de potreros

## Lectura vigente

Esta HU debe leerse como backlog no implementado. Su versión anterior mezclaba intención válida con detalles de implementación no confirmados, incluyendo controladores nuevos, repositorios JPA, auditoría cerrada y publicación de eventos SNS.

## Qué sí puede afirmarse

- el caso de uso de eliminación lógica tiene sentido para preservar historial
- la necesidad de distinguir datos activos de datos retirados es consistente con la evolución esperable del dominio
- la historia sigue siendo útil como backlog de integridad y gobierno de datos

## Qué no debe afirmarse sin refinamiento nuevo

- que ya existe `DELETE /farms/{farmId}/pastures/{pastureId}`
- que ya existe `PATCH /farms/{farmId}/pastures/{pastureId}/restore`
- que el modelo actual de pastures tenga `deletedAt`, `deletedBy` o `REMOVED`
- que exista auditoría operativa cerrada para este flujo
- que se publique `PASTURE_DELETED` por SNS al eliminar

## Riesgos de la versión histórica

La versión anterior también arrastraba supuestos que hoy son engañosos para desarrollo:

- proponía `PastureService` y `PastureRepository` con estilo JPA no alineado con la superficie real revisada
- describía `EventPublisher` y `DeleteEventPublisher` como piezas existentes
- trataba `application.properties` y configuraciones de modo de delete como si ya gobernaran este comportamiento

## Uso recomendado

Si esta HU vuelve a priorizarse:

1. confirmar primero el modelo de persistencia real de `Pasture`
2. decidir si el soft delete se implementará con estado lógico, timestamp de baja o ambos
3. definir después el contrato HTTP real, la trazabilidad de auditoría y si existirá o no publicación de eventos

## Referencias vigentes

- `src/main/java/com/cattle/controller/PastureController.java`
- `src/main/java/com/cattle/entities/Pasture.java`
- `src/main/java/com/cattle/enums/PastureStatus.java`
- `docs/arquitectura/architecture-cattle-lambda-function.md`