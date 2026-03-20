# 🗄️ Modelos de Datos - Cattle Backend

**Última actualización**: 2026-02-03

---

## 📋 Descripción

Esta carpeta documenta el diseño de datos del sistema, incluyendo el modelado de DynamoDB y el ciclo de vida de las entidades.

---

## 📁 Archivos Disponibles

| Documento | Descripción |
|-----------|-------------|
| [analysis-table-design.md](analysis-table-design.md) | Diseño detallado de tablas DynamoDB, claves, GSIs |
| [lifecycle-model.md](lifecycle-model.md) | Modelo de ciclo de vida del bovino (LifeStage, BovineCategory) |

---

## 🗃️ Tablas DynamoDB

| Tabla | Descripción | Clave Primaria |
|-------|-------------|----------------|
| `TABLE_CATTLE` | Información de bovinos | `PK: farmId`, `SK: bovineId` |
| `TABLE_FARM_MILKING` | Registros de lactancia | `PK: farmId`, `SK: milkingId` |
| `TABLE_PASTURE` | Potreros y rotación | `PK: farmId`, `SK: pastureId` |
| `TABLE_PLAN` | Planes de rotación | `PK: farmId`, `SK: planId` |
| `TABLE_COUNTERS` | Auto-increment IDs | `PK: farmId`, `SK: counterType` |

---

## 📊 Patrones de Acceso

1. **Single-Table Design**: Múltiples entidades en misma tabla
2. **Composite Keys**: Claves compuestas para consultas eficientes
3. **GSI**: Índices secundarios para consultas alternativas
4. **Counter Pattern**: Generación de IDs auto-incrementales

---

## 🔗 Documentación Relacionada

- [Arquitectura Backend](../arquitectura/architecture-cattle-lambda-function.md)
- [Eventos del Sistema](../arquitectura/eventos/)
