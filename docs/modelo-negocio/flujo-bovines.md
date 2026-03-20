# 🐄 Flujo Detallado: Gestión de Bovines

**Fecha**: 2026-01-09 | **Parte**: Flujo de Negocio

## 🎯 Objetivo

Documentar en detalle cómo se gestiona el ciclo de vida completo de un bovino, desde registro hasta descarte, incluyendo decisiones de reproducción, salud y productividad.

---

## 📊 Flujo Simplificado

```
REGISTRO → CRECIMIENTO → REPRODUCCIÓN → LACTANCIA → DESCARTE
   ↓           ↓              ↓            ↓         ↓
 Sistema    Análisis        IA       Producción   Evaluación
 Bovines    potencial     programa    histórico    económica
```

---

## Fase 1: Registro e Identificación

### Entrada Manual (Nuevo Bovino)

```
USUARIO abre BOVINES → "Nuevo Bovino"
  │
  ├─ FORMULARIO:
  │   ├─ Nombre *: "Estrella"
  │   ├─ Género *: "female" (radio)
  │   ├─ Fecha nacimiento *: 2024-01-15 (date picker)
  │   ├─ Raza: "Holstein" (select)
  │   ├─ Color: "black & white"
  │   ├─ Estado: "CALF" (auto-select por género/edad)
  │   ├─ Padre: "Bull01" (opcional)
  │   ├─ Madre: "Daisy" (opcional)
  │   ├─ Arete/Tag: "ABC123XYZ" (RFID - scan o manual)
  │   └─ Activo: ✓ (checkbox)
  │
  ├─ FRONTEND VALIDATION:
  │   ├─ Nombre no vacío ✓
  │   ├─ Género seleccionado ✓
  │   ├─ Fecha no futura ✓
  │   └─ Fecha formato válido ✓
  │
  ├─ SUBMIT:
  │   └─ POST /bovineIdentityItems con datos
  │
  ├─ BACKEND PROCESSING:
  │   ├─ BovinesController.save(BovineDTO)
  │   ├─ BovinesProcessor.save(DTO)
  │   │   ├─ Mapear a Entity
  │   │   └─ Delegar a Service
  │   │
  │   ├─ BovinesService.save(Entity)
  │   │   ├─ Validar campos
  │   │   ├─ Obtener siguiente ID: CountersRepository.getNextId()
  │   │   │   └─ Counter incrementa: 47
  │   │   │
  │   │   ├─ Generar claves:
  │   │   │   ├─ pk = "BOVINE#47"
  │   │   │   ├─ sk = "PROFILE"
  │   │   │   ├─ gsi1pk = "PROFILE"
  │   │   │   └─ gsi1sk = "BOVINE#47"
  │   │   │
  │   │   ├─ Setear metadata:
  │   │   │   ├─ bovineId = 47
  │   │   │   ├─ createdAt = ISO timestamp
  │   │   │   ├─ updatedAt = ISO timestamp
  │   │   │   └─ enabled = true
  │   │   │
  │   │   └─ Delegar a Repository
  │   │
  │   ├─ BovineRepository.save(Entity)
  │   │   ├─ PutItem en TABLE_FARM_BOVINES
  │   │   ├─ pk: "BOVINE#47"
  │   │   └─ sk: "PROFILE"
  │   │
  │   └─ Response: BovineDTO con ID generado
  │
  └─ FRONTEND FEEDBACK:
      ├─ Cerrar formulario
      ├─ Mostrar confirmación: "Bovino #47 creado"
      ├─ Refetch lista
      └─ Navegar a detalle o volver a lista
```

### Entrada por Escaneo RFID

