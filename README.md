# Cattle Lambda Functions - Backend API Serverless

Backend serverless del ecosistema Cattle para gestión de bovinos, ordeño, potreros y capacidades de chatbot con Amazon Bedrock.

## Stack principal

| Tecnología | Uso |
|---|---|
| Java 21 | Runtime |
| Spring Boot 3.4.5 | API y configuración |
| AWS Lambda | Ejecución serverless HTTP y jobs programados |
| API Gateway | Publicación HTTP |
| DynamoDB Enhanced Client | Persistencia |
| Amazon Bedrock | Chatbot y Knowledge Base |
| Gradle Wrapper | Build y test |
| AWS SAM | Empaquetado y despliegue |

## Quick start

### Prerrequisitos

```bash
java -version
aws --version
sam --version
```

### Build y tests

```bash
./gradlew clean build
./gradlew test jacocoTestReport
```

Reporte de cobertura:

```text
build/reports/jacoco/test/html/index.html
```

### Desarrollo local

```bash
sam build
sam local start-api
```

## Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/actuator/ping` | Health check básico |
| GET | `/bovines` | Listar bovinos |
| GET | `/bovines/{id}` | Obtener bovino |
| POST | `/bovines` | Crear bovino |
| PUT | `/bovines/{id}` | Actualizar bovino |
| GET | `/summary` | Listar resúmenes de bovinos |
| GET | `/summary/{id}` | Obtener resumen |
| PUT | `/summary/{id}/refresh` | Regenerar resumen |
| POST | `/summary/refresh` | Regenerar todos los resúmenes |
| GET | `/summary/categories` | Regenerar categorías |
| POST | `/site/{siteId}/milkingProd` | Registrar ordeño |
| GET | `/site/{siteId}/milkingProd` | Vacas con lactancias |
| GET | `/site/{siteId}/milkingProd/{idBovine}` | Historial de ordeño |
| GET | `/site/{siteId}/milkingProd/{idBovine}/lactation/{lactationNumber}` | Ordeño por lactancia |
| GET | `/farms/{farmId}/pastures` | Estado de potreros |
| POST | `/api/chat/message` | Chat sobre datos de finca |
| POST | `/api/chat/knowledge` | Consulta a Knowledge Base |
| GET | `/api/chat/health` | Health del módulo chatbot |
| GET | `/swagger-ui.html` | Swagger UI |
| GET | `/v3/api-docs` | OpenAPI |

## Seguridad y operación

- JWT configurable mediante `security.enabled`.
- Rate limiting por finca.
- Sanitización de input y auditoría para endpoints de chatbot.
- CORS configurable por variable de entorno.
- Scheduler diario de `summary` a las `3:00 AM` con `EventBridge Scheduler` y Lambda dedicada.
- Swagger y endpoints de health públicos según configuración de seguridad actual.

## Variables de entorno relevantes

| Variable | Descripción |
|---|---|
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos |
| `SECURITY_ENABLED` | Activa seguridad JWT |
| `JWT_SECRET` | Secreto JWT |
| `RATE_LIMIT_PER_HOUR` | Límite por hora |
| `BEDROCK_MODEL_ID` | Modelo de Bedrock para chat |
| `BEDROCK_KB_ID` | ID de Knowledge Base |
| `BEDROCK_KB_MODEL_ARN` | Modelo usado por Knowledge Base |
| `APP_TIMEZONE` | Zona horaria operativa y del scheduler diario |
| `TABLE_BOVINES` | Tabla de bovinos |
| `TABLE_FARM_MILKING` | Tabla de ordeño |
| `TABLE_PASTURE` | Tabla de potreros |
| `TABLE_PLAN` | Tabla de planes |
| `TABLE_COUNTERS` | Tabla de contadores |

## Documentación recomendada

- [Índice de documentación](docs/README.md)
- [Índice de arquitectura](docs/arquitectura/index.md)
- [Arquitectura base del backend](docs/arquitectura/architecture-cattle-lambda-function.md)
- [Scheduler de refresh de summary](docs/arquitectura/summary-refresh-scheduler.md)
- [Arquitectura del chatbot integrado](docs/arquitectura/chatbot/ARCHITECTURE.md)

## Notas importantes

- El chatbot Bedrock está integrado en este repositorio; no debe asumirse un proyecto activo separado para esa capacidad.
- `template.yml` define la función Lambda y parte de la configuración, pero no documenta por sí solo toda la infraestructura necesaria del entorno.
- La fuente documental vigente para arquitectura y gaps es `docs/arquitectura/`.