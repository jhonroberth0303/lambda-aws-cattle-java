# HU-20260428: Deuda Técnica de Testing y Cobertura del Componente Summary

**ID**: HU-20260428-deuda-tecnica-summary  
**Tipo**: Technical Debt / Quality Assurance  
**Prioridad**: Alta  
**Fecha**: 2026-04-28  
**Estado actual**: Cerrada - implementada, validada y consolidada documentalmente el 2026-04-28

## Registro de cambios

| Fecha | Versión | Descripción | Resultado |
|------|---------|-------------|-----------|
| 2026-04-28 | 1.0 | Creación de HU de deuda técnica para summary | HU abierta |
| 2026-04-28 | 1.1 | Análisis y diseño del slice summary basado en evidencia del código actual | Alcance técnico definido |
| 2026-04-28 | 1.2 | Refinamiento técnico con matriz de suites, fases y validaciones | HU lista para desarrollo |
| 2026-04-28 | 1.3 | Estimación técnica consolidada | 8 SP / 64h |
| 2026-04-28 | 1.4 | Implementación de pruebas faltantes y ampliación de suites existentes | Slice primario cubierto |
| 2026-04-28 | 1.5 | Consolidación documental de todos los artefactos en este único archivo | HU cerrada |

## Objetivo

Incrementar la seguridad de cambio del slice `summary` en `lambda-aws-cattle-java`, completando pruebas unitarias y cobertura verificable para las capas que participan en la construcción, persistencia y exposición del resumen consolidado de bovinos, con meta mínima de `90%` sobre el alcance acordado.

## Alcance vigente

La historia cubre el flujo backend asociado a `BovineSummary`, incluyendo como mínimo:

- componente expuesto del flujo summary (`BovinesSummaryController` y su punto de entrada operativo)
- `BovinesSummaryProcessor`
- `BovineSummaryService`
- `BovineSummaryRepository`
- `BovineSummaryMapper`
- `BovineSummaryDTO`
- `BovineSummary`
- utilidades o clases auxiliares con lógica propia que sean necesarias para sostener el comportamiento del summary bajo prueba

## Desarrollo de la HU

### Paso 1: Análisis basado en evidencia

#### Código revisado

- `src/main/java/com/cattle/controller/BovinesSummaryController.java`
- `src/main/java/com/cattle/processor/BovinesSummaryProcessor.java`
- `src/main/java/com/cattle/services/BovineSummaryService.java`
- `src/main/java/com/cattle/repository/BovineSummaryRepository.java`
- `src/main/java/com/cattle/mapper/BovineSummaryMapper.java`
- `src/main/java/com/cattle/dtos/BovineSummaryDTO.java`
- `src/main/java/com/cattle/entities/bovines/BovineSummary.java`
- `src/main/java/com/cattle/services/ProductiveStateCalculator.java`

#### Línea base detectada

- había suites visibles para controller, service, mapper y `ProductiveStateCalculator`
- no había evidencia directa para processor y repository
- `build.gradle` excluye `com/cattle/dtos/**/*.class` de JaCoCo

#### Decisión técnica

Se tomó como slice primario medible de la HU a `BovinesSummaryController`, `BovinesSummaryProcessor`, `BovineSummaryService`, `BovineSummaryRepository` y `BovineSummaryMapper`. `BovineSummaryDTO` y `BovineSummary` quedaron fuera del cierre inicial de cobertura y como superficie contractual opcional.

### Paso 2: Refinamiento técnico

#### Estrategia acordada

- no modificar `build.gradle` en esta iteración
- no usar integración real con AWS ni LocalStack
- crear primero las suites faltantes de processor y repository
- ampliar solo las ramas críticas no cubiertas del service

#### Plan de ejecución

- crear `src/test/java/com/cattle/processor/BovinesSummaryProcessorTest.java`
- crear `src/test/java/com/cattle/repository/BovineSummaryRepositoryTest.java`
- ajustar `src/test/java/com/cattle/services/BovineIdentityItemSummaryServiceTest.java`
- mantener sin cambios funcionales las suites ya suficientes de controller y mapper

#### Casos priorizados

- processor: delegación, retorno y propagación de excepciones
- repository: consultas, paginación, PK/SK, persistencia, borrado y traducción de excepciones DynamoDB
- service: ordenamiento `OPEN`, preñez cerrada, fallback de lifecycle, lote vacío y errores de repositorio

### Paso 3: Estimación

#### Supuestos

1. La meta del `90%` se mediría sobre controller, processor, service, repository y mapper.
2. `BovineSummaryDTO` permanecería fuera del denominador de JaCoCo.
3. Las pruebas serían unitarias con JUnit 5 y Mockito.
4. No sería necesario refactor productivo mayor para habilitar testabilidad.

#### Resultado de estimación

```text
8 Story Points
64 horas estimadas
Complejidad Alta
Riesgo Medio
```

