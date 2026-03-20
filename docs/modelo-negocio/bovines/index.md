# 🐄 Módulo Bovines (Bovinos): Documentación Completa

**Fecha**: 2026-01-09

## 📚 Índice

1. [bovineIdentityItems-overview.md](bovineIdentityItems-overview.md) - Visión técnica general
2. [components-frontend.md](components-frontend.md) - Componentes React con código fuente
3. [implementation-guide.md](implementation-guide.md) - Guía de implementación paso a paso
4. [tasks-pending.md](tasks-pending.md) - 12 tareas priorizadas + roadmap

---

## 🎯 Descripción Rápida

**Bovines** es el módulo que gestiona la **información de bovinos** en la finca:
- 📝 Registrar nuevos bovinos
- 🔍 Buscar y ver detalles
- ✏️ Editar información
- 🏷️ Escanear aretes RFID
- 📊 Seguimiento de características

**Estado**: 85% implementado
- ✅ Backend completo (GET, POST, PUT)
- ✅ Frontend funcional (listado, detalle, formulario)
- ✅ Validaciones básicas
- ⚠️ Mejorar error handling
- ❌ Genética (parentesco)
- ❌ Historial de cambios
- ❌ Reportes

---

## 📊 Características Principales

| Feature | Estado | Prioridad |
|---------|--------|-----------|
| Listar bovinos | ✅ Implementado | P0 |
| Ver detalle | ✅ Implementado | P0 |
| Agregar bovino | ✅ Implementado | P0 |
| Editar bovino | ✅ Implementado | P0 |
| Escanear arete RFID | ✅ Implementado | P1 |
| Calcular edad automática | ✅ Implementado | P1 |
| Validaciones mejoradas | ⚠️ Básicas | P1 |
| Búsqueda por nombre/ID | ❌ Falta | P1 |
| Filtrar por género/estado | ❌ Falta | P2 |
| Árbol genealógico | ❌ Falta | P2 |
| Historial de cambios | ❌ Falta | P3 |
| Reportes | ❌ Falta | P3 |

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────┐
│           BOVINES FRONTEND (React)                  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  BovineList.jsx (grid de bovinos)                   │
│   └─ BovineCard.jsx (tarjeta individual)            │
│                                                     │
│  AddBovine.jsx (formulario)                         │
│   └─ Formulario con campos múltiples                │
│                                                     │
│  BovineDetail.jsx (vista detalle)                   │
│   └─ Información completa + acciones                │
│                                                     │
│  Hooks:                                             │
│   ├─ useBovineForm (form + submit)                  │
│   └─ useBovineDetail (fetch detail)                 │
│                                                     │
│  Services:                                          │
│   └─ bovinesServices.js                             │
│                                                     │
└────────────────┬─────────────────────────────────────┘
                 │ HTTP API
┌────────────────▼─────────────────────────────────────┐
│         BOVINES BACKEND (Spring Boot)                │
├─────────────────────────────────────────────────────┤
│                                                     │
│  BovinesController.java                            │
│   GET    /bovineIdentityItems                 (listar)         │
│   GET    /bovineIdentityItems/{id}            (detalle)        │
│   POST   /bovineIdentityItems                 (crear)          │
│   PUT    /bovineIdentityItems/{id}            (actualizar)     │
│                                                     │
│  BovinesProcessor.java                             │
│   ├─ findAll() → mapeo a DTO                        │
│   ├─ findById(id)                                   │
│   ├─ save(dto) → generar ID                         │
│   └─ update(dto)                                    │
│                                                     │
│  BovinesService.java                               │
│   ├─ save(bovineIdentityItem) → Counter para ID                 │
│   ├─ findAll()                                      │
│   ├─ findById(id)                                   │
│   └─ update(bovineIdentityItem)                                 │
│                                                     │
│  BovineRepository.java                             │
│   ├─ save() → TABLE_FARM_BOVINES                    │
│   ├─ findAll() → Query GSI                          │
│   └─ findById(id) → Get item                        │
│                                                     │
└────────────────┬──────────────────────────────────────┘
                 │ DynamoDB
┌────────────────▼──────────────────────────────────────┐
│   TABLE_FARM_BOVINES (DynamoDB)                     │
│                                                     │
│   PK: BOVINE#{bovineId}                            │
│   SK: PROFILE                                       │
│   GSI1: PROFILE → BOVINE#{bovineId}                 │
│                                                     │
│   Atributos:                                       │
│   - bovineId, name, breed, gender, status          │
│   - bornDate, age, color, enabled                  │
│   - parentage info, RFID tag                        │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 🔑 Claves DynamoDB

### Tabla: TABLE_FARM_BOVINES

```
PK: BOVINE#{bovineId}
    BOVINE#1
    BOVINE#5
    BOVINE#42

SK: PROFILE (todos tienen el mismo)

GSI1-PK: PROFILE (índice para listar todos)
GSI1-SK: BOVINE#{bovineId} (ordenado)
```

**Query Ejemplos**:
```bash
# Obtener un bovino por ID
Get PK = BOVINE#5, SK = PROFILE

# Obtener todos los bovinos
Query GSI1: PROFILE con rango ordenado
```

