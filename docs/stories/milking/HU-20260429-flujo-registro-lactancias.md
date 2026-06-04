# HU-20260429: Flujo de Registro de Lactancias para Ordeño

**ID**: HU-20260429-flujo-registro-lactancias  
**Tipo**: Ajuste funcional y consistencia operativa  
**Prioridad**: Alta  
**Fecha**: 2026-04-29  
**Estado actual**: En progreso - implementación backend y validación focalizada completadas; pendiente alineación de consumidores y cierre final

## Registro de cambios

| Fecha | Versión | Descripción | Resultado |
|------|---------|-------------|-----------|
| 2026-04-29 | 1.0 | Creación de HU para corregir el flujo de registro de ordeño y el listado operativo de vacas con lactancias | HU abierta |
| 2026-04-29 | 1.1 | Análisis arquitectónico y diseño técnico del slice `milkingProd` incorporados al artefacto | Lista para implementación |
| 2026-04-29 | 1.2 | Refinamiento técnico ejecutado con estrategia de implementación, validación y migración contractual | Lista para desarrollo |
| 2026-04-29 | 1.3 | Estimación de la historia formalizada con supuestos, confianza y desglose de esfuerzo | Lista para desarrollo |
| 2026-04-29 | 1.4 | Implementación backend del slice `milkingProd` completada con separación operativo/histórico y validación focalizada | Lista para integración |

## Contexto de negocio

En el flujo actual de ordeño se están incluyendo bovinos que ya no deberían participar en operación, por ejemplo vacas con `ProfileLifecycle` en estado `SOLD`, pero que todavía conservan una lactancia con estado `LACTATING`.

Esto genera dos problemas de negocio:

- se pueden exponer vacas no operativas dentro del módulo de ordeño
- el registro de un ordeño puede apoyarse en una lactancia abierta pero inconsistente con el estado real del bovino

Adicionalmente, el flujo actual consulta todas las lactancias del bovino para decidir cuál asignar al ordeño, lo que aumenta costo y complejidad sin necesidad cuando ya existe una referencia explícita a la lactancia actual en `ProfileReproductive.currentLactationId`.

## Problema observado

Caso reportado:

- el endpoint `GET /site/{siteId}/milkingProd` devuelve el bovino `172`
- ese bovino fue vendido y no debería aparecer en la vista operativa de ordeño
- dentro de la misma respuesta también se devuelven lactancias `CLOSED`, aunque para operación diaria solo interesa la lactancia activa válida

Evidencia funcional revisada en código:

- `MilkingProcessor.assignLactationToMilking(...)` consulta `findAllLactationsByBovine(pk)` y toma la primera lactancia `LACTATING`
- el flujo no valida `ProfileLifecycle` antes de asignar la lactancia al ordeño
- `GET /site/{siteId}/milkingProd` hoy agrupa todas las lactancias encontradas por finca y las devuelve como histórico agregado, sin filtrar estado operativo del bovino

## Objetivo

Corregir el flujo operativo de ordeño para que solo participen bovinos habilitados para operación y para que la asignación de lactancia durante el registro de ordeño use la fuente mínima y consistente de verdad.

## Análisis arquitectónico

### Evidencia revisada

- `MilkingController` expone actualmente `POST /site/{siteId}/milkingProd` y `GET /site/{siteId}/milkingProd` bajo el mismo recurso.
- `MilkingProcessor.createMilking(...)` delega en `assignLactationToMilking(...)` la resolución de la lactancia a asociar.
- `MilkingProcessor.assignLactationToMilking(...)` usa hoy `ProfileLactancyRepository.findAllLactationsByBovine(pk)` y toma la primera lactancia `LACTATING`.
- `MilkingProcessor.getCowsWithLactations(siteId)` usa `ProfileLactancyRepository.findAllLactations(siteId)` y agrupa por bovino sin filtrar `ProfileLifecycle`.
- `ProfileLifecycleRepository` y `ProfileReproductiveRepository` ya existen y permiten leer `PROFILE#LIFECYCLE` y `PROFILE#REPRODUCTIVE` por PK/SK.

