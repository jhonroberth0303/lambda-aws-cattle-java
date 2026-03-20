# 🌱 PASTURES-HU#16: Backend: SNS/SQS Integration

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟢 BAJO (P3) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Event Publishing + Async Message Queue | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend architect**, quiero integrar SNS (Simple Notification Service) y SQS (Simple Queue Service) de AWS para comunicación asincrónica, de tal forma que:

1. Los eventos de cambio se publiquen en SNS
2. Los suscriptores reciban mensajes vía SQS
3. Las notificaciones se envíen de forma asincrónica (no bloqueante)
4. Los cambios se procesen en segundo plano
5. El sistema sea escalable y desacoplado
6. Se puedan reintentar mensajes fallidos
7. Se registren todos los eventos en Dead Letter Queue si fallan

Esto habilitará que el sistema escale a múltiples instancias, procese cambios en segundo plano, y envíe notificaciones sin bloquear operaciones principales.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Setup de SNS/SQS
```gherkin
Scenario: Configurar AWS SNS y SQS
  Given proyecto sin SNS/SQS
  When se agregan dependencias: aws-sdk, spring-cloud-aws
  And se configura AWS credentials (application.properties)
  Then:
    [ ] Dependencias instaladas
    [ ] AWS credentials configurados
    [ ] SNS topic creado: pasture-events
    [ ] SQS queue creada: pasture-events-queue
    [ ] Queue suscrita a topic
    [ ] DLQ creado: pasture-events-dlq
```

### AC#2: Publicar Evento en SNS
```gherkin
Scenario: Publicar evento cuando se abre potrero
  Given usuario realiza POST /open en potrero P001
  When se procesa exitosamente
  Then:
    [ ] Evento se publica en SNS topic
    [ ] Formato: JSON con operación y datos
    [ ] Incluye: timestamp, userId, pastureId, operation, beforeState, afterState
    [ ] NO bloquea la respuesta HTTP
    [ ] Se registra en logs
    [ ] MessageId de SNS se retorna (opcional)
```

### AC#3: Suscriptor Procesa Evento
```gherkin
Scenario: Listener procesa evento de SNS
  Given evento publicado en SNS
  When mensaje llega a SQS
  Then:
    [ ] EventListener recibe mensaje automáticamente
    [ ] Extrae datos del mensaje
    [ ] Procesa lógica (ej: actualizar cache, enviar email)
    [ ] Marca mensaje como procesado (delete de SQS)
    [ ] Sin errores
```

### AC#4: Manejo de Errores en Mensajes
```gherkin
Scenario: Reintentar mensaje fallido
  Given mensaje en SQS fallido
  When procesa con error (excepción)
  Then:
    [ ] Reintenta automáticamente (3 intentos)
    [ ] Espera entre reintentos (exponential backoff)
    [ ] Si sigue fallando después 3 intentos: envía a DLQ
    [ ] DLQ registra errores para análisis
    [ ] Alerta a equipo sobre mensajes en DLQ
```

### AC#5: Dead Letter Queue
```gherkin
Scenario: Mensajes fallidos en DLQ
  Given mensaje fallido después 3 reintentos
  When se envía a DLQ
  Then:
    [ ] Mensaje se guarda en DLQ
    [ ] Incluye: error, stack trace, original message
    [ ] Equipo recibe notificación
    [ ] DLQ es monitoreable desde AWS CloudWatch
    [ ] Pueden reprocessarse manualmente
```

### AC#6: Transaccionalidad
```gherkin
Scenario: Evento se publica solo si operación exitosa
  Given cambio en potrero
  When se realiza PUT /pastures/{id}
  Then:
    [ ] Si PUT exitoso: evento se publica en SNS
    [ ] Si PUT falla (error): evento NO se publica
    [ ] Si SNS falla: PUT sigue siendo exitoso (best effort)
    [ ] Transacción de BD y SNS desacopladas
```

### AC#7: Idempotencia
```gherkin
Scenario: No procesar el mismo evento dos veces
  Given evento procesado correctamente
  When el mismo mensaje se recibe de nuevo (accidente)
  Then:
    [ ] Listener detecta duplicado (por MessageId)
    [ ] No re-procesa
    [ ] Se registra el intento duplicado
    [ ] Mensaje se marca como procesado
```

### AC#8: Monitoreo y Logging
```gherkin
Scenario: Registrar eventos publicados y procesados
  Given eventos en SNS y SQS
  When se procesan
  Then:
    [ ] Cada publicación registrada en logs (DEBUG level)
    [ ] Cada procesamiento registrado (INFO level)
    [ ] Errores registrados (ERROR level)
    [ ] CloudWatch métricas disponibles:
        * Mensajes publicados
        * Mensajes procesados
        * Mensajes en DLQ
        * Latencia de procesamiento
```

