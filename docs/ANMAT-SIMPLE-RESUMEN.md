# Análisis de Cumplimiento ANMAT - Sistema de Stock Conitrack
## Versión Simplificada para Sistema de Inventario

**Fecha:** 2025-11-20
**Sistema:** Conitrack - Sistema de Gestión de Stock
**Propósito:** Reemplazar planilla Excel para control de inventario

---

## ⚠️ IMPORTANTE: Reclasificación del Sistema

### Contexto Real del Sistema

**LO QUE CONITRACK NO ES:**
- ❌ NO es un ERP
- ❌ NO gestiona producción
- ❌ NO libera lotes (liberación es externa)
- ❌ NO gestiona análisis de QC (están en Access)
- ❌ NO es un sistema crítico para decisiones GxP

**LO QUE CONITRACK SÍ ES:**
- ✅ Sistema de gestión de stock/inventario
- ✅ Reemplaza planilla Excel manual
- ✅ Refleja estados de stock actualizados por procesos externos
- ✅ Proporciona trazabilidad de movimientos de inventario
- ✅ Control de acceso y permisos

### Reclasificación GAMP

**Original (incorrecto):** GAMP Categoría 4 - Sistema Crítico GxP
**Real:** GAMP Categoría 1 o 3 - Sistema de Soporte

**Justificación:**
- Sistema NO toma decisiones críticas de calidad/seguridad
- Sistema NO genera registros que se usan para liberación de lotes
- Sistema solo registra y consulta información de stock
- Similar a una planilla Excel (que no requiere validación formal completa)

### Impacto en Requisitos ANMAT

**Cambio dramático en requerimientos:**

| Requisito ANMAT | Sistema Crítico GxP | Sistema de Stock | Cambio |
|-----------------|---------------------|------------------|--------|
| **Firma Electrónica** (Req. 14-15) | ✅ Obligatorio | ❌ NO requerido | Eliminado |
| **Validación Formal IQ/OQ/PQ** (Req. 4) | ✅ Completa | ⚠️ Simplificada | Reducida 70% |
| **Audit Trail Completo** (Req. 9) | ✅ Old/New values + motivo | ⚠️ Básico | Reducido 50% |
| **Gestión Cambios Formal** (Req. 10) | ✅ Change Control formal | ⚠️ Informal OK | Simplificado |
| **Backup Automático** (Req. 7) | ✅ Diario + verificación | ✅ Semanal OK | Igual |
| **Control Acceso** (Req. 12) | ✅ MFA + políticas estrictas | ✅ Básico OK | Simplificado |

---

## Requisitos Mínimos para Pasar Auditoría

### 1. ✅ Control de Acceso (Req. 12) - YA CUMPLE MAYORMENTE

**Estado Actual:** 80% cumplido

**Lo que ya existe:**
- ✅ 8 roles con permisos definidos
- ✅ BCrypt password hashing
- ✅ Control de acceso por endpoint
- ✅ Sesiones con timeout

**Mejoras mínimas necesarias:**
```
PRIORIDAD MEDIA:
- Aumentar mínimo de contraseña de 3 a 8 caracteres
- Agregar expiración de contraseña (opcional: 180 días)
- Implementar lockout básico (5 intentos fallidos)

TIEMPO: 1 semana
COSTO: ~$2,000 USD
```

---

### 2. ⚠️ Audit Trail Básico (Req. 9) - NECESITA MEJORAS

**Estado Actual:** 45% cumplido

**Lo que ya existe:**
- ✅ `AuditoriaAcceso` registra accesos de usuarios
- ✅ Tabla con IP, timestamp, usuario, acción

**Mejoras mínimas necesarias:**
```
PRIORIDAD ALTA:
- Agregar audit trail de cambios a registros de stock
- Capturar: usuario, fecha, campo modificado, valor anterior → valor nuevo
- NO requiere "motivo" obligatorio (sistema no crítico)
- Suficiente con logging automático

IMPLEMENTACIÓN:
1. Tabla auditoria_cambios_stock (versión simple)
2. Trigger en BD o Aspect AOP básico
3. Vista de consulta de historial

TIEMPO: 2 semanas
COSTO: ~$4,000 USD
```

**Esquema simplificado:**
```sql
CREATE TABLE auditoria_cambios_stock (
    id BIGSERIAL PRIMARY KEY,
    tabla VARCHAR(50),           -- 'lote', 'movimiento'
    registro_id BIGINT,
    campo VARCHAR(50),
    valor_anterior TEXT,
    valor_nuevo TEXT,
    usuario_id BIGINT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);
```

---

### 3. ✅ Backup y Recuperación (Req. 7) - NECESITA AUTOMATIZACIÓN

**Estado Actual:** 60% cumplido

