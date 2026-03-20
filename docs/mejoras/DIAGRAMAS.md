# 🎯 Diagramas de Arquitectura: Antes vs Después

## 1. Estado Actual - Acoplamiento Problemático

```
┌─────────────────────────────────────────────────────────────┐
│                    CATTLE-LAMBDA-FUNCTION                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              CONTROLLERS (REST)                         │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  BovineController      MilkingController   PastureCtrl  │ │
│  │  - 5 inyecciones       - 3 inyecciones     - 2 inject   │ │
│  └────────────────────────────────────────────────────────┘ │
│            ↑                  ↑                    ↑          │
│            │                  │                    │          │
│  ┌─────────┴──────────────────┴────────────────────┴────────┐ │
│  │            PROCESADORES (Orquestación)                    │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  BovineProcessor  MilkingProcessor  RotationPlan...    │ │
│  └────────────────────────────────────────────────────────┘ │
│            ↑                  ↑                    ↑          │
│            │                  │                    │          │
│  ┌─────────┴──────────────────┴────────────────────┴────────┐ │
│  │         SERVICIOS & QUERY SERVICES (Lógica)             │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  BovineService        MilkingService    PastureService  │ │
│  │  BovineQueryService   MilkingQueryServ  PastureQuerySrv │ │
│  │  BovineSummaryService                                   │ │
│  └────────────────────────────────────────────────────────┘ │
│            ↑                  ↑                    ↑          │
│            │                  │                    │          │
│  ┌─────────┴──────────────────┴────────────────────┴────────┐ │
│  │      REPOSITORIES (Acceso a Datos)                       │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  BovineRepository  MilkingRepository  PastureRepository  │ │
│  │  BovineSummaryRepository             Others...           │ │
│  └────────────────────────────────────────────────────────┘ │
│                          ↓                                    │
│                    ┌──────────────┐                          │
│                    │   DynamoDB   │                          │
│                    └──────────────┘                          │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │       ❌ PROBLEMA: ChatbotController                    │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  Inyecta:                                               │ │
│  │    - BovineQueryService      (Dominio Bovinos)         │ │
│  │    - MilkingQueryService     (Dominio Ordeño)          │ │
│  │    - PastureQueryService     (Dominio Potreros)        │ │
│  │    - ContextBuilderService   (Orquestador)             │ │
│  │    - ChatbotService          (Logica Chatbot)          │ │
│  │    - KnowledgeBaseService    (Knowledge Base)          │ │
│  │                                                          │ │
│  │  ❌ Acoplamiento: 3 dominios de negocio + 3 servicios   │ │
│  │  ❌ Si cambias BovineQueryService, afecta Chatbot       │ │
│  │  ❌ No se puede mover Chatbot a Lambda sin duplicar      │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Después Fase 1: Abstracciones

```
┌─────────────────────────────────────────────────────────────┐
│                    CATTLE-LAMBDA-FUNCTION                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              CONTROLLERS (REST)                         │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  BovineController      MilkingController   PastureCtrl  │ │
│  │  - 5 inyecciones       - 3 inyecciones     - 2 inject   │ │
│  │                    ChatbotController                    │ │
│  │                    - 1 inyección (ContextBuilder)       │ │
│  └────────────────────────────────────────────────────────┘ │
│                          ↑                                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │    ✅ ABSTRACCIONES (Nuevas interfaces)                 │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │                                                          │ │
│  │      <<interface>>                                       │ │
│  │    DomainQueryService<T>                                │ │
│  │    ├─ buildContext(String farmId)                       │ │
│  │    └─ buildContextByIntent(String farmId, Intent)       │ │
│  │                                                          │ │
│  │       ↑           ↑            ↑                         │ │
│  │       │           │            │                         │ │
│  │   (impl)      (impl)       (impl)                        │ │
│  │       │           │            │                         │ │
│  └───────┼───────────┼────────────┼────────────────────────┘ │
│          │           │            │                          │
│          ↓           ↓            ↓                          │
│  ┌──────────────┬──────────────┬──────────────────────────┐ │
│  │BovineQuerySr │MilkingQuerySr│PastureQueryService      │ │
│  │implements    │implements    │implements               │ │
│  │DomainQuery   │DomainQuery   │DomainQueryService       │ │
│  │Service<Bov>  │Service<Milk> │Service<Pasture>         │ │
│  └──────────────┴──────────────┴──────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  ✅ REFACTORIZADO: ContextBuilderService               │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │                                                          │ │
│  │  Map<QueryIntent, DomainQueryService<?>> queryServices  │ │
│  │                                                          │ │
│  │  public String buildContext(Intent intent, String fId) {│
│  │    DomainQueryService<?> service =                      │ │
│  │        queryServices.get(intent);                       │ │
│  │                                                          │ │
│  │    List<?> data = service.buildContextByIntent(...);    │ │
│  │    return formatContext(data);                          │ │
│  │  }                                                       │ │
│  │                                                          │ │
│  │  ✅ Agnóstico a implementaciones concretas              │ │
│  │  ✅ 66% menos código que antes                          │ │
│  │  ✅ Agregar nuevo QueryService = 1 línea               │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Después Fase 2: Facades por Dominio

