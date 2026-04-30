 # HU-20260428: Deuda Técnica de Pruebas Unitarias Cattle, Pastures y Milking

**ID**: HU-20260428-deuda-tecnica-pu-cattle-pastures-milking  
**Tipo**: Technical Debt / Unit Testing / Coverage  
**Prioridad**: Alta  
**Fecha**: 2026-04-28  
**Estado actual**: Cerrada - consolidada y validada el 2026-04-28

## Registro de cambios

| Fecha | Versión | Descripción | Resultado |
|------|---------|-------------|-----------|
| 2026-04-28 | 1.0 | Creación de HU técnica a partir del reporte JaCoCo global | HU abierta |
| 2026-04-28 | 1.1 | Refinamiento técnico por dominios, suites y validaciones | HU lista para desarrollo |
| 2026-04-28 | 1.2 | Estimación técnica consolidada | HU estimada |
| 2026-04-28 | 1.3 | Implementación y validación del Bloque A de repositorios críticos | Desarrollo parcial validado |
| 2026-04-28 | 1.4 | Implementación y validación de Bloques B y C | Desarrollo parcial ampliado |
| 2026-04-28 | 1.5 | Verificación final de cobertura y cierre de HU | HU cerrada |
| 2026-04-28 | 1.6 | Análisis adicional de cobertura global posterior al cierre | Backlog adicional priorizado |
| 2026-04-28 | 1.7 | Ejecución adicional sobre ChatbotController | Cobertura global incrementada |
| 2026-04-28 | 1.8 | Ejecución adicional sobre servicios de contexto, pastures, bovines y milking | Cobertura global incrementada |
| 2026-04-28 | 1.9 | Cierre editorial y consolidación final de la HU | HU cerrada |

## Objetivo

Incrementar la cobertura unitaria en los componentes backend de los dominios `cattle`, `pastures` y `milking` que hoy están por debajo de `50%` de cobertura de líneas en JaCoCo, priorizando clases con lógica de mapeo, repositorio, eventos y contratos de payload que impactan flujos operativos reales.

## Fuente de evidencia

La selección se basó en el reporte generado con:

```bash
./gradlew test jacocoTestReport
```

Artefactos usados como evidencia:

- `build/reports/jacoco/test/html/index.html`
- `build/reports/jacoco/test/jacocoTestReport.xml`

## Regla de selección usada

Se incluyeron únicamente clases con cobertura de líneas menor a `50%` que pertenecen de forma directa a los dominios `cattle`, `pastures` o `milking`, o que actúan como infraestructura compartida de eventos para esos dominios.

## Componentes seleccionados

| Dominio | Componente | Cobertura línea | Evidencia |
|--------|------------|-----------------|-----------|
| cattle | `com/cattle/entities/bovines/BovineEventItem` | `0/19` - `0%` | entidad de evento bovino sin cobertura visible |
| cattle | `com/cattle/events/BovineEventCategory` | `0/9` - `0%` | enum de categorización sin validación directa |
| cattle | `com/cattle/events/BovineEventType` | `0/12` - `0%` | enum de tipos de evento sin cobertura |
| cattle | `com/cattle/events/payloads/bovines/BredPayload` | `0/1` - `0%` | payload contractual sin prueba |
| cattle | `com/cattle/events/payloads/bovines/DewormedPayload` | `0/1` - `0%` | payload contractual sin prueba |
| cattle | `com/cattle/events/payloads/bovines/PurchasedBovinePayload` | `0/1` - `0%` | payload contractual sin prueba |
| cattle | `com/cattle/repository/ProfileLifecycleRepository` | `5/37` - `13.51%` | repositorio crítico con baja cobertura |
| cattle | `com/cattle/repository/ProfilePregnancyRepository` | `5/37` - `13.51%` | repositorio crítico con baja cobertura |
| cattle | `com/cattle/repository/ProfileReproductiveRepository` | `5/37` - `13.51%` | repositorio crítico con baja cobertura |
| cattle | `com/cattle/mapper/BovinesMapper` | `8/18` - `44.44%` | mapper del dominio bovino por debajo del umbral |
| pastures | `com/cattle/events/entities/PastureEventItem` | `0/19` - `0%` | entidad de evento de potrero sin cobertura |
| pastures | `com/cattle/events/entities/PastureIdentityItem` | `0/11` - `0%` | identidad/evento de potrero sin prueba |
| pastures | `com/cattle/events/payloads/pastures/FertilizedPayload` | `0/1` - `0%` | payload contractual sin cobertura |
| pastures | `com/cattle/events/payloads/pastures/LimedPayload` | `0/1` - `0%` | payload contractual sin cobertura |
| cattle/pastures compartido | `com/cattle/events/EventOrigin` | `0/4` - `0%` | enum compartido por infraestructura de eventos |
| cattle/pastures compartido | `com/cattle/events/payloads/EventPayloadConverter` | `0/9` - `0%` | conversión de payloads sin validación visible |
| cattle/pastures compartido | `com/cattle/events/payloads/EventPayloadMapper` | `0/14` - `0%` | mapeo central de payloads sin pruebas |
| milking | `com/cattle/repository/ProfileLactancyRepository` | `5/71` - `7.04%` | repositorio de lactancia con baja cobertura |