**Lo que ya existe:**
- ✅ Script de backup manual (`custom_backup.sh`)
- ✅ Volúmenes Docker persistentes

**Mejoras mínimas necesarias:**
```
PRIORIDAD ALTA:
- Automatizar backup (cron job diario o semanal)
- Probar restore al menos una vez
- Documentar procedimiento de recuperación

IMPLEMENTACIÓN:
1. Cron job en servidor: 0 2 * * 0 (domingos 2am)
2. Retener últimos 4 backups (1 mes)
3. Test de restore manual documentado

TIEMPO: 3 días
COSTO: ~$1,000 USD
```

---

### 4. ⚠️ Documentación Mínima (Req. 4) - CRÍTICO

**Estado Actual:** 20% cumplido

**Lo que falta:**
- ❌ No hay documentación de validación

**Documentación MÍNIMA requerida:**
```
PRIORIDAD CRÍTICA:

1. DOCUMENTO DE ESPECIFICACIONES (ERU simplificado)
   - Descripción del sistema
   - Funcionalidades principales
   - Usuarios y roles
   - ~5-10 páginas
   TIEMPO: 1 semana

2. PROTOCOLO DE CALIFICACIÓN SIMPLIFICADO (IQ/OQ combinado)
   - Verificar instalación correcta
   - Probar funcionalidades principales (10-15 test cases)
   - Documentar resultados
   - ~15-20 páginas
   TIEMPO: 2 semanas

3. MANUAL DE USUARIO
   - Cómo usar el sistema
   - Screenshots
   - ~10-15 páginas
   TIEMPO: 1 semana

4. SOP BÁSICO DE RESPALDO
   - Procedimiento de backup
   - Procedimiento de restore
   - ~3-5 páginas
   TIEMPO: 2 días

TOTAL DOCUMENTACIÓN: 3-4 semanas
COSTO: ~$6,000 USD
```

---

### 5. ⚠️ Gestión de Cambios Informal (Req. 10) - ACEPTABLE

**Estado Actual:** 40% cumplido

**Lo que ya existe:**
- ✅ Git para control de versiones
- ✅ Flyway para migraciones de BD

**Mejoras mínimas necesarias:**
```
PRIORIDAD BAJA (nice to have):
- Documentar en Git commit messages:
  - Qué se cambió
  - Por qué
  - Quién aprobó
- Crear changelog simple (CHANGELOG.md)
- NO requiere Change Control formal

TIEMPO: Ongoing (buena práctica)
COSTO: $0 (disciplina de equipo)
```

---

### 6. ✅ Integridad de Datos (Req. 5) - YA CUMPLE

**Estado Actual:** 90% cumplido

**Lo que ya existe:**
- ✅ Validaciones de entrada (Jakarta Validation)
- ✅ Transacciones ACID
- ✅ Foreign keys en BD
- ✅ Soft deletes

**Mejoras necesarias:** Ninguna crítica

---

## Resumen: Plan Mínimo de Cumplimiento

### Inversión Total Reducida

| Tarea | Prioridad | Tiempo | Costo USD |
|-------|-----------|--------|-----------|
| **1. Audit Trail Básico** | Alta | 2 semanas | $0 (recurso interno) |
| **2. Automatización Backup** | Alta | 3 días | $0 (recurso interno) |
| **3. Documentación Mínima** | Crítica | 4 semanas | $0 (recurso interno) |
| **4. Mejoras Seguridad** | Media | 1 semana | $0 (recurso interno) |
| **5. Herramientas/Licencias** | Media | - | $500 |
| **6. Consultor BPF (opcional)** | Baja | 2 días | $2,000 |
| **TOTAL** | | **7-8 semanas** | **$2,500** |

**Nota:** El desarrollador es recurso interno de la empresa, por lo que no representa un gasto adicional, solo una asignación de tiempo.

### Comparación con Análisis Original

| Métrica | Análisis Original (Incorrecto) | Análisis Simplificado (Correcto) | Reducción |
|---------|--------------------------------|----------------------------------|-----------|
| **Duración** | 12 meses | 2 meses | -83% |
| **Inversión** | $205K-270K USD | $2.5K USD | -99% |
| **Firma Electrónica** | Requerida | NO requerida | Eliminado |
| **Validación Formal** | IQ/OQ/PQ completo | Calificación simplificada | -70% |
| **Documentación** | 10+ docs (200+ pgs) | 4 docs (~40 pgs) | -80% |

**Nota:** El análisis simplificado considera desarrollador interno. Si fuera externo, sumar ~$10K adicionales.

---

## Plan de Implementación Simplificado

### Fase Única: 2 Meses

#### Semana 1-2: Audit Trail Básico
- [ ] Crear tabla `auditoria_cambios_stock`
- [ ] Implementar captura automática (triggers o AOP simple)
- [ ] Vista de consulta de historial
- [ ] Testing básico

