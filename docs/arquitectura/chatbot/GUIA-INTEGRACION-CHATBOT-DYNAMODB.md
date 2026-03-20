# 🤖 Guía de Integración: Chatbot Bedrock ↔ DynamoDB

**Documento de Implementación para Conectar el Chatbot IA con Datos Ganaderos en DynamoDB**

---

## 📋 Resumen Ejecutivo

Este documento es un **roadmap de implementación técnica** para conectar `cattle-bedrock` (chatbot con Bedrock) con `cattle-lambda-function` (datos en DynamoDB). Una vez completada esta integración, el chatbot podrá responder preguntas inteligentes sobre:

- 📊 Edades de bovinos y distribución demográfica
- 🐄 Conteos por categoría, género y estado reproductivo  
- 🤰 Información sobre preñez y crías para destete
- 🌱 Estado de potreros y disponibilidad de pastoreo
- 🥛 Estadísticas de producción láctea

---

## 🎯 Flujo Objetivo (Después de Integración)

```
┌─────────────────────────────┐
│   Usuario del Chatbot       │
│   "¿Cuántas vacas preñadas  │
│    tengo en este momento?"  │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────────────────────┐
│ Lambda: cattle-bedrock                      │
│ ChatbotController → ChatbotService          │
└─────────────┬───────────────────────────────┘
              │ NEW: Consultar datos
              ▼
┌─────────────────────────────────────────────┐
│ Lambda: cattle-lambda-function              │
│ BovinesService → BovineRepository           │
└─────────────┬───────────────────────────────┘
              │ Enriquecimiento de contexto
              ▼
┌─────────────────────────────────────────────┐
│ DynamoDB: TABLE_CATTLE                      │
│ Query: status="PREGNANT" AND gender="female"│
│ Result: 45 bovinos                          │
└─────────────┬───────────────────────────────┘
              │
              ▼
┌──────────────────────────────────────────────────────┐
│ PromptBuilder construye contexto:                    │
│ "Total bovinos: 150                                  │
│  Vacas preñadas: 45 (30%)                            │
│  Próximas a parto (2 semanas): 12                    │
│  Razas principales: Holstein (18), Jersey (15)       │
│  ..."                                                │
└──────────────┬───────────────────────────────────────┘
               │
               ▼
┌────────────────────────────────────────────┐
│ Amazon Bedrock: Claude 3 Haiku             │
│ Prompt: contexto + pregunta del usuario    │
└──────────────┬─────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────┐
│ Respuesta Inteligente:                               │
│ "Tienes 45 vacas preñadas en este momento,           │
│  representando el 30% de tu rebaño. De estas,        │
│  12 están próximas al parto (últimas 2 semanas).     │
│                                                       │
│  Recomendaciones:                                    │
│  - Separar las que están en últimas 2 semanas       │
│    para monitored cercano                           │
│  - Asegurar nutrición adecuada en esta fase          │
│  - Preparar instalaciones de maternidad              │
│  ..."                                                │
└──────────────────────────────────────────────────────┘
```

---

## 🔧 Plan de Implementación (Paso a Paso)

### FASE 1: Preparación (1-2 días)

#### 1.1 Agregar Dependencia DynamoDB a cattle-bedrock

```xml
<!-- En cattle-bedrock/pom.xml, agregar si no existe: -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>dynamodb</artifactId>
    <version>2.32.16</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>dynamodb-enhanced</artifactId>
    <version>2.32.16</version>
</dependency>
```

#### 1.2 Crear Servicios de Consulta en cattle-bedrock

Agregar nuevas clases en `cattle-bedrock/src/main/java/com/cattle/services/`:

**A. BovineQueryService.java** - Servicio para consultar bovinos desde Bedrock
```java
@Service
public class BovineQueryService {
    
    private final DynamoDbEnhancedClient dynamoDbClient;
    
    /**
     * Obtener todos los bovinos de una finca
     */
    public List<BovineContextDTO> getAllBovinesByFarm(String farmId) {
        // Query TABLE_CATTLE con GSI1 donde farmId = input
        // Retornar lista simplificada para contexto
    }
    
    /**
     * Contar bovinos preñados
     */
    public Long countPregnantBovines(String farmId) {
        // Query donde status="PREGNANT"
    }
    
    /**
     * Contar bovinos por categoría
     */
    public Map<String, Long> countByCategory(String farmId) {
        // Retorna {cow: 45, heifer: 23, calf: 12, ...}
    }
    
    /**
     * Contar por género
     */
    public Map<String, Long> countByGender(String farmId) {
        // Retorna {female: 100, male: 50}
    }
    
    /**
     * Obtener distribución de edades
     */
    public Map<String, Object> getAgeDistribution(String farmId) {
        // Calcula edades desde bornDate
        // Retorna grupos: crías, terneros, jóvenes, adultos
    }
    
    /**
     * Crías listas para destete
     */
    public List<BovineContextDTO> getCalvesReadyForWeaning(String farmId) {
        // Query donde category="calf" AND age > 6 meses
    }
}
```