| Fase | Alcance | SP | Horas |
|------|---------|----|-------|
| F1 | Processor | 1 | 8h |
| F2 | Repository | 3 | 24h |
| F3 | Ramas críticas de Service | 3 | 24h |
| F4 | Validación JaCoCo y ajuste fino | 1 | 8h |
| | **TOTAL** | **8 SP** | **64h** |

### Paso 4: Desarrollo ejecutado

#### Suites creadas o ajustadas

- nueva suite `src/test/java/com/cattle/processor/BovinesSummaryProcessorTest.java`
- nueva suite `src/test/java/com/cattle/repository/BovineSummaryRepositoryTest.java`
- ampliación de `src/test/java/com/cattle/services/BovineIdentityItemSummaryServiceTest.java`

#### Cobertura funcional incorporada

- processor: `refreshAllCategoriesSummary()`, `refreshAllSummaries()`, `findAll()`, `findById()`, `refreshSummary()` y propagación de excepción
- repository: `findAll()`, `findAllPaginated()`, `findById(String|Integer)`, `save()`, `saveAll()`, `delete(String|Integer)` y errores `ResourceNotFoundException` / `DynamoDbException`
- service: ordenamiento `OPEN`, preñez cerrada con `calvingDate`, fallback cuando `applyRecalculation()` retorna `null`, lote vacío y error de repositorio

No fue necesario cambiar código productivo para cerrar la HU.

## Validación y cierre

### Validación ejecutada

- `./gradlew test --tests "com.cattle.processor.BovinesSummaryProcessorTest"`
- `./gradlew test --tests "com.cattle.repository.BovineSummaryRepositoryTest"`
- `./gradlew test --tests "com.cattle.services.BovineIdentityItemSummaryServiceTest"`
- `./gradlew test --tests "com.cattle.processor.BovinesSummaryProcessorTest" --tests "com.cattle.repository.BovineSummaryRepositoryTest" --tests "com.cattle.services.BovineIdentityItemSummaryServiceTest" --tests "com.cattle.controller.BovinesSummaryControllerTest" --tests "com.cattle.mapper.BovineIdentityItemSummaryMapperTest" jacocoTestReport`

### Resultado observado

- `BovinesSummaryController`: `100%` líneas
- `BovinesSummaryProcessor`: `100%` líneas
- `BovineSummaryService`: `100%` líneas
- `BovineSummaryRepository`: `100%` líneas

Con la decisión vigente de mantener DTO fuera del denominador de JaCoCo, la meta de `>= 90%` quedó sobrecumplida en el slice primario.

### Criterios de aceptación evaluados

| Criterio | Estado | Evidencia |
|---------|--------|-----------|
| CA-001 Ejecución verificable | Cumplido | suites focalizadas y `jacocoTestReport` ejecutados en verde |
| CA-002 Cobertura funcional del slice | Cumplido | hay pruebas para listado, consulta por ID, refresh individual, refresh batch, ordenamiento y manejo de errores |
| CA-003 Cobertura mínima objetivo | Cumplido | el slice primario quedó por encima del `90%`, con `100%` por línea en controller, processor, service y repository |
| CA-004 Trazabilidad por capa | Cumplido | existe evidencia en controller, processor, service, repository y mapper |
| CA-005 Casos límite del service | Cumplido | la suite cubre inexistencia, ausencia de perfiles, preñez cerrada, fallback de lifecycle y fallos parciales |
| CA-006 Riesgo de configuración | Cumplido | DTO quedó fuera de JaCoCo por decisión explícita y documentada |

### Restricción técnica vigente

`build.gradle` excluye `com/cattle/dtos/**/*.class` de JaCoCo. Si en el futuro la meta del `90%` debe incluir DTOs, será necesario ajustar esa exclusión o agregar una política formal que mantenga esa superficie fuera del denominador.

### Pendientes fuera del núcleo de cierre

- pruebas contractuales dedicadas para `BovineSummaryDTO`
- pruebas contractuales dedicadas para `BovineSummary`
- referencias heredadas de la historia anterior alineadas al identificador vigente de esta HU

### Decisión de cierre

La HU queda cerrada porque se implementaron los gaps principales, la validación ejecutable quedó en verde, la cobertura del slice primario supera el objetivo comprometido y toda la trazabilidad del ciclo quedó consolidada en este único archivo.

## Artefacto consolidado

Este archivo reemplaza los documentos satélite de análisis y diseño, refinamiento técnico y estimación. Desde esta consolidación, `HU-20260428-deuda-tecnica-summary.md` queda como fuente única de verdad documental para el ciclo completo de la historia.

## Siguiente paso recomendado

1. Mantener estas suites como puerta de cambio obligatoria cuando se modifique el slice summary.
2. Agregar `BovineSummaryDTOTest` y `BovineSummaryTest` solo si el equipo necesita trazabilidad contractual adicional.
3. Revisar exclusiones de JaCoCo únicamente si se decide medir DTOs dentro del porcentaje objetivo futuro.
