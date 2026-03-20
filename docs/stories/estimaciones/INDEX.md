# P3 - Historias de Baja Prioridad (BAJO)

**Fecha**: 2026-01-09 | **Estado**: 🟢 BAJO - 9 HUs completadas ✅✅✅

---

## 📊 Estado General - P3

```
Total HUs P3: 9
Completas: 9  ✅✅✅
Pendientes: 0 🔲

Desglose:
├─ HU#16 ✅ Backend: SNS/SQS Integration             [5-6h  ] Completada
├─ HU#17 ✅ Backend: Documentación OpenAPI/Swagger   [3-4h  ] Completada
├─ HU#20 ✅ Backend: DELETE Potrero (Soft Delete)    [3-4h  ] Completada
├─ HU#21 ✅ Frontend: Exportar CSV/Excel             [3-4h  ] Completada
├─ HU#19 ✅ Backend: Soporte Multi-tenant            [4-5h  ] Completada
├─ HU#22 ✅ Frontend: Modo Oscuro                    [2-3h  ] Completada
├─ HU#24 ✅ Tests E2E (Cypress/Playwright)           [6-8h  ] Completada
├─ HU#23 ✅ Backend: Caching Distribuido             [4-5h  ] Completada
└─ HU#25 ✅ Frontend: Responsive Mobile              [3-4h  ] Completada

🎯 P3 100% COMPLETADA: 9/9 HUs
```

---

## ✅ HU#16: Backend SNS/SQS Integration
- **Estado**: Completada ✅
- **Archivo**: [PASTURES-HU-016-sns-sqs.md](./PASTURES-HU-016-sns-sqs.md)
- **Descripción**: Integración con AWS SNS/SQS para comunicación asincrónica
- **Horas**: 5-6 horas
- **Prioridad**: 🟢 BAJO (P3)
- **Dependencias**: AWS SDK, Spring Cloud AWS
- **AC**: 15 Criterios de Aceptación
- **Tests**: Unitarios + Integración (LocalStack)
- **Fecha Completada**: 2026-01-09

**Implementación incluye**:
```
Archivos nuevos:
├─ AwsConfiguration.java (configuración AWS)
├─ EventPublisher.java (publicador SNS)
├─ PastureEventListener.java (listener SQS)
├─ EventIdempotencyService.java (idempotencia)
├─ EventPublisherTest.java (tests)
└─ PastureEventListenerTest.java (tests integración)

Archivos a modificar:
├─ pom.xml (AWS SDK)
├─ application.properties (SNS/SQS config)
└─ PastureService.java (inyectar publisher)

Funcionalidades:
├─ SNS topic: pasture-events
├─ SQS queue: pasture-events-queue
├─ DLQ: pasture-events-dlq
├─ Publicación async (no bloqueante)
├─ Listeners para procesar eventos
├─ Reintentos automáticos (3 intentos)
├─ Exponential backoff
├─ Dead Letter Queue para fallos
├─ Idempotencia (no procesar duplicados)
├─ CloudWatch métricas
├─ Logging completo
├─ Testing con LocalStack
├─ Configuración por ambiente
└─ Documentación de arquitectura
```

---

### ✅ HU#17: Backend Documentación OpenAPI/Swagger
- **Estado**: Completada ✅
- **Archivo**: [PASTURES-HU-017-openapi-swagger.md](./PASTURES-HU-017-openapi-swagger.md)
- **Descripción**: Documentación interactiva OpenAPI 3.0 y Swagger UI
- **Horas**: 3-4 horas
- **Prioridad**: 🟢 BAJO (P3)
- **Dependencias**: SpringDoc OpenAPI, Spring Boot
- **AC**: 15 Criterios de Aceptación
- **Tests**: Tests de especificación
- **Fecha Completada**: 2026-01-09

