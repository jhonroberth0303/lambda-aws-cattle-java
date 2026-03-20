# Arquitectura de Cattle-Bedrock

## Diagrama C4 - Nivel 1 (Sistema Completo)

```mermaid
graph TB
    subgraph "AWS Cloud"
        subgraph "API Gateway"
            APIGW["🔌 API Gateway<br/>REST API<br/>HTTP/HTTPS"]
        end
        
        subgraph "Compute"
            LAMBDA["⚡ AWS Lambda<br/>Runtime: Java 21<br/>Memory: 512MB<br/>Timeout: 30s"]
        end
        
        subgraph "Data & Storage"
            DYNAMODB["🗄️ Amazon DynamoDB<br/>NoSQL Database<br/>Tables: Bovines,<br/>Milking, Pastures, etc."]
        end
        
        subgraph "AI & ML"
            BEDROCK["🤖 Amazon Bedrock<br/>Model: Claude 3 Haiku<br/>anthropic.claude-3-haiku<br/>v1:0"]
        end
        
        subgraph "Monitoring & Logs"
            CLOUDWATCH["📊 CloudWatch<br/>Logs<br/>Metrics<br/>Alarms"]
        end
        
        subgraph "Security"
            IAM["🔐 IAM Role<br/>cattle-bedrock-lambda-role<br/>Permissions & Policies"]
        end
    end
    
    subgraph "Client"
        CLIENT["👤 Client<br/>REST API Consumer"]
    end
    
    CLIENT -->|HTTP Request<br/>GET/POST/PUT/DELETE| APIGW
    APIGW -->|Invoke| LAMBDA
    IAM -.->|Assume Role| LAMBDA
    LAMBDA -->|Query/Update| DYNAMODB
    LAMBDA -->|InvokeModel| BEDROCK
    LAMBDA -->|Logs & Metrics| CLOUDWATCH
    APIGW -->|HTTP Response| CLIENT
    
    style LAMBDA fill:#FF9900,stroke:#232F3E,color:#fff
    style APIGW fill:#FF9900,stroke:#232F3E,color:#fff
    style DYNAMODB fill:#527FFF,stroke:#232F3E,color:#fff
    style BEDROCK fill:#FF9900,stroke:#232F3E,color:#fff
    style CLOUDWATCH fill:#759C3E,stroke:#232F3E,color:#fff
    style IAM fill:#EC7211,stroke:#232F3E,color:#fff
    style CLIENT fill:#146EB4,stroke:#232F3E,color:#fff
```

## Diagrama C4 - Nivel 2 (Contenedores)

```mermaid
graph TB
    subgraph "Client Layer"
        HTTP["HTTP/REST Client"]
    end
    
    subgraph "AWS Cloud"
        subgraph "API Gateway"
            APIGW["API Gateway<br/>{proxy+}"]
        end
        
        subgraph "Lambda Container"
            HANDLER["StreamLambdaHandler<br/>Handler Entry Point"]
            SPRING["Spring Boot 3<br/>Application Context<br/>Embedded Application Server"]
            
            subgraph "Spring Controllers"
                CHATBOT["ChatbotController<br/>POST /chat<br/>GET /health"]
            end
            
            subgraph "Spring Services"
                CHATBOT_SVC["ChatbotService<br/>- Bedrock Integration<br/>- Request Processing<br/>- Response Building"]
            end
            
            subgraph "Repositories"
                BOVINE_REPO["BovineRepository"]
                MILKING_REPO["MilkingRepository"]
                PASTURE_REPO["PastureRepository"]
            end
            
            subgraph "Builders & DTOs"
                BUILDERS["Builders<br/>EventBuilder<br/>PastureBuilder<br/>TaskBuilder"]
                DTOS["DTOs<br/>ChatDTO<br/>BovineDTO<br/>MilkingDTO"]
            end
            
            subgraph "Config & Utils"
                CONFIG["Configuration<br/>CorsConfig<br/>RepositoryConfig<br/>LambdaContext"]
                UTILS["Utils<br/>Mapping<br/>Processing"]
            end
        end
        
        subgraph "Data Layer"
            DYNAMODB["DynamoDB<br/>Tables"]
        end
        
        subgraph "AI Layer"
            BEDROCK["Amazon Bedrock<br/>Claude 3 Haiku"]
        end
        
        subgraph "Observability"
            CLOUDWATCH["CloudWatch<br/>Logs & Metrics"]
        end
    end
    
    HTTP -->|REST Request| APIGW
    APIGW -->|Invoke| HANDLER
    HANDLER -->|Initialize| SPRING
    SPRING -->|Route Request| CHATBOT
    CHATBOT -->|Call| CHATBOT_SVC
    CHATBOT_SVC -->|Query Data| BOVINE_REPO
    CHATBOT_SVC -->|Query Data| MILKING_REPO
    CHATBOT_SVC -->|Query Data| PASTURE_REPO
    BOVINE_REPO -->|CRUD| DYNAMODB
    MILKING_REPO -->|CRUD| DYNAMODB
    PASTURE_REPO -->|CRUD| DYNAMODB
    CHATBOT_SVC -->|Build Prompt| BUILDERS
    CHATBOT_SVC -->|InvokeModel| BEDROCK
    CHATBOT_SVC -->|Transform Response| DTOS
    CHATBOT -->|Return DTO| HTTP
    SPRING -->|Emit| CLOUDWATCH
    CHATBOT_SVC -->|Emit| CLOUDWATCH
    
    style HANDLER fill:#FF9900,stroke:#232F3E,color:#fff
    style SPRING fill:#6BA539,stroke:#232F3E,color:#fff
    style CHATBOT fill:#00A4EF,stroke:#232F3E,color:#fff
    style CHATBOT_SVC fill:#00A4EF,stroke:#232F3E,color:#fff
    style DYNAMODB fill:#527FFF,stroke:#232F3E,color:#fff
    style BEDROCK fill:#FF9900,stroke:#232F3E,color:#fff
    style CLOUDWATCH fill:#759C3E,stroke:#232F3E,color:#fff
    style BUILDERS fill:#90C695,stroke:#232F3E,color:#fff
    style DTOS fill:#90C695,stroke:#232F3E,color:#fff
```