**B. MilkingQueryService.java** - Servicio para consultar lactancia
```java
@Service
public class MilkingQueryService {
    
    /**
     * Producción promedio del mes
     */
    public Double getMonthlyAverageProduction(String farmId) {
        // Query TABLE_FARM_MILKING últimos 30 días
        // Calcular promedio de liters
    }
    
    /**
     * Bovino de mayor producción
     */
    public BovineProductionDTO getTopProducerBovine(String farmId) {
        // Query últimos 7 días
        // Retornar bovino con máxima producción
    }
}
```

**C. PastureQueryService.java** - Servicio para consultar potreros
```java
@Service
public class PastureQueryService {
    
    /**
     * Potreros disponibles
     */
    public List<PastureContextDTO> getAvailablePastures(String farmId) {
        // Query donde status="DISPONIBLE"
    }
    
    /**
     * Total de hectáreas en uso
     */
    public Double getTotalHectaresInUse(String farmId) {
        // Sum areaHa donde status="EN_USO"
    }
}
```

---

### FASE 2: Construcción de Contexto (2-3 días)

#### 2.1 Crear ContextBuilder en cattle-bedrock

**Archivo: `cattle-bedrock/src/main/java/com/cattle/services/PromptContextBuilder.java`**

```java
@Service
public class PromptContextBuilder {
    
    @Autowired
    private BovineQueryService bovineQueryService;
    
    @Autowired
    private MilkingQueryService milkingQueryService;
    
    @Autowired
    private PastureQueryService pastureQueryService;
    
    /**
     * Construir contexto completo basado en la pregunta del usuario
     * 
     * Ejemplo entrada: "¿Cuántas vacas preñadas tengo?"
     * 
     * Ejemplo salida: "
     * Tu finca (F001) tiene 150 bovinos en total.
     * De estos:
     * - 45 están preñados (30% del rebaño)
     * - 12 están próximos a parto (menos de 2 semanas)
     * - Las razas principales son Holstein (18) y Jersey (15)
     * - Edades: 23 crías, 45 terneros, 50 jóvenes, 32 adultos
     * - Género: 100 hembras, 50 machos
     * ...
     * "
     */
    public String buildContextFromUserQuery(String userQuery, String farmId) {
        
        // 1. Analizar pregunta para identificar tema
        QueryTopic topic = identifyQueryTopic(userQuery);
        
        // 2. Recopilar datos relevantes según el tema
        StringBuilder context = new StringBuilder();
        context.append(buildGeneralStats(farmId));
        
        if (topic.isAboutPregnancy()) {
            context.append(buildPregnancyContext(farmId));
        }
        
        if (topic.isAboutAges()) {
            context.append(buildAgeContext(farmId));
        }
        
        if (topic.isAboutCategories()) {
            context.append(buildCategoryContext(farmId));
        }
        
        if (topic.isAboutMilking()) {
            context.append(buildMilkingContext(farmId));
        }
        
        if (topic.isAboutPastures()) {
            context.append(buildPastureContext(farmId));
        }
        
        return context.toString();
    }
    
    private String buildGeneralStats(String farmId) {
        long totalBovines = bovineQueryService.countAllBovines(farmId);
        Map<String, Long> byGender = bovineQueryService.countByGender(farmId);
        
        return String.format(
            "Tu finca (%s) tiene %d bovinos en total: " +
            "%d hembras y %d machos.\n",
            farmId, totalBovines, byGender.get("female"), byGender.get("male")
        );
    }
    
    private String buildPregnancyContext(String farmId) {
        long pregnantCount = bovineQueryService.countPregnantBovines(farmId);
        long totalBovines = bovineQueryService.countAllBovines(farmId);
        double percentage = (pregnantCount * 100.0) / totalBovines;
        
        long nearExpectedDate = bovineQueryService.countNearExpectedCalvingDate(farmId, 14);
        
        return String.format(
            "Preñez:\n" +
            "- Bovinos preñados: %d (%.1f%% del rebaño)\n" +
            "- Próximos a parto (2 semanas): %d\n",
            pregnantCount, percentage, nearExpectedDate
        );
    }
    
    private String buildAgeContext(String farmId) {
        Map<String, Object> ageDistribution = bovineQueryService.getAgeDistribution(farmId);
        
        return String.format(
            "Distribución de edades:\n" +
            "- Crías (0-6 meses): %s\n" +
            "- Terneros (6-18 meses): %s\n" +
            "- Jóvenes (1.5-3 años): %s\n" +
            "- Adultos (3+ años): %s\n",
            ageDistribution.get("calves"),
            ageDistribution.get("weanlings"),
            ageDistribution.get("young"),
            ageDistribution.get("adults")
        );
    }
    
    private String buildCategoryContext(String farmId) {
        Map<String, Long> byCategory = bovineQueryService.countByCategory(farmId);
        
        return String.format(
            "Composición del rebaño:\n" +
            "- Vacas: %d\n" +
            "- Novillos/Novillas: %d\n" +
            "- Toros: %d\n" +
            "- Castrados: %d\n" +
            "- Crías: %d\n",
            byCategory.get("cow"),
            byCategory.get("heifer"),
            byCategory.get("bull"),
            byCategory.get("steer"),
            byCategory.get("calf")
        );
    }
    
    private String buildMilkingContext(String farmId) {
        Double monthlyAvg = milkingQueryService.getMonthlyAverageProduction(farmId);
        BovineProductionDTO topProducer = milkingQueryService.getTopProducerBovine(farmId);
        
        return String.format(
            "Producción láctea:\n" +
            "- Promedio mensual: %.1f litros/bovino\n" +
            "- Mayor productor: %s (%s) con %.1f litros\n",
            monthlyAvg,
            topProducer.getName(),
            topProducer.getId(),
            topProducer.getProductionLastWeek()
        );
    }
    
    private String buildPastureContext(String farmId) {
        List<PastureContextDTO> available = pastureQueryService.getAvailablePastures(farmId);
        Double hectaresInUse = pastureQueryService.getTotalHectaresInUse(farmId);
        
        return String.format(
            "Potreros:\n" +
            "- Disponibles para pastoreo: %d\n" +
            "- Hectáreas en uso: %.1f\n",
            available.size(),
            hectaresInUse
        );
    }
    
    private QueryTopic identifyQueryTopic(String userQuery) {
        QueryTopic topic = new QueryTopic();
        String query = userQuery.toLowerCase();
        
        topic.setAboutPregnancy(
            query.contains("preñado") || query.contains("gestación") ||
            query.contains("parto") || query.contains("embaraza")
        );
        
        topic.setAboutAges(
            query.contains("edad") || query.contains("años") ||
            query.contains("meses")
        );
        
        topic.setAboutCategories(
            query.contains("vaca") || query.contains("novill") ||
            query.contains("toro") || query.contains("cría") ||
            query.contains("ternero")
        );
        
        topic.setAboutMilking(
            query.contains("producción") || query.contains("leche") ||
            query.contains("lactancia") || query.contains("ordeño")
        );
        
        topic.setAboutPastures(
            query.contains("potrer") || query.contains("pasto") ||
            query.contains("rotación") || query.contains("disponible")
        );
        
        return topic;
    }
}
```