### Arquitectura actual del slice

```text
MilkingController
	-> MilkingProcessor
		-> MilkingService
		-> ProfileLactancyRepository
```

Limitaciones observadas:

- el processor decide la elegibilidad operativa del bovino sin consultar lifecycle
- el alta de ordeño resuelve lactancia por exploración completa de registros del bovino en vez de usar el puntero funcional existente
- el listado raíz de `milkingProd` mezcla una necesidad operativa con una vista histórica agregada

### Síntoma, causa raíz e impacto

Síntoma:

- el endpoint operativo devuelve vacas vendidas y lactancias cerradas
- el alta de ordeño podría aceptar una vaca con lactancia abierta pero no operativa

Causa raíz:

- el criterio de elegibilidad está inferido desde lactancia y no desde lifecycle + reproductive
- el contrato actual de `GET /milkingProd` fue implementado como histórico agregado y no como lista operativa de ordeño

Impacto:

- inconsistencias de negocio visibles en UI y API
- mayor consumo de lectura para el flujo de alta
- mayor riesgo de registrar ordeños contra animales fuera de operación

### Restricciones y decisiones de contorno

- no se rediseñará DynamoDB en esta HU
- se prioriza corrección funcional y consistencia contractual sobre optimización masiva
- se debe reutilizar infraestructura y repositorios ya existentes en el proyecto

## Diseño técnico

### Principio de diseño adoptado

El backend debe resolver el flujo de ordeño desde la fuente de verdad más cercana a la decisión operativa:

1. estado operativo del bovino en `ProfileLifecycle`
2. referencia de lactancia vigente en `ProfileReproductive.currentLactationId`
3. detalle de la lactancia vigente en `ProfileLactancy`

### Arquitectura objetivo del slice

```text
MilkingController
	-> MilkingProcessor
		-> MilkingService
		-> ProfileLifecycleRepository
		-> ProfileReproductiveRepository
		-> ProfileLactancyRepository
```

### Diseño del flujo de registro de ordeño

#### Secuencia objetivo

```text
POST /site/{siteId}/milkingProd
	-> MilkingController.createMilking()
	-> MilkingProcessor.createMilking()
	-> validar campos básicos del ordeño
	-> leer PROFILE#LIFECYCLE del bovino
	-> validar status OPEN y enabled=true
	-> leer PROFILE#REPRODUCTIVE del bovino
	-> obtener currentLactationId
	-> leer ProfileLactancy por PK + currentLactationId
	-> validar status LACTATING y endDate null
	-> asignar lactationNumber, GSI2PK y GSI2SK
	-> guardar MilkingRecord
```

#### Reglas de validación propuestas

| Orden | Validación | Resultado esperado |
|------|------------|-------------------|
| V-01 | `bovineId`, `date`, `shift` válidos | continuar |
| V-02 | existe `PROFILE#LIFECYCLE` | si no existe, rechazar |
| V-03 | `ProfileLifecycle.status == OPEN` | si no, rechazar |
| V-04 | `ProfileLifecycle.enabled == true` | si no, rechazar |
| V-05 | existe `PROFILE#REPRODUCTIVE` | si no existe, rechazar |
| V-06 | `currentLactationId` presente | si no, rechazar |
| V-07 | existe `ProfileLactancy` referenciada | si no, rechazar |
| V-08 | `status == LACTATING` | si no, rechazar |
| V-09 | `endDate == null` | si no, rechazar |

#### Hipótesis de implementación

La forma más pequeña y coherente de implementar el cambio es inyectar `ProfileLifecycleRepository` y `ProfileReproductiveRepository` en `MilkingProcessor` y encapsular la validación en un método interno dedicado, sin introducir todavía un nuevo servicio transversal.

Motivo:

- el slice ya centraliza la decisión en `MilkingProcessor`
- el cambio afecta un punto local bien identificado
- permite validar rápido con pruebas focalizadas sin reabrir alcance arquitectónico innecesario

### Diseño del listado operativo de vacas para ordeño

#### Problema contractual actual

El recurso `GET /site/{siteId}/milkingProd` hoy está documentado y construido como listado histórico agregado de lactancias. Ese contrato no coincide con la necesidad operativa del módulo de ordeño.

#### Decisión de diseño adoptada

Separar explícitamente vista operativa e histórica con el siguiente contrato:

- `POST /site/{siteId}/milkingProd`: registra un ordeño y valida elegibilidad operativa antes de asignar la lactancia
- `GET /site/{siteId}/milkingProd`: lista operativa de vacas ordeñables
- `GET /site/{siteId}/milkingProd/history`: histórico agregado de lactancias

Razón:

- evita ambigüedad semántica en el recurso principal
- reduce el riesgo de romper la expectativa futura del módulo operativo
- hace explícito cuándo se devuelven lactancias cerradas o vacas no operativas por fines históricos

Esta decisión queda cerrada en la HU como contrato objetivo para implementación y documentación.

#### Contrato objetivo del recurso

| Método | Ruta | Propósito |
|--------|------|-----------|
| `POST` | `/site/{siteId}/milkingProd` | Registrar un ordeño usando validación operativa y lactancia vigente |
| `GET` | `/site/{siteId}/milkingProd` | Listar vacas disponibles para el flujo operativo de ordeño |
| `GET` | `/site/{siteId}/milkingProd/history` | Consultar vacas con lactancias activas e históricas |
| `GET` | `/site/{siteId}/milkingProd/{idBovine}` | Consultar historial de ordeños de un bovino |
| `GET` | `/site/{siteId}/milkingProd/{idBovine}/lactation/{lactationNumber}` | Consultar ordeños de un bovino por lactancia |

#### Regla de elegibilidad para listado operativo

Un bovino debe aparecer en la lista operativa solo si:

- tiene `ProfileLifecycle.status = OPEN`
- tiene `ProfileLifecycle.enabled = true`
- tiene `ProfileReproductive.currentLactationId`
- la lactancia referenciada existe
- la lactancia referenciada tiene `status = LACTATING`
- la lactancia referenciada tiene `endDate = null`

#### Estrategia de datos propuesta

Como en esta HU no se rediseñan índices, el filtrado operativo debe construirse sobre la información ya disponible:

1. obtener las lactancias por finca como se hace hoy
2. agrupar por bovino
3. para cada bovino candidato, consultar lifecycle y reproductive
4. validar que la lactancia incluida en la respuesta operativa sea únicamente la lactancia vigente válida

Tradeoff aceptado:

- esta estrategia no es la más barata en RCUs para fincas grandes
- sí resuelve la inconsistencia funcional sin bloquear la HU por un rediseño de índice

### Componentes impactados

| Componente | Responsabilidad actual | Cambio esperado |
|-----------|------------------------|-----------------|
| `MilkingController` | expone endpoints de alta, listado agregado e historial | ajustar contrato del listado operativo e histórico |
| `MilkingProcessor` | decide alta y listado con base en lactancia | incorporar validación por lifecycle y reproductive |
| `ProfileLifecycleRepository` | lectura de lifecycle por PK/SK | reutilización directa |
| `ProfileReproductiveRepository` | lectura de reproductive por PK/SK | reutilización directa |
| `ProfileLactancyRepository` | lectura por bovino e índice por finca | seguir usándose para resolver la lactancia referenciada y el listado base |

### Impacto en pruebas

Se deben cubrir al menos estos escenarios:

- bovino `SOLD` con lactancia `LACTATING` no puede registrar ordeño
- bovino `OPEN` con `enabled=false` no puede registrar ordeño
- bovino `OPEN` sin `currentLactationId` no puede registrar ordeño
- bovino `OPEN` con lactancia `CLOSED` no puede registrar ordeño
- bovino `OPEN` con lactancia `LACTATING` y `endDate=null` sí puede registrar ordeño
- listado operativo excluye bovino vendido con lactancia abierta
- listado operativo excluye lactancias cerradas