```
USUARIO hace click "Escanear arete"
  │
  ├─ FRONTEND:
  │   ├─ Activar scanner RFID (hardware simulation)
  │   ├─ Estado: "Escaneando..."
  │   └─ Timeout: 30 segundos
  │
  ├─ HARDWARE:
  │   └─ Leer código RFID: "ABC123XYZ"
  │
  ├─ FRONTEND recibe:
  │   ├─ Código parseado
  │   ├─ Input "tag" auto-rellenado: "ABC123XYZ"
  │   └─ Usuario verifica antes de guardar
  │
  └─ NOTA: En sistema real, validar que tag no esté duplicado
      └─ DB query: "¿Existe otro BOVINE con tag = ABC123XYZ?"
          ├─ Si existe: ERROR "Tag ya registrado"
          └─ Si no existe: OK, continuar
```

---

## Fase 2: Evaluación de Edad y Capacidad

### Cálculo Automático de Edad

```
Frontend useBovineForm hook:
  │
  ├─ INPUT: bornDate = "2024-01-15"
  ├─ LOGIC:
  │   ├─ now = 2025-12-20
  │   ├─ years = 2025 - 2024 = 1
  │   ├─ months = 12 - 1 = 11
  │   └─ age = "1 año 11 meses"
  │
  ├─ DISPLAY:
  │   ├─ En formulario: "Edad estimada: 1 año 11 meses"
  │   ├─ En tarjeta: mostrar en encabezado
  │   └─ En detalle: sección "Datos biológicos"
  │
  └─ STATUS AUTO-SELECT:
      ├─ Si edad < 6 meses: CALF
      ├─ Si 6 < edad < 18 meses: HEIFER
      ├─ Si edad > 18 meses:
      │   └─ Si gender = female: OPEN
      │   └─ Si gender = male: BULL
      │
      └─ En este caso: edad 1año 11 meses → OPEN (lista para IA)
```

### Determinación de Potencial Productivo

```
SISTEMA INTERNO (NO visible usuario, pero relevante para decisiones):
  │
  ├─ RAZA: Holstein = alto potencial (22-25 L/día)
  ├─ EDAD: 1 año 11 meses = lista para primer parto
  ├─ LINAJE: Padre Bull01 + Madre Daisy = buen linaje
  ├─ ESTÁNDAR FINCA: promedio 20L/día
  │
  └─ PREDICCIÓN: Estrella debe producir 20-22L/día en lactancia
      ├─ Usar para comparación futura
      └─ Alertar si desempeño << predicción
```

---

## Fase 3: Preparación para Reproducción

### Programa de Inseminación

```
EDAD: 18 meses (18 meses después del nacimiento)
  │
  ├─ VETERINARIO evaluación:
  │   ├─ Peso: debe ser > 80% peso adulto (500kg)
  │   ├─ Salud: examinar vulva, ciclo estral
  │   ├─ BCS: Score 2.5-3.0 (buena condición)
  │   └─ PASS/FAIL
  │
  ├─ SI PASA → Ingreso a programa reproducción
  │   └─ BOVINES: actualizar status → "OPEN"
  │
  └─ SI FALLA → Esperar 30 días y reintentar

PRIMER CICLO ESTRAL (21 días):
  │
  ├─ OBSERVACIÓN de signos:
  │   ├─ Hinchazón vulva
  │   ├─ Mucosa mucosa clara
  │   ├─ Comportamiento: receptiva
  │   └─ Posición de monta: acepta
  │
  ├─ REGISTRO MANUAL:
  │   ├─ BOVINES: detectar ciclo
  │   ├─ Crear evento: "ESTRO_DETECTED"
  │   └─ Fecha: 2025-02-15
  │
  ├─ SISTEMA AVISA:
  │   ├─ Alert: "Estrella en celo - lista para IA"
  │   ├─ Ventana óptima: 12-18 horas desde inicio
  │   └─ Prioridad: media
  │
  └─ INSEMINACIÓN:
      ├─ Técnico selecciona semen
      │   ├─ Por linaje deseado
      │   ├─ Por objetivos genéticos
      │   └─ Registrar # lote semen usado
      │
      ├─ Ejecutar IA
      ├─ BOVINES: registrar
      │   ├─ Evento: "IA_PERFORMED"
      │   ├─ Fecha: 2025-02-15
      │   ├─ Status: PREGNANT (provisional)
      │   ├─ Fecha parto estimada: 2025-11-21 (+280 días)
      │   └─ Semen: lote XYZ
      │
      └─ RESULTADO (21 días después):
          ├─ ¿Vuelve a celo? SI → IA falló, reintentar
          └─ ¿NO regresa a celo? SI → preñez confirmada ✓
              └─ Confirmación ecografía: semana 6
```

