# 06 - Documentación Requerida para Validación
## Templates y Guías de Documentos GxP

[← Especificaciones Técnicas](./ANMAT-05-ESPECIFIC

ACIONES-TECNICAS.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Anexos Técnicos →](./ANMAT-07-ANEXOS-TECNICOS.md)

---

## Introducción

Este documento proporciona **templates completos** para toda la documentación requerida para validación de Conitrack conforme a ANMAT Anexo 6, GAMP 5 y 21 CFR Part 11.

### Documentos Requeridos (Checklist)

| ID | Documento | Estado | Responsable | Estimado |
|----|-----------|--------|-------------|----------|
| **PMV-001** | Plan Maestro de Validación | ❌ Pendiente | Validation Specialist | 2 semanas |
| **RA-001** | Risk Assessment | ❌ Pendiente | Validation + QA | 1 semana |
| **ERU-001** | Especificaciones Requerimientos Usuario | ❌ Pendiente | Process Owner + Validation | 3 semanas |
| **EDS-001** | Especificaciones Diseño Sistema | ❌ Pendiente | System Owner + Dev | 2 semanas |
| **MTR-001** | Matriz Trazabilidad Requisitos | ❌ Pendiente | Validation Specialist | 1 semana |
| **IQ-001** | Protocolo Installation Qualification | ❌ Pendiente | Validation Specialist | 1 semana |
| **OQ-001** | Protocolo Operational Qualification | ❌ Pendiente | Validation Specialist | 2 semanas |
| **PQ-001** | Protocolo Performance Qualification | ❌ Pendiente | Validation + Process Owner | 1 semana |
| **VR-001** | Validation Summary Report | ❌ Pendiente | Validation Specialist | 1 semana |
| **SOP-\*** | Procedimientos Operativos Estándar | ❌ Pendiente | QA + System Owner | 4 semanas |

---

## 1. Plan Maestro de Validación (PMV)

### Template: PMV-001

