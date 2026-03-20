# 🚀 Guía de Implementación: Bovines

**Fecha**: 2026-01-09

## 🎯 Objetivo

Guía para mejorar y completar el módulo Bovines con 12 tareas priorizadas.

---

## 📚 Tabla de Contenidos

1. [Estado Actual](#estado-actual)
2. [Tareas P0 (Críticas)](#tareas-p0-críticas)
3. [Tareas P1 (Importantes)](#tareas-p1-importantes)
4. [Tareas P2 (Deseables)](#tareas-p2-deseables)
5. [Roadmap](#roadmap)

---

## Estado Actual

### ✅ Implementado (85%)

| Componente | Status | Notas |
|-----------|--------|-------|
| DynamoDB | ✅ | Tabla con claves correctas + GSI |
| CounterRepository | ✅ | Genera IDs secuenciales |
| Controller | ✅ | GET, POST, PUT funcionales |
| Service | ✅ | Completa, con auto-ID |
| Repository | ✅ | CRUD básico |
| Frontend list | ✅ | Grid de bovinos |
| Tarjetas | ✅ | BovineCard completo |
| Formulario | ✅ | Crear/editar + RFID |
| Detalle | ✅ | Vista lectura |
| Hooks | ✅ | useBovineForm, useBovineDetail |
| Edad automática | ✅ | Calcula años/meses |

### ⚠️ A Mejorar (10%)

- Validaciones incompletas
- Error handling básico
- UI/UX mejorable

### ❌ No Implementado (5%)

- Búsqueda/filtros
- Árbol genealógico
- Historial de cambios
- Reportes

---

## Tareas P0 (Críticas)

### P0#1: Validaciones Mejoradas Backend

**Descripción**: Agregar validaciones enum para status y razas.

**Tiempo**: 1-2 horas

**Código**:

```java
// BovinesService.java
public Optional<Bovine> save(Bovine bovineIdentityItem) {
    try {
        // ✅ Validar nombre
        if (bovineIdentityItem.getName() == null || bovineIdentityItem.getName().isBlank()) {
            throw new IllegalArgumentException("El campo name es obligatorio");
        }

        // ✅ Validar género
        if (bovineIdentityItem.getGender() == null || 
            (!bovineIdentityItem.getGender().equals("female") && !bovineIdentityItem.getGender().equals("male"))) {
            throw new IllegalArgumentException(
                "El campo gender debe ser 'female' o 'male'"
            );
        }

        // ✅ Validar status según género
        String status = bovineIdentityItem.getStatus();
        if (status != null) {
            List<String> allowedStatuses = bovineIdentityItem.getGender().equals("female")
                ? List.of("OPEN", "PREGNANT", "DRY", "LACTATING")
                : List.of("BULL", "STEER", "CALF");
            
            if (!allowedStatuses.contains(status)) {
                throw new IllegalArgumentException(
                    "Status inválido para género: " + status
                );
            }
        }

        // ✅ Validar raza
        if (bovineIdentityItem.getBreed() != null && !isValidBreed(bovineIdentityItem.getBreed())) {
            throw new IllegalArgumentException(
                "Raza no válida: " + bovineIdentityItem.getBreed()
            );
        }

        // ✅ Validar fecha de nacimiento
        if (bovineIdentityItem.getBornDate() == null || bovineIdentityItem.getBornDate().isBlank()) {
            throw new IllegalArgumentException(
                "El campo bornDate es obligatorio"
            );
        }

        try {
            LocalDate born = LocalDate.parse(bovineIdentityItem.getBornDate());
            if (born.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser en el futuro"
                );
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Formato de fecha inválido (YYYY-MM-DD)"
            );
        }

        // ... rest of save logic
        return bovineRepository.save(bovineIdentityItem);
    } catch (RepositoryException e) {
        throw new ServiceException("Failed to save bovineIdentityItem", e);
    }
}

private static final List<String> VALID_BREEDS = List.of(
    "Holstein", "Jersey", "Angus", "Brahman", "Simmental",
    "Normando", "Gyr", "Pardo Suizo"
);

private boolean isValidBreed(String breed) {
    return VALID_BREEDS.contains(breed);
}
```

### P0#2: Mejorar Error Handling Frontend

**Descripción**: Capturar y mostrar errores en UI.

**Tiempo**: 1-2 horas

**Código**:

```jsx
// AddBovine.jsx
export default function AddBovine({ onBovineAdded, initialBovine }) {
  const {
    formData,
    handleChange,
    handleSubmit,
    ...rest
  } = useBovineForm({ onBovineAdded, initialBovine });

  const [error, setError] = useState(null);           // ✅ NUEVO
  const [fieldErrors, setFieldErrors] = useState({});  // ✅ NUEVO

  const handleSubmitWithErrors = async (e) => {
    e.preventDefault();
    setError(null);
    setFieldErrors({});

    // Validaciones frontend
    const errors = {};
    if (!formData.name) errors.name = "Nombre requerido";
    if (!formData.gender) errors.gender = "Género requerido";
    if (!formData.bornDate) errors.bornDate = "Fecha requerida";

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    try {
      await handleSubmit(e);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.message ||
        "Error desconocido"
      );
    }
  };

  return (
    <div className="add-bovineIdentityItem-container">
      {/* ✅ NUEVO: Error banner */}
      {error && (
        <div className="error-banner">
          ⚠️ {error}
        </div>
      )}

      <form onSubmit={handleSubmitWithErrors}>
        {/* Campo: Nombre */}
        <div>
          <label>Nombre *</label>
          <input
            name="name"
            value={formData.name}
            onChange={handleChange}
            className={fieldErrors.name ? "input-error" : ""}
          />
          {fieldErrors.name && (
            <span className="field-error">{fieldErrors.name}</span>
          )}
        </div>

        {/* ... rest de campos ... */}
      </form>
    </div>
  );
}
```

### ✅ Checklist P0

- [ ] Validar nombre (requerido)
- [ ] Validar género (female/male)
- [ ] Validar status por género
- [ ] Validar raza en enum
- [ ] Validar fecha (YYYY-MM-DD, ≤ hoy)
- [ ] Error banner en frontend
- [ ] Field errors junto a campos
- [ ] Tests de validación

---

## Tareas P1 (Importantes)

### P1#1: Búsqueda por Nombre/ID

**Descripción**: Agregar búsqueda y filtros a lista.

**Tiempo**: 2-3 horas

**Frontend**:

```jsx
function BovineList() {
  const [bovineIdentityItems, setBovines] = useState([]);
  const [search, setSearch] = useState("");
  const [filtered, setFiltered] = useState([]);

  // Filtrar localmente
  useEffect(() => {
    const query = search.toLowerCase();
    setFiltered(
      bovineIdentityItems.filter(b =>
        b.name.toLowerCase().includes(query) ||
        b.bovineId.toString().includes(query)
      )
    );
  }, [bovineIdentityItems, search]);

  return (
    <div>
      <input
        type="text"
        placeholder="Buscar por nombre o ID..."
        value={search}
        onChange={e => setSearch(e.target.value)}
        className="search-input"
      />
      <div className="grid">
        {filtered.map(b => <BovineCard key={b.bovineId} bovineIdentityItem={b} />)}
      </div>
    </div>
  );
}
```

---

### P1#2: Filtrar por Género/Estado

**Descripción**: Agregar selectores para filtrar.

**Tiempo**: 2-3 horas

**Código**:

```jsx
function BovineList() {
  const [gender, setGender] = useState("all");
  const [status, setStatus] = useState("all");

  const filtered = bovineIdentityItems.filter(b =>
    (gender === "all" || b.gender === gender) &&
    (status === "all" || b.status === status)
  );

  return (
    <div>
      <div className="filters">
        <select value={gender} onChange={e => setGender(e.target.value)}>
          <option value="all">Todos los géneros</option>
          <option value="female">Hembras</option>
          <option value="male">Machos</option>
        </select>

        <select value={status} onChange={e => setStatus(e.target.value)}>
          <option value="all">Todos los estados</option>
          <option value="OPEN">Disponibles</option>
          <option value="PREGNANT">Preñadas</option>
          <option value="LACTATING">Lactando</option>
        </select>
      </div>

      <div className="grid">
        {filtered.map(b => <BovineCard key={b.bovineId} bovineIdentityItem={b} />)}
      </div>
    </div>
  );
}
```

---

### P1#3: Confirmación antes de Editar

**Descripción**: Pedir confirmación antes de cambios importantes.

**Tiempo**: 1-2 horas

```jsx
const handleSubmit = async (e) => {
  if (isEditMode) {
    // Mostrar cambios vs original
    const changes = {};
    for (const key in formData) {
      if (formData[key] !== initialBovine[key]) {
        changes[key] = {
          old: initialBovine[key],
          new: formData[key]
        };
      }
    }

    if (Object.keys(changes).length > 0) {
      const confirm = window.confirm(
        `¿Actualizar ${Object.keys(changes).length} campos?`
      );
      if (!confirm) return;
    }
  }

  // Proceder con submit
  await handleSubmit(e);
};
```

---

### P1#4: Eliminar Bovino

**Descripción**: Agregar endpoint DELETE.

**Tiempo**: 2-3 horas

**Backend**:

```java
// BovinesController.java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Integer id) {
    if (id <= 0) return ResponseEntity.badRequest().build();
    bovineProcessor.delete(id);
    return ResponseEntity.noContent().build();
}

// BovinesProcessor.java
public void delete(Integer id) {
    try {
        bovineService.delete(id);
    } catch (ServiceException e) {
        throw new ProcessingException("Failed to delete bovineIdentityItem", e);
    }
}

// BovinesService.java
public void delete(Integer id) {
    try {
        bovineRepository.delete("BOVINE#" + id, "PROFILE");
    } catch (RepositoryException e) {
        throw new ServiceException("Failed to delete bovineIdentityItem", e);
    }
}
```

---

### P1#5: Tests Unitarios

**Descripción**: Agregar tests para validaciones.

**Tiempo**: 2-3 horas

```java
@Test
public void testSaveWithInvalidGender() {
    BovineDTO dto = new BovineDTO();
    dto.setName("Test");
    dto.setGender("UNKNOWN");
    
    assertThrows(IllegalArgumentException.class, () -> {
        bovineProcessor.save(dto);
    });
}

@Test
public void testSaveWithFutureBornDate() {
    BovineDTO dto = new BovineDTO();
    dto.setName("Test");
    dto.setGender("female");
    dto.setBornDate(LocalDate.now().plusDays(1).toString());
    
    assertThrows(IllegalArgumentException.class, () -> {
        bovineProcessor.save(dto);
    });
}

@Test
public void testStatusValidation() {
    BovineDTO dto = new BovineDTO();
    dto.setName("Test");
    dto.setGender("female");
    dto.setStatus("BULL");  // ❌ Inválido para hembra
    
    assertThrows(IllegalArgumentException.class, () -> {
        bovineProcessor.save(dto);
    });
}
```

---

## Tareas P2 (Deseables)

### P2#1: Árbol Genealógico

**Descripción**: Mostrar relaciones padre-madre-hijo.

**Tiempo**: 6-8 horas

**Estructura**:

```
Crear tabla TABLE_BOVINE_GENEALOGY:
  PK: BOVINE#{childId}
  SK: PARENT#{parentId}
  Atributos:
    - parentId
    - parentRole (father/mother)
    - addedAt
```

---

### P2#2: Historial de Cambios

**Descripción**: Registrar auditoría de cambios.

**Tiempo**: 4-5 horas

```java
// Crear tabla TABLE_BOVINE_AUDIT
@DynamoDbBean
public class BovineAudit {
    @DynamoDbPartitionKey
    private String pk;  // BOVINE#{id}
    
    @DynamoDbSortKey
    private String sk;  // AUDIT#{timestamp}
    
    private String action;      // CREATE, UPDATE
    private String userId;
    private Map<String, Object> changes;
    private String timestamp;
}
```

---

### P2#3: Exportar a CSV

**Descripción**: Descargar lista en CSV.

**Tiempo**: 2-3 horas

```jsx
const handleExport = () => {
  const csv = [
    ["ID", "Nombre", "Raza", "Género", "Estado", "Edad"].join(","),
    ...bovineIdentityItems.map(b =>
      [b.bovineId, b.name, b.breed, b.gender, b.status, b.age].join(",")
    )
  ].join("\n");

  const link = document.createElement("a");
  link.href = "data:text/csv," + encodeURIComponent(csv);
  link.download = "bovineIdentityItems.csv";
  link.click();
};
```

---

## Roadmap

### Fase 1: Validaciones (Semana 1)

**Tareas**: P0#1, P0#2

**Horas**: 2-4

**Objetivo**: Backend robusto, UI con error handling

---

### Fase 2: Búsqueda y Filtros (Semana 2)

**Tareas**: P1#1, P1#2

**Horas**: 4-6

**Objetivo**: Encontrar bovinos fácilmente

---

### Fase 3: Confirmaciones y Tests (Semana 2-3)

**Tareas**: P1#3, P1#4, P1#5

**Horas**: 5-8

**Objetivo**: Seguridad, confianza, cobertura de tests

---

### Fase 4: Genealogía y Auditoría (Semanas 4-5)

**Tareas**: P2#1, P2#2

**Horas**: 10-13

**Objetivo**: Seguimiento genético e historial

---

### Fase 5: Reportes (Semana 5+)

**Tareas**: P2#3

**Horas**: 2-3

**Objetivo**: Exportación de datos

---

## 📊 Resumen de Esfuerzo

| Fase | Tareas | Horas | Semanas |
|------|--------|-------|---------|
| **P0** | #1, #2 | 2-4 | 1 |
| **P1** | #1-5 | 9-15 | 2-3 |
| **P2** | #1-3 | 12-16 | 2-3 |
| **TOTAL** | 10+ | 23-35 | 5-7 semanas |

---

**Generado**: 2026-01-09 | **Versión**: 1.0