```
┌─────────────────────────────────────────────────────────────────┐
│                    CATTLE-LAMBDA-FUNCTION                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              CONTROLLERS (REST)                           │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │                                                           │  │
│  │  BovineController  MilkingController  PastureController  │  │
│  │  @Autowired        @Autowired         @Autowired         │  │
│  │  BovineFacade      MilkingFacade      PastureFacade      │  │
│  │                                                           │  │
│  │  ChatbotController                                        │  │
│  │  @Autowired                                               │  │
│  │  ContextBuilderService                                   │  │
│  │                                                           │  │
│  │  ✅ 1 inyección = 1 dominio                              │  │
│  │  ✅ Controllers simples y enfocados                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│         ↓              ↓               ↓                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │          ✅ FACADES (Nuevos - Punto único)               │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │                                                           │  │
│  │  ┌─────────────────┬─────────────────┬─────────────────┐ │
│  │  │ BovineFacade    │MilkingFacade    │PastureFacade    │ │
│  │  ├─────────────────┼─────────────────┼─────────────────┤ │
│  │  │ - getAllBovines │- getMilkingByPk │- getPastures    │ │
│  │  │ - getBovineById │- saveMilking    │- getStatus      │ │
│  │  │ - createBovine  │- getMilkingByLct│- updatePasture  │ │
│  │  │ - updateBovine  │- getContextFor  │- validatePast   │ │
│  │  │ - deleteBovine  │  Chatbot        │                 │ │
│  │  │ - getSummary    │                 │                 │ │
│  │  │ - getContextFor │                 │                 │ │
│  │  │   Chatbot       │                 │                 │ │
│  │  │ - validateBovin │                 │                 │ │
│  │  │                 │                 │                 │ │
│  │  │ Encapsula:      │Encapsula:       │Encapsula:       │ │
│  │  │  - Processor    │ - Processor     │ - Processor     │ │
│  │  │  - Service      │ - Service       │ - Service       │ │
│  │  │  - QueryService │ - QueryService  │ - QueryService  │ │
│  │  │  - Repository   │ - Repository    │ - Repository    │ │
│  │  │  - Summary Srv  │                 │                 │ │
│  │  └─────────────────┴─────────────────┴─────────────────┘ │
│  │                                                           │  │
│  │  ✅ Controllers no conocen Processor/Service/Repository  │  │
│  │  ✅ Cambios internos no afectan Controller               │  │
│  │  ✅ Fácil agregar auditoría/rate-limit en Facade        │  │
│  └──────────────────────────────────────────────────────────┘  │
│         ↓              ↓               ↓                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │       SERVICIOS, PROCESADORES, REPOSITORIOS              │  │
│  │       (Misma estructura que antes, pero ocultos en Facade) │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. Después Fase 3: Separación Física por Dominio

```
┌──────────────────────────────────────────────────────────────────┐
│                    CATTLE-LAMBDA-FUNCTION                        │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                  src/main/java/com/cattle/                 │ │
│  ├─────────────────────────────────────────────────────────────┤ │
│  │                                                             │ │
│  │  ┌───────────────────────────────────────────────────────┐ │ │
│  │  │  SHARED (Código compartido entre dominios)            │ │ │
│  │  ├───────────────────────────────────────────────────────┤ │ │
│  │  │  ├─ config/                                           │ │ │
│  │  │  │   ├─ LambdaContext.java                           │ │ │
│  │  │  │   ├─ RepositoryConfig.java                        │ │ │
│  │  │  │   └─ CorsConfig.java                              │ │ │
│  │  │  ├─ security/                                         │ │ │
│  │  │  │   └─ [Clases de seguridad]                        │ │ │
│  │  │  ├─ abstractions/                                     │ │ │
│  │  │  │   ├─ DomainQueryService.java                      │ │ │
│  │  │  │   └─ DomainFacade.java                            │ │ │
│  │  │  ├─ events/                                           │ │ │
│  │  │  │   └─ [Domain events]                              │ │ │
│  │  │  └─ exceptions/                                       │ │ │
│  │  │      └─ [Excepciones comunes]                         │ │ │
│  │  └───────────────────────────────────────────────────────┘ │ │
│  │                                                             │ │
│  │  ┌───────────────────┬───────────────────┬───────────────┐ │ │
│  │  │                   │                   │               │ │ │
│  │  ▼                   ▼                   ▼               │ │ │
│  │  ┌────────────────┐ ┌────────────────┐ ┌─────────────┐ │ │ │
│  │  │ domains/       │ │ domains/       │ │ domains/    │ │ │ │
│  │  │  bovineIdentityItem/       │ │  milking/      │ │  pasture/   │ │ │ │
│  │  ├────────────────┤ ├────────────────┤ ├─────────────┤ │ │ │
│  │  │ ├─ controller/ │ │ ├─ controller/ │ │├─controller/│ │ │ │
│  │  │ │  ├─ Bovine   │ │ │  ├─ Milking  │ ││├─Pasture   │ │ │ │
│  │  │ │  │  Controller│ │ │  │  Controller││ │Controller │ │ │ │
│  │  │ │  └─ ...      │ │ │  └─ ...      │ ││└─ ...      │ │ │ │
│  │  │ ├─ service/    │ │ ├─ service/    │ ││├─ service/ │ │ │ │
│  │  │ │  ├─ Bovine   │ │ │  ├─ Milking  │ ││├─ Pasture  │ │ │ │
│  │  │ │  │  Service  │ │ │  │  Service  │ ││ Service    │ │ │ │
│  │  │ │  ├─ Bovine   │ │ │  ├─ Milking  │ ││├─ Pasture  │ │ │ │
│  │  │ │  │  QuerySrv │ │ │  │  QuerySrv ││ │QueryService│ │ │ │
│  │  │ │  └─ Bovine   │ │ │  └─ ...      │ ││└─ ...      │ │ │ │
│  │  │ │     Facade   │ │ │             │ ││             │ │ │ │
│  │  │ ├─ processor/  │ │ ├─ processor/  │ ││├─processor/│ │ │ │
│  │  │ │  └─ Bovine   │ │ │  └─ Milking  │ ││└─ Rotation │ │ │ │
│  │  │ │     Processor│ │ │     Processor││  Processor   │ │ │ │
│  │  │ ├─ repository/ │ │ ├─ repository/ │ ││├─repository│ │ │ │
│  │  │ │  ├─ Bovine   │ │ │  ├─ Milking  │ ││├─ Pasture  │ │ │ │
│  │  │ │  │  Repository│ │ │  │  Repository││ Repository  │ │ │ │
│  │  │ │  └─ ...      │ │ │  └─ ...      │ ││└─ ...      │ │ │ │
│  │  │ ├─ dto/        │ │ ├─ dto/        │ ││├─ dto/     │ │ │ │
│  │  │ │  └─ Bovine   │ │ │  └─ Milking  │ ││└─ Pasture  │ │ │ │
│  │  │ │     DTO      │ │ │     DTO      │ ││  DTO       │ │ │ │
│  │  │ ├─ entity/     │ │ ├─ entity/     │ ││├─ entity/  │ │ │ │
│  │  │ │  └─ Bovine   │ │ │  └─ Milking  │ ││└─ Pasture  │ │ │ │
│  │  │ │     Entity   │ │ │     Entity   │ ││  Entity    │ │ │ │
│  │  │ └─ exception/  │ │ └─ exception/  │ ││└─exception/│ │ │ │
│  │  │    └─ Bovine   │ │    └─ Milking  │ ││  └─Pasture │ │ │ │
│  │  │       Exception│ │       Exception││   Exception  │ │ │ │
│  │  └────────────────┘ └────────────────┘ └─────────────┘ │ │ │
│  │                                                             │ │ │
│  │  ┌────────────────────────────────────────────────────┐   │ │ │
│  │  │ domains/chatbot/                                   │   │ │ │
│  │  ├────────────────────────────────────────────────────┤   │ │ │
│  │  │ ├─ controller/                                     │   │ │ │
│  │  │ │  └─ ChatbotController.java                      │   │ │ │
│  │  │ ├─ service/                                        │   │ │ │
│  │  │ │  ├─ ChatbotService.java                         │   │ │ │
│  │  │ │  ├─ ContextBuilderService.java                  │   │ │ │
│  │  │ │  └─ KnowledgeBaseService.java                   │   │ │ │
│  │  │ └─ dto/                                            │   │ │ │
│  │  │    └─ [DTOs del chatbot]                           │   │ │ │
│  │  │                                                    │   │ │ │
│  │  │ ✅ Auto-contenido                                 │   │ │ │
│  │  │ ✅ Sin inyectar servicios de otros dominios      │   │ │ │
│  │  │ ✅ Usa abstracciones de shared/                  │   │ │ │
│  │  └────────────────────────────────────────────────────┘   │ │ │
│  │                                                             │ │ │
│  │  ✅ Cada dominio es independiente                           │ │ │
│  │  ✅ Fácil extraer a Lambda separada (copy folder)           │ │ │
│  │  ✅ Cambios en 1 dominio no afectan otros                   │ │ │
│  └─────────────────────────────────────────────────────────────┘ │ │
│                                                                   │ │
│  ┌─────────────────────────────────────────────────────────────┐ │ │
│  │  Application.java                                           │ │ │
│  │  StreamLambdaHandler.java                                  │ │ │
│  └─────────────────────────────────────────────────────────────┘ │ │
│                                                                   │ │
└──────────────────────────────────────────────────────────────────┘
```

---

## 5. Matriz de Transición

```
                    FASE 1              FASE 2             FASE 3
                (Abstracciones)      (Facades)        (Separación Física)
                