### AC#9: Configuración de Reintentos
```gherkin
Scenario: Configurar política de reintentos
  Given application.properties
  When se especifica:
    [ ] maxRetries: 3
    [ ] retryBackoffMs: 1000 (inicial)
    [ ] retryBackoffMultiplier: 2
    [ ] maxBackoffMs: 30000
  Then:
    [ ] Reintentos siguen política
    [ ] 1er reintento: 1s
    [ ] 2do reintento: 2s
    [ ] 3er reintento: 4s
    [ ] Si falla: a DLQ
```

### AC#10: Múltiples Suscriptores
```gherkin
Scenario: Múltiples servicios suscritos a eventos
  Given evento en SNS (ej: PASTURE_OPENED)
  When se publica
  Then puede haber múltiples suscriptores:
    [ ] Suscriptor 1: Actualizar cache
    [ ] Suscriptor 2: Enviar notificación al usuario
    [ ] Suscriptor 3: Registrar en analytics
    [ ] Suscriptor 4: Actualizar UI en tiempo real (WebSocket)
    [ ] Todos reciben en paralelo (async)
```

### AC#11: Performance
```gherkin
Scenario: Sistema no bloquea en publicación SNS
  Given usuario realiza operación
  When se publica evento en SNS
  Then:
    [ ] Operación retorna antes que SNS complete
    [ ] SNS publish es async y no-bloqueante
    [ ] Latencia HTTP < 200ms (sin esperar SNS)
    [ ] SNS procesamiento en background
```

### AC#12: Testing
```gherkin
Scenario: Tests para SNS/SQS
  Given código con SNS/SQS
  When se escriben tests
  Then:
    [ ] Tests unitarios con mock de SNS/SQS
    [ ] Tests de integración con LocalStack o TestContainers
    [ ] Tests de error handling (reintentosn DLQ)
    [ ] Cobertura >= 80%
```

### AC#13: Configuración por Ambiente
```gherkin
Scenario: SNS/SQS configurado diferente según ambiente
  Given desarrollo, staging, producción
  When se configura
  Then:
    [ ] LOCAL (dev): usar LocalStack/Mock
    [ ] STAGING: usar SQS/SNS staging
    [ ] PRODUCTION: usar SQS/SNS production
    [ ] Configuración por environment profiles
```

### AC#14: Visualización de Métricas
```gherkin
Scenario: Monitorear SNS/SQS en CloudWatch
  Given eventos siendo procesados
  When usuario revisa CloudWatch
  Then:
    [ ] Dashboard muestra:
        * Mensajes publicados/hora
        * Mensajes procesados/hora
        * Mensajes en DLQ
        * Latencia promedio
        * Tasa de error
    [ ] Alertas configuradas para anomalías
```

### AC#15: Documentación
```gherkin
Scenario: Documentar arquitectura SNS/SQS
  Given integración implementada
  Then:
    [ ] Diagrama de arquitectura (SNS → SQS → Listener)
    [ ] Lista de topics y queues
    [ ] Estructura de mensajes documentada
    [ ] Políticas de reintento documentadas
    [ ] Runbook para troubleshooting
    [ ] Swagger UI muestra eventos publicables
```

---

## 📊 **Especificación Técnica**

### Arquitectura

```
┌─────────────────┐
│  HTTP Request   │
│  (PUT /open)    │
└────────┬────────┘
         │
         ↓
┌─────────────────────────────────────┐
│  PastureController / Service        │
│  - Validar y guardar en BD          │
│  - Publicar evento SNS (async)      │
│  - Retornar response HTTP           │
└────────┬────────────────────────────┘
         │ (NO BLOQUEANTE)
         ↓
    ┌─────────────────┐
    │  AWS SNS Topic  │
    │  pasture-events │
    └────────┬────────┘
             │
        ┌────┴────────────────────────────┐
        ↓                                  ↓
   ┌──────────────┐            ┌──────────────────┐
   │  SQS Queue 1 │            │  SQS Queue 2     │
   │  (notify)    │            │  (analytics)     │
   └──────┬───────┘            └────────┬─────────┘
          │                             │
          ↓                             ↓
   ┌──────────────────┐       ┌──────────────────┐
   │ NotificationSub  │       │ AnalyticsSub     │
   │ - Enviar email   │       │ - Registrar datos│
   │ - WebSocket push │       │ - Actualizar BI  │
   └──────────────────┘       └──────────────────┘
```

