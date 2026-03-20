# 📚 Documentación - Cattle Backend (Lambda Function)

**Última actualización**: 2026-02-03  
**Proyecto**: cattle-lambda-function  
**Stack**: Java 21 + Spring Boot 3.4.5 + AWS Lambda + DynamoDB

---

## 📁 Estructura de Documentación

```
docs/
├── README.md                         # Este archivo - Índice principal
│
├── arquitectura/                     # 🏗️ Arquitectura del sistema
│   ├── index.md                      # GPS arquitectónico principal
│   ├── architecture-cattle-lambda-function.md  # Detalle del componente backend
│   ├── eventos/                      # Sistema de eventos
│   │   ├── index.md
│   │   ├── events-overview.md
│   │   ├── sealed-interface-pattern.md
│   │   ├── generic-events-builder.md
│   │   └── entity-patch-pattern.md
│   ├── chatbot/                      # Arquitectura Bedrock Chatbot
│   │   ├── ARCHITECTURE.md
│   │   ├── architecture-diagram.puml
│   │   └── GUIA-INTEGRACION-CHATBOT-DYNAMODB.md
│   └── diagramas/                    # Diagramas PlantUML generales
│
├── modelo-negocio/                   # 📊 Flujos y reglas de negocio
│   ├── index.md                      # Índice de flujos
│   ├── flujo-bovineIdentityItems.md              # Ciclo de vida bovino
│   ├── flujo-pastures.md             # Gestión de potreros
│   ├── flujo-milking.md              # Producción lechera
│   ├── bovineIdentityItems/                      # Detalle módulo bovinos
│   │   ├── index.md
│   │   ├── bovineIdentityItems-overview.md
│   │   ├── components-frontend.md
│   │   └── implementation-guide.md
│   ├── pastures/                     # Detalle módulo potreros
│   │   ├── index.md
│   │   ├── pastures-overview.md
│   │   ├── events-architecture.md
│   │   ├── components-frontend.md
│   │   ├── implementation-guide.md
│   │   └── tasks-pending.md
│   └── milking/                      # Detalle módulo lactancia
│       ├── index.md
│       ├── milking-overview.md
│       ├── components-frontend.md
│       └── implementation-guide.md
│
├── modelos-datos/                    # 🗄️ Diseño de datos
│   ├── analysis-table-design.md      # Diseño de tablas DynamoDB
│   └── lifecycle-model.md            # Modelo ciclo de vida bovino
│
├── bases-conocimiento/               # 🧠 Knowledge Base para IA
│   ├── cattle.csv                    # Datos consolidados
│   └── knowledge-base/               # CSVs temáticos
│       ├── 01_kb_sanidad.csv
│       ├── 02_kb_nutricion.csv
│       ├── 03_kb_pasticultura.csv
│       ├── 04_kb_potreros_rotacion.csv
│       ├── 05_kb_normativas.csv
│       ├── 06_kb_faq_app.csv
│       └── 07_kb_lecciones_aprendidas.csv
│
├── estandares-codigo/                # 📏 Estándares de desarrollo
│   ├── index.md
│   ├── backend-standards.md
│   └── frontend-standards.md
│
├── dod-pivotes/                      # ✅ Definition of Done
│   ├── index.md
│   ├── guia-verificacion.md
│   └── pivotes-por-tipo.md
│
├── stories/                          # 📋 Historias de usuario
│   ├── README.md                     # Índice y estado de HUs
│   ├── bedrock/                      # HUs del chatbot
│   │   ├── HU-BEDROCK-001-IMPLEMENTACION.md
│   │   ├── HU-BEDROCK-002-TESTING.md
│   │   ├── HU-BEDROCK-003-SEGURIDAD.md
│   │   ├── HU-BEDROCK-004-DOCUMENTACION.md
│   │   └── HU-BEDROCK-AGENT-001-knowledge-base.md
│   ├── pastures/                     # HUs de potreros
│   │   ├── HU-INDEX.md
│   │   └── HU-PASTURES-001-backend-post-eventos.md
│   └── estimaciones/                 # Estimaciones técnicas
│       ├── ESTIMACIONES-TECNICAS-25-HU.md
│       ├── MATRIZ-ASIGNACION-RECURSOS.md
│       ├── MATRIZ-COMPLEJIDAD-DETALLADA.md
│       └── QUICK-REFERENCE-ESTIMACIONES.md
│
└── changelog/                        # 📝 Registro de cambios
    └── MIGRATION-BEDROCK-COMPLETED.md
```

---

## 🎯 Guía Rápida por Rol

### 🆕 Desarrollador Nuevo
1. Leer [arquitectura/index.md](arquitectura/index.md) - Visión general del sistema
2. Revisar [arquitectura/architecture-cattle-lambda-function.md](arquitectura/architecture-cattle-lambda-function.md) - Estructura del código
3. Consultar [estandares-codigo/](estandares-codigo/) - Convenciones obligatorias

### 👨‍💻 Backend Developer (Java)
1. [arquitectura/architecture-cattle-lambda-function.md](arquitectura/architecture-cattle-lambda-function.md) - Capas y patrones
2. [arquitectura/eventos/](arquitectura/eventos/) - Sistema de eventos
3. [modelo-negocio/](modelo-negocio/) - Lógica de negocio por módulo
4. [modelos-datos/](modelos-datos/) - Diseño DynamoDB

### 🤖 AI/ML Developer
1. [arquitectura/chatbot/](arquitectura/chatbot/) - Integración Bedrock
2. [bases-conocimiento/](bases-conocimiento/) - Knowledge base
3. [stories/bedrock/](stories/bedrock/) - HUs de chatbot

### 📊 Product Owner / Analista
1. [modelo-negocio/](modelo-negocio/) - Flujos de negocio documentados
2. [stories/](stories/) - Backlog de historias

### 🔧 DevOps / SRE
1. Ver `README.md` en raíz del proyecto - Comandos de build/deploy
2. [arquitectura/chatbot/](arquitectura/chatbot/) - Políticas IAM

---

## 🔗 Documentación Relacionada

- **Frontend**: Ver `cattle-front/docs/`
- **Estándares Generales**: Ver `docs/architecture/coding-standards.md` (raíz del monorepo)

---

## 📝 Convenciones

- Diagramas: PlantUML (.puml) o Mermaid (embebido en markdown)
- Historias de usuario: `HU-MODULO-NNN-descripcion.md`
- Índices: Cada carpeta tiene un `index.md` navegable
- Fechas: Formato ISO 8601 (YYYY-MM-DD)