## Diagrama C4 - Nivel 3 (Componentes - Flujo de Solicitud de Chat)

```mermaid
sequenceDiagram
    actor Client
    participant APIGW as API Gateway
    participant HANDLER as StreamLambdaHandler
    participant SPRING as Spring Boot
    participant CTRL as ChatbotController
    participant SVC as ChatbotService
    participant REPO as Repository Layer
    participant DB as DynamoDB
    participant BEDROCK as Amazon Bedrock
    participant CW as CloudWatch
    
    Client->>APIGW: POST /chat<br/>{userMessage, context}
    APIGW->>HANDLER: Invoke Lambda<br/>with API event
    HANDLER->>SPRING: Initialize Spring Context
    SPRING->>CTRL: Route to ChatbotController
    CTRL->>SVC: processMessage(request)
    
    Note over SVC: Retrieve Context Data
    SVC->>REPO: Query Bovines, Milking, Pastures
    REPO->>DB: Scan/Query Operations
    DB-->>REPO: Return Data
    REPO-->>SVC: Return Domain Objects
    
    Note over SVC: Build AI Prompt
    SVC->>SVC: Transform data to DTOs
    SVC->>SVC: Build context prompt
    
    Note over SVC: Invoke Bedrock
    SVC->>BEDROCK: InvokeModel<br/>Model: Claude 3 Haiku<br/>Prompt: user message + context
    BEDROCK-->>SVC: Response: AI generated text
    
    Note over SVC: Process Response
    SVC->>SVC: Parse response
    SVC->>SVC: Build response DTO
    
    SVC->>CW: Emit logs & metrics
    SVC-->>CTRL: Return ResponseDTO
    CTRL-->>SPRING: HTTP Response 200 OK
    SPRING-->>HANDLER: Return JSON response
    HANDLER-->>APIGW: Return to client
    APIGW-->>Client: {responseMessage, metadata}
    CW->>CW: Store logs
```

## Diagrama C4 - Nivel 4 (Código)

```mermaid
graph LR
    subgraph "src/main/java/com/cattle"
        APP["Application.java<br/>@SpringBootApplication"]
        HANDLER["StreamLambdaHandler.java<br/>AWS Lambda Handler"]
        
        subgraph "config"
            CORS["CorsConfig.java"]
            REPO_CFG["RepositoryConfig.java"]
            LAMBDA_CTX["LambdaContext.java"]
        end
        
        subgraph "controller"
            CHATBOT_CTRL["ChatbotController.java<br/>@RestController<br/>@RequestMapping /chat"]
        end
        
        subgraph "services"
            CHATBOT_SRVC["ChatbotService.java<br/>@Service<br/>- buildPrompt()<br/>- invokeModel()<br/>- processResponse()"]
        end
        
        subgraph "repository"
            BOVINE_R["BovineRepository"]
            MILKING_R["MilkingRepository"]
            PASTURE_R["PastureRepository"]
        end
        
        subgraph "dtos"
            CHAT_DTO["ChatDTO"]
            BOVINE_DTO["BovineDTO"]
            MILKING_DTO["MilkingDTO"]
        end
        
        subgraph "entities"
            BOVINE_E["Bovine Entity"]
            MILKING_E["Milking Entity"]
            PASTURE_E["Pasture Entity"]
        end
        
        subgraph "builders"
            EVENT_B["EventBuilder"]
            PASTURE_B["PastureBuilder"]
            TASK_B["TaskBuilder"]
        end
        
        subgraph "mapper"
            MAPPERS["Entity → DTO Mappers"]
        end
        
        subgraph "processor"
            PROCESSOR["Business Logic Processors"]
        end
    end
    
    APP -->|Contains| HANDLER
    HANDLER -->|Initialize| CORS
    HANDLER -->|Initialize| REPO_CFG
    CORS -->|Configure| CHATBOT_CTRL
    CHATBOT_CTRL -->|Inject| CHATBOT_SRVC
    CHATBOT_SRVC -->|Use| BOVINE_R
    CHATBOT_SRVC -->|Use| MILKING_R
    CHATBOT_SRVC -->|Use| PASTURE_R
    BOVINE_R -->|Map| BOVINE_E
    CHATBOT_SRVC -->|Transform| CHAT_DTO
    CHATBOT_SRVC -->|Transform| BOVINE_DTO
    CHATBOT_SRVC -->|Use| BUILDERS
    CHATBOT_CTRL -->|Return| CHAT_DTO
    PROCESSOR -->|Process| BOVINE_E
```