**Implementación incluye**:
```
Archivos nuevos:
├─ OpenAPIConfiguration.java (configuración OpenAPI)
├─ ErrorResponse.java (esquema de errores)
└─ api-documentation.md (doc en Markdown bonus)

Archivos a modificar:
├─ pom.xml (agregar springdoc-openapi)
├─ application.properties (configurar Swagger)
├─ Todos Controllers (@Operation, @ApiResponse, @Tag)
├─ Todos DTOs (@Schema con description)
└─ Todos Request/Response (@Schema)

Funcionalidades:
├─ Swagger UI en /swagger-ui.html
├─ OpenAPI JSON en /v3/api-docs
├─ OpenAPI YAML en /v3/api-docs.yaml
├─ Todos los endpoints documentados
├─ Parámetros y validaciones visibles
├─ Ejemplos request/response
├─ Errores documentados (400, 403, 404, 500)
├─ Tags por categoría (Pastures, Events, Audit)
├─ Autenticación documentada (Bearer JWT)
├─ Try it out funcional
├─ Versionado de API
├─ Sincronización automática con código
└─ Redoc alternativo (bonus)
```

---

### ✅ HU#20: Backend DELETE Potrero (Soft Delete)
- **Estado**: Completada ✅
- **Archivo**: [PASTURES-HU-020-delete-potrero.md](./PASTURES-HU-020-delete-potrero.md)
- **Descripción**: Soft delete de potreros con preservación de datos históricos
- **Horas**: 3-4 horas
- **Prioridad**: 🟢 BAJO (P3)
- **Dependencias**: HU#1 (Backend base), HU#13 (Auditoría)
- **AC**: 15 Criterios de Aceptación
- **Tests**: Unitarios + Integración
- **Fecha Completada**: 2026-01-09

**Implementación incluye**:
```
Archivos nuevos:
├─ PastureControllerDeleteTest.java (tests)
└─ Migration SQL (agregar columnas)

Archivos a modificar:
├─ PastureEntity.java (deletedAt, deletedBy)
├─ PastureStatus.java (agregar REMOVED)
├─ PastureController.java (DELETE, PATCH endpoints)
├─ PastureService.java (lógica soft delete)
└─ PastureRepository.java (queries actualizadas)

Funcionalidades:
├─ DELETE /pastures/{id} (soft delete)
├─ PATCH /pastures/{id}/restore (restaurar)
├─ GET /pastures?includeDeleted=true (con opción)
├─ Status REMOVED para eliminados
├─ Campos deletedAt, deletedBy
├─ Filtrado automático de REMOVED
├─ Verificación de permisos (admin)
├─ Validación de estado (no eliminar EN_USO)
├─ Evento SNS publicado (HU#16)
├─ Auditoría registrada (HU#13)
├─ Preservación de histórico
├─ Integridad referencial
├─ Performance optimizado
├─ Tests >= 85%
└─ Documentación OpenAPI (HU#17)
```

---

### ✅ HU#21: Frontend Exportar CSV/Excel
- **Estado**: Completada ✅
- **Archivo**: [PASTURES-HU-021-export-csv-excel.md](./PASTURES-HU-021-export-csv-excel.md)
- **Descripción**: Exportar datos de potreros a CSV y Excel
- **Horas**: 3-4 horas
- **Prioridad**: 🟢 BAJO (P3)
- **Dependencias**: Frontend, PaddockPage
- **AC**: 15 Criterios de Aceptación
- **Tests**: Unitarios + Integración
- **Fecha Completada**: 2026-01-09

**Implementación incluye**:
```
Archivos nuevos:
├─ ExportButton.jsx (componente)
├─ exportService.js (servicio)
├─ export.css (estilos)
└─ ExportButton.test.jsx (tests)

Archivos a modificar:
├─ package.json (papaparse, xlsx, file-saver)
├─ PaddockPage.jsx (integrar ExportButton)
└─ SearchBar.jsx (agregar botones)

Librerías:
├─ papaparse (CSV)
├─ xlsx (Excel .xlsx)
└─ file-saver (descargas)

Funcionalidades:
├─ Botón "Descargar CSV"
├─ Botón "Descargar Excel"
├─ Respetar filtros aplicados
├─ Nombre archivo: {name}-{date}.{ext}
├─ Encabezado con metadatos
├─ Formateo profesional Excel
├─ Ancho de columnas automático
├─ Caracteres especiales UTF-8
├─ Performance optimizado
├─ Móvil compatible
├─ Accesibilidad ARIA
├─ Spinner mientras se genera
├─ Sin bloqueo de UI
└─ Tests >= 80%
```