---

## Fase 4: Gestación y Preparación al Parto

### Seguimiento de Gestación

```
DURACIÓN: 280 días (9 meses)

Mes 1-3 (Inicio):
  ├─ BOVINES: status = PREGNANT
  ├─ PASTURES: potrero normal (carga media)
  ├─ MILKING: sin registros (aún no ordenando)
  └─ Observación: comportamiento normal

Mes 4-6 (Desarrollo fetal):
  ├─ Crecimiento lento aparente
  ├─ BOVINES: revisar cada semana
  ├─ PASTURES: aumentar quality lentamente
  ├─ Nutrición: suplementar (especialmente Ca, P, Mg)
  └─ BCS: mantener 2.5-3.0

Mes 7 (Semanas 28-32 gestación):
  ├─ Crecimiento fetal acelerado
  ├─ BOVINES: revisión veterinaria (ecografía)
  ├─ Confirmar viabilidad fetal
  ├─ Evaluar tamaño (parto previsto)
  └─ PASTURES: calidad máxima

Mes 8-9 (Últimas 8 semanas):
  ├─ BOVINES: status aún PREGNANT
  ├─ PASTURES: potrero pre-parto
  │   ├─ Máxima calidad de forraje
  │   ├─ Acceso 24/7 a agua fresca
  │   ├─ Suplementación (2kg grain/día)
  │   ├─ Carga reducida (1.0 vaca/ha)
  │   └─ Cercanía a corral para monitoreo
  │
  ├─ MONITOREO:
  │   ├─ Observar 2× diarias (mañana/tarde)
  │   ├─ Signos pre-parto:
  │   │   ├─ Relajación ligamentos pélvicos
  │   │   ├─ Hinchazón ubre
  │   │   ├─ Cambio de comportamiento
  │   │   └─ Pérdida de tapón cervical
  │   │
  │   └─ Cuando aparezcan: preparar corral
  │
  └─ BOVINES: evento parto
      ├─ Timestamp: 2025-11-21 04:30:00
      ├─ Tipo parto: normal/asistido/cesárea
      ├─ Duración: 45 minutos
      ├─ Ternero: sexo, peso (35kg aprox)
      └─ Complicaciones: ninguna
```

### Día del Parto