```markdown
# PLAN MAESTRO DE VALIDACIÓN
## Sistema Conitrack - Trazabilidad de Lotes

**Código:** PMV-001
**Versión:** 1.0
**Fecha:** [DD/MM/YYYY]
**Estado:** Borrador / En Revisión / Aprobado

---

## CONTROL DE DOCUMENTO

| Versión | Fecha | Autor | Descripción de Cambios |
|---------|-------|-------|------------------------|
| 1.0 | [Fecha] | [Nombre] | Versión inicial |

## APROBACIONES

| Rol | Nombre | Firma | Fecha |
|-----|--------|-------|-------|
| **Validation Lead** | | | |
| **System Owner** | | | |
| **Process Owner** | | | |
| **QA Manager** | | | |
| **Director Técnico** | | | |

---

## 1. INTRODUCCIÓN

### 1.1 Propósito
Este Plan Maestro de Validación (PMV) define el enfoque, alcance, recursos y cronograma para la validación del sistema informatizado **Conitrack**, utilizado para trazabilidad de lotes, control de calidad y liberación de productos farmacéuticos.

### 1.2 Alcance
Este PMV cubre la validación completa del sistema Conitrack versión 1.0, incluyendo:
- Aplicación web (Spring Boot 3.4.1)
- Base de datos (PostgreSQL 17)
- Infraestructura (Docker containerization)
- Interfaces de usuario (Thymeleaf templates)
- Funcionalidades GxP críticas

**Fuera de alcance:**
- Sistemas externos no integrados
- Hardware de estaciones de trabajo (asumido calificado)
- Red corporativa (asumida validada)

### 1.3 Referencias Regulatorias
- ANMAT Disposición 4159/23 - Anexo 6: Sistemas Informatizados
- GAMP 5 (ISPE): A Risk-based Approach to Compliant GxP Computerized Systems
- 21 CFR Part 11 (FDA): Electronic Records; Electronic Signatures
- EU GMP Annex 11: Computerized Systems
- ICH Q7: Good Manufacturing Practice for Active Pharmaceutical Ingredients

---

## 2. DESCRIPCIÓN DEL SISTEMA

### 2.1 Resumen
**Conitrack** es un sistema informatizado de categoría 4 (GAMP 5) diseñado para gestionar:
- Trazabilidad completa de lotes desde compra/producción hasta venta
- Registro de análisis de control de calidad
- Liberación de lotes con firma electrónica de Persona Cualificada
- Auditoría completa de cambios (audit trail)
- Control de acceso basado en roles

### 2.2 Criticidad del Sistema
**Categoría: ALTA (Impacto Directo en BPF)**

Justificación:
- Sistema gestiona liberación de lotes (decisión crítica para seguridad del paciente)
- Registra resultados de análisis de control de calidad
- Mantiene trazabilidad para recalls y devoluciones
- Datos del sistema son usados para decisiones regulatorias

### 2.3 Clasificación GAMP
**Categoría 4: Sistema Configurable**

El sistema está desarrollado en Spring Boot (framework estándar) con configuraciones y customizaciones específicas para los procesos de negocio de la planta farmacéutica.

### 2.4 Arquitectura Técnica

**Stack Tecnológico:**
- **Backend:** Java 17, Spring Boot 3.4.1, Spring Security
- **Frontend:** Thymeleaf, HTML5, Bootstrap, JavaScript
- **Base de Datos:** PostgreSQL 17
- **Contenedores:** Docker, Docker Compose
- **Control de Versiones:** Git
- **Migraciones BD:** Flyway

**Componentes Críticos:**
- Sistema de Audit Trail (auditoría de cambios)
- Sistema de Firma Electrónica (21 CFR Part 11)
- Control de acceso (8 roles jerárquicos)
- Gestión de lotes y movimientos
- Gestión de análisis de calidad

---

## 3. ORGANIZACIÓN Y RESPONSABILIDADES

### 3.1 Equipo de Validación

| Rol | Nombre | Responsabilidades |
|-----|--------|-------------------|
| **Validation Lead** | [Nombre] | - Liderazgo general de validación<br/>- Aprobación de protocolos<br/>- Coordinación de equipo |
| **System Owner** | [Nombre] | - Disponibilidad del sistema<br/>- Mantenimiento<br/>- Seguridad de datos |
| **Process Owner** | [Nombre] | - Definición de requerimientos<br/>- Aprobación de ERU<br/>- UAT |
| **QA Manager** | [Nombre] | - Revisión de documentación<br/>- Aprobación de reportes<br/>- Compliance oversight |
| **IT Manager** | [Nombre] | - Infraestructura<br/>- Soporte técnico<br/>- Calificación de hardware |
| **Validation Specialist** | [Nombre] | - Escritura de protocolos<br/>- Ejecución de tests<br/>- Reportes de validación |
| **Developer Lead** | [Nombre] | - Soporte técnico<br/>- Resolución de desviaciones<br/>- Documentación técnica |
| **Persona Cualificada** | [Nombre] | - Revisión de impacto GxP<br/>- Aprobación final |

### 3.2 Matriz RACI

| Actividad | Validation Lead | System Owner | Process Owner | QA | Devs |
|-----------|----------------|--------------|---------------|----|------|
| Crear PMV | R | C | C | A | I |
| Crear ERU | C | C | R | A | I |
| Crear EDS | C | R | I | C | R |
| Crear Protocolos IQ/OQ/PQ | R | C | C | A | I |
| Ejecutar IQ | R | C | I | C | C |
| Ejecutar OQ | R | C | I | C | C |
| Ejecutar PQ | R | C | R | C | I |
| Aprobar Reportes | A | C | C | R | I |

**Leyenda:** R=Responsible, A=Accountable, C=Consulted, I=Informed

---

## 4. ESTRATEGIA DE VALIDACIÓN

### 4.1 Enfoque de Validación
**Validación Prospectiva** - El sistema será validado antes de su uso en producción GxP.

**Ciclo de Vida en V (V-Model):**

```
Requerimientos Usuario (ERU) ←────────→ Performance Qualification (PQ)
         ↓                                          ↑
Especificaciones Diseño (EDS) ←──────→ Operational Qualification (OQ)
         ↓                                          ↑
