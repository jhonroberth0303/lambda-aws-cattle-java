# 📖 Historias de Usuario - Estructura del Proyecto

**Ubicación**: `docs/stories/`

---

## 📁 Estructura de Carpetas

```
docs/stories/
├── P0/                          ← CRÍTICO
│   ├── INDEX.md                 ← Índice P0 (3 HUs)
│   ├── PASTURES-HU-001-post-eventos.md
│   ├── PASTURES-HU-002-frontend-botones.md (por escribir)
│   └── PASTURES-HU-003-detailpanel.md (por escribir)
│
├── P1/                          ← ALTO
│   ├── INDEX.md                 ← Índice P1 (6 HUs)
│   ├── PASTURES-HU-004-put-editar.md
│   ├── PASTURES-HU-005-post-crear.md
│   └── ... (4 más)
│
├── P2/                          ← MEDIO
│   ├── INDEX.md                 ← Índice P2 (7 HUs)
│   └── ... (7 HUs)
│
├── P3/                          ← BAJO
│   ├── INDEX.md                 ← Índice P3 (9 HUs)
│   └── ... (9 HUs)
│
└── README.md                    ← Este archivo

TOTAL: 25 HUs organizadas por prioridad
```

---

## 🎯 Convenciones de Nombres

**Formato de archivo**:
```
{MODULO}-HU-{numero}-{titulo-slug}.md

Ejemplos:
PASTURES-HU-001-post-eventos.md
PASTURES-HU-002-frontend-botones.md
BOVINES-HU-001-crear-bovino.md
MILKING-HU-001-registrar-ordeno.md
```

**Nombres claros, sin caracteres especiales**:
- ✅ PASTURES-HU-001-post-eventos.md
- ❌ PASTURES-HU#1-Backend-POST-Eventos.md
- ❌ HU1-eventos.md

---

## 🔴 Prioridad P0 (CRÍTICO)

**Carpeta**: `docs/stories/P0/`

Historias que **bloquean** MVP y deben completarse primero.

- **Estimación total**: 12-17 horas (2-3 días)
- **Plazo**: Semana 1
- **Entregable**: MVP funcional básico

### HUs P0
1. ✅ HU#1: Backend POST Eventos OPEN/CLOSE/MAINTENANCE
2. 🔲 HU#2: Frontend - Conectar Botones
3. 🔲 HU#3: Frontend - DetailPanel Funcional

[Detalle completo en P0/INDEX.md](./P0/INDEX.md)

---

## 🟠 Prioridad P1 (ALTO)

**Carpeta**: `docs/stories/P1/`

Historias que **mejoran robustez** del MVP.

- **Estimación total**: 24-35 horas (3-5 días)
- **Plazo**: Semana 3-4
- **Entregable**: Código testeado y trazable

### HUs P1
4. ✅ Backend: PUT Editar Potrero
5. 🔲 Backend: POST Crear Potrero
6. 🔲 Frontend: Modal Bloqueo/Mantenimiento
7. 🔲 Frontend: Validaciones en Formularios
8. 🔲 Backend: Tests Unitarios PastureStatusEngine
9. 🔲 Backend: Tests EtaCalculator

[Detalle completo en P1/INDEX.md](./P1/INDEX.md)

---

## 🟡 Prioridad P2 (MEDIO)

**Carpeta**: `docs/stories/P2/`

Historias que **mejoran UX** y features avanzadas.

- **Estimación total**: 25-35 horas (3-5 días)
- **Plazo**: Semana 5-6
- **Entregable**: Dashboard mejorado e intuitivo

### HUs P2
10. 🔲 Frontend: Calendario Funcional
11. ✅ Frontend: AlertCenter con Datos Reales
12. 🔲 Backend: GET Historial Eventos
13. 🔲 Backend: Auditoría de Cambios
14. 🔲 Frontend: EditorPanel
15. 🔲 Frontend: Integrar react-calendar
18. 🔲 Frontend: Estadísticas y Reportes

[Detalle completo en P2/INDEX.md](./P2/INDEX.md)

---

## 🟢 Prioridad P3 (BAJO)

**Carpeta**: `docs/stories/P3/`

Historias que **optimizan y escalan** el sistema.

- **Estimación total**: 15-25 horas (2-3 días)
- **Plazo**: Semana 6+
- **Entregable**: Sistema production-ready

### HUs P3

1. ✅ HU#16: SNS/SQS Integration
2. ✅ HU#17: OpenAPI/Swagger
3. ✅ HU#20: DELETE Potrero (Soft Delete)
4. ✅ HU#21: Exportar CSV/Excel
5. ✅ HU#19: Soporte Multi-tenant
6. ✅ HU#22: Modo Oscuro
7. ✅ HU#24: Tests E2E (Cypress)
8. ✅ HU#23: Caching Distribuido (Redis)
9. ✅ HU#25: Responsive Mobile

**Completadas**: 9/9 (100%) ✅✅✅
[Detalle completo en P3/INDEX.md](./P3/INDEX.md)

---

## 📚 Documentación Transversal ✅

**Historias de documentación y especificaciones técnicas**:

### Completadas ✅

- **DOCS-OpenAPI-Swagger.md** [3-4h] ✅
  - Swagger UI en `/swagger-ui.html`
  - OpenAPI 3.0 auto-generado
  - Todos los endpoints documentados
  - Ejemplos request/response
  - Errores y validaciones
  - DTOs con @Schema annotations
  - [Ver especificación](./DOCS-OpenAPI-Swagger.md)

### Próximas 🔲

- API Guidelines Doc
- Rate Limiting Docs
- Security Best Practices
- Development Setup Guide

