# Cumplimiento ANMAT - Sistema Conitrack
## Documentación para Auditoría de Sistema de Stock

---

## 📋 Contexto del Sistema

**Sistema:** Conitrack - Gestión de Stock Farmacéutico
**Propósito:** Reemplazar planilla Excel para control de inventario
**Tipo:** Sistema de soporte NO crítico (gestión de inventario)
**Clasificación GAMP:** Categoría 5 (Software Custom) con Criticidad BAJA

### ⚠️ Importante: Lo que Conitrack NO es

- ❌ NO gestiona producción
- ❌ NO libera lotes (liberación es externa)
- ❌ NO gestiona análisis de QC (están en Access)
- ❌ NO toma decisiones críticas de calidad/seguridad
- ✅ Solo registra y consulta información de stock

---

## 📁 Documentos Disponibles

### 1. **ANMAT-SIMPLE-RESUMEN.md** ⭐ LEER PRIMERO
**Propósito:** Resumen ejecutivo simplificado
**Contenido:**
- Reclasificación correcta del sistema (GAMP 5, baja criticidad)
- Cambios mínimos requeridos para cumplimiento
- Requisitos eliminados (firma electrónica, validación exhaustiva, MFA)
- Inversión realista: $13K USD en 2-3 meses
- Comparación: Original $270K vs Simplificado $13K (95% reducción)

**Audiencia:** Management, QA, Auditores

---

### 2. **ANMAT-PLAN-1-DESARROLLADOR.md** ⭐ PLAN DE TRABAJO
**Propósito:** Plan detallado de implementación para 1 desarrollador senior
**Contenido:**
- Cronograma semanal de 12 semanas (3 meses)
- Código SQL listo para implementar (triggers audit trail)
- Código Java para seguridad (lockout, contraseñas)
- Templates de documentación (ERU, IQ/OQ, Manual, SOPs)
- Distribución del tiempo: 50% ANMAT / 50% otras tareas
- Presupuesto detallado: $13,000 USD

**Audiencia:** Desarrollador, Project Manager, IT

---

## 🎯 Resumen de Cambios Requeridos

### Mes 1: Implementación Técnica (4 semanas)

| # | Cambio | Tiempo | Prioridad |
|---|--------|--------|-----------|
| 1 | **Audit Trail Básico** | 1.5 sem | Alta |
| 2 | **Backup Automatizado** | 0.5 sem | Alta |
| 3 | **Mejoras Seguridad** | 0.5 sem | Media |
| 4 | **Testing** | 1 sem | Alta |

### Mes 2-3: Documentación (8 semanas)

| # | Documento | Páginas | Tiempo |
|---|-----------|---------|--------|
| 5 | **ERU** - Especificaciones | 15-20 | 2 sem |
| 6 | **IQ/OQ** - Calificación | 20-25 | 2 sem |
| 7 | **Manual Usuario** | 10-15 | 1 sem |
| 8 | **SOP Backup** | 5 | 2 días |
| 9 | **Revisión Final** | - | 2 sem |

---

## 💰 Inversión Total

| Concepto | Monto USD |
|----------|-----------|
| Desarrollador Senior (6 sem FTE) | $0 (recurso interno) |
| Herramientas/Licencias | $500 |
| Consultor BPF (opcional) | $2,000 |
| **TOTAL** | **$2,500** |

**Notas:**
- El desarrollador es **recurso interno** de la empresa (no tercerizado)
- No representa gasto adicional, solo asignación de tiempo del equipo existente
- Si el consultor BPF es opcional, inversión mínima: **$500**

**Comparación:**
- Análisis original (incorrecto): $205K-270K USD, 12 meses
- Análisis simplificado (correcto): $2.5K USD, 3 meses
- **Ahorro: 99%**

---

## ✅ Checklist de Cumplimiento Mínimo

### Técnico
- [ ] Audit trail registra cambios a stock (quién, cuándo, qué, old→new)
- [ ] Vista de consulta de historial de cambios
- [ ] Backup automático semanal configurado
- [ ] Restore probado exitosamente
- [ ] Contraseña mínima 8 caracteres
- [ ] Lockout después de 5 intentos fallidos
- [ ] Expiración contraseña 180 días

