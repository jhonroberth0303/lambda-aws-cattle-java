# HU-20260429: Configuración Reutilizable de Precio de Leche por Sitio

**ID**: HU-20260429-configuracion-precio-leche-por-sitio  
**Tipo**: Mejora funcional backend  
**Prioridad**: Media  
**Fecha**: 2026-04-29  
**Estado actual**: Completada - slice backend reusable implementado y validado técnicamente con pruebas focalizadas, compilación y validación SAM local

## Trazabilidad

- HU frontend relacionada: `cattle-front/docs/stories/HU-20260429-panel-ventas-leche.md`
- Dominio funcional relacionado: `milkingProd`
- Dependencia de integración: consumidor frontend del panel de ventas de leche
- Modelo reusable relacionado: `SiteSetting`

## Registro de cambios

| Fecha | Versión | Descripción | Resultado |
|------|---------|-------------|-----------|
| 2026-04-29 | 1.0 | Creación de HU backend para persistir el valor de leche por litro como configuración operativa del sitio | HU abierta |
| 2026-04-29 | 1.1 | Análisis y diseño técnico actualizados para reemplazar el setting puntual por un modelo reutilizable de configuraciones por sitio | Lista para refinamiento |
| 2026-04-29 | 1.2 | Refinamiento técnico actualizado con estrategia de almacenamiento genérico y soporte estructural para historial futuro | Lista para estimación |
| 2026-04-29 | 1.3 | Estimación ajustada al nuevo alcance backend reutilizable | Lista para desarrollo |
| 2026-04-29 | 1.4 | Implementación backend base completada con tabla `TABLE_SITE_SETTINGS`, repositorio reusable, servicio genérico y fachada `milk-price` | Lista para integración frontend |
| 2026-04-29 | 1.5 | Se corrige la definición SAM de `SiteSettingsTable`, se valida localmente el template y queda lista la base para despliegue | Lista para despliegue y prueba integrada |
| 2026-04-29 | 1.6 | El `GET /settings/milk-price` pasa a responder un valor por defecto cuando no existe configuración persistida para evitar `404` en el dashboard | Lista para despliegue y prueba integrada |
| 2026-04-29 | 1.7 | Cierre documental de la HU con semántica final actualizada, evidencia técnica y estado completado | HU completada |

## Contexto de negocio

El dashboard de ordeño necesita calcular un valor económico estimado a partir del acumulado de litros por lactancia activa. Para que ese cálculo sea consistente entre usuarios y sobreviva entre sesiones, el valor de pesos por litro no puede depender del estado local del navegador.

Sin embargo, modelar esto como un setting aislado de `milkPricePerLiter` dejaría abierta una deuda técnica inmediata: en el producto aparecerán otras configuraciones operativas por sitio y, además, el precio de leche probablemente necesitará historial de vigencia en el futuro.

Por eso esta HU debe resolver no solo el caso actual del precio por litro, sino la base de persistencia reutilizable para otras configuraciones del sitio y para futura evolución hacia historial de precios.

## Problema observado

Situación actual:

- el frontend ya puede calcular litros acumulados y total estimado por vaca
- el valor de pesos por litro solo existe en memoria de pantalla
- al recargar la aplicación el valor se pierde
- dos usuarios del mismo sitio podrían trabajar con valores distintos sin saberlo
- el backend no ofrece hoy una estructura genérica de configuraciones por sitio

Evidencia técnica revisada:

- el módulo `milkingProd` ya expone rutas por `siteId` para operación y consulta
- el repositorio usa DynamoDB Enhanced Client con entidades anotadas y repositorios especializados
- `BaseDdbItem` define un patrón reutilizable de `PK`, `SK`, `GSI1PK`, `GSI1SK`, `createdAt`, `updatedAt`
- no se evidenció en `lambda-aws-cattle-java/src/**` una superficie existente de configuración por sitio reutilizable para `milkPricePerLiter`

## Objetivo

Exponer una API mínima de backend para el precio de leche por sitio, pero persistida sobre un modelo genérico de configuraciones que permita:

- consultar el valor vigente de `milkPricePerLiter` por `siteId`
- actualizar el valor vigente de `milkPricePerLiter` por `siteId`
- reutilizar el mismo modelo para futuras configuraciones por sitio
- dejar preparada la estructura para incorporar historial de precios sin romper la base de datos ni el patrón de acceso

