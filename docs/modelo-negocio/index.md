# 📊 Flujos de Negocio: Cattle System

**Fecha**: 2026-01-09

## 🎯 Objetivo

Documentación de los flujos de negocio completos del sistema Cattle, integrando Bovines, Pastures y Milking en contextos realistas de operación de finca.

---

## 📚 Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Flujo #1: Ciclo Completo de Vida del Bovino](#flujo-1-ciclo-completo-de-vida-del-bovino)
3. [Flujo #2: Gestión de Potreros y Rotación](#flujo-2-gestión-de-potreros-y-rotación)
4. [Flujo #3: Producción Lechera Diaria](#flujo-3-producción-lechera-diaria)
5. [Flujo #4: Decisiones de Manejo Integrado](#flujo-4-decisiones-de-manejo-integrado)
6. [Puntos Críticos y Alertas](#puntos-críticos-y-alertas)
7. [KPIs y Reportes](#kpis-y-reportes)

---

## Visión General

El sistema Cattle maneja **tres áreas críticas** que se conectan mediante flujos de negocio:

```
┌─────────────────────────────────────────────────────────────┐
│                  SISTEMA CATTLE (FINCA)                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────────┐    ┌────────────────────┐            │
│  │   BOVINES          │    │   PASTURES         │            │
│  │                    │    │                    │            │
│  │ • 50 bovinos       │    │ • 10 potreros      │            │
│  │ • Genética         │    │ • Rotación diaria  │            │
│  │ • Productividad    │    │ • Forraje/carga    │            │
│  │                    │    │                    │            │
│  └────────┬───────────┘    └────────┬───────────┘            │
│           │                         │                       │
│           │    ◄─────────────────►  │                       │
│           │    Asignación           │                       │
│           │    de potrero           │                       │
│           │                         │                       │
│  ┌────────▼──────────────────────────▼──────┐              │
│  │          MILKING (Lactancia)              │              │
│  │                                           │              │
│  │  • Registros diarios (AM/PM)              │              │
│  │  • Producción por bovino                  │              │
│  │  • Calidad de leche                       │              │
│  │  • Relación: potrero → producción         │              │
│  │                                           │              │
│  └───────────────────────────────────────────┘              │
│                                                               │
│                      ▼                                        │
│              📊 DASHBOARD                                    │
│       (KPIs, alertas, decisiones)                           │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Flujo #1: Ciclo Completo de Vida del Bovino

**Objetivo**: Seguimiento de un bovino desde nacimiento hasta descarte.

### Fase 1: Registro Inicial

```
INICIO: Nace ternera
  │
  ├─ Sistema BOVINES
  │   ├─ Registrar bovino
  │   ├─ ID auto-generado (#47)
  │   ├─ Nombre: "Estrella"
  │   ├─ Género: "female"
  │   ├─ Fecha nacimiento: 2024-01-15
  │   ├─ Raza: Holstein
  │   ├─ Estado: CALF
  │   ├─ Arete RFID: ABC123XYZ
  │   └─ Activo: true
  │
  └─ Base de datos
      └─ TABLE_FARM_BOVINES: BOVINE#47 / PROFILE
```

### Fase 2: Crecimiento y Desarrollo

```
Meses 1-6: Lactancia materna (con criadora)
  │
  ├─ BOVINES estado: CALF
  ├─ No en PASTURES aún (con madre)
  └─ No registrada en MILKING (demasiado joven)

Meses 7-18: Pre-pubertad (en pastoreo)
  │
  ├─ BOVINES
  │   └─ edad actualizada: "1 año 2 meses"
  │
  ├─ PASTURES
  │   ├─ Asignada a potrero de cría
  │   ├─ Carga: 1 novilla per hectárea
  │   └─ Objetivo: ganancia de peso
  │
  └─ MILKING: Sin registros (no está produciendo)
```

### Fase 3: Edad Reproductiva

```
Mes 18-24: Primera inseminación
  │
  ├─ BOVINES
  │   ├─ Estado: PREGNANT (desde IA)
  │   ├─ Fecha estimada parto: en 280 días
  │   └─ Observaciones: Ternera 2, programa genético A
  │
  └─ PASTURES
      └─ Potrero especial: "pre-parto"
          ├─ Mayor calidad de forraje
          └─ Control de carga más estricto

Día parto (Mes 24):
  │
  ├─ BOVINES
  │   ├─ Estado: LACTATING (cambia automático o manual)
  │   └─ Observaciones: "Parto normal, ternero macho"
  │
  ├─ PASTURES
  │   └─ Potrero de "lactancia temprana"
  │       ├─ Máxima calidad de forraje
  │       └─ Carga reducida: 1.5 vacas/ha
  │
  └─ MILKING: Se abre registro
      ├─ Primer ordeno: 2025-01-20
      ├─ Día 1: 8L (calostro)
      ├─ Semana 1: promedio 12L
      └─ Semana 2+: 18-20L esperados
```

### Fase 4: Producción Plena

```
Años 3-7: Máxima productividad
  │
  ├─ BOVINES
  │   ├─ Estado: LACTATING (ciclos continuos)
  │   ├─ Ciclos: lactancia-secado-preñez-lactancia
  │   └─ Productividad acumulada: seguimiento
  │
  ├─ PASTURES
  │   ├─ Rotación óptima basada en producción
  │   ├─ Algoritmo: si producción < umbral
  │   │   → mejor potrero
  │   └─ Seguimiento de respuesta a forraje
  │
  └─ MILKING
      ├─ Registros diarios (AM/PM)
      ├─ Producción acumulada por ciclo: 7,000-8,000L
      ├─ KPI: Persistencia > 80%
      └─ Alertas: caída >15% vs promedio
```

### Fase 5: Evaluación y Descarte

```
Años 7-10: Decisión de permanencia
  │
  ├─ ANÁLISIS:
  │   ├─ Histórico de producción (MILKING)
  │   ├─ Salud reproductiva (BOVINES)
  │   ├─ Respuesta a manejo (PASTURES)
  │   └─ Costo vs beneficio
  │
  ├─ DECISIÓN:
  │   ├─ Si: Continúa → volver a Fase 4
  │   └─ Si: Descarte → marcar en BOVINES
  │
  └─ BOVINES
      └─ Estado: OPEN (inactiva)
          └─ enabled: false
              └─ Último registro de producción: 2027-12-31
```

---

## Flujo #2: Gestión de Potreros y Rotación

**Objetivo**: Asignar bovinos a potreros según calidad de forraje y carga.

### Contexto Inicial

```
Finca: 100 hectáreas
├─ 10 potreros de 10 hectáreas c/u
├─ 50 bovinos (25 lactando, 25 en cría/secado)
└─ Sistema rotacional cada 1-2 días
```

### Flujo Diario de Rotación

```
06:00 - MAÑANA (Turno AM)
  │
  ├─ Sistema PASTURES consulta:
  │   ├─ Estado actual de cada potrero
  │   ├─ Cantidad de forraje disponible (estimado)
  │   ├─ Bovinos actualmente asignados
  │   └─ Carga actual
  │
  ├─ ALGORITMO DE ROTACIÓN:
  │   ├─ Para cada potrero:
  │   │   ├─ Calcular: forraje/carga
  │   │   ├─ Si forraje muy bajo (<2kg/día)
  │   │   │   → Marcar para descarga
  │   │   │   → ESTADO: "NEEDS_ROTATION"
  │   │   │
  │   │   └─ Si forraje bueno (>3kg/día)
  │   │       → Potrero candidato para entrada
  │   │       → ESTADO: "READY_FOR_ENTRY"
  │   │
  │   └─ Para cada bovino en potrero con baja calidad:
  │       ├─ Buscar mejor potrero disponible
  │       ├─ Prioridad por tipo:
  │       │   ├─ Lactancia: máxima calidad
  │       │   ├─ Preñez: media calidad
  │       │   └─ Secado: calidad media-baja
  │       │
  │       └─ Generar propuesta de movimiento
  │
  ├─ MANEJO MANUAL (Usuario):
  │   ├─ Ve sugerencias en dashboard
  │   ├─ Revisa cada movimiento propuesto
  │   ├─ Acepta/rechaza/modifica
  │   └─ Ejecuta movimientos
  │
  └─ REGISTRAR EN SISTEMA:
      ├─ Para cada bovino movido:
      │   ├─ BOVINES: actualizar ubicación
      │   ├─ PASTURES: registrar evento de entrada
      │   │   └─ tipo: "ENTRY"
      │   │   └─ bovineId: 47
      │   │   └─ timestamp: 2025-12-20 06:30:00
      │   │
      │   └─ MILKING: verificar si lactando
      │       └─ Si sí: esperar efectos (1-3 días)
      │
      └─ ANÁLISIS:
          ├─ Almacenar para ML:
          │   ├─ Potrero anterior: PASTURE_A
          │   ├─ Potrero nuevo: PASTURE_B
          │   ├─ Producción anterior: 18L/día
          │   ├─ Producción post-cambio: 18.5L/día (+2.7%)
          │   └─ Conclusión: movimiento exitoso ✓

12:00 - MEDIO DÍA (Turno PM - Ordeno)
  │
  ├─ MILKING: Registrar producción PM
  │   ├─ Bovino #47 "Estrella"
  │   ├─ Turno: PM
  │   ├─ Litros: 9.2L
  │   ├─ Estado: completo
  │   ├─ Potrero actual: PASTURE_B
  │   ├─ Observaciones: "Buena producción tras cambio"
  │   │
  │   └─ Análisis de desempeño:
  │       ├─ Vs promedio de potrero: +5%
  │       ├─ Vs su promedio personal: +2%
  │       └─ Conclusión: Potrero PASTURE_B exitoso

18:00 - TARDE (Evaluación del día)
  │
  ├─ DASHBOARD integrado:
  │   ├─ Resumen de movimientos: 12 bovinos rotados
  │   ├─ Producción del día: 525L (vs 510L ayer)
  │   ├─ Bovinos con alerta de producción: 2
  │   │   ├─ #23 "Molly": caída 12% (potrero anterior mejor)
  │   │   └─ #31 "Daisy": caída 8% (observar próximos 2 días)
  │   │
  │   └─ Recomendaciones para mañana:
  │       ├─ Mantener PASTURE_B (excelente desempeño)
  │       ├─ Rotar #23 de vuelta a PASTURE_A
  │       └─ Observar #31 (podría ser estrés, no potrero)
```

### Escenario de Crisis: Potrero Dañado

```
Evento: Lluvia intensa overnight → encharcamiento potrero A

09:00 - MAÑANA SIGUIENTE
  │
  ├─ Inspector detecta problema
  ├─ PASTURES: Actualizar estado
  │   └─ PASTURE_A
  │       ├─ status: "DAMAGED"
  │       ├─ reason: "Waterlogged - heavy rain"
  │       ├─ forraje_disponible: 0.5kg/ha (crítico)
  │       └─ closed_until: 2025-12-25
  │
  ├─ SISTEMA AUTOMÁTICO:
  │   ├─ Identifica 15 bovinos en PASTURE_A
  │   ├─ Rechaza para entrada/salida
  │   ├─ Bloquea status: "UNAVAILABLE"
  │   │
  │   └─ Genera ALERTA crítica
  │       ├─ Severidad: CRITICAL
  │       ├─ Acción requerida: Reasignar 15 bovinos
  │       └─ Tiempo máximo: 2 horas
  │
  ├─ SISTEMA DE RECOMENDACIÓN:
  │   └─ Propone redistribución:
  │       ├─ 5 lactancia → PASTURE_B (mejor)
  │       ├─ 5 preñez → PASTURE_C
  │       ├─ 5 secado → PASTURE_D
  │       └─ Densidad resultante: 80% capacidad
  │
  ├─ USUARIO ejecuta:
  │   └─ Aprueba plan en dashboard
  │       └─ Sistema aplica cambios
  │
  └─ RESULTADO:
      ├─ 15 bovinos reasignados en 45 min
      ├─ Producción estimada: -3% (temporal)
      └─ Potrero se recupera en 5 días
```

---

## Flujo #3: Producción Lechera Diaria

**Objetivo**: Capturar registros de lactancia y analizar tendencias.

### Día Operativo: 2025-12-20

```
05:00 - PRE-ORDENO
  │
  ├─ PREPARACIÓN:
  │   ├─ Verificar bovinos en corral
  │   ├─ Revisar alertas de salud en BOVINES
  │   │   ├─ #47 "Estrella": OK
  │   │   ├─ #23 "Molly": Alerta mastitis (observar)
  │   │   └─ #31 "Daisy": OK
  │   │
  │   └─ Revisar potrero actual
  │       └─ Todos en PASTURE_B, estado: bueno

05:30 - ORDENO MAÑANA (AM)
  │
  ├─ BOVINO #47 "Estrella"
  │   ├─ Ordeno completo
  │   ├─ Litros: 9.8L
  │   ├─ Calidad: buena
  │   ├─ Observaciones: "-"
  │   │
  │   └─ Sistema MILKING registra:
  │       ├─ POST /milkingRecord
  │       ├─ {
  │       │   "bovineId": 47,
  │       │   "date": "2025-12-20",
  │       │   "shift": "AM",
  │       │   "liters": 9.8,
  │       │   "status": "completo",
  │       │   "recordedBy": "jhonroberth"
  │       │ }
  │       │
  │       └─ ANÁLISIS INMEDIATO:
  │           ├─ vs ayer AM: 9.5L → +3.1% ✓
  │           ├─ vs promedio semana: 9.6L → +2.0% ✓
  │           ├─ Conclusión: Excelente día
  │           └─ Potrero PASTURE_B: +5 puntos
  │
  ├─ BOVINO #23 "Molly"
  │   ├─ Ordeno: parcial (difícil sacar)
  │   ├─ Litros: 7.2L (esperado: 9.0L)
  │   ├─ Observación: Mastitis cuarto anterior derecho
  │   │
  │   └─ Sistema MILKING registra:
  │       ├─ {
  │       │   "bovineId": 23,
  │       │   "date": "2025-12-20",
  │       │   "shift": "AM",
  │       │   "liters": 7.2,
  │       │   "status": "parcial",
  │       │   "observations": "Mastitis detectada, cuarto anterior",
  │       │   "recordedBy": "jhonroberth"
  │       │ }
  │       │
  │       └─ ALERTAS AUTOMÁTICAS:
  │           ├─ Caída 20% vs promedio → ALERT
  │           ├─ Estatus "parcial" → FLAG
  │           ├─ Observación contiene "mastitis" → CRITICAL
  │           │
  │           └─ SISTEMA notifica:
  │               ├─ Alerta a veterinario
  │               ├─ Recomienda tratamiento
  │               ├─ Marca BOVINES #23:
  │               │   └─ status_salud: "UNDER_TREATMENT"
  │               │
  │               └─ Recomendación en PASTURES:
  │                   ├─ Si es infecciosa: aislar en potrero separado
  │                   └─ Si es estrés: reducir densidad

06:30 - DESPUÉS DE ORDENO AM
  │
  ├─ RESULTADO ACUMULADO (AM):
  │   ├─ Total bovinos: 25 (activos en lactancia)
  │   ├─ Total ordeno: 24 (1 no se ordena)
  │   ├─ Litros: 450L
  │   ├─ Promedio por bovino: 18.75L
  │   ├─ Completitud: 95% (1 parcial, 1 omitido)
  │   └─ Calidad: 98% (1 con mastitis)
  │
  ├─ ACTUALIZAR DASHBOARD:
  │   └─ Producción AM: 450L
  │       ├─ vs ayer: -2% (debido a mastitis #23)
  │       └─ Bovino con alerta: #23
  │
  └─ ACCIONES RECOMENDADAS:
      ├─ Llamar veterinario para #23
      ├─ Considerar antibiótico
      ├─ Reducir ordeno a 2× al día (solo AM por ahora)
      └─ Reasignar a potrero menos estresante

12:00 - ANÁLISIS DE MEDIO DÍA
  │
  ├─ VETERINARIO revisa #23
  │   ├─ Diagnóstico: Mastitis subclinica
  │   ├─ Tratamiento: Antibiótico local + enjuague frecuente
  │   └─ BOVINES: Actualizar
  │       └─ notas: "Mastitis - tratamiento iniciado 20/12"
  │
  └─ RECOMENDACIÓN TEMPORAL:
      └─ Reasignar #23 a potrero "enfermería"
          ├─ Menor estrés
          ├─ Acceso a agua/minerales especiales
          └─ Ordeno más frecuente (3× día) con protocolo sanitario

17:00 - ORDENO TARDE (PM)
  │
  ├─ BOVINO #47 "Estrella"
  │   ├─ Ordeno: completo
  │   ├─ Litros: 9.2L
  │   ├─ Estado: excelente
  │   │
  │   └─ MILKING registra PM:
  │       ├─ Total día: 9.8 + 9.2 = 19.0L
  │       ├─ vs ayer: 18.5L → +2.7% ✓✓
  │       ├─ Persistencia: 94% (9.2/9.8)
  │       └─ Conclusión: Estrella en forma óptima
  │
  ├─ BOVINO #23 "Molly"
  │   ├─ Ordeno especial (antibiótico)
  │   ├─ Litros: 5.8L (menos flujo por mastitis)
  │   ├─ Observación: "Mejora visible vs AM"
  │   │
  │   └─ MILKING registra PM:
  │       ├─ Total día: 7.2 + 5.8 = 13.0L
  │       ├─ Caída vs ayer: 22.1L → -41% CRITICAL
  │       └─ Acción: Observar próximos 3 días
  │
  └─ RESULTADO FINAL (PM):
      ├─ Litros PM: 475L
      ├─ Litros día total: 450 + 475 = 925L
      ├─ Promedio por bovino: 37.0L/día
      ├─ vs ayer: 950L → -2.6%
      │   └─ Razón: Mastitis #23 (-9L)
      │
      └─ ESTADO: Normal, monitoreando #23

20:00 - CIERRE DE DÍA
  │
  ├─ DASHBOARD FINAL:
  │   ├─ Producción: 925L
  │   ├─ KPI producción: 37.0L/bovino (-2.6% vs ayer)
  │   ├─ Bovinos en alerta: 1 (#23)
  │   ├─ Completitud: 96%
  │   ├─ Potrero mejor desempeño: PASTURE_B (+3%)
  │   └─ Potrero peor desempeño: PASTURE_C (-1%)
  │
  ├─ PREDICCIÓN PARA MAÑANA:
  │   ├─ Si #23 mejora: 940L estimado
  │   ├─ Si #23 empeora: 900L estimado
  │   └─ Umbral de alerta: < 910L
  │
  └─ ACCIONES PENDIENTES:
      ├─ [ ] Veterinario: Chequeo #23 mañana AM
      ├─ [ ] Reasignar #23 a potrero enfermería
      ├─ [ ] Verificar inventario de antibióticos
      └─ [ ] Analizar correlación PASTURE_C (baja producción)
```

---

## Flujo #4: Decisiones de Manejo Integrado

**Objetivo**: Tomar decisiones cruzando datos de Bovines, Pastures y Milking.

### Caso #1: Bovino con Baja Producción

```
Situación: #31 "Daisy" produce 15L/día vs 20L normal (-25%)

SEMANA 1 - DIAGNÓSTICO:
  
  MILKING data:
  ├─ Últimos 7 días: [20, 20, 19, 18, 16, 15, 15]L
  ├─ Tendencia: ↓ lineal en 5 días
  ├─ Status registros: 4 completos, 3 parciales
  └─ Observaciones: "Se ve apagada"

  BOVINES data:
  ├─ Edad: 4 años 2 meses (plena edad productiva)
  ├─ Estado: LACTATING (ciclo 6 meses)
  ├─ Última preñez: 5 meses atrás
  ├─ Notas: "Sin problemas aparentes"
  └─ Salud: Status normal

  PASTURES data:
  ├─ Última rotación: hace 1 día
  ├─ Potrero actual: PASTURE_A
  ├─ Potrero anterior: PASTURE_C
  ├─ Calidad forraje A: buena (3.5kg/ha)
  ├─ Carga actual: normal (2.0 vacas/ha)
  └─ Ningún evento de estrés registrado

HIPÓTESIS:
  ├─ H1: Problema de salud (mastitis, infección) → 30% probabilidad
  ├─ H2: Estrés por cambio de potrero → 40% probabilidad
  ├─ H3: Ciclo reproductivo (preñez temprana) → 20% probabilidad
  └─ H4: Calidad de alimento inconsistente → 10% probabilidad

ACCIONES:
  ├─ DAY 1: Veterinario revisa
  │   ├─ Descarta infección clínica
  │   ├─ Ubres normales
  │   ├─ Temperatura normal
  │   └─ Conclusión: No es problema sanitario
  │
  ├─ DAY 2: Cambiar potrero
  │   ├─ Mover de PASTURE_A a PASTURE_B (mejor calidad)
  │   ├─ Mantener control diario de producción
  │   └─ Esperar 2-3 días para efecto
  │
  └─ DAY 3-7: Monitorear
      ├─ MILKING diarios (AM/PM)
      ├─ Si sube → potrero era problema (PASTURES_A degradado)
      └─ Si baja más → problema reproductivo confirmado

RESULTADO (Día 7):
  ├─ Cambio a PASTURE_B Day 2
  ├─ Producción Day 3: 17L ↑
  ├─ Producción Day 4: 18L ↑
  ├─ Producción Day 5: 19L ↑
  ├─ Producción Day 6: 20L ✓
  ├─ Producción Day 7: 20L ✓
  │
  └─ CONCLUSIÓN:
      ├─ Problema: PASTURE_A degradación
      ├─ Solución: Extender rotación de PASTURE_A
      ├─ Acción: Cerrar PASTURE_A por 7 días
      ├─ ML Update: "Potrero A con forraje comprometido en período X"
      └─ Valor agregado: Prevenir futuros problemas
```

### Caso #2: Introducción de Nuevo Bovino

```
Situación: Compra de novilla "Bella" de otra finca (ID #52)

SEMANA PRE-COMPRA:
  ├─ Veterinario de finca vendedora: certificación
  ├─ Tests: brucela, TB, rinotraqueitis (todos negativos)
  └─ Documentación: pedigree, vacunaciones OK

DAY 0 - LLEGADA:
  └─ BOVINES registro:
      ├─ bovineId: 52 (auto-generado)
      ├─ name: "Bella"
      ├─ breed: Holstein
      ├─ bornDate: 2021-03-10
      ├─ age: 3 años 9 meses
      ├─ gender: female
      ├─ status: OPEN
      ├─ tag: ScanEAR#XYZ789
      ├─ nota: "Compra externa, verificar adaptación"
      └─ enabled: true

DAY 0-3 - CUARENTENA:
  ├─ Potrero aislado: PASTURE_Q (enfermería)
  ├─ Forraje de calidad media
  ├─ Monitoreo diario de comportamiento
  ├─ Sin ordeno (aunque no estaba lactando)
  └─ Observaciones: "Come bien, adaptándose"

DAY 4 - INTRODUCCIÓN GRADUAL:
  ├─ PASTURES: mover a potrero con 2-3 vacas conocidas
  ├─ Ubicación: PASTURE_D (media calidad)
  │   ├─ Permite que aprenda dinámica
  │   └─ Sin presión de competencia
  │
  ├─ Comportamiento esperado:
  │   ├─ Primeros 2 días: cautela
  │   ├─ Día 3-4: aceptación
  │   └─ Día 5+: integración normal
  │
  └─ BOVINES status: OPEN (no reproducir aún)

DAY 10 - INSEMINACIÓN:
  └─ BOVINES:
      ├─ status: cambiar a PREGNANT
      ├─ reproducción: programa B (cruzamiento)
      ├─ fecha estimada parto: 280 días
      ├─ fecha IA: 2025-12-20
      └─ notas: "Entrada reciente, vigilancia mayor"

SEMANA 2+ - SEGUIMIENTO:
  ├─ PASTURES:
  │   ├─ Observar interacción social
  │   ├─ Respuesta a cambios de potrero
  │   └─ Si rechaza algún potrero: detectar problema
  │
  ├─ BOVINES:
  │   ├─ Ganancia de peso esperada: 0.5-0.7 kg/día
  │   ├─ Estado reproductivo: vigilancia cada semana
  │   ├─ Anotar observaciones de comportamiento
  │   └─ Fotografía para comparación (15 días)
  │
  └─ SISTEMA:
      ├─ Crear modelo ML "nueva compra"
      ├─ Comparar con historial de finca
      └─ Predecir productividad esperada (20-22L/día)

MONTH 1+ - EVALUACIÓN:
  └─ ¿Ha logrado adaptarse?
      ├─ SI → continuar programa normal
      ├─ NO → investigar problema
      │   ├─ ¿Problema sanitario?
      │   ├─ ¿Genética incompatible?
      │   └─ ¿Manejo inadecuado?
      │
      └─ Tomar decisión (mantener/retornar/descartar)
```

---

## Puntos Críticos y Alertas

### Sistema de Alertas Integrado

```
NIVEL 1: INFORMACIÓN (amarillo)
├─ Bovino cambió de potrero
├─ Producción varía ±5% vs promedio
├─ Potrero necesita rotación pronto (1-2 días)
└─ Acción: Monitorear, ninguna urgencia

NIVEL 2: ADVERTENCIA (naranja)
├─ Producción cae 10-15% vs promedio
├─ Mastitis subclinica detectada
├─ Potrero necesita rotación HOY
├─ Bovino no se ordena (omisión)
└─ Acción: Revisar, posible acción necesaria

NIVEL 3: ALERTA (rojo)
├─ Producción cae >20% vs promedio
├─ Mastitis clínica detectada
├─ Potrero criticidad baja (cierre inmediato)
├─ Bovino ordeno parcial (infección probable)
├─ Fallo de equipo de ordeno
└─ Acción: INMEDIATA, contactar veterinario

NIVEL 4: CRÍTICO (rojo + alarma)
├─ Potrero inutilizable (inundación, contaminación)
├─ Bovino en shock o colapso
├─ Brote de enfermedad (2+ bovinos enfermos)
├─ Falla total de sistema de ordeno
└─ Acción: EMERGENCIA, llamar veterinario + gerente
```

### Umbral de Alertas por Módulo

```
BOVINES:
├─ Edad: si > 10 años → evaluación descarte
├─ Salud: si "under treatment" > 30 días → revisar diagnóstico
├─ Reproducción: si no preñada después de IA → nuevo intento en 21 días
└─ Activo: si "enabled: false" > 60 días → registrar como descarte

PASTURES:
├─ Forraje: si < 2.0 kg/ha → rotación urgente
├─ Carga: si > capacidad máxima → reducir inmediatamente
├─ Drenaje: si encharcado > 24h → cierre de potrero
├─ Contaminación: si presencia de plagas/enfermedades → aislamiento
└─ Uso: si sin movimiento > 10 días → evaluar capacidad ociosa

MILKING:
├─ Producción: si < -20% vs promedio → investigar causa
├─ Completitud: si < 90% → revisar faltas de ordeno
├─ Status parcial: si > 3 consecutivos → revisión veterinaria
├─ Ausencia: si > 2 días sin registros → bovino escapó o murió
└─ Mastitis: si clínica → tratamiento + cuarentena
```

---

## KPIs y Reportes

### KPIs Diarios

```
PRODUCCIÓN:
├─ Litros totales/día: 925L (objetivo: >900L)
├─ Litros/bovino: 37.0L (objetivo: >35L)
├─ Completitud ordeno: 96% (objetivo: >95%)
└─ Calidad leche: 98% (objetivo: >95%)

SALUD:
├─ Bovinos bajo tratamiento: 1 (#23)
├─ Bovinos con alerta: 1 (#23)
├─ Masteritis: 1 caso (objetivo: 0)
└─ Mortalidad: 0% (objetivo: 0%)

PASTURES:
├─ Potreros en rotación: 7 (de 10)
├─ Potrero con mejor desempeño: PASTURE_B (+3%)
├─ Potrero con peor desempeño: PASTURE_C (-1%)
└─ Eficiencia forraje: 2.3kg/L producida

INTEGRACIÓN:
├─ Bovinos lactando / total: 25/50 (50%)
├─ Bovinos en rotación activa: 48/50 (96%)
├─ Tasa de rotación (cambios/día): 12 movimientos
└─ Efectividad predicción: 87% (ML)
```

### KPIs Semanales

```
PRODUCCIÓN:
├─ Total semana: 6,475L (vs 6,650L semana anterior: -2.6%)
├─ Promedio/bovino: 259.0L/semana (vs 266.0L: -2.7%)
├─ Consumo alimento: 122 toneladas forraje
├─ Ratio producción/alimento: 53.1 L/tonelada
└─ Tendencia: ESTABLE (-2% debido a mastitis temporal)

SALUD:
├─ Nuevos casos de enfermedad: 1 (mastitis #23)
├─ Resolución de casos: 0 (aún en tratamiento)
├─ Tasa de nuevas infecciones: 2.0% (1/50)
├─ Antibióticos usados: 1 bovino
└─ Efectividad tratamientos: N/A (en progreso)

REPRODUCCIÓN:
├─ Ias realizadas: 2 (#52 "Bella", #44)
├─ Preñeces detectadas: 1 (#12)
├─ Ciclos promedio: 23.0 días (objetivo: 21 días)
├─ Tasa preñez/IA: 50% (objetivo: >70%)
└─ Próximas IA programadas: 5 (semana siguiente)

POTREROS:
├─ Calidad promedio: 3.2 kg/ha (objetivo: 3.0+)
├─ Rotaciones realizadas: 84 (12/día × 7)
├─ Potrero con máxima respuesta: PASTURE_B (+5%)
├─ Eficiencia rotacional: 94% (movimientos exitosos)
└─ Capacidad ociosa: 6% (PASTURE_A en recuperación)
```

### Reportes Mensuales

```
MES: DICIEMBRE 2025

1. RESUMEN EJECUTIVO
   ├─ Producción total: 27,850L (vs 28,500L noviembre: -2.3%)
   ├─ Promedio diario: 898L
   ├─ Promedio/bovino: 35.9L
   ├─ Días operando: 31/31 (100%)
   ├─ Incidencias mayores: 2 (mastitis, enfermedad)
   └─ Estado general: BUENO

2. PRODUCCIÓN
   ├─ Gráfico tendencia: ↓ leve (recuperación esperada enero)
   ├─ Bovinos contribuyentes: 25/50 (50%)
   ├─ Top 5 productores:
   │  ├─ #47 "Estrella": 608L (19.6L/día)
   │  ├─ #02 "Bossy": 595L (19.2L/día)
   │  ├─ #15 "Molly": 580L (18.7L/día)
   │  ├─ #08 "Daisy": 575L (18.5L/día)
   │  └─ #44 "Clara": 570L (18.4L/día)
   │
   └─ Bottom 5 productores:
      ├─ #23 "Molly": 425L (13.7L/día) ← mastitis
      ├─ #31 "Daisy": 550L (17.7L/día) ← problema pasture
      ├─ #29: 555L (17.9L/día)
      ├─ #35: 565L (18.2L/día)
      └─ #38: 568L (18.3L/día)

3. SALUD
   ├─ Casos de enfermedad: 2
   │  ├─ #23 mastitis (11-20 dic): en tratamiento, mejorando
   │  └─ #01 mastitis subclinica (5 dic): resuelta
   │
   ├─ Tratamientos: 1 bovino (antibióticos)
   ├─ Muertes: 0
   ├─ Descartes: 0
   └─ Tasa enfermedad: 4% (2/50)

4. REPRODUCCIÓN
   ├─ Ciclos promedio: 23.2 días
   ├─ IAs realizadas: 8
   ├─ Preñeces confirmadas: 3 (#12, #27, #39)
   ├─ Fallos reproductivos: 2 (IA no pegó)
   ├─ Tasa éxito: 62.5% (5/8)
   └─ Próximo parto esperado: 28-01-2026 (#12)

5. PASTURES
   ├─ Uso de potreros: 8/10 (80%)
   │  ├─ PASTURE_A: en recuperación (7 días aún)
   │  ├─ PASTURE_B: máximo desempeño
   │  └─ Otros: operación normal
   │
   ├─ Rotaciones realizadas: 372 movimientos (12.0/día)
   ├─ Eficiencia rotacional: 96%
   ├─ Incidencias: 1 (encharcamiento A)
   └─ Calidad promedio: 3.1 kg/ha

6. ANÁLISIS INTEGRADO
   ├─ Correlación potrero → producción:
   │  ├─ PASTURE_B: +5% efecto positivo ✓
   │  ├─ PASTURE_C: -1% efecto neutral
   │  └─ PASTURE_A: -3% efecto negativo (ahora recuperando)
   │
   ├─ Correlación salud → producción:
   │  ├─ Mastitis #23: -41% caída producción
   │  └─ Otros: sin impacto mensurable
   │
   └─ Correlación reproducción → productividad:
      ├─ Ciclos largos: -2% vs promedio
      └─ Necesario mejorar programa IA

7. RECOMENDACIONES ENERO 2026
   ├─ Seguimiento:
   │  ├─ Vigilancia #23 (mastitis) - otros 7 días
   │  ├─ Confirmación preñeces: ecografía semana 2
   │  └─ Revisión PASTURE_A: si no recuperado, análisis suelo
   │
   ├─ Acciones correctivas:
   │  ├─ Mejorar tasa éxito IA (62.5% → 80%)
   │  ├─ Reducir incidencia mastitis (2 casos → 0)
   │  └─ Optimizar rotación (recuperar PASTURE_A)
   │
   └─ Inversiones:
      ├─ Drenaje para PASTURE_A: $500
      ├─ Análisis de suelo: $200
      └─ Capacitación equipo IA: $300

8. PROYECCIONES
   ├─ Enero 2026: 28,500L (recuperación)
   ├─ Febrero 2026: 29,000L (+1.8%)
   ├─ Q1 2026: 85,500L
   └─ Full year 2026: 340,000L (vs 2025: 330,000L = +3% growth)
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