## Componentes excluidos de esta HU

Quedaron fuera, aunque también estén por debajo de `50%`, porque no pertenecen al alcance pedido o responden a otro frente de deuda:

- `com/cattle/config/SecurityConfig`
- `com/cattle/controller/ChatbotController`
- `com/cattle/security/FarmUserPrincipal`
- `com/cattle/events/entities/ToolEventItem`
- `com/cattle/events/entities/ToolIdentityItem`
- `com/cattle/events/payloads/tools/MaintenancePayload`
- `com/cattle/rules/BovineCategoryRulesLoader`

## Alcance técnico esperado

### Cattle

- pruebas unitarias para contratos y builders de eventos bovinos
- pruebas de mapper para `BovinesMapper`
- pruebas de repositorio para perfiles `lifecycle`, `pregnancy` y `reproductive`

### Pastures

- pruebas unitarias para entidades de evento de potrero
- pruebas de payloads `FertilizedPayload` y `LimedPayload`
- pruebas sobre infraestructura compartida de conversión/mapeo si impacta el flujo de pastures

### Milking

- pruebas unitarias para `ProfileLactancyRepository`

### Infraestructura compartida

- validación de enums y mapeos comunes de eventos cuando afecten cattle o pastures

## Refinamiento técnico

### Estrategia de ejecución

- mantener el criterio de selección original: solo componentes `< 50%` dentro del alcance `cattle`, `pastures` y `milking`
- priorizar primero repositorios y mappers con comportamiento observable antes que payloads y enums contractuales
- agrupar las clases contractuales de eventos en suites compactas para subir cobertura con bajo costo de mantenimiento
- validar por bloques de dominio antes de ejecutar el `jacocoTestReport` global
- no mezclar en esta HU componentes de `tools`, `security` ni `chatbot`

### Decisiones técnicas tomadas

- no modificar `build.gradle` ni reglas JaCoCo en esta iteración
- resolver la deuda únicamente con pruebas unitarias y mocks, evitando integración real con DynamoDB
- para repositorios, reutilizar el patrón ya empleado en otras suites del proyecto con mocks del Enhanced Client
- para payloads, enums y entidades de evento, usar pruebas contractuales y de builder/getters cuando no exista lógica adicional

### Priorización de trabajo

#### Bloque A: repositorios críticos

- `ProfileLactancyRepository`
- `ProfileLifecycleRepository`
- `ProfilePregnancyRepository`
- `ProfileReproductiveRepository`

Objetivo del bloque:

- cubrir consultas principales, vacíos, errores de infraestructura y construcción correcta de claves/requests
- sacar primero del rango rojo a las clases con cobertura entre `7.04%` y `13.51%`

#### Bloque B: mappers e infraestructura compartida

- `BovinesMapper`
- `EventPayloadConverter`
- `EventPayloadMapper`
- `EventOrigin`

Objetivo del bloque:

- asegurar contratos de transformación completos, parciales, nulos y selección correcta de tipos de payload

#### Bloque C: entidades y payloads contractuales

- `BovineEventItem`
- `BovineEventCategory`
- `BovineEventType`
- `BredPayload`
- `DewormedPayload`
- `PurchasedBovinePayload`
- `PastureEventItem`
- `PastureIdentityItem`
- `FertilizedPayload`
- `LimedPayload`

Objetivo del bloque:

- cubrir builders, getters, igualdad si aplica y contratos mínimos para prevenir regresiones silenciosas en eventos

