# Documentación - lambda-aws-cattle-java

Este directorio reúne la documentación operativa y arquitectónica del backend serverless `lambda-aws-cattle-java`.

## Punto de entrada recomendado

1. [Índice de arquitectura](./arquitectura/index.md)
2. [Arquitectura base del backend](./arquitectura/architecture-cattle-lambda-function.md)
3. Dominio específico: chatbot, eventos, modelo de negocio o datos

## Estructura vigente

```text
docs/
├── README.md
├── arquitectura/
├── bases-conocimiento/
├── changelog/
├── dod-pivotes/
├── estandares-codigo/
├── mejoras/
├── mockups/
├── modelo-negocio/
├── modelos-datos/
├── scripts/
├── stories/
└── tables/
```

## Contenido principal

### Arquitectura

- `arquitectura/index.md`: índice navegable del backend.
- `arquitectura/architecture-cattle-lambda-function.md`: fuente principal para stack, capas, endpoints, seguridad, despliegue y gaps.
- `arquitectura/chatbot/`: documentación del módulo Bedrock integrado dentro del backend.
- `arquitectura/eventos/`: documentación del sistema de eventos y patches del dominio de potreros.

### Negocio y datos

- `modelo-negocio/`: flujos y documentación por dominio.
- `modelos-datos/`: notas de diseño y modelos de datos.
- `bases-conocimiento/`: insumos de Knowledge Base para el módulo Bedrock.

### Gobierno de desarrollo

- `estandares-codigo/`: estándares técnicos.
- `dod-pivotes/`: material de Definition of Done y verificación.
- `stories/`: historias, estimaciones y artefactos de entrega.

## Qué reflejan estos documentos

La documentación vigente debe leerse con estas premisas, confirmadas en el código actual:

- backend monolítico serverless sobre una sola Lambda Java 21
- Spring Boot 3.4.5 como framework HTTP y de configuración
- persistencia principal en DynamoDB mediante Enhanced Client
- integración de Bedrock y Knowledge Base dentro del mismo repositorio, no en un proyecto separado activo
- seguridad JWT condicional por configuración
- despliegue mediante AWS SAM con infraestructura parcial declarada en `template.yml`

## Límites y criterio de mantenimiento

- La arquitectura base es la fuente principal de verdad para topología, endpoints e integraciones.
- Los documentos de `chatbot/` y `eventos/` amplían dominios concretos y no deben contradecir la arquitectura base.
- Cuando cambie código, configuración Spring, seguridad o template SAM, primero deben actualizarse los documentos de arquitectura antes que las guías derivadas.

Si un documento describe un componente archivado, una ruta HTTP que ya no existe o infraestructura no declarada en el repo, debe corregirse o marcarse explícitamente como legado.