### Riesgos residuales de diseño

- si existen datos inconsistentes entre lifecycle, reproductive y lactancy, el nuevo flujo rechazará operaciones que antes pasaban
- si el frontend actual depende del histórico en el endpoint raíz, habrá que coordinar el ajuste contractual
- si el volumen por finca es alto, la solución funcional puede requerir una iteración posterior de optimización

## Refinamiento técnico ejecutado

### Solución técnica adoptada

La implementación se hará como refactor local del slice `milkingProd`, manteniendo `MilkingProcessor` como punto de decisión del flujo y extendiendo sus dependencias para validar elegibilidad operativa y distinguir vista operativa de vista histórica.

No se creará en esta HU un servicio transversal nuevo, porque el cambio sigue concentrado en una superficie pequeña y ya existe un processor que orquesta el comportamiento relevante.

### Estrategia de implementación por componente

#### 1. Registro de ordeño

Cambios esperados:

- inyectar `ProfileLifecycleRepository` en `MilkingProcessor`
- inyectar `ProfileReproductiveRepository` en `MilkingProcessor`
- reemplazar el uso de `findAllLactationsByBovine(pk)` en `assignLactationToMilking(...)`
- resolver la lactancia mediante `currentLactationId` y `ProfileLactancyRepository.findById(pk, currentLactationId)`
- devolver errores de negocio claros para lifecycle inválido, bovino deshabilitado, reproductive ausente o lactancia no válida

Resultado esperado:

- el alta de ordeño deja de depender de una consulta completa por bovino
- el flujo rechaza bovinos `SOLD`, `TRANSFERRED`, `INACTIVE`, `DEAD` y `CULLED`

#### 2. Listado operativo de vacas ordeñables

Cambios esperados:

- cambiar la semántica de `MilkingProcessor.getCowsWithLactations(siteId)` para que represente la vista operativa
- filtrar cada bovino candidato usando `PROFILE#LIFECYCLE` y `PROFILE#REPRODUCTIVE`
- devolver únicamente la lactancia vigente válida en la respuesta operativa

Resultado esperado:

- `GET /site/{siteId}/milkingProd` deja de exponer bovinos no operativos
- `GET /site/{siteId}/milkingProd` deja de incluir lactancias cerradas o históricas

#### 3. Listado histórico

Cambios esperados:

- agregar un nuevo método en controller y processor para `GET /site/{siteId}/milkingProd/history`
- reutilizar la lógica de agrupación histórica existente con el menor cambio posible

Resultado esperado:

- el histórico actual se conserva en una ruta explícita
- el contrato del recurso principal queda limpio para operación

### Archivos y superficies impactadas

Archivos backend a modificar:

- `src/main/java/com/cattle/controller/MilkingController.java`
- `src/main/java/com/cattle/processor/MilkingProcessor.java`
- `src/test/java/com/cattle/controller/MilkingRecordControllerTest.java`
- `src/test/java/com/cattle/processor/MilkingProcessorTest.java`

Dependencias reutilizadas sin cambios estructurales previstos:

- `src/main/java/com/cattle/repository/ProfileLifecycleRepository.java`
- `src/main/java/com/cattle/repository/ProfileReproductiveRepository.java`
- `src/main/java/com/cattle/repository/ProfileLactancyRepository.java`

### Secuencia de desarrollo recomendada

1. Refactorizar `MilkingProcessor` para el alta de ordeño.
2. Actualizar o agregar pruebas unitarias del processor para los nuevos rechazos de negocio.
3. Refactorizar el listado operativo manteniendo el histórico aparte.
4. Agregar `GET /site/{siteId}/milkingProd/history` en controller y processor.
5. Actualizar pruebas de controller según el nuevo contrato.
6. Ejecutar validación focalizada del slice `milkingProd`.

