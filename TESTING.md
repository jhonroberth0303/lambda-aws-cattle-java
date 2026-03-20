# 🧪 Suite de Tests - Cattle Bedrock Chatbot

## 📋 Descripción

Suite completa de tests para el chatbot con inteligencia artificial (AWS Bedrock) integrado en el sistema de gestión ganadera.

### Cobertura de Tests

```
┌─────────────────────────────────┐
│   TESTS UNITARIOS: 35           │
├─────────────────────────────────┤
│ IntentDetectionService    │ 8   │
│ BovineQueryService        │ 7   │
│ MilkingQueryService       │ 5   │
│ PastureQueryService       │ 4   │
│ ContextBuilderService     │ 6   │
│ BedrockService            │ 5   │
│ ChatbotService            │ 6   │
│ ChatbotController         │ 3   │
│ Error Handling            │ 2   │
│ Rate Limiting             │ 2   │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│   TESTS INTEGRACIÓN: 8          │
├─────────────────────────────────┤
│ DynamoDB Integration      │ 3   │
│ End-to-End Flow           │ 3   │
│ Security Integration      │ 1   │
│ Performance Testing       │ 1   │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│   TESTS SEGURIDAD: 12           │
├─────────────────────────────────┤
│ JWT Validation            │ 5   │
│ Unauthorized Access       │ 2   │
│ Input Sanitization        │ 2   │
│ Rate Limiting Security    │ 2   │
│ CORS Policy               │ 1   │
└─────────────────────────────────┘

   TOTAL: 47+ TESTS
```

## 🛠️ Prerequisitos

### Software Requerido

- **Java 21** - JDK instalado y configurado
- **Docker** - Para LocalStack (tests de integración)
- **Gradle** - Wrapper incluido (`./gradlew`)

### Verificar Prerequisitos

```bash
# Verificar Java
java -version
# Debe mostrar: openjdk version "21.x.x"

# Verificar Docker
docker --version
docker ps

# Verificar Gradle
./gradlew --version
```

## 🚀 Ejecutar Tests

### Tests Unitarios (Rápidos - ~15 segundos)

```bash
# Ejecutar solo tests unitarios
./gradlew test

# Ejecutar test específico
./gradlew test --tests BovineQueryServiceTest

# Ejecutar con reporte detallado
./gradlew test --info
```

### Tests de Integración (Requiere Docker - ~60 segundos)

```bash
# Iniciar LocalStack manualmente (opcional)
docker run --rm -it -p 4566:4566 localstack/localstack

# Ejecutar tests de integración
./gradlew integrationTest

# Ejecutar TODOS los tests (unitarios + integración)
./gradlew verify
```

### Tests de Seguridad

```bash
# Ejecutar solo tests de seguridad
./gradlew test --tests "com.cattle.security.*"
```

## 📊 Reporte de Cobertura

### Generar Reporte de Cobertura (Jacoco)

```bash
# Ejecutar tests y generar reporte
./gradlew test jacocoTestReport

# Ver reporte HTML
open build/reports/jacoco/test/html/index.html
# Windows: start build/reports/jacoco/test/html/index.html
```

### Métricas de Cobertura

- **Cobertura de líneas:** ≥ 85%
- **Cobertura de ramas:** ≥ 80%
- **Cobertura de métodos:** ≥ 90%

## 🧩 Estructura de Tests

```
src/test/java/com/cattle/
├── config/
│   └── TestConfiguration.java        # Configuración compartida
├── utils/
│   └── TestDataBuilder.java         # Builder de datos de test
├── mocks/
│   └── MockBedrockClient.java       # Mock de AWS Bedrock
├── containers/
│   └── LocalStackTestContainer.java # LocalStack setup
├── services/
│   ├── IntentDetectionServiceTest.java
│   ├── BovineQueryServiceTest.java
│   ├── MilkingQueryServiceTest.java
│   ├── PastureQueryServiceTest.java
│   ├── ContextBuilderServiceTest.java
│   ├── BedrockServiceTest.java
│   ├── ChatbotServiceTest.java
│   ├── ErrorHandlingTest.java
│   └── RateLimitingTest.java
├── controller/
│   └── ChatbotControllerTest.java
├── integration/
│   ├── DynamoDBIntegrationTest.java
│   ├── ChatbotIntegrationTest.java
│   ├── SecurityIntegrationTest.java
│   └── PerformanceIntegrationTest.java
└── security/
    ├── JWTValidationTest.java
    ├── UnauthorizedAccessTest.java
    ├── InputSanitizationTest.java
    ├── RateLimitingSecurityTest.java
    └── CORSPolicyTest.java
```