Interfaz        Crea:               Usa:               Usa:
Común           DomainQuerySrv      Facades            Facades (compartidas)
                ✅                  ✅✅               ✅✅✅

Acoplamiento    Chatbot →           Chatbot →          Chatbot →
                1 interfaz          1 interfaz         1 interfaz
                ✅                  ✅✅               ✅✅✅

Controllers     5+ inyecciones      1 inyección        1 inyección
                ❌                  ✅                 ✅

Testabilidad    1 mock              1 mock             1 mock
                ✅                  ✅✅               ✅✅✅

Escalabilidad   Preparada           Lista              Implementada
                para P1             para P2            para Lambda separada
                ✅                  ✅✅               ✅✅✅

Costo Tiempo    2-3 días            5-7 días           10-14 días
                                    (total)            (total)
```

---

## 6. Dependencias Visuales: Antes vs Después

### ❌ ANTES (Problema)
```
ChatbotController
├── BovineQueryService
├── MilkingQueryService
├── PastureQueryService
├── ContextBuilderService
├── ChatbotService
├── KnowledgeBaseService
└── RateLimitingService
    └── InputValidationService

    → 7-8 dependencias directas
    → Cambio en cualquier QueryService afecta Chatbot
    → No escalable
```

### ✅ DESPUÉS FASE 1
```
ChatbotController
└── ContextBuilderService
    └── DomainQueryService (interfaz)
        ├── BovineQueryService (impl)
        ├── MilkingQueryService (impl)
        └── PastureQueryService (impl)

    → 1 dependencia directa (ContextBuilder)
    → ContextBuilder depende de abstracción
    → Cambios en servicios no afectan Chatbot
