# Estandares de Codigo - Proyecto Cattle

## Objetivo

Centralizar las guias de estilo y construccion de codigo usadas para `cattle-front` y `lambda-aws-cattle-java` a partir de evidencia real del workspace.

## Documentos principales

- [Estandares frontend](./frontend-standards.md)
- [Estandares backend](./backend-standards.md)
- [Checklist de code review para PRs](./code-review-checklist.md)

## Alcance

Estos documentos cubren:

- stack vigente por repositorio
- convenciones de nombres y organizacion
- responsabilidades por capa o modulo
- validaciones realmente disponibles en el repo
- gaps actuales de enforcement automatico

## Como leerlos

1. Leer primero el documento del repositorio que se vaya a tocar.
2. Validar despues el cambio con el comando real del repo: `npm run lint` o `gradlew test`/`gradlew build` segun aplique.
3. Si un patron documentado no coincide con el codigo vigente, tomar el codigo como evidencia y actualizar esta carpeta.

## Regla de mantenimiento

No usar estandares genericos o heredados de otro stack como fuente de verdad.

Si cambia el stack, el lint, el build o el patron arquitectonico dominante, esta carpeta debe actualizarse en la misma iniciativa o inmediatamente despues.