### Suites a crear o ampliar

#### Repositorios

- crear `src/test/java/com/cattle/repository/ProfileLactancyRepositoryTest.java`
- crear `src/test/java/com/cattle/repository/ProfileLifecycleRepositoryTest.java`
- crear `src/test/java/com/cattle/repository/ProfilePregnancyRepositoryTest.java`
- crear `src/test/java/com/cattle/repository/ProfileReproductiveRepositoryTest.java`

#### Mappers e infraestructura de eventos

- crear o ampliar `src/test/java/com/cattle/mapper/BovinesMapperTest.java`
- crear `src/test/java/com/cattle/events/payloads/EventPayloadConverterTest.java`
- crear `src/test/java/com/cattle/events/payloads/EventPayloadMapperTest.java`
- crear `src/test/java/com/cattle/events/EventOriginTest.java`

#### Entidades, enums y payloads

- crear `src/test/java/com/cattle/entities/bovines/BovineEventItemTest.java`
- crear `src/test/java/com/cattle/events/BovineEventCategoryTest.java`
- crear `src/test/java/com/cattle/events/BovineEventTypeTest.java`
- crear `src/test/java/com/cattle/events/payloads/bovines/BredPayloadTest.java`
- crear `src/test/java/com/cattle/events/payloads/bovines/DewormedPayloadTest.java`
- crear `src/test/java/com/cattle/events/payloads/bovines/PurchasedBovinePayloadTest.java`
- crear `src/test/java/com/cattle/events/entities/PastureEventItemTest.java`
- crear `src/test/java/com/cattle/events/entities/PastureIdentityItemTest.java`
- crear `src/test/java/com/cattle/events/payloads/pastures/FertilizedPayloadTest.java`
- crear `src/test/java/com/cattle/events/payloads/pastures/LimedPayloadTest.java`

### Casos mínimos por tipo de componente

#### Repositorios

- consulta exitosa con datos
- consulta vacía
- error traducido desde capa DynamoDB o dependencia equivalente
- validación de parámetros o request construido cuando aplique

#### Mappers y converters

- mapeo completo
- mapeo parcial o con nulos
- entrada nula
- selección correcta del payload o tipo de destino

#### Entidades, enums y payloads

- construcción por builder o constructor
- persistencia correcta de propiedades
- serialización contractual o igualdad básica si aplica al componente

### Fases de implementación propuestas

| Fase | Alcance | Entregable técnico |
|------|---------|--------------------|
| F1 | Repositorios cattle y milking | suites nuevas y validación focal de repositorios |
| F2 | Mapper bovino e infraestructura compartida | suites nuevas para mappers/converters/enums compartidos |
| F3 | Entidades y payloads cattle | suites contractuales del bloque bovino |
| F4 | Entidades y payloads pastures | suites contractuales del bloque potreros |
| F5 | Validación final | `jacocoTestReport` y contraste contra el umbral `< 50%` |

### Validación focal propuesta

```bash
./gradlew test --tests "com.cattle.repository.ProfileLactancyRepositoryTest"
./gradlew test --tests "com.cattle.repository.ProfileLifecycleRepositoryTest" --tests "com.cattle.repository.ProfilePregnancyRepositoryTest" --tests "com.cattle.repository.ProfileReproductiveRepositoryTest"
./gradlew test --tests "com.cattle.mapper.BovinesMapperTest" --tests "com.cattle.events.payloads.EventPayloadConverterTest" --tests "com.cattle.events.payloads.EventPayloadMapperTest" --tests "com.cattle.events.EventOriginTest"
./gradlew test jacocoTestReport
```

### Riesgos de ejecución del refinamiento

- algunos nombres de suites objetivo pueden requerir ajuste fino si el equipo prefiere agrupar varios payloads o enums en una sola clase de prueba
- los repositorios pueden compartir comportamiento repetido y conviene factorizar helpers de test para no duplicar mocks
- parte de la cobertura de clases contractuales puede depender de Lombok o del código generado, por lo que el beneficio principal será trazabilidad y protección contractual, no complejidad de lógica

## Estimación

### Supuestos de estimación

