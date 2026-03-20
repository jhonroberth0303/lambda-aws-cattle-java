# ☕ Estándares Backend: Java/Spring Boot

**Fecha**: 2026-01-09 | **Versión**: 1.0

## 🎯 Objetivo

Estándares específicos para código Java en cattle-lambda-function.

---

## 📋 Tabla de Contenidos

1. [Setup del Proyecto](#setup-del-proyecto)
2. [Patrones Arquitectónicos](#patrones-arquitectónicos)
3. [Convenciones Java](#convenciones-java)
4. [Manejo de Excepciones](#manejo-de-excepciones)
5. [Logging](#logging)
6. [Testing](#testing-backend)
7. [Performance & Seguridad](#performance--seguridad)

---

## Setup del Proyecto

### pom.xml Configuración

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.cattle</groupId>
  <artifactId>cattle-lambda-function</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>

  <name>Cattle Lambda Function</name>
  <description>Backend para sistema de gestión de finca</description>

  <properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <spring-boot.version>2.7.0</spring-boot.version>
    <aws-sdk.version>1.12.261</aws-sdk.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>bom</artifactId>
        <version>2.17.100</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Spring Boot -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <version>${spring-boot.version}</version>
    </dependency>

    <!-- AWS DynamoDB -->
    <dependency>
      <groupId>software.amazon.awssdk</groupId>
      <artifactId>dynamodb</artifactId>
    </dependency>
    <dependency>
      <groupId>software.amazon.awssdk</groupId>
      <artifactId>dynamodb-enhanced</artifactId>
    </dependency>

    <!-- Logging -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-logging</artifactId>
      <version>${spring-boot.version}</version>
    </dependency>

    <!-- Testing -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <version>${spring-boot.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.mockito</groupId>
      <artifactId>mockito-core</artifactId>
      <version>4.6.1</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.1</version>
        <configuration>
          <source>11</source>
          <target>11</target>
        </configuration>
      </plugin>

      <!-- Checkstyle -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-checkstyle-plugin</artifactId>
        <version>3.1.2</version>
        <configuration>
          <configLocation>google_checks.xml</configLocation>
        </configuration>
      </plugin>

      <!-- SpotBugs -->
      <plugin>
        <groupId>com.github.spotbugs</groupId>
        <artifactId>spotbugs-maven-plugin</artifactId>
        <version>4.6.0.0</version>
      </plugin>

      <!-- Test Coverage -->
      <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.7</version>
        <executions>
          <execution>
            <goals>
              <goal>prepare-agent</goal>
            </goals>
          </execution>
          <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
              <goal>report</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

---

## Patrones Arquitectónicos

### 🏗️ Patrón: Layered Architecture (4 Capas)

```
┌─────────────────────────────────────────────┐
│            CONTROLLER (HTTP)                 │
│  ├─ Maneja requests HTTP                    │
│  ├─ Valida entrada (nivel básico)           │
│  └─ Delega a Processor                      │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│         PROCESSOR (Orquestación)             │
│  ├─ Orquesta flujo de negocio              │
│  ├─ Mapea DTO ↔ Entity                      │
│  ├─ Validación de negocio                   │
│  └─ Delega a Service                        │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│          SERVICE (Lógica Negocio)           │
│  ├─ Reglas de negocio                       │
│  ├─ Cálculos y transformaciones             │
│  ├─ Coordinación de repositories            │
│  └─ Delega a Repository                     │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│         REPOSITORY (Acceso a Datos)         │
│  ├─ Queries a DynamoDB                      │
│  ├─ Mapeo Entity ↔ DynamoDB Item            │
│  ├─ Validaciones de DB                      │
│  └─ Manejo de errores DynamoDB              │
└─────────────────────────────────────────────┘
```

### ✅ Ejemplo Completo: Crear Bovino

```java
// 1. CONTROLLER: Entrada HTTP
@RestController
@RequestMapping("/bovineIdentityItems")
public class BovinesController {
    private final BovinesProcessor processor;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BovineDTO> create(
        @Valid @RequestBody BovineDTO dto
    ) {
        try {
            BovineDTO created = processor.save(dto);
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
        } catch (ProcessingException e) {
            log.error("Error creating bovineIdentityItem", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }
}

// 2. PROCESSOR: Orquestación
@Component
public class BovinesProcessor {
    private final BovinesService service;
    private final BovinesMapper mapper;

    public BovineDTO save(BovineDTO dto) {
        try {
            // Mapear DTO a Entity
            Bovine entity = mapper.toEntity(dto);

            // Delegar a Service
            Bovine saved = service.save(entity);

            // Mapear resultado a DTO
            return mapper.toDTO(saved);
        } catch (ServiceException e) {
            throw new ProcessingException("Failed to save bovineIdentityItem", e);
        }
    }
}

// 3. SERVICE: Lógica de Negocio
@Service
@Transactional
public class BovinesService {
    private final BovineRepository repository;
    private final CountersRepository counters;

    public Bovine save(Bovine bovineIdentityItem) {
        try {
            // Validar
            BovineValidator.validateForCreate(bovineIdentityItem);

            // Generar ID
            String nextId = counters.getNextId(TABLE_FARM_BOVINES);
            bovineIdentityItem.setBovineId(Integer.parseInt(nextId));

            // Generar claves DynamoDB
            bovineIdentityItem.setPk("BOVINE#" + nextId);
            bovineIdentityItem.setSk("PROFILE");
            bovineIdentityItem.setGsi1pk("PROFILE");
            bovineIdentityItem.setGsi1sk("BOVINE#" + nextId);

            // Timestamps
            String now = Instant.now().toString();
            bovineIdentityItem.setCreatedAt(now);
            bovineIdentityItem.setUpdatedAt(now);

            // Enabled por defecto
            bovineIdentityItem.setEnabled(true);

            // Guardar
            return repository.save(bovineIdentityItem);
        } catch (RepositoryException e) {
            log.error("Repository error while saving bovineIdentityItem", e);
            throw new ServiceException("Failed to save bovineIdentityItem to database", e);
        }
    }
}

// 4. REPOSITORY: Acceso a Datos
@Repository
public class BovineRepository {
    private final DynamoDbEnhancedClient client;

    public Bovine save(Bovine bovineIdentityItem) {
        try {
            DynamoDbTable<Bovine> table = client.table(
                TABLE_FARM_BOVINES,
                TableSchema.fromClass(Bovine.class)
            );

            table.putItem(bovineIdentityItem);
            return bovineIdentityItem;
        } catch (DynamoDbException e) {
            log.error("DynamoDB error while saving bovineIdentityItem", e);
            throw new RepositoryException("Database error", e);
        }
    }
}
```

---

## Convenciones Java

### 📝 Nombres y Formato

```java
// PAQUETES: com.cattle.{feature}.{layer}
com.cattle.bovineIdentityItems.controller      // Controllers de Bovines
com.cattle.bovineIdentityItems.service         // Services de Bovines
com.cattle.bovineIdentityItems.repository      // Repositories de Bovines
com.cattle.pastures.controller     // Controllers de Pastures
com.cattle.shared.utils            // Utilities compartidas
com.cattle.shared.exception        // Excepciones customizadas


// CLASES: PascalCase (nombres descriptivos)
public class BovinesController { }      // ✅
public class BovineRepository { }       // ✅
public class BovineValidator { }        // ✅
public class InvalidBovineException { } // ✅

public class BovineCtrl { }             // ❌ Abreviación
public class bovineService { }         // ❌ Minúscula
public class Bovines_Repository { }     // ❌ Underscore


// INTERFACES: nombrar por comportamiento
public interface BovineRepository { }       // ✅
public interface BovineValidator { }        // ✅
public interface BovineCache { }            // ✅

public interface IBovineRepository { }      // ❌ Prefijo I
public class BovineRepositoryImpl { }       // ❌ Sufijo Impl


// MÉTODOS: verbo + sustantivo, camelCase
public Bovine findById(Integer id) { }
public Optional<Bovine> findByName(String name) { }
public List<Bovine> findAll() { }
public Bovine save(Bovine bovineIdentityItem) { }
public void delete(String pk, String sk) { }
public boolean exists(Integer id) { }

public Bovine getBovineById(Integer id) { }  // ❌ Verboso


// VARIABLES: camelCase (significativas)
private String bovineId;           // ✅
private Integer ageInYears;        // ✅
private List<Bovine> bovineIdentityItems;      // ✅
private final DynamoDbClient db;   // ✅

private String bId;                // ❌ Demasiado corta
private String bovine_id;          // ❌ snake_case
private final int INITIAL_SIZE = 10;  // ❌ Constante como variable


// CONSTANTES: UPPER_SNAKE_CASE
private static final String TABLE_FARM_BOVINES = "TABLE_FARM_BOVINES";
private static final Integer MAX_RETRIES = 3;
private static final Duration TIMEOUT = Duration.ofSeconds(30);

private static final String tableName = "...";  // ❌ camelCase
private static final int maxRetries = 3;        // ❌ camelCase
```

### 🎯 Documentación de Código

```java
/**
 * Obtiene bovino por ID.
 * 
 * <p>Busca un bovino en la base de datos usando su ID primario.
 * Retorna Optional.empty() si no existe.
 * 
 * <p><strong>Performance:</strong> O(1) - acceso directo por clave.
 * 
 * @param id ID del bovino (debe ser > 0)
 * @return Optional que contiene el bovino si existe
 * @throws RepositoryException si hay error en DB
 * @throws IllegalArgumentException si id <= 0
 * 
 * @example
 *   Optional<Bovine> bovineIdentityItem = repository.findById(47);
 *   bovineIdentityItem.ifPresent(b -> System.out.println(b.getName()));
 * 
 * @see #save(Bovine)
 * @see #delete(String, String)
 * 
 * @since 1.0
 * @author [tu nombre]
 */
public Optional<Bovine> findById(Integer id) {
    if (Objects.isNull(id) || id <= 0) {
        throw new IllegalArgumentException("ID debe ser mayor a 0");
    }
    // implementación
}


/**
 * Valida datos de bovino para creación.
 * 
 * <p>Verifica:
 * <ul>
 *   <li>Nombre no vacío</li>
 *   <li>Género válido (female/male)</li>
 *   <li>Fecha nacimiento en el pasado</li>
 *   <li>Estatus compatible con género</li>
 * </ul>
 * 
 * @param bovineIdentityItem Bovino a validar (no nulo)
 * @throws IllegalArgumentException si validación falla
 */
public static void validateForCreate(Bovine bovineIdentityItem) {
    // validación
}


/**
 * Enumeración de estados posibles de un bovino.
 * 
 * <p>Estados del ciclo de vida:
 * <table>
 *   <tr><th>Estado</th><th>Descripción</th></tr>
 *   <tr><td>CALF</td><td>Ternera/Ternero (0-6 meses)</td></tr>
 *   <tr><td>OPEN</td><td>Vacía, lista para inseminación</td></tr>
 *   <tr><td>PREGNANT</td><td>Preñada</td></tr>
 *   <tr><td>LACTATING</td><td>En producción lechera</td></tr>
 *   <tr><td>DRY</td><td>En secado (pre-parto)</td></tr>
 * </table>
 * 
 * @see Bovine
 * @since 1.0
 */
public enum BovineStatus {
    CALF,
    HEIFER,
    OPEN,
    PREGNANT,
    LACTATING,
    DRY,
    BULL,
    STEER
}
```

---

## Manejo de Excepciones

### ✅ Jerarquía de Excepciones

```java
// BASE: RuntimeException (no checked)
public abstract class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

// ESPECÍFICAS: por capa
public class RepositoryException extends BusinessException { }
public class ServiceException extends BusinessException { }
public class ProcessingException extends BusinessException { }

// VALIDACIÓN
public class ValidationException extends BusinessException {
    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}

// USO: Handler global
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProcessingException.class)
    public ResponseEntity<ErrorResponse> handleProcessingException(
        ProcessingException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Internal server error")
            .timestamp(LocalDateTime.now())
            .build();

        log.error("Processing error", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        ValidationException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation error")
            .details(ex.getErrors())
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }
}
```

### ✅ Mejores Prácticas

```java
// ❌ ANTI-PATRÓN: Silenciar excepciones
try {
    bovineRepository.save(bovineIdentityItem);
} catch (Exception e) {
    // ignorar
}

// ✅ CORRECTO: Manejar específicamente
try {
    bovineRepository.save(bovineIdentityItem);
} catch (RepositoryException e) {
    log.error("Failed to save bovineIdentityItem", e);
    throw new ServiceException("Database error while saving bovineIdentityItem", e);
}


// ❌ ANTI-PATRÓN: Excepción demasiado genérica
throw new Exception("Error");

// ✅ CORRECTO: Específica y descriptiva
throw new ServiceException(
    "Failed to calculate age for bovineIdentityItem #" + id +
    ": born date is " + bornDate
);


// ❌ ANTI-PATRÓN: No loguear antes de relanzar
try {
    operation();
} catch (Exception e) {
    throw new ServiceException("Failed", e);  // sin log
}

// ✅ CORRECTO: Log antes de relanzar
try {
    operation();
} catch (Exception e) {
    log.error("Operation failed for bovineIdentityItem {}: {}", bovineId, e.getMessage(), e);
    throw new ServiceException("Operation failed", e);
}
```

---

## Logging

### ✅ Convenciones de Logging

```java
// Configuración: application.properties
logging.level.root=INFO
logging.level.com.cattle=DEBUG
logging.level.software.amazon.awssdk=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.file.name=logs/cattle.log
logging.file.max-size=10MB
logging.file.max-history=30

// Uso en código
private static final Logger log = LoggerFactory.getLogger(BovineService.class);

// NIVELES:
log.debug("Fetching bovineIdentityItem with id: {}", bovineId);      // Desarrollo
log.info("Bovine created: {} (id: {})", name, bovineId); // Operación normal
log.warn("Mastitis detected on bovineIdentityItem {}", bovineId);    // Situación anómala
log.error("Failed to save bovineIdentityItem", exception);           // Error que requiere atención

// ✅ BUEN PATRÓN: Usar placeholders, no concatenación
log.info("Created bovineIdentityItem {} in {} ms", bovineId, duration);  // ✅

log.info("Created bovineIdentityItem " + bovineId + " in " + duration + " ms");  // ❌

// ✅ BUEN PATRÓN: Loguear excepciones
try {
    bovineRepository.save(bovineIdentityItem);
} catch (Exception e) {
    log.error("Failed to save bovineIdentityItem {}: {}", bovineId, e.getMessage(), e);
    throw e;
}

// ❌ ANTI-PATRÓN: No loguear stack trace
log.error(e.toString());  // ❌
log.error(e.getMessage());  // ❌

// ✅ CORRECTO: Loguear el objeto exception
log.error("Error occurred", e);  // ✅
```

---

## Testing Backend

### ✅ Patrón: Unit Tests

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("BovinesService")
class BovinesServiceTest {

    @Mock
    private BovineRepository repository;

    @Mock
    private CountersRepository counters;

    @InjectMocks
    private BovinesService service;

    @Test
    @DisplayName("findById debe retornar bovino existente")
    void testFindByIdSuccess() {
        // Arrange
        Integer bovineId = 47;
        Bovine bovineIdentityItem = createTestBovine(bovineId);

        when(repository.findById(bovineId))
            .thenReturn(Optional.of(bovineIdentityItem));

        // Act
        Optional<Bovine> result = service.findById(bovineId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(bovineId, result.get().getBovineId());
        verify(repository).findById(bovineId);
    }

    @Test
    @DisplayName("findById debe retornar vacío si no existe")
    void testFindByIdNotFound() {
        // Arrange
        Integer bovineId = 999;
        when(repository.findById(bovineId))
            .thenReturn(Optional.empty());

        // Act
        Optional<Bovine> result = service.findById(bovineId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("save debe generar ID y guardar")
    void testSaveGeneratesId() {
        // Arrange
        Bovine bovineIdentityItem = createTestBovine();
        bovineIdentityItem.setBovineId(null);

        when(counters.getNextId("TABLE_FARM_BOVINES"))
            .thenReturn("48");
        when(repository.save(any(Bovine.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // Act
        Bovine saved = service.save(bovineIdentityItem);

        // Assert
        assertNotNull(saved.getBovineId());
        assertEquals(48, saved.getBovineId().intValue());
        assertEquals("BOVINE#48", saved.getPk());
        assertEquals("PROFILE", saved.getSk());
    }

    @Test
    @DisplayName("save debe lanzar excepción si validación falla")
    void testSaveValidationFails() {
        // Arrange
        Bovine bovineIdentityItem = new Bovine();
        bovineIdentityItem.setName("");  // Inválido

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.save(bovineIdentityItem);
        });
        verify(repository, never()).save(any());
    }

    // Helper
    private Bovine createTestBovine(Integer id) {
        Bovine bovineIdentityItem = new Bovine();
        bovineIdentityItem.setBovineId(id);
        bovineIdentityItem.setName("Estrella");
        bovineIdentityItem.setGender("female");
        bovineIdentityItem.setStatus(BovineStatus.OPEN);
        bovineIdentityItem.setBornDate("2023-05-10");
        return bovineIdentityItem;
    }
}
```

### ✅ Patrón: Integration Tests

```java
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@DisplayName("BovinesController Integration Tests")
class BovinesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BovinesProcessor processor;

    @Test
    @DisplayName("POST /bovineIdentityItems debe crear bovino")
    void testCreateBovine() throws Exception {
        // Arrange
        BovineDTO dto = BovineDTO.builder()
            .name("Estrella")
            .gender("female")
            .bornDate("2023-05-10")
            .build();

        BovineDTO savedDto = BovineDTO.builder()
            .bovineId(47)
            .name("Estrella")
            .gender("female")
            .bornDate("2023-05-10")
            .build();

        when(processor.save(dto))
            .thenReturn(savedDto);

        // Act & Assert
        mockMvc.perform(post("/bovineIdentityItems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.bovineId").value(47))
            .andExpect(jsonPath("$.name").value("Estrella"));

        verify(processor).save(any());
    }
}
```

---

## Performance & Seguridad

### ⚡ Performance

```java
// ❌ PROBLEMA: N+1 Query
List<Bovine> bovineIdentityItems = repository.findAll();  // 1 query
bovineIdentityItems.forEach(b -> {
    Pasture pasture = pastureRepository.findById(b.getPastureId());  // N queries!
});

// ✅ SOLUCIÓN: Batch fetch
List<Bovine> bovineIdentityItems = repository.findAllWithPastures();  // 1 query con join


// ❌ PROBLEMA: Sin caching
public Bovine getById(Integer id) {
    return repository.findById(id).orElseThrow();
}

// ✅ SOLUCIÓN: Caché
@Cacheable(value = "bovineIdentityItems", key = "#id")
public Bovine getById(Integer id) {
    return repository.findById(id).orElseThrow();
}


// ❌ PROBLEMA: String concatenación en loop
String result = "";
for (String name : names) {
    result = result + name + ", ";  // Ineficiente
}

// ✅ SOLUCIÓN: StringBuilder
StringBuilder result = new StringBuilder();
for (String name : names) {
    result.append(name).append(", ");  // Eficiente
}
```

### 🔐 Seguridad

```java
// ❌ PROBLEMA: SQL Injection (aunque DynamoDB no es vulnerable)
String query = "bovineId = " + userInput;  // Nunca hacer

// ✅ CORRECTO: Usar parámetros
String bovineId = validateInteger(userInput);

// ❌ PROBLEMA: Secrets en código
String dbPassword = "MySecretPassword123";

// ✅ CORRECTO: Variables de entorno
String dbPassword = System.getenv("DB_PASSWORD");

// ❌ PROBLEMA: No validar entrada
public void updateBovine(BovineDTO dto) {
    service.save(mapper.toEntity(dto));
}

// ✅ CORRECTO: Validar antes de procesar
public void updateBovine(@Valid BovineDTO dto) {
    BovineValidator.validateForUpdate(dto);
    service.save(mapper.toEntity(dto));
}

// ❌ PROBLEMA: Revelar información sensitiva en error
catch (DatabaseException e) {
    log.error("DB connection failed: " + e.getMessage());  // Expone detalles
    throw e;
}

// ✅ CORRECTO: Log detallado internamente, respuesta genérica
catch (DatabaseException e) {
    log.error("Database operation failed", e);  // Log completo
    throw new ServiceException("Operation failed");  // Respuesta genérica
}
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