#### Semana 3: Mejoras de Seguridad
- [ ] Aumentar mínimo contraseña a 8 caracteres
- [ ] Implementar lockout básico (5 intentos)
- [ ] Considerar expiración de contraseña (180 días)

#### Semana 4: Backup Automatizado
- [ ] Configurar cron job de backup
- [ ] Test de restore
- [ ] Documentar procedimiento

#### Semana 5-8: Documentación
- [ ] Semana 5: Especificaciones del Sistema (ERU simplificado)
- [ ] Semana 6-7: Protocolo de Calificación (IQ/OQ)
- [ ] Semana 7: Manual de Usuario
- [ ] Semana 8: SOP de Backup + Revisión final

---

## Requisitos Eliminados (NO Aplicables)

### ❌ Firma Electrónica (Req. 14-15)
**Razón:** Sistema NO libera lotes. Liberación es externa.
**Ahorro:** ~$30K-50K USD + 6-8 semanas

### ❌ Validación Formal Completa (Req. 4)
**Razón:** Sistema no crítico, reemplaza Excel.
**Validación simplificada suficiente.**
**Ahorro:** ~$40K USD + 12 semanas

### ❌ Gestión de Cambios Formal (Req. 10)
**Razón:** Sistema simple, Git + buenas prácticas suficientes.
**Ahorro:** ~$10K USD + 4 semanas

### ❌ Gestión de Incidencias (Req. 13)
**Razón:** Sistema no crítico, incidentes se manejan informalmente.
**Ahorro:** ~$5K USD + 2 semanas

### ❌ Evaluación Periódica Formal (Req. 11)
**Razón:** Revisión informal anual suficiente.
**Ahorro:** ~$3K USD/año

### ❌ Multi-Factor Authentication (MFA)
**Razón:** Sistema interno, username/password suficiente.
**Ahorro:** ~$5K USD + 2 semanas

---

## Criterios de Aceptación para Auditoría

### Para que el sistema pase auditoría ANMAT:

✅ **Mínimos obligatorios:**
1. Audit trail registra cambios a stock (quién, cuándo, qué)
2. Backup automático funcional con restore probado
3. Control de acceso por roles funcional
4. Documentación mínima (ERU + IQ/OQ + Manual + SOP)
5. Contraseñas razonablemente seguras (8+ caracteres)

⚠️ **Recomendables (no bloqueantes):**
1. Lockout después de intentos fallidos
2. Expiración de contraseñas
3. Changelog de versiones
4. Training básico de usuarios

❌ **NO requeridos para este tipo de sistema:**
1. Firma electrónica
2. Validación formal exhaustiva (IQ/OQ/PQ 100+ tests)
3. Plan Maestro de Validación
4. Sistema de gestión de cambios formal
5. Vendor qualifications
6. Disaster Recovery Plan formal

---

## Preguntas Clave para Inspector ANMAT

**P1: "¿Liberan lotes con este sistema?"**
R: No, la liberación es externa. Este sistema solo registra el stock.

**P2: "¿Toman decisiones de calidad con este sistema?"**
R: No, los análisis de QC están en Access. Esto es solo inventario.

**P3: "¿Tienen audit trail?"**
R: Sí, registramos quién modificó qué y cuándo.

**P4: "¿Tienen backups?"**
R: Sí, backups automáticos semanales con restore probado.

**P5: "¿Está documentado el sistema?"**
R: Sí, tenemos especificaciones, protocolo de calificación y manual de usuario.

**P6: "¿Validaron el sistema?"**
R: Sí, ejecutamos calificación IQ/OQ con resultados documentados.

**Conclusión esperada:** Sistema simple de inventario, reemplaza Excel, cumple requisitos básicos de ANMAT Anexo 6 para sistemas de soporte no críticos. **APROBADO**.

---

## Recomendación Final

**Sistema Conitrack en su contexto real:**
- Es un sistema de **BAJO RIESGO** (no crítico GxP)
- Requiere **CUMPLIMIENTO BÁSICO** de ANMAT Anexo 6
- Inversión razonable: **$13K USD en 2 meses**
- Documentación mínima suficiente
- **NO necesita firma electrónica ni validación exhaustiva**

**Próximos pasos:**
1. Implementar audit trail básico (Semana 1-2)
2. Automatizar backup (Semana 3)
3. Generar documentación mínima (Semana 4-8)
4. Realizar calificación simplificada (Semana 6-7)
5. Solicitar auditoría interna pre-ANMAT (Semana 8)

**Riesgo de no aprobar auditoría:** BAJO (si se implementan los 5 mínimos obligatorios)

---

**Versión:** 2.0 - Simplificada para Sistema de Stock
**Fecha:** 2025-11-20
**Estado:** Recomendado para aprobación