## 📝 Patrón AAA (Arrange-Act-Assert)

Todos los tests siguen el patrón AAA:

```java
@Test
void countAllBovines_success() {
    // Arrange (Setup)
    String farmId = "farm-001";
    List<Bovine> mockBovines = TestDataBuilder.createBovineList(farmId, 5);
    when(bovineRepository.findAllByFarmId(farmId)).thenReturn(mockBovines);
    
    // Act (Execution)
    Long result = bovineQueryService.countAllBovines(farmId);
    
    // Assert (Verification)
    assertThat(result).isEqualTo(5L);
    verify(bovineRepository).findAllByFarmId(farmId);
}
```

## 🐳 LocalStack para Tests de Integración

### Configuración Automática

LocalStack se inicia automáticamente en tests de integración mediante TestContainers.

### Configuración Manual

```bash
# Opción 1: Docker Compose (recomendado)
docker-compose -f docker-compose.test.yml up

# Opción 2: Docker directo
docker run --rm -it \
  -p 4566:4566 \
  -e SERVICES=dynamodb \
  localstack/localstack
```

### Verificar LocalStack

```bash
# Health check
curl http://localhost:4566/_localstack/health

# Listar tablas creadas
aws dynamodb list-tables --endpoint-url=http://localhost:4566
```

## 🔧 Troubleshooting

### Docker no inicia

```bash
# Verificar estado de Docker
docker ps

# Reiniciar Docker service
# Windows: Reiniciar Docker Desktop
# Linux: sudo systemctl restart docker
```

### Tests fallan con LocalStack

```bash
# Limpiar containers
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)

# Limpiar cache de Gradle
./gradlew clean

# Re-ejecutar tests
./gradlew test
```

### Cobertura < 85%

```bash
# Ver qué líneas no están cubiertas
open build/reports/jacoco/test/html/index.html

# Agregar tests para clases con baja cobertura
# Priorizar: Services > Repositories > Controllers
```

## 📚 Herramientas Utilizadas

| Herramienta | Versión | Propósito |
|-------------|---------|-----------|
| JUnit 5 | 5.11.0 | Framework de testing |
| Mockito | 5.2.0 | Mocking de dependencias |
| AssertJ | 3.24.2 | Assertions fluidas |
| TestContainers | 1.19.3 | LocalStack/Docker |
| Jacoco | 0.8.11 | Cobertura de código |
| Spring Test | 3.4.5 | Testing Spring Boot |

## 🎯 Comandos Útiles

```bash
# Clean & test
./gradlew clean test

# Test con logs detallados
./gradlew test --info --stacktrace

# Test en modo watch (re-ejecuta al cambiar código)
./gradlew test --continuous

# Skip tests (para builds rápidos)
./gradlew build -x test

# Ejecutar tests en paralelo
./gradlew test --parallel

# Generar solo reporte de cobertura (sin re-ejecutar tests)
./gradlew jacocoTestReport
```

## 📈 Métricas de Performance

- **Tests unitarios:** < 30 segundos
- **Tests integración:** < 90 segundos
- **Tests completos:** < 2 minutos
- **P99 Latency:** < 500ms

## ✅ Definición de Hecho (DoD)

- [ ] Todos los tests pasan (100% pass rate)
- [ ] Cobertura ≥ 85%
- [ ] Sin errores de compilación
- [ ] Reporte Jacoco generado
- [ ] Docker/LocalStack funcional
- [ ] Documentación actualizada

## 📖 Referencias

- [HU-BEDROCK-002: Testing & QA](docs/bedrock-chatbot/stories/HU-BEDROCK-002-TESTING.md)
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [TestContainers Documentation](https://www.testcontainers.org/)

---

**Última actualización:** Enero 2026  
**Versión:** 1.0  
**Autor:** Equipo Cattle Development
