# Componente Summary de Bovinos - Backend

## Contexto

En `lambda-aws-cattle-java`, `summary` es un componente backend especializado que expone una vista consolidada del bovino para consumo rápido desde frontend.

No es simplemente otro alias de `/bovines`. Tiene controlador, processor, servicio, repositorio, entidad y DTO propios.

## Evidencia revisada

- `src/main/java/com/cattle/controller/BovinesSummaryController.java`
- `src/main/java/com/cattle/processor/BovinesSummaryProcessor.java`
- `src/main/java/com/cattle/services/BovineSummaryService.java`
- `src/main/java/com/cattle/repository/BovineSummaryRepository.java`
- `src/main/java/com/cattle/dtos/BovineSummaryDTO.java`

## Propósito

El componente `summary` sirve para:

- entregar una vista resumida y lista para UI de cada bovino
- centralizar estados calculados que el frontend usa en tarjetas
- regenerar esa vista cuando cambian perfiles o estado productivo

## Estructura del componente

### Capa HTTP

`BovinesSummaryController` expone:

- `GET /summary`
- `GET /summary/{id}`
- `PUT /summary/{id}/refresh`
- `POST /summary/refresh`
- `GET /summary/categories`

Responsabilidad: exponer contratos REST, validar IDs simples y devolver estados HTTP apropiados.

### Capa de orquestación

`BovinesSummaryProcessor` es un pass-through liviano sobre `BovineSummaryService`.

Responsabilidad: mantener consistente el patrón `Controller -> Processor -> Service` del backend.

### Capa de negocio

`BovineSummaryService`:

- consulta identidad base del bovino
- consulta perfiles de lifecycle, reproducción, lactancia y preñez
- recalcula lifecycle cuando corresponde
- calcula estados productivos y alertas
- construye y persiste el ítem `SUMMARY`

Responsabilidad real: ensamblar una proyección lista para presentación, no solo hacer passthrough a DynamoDB.

### Capa de persistencia

`BovineSummaryRepository`:

- lee todos los summaries usando `GSI1PK = SUMMARY`
- obtiene un summary puntual con `PK = BOVINE#{id}` y `SK = SUMMARY`
- persiste ítems summary individual o batch

La implementación usa la misma tabla de bovinos, pero como una proyección separada dentro de ese agregado.

## Contrato expuesto

`BovineSummaryDTO` entrega información consolidada en cinco bloques:

- identidad
- lifecycle
- preñez
- lactancia
- estados calculados y alertas

Esto permite que el frontend renderice tarjetas sin recomponer reglas complejas en el cliente.

## Contrato exacto consumido por `BovineCard`

El frontend no consume todos los campos del DTO con la misma intensidad. El consumo actual confirmado en `BovineCard.jsx` es este:

| Campo DTO | Consumo confirmado en UI | Observación |
|---|---|---|
| `bovineId` | sí | usado para acciones y navegación |
| `name` | sí | título principal |
| `breed` | sí | subtítulo |
| `gender` | sí | reglas visuales macho/hembra |
| `bornDate` | sí | fecha visible y edad derivada |
| `enabled` | sí | estado visual y acciones habilitadas |
| `status` | sí | badge de inactividad |
| `category` | sí | badge principal y métricas secundarias |
| `lifeStage` | sí | visible para animales fuera del flujo productivo |
| `productiveState` | sí | principal pivote visual para reproducción y lactancia |
| `isPregnant` | sí | fallback de inferencia |
| `expectedDueDate` | sí | fecha estimada de parto |
| `daysUntilDue` | sí | semántica de proximidad de parto |
| `lactationStartDate` | sí | fallback para DEL |
| `daysInLactation` | sí | pill y métrica visible |
| `lactationNumber` | sí | contador y badge de lactancia |
| `daysSinceCalving` | sí | postparto visible |
| `alerts` | sí | alertas activas |

Campos expuestos pero no consumidos de forma directa en ese card:

- `farmId`
- `pregnancyStatus`
- `calvingDate`
- `isLactating`
- `lactationStatus`
- `reproductiveState`
- `updatedAt`

Implicación: no todo cambio en `BovineSummaryDTO` rompe la tarjeta, pero sí rompe la UX cualquier cambio incompatible sobre los campos consumidos arriba.

## Flujo principal

### Consulta de listado

1. El cliente llama `GET /summary`.
2. El controller delega en `BovinesSummaryProcessor.findAll()`.
3. El servicio consulta `BovineSummaryRepository.findAll()`.
4. Los resultados se mapean a `BovineSummaryDTO` y se ordenan con `OPEN` primero.
5. Se devuelve la lista al frontend.

### Regeneración de un summary

1. El cliente llama `PUT /summary/{id}/refresh`.
2. El servicio consulta la identidad base del bovino.
3. Lee perfiles relacionados.
4. Recalcula lifecycle y estado productivo.
5. Persiste el nuevo ítem `SUMMARY`.
6. Devuelve el DTO regenerado.

### Regeneración batch

1. El cliente llama `POST /summary/refresh`.
2. El servicio recorre todos los bovinos.
3. Intenta reconstruir cada summary.
4. Guarda el lote disponible y devuelve el conteo actualizado.

## Dependencias del componente

- `BovineRepository`
- `ProfileLifecycleRepository`
- `ProfileReproductiveRepository`
- `ProfileLactancyRepository`
- `ProfilePregnancyRepository`
- `LifecycleRecalculationService`
- `ProductiveStateCalculator`
- `BovineSummaryMapper`
- `LambdaContext`

Conclusión: `summary` es una proyección derivada con reglas de negocio relevantes, no una consulta plana.

## Riesgos y gaps observados

- el endpoint `/summary/categories` llama internamente a `refreshAllSummaries()`, así que su nombre público no refleja con precisión el comportamiento implementado
- la proyección `SUMMARY` puede divergir de la identidad base si no se ejecuta regeneración tras cambios relevantes
- el frontend principal de bovinos depende de esta proyección para listar, por lo que una degradación aquí afecta la entrada al dominio completo
- el componente no está aislado en infraestructura propia; comparte tabla y ciclo operativo con el dominio bovino principal
- parte de la semántica visual del frontend sigue dependiendo de combinaciones entre `productiveState`, fechas y fallbacks locales, por lo que pequeños cambios en datos calculados pueden alterar el comportamiento del card sin cambiar el contrato HTTP formal

## Relación con otros artefactos

- Este componente sostiene la vista de entrada del flujo transversal de bovinos.
- Complementa, pero no reemplaza, el contrato CRUD de `/bovines`.
- Debe leerse junto con `flujos/flujo-transversal-bovinos-frontend-backend.md` cuando se cambie el listado principal.