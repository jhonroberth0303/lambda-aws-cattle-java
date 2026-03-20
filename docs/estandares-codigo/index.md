# 📋 Estándares de Código: Proyecto Cattle

**Fecha**: 2026-01-09 | **Versión**: 1.0

## 🎯 Objetivo

Documentar estándares de código, convenciones de nombres, patrones y mejores prácticas para mantener consistencia y calidad en el proyecto Cattle.

---

## 📚 Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Frontend Standards](#frontend-standards)
3. [Backend Standards](#backend-standards)
4. [Database Standards](#database-standards)
5. [Git & Versionado](#git--versionado)
6. [Testing & QA](#testing--qa)
7. [Documentation](#documentation)
8. [Code Review Checklist](#code-review-checklist)

---

## Visión General

### Principios Fundamentales

```
1. CLARIDAD SOBRE BREVEDAD
   └─ Código legible > código corto
   └─ Nombres descriptivos, no abreviaciones

2. CONSISTENCIA
   └─ Mismo patrón en toda la base de código
   └─ Linting y formateo automático

3. MANTENIBILIDAD
   └─ Fácil de cambiar sin romper
   └─ Tests que lo validen

4. SEGURIDAD
   └─ Validación de entradas
   └─ No secrets en código
   └─ Verificaciones de permisos

5. PERFORMANCE
   └─ N+1 queries evitadas
   └─ Lazy loading cuando sea posible
   └─ Caching inteligente
```

### Herramientas de Cumplimiento

```
Frontend:
├─ ESLint: linting JavaScript/TypeScript
├─ Prettier: formateo automático
├─ TypeScript: type safety
└─ Jest: testing

Backend:
├─ Checkstyle: style validation
├─ Spotbugs: bug detection
├─ JUnit 5: testing
└─ Maven: build & dependency management

Database:
├─ Naming conventions
├─ Validation rules
└─ Documentation templates

Git:
├─ Pre-commit hooks
├─ Conventional commits
└─ Branch naming
```

---

## Frontend Standards

### 📁 Estructura de Carpetas

```
cattle-front/src/
├── assets/
│   ├── icons/
│   ├── images/
│   └── styles/
│
├── components/
│   ├── Bovines/
│   │   ├── BovineList.jsx
│   │   ├── BovineList.css
│   │   ├── cards/
│   │   │   ├── BovineCard.jsx
│   │   │   └── BovineCard.css
│   │   ├── forms/
│   │   │   ├── AddBovine.jsx
│   │   │   └── AddBovine.css
│   │   ├── hooks/
│   │   │   └── useBovineForm.ts
│   │   └── __tests__/
│   │       ├── BovineList.test.jsx
│   │       └── BovineCard.test.jsx
│   │
│   ├── Shared/
│   │   ├── Header.jsx
│   │   ├── Footer.jsx
│   │   ├── Loading.jsx
│   │   └── Error.jsx
│   │
│   └── [Otros módulos similar]
│
├── layouts/
│   ├── DashboardLayout.jsx
│   └── DashboardLayout.css
│
├── services/
│   ├── bovinesServices.js
│   ├── pasturesServices.js
│   ├── milkingService.js
│   └── api.js (configuración central)
│
├── types/
│   ├── bovineIdentityItem.ts
│   ├── pasture.ts
│   └── milkingRecord.ts
│
├── utils/
│   ├── date.ts
│   ├── formatAge.ts
│   ├── icons.svg.js
│   └── helpers.ts
│
├── App.jsx
├── App.css
├── main.jsx
└── index.css
```

### 📝 Convenciones de Nombres

```javascript
// COMPONENTES: PascalCase
// ✅ BIEN
function BovineList() { }
function AddBovineForm() { }
function MilkingTable() { }

// ❌ MAL
function bovineList() { }
function add_bovine_form() { }
function MilkingTableComponent() { }


// ARCHIVOS: same as export
BovineList.jsx          // para export default function BovineList
useBovineForm.ts        // para export function useBovineForm
bovinesServices.js      // para funciones exportadas


// VARIABLES: camelCase
// ✅ BIEN
const bovineId = 47
const isLoading = false
const handleChange = () => {}
const formData = {}

// ❌ MAL
const bovineID = 47
const is_loading = false
const HandleChange = () => {}
const form_data = {}


// CONSTANTES: UPPER_SNAKE_CASE
const API_BASE_URL = "https://..."
const MAX_RETRIES = 3
const DEFAULT_PAGE_SIZE = 20

// ❌ MAL
const apiBaseUrl = "..."
const maxRetries = 3


// BOOLEANOS: prefix is/has/should
const isLoading = true
const hasError = false
const shouldRefresh = true

// ❌ MAL
const loading = true
const error = false
const refresh = true
```

### 🎨 Estructura de Componentes

```jsx
// ✅ PATRÓN RECOMENDADO

import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./BovineCard.css";

/**
 * Componente: Tarjeta de bovino individual
 * 
 * Props:
 *   - bovineIdentityItem (object): datos del bovino
 *   - onEdit (function): callback al editar
 * 
 * Ejemplo:
 *   <BovineCard bovineIdentityItem={bovineData} onEdit={handleEdit} />
 */
function BovineCard({ bovineIdentityItem, onEdit }) {
  const navigate = useNavigate();
  const [isExpanded, setIsExpanded] = useState(false);

  // Validar props
  if (!bovineIdentityItem) {
    return <div className="error">Bovino no disponible</div>;
  }

  const handleEditClick = () => {
    onEdit?.(bovineIdentityItem.bovineId);
    navigate(`/edit/${bovineIdentityItem.bovineId}`);
  };

  return (
    <article className="bovineIdentityItem-card">
      <header className="bovineIdentityItem-card-header">
        <h3>{bovineIdentityItem.name}</h3>
        <span className="bovineIdentityItem-id">#{bovineIdentityItem.bovineId}</span>
      </header>

      <section className="bovineIdentityItem-card-details">
        {/* Contenido */}
      </section>

      <footer className="bovineIdentityItem-card-actions">
        <button onClick={handleEditClick}>Editar</button>
      </footer>
    </article>
  );
}

// PropTypes o TypeScript
/**
 * @type {React.FC<{bovineIdentityItem: Bovine, onEdit?: (id: number) => void}>}
 */

export default BovineCard;
```

### 🎣 Hooks Personalizados

```typescript
// ✅ PATRÓN: Naming convención use*

import { useState, useEffect, useCallback } from "react";

/**
 * Hook para gestionar formulario de bovino
 * 
 * @param {Partial<BovineFormData>} initialData - datos iniciales
 * @returns {Object} formState, handlers, validations
 * 
 * Uso:
 *   const { formData, handleChange, handleSubmit } = useBovineForm()
 */
export function useBovineForm(initialData = {}) {
  const [formData, setFormData] = useState(initialData);
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = useCallback((e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value
    }));
  }, []);

  const validate = useCallback(() => {
    const newErrors = {};
    if (!formData.name) newErrors.name = "Nombre requerido";
    if (!formData.bornDate) newErrors.bornDate = "Fecha requerida";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [formData]);

  const handleSubmit = useCallback(async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    try {
      // API call
      const response = await createBovine(formData);
      return response;
    } catch (err) {
      setErrors({ submit: err.message });
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [formData, validate]);

  return {
    formData,
    setFormData,
    errors,
    isLoading,
    handleChange,
    handleSubmit,
    validate
  };
}
```

### 🌐 Servicios (API)

```javascript
// ✅ PATRÓN: servicios centralizados

import axios from "axios";

const API_BASE = process.env.REACT_APP_API_URL;

// Cliente HTTP centralizado
const apiClient = axios.create({
  baseURL: API_BASE,
  headers: {
    "Content-Type": "application/json"
  }
});

// Interceptor para errores
apiClient.interceptors.response.use(
  response => response.data,
  error => {
    console.error("API Error:", error);
    throw {
      status: error.response?.status,
      message: error.response?.data?.message || error.message
    };
  }
);

// BOVINES API
export const bovinesAPI = {
  getAll: () => apiClient.get("/bovineIdentityItems"),
  
  getById: (id) => {
    if (!id || id <= 0) throw new Error("ID inválido");
    return apiClient.get(`/bovineIdentityItems/${id}`);
  },
  
  create: (payload) => {
    validatePayload(payload, ["name", "gender", "bornDate"]);
    return apiClient.post("/bovineIdentityItems", payload);
  },
  
  update: (id, payload) => {
    if (!id) throw new Error("ID requerido");
    return apiClient.put(`/bovineIdentityItems/${id}`, { bovineId: id, ...payload });
  },
  
  delete: (id) => {
    if (!id) throw new Error("ID requerido");
    return apiClient.delete(`/bovineIdentityItems/${id}`);
  }
};

// Validación de payloads
function validatePayload(payload, requiredFields) {
  requiredFields.forEach(field => {
    if (!payload[field]) {
      throw new Error(`Campo ${field} es requerido`);
    }
  });
}

// Uso en componentes
const handleCreate = async (formData) => {
  try {
    const bovineIdentityItem = await bovinesAPI.create(formData);
    showSuccess("Bovino creado");
    return bovineIdentityItem;
  } catch (err) {
    showError(err.message);
    throw err;
  }
};
```

### 🧪 Testing

```javascript
// ✅ PATRÓN: Jest + React Testing Library

import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import BovineCard from "./BovineCard";

describe("BovineCard", () => {
  // Setup
  const mockBovine = {
    bovineId: 47,
    name: "Estrella",
    breed: "Holstein",
    gender: "female"
  };

  const mockOnEdit = jest.fn();

  // Tests
  it("debe renderizar información del bovino", () => {
    render(<BovineCard bovineIdentityItem={mockBovine} onEdit={mockOnEdit} />);
    
    expect(screen.getByText("Estrella")).toBeInTheDocument();
    expect(screen.getByText("#47")).toBeInTheDocument();
  });

  it("debe llamar onEdit cuando se hace clic en editar", async () => {
    const user = userEvent.setup();
    render(<BovineCard bovineIdentityItem={mockBovine} onEdit={mockOnEdit} />);
    
    await user.click(screen.getByRole("button", { name: /editar/i }));
    
    expect(mockOnEdit).toHaveBeenCalledWith(47);
  });

  it("debe mostrar error si bovineIdentityItem es nulo", () => {
    render(<BovineCard bovineIdentityItem={null} />);
    
    expect(screen.getByText(/no disponible/i)).toBeInTheDocument();
  });

  it("debe manejar bovineIdentityItems sin datos opcionales", () => {
    const minimalBovine = { bovineId: 1, name: "Test" };
    
    expect(() => {
      render(<BovineCard bovineIdentityItem={minimalBovine} />);
    }).not.toThrow();
  });
});
```

### ⚡ Performance

```javascript
// ❌ PROBLEMA: Re-renders innecesarios
function BovineList() {
  const [bovineIdentityItems, setBovines] = useState([]);

  // Cada render crea nueva función
  const handleEdit = (id) => {
    navigate(`/edit/${id}`);
  };

  return bovineIdentityItems.map(b => (
    <BovineCard key={b.bovineId} bovineIdentityItem={b} onEdit={handleEdit} />
  ));
}

// ✅ SOLUCIÓN: useMemo + useCallback
function BovineList() {
  const [bovineIdentityItems, setBovines] = useState([]);

  // Función memoizada
  const handleEdit = useCallback((id) => {
    navigate(`/edit/${id}`);
  }, [navigate]);

  // Componentes memoizados
  const renderedCards = useMemo(() =>
    bovineIdentityItems.map(b => (
      <BovineCard
        key={b.bovineId}
        bovineIdentityItem={b}
        onEdit={handleEdit}
      />
    )),
    [bovineIdentityItems, handleEdit]
  );

  return renderedCards;
}

// ❌ PROBLEMA: Query N+1
const bovineIdentityItems = await fetch("/bovineIdentityItems"); // 24 bovinos
bovineIdentityItems.forEach(b => {
  const detail = await fetch(`/bovineIdentityItems/${b.bovineId}`); // 24 queries adicionales!
});

// ✅ SOLUCIÓN: Batch fetch
const bovineIdentityItems = await fetch("/bovineIdentityItems?_expand=detail");
```

---

## Backend Standards

### 📁 Estructura de Proyecto

```
cattle-lambda-function/
├── src/
│   ├── main/
│   │   ├── java/com/cattle/
│   │   │   ├── config/
│   │   │   │   ├── LambdaContext.java
│   │   │   │   └── AppConfig.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── BovinesController.java
│   │   │   │   ├── PasturesController.java
│   │   │   │   └── MilkingController.java
│   │   │   │
│   │   │   ├── processor/
│   │   │   │   ├── BovinesProcessor.java
│   │   │   │   ├── PasturesProcessor.java
│   │   │   │   └── MilkingProcessor.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── BovinesService.java
│   │   │   │   ├── PasturesService.java
│   │   │   │   └── MilkingService.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── BovineRepository.java
│   │   │   │   ├── PastureRepository.java
│   │   │   │   ├── MilkingRepository.java
│   │   │   │   └── CountersRepository.java
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── Bovine.java
│   │   │   │   ├── Pasture.java
│   │   │   │   ├── Milking.java
│   │   │   │   └── Event.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── BovineDTO.java
│   │   │   │   ├── PastureDTO.java
│   │   │   │   └── MilkingDTO.java
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   ├── BovinesMapperImpl.java
│   │   │   │   ├── PasturesMapperImpl.java
│   │   │   │   └── MilkingMapperImpl.java
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── RepositoryException.java
│   │   │   │   ├── ServiceException.java
│   │   │   │   └── ProcessingException.java
│   │   │   │
│   │   │   ├── enums/
│   │   │   │   ├── BovineStatus.java
│   │   │   │   ├── PastureStatus.java
│   │   │   │   └── LogType.java
│   │   │   │
│   │   │   └── utils/
│   │   │       ├── DateUtils.java
│   │   │       └── ValidationUtils.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       ├── java/com/cattle/
│       │   ├── service/
│       │   │   ├── BovinesServiceTest.java
│       │   │   └── PasturesServiceTest.java
│       │   │
│       │   ├── controller/
│       │   │   └── BovinesControllerTest.java
│       │   │
│       │   └── repository/
│       │       └── BovineRepositoryTest.java
│       │
│       └── resources/
│           └── application-test.properties
│
├── pom.xml
└── template.yml
```

### 📝 Convenciones Java

```java
// CLASES: PascalCase
public class BovinesService { }
public class BovineRepository { }
public class BovineDTO { }

// ❌ MAL
public class bovines_service { }
public class BovineService { } // genérico, puede ser plural si lista


// MÉTODOS: camelCase (acción en verbo)
public List<Bovine> findAll() { }
public Optional<Bovine> findById(Integer id) { }
public Optional<Bovine> save(Bovine bovineIdentityItem) { }
public Optional<Bovine> update(Bovine bovineIdentityItem) { }
public void delete(String pk, String sk) { }

// ❌ MAL
public List<Bovine> GetAll() { }
public Optional<Bovine> fetch_by_id(Integer id) { }
public Optional<Bovine> createOrUpdate(Bovine bovineIdentityItem) { }


// VARIABLES: camelCase
private Integer bovineId;
private String name;
private boolean enabled;
private List<Bovine> bovineIdentityItems;

// ❌ MAL
private Integer bovineID;
private String Name;
private Boolean isEnabled;


// CONSTANTES: UPPER_SNAKE_CASE
private static final String TABLE_FARM_BOVINES = "TABLE_FARM_BOVINES";
private static final Integer MAX_RETRIES = 3;
private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");

// ❌ MAL
private static final String tableFarmBovines = "...";
private static final Integer maxRetries = 3;


// ENUMS: PascalCase items UPPER_SNAKE_CASE
public enum BovineStatus {
    CALF,
    OPEN,
    PREGNANT,
    LACTATING,
    DRY,
    BULL,
    STEER
}

// ❌ MAL
public enum BovineStatus {
    calf,
    Open,
    pregnant_cow
}
```

### 🏗️ Patrón Layered Architecture

```java
// ✅ PATRÓN: Controller → Processor → Service → Repository

// 1. CONTROLLER: Recibe HTTP, valida, delega
@RestController
@RequestMapping("/bovineIdentityItems")
public class BovinesController {
    private final BovinesProcessor processor;

    @GetMapping("/{id}")
    public ResponseEntity<BovineDTO> findById(@PathVariable Integer id) {
        // Validación básica
        if (Objects.isNull(id) || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        // Delega a Processor
        return processor.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

// 2. PROCESSOR: Lógica de negocio, mapeo, coordinación
@Component
public class BovinesProcessor {
    private final BovinesService service;
    private final BovinesMapperImpl mapper;

    public Optional<BovineDTO> findById(Integer id) {
        try {
            // Lógica de negocio
            Optional<Bovine> bovineIdentityItem = service.findById(id);
            
            // Mapeo a DTO
            return bovineIdentityItem.map(mapper::toDTO);
        } catch (ServiceException e) {
            throw new ProcessingException("Failed to fetch bovineIdentityItem", e);
        }
    }
}

// 3. SERVICE: Acceso a datos, reglas de negocio
@Service
public class BovinesService {
    private final BovineRepository repository;
    private final CountersRepository counters;

    public Optional<Bovine> findById(Integer id) {
        try {
            return repository.findById(id);
        } catch (RepositoryException e) {
            throw new ServiceException("Failed to fetch bovineIdentityItem", e);
        }
    }

    public Optional<Bovine> save(Bovine bovineIdentityItem) {
        try {
            // Generar ID si es necesario
            String nextId = counters.getNextId(TABLE_FARM_BOVINES);
            bovineIdentityItem.setBovineId(Integer.parseInt(nextId));
            
            // Generar claves
            bovineIdentityItem.setPk("BOVINE#" + nextId);
            bovineIdentityItem.setSk("PROFILE");
            
            // Guardar
            return repository.save(bovineIdentityItem);
        } catch (Exception e) {
            throw new ServiceException("Failed to save bovineIdentityItem", e);
        }
    }
}

// 4. REPOSITORY: Acceso a DB, queries
@Repository
public class BovineRepository {
    private final DynamoDbClient client;

    public Optional<Bovine> findById(Integer id) {
        try {
            GetItemResponse response = client.getItem(request -> request
                .tableName(TABLE_FARM_BOVINES)
                .key("pk", AttributeValue.fromS("BOVINE#" + id))
                .key("sk", AttributeValue.fromS("PROFILE"))
            );
            
            return response.item().isEmpty()
                ? Optional.empty()
                : Optional.of(mapToBovine(response.item()));
        } catch (Exception e) {
            throw new RepositoryException("Failed to fetch from DB", e);
        }
    }
}
```

### 🎣 Exception Handling

```java
// ✅ PATRÓN: Hierarchy de excepciones customizadas

// Base
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Específicas
public class RepositoryException extends BusinessException { }
public class ServiceException extends BusinessException { }
public class ProcessingException extends BusinessException { }

// Uso
public Optional<Bovine> save(Bovine bovineIdentityItem) {
    try {
        // operación
        return repository.save(bovineIdentityItem);
    } catch (RepositoryException e) {
        // Log específico
        log.error("Repository failed: {}", e.getMessage());
        throw new ServiceException("Failed to save bovineIdentityItem", e);
    }
}

// Handler global
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

### 🧪 Testing

```java
// ✅ PATRÓN: JUnit 5 + Mockito

@ExtendWith(MockitoExtension.class)
class BovinesServiceTest {
    
    @Mock
    private BovineRepository repository;
    
    @Mock
    private CountersRepository counters;
    
    @InjectMocks
    private BovinesService service;
    
    @Test
    @DisplayName("debe guardar nuevo bovino con ID generado")
    void testSaveGeneratesNewId() {
        // Arrange
        Bovine bovineIdentityItem = new Bovine();
        bovineIdentityItem.setName("Estrella");
        bovineIdentityItem.setGender("female");
        
        when(counters.getNextId("TABLE_FARM_BOVINES"))
            .thenReturn("47");
        when(repository.save(any()))
            .thenReturn(Optional.of(bovineIdentityItem));
        
        // Act
        Optional<Bovine> result = service.save(bovineIdentityItem);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("47", bovineIdentityItem.getBovineId().toString());
        verify(repository).save(any());
    }
    
    @Test
    @DisplayName("debe lanzar excepción si repository falla")
    void testSaveThrowsException() {
        Bovine bovineIdentityItem = new Bovine();
        
        when(counters.getNextId(any()))
            .thenThrow(new RepositoryException("DB error"));
        
        assertThrows(ServiceException.class, () -> {
            service.save(bovineIdentityItem);
        });
    }
}
```

---

## Database Standards

### 🔑 DynamoDB Naming Conventions

```
TABLA: TABLE_{DOMAIN}_{ENTITY}
├─ TABLE_FARM_BOVINES
├─ TABLE_FARM_PASTURES
├─ TABLE_FARM_MILKING
├─ TABLE_COUNTERS
└─ TABLE_AUDIT_{ENTITY}

PARTITION KEY: {ENTITY}#{id}
├─ BOVINE#47
├─ PASTURE#A
└─ MILKING#2025-12-20

SORT KEY: {TYPE}
├─ PROFILE (perfil/configuración)
├─ {DATE}#{SHIFT} (milkingRecord diario)
├─ ENTRY (evento entrada)
├─ EXIT (evento salida)
└─ AUDIT#{TIMESTAMP} (auditoría)

GSI: {DOMAIN}__{QUERY}
├─ FARM__PROFILE (listar todos)
├─ FARM__BY_STATUS (filtrar por estado)
├─ FARM__BY_DATE (por fecha)
└─ FARM__BY_HEALTH (por estado salud)

EJEMPLO COMPLETO:
├─ PK: BOVINE#47
├─ SK: PROFILE
├─ GSI1-PK: PROFILE
├─ GSI1-SK: BOVINE#47
├─ Attributes: bovineId, name, breed, status, ...
└─ Timestamps: createdAt, updatedAt
```

### 📋 Atributos Estándar

```java
// Todos los entities incluyen:

@DynamoDbBean
public class BaseEntity {
    
    @DynamoDbPartitionKey
    private String pk;              // OBLIGATORIO
    
    @DynamoDbSortKey
    private String sk;              // OBLIGATORIO
    
    @Getter @Setter
    private String gsi1pk;          // Para GSI1
    
    @Getter @Setter
    private String gsi1sk;          // Para GSI1
    
    @Getter @Setter
    private String createdAt;       // ISO timestamp
    
    @Getter @Setter
    private String updatedAt;       // ISO timestamp
    
    @Getter @Setter
    private Boolean enabled;        // Soft delete
    
    // Validación
    @PrePersist
    public void validate() {
        if (this.pk == null || this.pk.isBlank()) {
            throw new IllegalArgumentException("PK es requerida");
        }
    }
}
```

### 🔐 Validación de Datos

```java
// Validar EN aplicación, no en DB

public class BovineValidator {
    
    public static void validateForCreate(Bovine bovineIdentityItem) {
        if (bovineIdentityItem.getName() == null || bovineIdentityItem.getName().isBlank()) {
            throw new IllegalArgumentException("name es requerido");
        }
        
        if (!isValidGender(bovineIdentityItem.getGender())) {
            throw new IllegalArgumentException(
                "gender debe ser 'female' o 'male'"
            );
        }
        
        if (!isValidStatus(bovineIdentityItem.getGender(), bovineIdentityItem.getStatus())) {
            throw new IllegalArgumentException(
                "status inválido para este género"
            );
        }
        
        if (bovineIdentityItem.getBornDate() == null) {
            throw new IllegalArgumentException("bornDate es requerido");
        }
        
        LocalDate born = LocalDate.parse(bovineIdentityItem.getBornDate());
        if (born.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "bornDate no puede ser en el futuro"
            );
        }
    }
    
    private static boolean isValidGender(String gender) {
        return "female".equals(gender) || "male".equals(gender);
    }
    
    private static boolean isValidStatus(String gender, String status) {
        if (gender == null || status == null) return true;
        
        List<String> allowedForGender = gender.equals("female")
            ? List.of("OPEN", "PREGNANT", "DRY", "LACTATING")
            : List.of("BULL", "STEER", "CALF");
        
        return allowedForGender.contains(status);
    }
}
```

---

## Git & Versionado

### 🌿 Branch Naming

```
main                    # Producción
├─ develop             # Integración
│  ├─ feature/ISSUE-XX-descripción
│  ├─ bugfix/ISSUE-XX-descripción
│  ├─ hotfix/ISSUE-XX-descripción
│  └─ refactor/ISSUE-XX-descripción

EJEMPLOS:
├─ feature/CATTLE-47-bovineIdentityItem-form-validation
├─ bugfix/CATTLE-52-mastitis-detection
├─ refactor/CATTLE-38-service-layer
└─ hotfix/CATTLE-61-critical-api-issue
```

### 📝 Commit Messages (Conventional Commits)

```
FORMATO: <type>(<scope>): <subject>
         <blank line>
         <body>
         <blank line>
         <footer>

TIPOS:
├─ feat: Nueva feature
├─ fix: Bug fix
├─ docs: Documentación
├─ style: Formato (no afecta lógica)
├─ refactor: Cambio de código (no feature, no fix)
├─ perf: Mejora de performance
├─ test: Agregar tests
└─ ci: Cambios CI/CD

EJEMPLOS:

feat(bovineIdentityItems): agregar validación de género en formulario
  - Validar que género sea female o male
  - Mostrar error en campo si inválido
  - Add tests para validación

Closes #47

---

fix(milkingRecord): corregir cálculo de persistencia

Persistencia se calculaba como (AM-1L)/AM en lugar de AM/PM.
Ahora cálculo correcto: PM/AM.

Fixes #52

---

refactor(backend): simplificar estructura de excepciones
  - Remover excepción genérica BusinessException
  - Usar específicas: RepositoryException, ServiceException
  - Update handlers

Breaking change: Aplicaciones que catcheaban BusinessException
deben actualizar a ServiceException.

---

docs: actualizar README con instrucciones setup

---

perf(pastures): optimizar query de potreros disponibles

Cambiar de N queries (uno per potrero) a 1 batch query.
Resultado: -500ms en listado de potreros.

Benchmark:
  Antes: 1200ms
  Después: 700ms
```

### ✅ Pull Request Checklist

```markdown
## Descripción
[Describir cambios]

## Tipo
- [ ] Feature
- [ ] Bug fix
- [ ] Refactoring
- [ ] Documentation

## Testing
- [ ] Tests unitarios agregados
- [ ] Tests integración pasados
- [ ] Sin warnings en console

## Código
- [ ] Sigue estándares
- [ ] Sin código muerto
- [ ] No secrets en código
- [ ] Linting pass (ESLint/Checkstyle)

## Performance
- [ ] Sin N+1 queries
- [ ] Sin memory leaks
- [ ] Caching implementado si necesario

## Security
- [ ] Validación de entradas
- [ ] No SQL injection
- [ ] Permisos validados
- [ ] Secrets en .env, no en código

## Documentation
- [ ] JSDoc/JavaDoc actualizado
- [ ] README actualizado si aplica
- [ ] Cambios en API documentados

## Resolves
Closes #ISSUE-XX
```

---

## Testing & QA

### 📊 Coverage Mínimo

```
Frontend:
├─ Componentes: 80% líneas
├─ Hooks: 85% líneas
├─ Servicios: 90% líneas
└─ Utils: 95% líneas

Backend:
├─ Controllers: 70% (endpoints cubiertos)
├─ Services: 90% (lógica crítica)
├─ Repositories: 85% (queries probadas)
└─ Validators: 100% (todo validado)

Minimum: 75% overall
Target: 85%+
```

### 🧪 Testing Strategy

```
UNITARIOS (fast, isolated):
├─ Funciones puras
├─ Métodos helpers
├─ Lógica de validación
├─ Transformaciones de datos

INTEGRACIÓN (slower, with DB):
├─ Repository queries
├─ Service workflows
├─ Manejo de excepciones

E2E (slowest, full stack):
├─ Flujos completos usuario
├─ APIs end-to-end
├─ Reportes

RATIO RECOMENDADO: 70% unit, 20% integration, 10% e2e
```

---

## Documentation

### 📖 Documentación Requerida

```
CÓDIGO FUENTE:
├─ JSDoc (Frontend)
│   /**
│    * Renderiza tarjeta de bovino
│    * 
│    * @param {Bovine} bovineIdentityItem - datos del bovino
│    * @param {Function} onEdit - callback edit
│    * @returns {JSX.Element}
│    */
│
├─ JavaDoc (Backend)
│   /**
│    * Obtiene bovino por ID
│    * 
│    * @param id ID del bovino (debe ser > 0)
│    * @return Optional<Bovine> bovino encontrado
│    * @throws ServiceException si DB falla
│    */

APIS:
├─ Endpoint documentado
│   GET /bovineIdentityItems/{id}
│   ├─ Descripción
│   ├─ Path params
│   ├─ Query params
│   ├─ Request body (si aplica)
│   ├─ Response 200
│   ├─ Response 404
│   └─ Error handling

MÓDULOS/MÓDULOS:
├─ README.md con descripción
├─ Flujo de datos
├─ Casos de uso principales
├─ Ejemplos de código
└─ Requisitos (si aplica)

RELEASE NOTES:
├─ Features nuevas
├─ Bug fixes
├─ Breaking changes
├─ Migration guide (si necesario)
```

---

## Code Review Checklist

```markdown
### Funcionalidad
- [ ] ¿Resuelve el issue/feature descrito?
- [ ] ¿Hay edge cases no cubiertos?
- [ ] ¿Error handling es robusto?
- [ ] ¿Validación es completa?

### Calidad
- [ ] ¿Sigue estándares del proyecto?
- [ ] ¿Código es legible y mantenible?
- [ ] ¿No hay duplicación?
- [ ] ¿Tests adecuados?

### Performance
- [ ] ¿N+1 queries evitadas?
- [ ] ¿Memory leaks posibles?
- [ ] ¿Caching implementado si necesario?
- [ ] ¿Rendering optimizado? (Frontend)

### Security
- [ ] ¿Validación de inputs?
- [ ] ¿Autorización verificada?
- [ ] ¿Secrets seguros?
- [ ] ¿SQL injection prevención?

### Documentación
- [ ] ¿Código comentado si complejo?
- [ ] ¿JSDoc/JavaDoc actualizado?
- [ ] ¿APIs documentadas?
- [ ] ¿README actualizado?

### Cambios
- [ ] ¿Breaking changes documentados?
- [ ] ¿Migrations incluidas (si BD)?
- [ ] ¿Backwards compatible?

### Comentarios
- [ ] ¿Feedback es constructivo?
- [ ] ¿Sugerencias son alternativas claras?
- [ ] ¿Se responden comentarios?

APROBACIÓN:
- [ ] 2 approvals mínimo
- [ ] Todos los comentarios resueltos
- [ ] Tests pasando (CI/CD green)
- [ ] Linting limpio
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