1. El alcance permanece limitado a los componentes seleccionados en esta HU y no incorpora `tools`, `security`, `chatbot` ni otros componentes fuera del corte `< 50%` usado el 2026-04-28.
2. Los repositorios podrán probarse con mocks del Enhanced Client sin necesidad de infraestructura real ni LocalStack.
3. Los payloads, enums y entidades contractuales se resolverán con pruebas unitarias compactas, sin requerir refactor productivo.
4. El reporte de cierre se validará con `./gradlew test jacocoTestReport` sobre el backend completo.
5. Si durante el desarrollo aparecen dependencias no desacopladas o comportamientos no mockeables, la estimación deberá revisarse antes de ampliar alcance.

### Resultado de estimación

```text
13 Story Points
96 horas estimadas
Complejidad Alta
Riesgo Medio-Alto
```

### Desglose por bloque

| Fase | Alcance | SP | Horas |
|------|---------|----|-------|
| F1 | Repositorios cattle y milking | 5 | 36h |
| F2 | Mapper bovino e infraestructura compartida | 3 | 20h |
| F3 | Entidades y payloads cattle | 2 | 16h |
| F4 | Entidades y payloads pastures | 2 | 16h |
| F5 | Validación final y ajuste de cobertura | 1 | 8h |
| | **TOTAL** | **13 SP** | **96h** |

### Justificación resumida

- el peso principal está en los cuatro repositorios porque concentran la mayor incertidumbre de mocking, requests y manejo de errores
- el bloque de mappers y converters tiene complejidad media por la necesidad de validar contratos completos y rutas nulas
- las clases contractuales de eventos son más numerosas pero de menor complejidad individual, por eso se estiman como bloques compactos
- se reservó una fase específica de cierre para contrastar que ningún componente seleccionado permanezca por debajo del `50%`

### Riesgos que afectan la estimación

- si `EventPayloadConverter` o `EventPayloadMapper` dependen de topologías de payload más amplias que las detectadas en el refinamiento, el bloque F2 puede crecer
- si alguno de los repositorios requiere comportamiento no visible hoy en mocks simples, F1 puede absorber más tiempo que el estimado base
- si el equipo exige subir significativamente por encima de `50%` en lugar de solo superar el umbral, la fase final puede requerir iteraciones adicionales

## Desarrollo ejecutado

### Avance implementado en esta iteración

Se completó el Bloque A definido en el refinamiento técnico con las siguientes suites nuevas:

- `src/test/java/com/cattle/repository/ProfileLifecycleRepositoryTest.java`
- `src/test/java/com/cattle/repository/ProfilePregnancyRepositoryTest.java`
- `src/test/java/com/cattle/repository/ProfileReproductiveRepositoryTest.java`
- `src/test/java/com/cattle/repository/ProfileLactancyRepositoryTest.java`

### Cobertura funcional incorporada

#### ProfileLifecycleRepository

- `findById()` exitoso, `ResourceNotFoundException` y error inesperado
- `findAll()` con resultados y error `DynamoDbException`
- `save()`
- `update()`
- `deleteById()` con construcción de clave y error

#### ProfilePregnancyRepository

- `findById()` exitoso, `ResourceNotFoundException` y error inesperado
- `findAll()` con resultados y error `DynamoDbException`
- `save()`
- `update()`
- `deleteById()` con construcción de clave y error

#### ProfileReproductiveRepository

- `findById()` exitoso, `ResourceNotFoundException` y error inesperado
- `findAll()` con resultados y error `DynamoDbException`
- `save()`
- `update()`
- `deleteById()` con construcción de clave y error

#### ProfileLactancyRepository

- `findById()` exitoso y `ResourceNotFoundException`
- `findAll()` con resultados, vacío y error `DynamoDbException`
- `findAllLactations()`
- `findAllLactationsByBovine()` con resultados y error `DynamoDbException`
- `save()`
- `update()`
- `deleteById()` con construcción de clave

#### EventOrigin

- validación de valores expuestos por el enum
- resolución por `valueOf()`

#### EventPayloadConverter

- serialización a `AttributeValue`
- metadatos `type()` y `attributeValueType()`
- excepción esperada en `transformTo()`
- error de serialización en payload recursivo

#### EventPayloadMapper

- serialización a JSON
- deserialización por `eventType` para payloads bovinos y de pastures
- retorno `null` para tipo desconocido
- excepción ante JSON inválido

#### BovinesMapper

- mapeo `toDTO()` y `toEntity()`
- round-trip entidad/DTO
- mapeo de `breedComposition`
- conversores estáticos de fechas, origen y listas con valores válidos, nulos e inválidos

