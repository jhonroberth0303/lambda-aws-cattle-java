# PASTURES-HU-018: Integración SNS/SQS

**ID**: PASTURES-HU-018  
**Tipo**: Estimación / backlog de integración  
**Estado actual**: Revisión documental parcial - Abril 2026

## Objetivo

Evaluar una evolución event-driven para publicar cambios operativos del dominio y desacoplar consumidores secundarios.

## Evidencia confirmada hoy en el repositorio

- `build.gradle` incluye dependencia AWS para SNS
- no se confirmó dependencia SQS en `build.gradle`
- no se confirmaron clases operativas equivalentes a `EventPublisher`, `PastureEventListener` o `EventIdempotencyService`
- `LocalStackTestContainer` está configurado solo para DynamoDB en tests de integración

## Lectura vigente

Esta HU debe tratarse como backlog técnico no implementado de forma operativa extremo a extremo. La versión anterior la presentaba como si ya existieran componentes y flujo cerrados de SNS/SQS.

## Qué sí puede afirmarse

- hay interés arquitectónico explícito por desacoplar eventos
- el backend ya usa AWS SDK y puede evolucionar hacia integraciones adicionales
- existe modelado de eventos en el dominio que podría servir como punto de partida

## Qué no debe afirmarse sin refinamiento nuevo

- que ya existe publicación efectiva a SNS
- que ya existe consumo desde SQS
- que exista DLQ operativa o política de reintentos implementada
- que LocalStack actual cubra esta integración

## Uso recomendado

Si esta HU vuelve a priorizarse:

1. definir si realmente se necesita SNS + SQS o solo publicación simple
2. cerrar primero contrato de evento y puntos de emisión reales
3. actualizar dependencias, tests y observabilidad a partir de diseño vigente

## Referencias vigentes

- `build.gradle`
- `src/test/java/com/cattle/containers/LocalStackTestContainer.java`
- `docs/arquitectura/eventos/events-overview.md`