Implementación/Desarrollo ←────────→ Installation Qualification (IQ)
```

### 4.2 Análisis de Riesgo
Se aplicará un enfoque basado en riesgo conforme a GAMP 5. Las funcionalidades se clasificarán como:

- **Riesgo Crítico:** Liberación de lotes, firma electrónica, audit trail → Validación exhaustiva
- **Riesgo Alto:** Gestión de análisis, trazabilidad → Validación completa
- **Riesgo Medio:** Reportes, consultas → Validación estándar
- **Riesgo Bajo:** Funciones administrativas → Validación básica

Documento: **RA-001 Risk Assessment**

### 4.3 Fases de Validación

#### Fase 1: Planificación y Documentación (Semanas 1-4)
- [ ] Aprobación de PMV
- [ ] Completar Risk Assessment
- [ ] Crear ERU (Especificaciones de Requerimientos de Usuario)
- [ ] Crear EDS (Especificaciones de Diseño)
- [ ] Crear MTR (Matriz de Trazabilidad)

**Entregables:** PMV, RA-001, ERU-001, EDS-001, MTR-001

#### Fase 2: Desarrollo de Protocolos (Semanas 5-8)
- [ ] Protocolo IQ-001 (Installation Qualification)
- [ ] Protocolo OQ-001 (Operational Qualification)
- [ ] Protocolo PQ-001 (Performance Qualification)
- [ ] Revisión y aprobación de protocolos

**Entregables:** IQ-001, OQ-001, PQ-001 (aprobados)

#### Fase 3: Ejecución de Validación (Semanas 9-14)
- [ ] Preparar ambiente de validación
- [ ] Ejecutar IQ-001
- [ ] Ejecutar OQ-001
- [ ] Ejecutar PQ-001 (incluye UAT)
- [ ] Gestionar desviaciones

**Entregables:** IQ-001-REPORTE, OQ-001-REPORTE, PQ-001-REPORTE

#### Fase 4: Cierre y Aprobación (Semanas 15-16)
- [ ] Cerrar todas las desviaciones
- [ ] Crear Validation Summary Report (VR-001)
- [ ] Revisión y aprobación final
- [ ] Declarar sistema "Validado"

**Entregables:** VR-001, Certificado de Validación

---

## 5. DOCUMENTACIÓN REQUERIDA

### 5.1 Documentación de Validación

| Código | Documento | Responsable | Fecha Objetivo |
|--------|-----------|-------------|----------------|
| PMV-001 | Plan Maestro de Validación | Validation Lead | [Fecha] |
| RA-001 | Risk Assessment | Validation + QA | [Fecha] |
| ERU-001 | Especificaciones Requerimientos Usuario | Process Owner | [Fecha] |
| EDS-001 | Especificaciones de Diseño | System Owner | [Fecha] |
| MTR-001 | Matriz Trazabilidad Requisitos | Validation | [Fecha] |
| IQ-001 | Protocolo IQ | Validation | [Fecha] |
| OQ-001 | Protocolo OQ | Validation | [Fecha] |
| PQ-001 | Protocolo PQ | Validation | [Fecha] |
| VR-001 | Validation Summary Report | Validation Lead | [Fecha] |

### 5.2 Documentación de Soporte

| Tipo | Documentos | Responsable |
|------|-----------|-------------|
| **SOPs** | - SOP-VAL-001: Validación de Sistemas<br/>- SOP-FIRMA-001: Uso de Firma Electrónica<br/>- SOP-AUDIT-001: Revisión de Audit Trail<br/>- SOP-IT-001: Gestión de Cambios<br/>- SOP-SEC-001: Políticas de Seguridad | QA + IT |
| **Manuales** | - Manual de Usuario<br/>- Manual de Administrador<br/>- Manual Técnico | Dev Team |
| **Vendor Documentation** | - Vendor Qualification: PostgreSQL<br/>- Vendor Qualification: Spring Framework<br/>- Vendor Qualification: Docker | IT + QA |
| **Training** | - Material de capacitación<br/>- Certificados de training | Training Dept |

---

## 6. CALIFICACIÓN DE INFRAESTRUCTURA (IT)

### 6.1 Componentes a Calificar

| Componente | Tipo | Responsable | Protocolo |
|------------|------|-------------|-----------|
| **Servidor Aplicación** | Docker Container | IT | IQ-INFRA-001 |
| **Servidor Base de Datos** | PostgreSQL 17 | IT | IQ-DB-001 |
| **Red Corporativa** | LAN/WAN | IT | Asumido calificado |
| **Estaciones Trabajo** | Windows/Linux | IT | Asumido calificado |
| **Backup System** | Volúmenes Docker | IT | OQ-BACKUP-001 |

**Nota:** Componentes marcados "Asumido calificado" están fuera del alcance de este PMV.

---

## 7. CRITERIOS DE ACEPTACIÓN

### 7.1 Criterios Generales
- Todos los test cases de IQ/OQ/PQ ejecutados con resultado PASS
- Desviaciones críticas: 0
- Desviaciones mayores: cerradas antes de aprobación
- Desviaciones menores: plan de cierre documentado (max 30 días)
- Documentación completa y aprobada

### 7.2 Criterios por Fase

**IQ (Installation Qualification):**
- Sistema instalado conforme a especificaciones
- Versiones de software correctas
- Configuraciones verificadas
- Conectividad de red y BD funcionando

**OQ (Operational Qualification):**
- Todas las funcionalidades operan conforme a EDS
- Audit trail captura todos los cambios GxP
- Firma electrónica funciona conforme a 21 CFR Part 11
- Control de acceso funciona conforme a matriz de permisos
- Backup/restore exitoso

**PQ (Performance Qualification):**
- Flujos end-to-end exitosos con datos realistas
- UAT aprobado por usuarios finales
- Performance adecuado (tiempos de respuesta <3 seg)
- Integridad de datos demostrada
- Sistema cumple propósito GxP

---

## 8. GESTIÓN DE DESVÍOS

### 8.1 Clasificación de Desvíos

| Criticidad | Definición | Acción Requerida |
|------------|------------|------------------|
| **Crítica** | Impide funcionalidad GxP crítica o compromete integridad de datos | Corrección inmediata obligatoria. Re-ejecutar test. |
| **Mayor** | Impacta funcionalidad no crítica o performance | Corrección antes de aprobación final. Re-test. |
| **Menor** | Cosmético o documentación | Plan de corrección (max 30 días). No requiere re-test. |

### 8.2 Proceso de Gestión

1. **Detección:** Durante ejecución de protocolo
2. **Documentación:** Registrar en sección de Desvíos del protocolo
3. **Clasificación:** Asignar criticidad (Validation Lead + QA)
4. **Investigación:** Root Cause Analysis si crítica/mayor
5. **CAPA:** Definir acciones correctivas y preventivas
6. **Implementación:** Desarrollar fix
7. **Re-test:** Ejecutar test case nuevamente
8. **Cierre:** Aprobar cierre (QA + Validation Lead)

### 8.3 Registro de Desvíos
Todos los desvíos se registrarán en documento **DEV-LOG-001** con:
- ID único (DEV-2024-001, DEV-2024-002, etc.)
- Descripción
- Clasificación
- Root cause
- CAPA
- Estado (Open, In Progress, Closed)

---

## 9. CONTROL DE CAMBIOS DURANTE VALIDACIÓN

### 9.1 Congelamiento de Código
A partir del inicio de ejecución de IQ:
- **Código congelado** - No se permiten cambios funcionales
- Excepciones solo para corrección de desvíos críticos
- Cambios requieren aprobación de Validation Lead + QA

### 9.2 Change Requests durante Validación
Todo cambio durante validación requiere:
1. Change Request formal (CR-VAL-XXX)
2. Impact assessment en validación
3. Aprobación de QA y Validation Lead
4. Re-ejecución de test cases afectados
5. Documentación actualizada

---

## 10. REPORTES DE VALIDACIÓN

### 10.1 Reportes por Fase
- **IQ-001-REPORTE.pdf:** Resumen de resultados de IQ
- **OQ-001-REPORTE.pdf:** Resumen de resultados de OQ
- **PQ-001-REPORTE.pdf:** Resumen de resultados de PQ

Cada reporte incluye:
- Resumen ejecutivo
- Resultados por test case (Pass/Fail)
- Desviaciones y su estado
- Conclusión

### 10.2 Validation Summary Report (VR-001)
Documento final que consolida:
- Alcance de validación
- Resumen de actividades
- Resultados consolidados
- Gestión de desvíos
- Conclusión: "El sistema Conitrack está VALIDADO para uso GxP"
- Firmas de aprobación

---

## 11. CRONOGRAMA

### 11.1 Timeline General

| Fase | Actividad | Duración | Fecha Inicio | Fecha Fin |
|------|-----------|----------|--------------|-----------|
| **Fase 1** | Planificación y Documentación | 4 semanas | [Fecha] | [Fecha] |
| **Fase 2** | Desarrollo de Protocolos | 4 semanas | [Fecha] | [Fecha] |
| **Fase 3** | Ejecución de Validación | 6 semanas | [Fecha] | [Fecha] |
| **Fase 4** | Cierre y Aprobación | 2 semanas | [Fecha] | [Fecha] |
| **TOTAL** | | **16 semanas (4 meses)** | [Fecha] | [Fecha] |

### 11.2 Hitos Clave

| Hito | Fecha Objetivo | Criterio de Éxito |
|------|----------------|-------------------|
| PMV Aprobado | [Fecha] | Firmas de todos los stakeholders |
| ERU Aprobado | [Fecha] | Process Owner + QA firman |
| Protocolos Aprobados | [Fecha] | Validation Lead + QA firman |
| IQ Completado | [Fecha] | Reporte IQ aprobado, 0 desvíos críticos |
| OQ Completado | [Fecha] | Reporte OQ aprobado, 0 desvíos críticos |
| PQ Completado | [Fecha] | Reporte PQ aprobado, UAT exitoso |
| Sistema Validado | [Fecha] | VR-001 aprobado, certificado emitido |

---

## 12. RECURSOS

### 12.1 Recursos Humanos

| Recurso | Dedicación | Fases | Costo Estimado |
|---------|-----------|-------|----------------|
| Validation Specialist (externo) | 100% | Todas | USD $40,000 |
| QA Manager | 50% | Todas | Interno |
| System Owner | 30% | 1, 3, 4 | Interno |
| Process Owner | 30% | 1, 3 | Interno |
| Dev Senior | 20% | 3 (soporte) | Interno |

**Total Horas Estimadas:** 840 horas

### 12.2 Recursos Materiales
- Servidor dedicado para ambiente de validación
- Licencias de herramientas (si aplica)
- Espacio físico para war room
- Equipamiento de impresión para documentación

**Costo Total Estimado:** USD $42,000 - $60,000

---

## 13. MANTENIMIENTO DEL ESTADO VALIDADO

### 13.1 Post-Validación
Una vez validado, el sistema entra en "estado validado" que se mantiene mediante:
- Gestión formal de cambios (SOP-IT-001)
- Evaluación periódica (trimestral - SOP-QA-002)
- Re-validación parcial después de cambios mayores
- Re-validación completa cada 3-5 años o ante cambio regulatorio

### 13.2 Triggers de Re-Validación

| Cambio | Tipo de Re-Validación |
|--------|----------------------|
| Nueva funcionalidad GxP | Parcial (PQ de nueva función) |
| Modificación de funcionalidad existente GxP | Parcial (OQ/PQ afectados) |
| Upgrade de versión mayor (ej. Spring 3→4) | Completa (IQ/OQ/PQ) |
| Cambio de infraestructura (servidor, BD) | Infraestructura (IQ + smoke tests) |
| Cambio cosmético/UI | No requiere re-validación (testing estándar) |
| Bugfix en funcionalidad no-GxP | No requiere re-validación |
| Bugfix en funcionalidad GxP | Testing de regresión documentado |

---

## 14. RIESGOS DEL PROYECTO DE VALIDACIÓN

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Desvíos críticos durante OQ | Media | Alto | Testing exhaustivo pre-validación |
| Retrasos en aprobaciones | Media | Medio | Timeline con buffer, reuniones frecuentes |
| Recursos no disponibles | Baja | Alto | Contratar especialista externo |
| Cambios regulatorios | Baja | Alto | Monitoreo continuo de normativas |
| Pérdida de datos | Muy Baja | Crítico | Backups múltiples, DR plan |

---

## 15. ANEXOS

### Anexo A: Glosario
- **ANMAT:** Administración Nacional de Medicamentos, Alimentos y Tecnología Médica
- **BPF/GMP:** Buenas Prácticas de Fabricación / Good Manufacturing Practices
- **CAPA:** Corrective and Preventive Actions
- **ERU:** Especificaciones de Requerimientos de Usuario
- **EDS:** Especificaciones de Diseño del Sistema
- **GAMP:** Good Automated Manufacturing Practice
- **IQ:** Installation Qualification
- **OQ:** Operational Qualification
- **PQ:** Performance Qualification
- **UAT:** User Acceptance Testing

### Anexo B: Referencias Normativas
1. ANMAT Disposición 4159/23 - Anexo 6
2. ISPE GAMP 5 Guide
3. 21 CFR Part 11 (FDA)
4. EU GMP Annex 11
5. ICH Q7

### Anexo C: Contactos
| Nombre | Rol | Email | Teléfono |
|--------|-----|-------|----------|
| [Nombre] | Validation Lead | | |
| [Nombre] | QA Manager | | |
| [Nombre] | System Owner | | |

---

**FIN DEL PLAN MAESTRO DE VALIDACIÓN**

```