---

### ✅ HU#19: Backend Soporte Multi-tenant
- **Estado**: Completada ✅
- **Archivo**: [PASTURES-HU-019-multitenant.md](./PASTURES-HU-019-multitenant.md)
- **Descripción**: Aislar datos por tenant (empresa/finca)
- **Horas**: 4-5 horas
- **Prioridad**: 🟢 BAJO (P3)
- **Dependencias**: Backend base, JWT, BD
- **AC**: 15 Criterios de Aceptación
- **Tests**: Unitarios + Integración
- **Fecha Completada**: 2026-01-09

**Implementación incluye**:
```
Archivos nuevos:
├─ TenantContext.java (contexto)
├─ TenantInterceptor.java (interceptor HTTP)
├─ TenantRepositoryAspect.java (AOP)
├─ TenantEntity.java (clase base)
├─ TenantSpecification.java (specs JPA)
└─ TenantControllerTest.java (tests)

Archivos a modificar:
├─ JwtTokenProvider.java (incluir tenant)
├─ Todos los repositories (filtros)
├─ Todas las entities (heredar TenantEntity)
├─ Todos los controllers (usar TenantContext)
├─ WebConfig.java (registrar interceptor)
└─ Migration SQL (agregar columnas)

Funcionalidades:
├─ TenantContext para request scope
├─ JWT con tenant info
├─ Interceptor para extraer tenant
├─ Filtrado automático de queries
├─ Prevención de acceso cruzado
├─ Cambio de tenant activo
├─ Entity auditable por tenant
├─ Repositories filtran automáticamente
├─ Transacciones validadas por tenant
├─ Configuración por tenant
├─ Datos global vs tenant
├─ Escalabilidad horizontal
├─ Performance optimizado
├─ Índices de BD
└─ Tests >= 85%
```

---

### ✅ HU#22: Frontend Modo Oscuro (Dark Theme)
- **Estado**: Completada ✅
- **Archivo**: [PASTURES-HU-022-dark-mode.md](./PASTURES-HU-022-dark-mode.md)
- **Descripción**: Tema oscuro profesional con CSS variables
- **Horas**: 2-3 horas
- **Prioridad**: 🟢 BAJO (P3)
- **Dependencias**: Frontend, React
- **AC**: 15 Criterios de Aceptación
- **Tests**: Unitarios + Integración
- **Fecha Completada**: 2026-01-09

**Implementación incluye**:
```
Archivos nuevos:
├─ ThemeContext.js (context global)
├─ ThemeToggle.jsx (botón)
├─ ThemeToggle.css (estilos)
├─ themes.css (variables CSS)
└─ ThemeToggle.test.jsx (tests)

Archivos a modificar:
├─ App.jsx (ThemeProvider)
├─ DashboardLayout.jsx (agregar toggle)
├─ tailwind.config.js (darkMode: class)
├─ index.css o App.css (usar variables)
└─ Todos componentes (usar var--)

Funcionalidades:
├─ Toggle de tema (🌙/☀️)
├─ Detectar preferencia SO
├─ localStorage persistencia
├─ CSS variables (@media dark)
├─ Transiciones suaves (0.3s)
├─ Contraste WCAG AA
├─ Color scheme ajustado
├─ Componentes soportan dark
├─ Cards formateadas
├─ Botones coloreados
├─ Inputs visible
├─ Gráficos adaptados
├─ Imágenes ajustadas
├─ Móvil responsive
├─ Sin parpadeos
└─ Tests >= 80%
```

---

### ✅ HU#24: Tests E2E (Cypress/Playwright)
- **Estado**: Completada ✅
- **Archivo**: [PASTURES-HU-024-e2e-tests.md](./PASTURES-HU-024-e2e-tests.md)
- **Descripción**: Tests end-to-end automatizados con Cypress
- **Horas**: 6-8 horas
- **Prioridad**: 🟢 BAJO (P3)
- **Dependencias**: Frontend, Backend, BD test
- **AC**: 15 Criterios de Aceptación
- **Tests**: +20 tests E2E
- **Fecha Completada**: 2026-01-09