---

### FASE 3: Integración con ChatbotService (2-3 días)

#### 3.1 Modificar ChatbotService.java

```java
@Service
public class ChatbotService {
    
    @Autowired
    private PromptContextBuilder contextBuilder;
    
    @Autowired
    private BedrockClient bedrockClient;
    
    /**
     * Procesar mensaje del usuario
     * 
     * FLUJO NUEVO:
     * 1. Recibir pregunta del usuario
     * 2. Construir contexto enriquecido desde DynamoDB
     * 3. Armar prompt completo (contexto + pregunta)
     * 4. Invocar Bedrock
     * 5. Retornar respuesta
     */
    public ChatResponse processMessage(String userMessage, String farmId, String userId) {
        
        try {
            // PASO 1: Extraer contexto de DynamoDB
            String enrichedContext = contextBuilder.buildContextFromUserQuery(
                userMessage, 
                farmId
            );
            
            // PASO 2: Construir prompt completo
            String fullPrompt = buildCompletionPrompt(enrichedContext, userMessage);
            
            // PASO 3: Invocar Bedrock
            String bedrockResponse = invokeBedrockModel(fullPrompt);
            
            // PASO 4: Construir respuesta
            ChatResponse response = new ChatResponse();
            response.setResponseMessage(bedrockResponse);
            response.setTimestamp(Instant.now());
            response.setModelUsed("Claude 3 Haiku");
            response.setTokensUsed(estimateTokenCount(fullPrompt, bedrockResponse));
            
            // LOGGING
            logger.info("Chatbot processed query from user {} in farm {}", userId, farmId);
            
            return response;
            
        } catch (DynamoDbException e) {
            logger.error("Error querying DynamoDB: {}", e.getMessage());
            return createErrorResponse("No pudimos acceder a los datos de tu finca. Intenta de nuevo.");
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage());
            return createErrorResponse("Disculpa, tuvimos un problema procesando tu consulta.");
        }
    }
    
    private String buildCompletionPrompt(String context, String userQuery) {
        return String.format(
            "Eres un asistente de gestión ganadera especializado en granjas de bovinos de pastoreo.\n" +
            "\n" +
            "CONTEXTO DE LA FINCA:\n" +
            "%s\n" +
            "\n" +
            "CONSULTA DEL USUARIO:\n" +
            "%s\n" +
            "\n" +
            "INSTRUCCIONES:\n" +
            "1. Responde basándote en los datos proporcionados\n" +
            "2. Si hay recomendaciones, hazlas específicas y prácticas\n" +
            "3. Usa lenguaje claro y profesional\n" +
            "4. Incluye números y porcentajes cuando sea relevante\n" +
            "5. Si falta información, indícalo claramente\n" +
            "\n" +
            "RESPUESTA:",
            context, userQuery
        );
    }
    
    private String invokeBedrockModel(String prompt) {
        // Usar AWS SDK para Bedrock
        InvokeModelRequest request = InvokeModelRequest.builder()
            .modelId("anthropic.claude-3-haiku-20240307-v1:0")
            .body(SdkBytes.fromString(new Gson().toJson(Map.of(
                "prompt", prompt,
                "max_tokens", 500,
                "temperature", 0.7,
                "top_p", 0.9
            ))))
            .contentType("application/json")
            .build();
        
        InvokeModelResponse response = bedrockClient.invokeModel(request);
        String responseBody = response.body().asUtf8String();
        
        // Parsear respuesta JSON de Bedrock
        // ... extracting text from response
        
        return extractTextFromBedrockResponse(responseBody);
    }
    
    private String extractTextFromBedrockResponse(String responseJson) {
        // Parse JSON response from Bedrock
        // Retornar el texto generado
    }
}
```