```
INICIO PARTO (Contracciones observadas):
  │
  ├─ TIMESTAMP: 2025-11-21 04:00:00
  ├─ BOVINES: registrar evento
  │   ├─ labor_start: timestamp
  │   ├─ status: LABORING
  │   └─ location: PRE_PARTO potrero
  │
  ├─ MONITOREO:
  │   ├─ Veterinario en standby
  │   ├─ Revisar cada 30 minutos
  │   ├─ Signos normalidad:
  │   │   ├─ Contracciones progresivas
  │   │   ├─ Placenta visible en 2 horas
  │   │   ├─ Ternero sale en 3-4 horas total
  │   │   └─ Sin hemorragia excesiva
  │   │
  │   └─ Signos problema:
  │       ├─ Placenta en 4+ horas: AYUDA NECESARIA
  │       ├─ Presentación anormal: INTERVENCIÓN
  │       ├─ Hemorragia excesiva: EMERGENCIA
  │       └─ Madre en colapso: EMERGENCIA
  │
  └─ RESULTADO (04:45):
      ├─ Ternero expulsado exitosamente
      ├─ Sexo: macho
      ├─ Peso aprox: 36kg
      ├─ Vitalidad: excelente (respira, intenta pararse)
      └─ Procedimiento: NORMAL ✓

PUERPERIO (Post-parto):
  │
  ├─ PRIMERAS 2 HORAS:
  │   ├─ Secar ternero
  │   ├─ Permitir que madre lo lama (bonding)
  │   ├─ Ternero intenta levantarse (normal después 1h)
  │   ├─ Colocar calostro (dentro de 6 horas)
  │   └─ Observar expulsión de placenta (dentro de 8h)
  │
  ├─ SI PLACENTA NO SALE:
  │   ├─ Esperar hasta 12 horas máximo
  │   ├─ Si sigue retenida: inyectar oxitocina
  │   ├─ Si aún no sale: antibiótico + manual remover (24h)
  │   └─ BOVINES: registrar retención placenta
  │       └─ Alert: riesgo metritis (infección)
  │
  ├─ PRIMERAS 24 HORAS:
  │   ├─ Calostro: 4 litros en 3 tomas (6h, 12h, 24h)
  │   ├─ Agua fresca + electrolitos
  │   ├─ Alimento madre: poquito (solo pasto buenos)
  │   ├─ Observar comportamiento maternal
  │   └─ Temperatura madre: normal (38.0-38.5°C)
  │
  └─ BOVINES ACTUALIZADO:
      ├─ status: LACTATING (cambio automático)
      ├─ parto_date: 2025-11-21
      ├─ ternero_id: (se abre nuevo bovino para el ternero)
      ├─ notas: "Parto normal, ternero macho 36kg"
      └─ health_status: POSTPARTUM (observación)
```

---

## Fase 5: Lactancia y Producción

### Transición a Ordeno (Post-Parto)

```
DÍAS 1-7 POST-PARTO:
  │
  ├─ MANEJO:
  │   ├─ Ternero: con madre (alimentación calostro)
  │   ├─ Madre: en potrero pre-lactancia
  │   ├─ Ordeno: NO aún (solo ternero se alimenta)
  │   └─ Nutrición: máxima calidad forraje
  │
  ├─ BOVINES:
  │   ├─ status: LACTATING
  │   ├─ Observaciones diarias: comportamiento, ubre
  │   ├─ Temperatura diaria (complicaciones post-parto)
  │   └─ Involución uterina: monitorear
  │
  └─ PASTURES:
      └─ Potrero especial lactancia temprana
          ├─ Máxima calidad forraje
          ├─ Carga: 1 vaca/ha (muy bajo)
          ├─ Acceso agua 24/7
          └─ Suplementación concentrado: 2kg/día

SEMANA 2 (Días 8-14):
  │
  ├─ SEPARACIÓN GRADUAL:
  │   ├─ Día 8: separar ternero durante noche
  │   ├─ Día 9: separación 12 horas
  │   ├─ Días 10-14: separación 16 horas
  │   ├─ Objetivo: estimular producción
  │   └─ Ternero sigue con madre 2-3 horas mañana
  │
  ├─ PRIMER ORDENO (Día 14):
  │   ├─ Manual suave (primeros 2-3 ordenos)
  │   ├─ Protocolo sanitario estricto
  │   ├─ Estimulación de reflejo eyección
  │   ├─ Resultado esperado: 6-8 litros
  │   │
  │   └─ MILKING: Primer registro
  │       ├─ bovineId: 47 (Estrella)
  │       ├─ date: 2025-11-28
  │       ├─ shift: AM
  │       ├─ liters: 7.2
  │       ├─ status: completo
  │       ├─ observations: "Primer ordeno post-parto"
  │       └─ recordedBy: "jhonroberth"
  │
  └─ MANEJO SIGUIENTE:
      ├─ Ordeno 2× diarios (AM/PM)
      ├─ Intervalos: 12 horas
      └─ Producción esperada: 8L AM + 7L PM = 15L/día

SEMANA 3-4 (Días 15-28):
  │
  ├─ CURVA DE ASCENSO:
  │   ├─ Día 14: 7.2 + 6.0 = 13.2L
  │   ├─ Día 21: 9.0 + 8.0 = 17.0L
  │   ├─ Día 28: 10.0 + 9.0 = 19.0L
  │   └─ Objetivo pico: semana 6 (20-22L/día)
  │
  ├─ MILKING REGISTROS:
  │   └─ 2 registros diarios durante 28 días
  │       ├─ Total: 56 registros
  │       ├─ Producción acumulada: 420L
  │       ├─ Promedio/día: 15.0L (aún subiendo)
  │       └─ Tendencia: ↑ positiva
  │
  ├─ PASTURES:
  │   ├─ Transición a potrero de lactancia media
  │   ├─ Calidad: 3.2 kg/ha
  │   ├─ Carga: 1.5 vacas/ha
  │   └─ Monitor: respuesta producción
  │
  └─ BOVINES:
      ├─ Health status: NORMAL
      ├─ Notas: "Lactancia progresando bien"
      ├─ Observación mastitis: monitoreando (especial en primiparas)
      └─ Próxima IA: después de 40-50 días post-parto
```