### Documentación
- [ ] ERU-001: Especificaciones del Sistema (15-20 pgs)
- [ ] IQ-OQ-001: Protocolo de Calificación (20-25 pgs)
- [ ] Manual de Usuario (10-15 pgs)
- [ ] SOP-BACKUP-001: Procedimiento Backup (5 pgs)
- [ ] Evidencia de restore exitoso
- [ ] Screenshots de tests ejecutados

---

## 🚫 Requisitos Eliminados (NO Aplicables)

### ❌ Firma Electrónica
**Razón:** Sistema NO libera lotes. Liberación es externa.
**Ahorro:** $30K-50K + 6-8 semanas

### ❌ Validación Formal Exhaustiva (200+ tests)
**Razón:** Sistema no crítico, 30-40 tests suficientes.
**Ahorro:** $40K + 8 semanas

### ❌ Multi-Factor Authentication (MFA)
**Razón:** Sistema interno, username/password suficiente.
**Ahorro:** $5K + 2 semanas

### ❌ Gestión de Cambios Formal
**Razón:** Git + buenas prácticas suficientes.
**Ahorro:** $10K + 4 semanas

### ❌ Plan Maestro Validación (50+ páginas)
**Razón:** Sobredimensionado para sistema simple.
**Ahorro:** $5K + 2 semanas

### ❌ Vendor Qualifications
**Razón:** Sistema no crítico.
**Ahorro:** $3K + 1 semana

---

## 📅 Timeline Simplificado

```
┌─────────────────────────────────────────────────────────┐
│                   MES 1 (Técnico)                       │
├─────────────────────────────────────────────────────────┤
│ Sem 1-2: Audit Trail (triggers BD + vista consulta)    │
│ Sem 3:   Backup auto + Seguridad (lockout, passwords)  │
│ Sem 4:   Testing completo                              │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                MES 2-3 (Documentación)                  │
├─────────────────────────────────────────────────────────┤
│ Sem 5-6:  ERU (Especificaciones)                        │
│ Sem 7-8:  IQ/OQ (Calificación + Ejecución)             │
│ Sem 9:    Manual Usuario                                │
│ Sem 10:   SOPs                                          │
│ Sem 11-12: Revisión final + Mock audit (opcional)      │
└─────────────────────────────────────────────────────────┘

RESULTADO: Sistema listo para auditoría ANMAT
```

---

## 🎓 Clasificación GAMP Explicada

### ¿Por qué GAMP 5 y no GAMP 3?

**GAMP 3** = Software comercial estándar (Excel, SAP sin customizar)
- Sistema que compras "off-the-shelf"
- Sin modificaciones de código
- Solo configuración estándar

**GAMP 5** = Software desarrollado a medida (Conitrack)
- Código fuente propio en Java/Spring Boot
- Base de datos diseñada específicamente
- Lógica de negocio custom
- NO es producto comercial

### Criticidad: BAJA (Clave!)

Aunque es GAMP 5, la criticidad es BAJA porque:
- Sistema NO toma decisiones críticas GxP
- NO libera lotes
- NO genera registros para decisiones de calidad
- Solo registra información de inventario

**Fórmula:**
```
GAMP 5 (Custom) + Criticidad BAJA
= Validación SIMPLIFICADA (no exhaustiva)
= $13K, 3 meses (vs $270K, 12 meses para sistema crítico)
```

---

## 🚀 Próximos Pasos Inmediatos

### Esta Semana
1. [ ] Leer **ANMAT-SIMPLE-RESUMEN.md** (30 min)
2. [ ] Leer **ANMAT-PLAN-1-DESARROLLADOR.md** (1 hora)
3. [ ] Aprobar presupuesto ($13K) y timeline (3 meses)
4. [ ] Asignar 50% tiempo desarrollador senior

### Próxima Semana (Inicio Mes 1)
1. [ ] Crear migración `V3__audit_trail_basico.sql`
2. [ ] Implementar triggers de BD para audit trail
3. [ ] Modificar servicios para setear usuario en contexto BD
4. [ ] Crear endpoint `/auditoria/historial`

