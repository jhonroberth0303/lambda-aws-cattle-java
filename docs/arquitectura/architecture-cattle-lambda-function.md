# Arquitectura Base: lambda-aws-cattle-java

## Contexto y alcance

`lambda-aws-cattle-java` es el backend serverless del ecosistema cattle. Expone una API HTTP sobre AWS Lambda con Spring Boot, persiste información en DynamoDB y además incorpora capacidades de chatbot con Amazon Bedrock.

Esta documentación se limita a la arquitectura base comprobable en el repositorio: código Java, configuración Spring, build Gradle y despliegue SAM. Cuando un aspecto depende de infraestructura no declarada en el repo, se marca explícitamente como gap.

## Evidencia revisada

- `build.gradle`: runtime Java 21, Spring Boot 3.4.5, AWS SDK, Bedrock, Security, OpenAPI, MapStruct, Lombok, JUnit y Testcontainers.
- `template.yml`: despliegue SAM de una Lambda Java 21 detrás de API Gateway con variables de entorno para CORS, seguridad, rate limit y Bedrock.
- `src/main/resources/application.properties`: configuración de CORS, JWT, Swagger y feature toggle de seguridad.
- `src/main/java/com/cattle/StreamLambdaHandler.java`: punto de entrada Lambda con `aws-serverless-java-container-springboot3`.
- `src/main/java/com/cattle/config/SecurityConfig.java`: autorización HTTP y activación condicional de seguridad.
- Controladores revisados: `BovineController`, `BovinesSummaryController`, `MilkingController`, `PastureController`, `ChatbotController`, `PingController`.
- Repositorios y servicios revisados por búsqueda: uso de `DynamoDbEnhancedClient` y tablas por variables de entorno (`TABLE_BOVINES`, `TABLE_FARM_MILKING`, `TABLE_PASTURE`, `TABLE_PLAN`, `TABLE_COUNTERS`).

## Propósito del sistema

El servicio concentra responsabilidades de backend para dominios operativos y analíticos del producto:

- CRUD y agregados de bovinos
- historial y registro de ordeño
- consulta de rotación de potreros
- resúmenes de bovinos
- chatbot transaccional y consultas a knowledge base
- seguridad, rate limiting y logging de auditoría

No es solo una Lambda delgada de passthrough. El código refleja una capa de negocio no trivial, especialmente en bovinos, milking, resumen y chatbot.

## Stack tecnológico

| Capa | Tecnología | Evidencia |
|---|---|---|
| Runtime | Java 21 | `build.gradle`, `template.yml` |
| Framework | Spring Boot 3.4.5 | `build.gradle` |
| Adaptador serverless | AWS Serverless Java Container Spring Boot 3 | `build.gradle`, `StreamLambdaHandler.java` |
| API | Spring Web MVC | `build.gradle`, controladores |
| Persistencia | DynamoDB + Enhanced Client | `build.gradle`, repositorios |
| IA generativa | Bedrock Runtime + Bedrock Agent Runtime | `build.gradle`, `ChatbotController.java` |
| Seguridad | Spring Security + JWT | `build.gradle`, `application.properties`, `SecurityConfig.java` |
| Documentación API | springdoc OpenAPI | `build.gradle`, `application.properties` |
| Build | Gradle Wrapper | `build.gradle`, `gradlew.bat` |
| Testing | JUnit 5, Mockito, Testcontainers, AssertJ, JaCoCo | `build.gradle` |

## Patrón arquitectónico principal

El patrón dominante es una arquitectura por capas montada sobre una única función Lambda.

Capas visibles:

- Controladores REST para exponer contratos HTTP.
- Processors para orquestación de casos de uso.
- Services para lógica de negocio y servicios transversales.
- Repositories para acceso a DynamoDB.
- DTOs y entidades para contratos y persistencia.

Además, el módulo de chatbot introduce servicios especializados para intención, contexto, validación, rate limiting, auditoría y Bedrock/Knowledge Base.

## Componentes principales y responsabilidades

### 1. Adaptador serverless

- `src/main/java/com/cattle/StreamLambdaHandler.java`
- `src/main/java/com/cattle/Application.java`