### Ciclo de Lactancia Plena (Semana 5+)

```
PRODUCCIÓN ESTABLE (Meses 2-6):
  │
  ├─ CURVA LACTANCIA:
  │   ├─ Pico: semana 6 (22L/día)
  │   ├─ Persistencia esperada: 85-90%
  │   ├─ Mes 6: 18-19L/día
  │   └─ Declinación: -2 a 3% semanal post-pico
  │
  ├─ MILKING DIARIOS:
  │   ├─ Registros: 2× día (AM/PM)
  │   ├─ Sistema: captura automática de producción
  │   ├─ Alertas:
  │   │   ├─ Si < -10% vs promedio: investigar
  │   │   ├─ Si status "parcial" > 2d: veterinario
  │   │   └─ Si mastitis detectada: cuarentena + tratamiento
  │   │
  │   └─ Análisis:
  │       ├─ Comparar vs predicción inicial (20-22L)
  │       ├─ Estrella produce 21L/día: ✓ EXCELENTE
  │       ├─ Contribución a producción finca: +2%
  │       └─ Status: "Vaca estrella" - mantener
  │
  ├─ PASTURES MANAGEMENT:
  │   ├─ Rotación normal: cada 2 días
  │   ├─ Potrero óptimo: PASTURE_B (mayor respuesta)
  │   ├─ Análisis: si baja 1.5L en mal potrero
  │   │   └─ Priorizar PASTURE_B para Estrella
  │   │
  │   └─ Decisión integrada:
  │       ├─ Alta productora + responde bien a potrero
  │       └─ Resultado: máxima asignación PASTURE_B
  │
  └─ REPRODUCCIÓN:
      ├─ Esperanza: 40-50 días post-parto
      ├─ Detectar ciclo estral
      ├─ Segunda IA: 2025-12-20 (aprox 30d después parto)
      │   └─ Status: PREGNANT (proyección)
      │
      └─ Ciclo lactancia-concepción:
          ├─ Ciclo 1: lactancia plena (120 días)
          ├─ Ciclo 2: lactancia + preñez (160 días)
          ├─ Ciclo 3: secado + preñez (60 días)
          └─ Total: 340 días, luego nuevo parto
```

---

## Fase 6: Evaluación y Decisión de Permanencia

### Análisis Económico (Año 3-4 de Vida)