---

### FASE 4: Testing (2-3 días)

#### 4.1 Tests Unitarios

```java
@SpringBootTest
public class ChatbotIntegrationTests {
    
    @Autowired
    private ChatbotService chatbotService;
    
    @MockBean
    private BovineQueryService bovineQueryService;
    
    @Test
    public void testPregnancyQuery() {
        // Arrange
        String query = "¿Cuántas vacas preñadas tengo?";
        String farmId = "F001";
        
        Mockito.when(bovineQueryService.countPregnantBovines(farmId))
            .thenReturn(45L);
        Mockito.when(bovineQueryService.countAllBovines(farmId))
            .thenReturn(150L);
        
        // Act
        ChatResponse response = chatbotService.processMessage(query, farmId, "user1");
        
        // Assert
        assertThat(response.getResponseMessage()).contains("45");
        assertThat(response.getResponseMessage()).contains("30%");
    }
    
    @Test
    public void testAgeDistributionQuery() {
        String query = "¿Cuáles son las edades de mis bovinos?";
        String farmId = "F001";
        
        // ... similar structure
    }
    
    @Test
    public void testDynamoDBFailureHandling() {
        // Testear error handling cuando DynamoDB falla
    }
}
```

#### 4.2 Tests de Integración

```bash
# Invocar función local con evento de test
sam local invoke ChatbotLambdaFunction -e events/test-pregnancy-query.json

# Evento de test: events/test-pregnancy-query.json
{
  "body": "{\"userMessage\": \"¿Cuántas vacas preñadas tengo?\", \"farmId\": \"F001\"}",
  "requestContext": {
    "authorizer": {
      "claims": {
        "sub": "user123"
      }
    }
  }
}
```

---

## 🗂️ Nueva Estructura de Archivos

```
cattle-bedrock/
├── src/main/java/com/cattle/
│   ├── services/
│   │   ├── ChatbotService.java          (MODIFICADO - agregar contexto)
│   │   ├── BovineQueryService.java      (NUEVO)
│   │   ├── MilkingQueryService.java     (NUEVO)
│   │   ├── PastureQueryService.java     (NUEVO)
│   │   └── PromptContextBuilder.java    (NUEVO)
│   ├── dtos/
│   │   ├── BovineContextDTO.java        (NUEVO)
│   │   ├── PastureContextDTO.java       (NUEVO)
│   │   ├── BovineProductionDTO.java     (NUEVO)
│   │   └── QueryTopic.java              (NUEVO)
│   └── config/
│       └── DynamoDbConfig.java          (NUEVO)
│
└── events/
    ├── test-pregnancy-query.json        (NUEVO)
    ├── test-age-query.json              (NUEVO)
    └── test-categories-query.json       (NUEVO)
```