Responsabilidad: traducir la invocación de AWS Lambda a una aplicación Spring Boot usando `SpringBootLambdaContainerHandler`.

Implicación operativa: el servicio depende de cold starts de Java y del tiempo de inicialización del contenedor Spring.

### 2. Capa HTTP

Controladores confirmados:

- `/actuator/ping` en `PingController`
- `/bovines` en `BovineController`
- `/summary` en `BovinesSummaryController`
- `/site/{siteId}/milking` en `MilkingController`
- `/farms/{farmId}/pastures` en `PastureController`
- `/api/chat` en `ChatbotController`

Responsabilidad: validar parámetros de entrada, exponer contratos REST y delegar a processors o servicios.

### 3. Orquestación de negocio

Por evidencia del árbol y los imports de controladores, el backend usa processors para encapsular casos de uso:

- `BovineProcessor`
- `BovinesSummaryProcessor`
- `MilkingProcessor`
- `RotationPlanProcessor`

Responsabilidad: coordinar reglas, consultas y escritura entre servicios y repositorios.

### 4. Servicios de dominio y transversales

Servicios confirmados por código:

- Dominio: `BovineService`, `BovineSummaryService`, `MilkingService`, `PastureService`, `PlanService`
- Consulta especializada: `BovineQueryService`, `MilkingQueryService`, `PastureQueryService`
- Reglas: `BovineCategoryRulesService`, `ProductiveStateCalculator`, `LifecycleRecalculationService`
- Chatbot: `ChatbotService`, `BedrockService`, `KnowledgeBaseService`, `IntentDetectionService`, `ContextBuilderService`
- Transversales: `InputValidationService`, `RateLimitingService`, `AuditLoggingService`

Responsabilidad: encapsular lógica de dominio, conectividad con Bedrock y controles transversales de seguridad.

### 5. Persistencia en DynamoDB

Repositorios confirmados:

- `BovineRepository`
- `BovineSummaryRepository`
- `MilkingRepository`
- `PastureRepository`
- `PlanRepository`
- `CounterRepository`
- repositorios de perfiles sobre bovinos (`ProfileLifecycleRepository`, `ProfilePregnancyRepository`, `ProfileReproductiveRepository`, `ProfileLactancyRepository`)

La persistencia usa `DynamoDbEnhancedClient` configurado en `RepositoryConfig` y obtiene nombres de tabla desde variables de entorno.

Conclusión arquitectónica: la aplicación no crea ni modela sus tablas desde SAM; asume tablas preexistentes y nombres inyectados en runtime.

### 6. Seguridad

`SecurityConfig` define dos modos:

- `security.enabled=false`: permite todas las requests.
- `security.enabled=true`: activa JWT stateless y protege rutas seleccionadas.

Elementos visibles:

- `JwtAuthenticationFilter`
- `JwtTokenProvider`
- `FarmUserPrincipal`
- `jwt.secret`, `jwt.issuer` y `security.enabled` en `application.properties`

Responsabilidad: autenticación de requests y propagación del contexto de usuario/finca para chatbot y otros endpoints protegidos.

### 7. Chatbot y conocimiento

`ChatbotController` expone:

- `POST /api/chat/message`
- `POST /api/chat/knowledge`
- `GET /api/chat/health`

Capacidades visibles:

- sanitización de entrada
- rate limiting por finca
- auditoría de eventos
- consultas a Bedrock Runtime
- consultas a Knowledge Base mediante Bedrock

Este módulo es una subarquitectura propia dentro del backend y no un simple controlador adicional.

## APIs e integraciones

### API REST expuesta

Contratos confirmados en código:

- `GET /actuator/ping`
- `GET /bovines`
- `GET /bovines/{id}`
- `POST /bovines`
- `PUT /bovines/{id}`
- `GET /summary`
- `GET /summary/{id}`
- `PUT /summary/{id}/refresh`
- `POST /summary/refresh`
- `GET /summary/categories`
- `POST /site/{siteId}/milking`
- `GET /site/{siteId}/milking`
- `GET /site/{siteId}/milking/{idBovine}`
- `GET /site/{siteId}/milking/{idBovine}/lactation/{lactationNumber}`
- `GET /farms/{farmId}/pastures`
- `POST /api/chat/message`
- `POST /api/chat/knowledge`
- `GET /api/chat/health`