## Pilas Tecnológicas

### 🖥️ Lenguajes & Runtimes
- **Java 21**: Lenguaje de programación principal
- **YAML**: Configuración CloudFormation

### 🏗️ Frameworks & Bibliotecas
- **Spring Boot 3.4.5**: Framework principal
- **Spring Web**: Controladores REST
- **Spring Data**: Acceso a datos
- **Lombok**: Generación automática de código (Builders, Getters/Setters)

### ☁️ Servicios AWS
- **Lambda**: Computación sin servidor
- **API Gateway**: Enrutador HTTP
- **DynamoDB**: Base de datos NoSQL
- **Amazon Bedrock**: Servicio de modelos IA
- **CloudWatch**: Registro y monitoreo
- **IAM**: Gestión de identidad y acceso

### 🤖 Modelos IA
- **Claude 3 Haiku**: Modelo de lenguaje de Anthropic
  - Función: Procesamiento de prompts y generación de respuestas
  - Uso: Análisis de contexto ganadero, recomendaciones

### 📦 Build & Deploy
- **Maven**: Gestor de dependencias
- **Gradle**: Gestor de construcción
- **SAM CLI**: AWS Serverless Application Model
- **CloudFormation**: Infraestructura como código

## Flujo de Despliegue

```
┌─────────────────┐
│   Código Fuente │
│   (Java/Spring) │
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│   Build Process     │
│ Maven/Gradle Build  │
│ JAR Package         │
└────────┬────────────┘
         │
         ▼
┌──────────────────────┐
│  AWS SAM Package     │
│  template.yml        │
│  S3 Upload           │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ CloudFormation Stack │
│ Crea:                │
│ - Lambda Function    │
│ - API Gateway        │
│ - IAM Role           │
│ - DynamoDB Tables    │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│   Lambda en Prod     │
│   Servicio Activo    │
└──────────────────────┘
```

## Permisos IAM - Matriz de Acceso

| Servicio | Acción | Recurso | Propósito |
|----------|--------|---------|-----------|
| **Bedrock** | `bedrock:InvokeModel` | `foundation-model/anthropic.claude-3-haiku-20240307-v1:0` | Invocar modelo Claude |
| **Bedrock** | `bedrock:ListFoundationModels` | `*` | Listar modelos disponibles |
| **DynamoDB** | `GetItem` | `arn:aws:dynamodb:*:*:table/*` | Lectura de registros |
| **DynamoDB** | `PutItem` | `arn:aws:dynamodb:*:*:table/*` | Insertar registros |
| **DynamoDB** | `UpdateItem` | `arn:aws:dynamodb:*:*:table/*` | Actualizar registros |
| **DynamoDB** | `Query` | `arn:aws:dynamodb:*:*:table/*` | Consultar datos |
| **DynamoDB** | `Scan` | `arn:aws:dynamodb:*:*:table/*` | Escanear tabla |
| **Logs** | `logs:*` | `*` | CloudWatch Logs |

## Endpoints Disponibles

```
POST /chat
  ├─ Body: {userMessage: string, context?: object}
  └─ Response: {responseMessage: string, metadata: object}

GET /health
  └─ Response: {status: UP, timestamp: datetime}

GET /models
  └─ Response: {availableModels: []}
```

## Configuración de Lambda

| Parámetro | Valor |
|-----------|-------|
| **Runtime** | Java 21 |
| **Memory** | 512 MB |
| **Timeout** | 30 segundos |
| **Handler** | `com.cattle.StreamLambdaHandler::handleRequest` |
| **Architecture** | x86_64 |
| **Layers** | - |

## Ambientes

- **Development**: Local (Maven + Spring Boot)
- **Production**: AWS Lambda + API Gateway

## Seguridad

- ✅ Encriptación en tránsito (HTTPS via API Gateway)
- ✅ Encriptación en reposo (DynamoDB)
- ✅ IAM Role con permisos específicos
- ✅ CORS configurado en Spring
- ✅ Lambda en VPC privada (opcional)