## Alcance funcional

Incluye:

- endpoint `GET` para consultar el precio vigente por sitio
- endpoint `PUT` para actualizar el precio vigente por sitio
- modelo de persistencia genérico para configuraciones por sitio
- estructura de datos preparada para historial futuro del precio
- validaciones mínimas de entrada
- pruebas unitarias y de controller del slice

No incluye:

- endpoint de consulta histórica de precios
- reglas de vigencia temporal completas
- versionado transversal expuesto a frontend para otras configuraciones
- integración financiera o facturación
- cálculo económico del dashboard, que sigue en frontend

## Criterios de aceptación

1. Existe un endpoint `GET /site/{siteId}/settings/milk-price` que devuelve el precio vigente persistido del sitio o un valor por defecto si aún no existe configuración previa.
2. Existe un endpoint `PUT /site/{siteId}/settings/milk-price` que actualiza el precio vigente persistido del sitio.
3. El valor persistido se identifica por `siteId` y no por navegador o usuario local.
4. La persistencia backend se implementa sobre un modelo reutilizable de configuraciones por sitio, no como una entidad ad hoc exclusiva de `milk-price`.
5. El backend valida que `milkPricePerLiter` sea numérico y no negativo.
6. La respuesta expone al menos `siteId`, `milkPricePerLiter`, `updatedAt` y `updatedBy` si está disponible.
7. El modelo deja prevista una forma de registrar histórico futuro sin rediseñar la clave principal del setting.
8. La implementación no rompe las rutas actuales del módulo `milkingProd`.
9. Existen pruebas backend que cubren lectura, actualización y validaciones básicas.
10. El consumidor frontend no depende de `404` para inicializar el precio de leche.

## Análisis arquitectónico

### Evidencia revisada

- `BaseDdbItem` muestra un patrón consolidado de modelado DynamoDB con `PK`, `SK`, timestamps y `GSI1`.
- `ProfileLifecycleRepository` y `MilkingRepository` muestran el estilo vigente del proyecto: repositorios especializados, operaciones explícitas y uso de `TableSchema.fromBean(...)`.
- el proyecto ya usa claves compuestas con semántica de dominio en texto, por ejemplo `BOVINE#...`, `PROFILE#...`, `MILKING#...`.
- no existe un repositorio de settings por sitio que hoy pueda absorber este requerimiento sin diseño adicional.

### Arquitectura actual del slice relevante

```text
Controllers de dominio
	-> Processors
		-> Services
			-> Repositories DynamoDB especializados
```

Limitaciones observadas:

- no existe un slice transversal ligero para configuraciones operativas por sitio
- si se crea una entidad exclusiva para `milk-price`, la siguiente configuración volvería a duplicar controller, repositorio y almacenamiento con baja reutilización
- si se guarda solo el valor vigente sin forma estructural de historia, el siguiente requerimiento de precios por vigencia obligará a rediseñar el almacenamiento

### Síntoma, causa raíz e impacto

Síntoma:

- el precio por litro no se comparte entre usuarios del mismo sitio
- el valor se pierde al recargar el frontend
- no hay base backend preparada para configuración operativa reutilizable

Causa raíz:

- no existe una fuente de verdad backend para configuraciones por sitio
- la configuración no está modelada como recurso reutilizable del sitio
- no existe una estrategia base para representar estado vigente e histórico de un setting

Impacto:

- inconsistencia funcional entre sesiones y usuarios
- deuda técnica temprana si el proyecto comienza a agregar más settings operativos
- mayor costo de cambio futuro para soportar historial de precios

### Restricciones y decisiones de contorno

- la HU debe seguir siendo ejecutable como slice backend pequeño, no como un framework completo de configuración corporativa
- se debe conservar el estilo actual de controller, processor y repository del proyecto
- se debe priorizar una estructura que permita crecimiento sin sobrediseñar el primer caso de uso
- no se implementarán en esta historia consultas históricas expuestas a frontend, pero sí la base para soportarlas

## Diseño técnico

### Principio de diseño adoptado

Separar la API específica de negocio del almacenamiento genérico:

- la API expuesta en esta HU sigue siendo específica para `milk-price`
- la persistencia subyacente se modela como `site settings` reutilizables
- la estructura de claves debe poder almacenar un valor vigente y eventos históricos por setting sin romper compatibilidad futura