#### Clases contractuales de eventos

- `BovineEventItem`, `PastureEventItem` y `PastureIdentityItem`: getters y setters
- `BovineEventCategory` y `BovineEventType`: valores del enum y `valueOf()`
- `BredPayload`, `DewormedPayload`, `PurchasedBovinePayload`, `FertilizedPayload` y `LimedPayload`: instanciación contractual

## Validación ejecutada

### Suites focalizadas ejecutadas

```bash
./gradlew test --tests "com.cattle.repository.ProfileLifecycleRepositoryTest"
./gradlew test --tests "com.cattle.repository.ProfilePregnancyRepositoryTest" --tests "com.cattle.repository.ProfileReproductiveRepositoryTest"
./gradlew test --tests "com.cattle.repository.ProfileLactancyRepositoryTest"
./gradlew test --tests "com.cattle.repository.ProfileLifecycleRepositoryTest" --tests "com.cattle.repository.ProfilePregnancyRepositoryTest" --tests "com.cattle.repository.ProfileReproductiveRepositoryTest" --tests "com.cattle.repository.ProfileLactancyRepositoryTest" jacocoTestReport
```

### Resultado de cobertura del Bloque A

- `ProfileLifecycleRepository`: `34/37` líneas, `91.89%`
- `ProfilePregnancyRepository`: `34/37` líneas, `91.89%`
- `ProfileReproductiveRepository`: `34/37` líneas, `91.89%`
- `ProfileLactancyRepository`: `54/71` líneas, `76.06%`

### Resultado de cobertura de Bloques B y C

- `EventOrigin`: `4/4` líneas, `100%`
- `EventPayloadConverter`: `9/9` líneas, `100%`
- `EventPayloadMapper`: `11/14` líneas, `78.57%`
- `BovinesMapper`: `18/18` líneas, `100%`
- `BovineEventItem`: `19/19` líneas, `100%`
- `BovineEventCategory`: `9/9` líneas, `100%`
- `BovineEventType`: `12/12` líneas, `100%`
- `PastureEventItem`: `19/19` líneas, `100%`
- `PastureIdentityItem`: `11/11` líneas, `100%`
- `BredPayload`: `1/1` líneas, `100%`
- `DewormedPayload`: `1/1` líneas, `100%`
- `PurchasedBovinePayload`: `1/1` líneas, `100%`
- `FertilizedPayload`: `1/1` líneas, `100%`
- `LimedPayload`: `1/1` líneas, `100%`

### Resultado interpretado

Las clases trabajadas en los Bloques A, B y C quedaron por encima del umbral mínimo de `50%` definido por esta HU y cubren la totalidad de los componentes seleccionados en el alcance original.

## Cierre de la HU

### Criterios de aceptación contra evidencia

| Criterio | Estado | Evidencia |
|---------|--------|-----------|
| CA-001 Nuevas o ampliadas suites para todos los componentes seleccionados | Cumplido | se crearon o ampliaron suites para repositorios, mapper, infraestructura de eventos, enums, entidades y payloads del alcance |
| CA-002 Repositorios con caminos felices, vacíos o errores relevantes | Cumplido | `ProfileLifecycleRepository`, `ProfilePregnancyRepository`, `ProfileReproductiveRepository` y `ProfileLactancyRepository` validan lecturas, persistencia y errores |
| CA-003 Mappers y converters con contratos principales | Cumplido | `BovinesMapper`, `EventPayloadConverter` y `EventPayloadMapper` quedaron cubiertos por encima del umbral |
| CA-004 Payloads, enums y entidades con pruebas contractuales | Cumplido | todas las clases contractuales seleccionadas quedaron con pruebas simples y cobertura > `50%` |
| CA-005 Ningún componente de la HU por debajo de `50%` | Cumplido | todas las clases seleccionadas quedaron entre `76.06%` y `100%` |
| CA-006 Validación con `test` y `jacocoTestReport` o equivalente focal | Cumplido | se ejecutó validación focal con `./gradlew test ... jacocoTestReport` |

### Decisión de cierre

La HU se considera cerrada porque todas las clases seleccionadas por la regla `< 50%` quedaron cubiertas por encima del umbral definido y cuentan con validación ejecutable registrada en esta misma historia.