### Instalación de Dependencias

#### pom.xml
```xml
<!-- AWS SDK for SNS/SQS -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sns</artifactId>
    <version>2.24.0</version>
</dependency>

<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sqs</artifactId>
    <version>2.24.0</version>
</dependency>

<!-- Spring Cloud AWS -->
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-messaging</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- For testing -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>localstack</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
```

### Configuración

#### application.properties
```properties
# AWS SNS/SQS Configuration
aws.sns.topic-name=pasture-events
aws.sqs.queue-name=pasture-events-queue
aws.sqs.dlq-name=pasture-events-dlq
aws.region=us-east-1

# Retry Configuration
aws.sqs.max-retries=3
aws.sqs.retry-backoff-ms=1000
aws.sqs.retry-backoff-multiplier=2
aws.sqs.max-backoff-ms=30000

# LocalStack (Development)
spring.cloud.aws.endpoint=http://localhost:4566
spring.cloud.aws.region.static=us-east-1
spring.cloud.aws.credentials.access-key=test
spring.cloud.aws.credentials.secret-key=test
```

### Service para Publicar Eventos

#### EventPublisher.java
```java
@Service
@Slf4j
public class EventPublisher {
    
    private final SnsClient snsClient;
    private final String topicArn;
    
    @Autowired
    public EventPublisher(SnsClient snsClient, 
                         @Value("${aws.sns.topic.arn}") String topicArn) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
    }
    
    public void publishPastureEvent(PastureEvent event) {
        try {
            String message = ObjectMapper.writeValueAsString(event);
            
            PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .messageAttributes(Map.of(
                    "EventType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(event.getOperation().toString())
                        .build(),
                    "PastureId", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(event.getEntityId())
                        .build()
                ))
                .build();
            
            PublishResponse response = snsClient.publish(request);
            
            log.info("Evento publicado exitosamente: {} - MessageId: {}", 
                event.getOperation(), response.messageId());
            
        } catch (Exception e) {
            log.error("Error publicando evento SNS", e);
            // No bloquear la operación principal
            // El error será registrado para monitoreo
        }
    }
}
```

### Listener para Procesar Eventos

#### PastureEventListener.java
```java
@Service
@Slf4j
public class PastureEventListener {
    
    private final NotificationService notificationService;
    private final AnalyticsService analyticsService;
    private final EventIdempotencyService idempotencyService;
    
    @SqsListener("${aws.sqs.queue-name}")
    public void handlePastureEvent(
        String message,
        @Header(name = "MessageId") String messageId,
        @Header(name = "ApproximateRetryCount", required = false) Integer retryCount
    ) {
        try {
            // Idempotencia: verificar si ya fue procesado
            if (idempotencyService.isProcessed(messageId)) {
                log.info("Evento ya procesado: {}", messageId);
                return;
            }
            
            PastureEvent event = ObjectMapper.readValue(message, PastureEvent.class);
            
            // Procesar evento
            processEvent(event);
            
            // Marcar como procesado
            idempotencyService.markAsProcessed(messageId);
            
            log.info("Evento procesado exitosamente: {} - ID: {}", 
                event.getOperation(), messageId);
            
        } catch (JsonProcessingException e) {
            log.error("Error parseando evento: {}", message, e);
            // Si la excepción ocurre, SQS reintentar automáticamente
            throw new RuntimeException("Error procesando evento", e);
        } catch (Exception e) {
            log.error("Error procesando evento", e);
            // Reintentará automáticamente
            throw new RuntimeException("Error procesando evento", e);
        }
    }
    
    private void processEvent(PastureEvent event) {
        // Ejecutar múltiples acciones (pueden ser async internamente)
        notificationService.sendNotificationForEvent(event);
        analyticsService.recordEvent(event);
    }
}
```

### Configuración de SNS y SQS