**Implementación incluye**:
```
Archivos nuevos:
├─ cypress.config.js (configuración)
├─ cypress/e2e/auth/login.cy.js (login tests)
├─ cypress/e2e/pastures/create.cy.js (CRUD tests)
├─ cypress/e2e/pastures/filtering.cy.js (filtros)
├─ cypress/support/commands.js (comandos)
├─ cypress/support/e2e.js (setup global)
└─ .github/workflows/e2e.yml (CI/CD)

Librerías:
├─ cypress (testing)
├─ cypress-real-events (interacciones)
└─ @cypress/code-coverage (coverage)

Funcionalidades:
├─ Tests de autenticación
├─ Tests CRUD (Create/Read/Update/Delete)
├─ Tests de validación
├─ Tests de filtros/búsqueda
├─ Tests de errores
├─ Tests responsive (3 tamaños)
├─ Tests multi-browser (Chrome, Firefox)
├─ Comandos personalizados (login, createPasture)
├─ Mocks de API
├─ Videos en fallos
├─ Screenshots automáticos
├─ Reportes HTML
├─ CI/CD en GitHub Actions
├─ Multi-browser en CI
└─ +20 tests implementados
```

---

### ✅ HU#23: Backend Caching Distribuido (Redis/Local)
- **Estado**: Completada ✅
- **Archivo**: [PASTURES-HU-023-caching-redis.md](./PASTURES-HU-023-caching-redis.md)
- **Descripción**: Caching con Redis (prod) y local (dev)
- **Horas**: 4-5 horas
- **Prioridad**: 🟢 BAJO (P3)
- **Dependencias**: Spring Boot, Redis
- **AC**: 15 Criterios de Aceptación
- **Tests**: Unitarios + Integración
- **Fecha Completada**: 2026-01-09

**Implementación incluye**:
```
Archivos nuevos:
├─ CacheConfiguration.java (setup)
├─ CacheMetricsController.java (monitoreo)
├─ CacheStats.java (DTO)
├─ CacheServiceTest.java (tests)
└─ docker-compose.yml (actualizado)

Archivos a modificar:
├─ pom.xml (dependencias)
├─ application.properties (Redis)
├─ application-dev.properties (cache local)
├─ PastureService.java (@Cacheable)
└─ Otros services (caché donde aplique)

Funcionalidades:
├─ Redis en producción
├─ Cache local en desarrollo
├─ @Cacheable para listados
├─ @Cacheable para items
├─ @CacheEvict al actualizar
├─ @CacheEvict al eliminar
├─ TTL configurable (5-10 min)
├─ Cache warming (bonus)
├─ Monitoreo hit/miss
├─ Admin endpoint para limpiar
├─ Métricas Prometheus
├─ Multi-instancia (Redis distribuido)
├─ Testing completo
├─ 10x menos latencia
└─ Performance optimizado
```

---

### ✅ HU#25: Frontend Responsive Design Móvil
- **Estado**: Completada ✅
- **Archivo**: [PASTURES-HU-025-responsive-mobile.md](./PASTURES-HU-025-responsive-mobile.md)
- **Descripción**: Diseño responsivo mobile-first
- **Horas**: 3-4 horas
- **Prioridad**: 🟢 BAJO (P3)
- **Dependencias**: Frontend, Tailwind
- **AC**: 15 Criterios de Aceptación
- **Tests**: Responsive + Lighthouse
- **Fecha Completada**: 2026-01-09

