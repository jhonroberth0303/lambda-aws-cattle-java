# Índice de Arquitectura - lambda-aws-cattle-java

Este índice organiza la documentación arquitectónica del backend serverless y referencia como fuente principal la arquitectura base validada contra el código actual.

## Documento principal

- [Arquitectura base del backend](./architecture-cattle-lambda-function.md)

## Qué cubre la arquitectura base

El documento principal resume:

- contexto del backend serverless en AWS Lambda
- stack Java 21 + Spring Boot + DynamoDB + Bedrock
- capas principales: controllers, processors, services, repositories y seguridad
- endpoints confirmados en código
- despliegue SAM, configuración operativa y dependencias de infraestructura
- riesgos, gaps y desalineaciones detectadas entre código y operación

## Artefactos relacionados

### Subarquitectura de chatbot

- [Arquitectura del chatbot](./chatbot/ARCHITECTURE.md)
- [Arquitectura del ecosistema cattle](./chatbot/ARQUITECTURA-ECOSISTEMA-CATTLE.md)
- [Guía de integración chatbot-DynamoDB](./chatbot/GUIA-INTEGRACION-CHATBOT-DYNAMODB.md)
- `chatbot/architecture-diagram.puml`

### Eventos y patrones internos

- [Índice de eventos](./eventos/index.md)
- [Visión general de eventos](./eventos/events-overview.md)
- [Entity patch pattern](./eventos/entity-patch-pattern.md)
- [Generic events builder](./eventos/generic-events-builder.md)
- [Sealed interface pattern](./eventos/sealed-interface-pattern.md)

### Notas complementarias

- [Mejoras de arquitectura](./mejoras.md)

## Criterio de lectura recomendado

1. Leer primero la arquitectura base.
2. Ir luego al dominio específico: chatbot, eventos o mejoras.
3. Contrastar cualquier duda operativa con `template.yml`, `build.gradle` y la configuración Spring antes de documentar nuevas decisiones.

## Notas de mantenimiento

- Si cambian endpoints, seguridad o dependencias AWS, primero debe actualizarse `architecture-cattle-lambda-function.md`.
- Si cambia la lógica del chatbot o del sistema de eventos, deben revisarse también los documentos en `chatbot/` y `eventos/`.
- Este índice evita repetir detalle técnico ya cubierto por la arquitectura base para reducir deriva documental.