### Mes 1
1. [ ] Completar audit trail funcional
2. [ ] Configurar backup automático (cron)
3. [ ] Implementar mejoras de seguridad
4. [ ] Testing exhaustivo

### Mes 2-3
1. [ ] Generar documentación (ERU, IQ/OQ, Manual, SOPs)
2. [ ] Ejecutar tests de calificación
3. [ ] Solicitar mock audit (opcional, $2K)
4. [ ] Preparar para auditoría ANMAT oficial

---

## 📞 Contacto y Responsabilidades

### Roles Necesarios

| Rol | Responsabilidad | Dedicación |
|-----|-----------------|------------|
| **Desarrollador Senior** | Implementación técnica + documentación | 50% x 3 meses |
| **QA/Responsable Calidad** | Revisión documentación + aprobaciones | 10% x 3 meses |
| **IT Manager** | Configurar backups, aprobar cambios | 5% x 3 meses |
| **Consultor BPF (opcional)** | Mock audit pre-ANMAT | 2 días |

### Aprobaciones Requeridas

- [ ] Management: Aprobar presupuesto y timeline
- [ ] QA: Aprobar ERU e IQ/OQ
- [ ] IT: Aprobar SOP de Backup
- [ ] Director Técnico: Aprobación final de documentación

---

## ❓ FAQ

**P: ¿Por qué NO necesitamos firma electrónica?**
R: Porque el sistema NO libera lotes. La liberación se hace externamente de forma manual.

**P: ¿Por qué solo 30-40 test cases y no 200+?**
R: Porque es un sistema de BAJA criticidad. No toma decisiones GxP críticas.

**P: ¿Excel también debería validarse?**
R: Sí, pero Excel es GAMP 3 (COTS) y requiere validación más simple (2-3 semanas).

**P: ¿Qué pasa si ANMAT pide más documentación?**
R: Lo mínimo aquí descrito debería ser suficiente. Si piden más, es trabajo adicional no contemplado.

**P: ¿Puedo usar triggers BD o debo usar AOP en Java?**
R: Triggers BD son más simples y robustos. Recomendados para 1 desarrollador.

**P: ¿Qué pasa si no termino en 3 meses?**
R: Prioriza: 1) Audit trail, 2) Backup, 3) ERU e IQ/OQ. Los SOPs pueden ser más breves.

---

## 📚 Referencias Normativas

1. **ANMAT Disposición 4159/23** - Anexo 6: Sistemas Informatizados
2. **ISPE GAMP 5** - Good Automated Manufacturing Practice
3. **ICH Q9** - Quality Risk Management

---

## ✅ Estado del Proyecto

| Documento | Estado | Ubicación |
|-----------|--------|-----------|
| Análisis Simplificado | ✅ Completo | ANMAT-SIMPLE-RESUMEN.md |
| Plan de Trabajo | ✅ Completo | ANMAT-PLAN-1-DESARROLLADOR.md |
| Implementación Técnica | ⏳ Pendiente | - |
| Documentación Validación | ⏳ Pendiente | - |
| Mock Audit | ⏳ Pendiente | - |
| Auditoría ANMAT | ⏳ Pendiente | - |

---

## 🎯 Criterio de Éxito

Sistema pasa auditoría ANMAT cuando cumple:

✅ **5 Mínimos Obligatorios:**
1. Audit trail funcional (registra cambios)
2. Backup automático + restore probado
3. Control de acceso por roles
4. Contraseñas seguras (8+ chars, lockout)
5. Documentación mínima (ERU + IQ/OQ + Manual + SOP)

**Resultado Esperado:**
Inspector ANMAT concluye: "Sistema simple de inventario, reemplaza Excel, cumple requisitos básicos para sistemas de soporte no críticos. **APROBADO**."

---

**Versión:** 1.0 Final
**Fecha:** 2025-11-20
**Estado:** ✅ Listo para Iniciar Implementación

---

## 🏁 Cierre de Análisis

Este análisis está **COMPLETO y LISTO PARA IMPLEMENTAR**.

Los documentos originales sobredimensionados han sido eliminados.
Solo quedan los 2 documentos relevantes + este README.

**Próximo paso:** Iniciar desarrollo técnico según plan de 3 meses.

¡Éxito! 🚀
