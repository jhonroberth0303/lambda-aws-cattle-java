# Flujo Ejecutivo - Potreros

## Objetivo

Resumir para negocio cómo el backend soporta la decisión operativa sobre disponibilidad de potreros.

## Qué aporta el backend al proceso

Cuando el frontend pide el estado de potreros, el backend:

1. consulta los potreros de la finca
2. consulta sus planes de rotación
3. calcula disponibilidad y ETA
4. corrige automáticamente estado derivado cuando corresponde
5. devuelve una vista lista para operación

## Diagrama ejecutivo

```mermaid
flowchart LR
    Q[Consulta del frontend] --> CTRL[PastureController]
    CTRL --> PROC[RotationPlanProcessor]
    PROC --> BASE[Potreros y planes]
    BASE --> REGLAS[ETA y reglas de estado]
    REGLAS --> DTO[Respuesta operativa]
    DTO --> UI[Dashboard de potreros]
```

## Mensaje clave para negocio

La API de potreros entrega una vista operativa enriquecida. No es una lectura plana de base de datos: incorpora cálculo de negocio para apoyar decisiones de rotación.

## Lectura recomendada

1. Leer este documento si se necesita explicar el aporte del backend sin entrar a detalle técnico.
2. Revisar luego `flujo-transversal-potreros-frontend-backend.md` para el flujo completo.
3. Bajar a la arquitectura base o al flujo detallado si se requiere profundidad de implementación.