```
BOVINO #47 "Estrella" - ANÁLISIS AÑO 4:

PRODUCTIVIDAD HISTÓRICA:
├─ Ciclo 1 (parto 1): 6,500L
├─ Ciclo 2 (parto 2): 7,200L
├─ Ciclo 3 (parto 3): 6,800L
├─ Ciclo 4 (parto 4): 6,500L (actual)
└─ TOTAL 4 años: 26,000L

COMPARATIVA:
├─ Promedio finca: 25,000L (4 ciclos)
├─ Estrella vs promedio: +4% ARRIBA
├─ Top 10% finca: 27,000L
├─ Estrella vs top: -3.7% DEBAJO
└─ Conclusión: EXCELENTE desempeño

SALUD:
├─ Incidencias enfermedad: 1 (mastitis año 2)
├─ Tasa enfermedad: 0.5 incidencias/año (promedio: 1.5)
├─ Éxito reproductivo: 95% (preñez tras IA)
├─ Promedio finca: 70%
└─ Conclusión: MUY SALUDABLE

ANÁLISIS FINANCIERO:
├─ Costo alimentación/año: $2,000
├─ Costo atención veterinaria: $150
├─ Costo reproducción: $300
├─ Total costo: $2,450/año
│
├─ Ingreso por leche: $1,600 (6,500L × $0.25/L)
├─ Valor reproductivo: $200 (ternero ocasional)
├─ Total ingreso: $1,800/año
│
├─ Ganancia/pérdida: -$650/año (con leche a $0.25/L)
├─ Break-even: $0.30/L (para ganar $300)
│
└─ DECISIÓN:
    ├─ SI precio leche < $0.28/L: considerar descarte
    ├─ SI precio > $0.30/L: mantener definitivamente
    └─ Actual $0.25/L: MARGINAL, mantener 1-2 ciclos más

PROYECCIÓN:
├─ Edad actual: 4 años 6 meses
├─ Vida útil estimada: hasta 8-10 años
├─ Ciclos restantes: 2-3 (años 5-6)
├─ Producción estimada restante: 13,000-19,500L
└─ Valor actual (descuento): $3,250-4,875
    └─ vs costo futuro: POSITIVO
```

### Decisión Final

```
CRITERIOS DE EVALUACIÓN:
├─ Productividad: ✓ EXCELENTE (arriba del promedio)
├─ Salud: ✓ EXCELENTE (baja enfermedad)
├─ Reproducción: ✓ EXCELENTE (95% éxito)
├─ Genética: ✓ BUENA (linaje destacable)
├─ Económico: ⚠ MARGINAL (depende de precio)
└─ Edad: ⚠ AVANZADA (4.5 años, esperanza 8-10)

RECOMENDACIÓN:
├─ MANTENER por 2 ciclos más (años 5-6)
├─ Monitor precios leche (umbral $0.28/L)
├─ Considerar descendencia para reproducción
│   └─ Si hijas son tan productivas: valorar genética
│
└─ Si desempeño declina:
    ├─ Opción 1: vender como vaca lechera usada (mercado secundario)
    ├─ Opción 2: descarte a procesamiento (carne, cuero)
    ├─ Opción 3: donate a proyecto (valor impositivo)
    └─ Timestamp: coordinar cuando ciclo termina (secado)
```

---

## Puntos Críticos y Decisiones Clave

```
Decisión 1: REGISTRO CORRECTO (Día 0)
  └─ Impacto: Si datos incorrectos, predic erróneas todo ciclo
  └─ Crítica: Alta (determina 4+ años de decisiones)

Decisión 2: EDAD PARA PRIMERA IA (18-24 meses)
  └─ Temprano: menor productividad, complicaciones parto
  └─ Tardío: costo alimentación sin producción
  └─ Crítica: Alta (compromete ciclo reproductivo)

Decisión 3: PROGRAMA REPRODUCTIVO (selección semen)
  └─ Impacto: Genética ternero, performance futuro
  └─ Crítica: Media (importante pero reversible)

Decisión 4: ASIGNACIÓN DE POTRERO (rotación diaria)
  └─ Impacto: ±2-5% producción diaria
  └─ Crítica: Media-Alta (impacto económico diario)

Decisión 5: RESPUESTA A PROBLEMAS (mastitis, baja producción)
  └─ Tardía: cronificación, menor recuperación
  └─ Temprana: mayor tasa éxito (80% vs 40%)
  └─ Crítica: Alta (salva vs sacrifica productividad)

Decisión 6: PERMANENCIA O DESCARTE (años 3-7)
  └─ Impacto: Costo capital a largo plazo
  └─ Crítica: Media-Alta (impacto económico acumulado)
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