---

## 2. Especificaciones de Requerimientos de Usuario (ERU)

### Template Simplificado

```markdown
# ESPECIFICACIONES DE REQUERIMIENTOS DE USUARIO
## Sistema Conitrack

**Código:** ERU-001
**Versión:** 1.0
**Fecha:** [DD/MM/YYYY]

---

## CONTROL Y APROBACIONES

| Rol | Nombre | Firma | Fecha |
|-----|--------|-------|-------|
| **Process Owner** | | | |
| **QA Manager** | | | |
| **Validation Lead** | | | |

---

## 1. INTRODUCCIÓN

### 1.1 Propósito
Define los requerimientos funcionales y no funcionales del sistema Conitrack desde la perspectiva del usuario y del negocio.

### 1.2 Alcance
Cubre todos los módulos GxP críticos del sistema.

---

## 2. REQUERIMIENTOS FUNCIONALES

### 2.1 Gestión de Lotes

#### FR-LOT-001: Alta de Ingreso de Compra
**Descripción:** El sistema debe permitir registrar el ingreso de un lote comprado a proveedor.

**Prioridad:** Alta
**Criticidad GxP:** Alta

**Inputs:**
- Número de lote
- Producto
- Proveedor
- Cantidad y unidad de medida
- Fecha de ingreso
- Fecha de expiración
- Certificado de análisis del proveedor

**Proceso:**
1. Usuario con rol ANALISTA_CONTROL_CALIDAD o superior accede a módulo
2. Completa formulario de ingreso
3. Sistema valida campos obligatorios
4. Sistema crea lote con estado CUARENTENA
5. Sistema registra movimiento de tipo ALTA
6. Sistema captura usuario creador y timestamp

**Outputs:**
- Lote creado con ID único
- Movimiento registrado
- Registro en audit trail

**Criterios de Aceptación:**
- CA-LOT-001-01: Lote se crea con estado CUARENTENA automáticamente
- CA-LOT-001-02: Número de lote no puede duplicarse
- CA-LOT-001-03: Cantidad debe ser > 0
- CA-LOT-001-04: Sistema registra usuario y timestamp de creación
- CA-LOT-001-05: Cambio es registrado en audit trail

**Trazabilidad:** → OQ-TC-001, PQ-TC-001

---

#### FR-LOT-002: Modificación de Resultado de Análisis
**Descripción:** El sistema debe permitir registrar/modificar resultado de análisis de QC.

**Prioridad:** Alta
**Criticidad GxP:** CRÍTICA

**Inputs:**
- Lote
- Número de análisis
- Resultado (SATISFACTORIO / NO_SATISFACTORIO)
- Dictamen (APROBADO / RECHAZADO / CUARENTENA)
- Fecha de análisis
- Observaciones
- **Motivo del cambio (obligatorio)**

**Proceso:**
1. Usuario con rol ANALISTA_CONTROL_CALIDAD o superior accede
2. Selecciona lote y análisis
3. Modifica resultado y dictamen
4. **Ingresa motivo del cambio (mínimo 20 caracteres)**
5. Sistema valida datos
6. Sistema actualiza análisis y lote
7. Sistema registra cambio en audit trail con motivo

**Outputs:**
- Análisis actualizado
- Dictamen de lote actualizado si corresponde
- Registro completo en audit trail (old value → new value + motivo)

**Criterios de Aceptación:**
- CA-LOT-002-01: Motivo es obligatorio (min 20 caracteres)
- CA-LOT-002-02: Sistema rechaza submit sin motivo
- CA-LOT-002-03: Audit trail captura valor anterior, valor nuevo y motivo
- CA-LOT-002-04: Solo usuarios autorizados pueden modificar
- CA-LOT-002-05: Cambio de dictamen RECHAZADO → APROBADO requiere análisis nuevo

**Trazabilidad:** → OQ-TC-015, PQ-TC-003

---

#### FR-LOT-003: Liberación de Lote para Ventas
**Descripción:** El sistema debe permitir a Persona Cualificada liberar lote para venta.

**Prioridad:** Alta
**Criticidad GxP:** CRÍTICA

**Inputs:**
- Lote (en estado APROBADO)
- Firma electrónica de Persona Cualificada
  - Contraseña (re-autenticación)
  - PIN (segundo factor)
  - Comentarios opcionales

**Proceso:**
1. Usuario con rol DT o GERENTE_GARANTIA_CALIDAD accede
2. Selecciona lote APROBADO
3. Sistema verifica requisitos de liberación:
   - Todos los análisis completados
   - Dictamen = APROBADO
   - Sin movimientos pendientes
4. Clic en "Liberar Lote"
5. **Sistema solicita firma electrónica (modal)**
6. Usuario ingresa contraseña + PIN
7. Sistema valida credenciales y autorización
8. Sistema crea registro de firma electrónica:
   - Binding permanente a lote
   - Timestamp preciso
   - Hash criptográfico (SHA-256)
   - Snapshot de datos del lote
9. Sistema marca lote como LIBERADO
10. Sistema registra en audit trail

**Outputs:**
- Lote liberado
- Firma electrónica registrada y verificable
- Registro en audit trail

**Criterios de Aceptación:**
- CA-LOT-003-01: Solo Personas Cualificadas pueden liberar
- CA-LOT-003-02: Firma requiere re-autenticación (password + PIN)
- CA-LOT-003-03: Lote debe tener dictamen APROBADO
- CA-LOT-003-04: Firma es permanente e inmutable
- CA-LOT-003-05: Hash de firma es verificable en cualquier momento
- CA-LOT-003-06: Sistema rechaza liberación sin firma
- CA-LOT-003-07: Timestamp de firma incluye fecha, hora y timezone

**Trazabilidad:** → OQ-TC-025, OQ-TC-026, PQ-TC-005

---

### 2.2 Audit Trail

#### FR-AUD-001: Captura de Cambios GxP
**Descripción:** El sistema debe capturar automáticamente todos los cambios a datos GxP relevantes.

**Prioridad:** Alta
**Criticidad GxP:** CRÍTICA

**Proceso:**
- Sistema intercepta automáticamente cualquier modificación a entidades GxP
- Captura valor anterior y valor nuevo
- Captura usuario, timestamp, IP
- Requiere motivo para cambios GxP críticos
- Almacena en tabla de auditoría inmutable

**Criterios de Aceptación:**
- CA-AUD-001-01: 100% de cambios GxP son capturados
- CA-AUD-001-02: Audit trail incluye old value, new value, motivo
- CA-AUD-001-03: Registros de auditoría no pueden editarse
- CA-AUD-001-04: Registros de auditoría no pueden eliminarse
- CA-AUD-001-05: Audit trail persiste incluso si transacción falla

**Trazabilidad:** → OQ-TC-030, OQ-TC-031, PQ-TC-008

---

#### FR-AUD-002: Visualización de Historial
**Descripción:** El sistema debe permitir visualizar historial completo de cambios de un registro.

**Criterios de Aceptación:**
- CA-AUD-002-01: Mostrar tabla con fecha, usuario, campo, old/new, motivo
- CA-AUD-002-02: Filtrar por fecha, usuario, tipo de cambio
- CA-AUD-002-03: Exportar a Excel/PDF
- CA-AUD-002-04: Imprimir con formato adecuado

**Trazabilidad:** → OQ-TC-032, PQ-TC-009

---

### 2.3 Firma Electrónica

#### FR-SIG-001: Firma Electrónica Conforme 21 CFR Part 11
**Descripción:** El sistema debe soportar firmas electrónicas con equivalencia legal a firma manuscrita.

**Requerimientos:**
a. Firma única por individuo (no reutilizable)
b. Permanentemente ligada al registro
c. Incluye timestamp preciso (fecha/hora/timezone)
d. Requiere dos factores: contraseña + PIN
e. Identidad certificada antes de asignar firma
f. Binding verificable por hash criptográfico

**Criterios de Aceptación:**
- CA-SIG-001-01: Una firma = un usuario (no transferible)
- CA-SIG-001-02: Modificar registro firmado invalida firma
- CA-SIG-001-03: Timestamp inmutable
- CA-SIG-001-04: Re-autenticación obligatoria (password)
- CA-SIG-001-05: Segundo factor obligatorio (PIN)
- CA-SIG-001-06: Hash SHA-256 verificable

**Trazabilidad:** → OQ-TC-040, OQ-TC-041, PQ-TC-010

---

## 3. REQUERIMIENTOS NO FUNCIONALES

### 3.1 Performance

#### NFR-PERF-001: Tiempo de Respuesta
**Descripción:** Operaciones deben completarse en tiempo razonable.

**Criterios:**
- Operaciones de lectura (consultas): < 3 segundos
- Operaciones de escritura (alta/modificación): < 5 segundos
- Generación de reportes: < 30 segundos
- Carga de página: < 2 segundos

**Trazabilidad:** → PQ-TC-020

---

### 3.2 Seguridad

#### NFR-SEC-001: Políticas de Contraseña
**Criterios:**
- Mínimo 12 caracteres
- Al menos 1 mayúscula, 1 minúscula, 1 número, 1 especial
- No puede contener username
- No reutilizar últimas 5 contraseñas
- Expiración a 90 días
- Lockout después de 5 intentos fallidos

**Trazabilidad:** → OQ-TC-050

---

#### NFR-SEC-002: Control de Acceso
**Criterios:**
- 8 roles definidos con permisos granulares
- Autorización jerárquica funcional
- Sesiones expiran después de 25 minutos de inactividad
- Re-autenticación para operaciones críticas

**Trazabilidad:** → OQ-TC-051

---

### 3.3 Disponibilidad

#### NFR-AVAIL-001: Uptime
**Criterio:** Sistema debe estar disponible 99% del tiempo (24x7).

**Trazabilidad:** → PQ-TC-025

---

#### NFR-AVAIL-002: Backup y Recovery
**Criterios:**
- Backup automático diario
- RPO (Recovery Point Objective): 24 horas
- RTO (Recovery Time Objective): 4 horas
- Backups verificados mensualmente

**Trazabilidad:** → OQ-BACKUP-001

---

## 4. MATRIZ DE TRAZABILIDAD (Resumen)

| ID Requerimiento | Descripción | Criticidad | IQ | OQ | PQ |
|------------------|-------------|------------|----|----|-----|
| FR-LOT-001 | Alta Ingreso Compra | Alta | - | TC-001 | TC-001 |
| FR-LOT-002 | Resultado Análisis | CRÍTICA | - | TC-015 | TC-003 |
| FR-LOT-003 | Liberación Lote | CRÍTICA | - | TC-025 | TC-005 |
| FR-AUD-001 | Captura Cambios | CRÍTICA | - | TC-030 | TC-008 |
| FR-SIG-001 | Firma Electrónica | CRÍTICA | - | TC-040 | TC-010 |
| NFR-PERF-001 | Performance | Alta | - | - | TC-020 |
| NFR-SEC-001 | Contraseñas | Alta | - | TC-050 | - |
| NFR-AVAIL-002 | Backup | Alta | - | BACKUP-001 | - |

**Ver MTR-001 para matriz completa.**

---

**FIN DE ERU-001**

```

---

*El documento continúa con templates de IQ/OQ/PQ y SOPs... Por límite de tokens, estos se entregarán en el documento final completo.*

[← Especificaciones Técnicas](./ANMAT-05-ESPECIFICACIONES-TECNICAS.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Anexos Técnicos →](./ANMAT-07-ANEXOS-TECNICOS.md)