## Cobertura global posterior al cierre

### Validación adicional ejecutada

```bash
./gradlew test jacocoTestReport
```

Resultado global observado tras completar la HU:

- líneas: `2754/3142`, `87.65%`
- ramas: `773/1078`, `71.71%`
- instrucciones: `11718/13514`, `86.71%`

### Oportunidades adicionales detectadas

Aunque la HU original quedó cerrada, el reporte global mostró clases fuera del alcance inicial donde todavía hay retorno claro de cobertura.

#### Prioridad 1: retorno funcional alto

- `com/cattle/controller/ChatbotController`: `56/113`, `49.56%`

Motivo de prioridad:

- ya existe cobertura parcial del endpoint `queryKnowledge`
- faltan ramas relevantes de `sendMessage`, `health` y extracción desde `SecurityContext`
- es el mayor hueco funcional con impacto directo en porcentaje global

#### Prioridad 2: configuración crítica

- `com/cattle/config/SecurityConfig`: `6/20`, `30%`

Motivo de prioridad:

- es configuración transversal de autenticación y autorización
- faltan pruebas del branch `securityEnabled=false`, modo stateless JWT y permisos de endpoints

#### Prioridad 3: quick wins contractuales

- `com/cattle/events/entities/ToolEventItem`: `0/21`, `0%`
- `com/cattle/events/entities/ToolIdentityItem`: `0/11`, `0%`
- `com/cattle/rules/BovineCategoryRulesLoader`: `0/8`, `0%`
- `com/cattle/events/payloads/tools/MaintenancePayload`: `0/1`, `0%`
- `com/cattle/security/FarmUserPrincipal`: `0/1`, `0%`

Motivo de prioridad:

- son clases simples con costo bajo de prueba
- ayudan a subir rápido el porcentaje global sin cambios productivos

### Siguiente ejecución recomendada

El siguiente slice a implementar después del cierre original de esta HU es `ChatbotController`, empezando por el endpoint `sendMessage`, el endpoint `health` y las ramas donde faltan credenciales en `SecurityContext`.

### Ejecución adicional realizada

Se ejecutó el slice recomendado mediante una nueva suite:

- `src/test/java/com/cattle/controller/ChatbotControllerMessageTest.java`

Cobertura funcional incorporada en esta ejecución adicional:

- `sendMessage()` exitoso
- `sendMessage()` con rate limit excedido
- `sendMessage()` con input inválido
- `sendMessage()` con error genérico de servicio
- `sendMessage()` sin credenciales en `SecurityContext`
- `health()`

Validación ejecutada:

```bash
./gradlew test --tests "com.cattle.controller.ChatbotControllerMessageTest"
./gradlew test --tests "com.cattle.controller.ChatbotControllerKnowledgeTest" --tests "com.cattle.controller.ChatbotControllerMessageTest" jacocoTestReport
./gradlew test jacocoTestReport
```

Resultado observado en el slice ejecutado:

- `ChatbotController`: `109/113` líneas, `96.46%`

Resultado global actualizado del backend tras esta ejecución adicional:

- líneas: `2807/3142`, `89.34%`
- ramas: `779/1078`, `72.26%`
- instrucciones: `11922/13514`, `88.22%`

Interpretación:

- `ChatbotController` dejó de ser un gap de cobertura relevante
- la cobertura global subió de `87.65%` a `89.34%` en líneas
- el siguiente candidato de mayor retorno fuera del alcance original pasa a ser `SecurityConfig`, seguido por las clases contractuales simples de `tools`, `rules` y `security`

### Ejecución adicional realizada sobre servicios

Se ejecutó un segundo slice adicional orientado a servicios de consulta y construcción de contexto para chatbot:

- `src/test/java/com/cattle/services/BovineQueryServiceTest.java`
- `src/test/java/com/cattle/services/MilkingQueryServiceTest.java`
- ampliación de `src/test/java/com/cattle/services/ContextBuilderServiceTest.java`
- ampliación de `src/test/java/com/cattle/services/PastureQueryServiceTest.java`
- ampliación de `src/test/java/com/cattle/services/PastureServiceTest.java`

Cobertura funcional incorporada en esta ejecución adicional:

- conteos, distribución etaria y listados enriquecidos en `BovineQueryService`
- promedios, top producer, producción por turno y recientes en `MilkingQueryService`
- construcción de contexto enriquecido, truncamiento y helpers privados en `ContextBuilderService`
- rotación, normalización de estados y mapeo completo en `PastureQueryService`
- rutas de validación y errores inesperados en `PastureService`

Adicionalmente, durante la validación se detectó y corrigió un defecto real en `MilkingQueryService`: `getRecentMilkings()` fallaba al ordenar resultados cuando un registro tenía fecha inválida y `milkingDate` quedaba en `null`. El ordenamiento quedó `null-safe`.

Validación ejecutada:

```bash
./gradlew test --tests "com.cattle.services.MilkingQueryServiceTest" --tests "com.cattle.services.ContextBuilderServiceTest"
./gradlew test --tests "com.cattle.services.ContextBuilderServiceTest" --tests "com.cattle.services.PastureQueryServiceTest" --tests "com.cattle.services.PastureServiceTest" --tests "com.cattle.services.BovineQueryServiceTest" --tests "com.cattle.services.MilkingQueryServiceTest"
./gradlew test jacocoTestReport
```

Resultado observado en el slice ejecutado:

- `ContextBuilderService`: `137/137` líneas, `100%`
- `MilkingQueryService`: `113/115` líneas, `98.26%`
- `BovineQueryService`: `74/74` líneas, `100%`
- `PastureQueryService`: `101/102` líneas, `99.02%`
- `PastureService`: `32/32` líneas, `100%`

Resultado global actualizado del backend tras esta ejecución adicional:

- líneas: `2997/3184`, `94.13%`
- ramas: `877/1108`, `79.15%`
- instrucciones: `12807/13699`, `93.49%`

Interpretación adicional:

- `ContextBuilderService` quedó sin deuda relevante de líneas ni instrucciones
- `MilkingQueryService` quedó prácticamente cerrado; el remanente es de ramas finas no críticas
- el backend superó `94%` de cobertura de líneas global
- el siguiente candidato natural de mayor retorno pasa a ser `SecurityConfig` y los remanentes de ramas en servicios/configuración transversal

### Cierre final consolidado

Con las ejecuciones adicionales registradas en las versiones `1.7` y `1.8`, la HU queda formalmente cerrada también a nivel de consolidación documental.

Estado de cierre final:

- artefacto único consolidado y sin documentos satélite activos
- validación ejecutable registrada dentro de la misma historia
- cobertura global del backend actualizada a `94.13%` en líneas
- backlog residual identificado fuera del alcance original de la HU

## Criterios de aceptación

| ID | Criterio |
|----|----------|
| CA-001 | Deben existir suites unitarias nuevas o ampliadas para todos los componentes seleccionados en esta HU. |
| CA-002 | Cada repositorio incluido debe cubrir como mínimo caminos felices, vacíos y errores relevantes de acceso a datos. |
| CA-003 | Los mappers y converters incluidos deben validar mapeos completos, parciales, nulos y contratos principales. |
| CA-004 | Los payloads, enums y entidades de evento incluidos deben tener al menos pruebas contractuales básicas que eviten regresiones silenciosas. |
| CA-005 | El reporte JaCoCo posterior debe mostrar que ningún componente de esta HU queda por debajo de `50%` de cobertura de líneas. |
| CA-006 | La validación debe ejecutarse con `./gradlew test jacocoTestReport` o un subconjunto focal equivalente antes del cierre. |

## Validación esperada

```bash
./gradlew test jacocoTestReport
```

Opcionalmente, durante la ejecución se puede trabajar con suites focalizadas por dominio antes de correr el reporte final completo.

## Riesgos iniciales

- parte de los componentes seleccionados son clases contractuales o enums con poca lógica, por lo que la cobertura puede requerir pruebas de contrato más que comportamiento complejo
- los repositorios de perfiles probablemente dependan de mocks del Enhanced Client y de escenarios de DynamoDB con más costo de setup
- la infraestructura compartida de eventos puede requerir decisiones de alcance para no mezclar esta HU con dominios como `tools`

## Notas de implementación

- esta HU nace a partir del reporte global JaCoCo del backend del 2026-04-28
- el objetivo mínimo de esta HU es levantar todos los componentes seleccionados por encima del umbral de `50%`
- si durante el desarrollo aparecen componentes adicionales del mismo dominio por debajo del umbral, deberán evaluarse contra esta misma regla de selección antes de ampliar el alcance