#### AwsConfiguration.java
```java
@Configuration
public class AwsConfiguration {
    
    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
            .region(Region.US_EAST_1)
            .build();
    }
    
    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
            .region(Region.US_EAST_1)
            .build();
    }
    
    @Bean
    public QueueNameToUrlCache queueNameToUrlCache(SqsClient sqsClient) {
        return new QueueNameToUrlCache();
    }
    
    // Crear topic y queue si no existen
    @Bean
    public void initializeSnsAndSqs(SnsClient snsClient, SqsClient sqsClient) {
        try {
            // Crear topic
            CreateTopicRequest topicRequest = CreateTopicRequest.builder()
                .name("pasture-events")
                .build();
            CreateTopicResponse topicResponse = snsClient.createTopic(topicRequest);
            String topicArn = topicResponse.topicArn();
            
            // Crear queue
            CreateQueueRequest queueRequest = CreateQueueRequest.builder()
                .queueName("pasture-events-queue")
                .attributes(Map.of(
                    QueueAttributeName.MESSAGE_RETENTION_PERIOD, "86400" // 24 horas
                ))
                .build();
            CreateQueueResponse queueResponse = sqsClient.createQueue(queueRequest);
            String queueUrl = queueResponse.queueUrl();
            
            // Crear DLQ
            CreateQueueRequest dlqRequest = CreateQueueRequest.builder()
                .queueName("pasture-events-dlq")
                .attributes(Map.of(
                    QueueAttributeName.MESSAGE_RETENTION_PERIOD, "1209600" // 14 días
                ))
                .build();
            CreateQueueResponse dlqResponse = sqsClient.createQueue(dlqRequest);
            
            // Suscribir queue a topic
            SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("sqs")
                .endpoint(queueUrl)
                .build();
            snsClient.subscribe(subscribeRequest);
            
        } catch (TopicAlreadyExistsException | QueueAlreadyExistsException e) {
            // Ya existen
        }
    }
}
```

### Testing

#### EventPublisherTest.java
```java
@SpringBootTest
class EventPublisherTest {
    
    @MockBean
    private SnsClient snsClient;
    
    @Autowired
    private EventPublisher eventPublisher;
    
    @Test
    void shouldPublishPastureEvent() {
        // Arrange
        PastureEvent event = createTestEvent();
        
        // Mock SNS response
        when(snsClient.publish(any(PublishRequest.class)))
            .thenReturn(PublishResponse.builder()
                .messageId("test-message-id")
                .build());
        
        // Act
        eventPublisher.publishPastureEvent(event);
        
        // Assert
        verify(snsClient).publish(any(PublishRequest.class));
    }
    
    @Test
    void shouldHandlePublishError() {
        // Arrange
        PastureEvent event = createTestEvent();
        
        // Mock SNS error
        when(snsClient.publish(any(PublishRequest.class)))
            .thenThrow(new RuntimeException("SNS error"));
        
        // Act & Assert - should not throw (error logged)
        assertDoesNotThrow(() -> eventPublisher.publishPastureEvent(event));
    }
}
```

#### PastureEventListenerTest.java (Integration)
```java
@SpringBootTest
@TestContainers
class PastureEventListenerTest {
    
    @Container
    static LocalStackContainer localstack = new LocalStackContainer()
        .withServices(SNS, SQS);
    
    @Autowired
    private PastureEventListener listener;
    
    @Test
    void shouldProcessEventFromSQS() throws Exception {
        // Arrange
        PastureEvent event = createTestEvent();
        String message = ObjectMapper.writeValueAsString(event);
        
        // Act
        listener.handlePastureEvent(message, "test-message-id", 0);
        
        // Assert
        // Verificar que se procesó (mocks, BD, etc)
    }
    
    @Test
    void shouldHandleProcessingError() {
        // Arrange
        String invalidMessage = "invalid json";
        
        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> listener.handlePastureEvent(invalidMessage, "test-id", 0));
    }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`AwsConfiguration.java`** - Configuración AWS
2. **`EventPublisher.java`** - Publicador SNS
3. **`PastureEventListener.java`** - Listener SQS
4. **`EventIdempotencyService.java`** - Idempotencia
5. **`EventPublisherTest.java`** - Tests
6. **`PastureEventListenerTest.java`** - Tests integración

### Archivos a Modificar

1. **`pom.xml`** - Agregar dependencias AWS
2. **`application.properties`** - Configuración SNS/SQS
3. **`PastureService.java`** - Inyectar EventPublisher
4. **Controllers** - Asegurar que publiquen eventos

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Agregar Dependencias
- Editar pom.xml
- AWS SDK, Spring Cloud AWS

### Paso 2: Configurar AWS
- AwsConfiguration.java
- Crear topic SNS y queue SQS

### Paso 3: Implementar Publicador
- EventPublisher.java
- Publicar en SNS (async)

### Paso 4: Implementar Listener
- PastureEventListener.java
- Procesar eventos desde SQS

### Paso 5: Idempotencia y Errores
- EventIdempotencyService
- Manejo de reintentos y DLQ

### Paso 6: Testing
- Tests unitarios
- Tests de integración (LocalStack)

---

## 🔧 **Refinamiento Técnico**

### AWS SNS/SQS Setup

```java
@Configuration
public class AwsMessagingConfig {
  