### Integraciones AWS

`template.yml` confirma:

- AWS Lambda como cómputo
- API Gateway REST tipo proxy
- IAM Role con permisos sobre CloudWatch, DynamoDB y Bedrock
- variables de entorno para CORS, toggle de seguridad, rate limit y modelo de Bedrock

### Integraciones de datos

El código confirma uso de nombres de tabla inyectados por ambiente:

- `TABLE_BOVINES`
- `TABLE_FARM_MILKING`
- `TABLE_PASTURE`
- `TABLE_PLAN`
- `TABLE_COUNTERS`

El backend también usa la tabla de bovinos para múltiples vistas o perfiles además de la identidad principal, lo que indica una estrategia de reutilización de tabla por agregado relacionado dentro de ese dominio.

## Despliegue y operación

### Empaquetado

`build.gradle` genera un zip desplegable mediante la tarea `buildZip` y hace que `build` dependa de ese artefacto.

### Despliegue

`template.yml` define una única función:

- nombre: `cattle-lambda-function`
- runtime: `java21`
- handler: `com.cattle.StreamLambdaHandler::handleRequest`
- memoria: `1024 MB`
- timeout: `30 s`
- evento: `/{proxy+}` con método `any`

### Configuración operativa visible

Variables configuradas en SAM:

- `BEDROCK_MODEL_ID`
- `BEDROCK_KB_MODEL_ARN`
- `CORS_ALLOWED_ORIGINS`
- `SECURITY_ENABLED`
- `RATE_LIMIT_PER_HOUR`

Configuración relevante en propiedades:

- `springdoc.api-docs.path=/v3/api-docs`
- `springdoc.swagger-ui.path=/swagger-ui.html`
- `cors.allowed-origins=${CORS_ALLOWED_ORIGINS:...}`
- `jwt.secret=${JWT_SECRET:...}`
- `security.enabled=${SECURITY_ENABLED:false}`
- `rate.limit.requests.per.hour=${RATE_LIMIT_PER_HOUR:100}`

## Decisiones arquitectónicas relevantes

- Ejecutar todo el backend como una sola Lambda con un único API Gateway proxy.
- Usar Spring Boot completo en serverless para conservar el modelo tradicional de controladores, seguridad y DI.
- Mantener separación por capas para que la lógica de dominio no quede atrapada en controladores o repositorios.
- Integrar chatbot y knowledge base dentro del mismo backend, reutilizando seguridad y contexto de finca.
- Delegar nombres de tablas y secretos a variables de entorno en lugar de acoplarlos al código.

## Riesgos y gaps detectados

- `template.yml` no declara variables de entorno para `TABLE_BOVINES`, `TABLE_FARM_MILKING`, `TABLE_PASTURE`, `TABLE_PLAN` ni `TABLE_COUNTERS`, aunque el código las necesita para operar.
- `template.yml` tampoco declara `JWT_SECRET`; en producción eso debe resolverse fuera de este template o el servicio dependerá del valor por defecto, lo que es inaceptable para un entorno real.
- `SecurityConfig` protege `/milking/**`, pero el controlador real usa `/site/{siteId}/milking/**`. Esa discrepancia puede dejar desalineada la seguridad respecto a las rutas reales.
- El template SAM no crea tablas DynamoDB; la arquitectura depende de infraestructura previa o administrada por fuera de este repositorio.
- La documentación raíz `README.md` del backend contiene afirmaciones que no coinciden completamente con el árbol actual y no debe usarse como única fuente de verdad.
- Al empaquetar Spring Boot dentro de una sola Lambda Java, el riesgo de cold start y crecimiento del artefacto aumenta frente a un diseño más segmentado.

## Siguiente recomendación

El siguiente paso útil es alinear documentación y operación en tres frentes:

1. Documentar explícitamente la infraestructura requerida por ambiente: tablas DynamoDB, secretos JWT y parámetros de Bedrock.
2. Corregir la matriz de seguridad para que las rutas protegidas coincidan con los controladores reales.
3. Bajar esta arquitectura base a documentos por dominio: bovinos, milking, pastures y chatbot/knowledge base.