**Implementación incluye**:
```
Archivos nuevos:
├─ index.html (viewport)
├─ Container.jsx (responsivo)
├─ LazyImage.jsx (lazy loading)
└─ ResponsiveImage.jsx (picture tags)

Archivos a modificar:
├─ tailwind.config.js (breakpoints)
├─ index.css (mobile-first)
├─ Navbar.jsx (hamburger menu)
├─ Card.jsx (padding responsivo)
├─ Table.jsx (cards en móvil)
└─ Todos los componentes

Funcionalidades:
├─ Viewport meta tag
├─ Mobile-first design
├─ Breakpoints: xs/sm/md/lg/xl
├─ Botones tocables (44px+)
├─ Hamburger menu en móvil
├─ Imágenes responsivas
├─ Lazy loading (native)
├─ Tablas → cards en móvil
├─ Formularios usables
├─ Touch-friendly interactions
├─ Font >= 16px en móvil
├─ Orientación portrait/landscape
├─ Teclado móvil integrado
├─ Performance optimizado
└─ Lighthouse score >= 90
```

---

## 🎯 P3 COMPLETADA: 9/9 HUs (100%)

✅ **Todas las historias de P3 están COMPLETAS**

```
HUs Completadas:
├─ HU#16 ✅ SNS/SQS (5-6h)
├─ HU#17 ✅ OpenAPI/Swagger (3-4h)
├─ HU#20 ✅ DELETE Soft Delete (3-4h)
├─ HU#21 ✅ Export CSV/Excel (3-4h)
├─ HU#19 ✅ Multi-tenant (4-5h)
├─ HU#22 ✅ Dark Mode (2-3h)
├─ HU#24 ✅ E2E Tests (6-8h)
├─ HU#23 ✅ Caching Redis (4-5h)
└─ HU#25 ✅ Responsive Mobile (3-4h)

Horas totales P3: 33-43 horas
Completadas: 100% de P3
```

---

## 🔲 HU#19: Backend Soporte Multi-tenant
- **Estado**: Pendiente 🔲
- **Descripción**: Aislar datos por empresa/finca
- **Horas**: 4-5 horas
- **Próximos pasos**: Escribir HU

---

## 🔲 HU#20: Frontend Exportar CSV/Excel
- **Estado**: Pendiente 🔲
- **Descripción**: Exportar datos a formatos
- **Horas**: 3-4 horas
- **Próximos pasos**: Escribir HU

---

## 🔲 HU#21: Frontend Modo Oscuro
- **Estado**: Pendiente 🔲
- **Descripción**: Tema oscuro en interfaz
- **Horas**: 2-3 horas
- **Próximos pasos**: Escribir HU

---

## 🔲 HU#22: Backend Caching Distribuido
- **Estado**: Pendiente 🔲
- **Descripción**: Redis para caché
- **Horas**: 4-5 horas
- **Próximos pasos**: Escribir HU

---

## 🔲 HU#23: Tests E2E (Cypress/Playwright)
- **Estado**: Pendiente 🔲
- **Descripción**: Tests end-to-end automatizados
- **Horas**: 6-8 horas
- **Próximos pasos**: Escribir HU

---

## 🔲 HU#24: Frontend Responsive Mobile
- **Estado**: Pendiente 🔲
- **Descripción**: Optimización para móvil
- **Horas**: 3-4 horas
- **Próximos pasos**: Escribir HU

---

## 🔲 HU#25: Backend API Rate Limiting
- **Estado**: Pendiente 🔲
- **Descripción**: Limitar rate de requests
- **Horas**: 3-4 horas
- **Próximos pasos**: Escribir HU

---

## 📈 Backlog P3

| # | Historia | Estado | Horas | Prioridad |
|---|----------|--------|-------|-----------|
| 16 | SNS/SQS Integration | ✅ | 5-6 | Media |
| 17 | OpenAPI/Swagger | ✅ | 3-4 | Media |
| 20 | DELETE Soft Delete | ✅ | 3-4 | Media |
| 21 | Export CSV/Excel | ✅ | 3-4 | Baja |
| 19 | Multi-tenant | ✅ | 4-5 | Baja |
| 22 | Dark Mode | ✅ | 2-3 | Baja |
| 24 | E2E Tests | ✅ | 6-8 | Alta |
| 23 | Caching Redis | ✅ | 4-5 | Media |
| 25 | Mobile Responsive | ✅ | 3-4 | Media |

---

**Generado**: 2026-01-09 | **Versión**: 1.0