  @Bean
  public SnsClient snsClient() {
    return SnsClient.builder()
      .region(Region.US_EAST_1)
      .build();
  }
  
  @Bean
  public SqsClient sqsClient() {
    return SqsClient.builder()
      .region(Region.US_EAST_1)
      .build();
  }
}
```

### Event Publisher - SNS

```java
@Service
public class EventPublisher {
  private final SnsClient snsClient;
  
  public void publishEvent(PastureEvent event) {
    String message = objectMapper.writeValueAsString(event);
    
    PublishRequest request = PublishRequest.builder()
      .topicArn("arn:aws:sns:us-east-1:123456789:pasture-events")
      .message(message)
      .build();
    
    snsClient.publish(request);
  }
}
```

### Event Listener - SQS

```java
@Component
public class PastureEventListener {
  
  @SqsListener("pasture-events-queue")
  public void handleEvent(PastureEvent event) {
    // Procesar evento
    pastureService.handleEvent(event);
  }
}
```

### DLQ Configuration

```
Main Queue: pasture-events-queue
DLQ: pasture-events-dlq
Max Receive Count: 3 reintentos
Visibility Timeout: 30 segundos
```

### Testing Strategy

**LocalStack (Docker):**
```bash
docker run --rm -p 4566:4566 localstack/localstack
```

**Integration Tests:**
- Publicar evento a SNS
- Verificar llega a SQS
- Listener procesa correctamente

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] Dependencias AWS instaladas
- [ ] SNS topic creado y configurado
- [ ] SQS queue creado y configurado
- [ ] DLQ creado y configurado
- [ ] EventPublisher.java funciona
- [ ] PastureEventListener.java funciona
- [ ] Eventos se publican al cambiar potreros
- [ ] Eventos se procesan desde SQS
- [ ] Idempotencia funciona
- [ ] Reintentos funcionan
- [ ] DLQ funciona
- [ ] CloudWatch métricas disponibles
- [ ] Tests unitarios: >= 80%
- [ ] Tests integración con LocalStack
- [ ] Documentación de arquitectura
- [ ] Configuración por ambiente
- [ ] Code review aprobado
- [ ] CI/CD green

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Event Publishing Pattern + Async Message Queue + Dead Letter Queue

**Justificación:** **Event Publishing**: SNS publica eventos de cambios potreros. **Async Processing**: SQS procesa en background sin bloquear. **Dead Letter Queue**: Captura fallos para análisis y recuperación. **Decoupling**: Productores y consumidores desacoplados. **Scalability**: Múltiples instancias sin impacto. **Resilience**: Reintentos automáticos + manejo fallos.

**Componentes Afectados:**

- **EventPublisher.java (Nuevo):** Publica eventos en SNS. Método: `publishEvent(event) → void`. Ejecutado asincronamente (no bloquea). Logea MessageId.

- **EventListener.java (Nuevo):** Consume eventos de SQS. Listener automático. Procesa `PastureEvent`, valida, actualiza cache/notifica. Idempotencia por MessageId.

- **SnsConfiguration.java (Nuevo):** Setup SNS. Bean: `AmazonSNS`. Topics: `pasture-events`. Propiedades configurables.

- **SqsConfiguration.java (Nuevo):** Setup SQS. Bean: `AmazonSQS`. Queue: `pasture-events-queue`. DLQ: `pasture-events-dlq`. Reintentos: 3, exponential backoff.

- **EventPublishingAspect.java (Nuevo):** AOP @Aspect. Intercepta métodos @PublishEvent. Publica automáticamente después de éxito. Sin cambios en código negocio.

- **DeadLetterQueueHandler.java (Nuevo):** Procesa mensajes fallidos. Registra error, notifica team. Manual retry disponible.

**Hitos:**
1. SnsConfiguration.java + SqsConfiguration.java (AWS setup)
2. EventPublisher.java + EventListener.java (publish/consume)
3. EventPublishingAspect.java (transparencia)
4. DeadLetterQueueHandler.java (error handling)
5. Tests + monitoring

### Validación de Impacto

✅ **Event Publishing**: Desacoplado, escalable
✅ **Async Processing**: No bloquea operaciones principales
✅ **Dead Letter Queue**: Captura fallos para análisis
✅ **Resilience**: Reintentos + backoff exponencial
✅ **Idempotencia**: MessageId previene duplicados
✅ **Monitoring**: CloudWatch métricas

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-020: DELETE publica evento
- → PASTURES-HU-018: SNS/SQS (esta - async messaging)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Async messaging + resilience (P3 infrastructure)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