---

## 📊 Ejemplo: Flujo Completo de Una Consulta

### Consulta: "¿Cuántos terneros tengo listos para destete?"

**1. Usuario envia**:
```json
{
  "userMessage": "¿Cuántos terneros tengo listos para destete?",
  "farmId": "F001",
  "userId": "user@farm.com"
}
```

**2. ChatbotService.processMessage() es invocado**:
```
→ Identificar tema: CATEGORÍAS + EDADES
→ Llamar BovineQueryService.getCalvesReadyForWeaning("F001")
→ Resultado: 23 terneros con edad 8-10 meses
```

**3. PromptContextBuilder construye contexto**:
```
Tu finca (F001) tiene 150 bovinos en total: 100 hembras y 50 machos.

Distribución de edades:
- Crías (0-6 meses): 15
- Terneros (6-18 meses): 45  ← RELEVANTE
- Jóvenes (1.5-3 años): 52
- Adultos (3+ años): 38

Composición del rebaño:
- Vacas: 38
- Novillos/Novillas: 45
- Toros: 8
- Castrados: 4
- Crías: 15

Terneros listos para destete (edad 8-10 meses): 23
```

**4. Prompt enviado a Bedrock**:
```
Eres un asistente de gestión ganadera especializado en granjas de bovinos de pastoreo.

CONTEXTO DE LA FINCA:
Tu finca (F001) tiene 150 bovinos en total...
[contexto completo]

CONSULTA DEL USUARIO:
¿Cuántos terneros tengo listos para destete?

INSTRUCCIONES:
1. Responde basándote en los datos proporcionados
2. Si hay recomendaciones, hazlas específicas y prácticas
3. ...

RESPUESTA:
```

**5. Claude 3 Haiku genera respuesta**:
```
Tienes 23 terneros listos o próximos a destete con edades entre 8-10 meses.

Recomendaciones prácticas:
1. SEPARACIÓN: Realiza el destete en 2-3 lotes para minimizar estrés
   - Lote 1 (9-10 meses): 10 terneros - mayor capacidad de adaptación
   - Lote 2 (8-9 meses): 13 terneros - en 2-3 semanas

2. NUTRICIÓN POST-DESTETE:
   - Ofrece concentrado de buena calidad (14-16% proteína)
   - Heno de calidad ad libitum
   - Agua fresca permanentemente

3. MANEJO:
   - Manténlos en potreros cercanos para monitoreo
   - Espaciamiento mínimo: 1 metro cuadrado por ternero
   - Evita cambios bruscos de dieta

4. SANIDAD:
   - Revisa estado sanitario antes del destete
   - Aplica desparasitantes si es necesario
   - Monitorea signos de diarrea los primeros 10 días
```

**6. Respuesta retorna al usuario**:
```json
{
  "responseMessage": "Tienes 23 terneros listos...",
  "timestamp": "2025-12-16T14:35:22Z",
  "modelUsed": "Claude 3 Haiku",
  "tokensUsed": 1247,
  "success": true
}
```

---

## 🚀 Checklist de Implementación

- [ ] Agregar dependencias DynamoDB a pom.xml
- [ ] Crear BovineQueryService.java
- [ ] Crear MilkingQueryService.java  
- [ ] Crear PastureQueryService.java
- [ ] Crear PromptContextBuilder.java
- [ ] Crear DTOs necesarios (BovineContextDTO, etc.)
- [ ] Crear DynamoDbConfig.java
- [ ] Modificar ChatbotService para usar contexto
- [ ] Crear tests unitarios
- [ ] Crear tests de integración
- [ ] Crear eventos de test JSON
- [ ] Testing local con SAM
- [ ] Deploy a AWS
- [ ] Documentar en README

---

## 🔗 Referencias

- **Amazon Bedrock API**: https://docs.aws.amazon.com/bedrock/latest/APIReference/
- **AWS SDK for Java**: https://github.com/aws/aws-sdk-java-v2
- **DynamoDB Enhanced Client**: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/dynamodb-enhanced-client.html
- **Spring Boot + Lambda**: https://github.com/aws/serverless-java-container/wiki/quick-start-springboot3

---

**📄 Documento de Implementación**  
**✍️ Para completar**: Integración Chatbot-DynamoDB  
**🎯 Estimado**: 1-2 semanas de desarrollo