### Compatibilidad y migración contractual

Decisión de migración:

- el contrato objetivo de la HU rompe la semántica anterior de `GET /site/{siteId}/milkingProd`
- para cerrar correctamente la historia, backend y frontend deben alinearse al nuevo significado operativo de esa ruta
- el comportamiento anterior queda preservado en `GET /site/{siteId}/milkingProd/history`

Implicación práctica:

- cualquier consumidor que espere lactancias cerradas o vacas vendidas en `GET /milkingProd` deberá migrarse a `GET /milkingProd/history`

### Estrategia de pruebas refinada

#### Processor

Casos nuevos obligatorios:

- rechaza ordeño para bovino con lifecycle `SOLD`
- rechaza ordeño para bovino con lifecycle `OPEN` y `enabled=false`
- rechaza ordeño para bovino sin `PROFILE#REPRODUCTIVE`
- rechaza ordeño para bovino sin `currentLactationId`
- rechaza ordeño para lactancia `CLOSED`
- rechaza ordeño para lactancia `LACTATING` con `endDate` informado
- registra ordeño correctamente para bovino `OPEN`, `enabled=true` y lactancia vigente válida
- `getCowsWithLactations(siteId)` devuelve solo vacas operativas
- `getCowsWithLactationsHistory(siteId)` conserva activas e históricas

#### Controller

Casos nuevos obligatorios:

- `GET /milkingProd` responde lista operativa
- `GET /milkingProd/history` responde lista histórica
- `GET /milkingProd/history` responde `404` cuando no hay histórico
- `POST /milkingProd` mantiene `400` para errores de negocio del processor

### Propuesta de validación ejecutable

- ejecutar pruebas focalizadas de `MilkingProcessorTest`
- ejecutar pruebas focalizadas de `MilkingRecordControllerTest`
- si el cambio impacta compilación por constructor de `MilkingProcessor`, correr la suite acotada de tests relacionados con `milkingProd`

### Estimación técnica

```text
5 Story Points
16 horas estimadas
Complejidad Media
Riesgo Medio
```

| Fase | Alcance | SP | Horas |
|------|---------|----|-------|
| F1 | Refactor de alta de ordeño con lifecycle + reproductive | 2 | 6h |
| F2 | Separación de listado operativo e histórico | 1 | 4h |
| F3 | Pruebas focalizadas de processor y controller | 1 | 4h |
| F4 | Ajuste documental y cierre de HU | 1 | 2h |
| | **TOTAL** | **5 SP** | **16h** |

### Tareas ejecutables refinadas

1. Inyectar `ProfileLifecycleRepository` y `ProfileReproductiveRepository` en `MilkingProcessor`.
2. Reescribir `assignLactationToMilking(...)` para resolver lactancia por `currentLactationId`.
3. Incorporar validaciones explícitas de lifecycle y estado de lactancia.
4. Cambiar `GET /milkingProd` a comportamiento operativo.
5. Agregar `GET /milkingProd/history` para conservar el comportamiento histórico.
6. Ajustar pruebas unitarias del processor y controller.
7. Ejecutar validación focalizada y registrar evidencia en la HU.

### Riesgos y decisiones a vigilar durante desarrollo

- revisar todos los puntos donde se construye `MilkingProcessor`, porque su constructor cambiará
- evitar duplicar lógica histórica y operativa cuando se separe el endpoint
- cuidar que los mensajes de error sigan siendo consistentes con el manejo actual de `IllegalArgumentException`
- verificar si el frontend consume hoy `GET /milkingProd` como histórico antes de cerrar despliegue

## Estimación de historia de usuario

### Metodología aplicada

La estimación se basó en la matriz histórica de complejidad del repositorio y en la evidencia concreta del slice `milkingProd` ya revisada en código. Para esta HU se ponderaron principalmente:

- complejidad técnica media por refactor local con cambio de dependencias
- testing medio por actualización de processor y controller
- riesgo medio por cambio contractual del endpoint raíz
- esfuerzo de refactoring medio por afectar comportamiento ya existente

La estimación no incluye optimización de índices DynamoDB ni saneamiento masivo de datos, porque ambos quedaron fuera de alcance en esta HU.

### Resultado de estimación

```text
Story Points: 5
Horas estimadas: 16h
Complejidad: Media
Riesgo: Medio
Confianza de estimación: Media-Alta
```

### Justificación de tamaño

Se mantiene en `5 SP` porque la historia combina cuatro elementos que elevan el esfuerzo por encima de un ajuste menor:

- refactor de lógica de negocio en un flujo crítico de alta
- cambio contractual en `GET /milkingProd`
- preservación del comportamiento histórico en una nueva ruta
- actualización de pruebas unitarias del slice afectado

No sube a `8 SP` porque:

- no requiere rediseño de modelo de datos
- no introduce infraestructura nueva
- no exige integración cross-service ni cambios distribuidos en múltiples bounded contexts
- el punto de cambio principal está bien localizado en `MilkingProcessor` y `MilkingController`

### Desglose de esfuerzo estimado

| Tramo | Alcance | Horas |
|------|---------|-------|
| E1 | Refactor de registro de ordeño con lifecycle + reproductive + lactancy | 6h |
| E2 | Separación de endpoint operativo e histórico | 4h |
| E3 | Ajuste y ampliación de pruebas unitarias de processor y controller | 4h |
| E4 | Ajuste documental y cierre de evidencia en la HU | 2h |
| | **TOTAL** | **16h** |

### Supuestos de estimación

1. No aparecen consumidores adicionales críticos de `GET /site/{siteId}/milkingProd` fuera del frontend esperado.
2. Los repositorios `ProfileLifecycleRepository` y `ProfileReproductiveRepository` cubren la necesidad sin cambios de infraestructura.
3. Las inconsistencias de datos detectadas se tratarán como errores de negocio en tiempo de ejecución, no como migración de datos en esta historia.
4. La suite de pruebas del slice `milkingProd` sigue siendo acotada y no arrastra fallos sistémicos ajenos a la HU.

### Factores que podrían mover la estimación

Podría subir a `8 SP` si durante desarrollo aparece alguno de estos hallazgos:

- consumidores adicionales que obliguen a una transición contractual más compleja
- múltiples puntos de construcción de `MilkingProcessor` con impacto extendido
- necesidad de tocar frontend en el mismo ciclo de entrega para absorber el nuevo contrato
- datos inconsistentes que obliguen a introducir reglas especiales de compatibilidad temporal

Podría bajar en esfuerzo real, sin cambiar la clasificación de `5 SP`, si el ajuste del controller y las pruebas resulta más lineal de lo previsto.

### Recomendación de planificación

La HU es apta para ejecutarse como una historia única dentro de un sprint normal, siempre que backend y consumidor principal del endpoint coordinen la migración contractual del listado histórico hacia `GET /site/{siteId}/milkingProd/history`.

## Implementación realizada

### Cambios aplicados

- `MilkingProcessor` ahora valida `ProfileLifecycle` antes de asignar lactancia al alta de ordeño.
- `MilkingProcessor` ahora lee `ProfileReproductive.currentLactationId` y resuelve una única lactancia vigente con `ProfileLactancyRepository.findById(...)`.
- el alta de ordeño rechaza bovinos no operativos y lactancias no válidas para ordeño.
- `MilkingProcessor.getCowsWithLactations(siteId)` quedó como vista operativa y devuelve solo vacas ordeñables con su lactancia vigente válida.
- se agregó `MilkingProcessor.getCowsWithLactationsHistory(siteId)` para preservar el comportamiento histórico.
- `MilkingController` expone ahora `GET /site/{siteId}/milkingProd/history` como ruta histórica explícita.
- las pruebas unitarias del processor y del controller fueron ajustadas al nuevo contrato.