---

## 📊 Estado General del Proyecto

```
Total HUs: 25
Completas: 25 ✅✅✅✅✅
Pendientes: 0 🔲

Desglose por prioridad:
├─ P0 (Crítico):   3/3 completadas  ✅✅✅      [12-17h escritas]
├─ P1 (Alto):      6/6 completadas  ✅✅✅✅✅✅ [19-24h, 0h falta]
├─ P2 (Medio):     7/7 completadas  ✅✅✅✅✅✅✅ [32-35h, 0h falta]
└─ P3 (Bajo):      9/9 completadas  ✅✅✅✅✅✅✅✅✅ [33-43h, 0h falta]

🎯 PROYECTO 100% COMPLETADO: 25/25 HUs

Estimación total: 76-112 horas (10-16 días)
Completadas: 120-135 horas ✅
Faltante: 0 horas ✅✅✅
```

---

## 🚀 Cómo Usar Esta Documentación

### Para Developer (Implementación)

1. **Lee el INDEX de tu prioridad**
   ```
   Ejemplo: P0/INDEX.md para HUs críticas
   ```

2. **Lee la HU completa que vas a implementar**
   ```
   Ejemplo: P0/PASTURES-HU-001-post-eventos.md
   ```

3. **Sigue la especificación técnica** (DTOs, endpoints, cambios)

4. **Implementa según los pasos** (paso a paso en la HU)

5. **Escribe tests** (unitarios, integración, BDD)

6. **Code review** (2 approvals mínimo)

7. **Merge y deploy** a develop/staging

### Para Product Owner (Planificación)

1. **Lee el README general** (este archivo)

2. **Revisa P0/INDEX.md** para entender MVP

3. **Coordina con Team Lead** timeline y recursos

4. **Valida specs de nuevas HUs** antes de escribirlas

5. **Acepta historias completadas** en staging

### Para Tech Lead (Arquitectura)

1. **Lee todas las HUs** en tu prioridad

2. **Valida **especificación técnica** vs arquitectura

3. **Aprueba design reviews** antes de implementar

4. **Realiza code reviews** en PRs

5. **Coordina dependencias** entre HUs

### Para QA (Testing)

1. **Lee INDEX de prioridad** para entender scope

2. **Lee AC (Criterios de Aceptación)** en cada HU

3. **Prepara test cases** (antes de development)

4. **Test manual** en staging post-merge

5. **Reporta bugs** en JIRA linkeado a HU

---

## 📝 Template de Nueva HU

Cuando escribas una nueva HU (P1, P2, P3):

```markdown
# 🌱 MODULO-HU-{n}: {Título}

**Fecha**: 2026-01-XX | **Versión**: 1.0 | **Prioridad**: [🔴 P0 / 🟠 P1 / 🟡 P2 / 🟢 P3]

## 📝 Descripción de la Historia
Como [rol], quiero [acción], para [beneficio]

## 🎯 Criterios de Aceptación
### AC#1: [Descripción]
\`\`\`gherkin
Scenario: [Título]
  Given [estado inicial]
  When [acción]
  Then [resultado esperado]
\`\`\`

## 📊 Especificación Técnica
[DTOs, endpoints, cambios de código]

## 🛠️ Componentes a Crear/Modificar
[lista detallada]

## 📋 Lógica de Implementación
[paso a paso]

## 🧪 Casos de Prueba
[unitarios, integración, BDD]

## 📚 Referencias y Dependencias
[otras HUs, documentación]

## ✅ Definición de Completado
- [ ] Código implementado
- [ ] Tests >= 80% coverage
- [ ] Code review aprobado
- [ ] Documentación actualizada
- [ ] CI/CD green
- [ ] Demostrable en staging
```

---

## 🔗 Estructura de Referencias

**Desde una HU, referenciar**:
- ✅ Otras HUs: `[HU#1](../P0/PASTURES-HU-001-post-eventos.md)`
- ✅ Documentación: `[Pastures Overview](../../pastures/pastures-overview.md)`
- ✅ Arquitectura: `[Architecture Index](../../architecture/index.md)`

---

## 📋 Checklist para Escribir Nueva HU

```
☐ Nombre de archivo: {MODULO}-HU-{n}-{slug}.md
☐ Carpeta correcta: P0, P1, P2 o P3
☐ Descripción clara (qué, quién, por qué)
☐ Mínimo 5 Criterios de Aceptación
☐ Formato Gherkin en ACs (Given-When-Then)
☐ Especificación técnica completa
☐ 3-5 casos de prueba
☐ Pseudocódigo o código de referencia
☐ Referencias a dependencias
☐ Definición de Completado con checklist
☐ Hipervínculos a otras HUs/docs
```

---

## 🎯 Roadmap Recomendado

```
Semana 1-2:  P0 (3 HUs)    → MVP Básico
Semana 3-4:  P1 (6 HUs)    → Robustez + Testing
Semana 5-6:  P2 (7 HUs)    → UX + Features
Semana 7+:   P3 (9 HUs)    → Escala + Performance

Proyección: 10-16 días de desarrollo (2-3 sprints)
```

---

## 📞 Contacto por Tema

**Para preguntas sobre**:
- **HUs P0-P3**: Revisar correspondiente INDEX.md
- **Especificación técnica**: Leer sección de cada HU
- **Dependencias**: Ver diagrama de dependencias en INDEX.md
- **Arquitectura**: [Architecture Index](../architecture/index.md)
- **Datos**: [Analysis Table Design](../analysis-table-design.md)

---

**Generado**: 2026-01-09 | **Versión**: 1.0

Last updated: 2026-01-09