### Contrato objetivo expuesto al consumidor actual

| Método | Ruta | Propósito |
|--------|------|-----------|
| `GET` | `/site/{siteId}/settings/milk-price` | consultar el valor vigente del precio de leche por litro |
| `PUT` | `/site/{siteId}/settings/milk-price` | actualizar el valor vigente del precio de leche por litro |

#### Semántica HTTP propuesta

| Método | Caso | Respuesta |
|--------|------|-----------|
| `GET` | configuración existente | `200 OK` + body |
| `GET` | configuración inexistente | `200 OK` con valor por defecto explícito |
| `PUT` | actualización válida | `200 OK` + body actualizado |
| `PUT` | payload inválido | `400 Bad Request` |

Para esta HU se recomienda como contrato más simple y explícito:

- `GET` devuelve `200 OK` con `milkPricePerLiter = 0` cuando no existe configuración vigente para el sitio y setting solicitado
- el dashboard consumidor no depende de `404` para inicializar el precio de leche

### DTO propuesto

#### Enum reusable

```text
SiteSettingValueType
- NUMBER
- STRING
- BOOLEAN
- JSON
```

#### DTO reusable interno: `SiteSettingCurrentDTO`

```text
{
	"siteId": "001",
	"settingKey": "MILK_PRICE_PER_LITER",
	"valueType": "NUMBER",
	"valueNumber": 1800,
	"valueString": null,
	"valueBoolean": null,
	"valueJson": null,
	"version": 3,
	"active": true,
	"createdAt": "2026-04-29T16:00:00Z",
	"updatedAt": "2026-04-29T18:45:00Z",
	"updatedBy": "jhonroberth"
}
```

#### DTO reusable interno: `SiteSettingHistoryDTO`

```text
{
	"siteId": "001",
	"settingKey": "MILK_PRICE_PER_LITER",
	"valueType": "NUMBER",
	"valueNumber": 1800,
	"valueString": null,
	"valueBoolean": null,
	"valueJson": null,
	"version": 3,
	"effectiveFrom": "2026-04-29T18:45:00Z",
	"effectiveTo": null,
	"createdAt": "2026-04-29T18:45:00Z",
	"updatedAt": "2026-04-29T18:45:00Z",
	"updatedBy": "jhonroberth",
	"changeReason": "Actualización manual desde dashboard"
}
```

#### Request reusable interno: `UpsertSiteSettingRequest`

```text
{
	"siteId": "001",
	"settingKey": "MILK_PRICE_PER_LITER",
	"valueType": "NUMBER",
	"valueNumber": 1800,
	"valueString": null,
	"valueBoolean": null,
	"valueJson": null,
	"updatedBy": "jhonroberth",
	"changeReason": "Actualización manual desde dashboard"
}
```

#### Response

```text
{
	"siteId": "001",
	"milkPricePerLiter": 1800,
	"updatedAt": "2026-04-29T18:45:00Z",
	"updatedBy": "jhonroberth"
}
```

#### Request de actualización

```text
{
	"milkPricePerLiter": 1800,
	"updatedBy": "jhonroberth"
}
```

#### Mapeo entre contrato público y modelo reusable

| Contrato público | Modelo reusable |
|------------------|-----------------|
| `milkPricePerLiter` | `valueNumber` |
| `siteId` | `siteId` |
| implícito `milk-price` | `settingKey = MILK_PRICE_PER_LITER` |
| `updatedAt` | `updatedAt` |
| `updatedBy` | `updatedBy` |

### Modelo de persistencia reutilizable propuesto

#### Tabla objetivo

Nueva tabla dedicada y reutilizable para settings por sitio:

- `TABLE_SITE_SETTINGS`

Razón:

- evita mezclar configuraciones operativas con entidades de bovinos o registros de ordeño
- permite reutilización por otros módulos sin contaminar tablas existentes
- facilita almacenar valor vigente e histórico en una misma tabla con single-table design ligero

#### Entidad base propuesta: `SiteSettingItem`

Modelo lógico:

```text
PK = SITE#<siteId>
SK = SETTING#<settingKey>#CURRENT

GSI1PK = SETTING#<settingKey>
GSI1SK = SITE#<siteId>

Atributos:
- siteId
- settingKey
- valueType
- valueNumber
- valueString
- valueBoolean
- valueJson
- version
- createdAt
- updatedAt
- updatedBy
- active
```