---

## 📋 Entidad: Bovine

```java
@DynamoDbBean
public class Bovine {
    
    @Getter @Setter
    @DynamoDbPartitionKey
    private String pk;              // BOVINE#{bovineId}
    
    @Getter @Setter
    @DynamoDbSortKey
    private String sk;              // PROFILE
    
    @Getter @Setter
    private Integer bovineId;       // ID auto-generado (1, 2, 3, ...)
    
    @Getter @Setter
    private String name;            // "Estrella", "Bossy", ...
    
    @Getter @Setter
    private String breed;           // "Holstein", "Jersey", ...
    
    @Getter @Setter
    private String gender;          // "female" o "male"
    
    @Getter @Setter
    private String status;          // "OPEN", "PREGNANT", "LACTATING" (hembras)
                                    // "BULL", "STEER", "CALF" (machos)
    
    @Getter @Setter
    private String bornDate;        // YYYY-MM-DD
    
    @Getter @Setter
    private String age;             // "2 años 3 meses" (calculado)
    
    @Getter @Setter
    private String color;           // "black", "white", "spotted", ...
    
    @Getter @Setter
    private String tag;             // RFID/arete (opcional)
    
    @Getter @Setter
    private String fatherNameSnapshot;  // Nombre del padre (snapshot)
    
    @Getter @Setter
    private String motherNameSnapshot;  // Nombre de la madre (snapshot)
    
    @Getter @Setter
    private Boolean enabled;        // true/false
    
    @Getter @Setter
    private String gsi1pk;          // PROFILE (para GSI)
    
    @Getter @Setter
    private String gsi1sk;          // BOVINE#{bovineId}
    
    @Getter @Setter
    private String createdAt;       // ISO timestamp
    
    @Getter @Setter
    private String updatedAt;       // ISO timestamp
}
```

---

## 🔄 Flujos Principales

### Flujo 1: Listar Bovinos

```
1. Usuario entra a módulo Bovines
   ↓
2. BovineList.jsx hace fetch
   ↓
3. GET /bovineIdentityItems
   ↓
4. Backend query GSI:
   GSI1-PK = "PROFILE"
   ↓
5. Retorna List<BovineDTO>
   ↓
6. Mapear a BovineCard (grid)
   ↓
7. Mostrar grid con tarjetas
```

### Flujo 2: Registrar Nuevo Bovino

```
1. Usuario abre formulario "Agregar"
   ↓
2. Llena campos:
   - Nombre *
   - Sexo (female/male) *
   - Estado reproductivo
   - Fecha nacimiento *
   - Raza, color, etc.
   - Tag RFID (scan o manual)
   ↓
3. Click "Guardar"
   ↓
4. POST /bovineIdentityItems con payload
   ↓
5. Backend:
   - Obtener siguiente ID de Counter
   - Generar PK = "BOVINE#{id}"
   - Crear entity
   - Guardar en DynamoDB
   ↓
6. Response 200 + BovineDTO
   ↓
7. Refetch lista
   ↓
8. Mostrar confirmación
```

### Flujo 3: Editar Bovino

```
1. Usuario hace click en "Editar" en tarjeta
   ↓
2. Navega a /edit/{bovineId}
   ↓
3. AddBovine carga con initialBovine
   ↓
4. Fetch GET /bovineIdentityItems/{id}
   ↓
5. Precarga formulario
   ↓
6. Usuario edita campos
   ↓
7. Click "Actualizar"
   ↓
8. PUT /bovineIdentityItems/{id} con payload
   ↓
9. Backend:
   - Validar que existe
   - Actualizar atributos
   - Guardar updatedAt
   ↓
10. Response 200 + BovineDTO actualizado
```

---

## 🧩 Componentes

### BovineList.jsx
Grid de tarjetas de bovinos, fetch automático.

### BovineCard.jsx
Tarjeta individual con acciones (Ver, Editar).

### AddBovine.jsx
Formulario completo para crear/editar bovinos.

### BovineDetail.jsx
Vista detalle (lectura) con toda la información.

### useBovineForm.ts
Hook con lógica de formulario, validaciones, submit.

---

## 🔗 Referencias

- [bovineIdentityItems-overview.md](bovineIdentityItems-overview.md) - Visión técnica detallada
- [components-frontend.md](components-frontend.md) - Componentes con código fuente
- [implementation-guide.md](implementation-guide.md) - Guía de implementación
- [tasks-pending.md](tasks-pending.md) - Tareas priorizadas

---

## 🚀 Próximos Pasos

**Corto Plazo (P0-P1)**:
- ✅ Backend funcional
- ✅ Frontend básico
- [ ] Mejorar validaciones
- [ ] Búsqueda/filtros

**Mediano Plazo (P2)**:
- [ ] Árbol genealógico
- [ ] Historial de cambios
- [ ] Reportes

**Largo Plazo (P3)**:
- [ ] Integración con análisis genético
- [ ] Machine learning para predicciones

---

**Generado**: 2026-01-09 | **Versión**: 1.0