```

### ✅ DESPUÉS FASE 2
```
ChatbotController
└── ContextBuilderService
    └── [Mismo que Fase 1]

BovineController
└── BovineFacade
    ├── BovineProcessor
    ├── BovineService
    ├── BovineQueryService
    └── BovineRepository

MilkingController
└── MilkingFacade
    ├── MilkingProcessor
    ├── MilkingService
    └── MilkingRepository

    → Controllers simples
    → Lógica encapsulada en Facades
```

### ✅ DESPUÉS FASE 3
```
Dominio Bovino (auto-contenido)
├── BovineController
├── BovineFacade
├── [Processor, Service, Repository, DTO, Entity]
└── Puede ser Lambda separada

Dominio Ordeño (auto-contenido)
├── MilkingController
├── MilkingFacade
├── [Processor, Service, Repository, DTO, Entity]
└── Puede ser Lambda separada

Dominio Chatbot (depende de abstracciones)
├── ChatbotController
└── ContextBuilderService
    └── DomainQueryService (interfaz)

Shared (Común a todos)
├── config/
├── security/
├── abstractions/
└── exceptions/

    → Monolito modular
    → Preparado para microservicios
    → Bajo riesgo de extracción
```

---

**Diagrama actualizado**: Marzo 2026  
**Estado**: Ready for Implementation  
**Siguiente paso**: Planificar Fase 1