### Archivos modificados

- `src/main/java/com/cattle/processor/MilkingProcessor.java`
- `src/main/java/com/cattle/controller/MilkingController.java`
- `src/test/java/com/cattle/processor/MilkingProcessorTest.java`
- `src/test/java/com/cattle/controller/MilkingRecordControllerTest.java`

## Validación ejecutada

Pruebas ejecutadas:

- `MilkingProcessorTest`: 23 pruebas pasando
- `MilkingRecordControllerTest`: 16 pruebas pasando

Verificación adicional:

- sin diagnósticos en `MilkingProcessor.java`
- sin diagnósticos en `MilkingController.java`
- sin diagnósticos en `MilkingProcessorTest.java`
- sin diagnósticos en `MilkingRecordControllerTest.java`

## Estado y siguiente paso

Estado actual: implementación backend y validación focalizada completadas.

Siguiente paso esperado:

1. Alinear el consumidor del endpoint `GET /site/{siteId}/milkingProd` con la nueva semántica operativa.
2. Migrar cualquier consumo histórico a `GET /site/{siteId}/milkingProd/history`.
3. Ejecutar validación integrada end-to-end cuando el frontend absorba el cambio contractual.

## Alcance

Incluye:

- validar `ProfileLifecycle` antes de asignar lactancia al registrar ordeño
- usar `ProfileReproductive.currentLactationId` como referencia primaria para obtener la lactancia vigente
- rechazar el registro de ordeño cuando el bovino no esté operativo o la lactancia referenciada no sea válida para ordeño
- separar la consulta operativa de ordeño del comportamiento histórico de listado de lactancias
- excluir de la respuesta operativa bovinos vendidos, transferidos, inactivos, muertos o descartados
- excluir de la respuesta operativa lactancias cerradas o no activas

No incluye:

- rediseño de índices DynamoDB para optimización masiva de consultas
- cambios de modelo histórico para analítica o reportes fuera del flujo operativo de ordeño
- saneamiento retroactivo automático de datos inconsistentes existentes, salvo que aparezca como subtarea explícita posterior

## Decisiones funcionales refinadas

### DF-01: Orden de validación para registrar ordeño

El backend debe validar en este orden:

1. `ProfileLifecycle` del bovino.
2. `ProfileReproductive.currentLactationId`.
3. `ProfileLactancy` apuntada por ese identificador.

No debe consultar todas las lactancias del bovino para decidir cuál usar durante el registro.

### DF-02: Regla operativa de elegibilidad

Un bovino solo puede participar en el flujo operativo de ordeño si cumple simultáneamente:

- `ProfileLifecycle.status = OPEN`
- `ProfileLifecycle.enabled = true`
- existe `currentLactationId`
- la lactancia referenciada existe
- la lactancia referenciada tiene `status = LACTATING`
- la lactancia referenciada no tiene `endDate`

### DF-03: Separación entre vista operativa e histórica

La consulta usada por el módulo operativo de ordeño no debe comportarse como histórico completo. Si el sistema necesita histórico de lactancias, ese comportamiento debe mantenerse separado del listado operativo para no mezclar vacas no operativas ni lactancias cerradas con el flujo diario.

### DF-04: Contrato de endpoints adoptado

La HU adopta explícitamente el siguiente contrato:

- `POST /site/{siteId}/milkingProd`: registra un ordeño y valida elegibilidad operativa antes de asignar la lactancia
- `GET /site/{siteId}/milkingProd`: devuelve la lista operativa de vacas ordeñables
- `GET /site/{siteId}/milkingProd/history`: devuelve el histórico agregado de vacas con lactancias activas e históricas

Con esta decisión, la raíz de `milkingProd` queda reservada para operación diaria y el histórico se expone en una ruta explícita separada.

## Criterios de aceptación