Uso en esta HU:

- `settingKey = MILK_PRICE_PER_LITER`
- `valueType = NUMBER`
- `valueNumber = 1800`

#### Entidad histórica propuesta: `SiteSettingHistoryItem`

Modelo lógico:

```text
PK = SITE#<siteId>
SK = SETTING#<settingKey>#HISTORY#<effectiveFromOrUpdatedAt>

GSI1PK = SETTING#<settingKey>
GSI1SK = SITE#<siteId>#HISTORY#<effectiveFromOrUpdatedAt>

Atributos:
- siteId
- settingKey
- valueType
- valueNumber
- valueString
- valueBoolean
- valueJson
- version
- effectiveFrom
- effectiveTo
- createdAt
- updatedAt
- updatedBy
- changeReason
```
```

En esta HU no es obligatorio exponer ni consultar historia, pero sí se recomienda que el `PUT` deje el camino abierto para registrar un snapshot histórico sin rediseño de claves.

### Claves exactas propuestas

#### Current

```text
PK  = SITE#001
SK  = SETTING#MILK_PRICE_PER_LITER#CURRENT
GSI1PK = SETTING#MILK_PRICE_PER_LITER
GSI1SK = SITE#001
```

#### History

```text
PK  = SITE#001
SK  = SETTING#MILK_PRICE_PER_LITER#HISTORY#2026-04-29T18:45:00Z
GSI1PK = SETTING#MILK_PRICE_PER_LITER
GSI1SK = SITE#001#HISTORY#2026-04-29T18:45:00Z
```

#### Convenciones recomendadas

- `settingKey` en mayúsculas y formato estable, por ejemplo `MILK_PRICE_PER_LITER`
- timestamps en ISO-8601 UTC
- `version` incremental sobre el item `CURRENT`
- el `HISTORY` replica el valor consolidado de la versión vigente al momento del cambio

### Estrategia mínima de implementación recomendada

Implementación mínima compatible con futuro historial:

1. guardar y leer el item `CURRENT`
2. definir desde ya las convenciones de clave para `HISTORY`
3. registrar en la misma actualización un item histórico por snapshot para dejar listo el camino de historial futuro

Decisión recomendada para esta historia:

- obligatorio: item `CURRENT`
- obligatorio: convenciones y modelo para `HISTORY`
- obligatorio: persistir snapshot histórico en cada `PUT`

### Arquitectura objetivo del slice

```text
MilkPriceSettingsController
	-> MilkPriceSettingsProcessor
		-> SiteSettingService
			-> SiteSettingRepository
			-> SiteSettingHistoryRepository (opcional si se separa física o lógicamente)
