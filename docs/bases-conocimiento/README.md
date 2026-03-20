# 🧠 Bases de Conocimiento - Cattle Chatbot

**Última actualización**: 2026-02-03

---

## 📋 Descripción

Esta carpeta contiene las bases de conocimiento utilizadas por el chatbot de Amazon Bedrock para responder consultas sobre el sistema ganadero.

---

## 📁 Estructura

```
bases-conocimiento/
├── README.md                 # Este archivo
├── cattle.csv                # Datos consolidados generales
└── knowledge-base/           # CSVs temáticos
    ├── 01_kb_sanidad.csv           # Sanidad animal
    ├── 02_kb_nutricion.csv         # Nutrición y alimentación
    ├── 03_kb_pasticultura.csv      # Manejo de pastos
    ├── 04_kb_potreros_rotacion.csv # Rotación de potreros
    ├── 05_kb_normativas.csv        # Normativas y regulaciones
    ├── 06_kb_faq_app.csv           # Preguntas frecuentes de la app
    └── 07_kb_lecciones_aprendidas.csv # Lecciones aprendidas
```

---

## 📊 Archivos Disponibles

| Archivo | Descripción | Registros |
|---------|-------------|-----------|
| `cattle.csv` | Datos consolidados del sistema | Variable |
| `01_kb_sanidad.csv` | Información sobre vacunas, desparasitación, enfermedades | ~50+ |
| `02_kb_nutricion.csv` | Requerimientos nutricionales por categoría | ~30+ |
| `03_kb_pasticultura.csv` | Especies forrajeras, manejo de praderas | ~40+ |
| `04_kb_potreros_rotacion.csv` | Reglas de rotación, descanso, aforo | ~35+ |
| `05_kb_normativas.csv` | Regulaciones, trazabilidad, normativas | ~20+ |
| `06_kb_faq_app.csv` | Preguntas frecuentes de la aplicación | ~60+ |
| `07_kb_lecciones_aprendidas.csv` | Experiencias y mejores prácticas | ~25+ |

---

## 🔗 Uso en el Sistema

El **ContextBuilderService** del chatbot utiliza estos archivos para:
1. Enriquecer el contexto de las consultas
2. Proporcionar información especializada
3. Mejorar la precisión de las respuestas de Claude 3

---

## 📝 Formato de los CSVs

Cada archivo sigue el formato:
```csv
id,categoria,pregunta,respuesta,fuente,fecha_actualizacion
```

---

## 🔄 Actualización

Para agregar nuevos conocimientos:
1. Editar el CSV correspondiente
2. Mantener el formato establecido
3. Actualizar la fecha de última modificación
