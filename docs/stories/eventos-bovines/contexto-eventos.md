# Contexto de Eventos Bovinos y Operativos

## Propósito

Este archivo funciona como insumo crudo curado para identificar eventos candidatos de negocio, mantenimiento y seguimiento zootécnico. No representa todavía un contrato técnico ni una estructura final de persistencia.

## Alcance del contenido fuente

El material original mezclaba:

- labores de potreros y cultivos
- compras e insumos
- seguimiento sanitario y reproductivo
- mantenimiento de equipos
- tareas pendientes y próximos eventos

## Eventos candidatos extraídos

### 1. Mantenimiento de potreros

- 2026-01-20: encalado con cal dolomita en potreros 001, 002, 003, 007, 008, 009 y 010
- 2026-01-20: aporque y manejo de pasto de corte en potreros 005 y 006
- 2026-02-06: aplicación de crento corona en potrero 004

### 2. Manejo de cultivos y pasturas

- 2025-11-22: siembra inicial de ryegrass, avena forrajera y maíz
- 2025-12 aproximado: resiembra de maíz por mala germinación inicial
- seguimiento agronómico pendiente sobre encalado, hongos, fertilización y cronograma productivo

### 3. Compras de insumos agropecuarios

- 2026-01-20: compra y uso de cal dolomita para encalado
- 2026-01-25: compra de crento corona
- 2026-01-25: compra de fosforita o roca fosfórica

### 4. Eventos sanitarios y de manejo bovino

- 2026-01-13: compra e ingreso de un ternero de 8 meses
- 2026-01-24: baño garrapaticida a animales 167, 173 y 174
- 2026-02-06: desparasitación de animales 172, 173 y 174; exclusión de 167 por condición reproductiva

### 5. Seguimiento reproductivo

- vaca jersey servida el 2025-07-06 con fecha estimada de parto el 2026-04-10
- interés explícito en una línea de tiempo reproductiva y de lactancia por animal
- referencia histórica a fiebre de garrapatas, muerte de cría en parto previo y fiebre de leche

### 6. Mantenimiento de equipos

- 2026-02-06: mantenimiento de picapastos Penagos PP300 con lavado, afilado y cambio de aceite

### 7. Próximos eventos declarados

- 2026-02-07: aporque de maíz y aplicación de sulfato de amonio
- 2026-02-07: aplicación de crento al cultivo de moras
- aplicar fósforo en potreros 001, 002, 003 y 004
- aplicar crento a Cuba 22 en potreros 005 y 006

## Posibles categorías de evento

Para una futura normalización técnica, este material sugiere al menos estas categorías:

- `PASTURE_MAINTENANCE`
- `CROP_MANAGEMENT`
- `SUPPLY_PURCHASE`
- `BOVINE_HEALTH`
- `BOVINE_REPRODUCTIVE_TRACKING`
- `EQUIPMENT_MAINTENANCE`
- `PLANNED_TASK`

## Gaps para convertirlo en modelo operativo

- faltan identificadores formales por animal, potrero, cultivo y proveedor
- varias fechas o cantidades requieren normalización
- el texto mezcla hechos ejecutados, observaciones, preferencias y tareas futuras
- no hay separación explícita entre evento observado y recomendación deseada

## Uso recomendado

Este archivo puede servir como base para:

1. diseñar taxonomía de eventos rurales/bovinos
2. preparar extracción estructurada desde texto libre
3. definir qué parte del contexto va a historial operativo y cuál va a notas de seguimiento