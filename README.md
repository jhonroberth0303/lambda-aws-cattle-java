# 🐄 Cattle Lambda Functions - Backend API Serverless

Backend serverless para gestión ganadera con AWS Lambda, Spring Boot 3 y Amazon Bedrock.

## 📋 Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Runtime |
| Spring Boot | 3.4.5 | Framework web |
| AWS Lambda | - | Compute serverless |
| DynamoDB | - | Base de datos NoSQL |
| Amazon Bedrock | Claude 3 Haiku | Chatbot IA |
| Gradle | 8.x | Build tool |
| SAM CLI | - | Deployment |

## 🚀 Quick Start

### Pre-requisitos

```bash
java -version    # Java 21+
gradle -version  # Gradle 8+
aws --version    # AWS CLI v2
sam --version    # SAM CLI
```

### Build y Test

```bash
# Clonar
git clone <repo-url>
cd cattle-lambda-function

# Build
./gradlew clean build

# Tests con cobertura
./gradlew test jacocoTestReport

# Ver reporte
start build/reports/jacoco/test/html/index.html
```

### Deploy a AWS

```bash
sam build
sam deploy --guided  # Primera vez
sam deploy           # Deploys posteriores
```

## 📁 Estructura del Proyecto

```
src/main/java/com/cattle/
├── controller/           # REST endpoints
│   ├── BovinesController.java
│   ├── MilkingController.java
│   ├── PasturesController.java
│   └── ChatbotController.java
├── services/             # Lógica de negocio
│   ├── chatbot/          # Integración Bedrock
│   ├── InputValidationService.java
│   ├── RateLimitingService.java
│   └── AuditLoggingService.java
├── repository/           # Acceso a DynamoDB
├── security/             # JWT, autenticación
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
├── dtos/                 # Data Transfer Objects
├── entities/             # Entidades DynamoDB
└── config/               # Configuración Spring
```

## 🔌 Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/actuator/ping` | Health check |
| GET | `/bovineIdentityItems` | Listar bovinos |
| GET | `/bovineIdentityItems/{id}` | Obtener bovino |
| POST | `/bovineIdentityItems` | Crear bovino |
| PUT | `/bovineIdentityItems/{id}` | Actualizar bovino |
| GET | `/milkingRecord/{idBovine}` | Historial ordeño |
| POST | `/milkingRecord` | Registrar ordeño |
| GET | `/farms/{farmId}/pastures` | Dashboard potreros |
| POST | `/api/chat/message` | Chatbot IA |
| GET | `/swagger-ui.html` | Documentación API |

## 🔐 Seguridad

- **JWT Authentication**: Tokens validados en cada request
- **Rate Limiting**: 100 requests/hora por finca
- **Input Validation**: Sanitización contra SQL/NoSQL/Prompt injection
- **CORS**: Orígenes configurables por ambiente

### Configuración de Seguridad

```properties
# application.properties
security.enabled=${SECURITY_ENABLED:false}
jwt.secret=${JWT_SECRET:...}
rate.limit.requests.per.hour=100
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

## 🧪 Tests

```bash
# Ejecutar todos los tests
./gradlew test

# Tests con cobertura
./gradlew test jacocoTestReport

# Tests específicos
./gradlew test --tests "*SecurityTest*"
```

**Cobertura actual**: ~85% (581 tests)

## 📊 Métricas y Logs

- **CloudWatch Logs**: Logs estructurados JSON
- **Audit Logging**: Eventos de seguridad trazables
- **Swagger UI**: Documentación interactiva en `/swagger-ui.html`

## 📚 Documentación Adicional

| Documento | Descripción |
|-----------|-------------|
| [ARCHITECTURE.md](docs/bedrock-chatbot/ARCHITECTURE.md) | Diagramas C4 |
| [HU-BEDROCK-001](docs/bedrock-chatbot/stories/HU-BEDROCK-001-IMPLEMENTACION.md) | Historia chatbot |
| [HU-BEDROCK-003](docs/bedrock-chatbot/stories/HU-BEDROCK-003-SEGURIDAD.md) | Seguridad |
| [Índice](docs/bedrock-chatbot/INDICE-DOCUMENTACION.md) | Navegación docs |

## 🛠️ Comandos Útiles

```bash
# Desarrollo local
sam local start-api

# Ver logs en CloudWatch
aws logs tail /aws/lambda/cattle-function --follow

# Invocar función localmente
sam local invoke CattleFunction -e events/test-event.json
```

## 📋 Variables de Entorno (Producción)

| Variable | Descripción |
|----------|-------------|
| `SECURITY_ENABLED` | Habilitar autenticación JWT |
| `JWT_SECRET` | Clave secreta para JWT (256 bits) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos |
| `RATE_LIMIT_PER_HOUR` | Límite de requests/hora |

---

**Versión**: 1.0.0  
**Última actualización**: Enero 2026
