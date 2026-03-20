# Modelo de Ciclo de Vida del Bovino

## Resumen del Diseño

Este documento describe el modelo de ciclo de vida del bovino, que separa claramente:

- **Lo que ES** (datos observables/registrados)
- **Lo que se INFIERE** (datos derivados por reglas)

## Conceptos Clave

### 1. LifeStage (Etapa de Vida)

**Responde a:** ¿En qué etapa biológica por edad está el animal?

| Valor | Descripción | Rango de Edad (meses) |
|-------|-------------|----------------------|
| `NEWBORN` | Recién nacido | 0-1 |
| `CALF` | Ternero/Lactante | 1-6 |
| `WEANED` | Destetado | 6-9 |
| `GROWER` | Levante/Crecimiento | 9-24 |
| `ADULT` | Adulto | 24+ |

**Características:**
- ✅ 100% derivable desde `bornDate`
- ✅ Siempre `Source.AUTO`
- ✅ El batch SIEMPRE puede actualizarlo

### 2. BovineCategory (Categoría Zootécnica)

**Responde a:** ¿Cómo lo clasifico zootécnicamente para manejo?

| Valor | Descripción | Derivación |
|-------|-------------|------------|
| `CALF` | Ternero/a (< 6 meses) | edad |
| `HEIFER` | Novilla (hembra 6-24 meses) | edad + sexo |
| `COW` | Vaca (hembra ≥ 24 meses) | edad + sexo |
| `YOUNG_BULL` | Toro joven (macho 6-24 meses) | edad + sexo |
| `BULL` | Toro (macho ≥ 24 meses) | edad + sexo |
| `OX` | Buey (macho castrado) | evento castración |

**Características:**
- ⚠️ Derivable con excepciones
- ⚠️ Puede ser `Source.AUTO` o `Source.MANUAL`
- ⚠️ El batch solo actualiza si `categorySource == AUTO`

### 3. Source (Fuente del Dato)

| Valor | Descripción |
|-------|-------------|
| `AUTO` | Valor calculado automáticamente por el sistema/batch |
| `MANUAL` | Valor establecido explícitamente por decisión humana |

## Estructura de ProfileLifecycle

```java
public class ProfileLifecycle {
    // === KEYS ===
    private String pk;                    // BOVINE#<id>
    private String sk;                    // PROFILE#LIFECYCLE
    
    // === GSI ===
    private String gsi1pk;
    private String gsi1sk;
    
    // === LIFECYCLE ATTRIBUTES ===
    private LifeStage lifeStage;          // NEWBORN, CALF, WEANED, GROWER, ADULT
    private Source lifeStageSource;       // Siempre AUTO
    
    private BovineCategory category;      // CALF, HEIFER, COW, YOUNG_BULL, BULL, OX
    private Source categorySource;        // AUTO o MANUAL
    
    private Boolean enabled;
    private String notes;
    
    // === BATCH SCHEDULING ===
    private String lastEvaluatedAt;       // Última evaluación
    private String nextRecalcDate;        // Próxima fecha de recálculo
    
    // === AUDIT ===
    private String updatedAt;
}
```

**Campos que pertenecen a Identity (Bovine), NO a ProfileLifecycle:**
- `sex` / `gender` → en Bovine
- `ageInMonths` → calculado desde `bornDate` en Bovine
- `status` → en Bovine (LifecycleStatus como OPEN, SOLD, DEAD)
- `castrationDate` → vendrá de EVENT#CASTRATION cuando se implementen eventos
```

## Reglas Configurables

Las reglas se definen por finca en `bovine-category-rules.yaml`:

```yaml
farms:
  finca1:
    lifeStage:
      - minAge: 0
        maxAge: 1
        stage: NEWBORN
      - minAge: 1
        maxAge: 6
        stage: CALF
      # ...
    
    female:
      - minAge: 0
        maxAge: 6
        category: CALF
      - minAge: 6
        maxAge: 24
        category: HEIFER
      - minAge: 24
        maxAge: 999
        category: COW
    
    male:
      - minAge: 0
        maxAge: 6
        category: CALF
      - minAge: 6
        maxAge: 24
        category: YOUNG_BULL
      - minAge: 24
        maxAge: 999
        category: BULL
    
    ox:
      castrated: true
      category: OX
```

## Batch de Recalculación

### Principios

1. **LifeStage:** SIEMPRE se recalcula (100% derivable)
2. **Category:** Solo se recalcula si `categorySource == AUTO`
3. **Optimización:** Usa `nextRecalcDate` para evitar recalcular todos los días

### Flujo del Batch

```
1. Query GSI2: GSI2PK = "RECALC#DUE" AND GSI2SK <= today
2. Para cada bovino:
   a. Calcular edad desde bornDate
   b. Evaluar lifeStage según reglas
   c. Si categorySource == AUTO, evaluar category
   d. Si cambió algo, UpdateItem
   e. Calcular nextRecalcDate (próximo threshold)
   f. Actualizar GSI2 con nueva fecha
```

### Ejemplo de Transición

| Edad | LifeStage | Category (♀) | nextRecalcDate |
|------|-----------|--------------|----------------|
| 0 meses | NEWBORN | CALF | bornDate + 1 mes |
| 3 meses | CALF | CALF | bornDate + 6 meses |
| 7 meses | WEANED | HEIFER | bornDate + 9 meses |
| 14 meses | GROWER | HEIFER | bornDate + 24 meses |
| 30 meses | ADULT | COW | +1 año (ya estable) |

## Servicios

### BovineCategoryRulesService

```java
// Inferir solo lifeStage
LifeStage stage = rulesService.inferLifeStage("finca1", ageMonths);

// Inferir solo category
BovineCategory cat = rulesService.inferCategory("finca1", Sex.FEMALE, 14, false);

// Inferencia completa con nextRecalcDate
InferenceResult result = rulesService.inferAll(
    "finca1", bornDate, Sex.FEMALE, false, Source.AUTO
);
```

### LifecycleRecalculationService

```java
// Recalcular y detectar cambios
RecalculationResult result = recalcService.recalculate(bovineIdentityItem, lifecycle);

if (result.hasChanges()) {
    // Aplicar cambios
    recalcService.applyRecalculation(lifecycle, result);
    // Guardar en DynamoDB
    lifecycleRepository.save(lifecycle);
}
```

## Regla de Oro

> **lifeStage** = biología (edad)  
> **category** = manejo (decisión zootécnica)

Si sigues esta separación, el modelo queda limpio, explicable y escalable.