```

Relación de responsabilidades:

- `MilkPriceSettingsController`: mantiene el contrato específico `milk-price`
- `MilkPriceSettingsProcessor`: valida request/response y orquesta el caso de uso
- `SiteSettingService`: abstrae lectura/escritura de settings genéricos por sitio
- `SiteSettingRepository`: encapsula la persistencia DynamoDB reusable

### Validaciones mínimas propuestas

| Código | Validación | Resultado esperado |
|--------|------------|-------------------|
| V-01 | `siteId` presente y válido | continuar |
| V-02 | `milkPricePerLiter` numérico | continuar |
| V-03 | `milkPricePerLiter >= 0` | continuar |
| V-04 | request body presente en `PUT` | continuar |
| V-05 | si no existe configuración previa, `GET` responde de forma consistente según contrato | continuar o responder vacío controlado |
| V-06 | `settingKey` interno resuelve a `MILK_PRICE_PER_LITER` | continuar |
| V-07 | `valueType` corresponde con el valor persistido | continuar |

## Refinamiento técnico

### Solución técnica refinada

La forma más pequeña y coherente de implementar esta HU es crear un slice backend nuevo con dos niveles:

1. fachada específica de negocio para `milk-price`
2. capa interna reutilizable de settings por sitio

Esto mantiene simple el consumo actual y evita rehacer almacenamiento cuando aparezcan otros settings o historial.

### Subtareas paso a paso

#### Fase 1. Contrato y modelo reusable

- [x] Confirmar la semántica final del `GET` cuando no exista configuración: `200` con valor por defecto explícito.
- [x] Crear el enum `SiteSettingValueType` con soporte mínimo para `NUMBER`, `STRING`, `BOOLEAN` y `JSON`.
- [x] Definir el `settingKey` estable `MILK_PRICE_PER_LITER`.
- [x] Formalizar los DTO internos `SiteSettingCurrentDTO`, `SiteSettingHistoryDTO` y `UpsertSiteSettingRequest` en el artefacto.
- [x] Formalizar e implementar el DTO público específico del contrato `milk-price` para `GET` y `PUT`.

#### Fase 2. Persistencia DynamoDB reusable

- [x] Crear la entidad `SiteSettingItem` para representar el valor vigente `CURRENT`.
- [x] Dejar implementada la misma tabla y entidad con la convención `HISTORY` lista para persistencia futura.
- [x] Implementar builders o helpers de claves para `PK`, `SK`, `GSI1PK` y `GSI1SK`.
- [x] Incorporar `TABLE_SITE_SETTINGS` en la configuración del backend.
- [x] Implementar `SiteSettingRepository` con `findCurrent(siteId, settingKey)` y `saveCurrent(item)`.
- [x] Implementar `saveHistorySnapshot(item)` para registrar snapshot por actualización.

#### Fase 3. Servicio reusable de settings

- [x] Crear `SiteSettingService` para centralizar lectura y escritura de settings por sitio.
- [x] Implementar la validación estructural mínima del tipo `NUMBER` para este setting.
- [x] Implementar incremento de `version` sobre el item `CURRENT`.
- [x] Implementar timestamps y metadata `updatedBy`.
- [x] Registrar el item `HISTORY` en cada actualización válida.

#### Fase 4. Fachada específica `milk-price`

- [x] Crear `MilkPriceSettingsProcessor`.
- [x] Crear `MilkPriceSettingsController`.
- [x] Implementar `GET /site/{siteId}/settings/milk-price` sobre el `SiteSettingService`.
- [x] Implementar `PUT /site/{siteId}/settings/milk-price` mapeando `milkPricePerLiter -> valueNumber`.
- [x] Garantizar que el frontend no vea detalles internos como `settingKey`, `valueType` o `version`.

#### Fase 5. Testing y validación

- [x] Validar lectura/escritura de `CURRENT` y `HISTORY` a nivel de service usando dobles de prueba del repository.
- [x] Agregar pruebas del service para validaciones, mapping y versionado.
- [x] Agregar pruebas de controller para `GET` y `PUT`.
- [x] Verificar el comportamiento cuando no existe configuración previa.
- [x] Verificar rechazo de valores negativos o payloads inválidos.
- [x] Probar la escritura de `HISTORY` a nivel de service usando dobles de prueba.

#### Fase 6. DynamoDB y SAM

- [x] Definir `TABLE_SITE_SETTINGS` como tabla dedicada y reusable.
- [x] Definir claves `PK` y `SK` para `CURRENT` e `HISTORY`.
- [x] Definir `GSI1` para búsquedas futuras por `settingKey`.
- [x] Inyectar `TABLE_SITE_SETTINGS` en la Lambda principal y en la scheduler function.
- [x] Agregar el recurso `AWS::DynamoDB::Table` en `template.yml` con `PAY_PER_REQUEST`.
- [x] Validar localmente `sam build` y `sam validate` tras corregir la definición de `SiteSettingsTable`.

### Estrategia de implementación por componente

#### 1. Entidades DynamoDB

Cambios esperados:

- crear una entidad `SiteSettingItem` para el valor vigente reusable
- crear una entidad `SiteSettingHistoryItem` o dejar modelada su convención de clave y DTO de persistencia futura
- seguir el patrón de `BaseDdbItem` con claves compuestas y timestamps
- incorporar helpers estáticos o factory methods para construir `PK`, `SK`, `GSI1PK`, `GSI1SK`

#### 2. Repository reutilizable

Cambios esperados:

- crear `SiteSettingRepository`
- soportar lectura del item `CURRENT` por `siteId + settingKey`
- soportar upsert del item `CURRENT`
- opcionalmente soportar persistencia de snapshot histórico
- exponer métodos explícitos como `findCurrent(siteId, settingKey)` y `saveCurrent(...)`
- si se implementa historia en esta HU, exponer `saveHistorySnapshot(...)`

#### 3. Service genérico

Cambios esperados:

- crear `SiteSettingService`
- encapsular resolución por `settingKey`
- centralizar conversión entre `NUMBER`, `STRING` o `JSON` si el modelo se extiende
- controlar `version`, timestamps y metadata de actualización
- mapear entre DTO reusable interno y contratos específicos de cada setting

#### 4. Processor y Controller específicos

Cambios esperados:

- crear `MilkPriceSettingsProcessor`
- crear `MilkPriceSettingsController`
- exponer `GET /site/{siteId}/settings/milk-price`
- exponer `PUT /site/{siteId}/settings/milk-price`
- mapear el contrato específico a la capa genérica de settings
- decidir y documentar que `GET` sin valor devuelve `200` con default controlado

#### 5. Configuración de infraestructura

Cambios esperados:

- incorporar `TABLE_SITE_SETTINGS` como variable de entorno/configuración
- asegurar que el repositorio se inicializa con `DynamoDbEnhancedClient` igual que el resto del proyecto
- si el despliegue SAM requiere tabla nueva, documentar ese impacto en la implementación

#### 6. Testing

Cobertura mínima esperada:

- `GET` devuelve configuración vigente existente
- `GET` responde correctamente cuando no existe configuración
- `PUT` crea o actualiza configuración vigente válida
- `PUT` rechaza valores negativos
- el service resuelve correctamente el uso de claves `CURRENT` y snapshot `HISTORY` sobre el repository
- validar la escritura del item `HISTORY` en esta HU
- validar mapping entre `milkPricePerLiter` y `valueNumber`
- validar incremento de `version` en actualizaciones consecutivas

### Dependencias y orden recomendado de ejecución

1. Definir contrato, DTOs y convención de claves.
2. Implementar entidad y repository reusable de `SiteSetting`.
3. Implementar `SiteSettingService` con versionado y metadata.
4. Exponer la fachada específica `milk-price` en controller y processor.
5. Ejecutar pruebas backend y dejar listo el contrato para integración frontend.

### Estrategia de validación

Validaciones mínimas esperadas:

1. pruebas unitarias del service o processor
2. pruebas de controller para `GET` y `PUT`
3. validación de `CURRENT` e `HISTORY` desde pruebas de service con dobles de repository
4. compilación Java del slice backend
5. validación SAM local del template corregido

### Criterios de terminado refinados

La HU puede considerarse lista para integración cuando:

- los endpoints `GET` y `PUT` existen y pasan pruebas
- la persistencia por `siteId` funciona de forma estable sobre un modelo genérico de settings
- el contrato devuelto es consistente con lo esperado por frontend
- la estructura de claves deja previsto `CURRENT` e `HISTORY` para el setting sin rediseño adicional
- el cambio no rompe el slice actual de `milkingProd`

## Evidencia de implementación y cierre

- [lambda-aws-cattle-java/src/main/java/com/cattle/entities/SiteSettingItem.java](e:/worskpace-cattle/lambda-aws-cattle-java/src/main/java/com/cattle/entities/SiteSettingItem.java): entidad reusable para `CURRENT` e `HISTORY` con builders de claves DynamoDB.
- [lambda-aws-cattle-java/src/main/java/com/cattle/enums/SiteSettingValueType.java](e:/worskpace-cattle/lambda-aws-cattle-java/src/main/java/com/cattle/enums/SiteSettingValueType.java): tipificación reusable de valores de settings.
- [lambda-aws-cattle-java/src/main/java/com/cattle/repository/SiteSettingRepository.java](e:/worskpace-cattle/lambda-aws-cattle-java/src/main/java/com/cattle/repository/SiteSettingRepository.java): acceso reusable a lectura de `CURRENT` y persistencia de snapshot histórico.
- [lambda-aws-cattle-java/src/main/java/com/cattle/services/SiteSettingService.java](e:/worskpace-cattle/lambda-aws-cattle-java/src/main/java/com/cattle/services/SiteSettingService.java): versionado, metadata y escritura coordinada de `CURRENT` e `HISTORY`.
- [lambda-aws-cattle-java/src/main/java/com/cattle/controller/MilkPriceSettingsController.java](e:/worskpace-cattle/lambda-aws-cattle-java/src/main/java/com/cattle/controller/MilkPriceSettingsController.java) y [lambda-aws-cattle-java/src/main/java/com/cattle/processor/MilkPriceSettingsProcessor.java](e:/worskpace-cattle/lambda-aws-cattle-java/src/main/java/com/cattle/processor/MilkPriceSettingsProcessor.java): fachada pública `milk-price` con `GET` y `PUT`.
- [lambda-aws-cattle-java/template.yml](e:/worskpace-cattle/lambda-aws-cattle-java/template.yml): tabla `SiteSettings`, `GSI1` y variables de entorno `TABLE_SITE_SETTINGS`.

## Validación ejecutada

- `./gradlew compileJava`: exitoso.
- `./gradlew test --tests com.cattle.services.SiteSettingServiceTest --tests com.cattle.controller.MilkPriceSettingsControllerTest`: exitoso.
- `sam build`: exitoso.
- `sam validate --template-file .aws-sam/build/template.yaml`: exitoso.

## Cierre

La HU queda cerrada como completada. El backend ya expone la fachada específica `milk-price`, persiste el valor por sitio sobre un modelo reusable `SiteSetting`, registra snapshot histórico por actualización y devuelve un valor por defecto cuando todavía no existe configuración persistida, permitiendo que el frontend consumidor opere sin depender de `404`.

## Estimación formal

### Supuestos de estimación

- se creará una tabla o slice de persistencia dedicada para settings por sitio
- el modelo reusable cubrirá al menos `NUMBER`, con diseño extensible a otros tipos
- el historial futuro se deja preparado a nivel de modelo y repositorio, pero no necesariamente con endpoints expuestos
- no se implementan permisos avanzados en esta historia, salvo validaciones mínimas

### Complejidad estimada

**Complejidad funcional**: Media  
**Complejidad técnica**: Media-alta  
**Riesgo operativo**: Medio

### Desglose de esfuerzo

| Actividad | Estimación |
|----------|------------|
| rediseño de la HU y definición del modelo reutilizable | 1.0 h |
| diseño de claves DynamoDB para `CURRENT` e `HISTORY` | 1.0 h |
| modelado de entidades y repositorio reusable | 2.0 h |
| implementación service genérico de settings | 1.5 h |
| implementación controller + processor específicos de `milk-price` | 1.5 h |
| pruebas unitarias, de repository y de controller | 2.0 h |
| ajuste de configuración/despliegue y validación focalizada | 1.0 h |
| **Total estimado** | **10.0 h** |

### Confianza de estimación

**Confianza**: Media.

Motivos:

- el alcance funcional visible para frontend sigue siendo pequeño
- pero el valor real de esta HU está en diseñar bien la base reusable de persistencia
- el mayor riesgo está en ajustar el almacenamiento al estilo actual del proyecto sin sobrediseñarlo

## Riesgos residuales

- si se decide crear tabla nueva, el despliegue y la infraestructura pueden ampliar el esfuerzo más allá del código Java
- si se decide registrar historia desde esta misma HU, el esfuerzo puede acercarse al límite superior de la estimación
- sin endpoints históricos todavía, el frontend seguirá consumiendo solo el valor vigente
- la política de autorización para cambiar el precio puede requerir endurecimiento posterior

## Evidencia de implementación y validación

- `template.yml`: se agregó `TableSiteSettings`, variable de entorno `TABLE_SITE_SETTINGS` y recurso `SiteSettingsTable` con `GSI1`.
- `src/main/java/com/cattle/entities/SiteSettingItem.java`: entidad reusable para `CURRENT` e `HISTORY`.
- `src/main/java/com/cattle/services/SiteSettingService.java`: servicio genérico con versionado y snapshot histórico.
- `src/main/java/com/cattle/controller/MilkPriceSettingsController.java`: fachada pública del contrato `milk-price`.
- `src/test/java/com/cattle/services/SiteSettingServiceTest.java`: cubre creación, versionado e historial.
- `src/test/java/com/cattle/controller/MilkPriceSettingsControllerTest.java`: cubre `GET` y `PUT` del contrato HTTP.
- Validación ejecutada: `./gradlew compileJava` y tests focalizados del slice con resultado exitoso.

## Siguiente paso sugerido

Implementar primero el modelo reusable `SiteSetting` con sus claves `CURRENT` y `HISTORY`, luego montar encima el contrato específico `GET/PUT /site/{siteId}/settings/milk-price` y finalmente alinear el consumidor frontend del panel de ventas de leche.