| ID | Criterio |
|----|----------|
| CA-001 | Al registrar un ordeño, el sistema valida primero el `ProfileLifecycle` del bovino antes de resolver la lactancia |
| CA-002 | Si el bovino tiene `ProfileLifecycle.status` distinto de `OPEN` o `enabled` distinto de `true`, el registro de ordeño se rechaza con error claro de negocio |
| CA-003 | El flujo de registro de ordeño usa `ProfileReproductive.currentLactationId` para obtener la lactancia vigente, sin consultar todas las lactancias del bovino |
| CA-004 | Si la lactancia referenciada no existe, está `CLOSED`, no está `LACTATING` o tiene `endDate`, el registro de ordeño se rechaza |
| CA-005 | La consulta operativa de vacas para ordeño no incluye bovinos con lifecycle `SOLD`, `TRANSFERRED`, `INACTIVE`, `DEAD` o `CULLED` |
| CA-006 | La consulta operativa de vacas para ordeño no incluye lactancias `CLOSED` ni lactancias no activas |
| CA-007 | El comportamiento histórico queda separado explícitamente del flujo operativo mediante `GET /site/{siteId}/milkingProd/history` |
| CA-008 | Existen pruebas focalizadas que cubren bovino vendido con lactancia abierta, lactancia cerrada y bovino operativo con lactancia válida |

## Supuestos

1. `ProfileLifecycle` es la fuente de verdad del estado operativo del bovino.
2. `ProfileReproductive.currentLactationId` representa la lactancia vigente esperada para operación.
3. Una lactancia con `status = LACTATING` y `endDate = null` es la única válida para asignar nuevos registros de ordeño.
4. La ruta histórica del módulo de ordeño se expondrá en `GET /site/{siteId}/milkingProd/history`.

## Impacto técnico esperado

- `MilkingProcessor` debe dejar de depender de `findAllLactationsByBovine(pk)` para el alta de ordeño
- el slice de ordeño necesitará acceso a `ProfileLifecycleRepository` y `ProfileReproductiveRepository` o un servicio equivalente que concentre la validación
- la consulta operativa de vacas con lactancias requerirá filtrado por estado operativo del bovino y por lactancia activa válida
- la documentación del contrato de `milkingProd` deberá reflejar explícitamente la separación entre `GET /milkingProd` operativo y `GET /milkingProd/history` histórico

## Riesgos y observaciones

- el frontend que hoy consuma histórico desde `GET /milkingProd` deberá alinearse con la nueva separación contractual
- si `currentLactationId` está desalineado con los datos de lactancia persistidos, el nuevo flujo hará visible esa inconsistencia más rápido, lo cual es correcto pero puede requerir saneamiento posterior
- la optimización por índice DynamoDB se difiere deliberadamente y no bloquea esta HU

## Tareas iniciales propuestas

1. Refactorizar el flujo de `assignLactationToMilking` para validar lifecycle y resolver una sola lactancia por referencia.
2. Ajustar el flujo de listado operativo para excluir bovinos no operativos y lactancias no válidas.
3. Definir si el histórico permanece en el mismo recurso con parámetro explícito o en un endpoint separado.
4. Agregar pruebas unitarias focalizadas del processor y, si aplica, del controller.
5. Actualizar la documentación viva de milkingProd al cerrar la implementación.

## Estado y siguiente paso

Estado actual: análisis y diseño completados, incluyendo decisión explícita del contrato de endpoints; implementación backend pendiente.

Siguiente paso esperado:

1. Implementar la validación de lifecycle y la resolución por `currentLactationId`.
2. Ajustar el listado operativo de vacas con lactancias.
3. Ejecutar pruebas focalizadas del slice `milkingProd` y actualizar esta HU con evidencia.

## Artefacto vivo

Esta HU debe mantenerse como artefacto vivo durante implementación, validación y cierre. Al completar el desarrollo, este mismo archivo debe registrar los cambios realizados, la decisión final sobre el contrato del endpoint operativo y la evidencia de pruebas ejecutadas.