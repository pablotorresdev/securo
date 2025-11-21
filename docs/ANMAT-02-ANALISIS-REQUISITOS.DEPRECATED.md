# 02 - Análisis Detallado por Requisito
## Comparación Punto a Punto con ANMAT Anexo 6

[← Resumen Ejecutivo](./ANMAT-01-RESUMEN-EJECUTIVO.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Gap Analysis →](./ANMAT-03-GAP-ANALYSIS.md)

---

## Requisito 1: Gestión de Riesgos

### 1.1 Gestión de Riesgos en Ciclo de Vida

**Texto ANMAT:**
> "La gestión de riesgos debe aplicarse durante el ciclo de vida del sistema informatizado teniendo en cuenta la seguridad del paciente, la integridad de datos y la calidad del producto. Como parte del sistema de gestión de riesgos, las decisiones sobre la extensión de la validación y de los controles de la integridad de datos deben basarse en una evaluación de riesgos del sistema informatizado justificada y documentada."

**Estado: 🔴 NO CUMPLE (40%)**

#### Evidencia en Código Actual

**LO QUE EXISTE:**
```java
// Algunas validaciones de riesgo implícitas en validators
// service/cu/validator/FechaValidator.java
public boolean validarFechaReanalisisPosteriorFechaExpiracionProveedor(
    LocalDate fechaReanalisis, LocalDate fechaExpiracionProveedor) {
    // Validación de negocio que reduce riesgo de lote expirado
    return fechaReanalisis.isBefore(fechaExpiracionProveedor);
}
```

**LO QUE FALTA:**
- ❌ No existe documento formal de "Risk Assessment" del sistema
- ❌ No se clasifica criticidad de funcionalidades (Alto/Medio/Bajo riesgo)
- ❌ No se documenta el análisis de riesgo que justifica nivel de validación
- ❌ No hay matriz de riesgo para integridad de datos
- ❌ No se evalúa impacto en seguridad del paciente por funcionalidad

#### Implementación Requerida

**Documento a Crear:**
```
CONITRACK-RA-001: Risk Assessment del Sistema
├── Sección 1: Clasificación GAMP
│   └── Categoría 4: Sistema configurable con impacto directo GxP
├── Sección 2: Análisis de Criticidad por Módulo
│   ├── Alta: Liberación de lotes, Resultados de análisis
│   ├── Media: Trazabilidad, Movimientos de inventario
│   └── Baja: Reportes, Consultas
├── Sección 3: Matriz de Riesgo (Probabilidad × Impacto)
│   └── Por cada funcionalidad crítica
├── Sección 4: Controles de Mitigación
│   ├── Firma electrónica para liberación
│   ├── Audit trail para cambios críticos
│   ├── Validación doble para entrada manual
│   └── Backup automático para continuidad
└── Sección 5: Justificación de Nivel de Validación
    └── IQ/OQ/PQ completo (sistema categoría 4 crítico)
```

**Ubicación Sugerida:** `C:\opt\securo\docs\validacion\CONITRACK-RA-001-Risk-Assessment.md`

#### Archivo Referencia
- **Código:** N/A (no hay implementación técnica, solo documental)
- **Documentación:** docs/validacion/CONITRACK-RA-001-Risk-Assessment.md (a crear)

---

## Requisito 2: Personal

### 2.1 Cooperación entre Personal y Cualificación

**Texto ANMAT:**
> "Debe existir una cooperación estrecha entre todo el personal relevante entre los que se encuentra el propietario del proceso (process owner), el propietario del sistema (system owner), las Personas Cualificadas e informática (IT). Todo el personal debe disponer de la cualificación apropiada, el nivel de acceso y tener definidas sus responsabilidades para llevar a cabo las tareas asignadas."

**Estado: 🟢 CUMPLE PARCIALMENTE (80%)**

#### Evidencia en Código Actual

**LO QUE EXISTE:**

1. **Roles Jerárquicos Definidos:**
```java
// enums/RoleEnum.java
public enum RoleEnum {
    ADMIN(6, true, true, false),
    DT(5, true, true, false),  // Director Técnico
    GERENTE_GARANTIA_CALIDAD(4, true, true, false),
    GERENTE_CONTROL_CALIDAD(3, true, true, false),
    SUPERVISOR_PLANTA(3, true, true, false),
    ANALISTA_CONTROL_CALIDAD(2, true, true, false),
    ANALISTA_PLANTA(2, true, true, false),
    AUDITOR(1, true, false, true);  // Solo lectura

    private final int hierarchyLevel;
    private final boolean canView;
    private final boolean canModify;
    private final boolean isReadOnly;
}
```

2. **Permisos Granulares por Caso de Uso:**
```java
// enums/PermisosCasoUsoEnum.java
ALTA_INGRESO_COMPRA("/cu/ingreso-compra/**",
    Set.of(ADMIN, DT, GERENTE_GARANTIA_CALIDAD, ANALISTA_CONTROL_CALIDAD)),

MODIF_RESULTADO_ANALISIS("/cu/resultado-analisis/**",
    Set.of(ADMIN, DT, GERENTE_CONTROL_CALIDAD, ANALISTA_CONTROL_CALIDAD)),

MODIF_LIBERACION_VENTAS("/cu/liberacion-ventas/**",
    Set.of(ADMIN, DT, GERENTE_GARANTIA_CALIDAD));  // Solo roles senior
```

3. **Autorización Jerárquica:**
```java
// service/ReversoAuthorizationService.java
public void validateReversoAuthorization(User currentUser, User creatorUser) {
    // Solo el creador o un superior jerárquico puede reversar
    if (currentUser.getId().equals(creatorUser.getId())) {
        return; // Mismo usuario, OK
    }

    if (currentUser.getRole().getHierarchyLevel() >
        creatorUser.getRole().getHierarchyLevel()) {
        return; // Superior jerárquico, OK
    }

    throw new SecurityException("No autorizado para reversar esta transacción");
}
```

**LO QUE FALTA:**
- ❌ No hay tabla de "Process Owner" / "System Owner" formal
- ❌ No se registra cualificación/certificación de usuarios
- ❌ No hay fecha de capacitación en BPF por usuario
- ❌ No se valida que usuario tenga training antes de operar sistema crítico
- ❌ No hay definición formal de responsabilidades por rol (solo permisos técnicos)

#### Implementación Requerida

**Nueva Entidad: `UserQualification`**
```java
// entity/maestro/UserQualification.java
@Entity
@Table(name = "user_qualifications")
public class UserQualification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QualificationType type;  // BPF_TRAINING, SYSTEM_TRAINING, etc.

    @Column(name = "qualification_date", nullable = false)
    private LocalDate qualificationDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "certificado_numero")
    private String certificadoNumero;

    @Column(name = "instructor")
    private String instructor;

    public boolean isExpired() {
        return expirationDate != null && LocalDate.now().isAfter(expirationDate);
    }
}
```

**Nueva Tabla: `system_roles`**
```sql
-- Definición formal de roles regulatorios
CREATE TABLE system_roles (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_type VARCHAR(50) NOT NULL,  -- PROCESS_OWNER, SYSTEM_OWNER, QUALIFIED_PERSON
    assigned_date DATE NOT NULL,
    revoked_date DATE,
    responsibilities TEXT,  -- Descripción formal de responsabilidades
    CONSTRAINT fk_system_roles_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### Archivos Referencia
- ✅ **Roles:** src/main/java/com/mb/conitrack/entity/maestro/Role.java
- ✅ **Enum Roles:** src/main/java/com/mb/conitrack/enums/RoleEnum.java
- ✅ **Permisos:** src/main/java/com/mb/conitrack/enums/PermisosCasoUsoEnum.java
- ✅ **Autorización:** src/main/java/com/mb/conitrack/service/ReversoAuthorizationService.java
- ❌ **Qualifications:** src/main/java/com/mb/conitrack/entity/maestro/UserQualification.java (a crear)

---

## Requisito 3: Proveedores y Proveedores de Servicios

### 3.1 Acuerdos Formales con Terceros

**Texto ANMAT:**
> "Cuando se emplea a terceros (como proveedores, proveedores de servicios) por ejemplo para suministrar, instalar, configurar, integrar, validar, mantener (ej. vía acceso remoto), modificar o conservar un sistema informatizado o un servicio relacionado o para el procesado de datos, tienen que existir acuerdos formales entre el fabricante y tercero."

**Estado: 🔴 NO CUMPLE (30%)**

#### Evidencia en Código Actual

**LO QUE EXISTE:**
```yaml
# docker-compose.yml - Uso de imágenes de terceros
services:
  postgres-db:
    image: postgres:17  # Proveedor: PostgreSQL Global Development Group
    # ❌ No hay Quality Agreement formal con PostgreSQL Foundation

  spring-app:
    # Dependencias de terceros en pom.xml/build.gradle
    # ❌ No hay evaluación de proveedores documentada
```

**Terceros Identificados en el Proyecto:**
1. **PostgreSQL** (base de datos)
2. **Spring Framework** (framework aplicación)
3. **Docker** (containerización)
4. **Thymeleaf** (template engine)
5. **Posibles proveedores de firma electrónica** (DocuSign, Adobe Sign - futuro)
6. **Proveedor de hosting/cloud** (si aplica)

**LO QUE FALTA:**
- ❌ No existe carpeta de "Vendor Qualification"
- ❌ No hay Quality Agreements con proveedores críticos
- ❌ No se documenta evaluación de competencia de proveedores
- ❌ No hay contratos de confidencialidad (NDA) registrados
- ❌ No se audita a proveedores de servicios críticos
- ❌ No hay lista de proveedores aprobados

### 3.2 Competencia y Fiabilidad del Proveedor

**Texto ANMAT:**
> "La competencia y la fiabilidad del proveedor son factores claves a la hora de seleccionar un producto o proveedor de servicios. La necesidad de realizar una auditoría debe basarse en una evaluación de riesgos."

**Estado: 🔴 NO CUMPLE (30%)**

**LO QUE FALTA:**
- ❌ No hay criterios documentados de selección de proveedores
- ❌ No se realiza risk assessment de proveedores
- ❌ No hay auditorías a proveedores registradas

### 3.3 Revisión de Documentación de Software Comercial

**Texto ANMAT:**
> "La documentación entregada con los software comerciales (commercial off-the-shelf software) debe revisarse por usuarios regulados para comprobar que los requerimientos de usuario se satisfacen."

**Estado: 🔴 NO CUMPLE (20%)**

**Software Comercial Usado:**
- PostgreSQL 17
- Spring Boot 3.4.1
- Docker
- (Futuro: SDK de firma electrónica)

**LO QUE FALTA:**
- ❌ No hay "Vendor Documentation Review" para PostgreSQL
- ❌ No se verifica que Spring Boot cumple requisitos GxP
- ❌ No hay checklist de evaluación de software comercial

### 3.4 y 3.5 Sistema de Calidad del Proveedor y Confidencialidad

**Estado: 🔴 NO CUMPLE (0%)**

**LO QUE FALTA:**
- ❌ No se solicita certificados de calidad de proveedores (ISO 9001, etc.)
- ❌ No hay información de auditorías disponible para inspectores
- ❌ No existen NDAs con proveedores con acceso remoto

#### Implementación Requerida

**Estructura de Carpetas a Crear:**
```
C:\opt\securo\docs\proveedores\
├── 00-VENDOR-QUALIFICATION-POLICY.md
├── 01-approved-vendors-list.xlsx
├── PostgreSQL\
│   ├── VQ-POSTGRESQL-001-Vendor-Assessment.pdf
│   ├── VQ-POSTGRESQL-002-Documentation-Review.pdf
│   └── VQ-POSTGRESQL-003-Quality-Agreement.pdf
├── Spring-Framework\
│   ├── VQ-SPRING-001-Vendor-Assessment.pdf
│   └── VQ-SPRING-002-Documentation-Review.pdf
├── Docker\
│   ├── VQ-DOCKER-001-Vendor-Assessment.pdf
│   └── VQ-DOCKER-002-Documentation-Review.pdf
└── Firma-Electronica\  (cuando se seleccione)
    ├── VQ-ESIGN-001-Vendor-Selection-Matrix.xlsx
    ├── VQ-ESIGN-002-Risk-Assessment.pdf
    ├── VQ-ESIGN-003-Quality-Agreement.pdf
    └── VQ-ESIGN-004-NDA.pdf
```

**Template: Vendor Qualification Checklist**
```markdown
# Vendor Qualification: [VENDOR NAME]
**Producto/Servicio:** [...]
**Criticidad:** [ ] Alta  [ ] Media  [ ] Baja
**Fecha Evaluación:** [...]

## 1. Competencia del Proveedor
- [ ] Proveedor tiene >5 años en el mercado
- [ ] Proveedor tiene clientes en industria regulada
- [ ] Proveedor tiene certificación ISO 9001 o equivalente
- [ ] Proveedor proporciona soporte técnico 24/7

## 2. Fiabilidad del Producto
- [ ] Producto ampliamente usado (>10,000 instalaciones)
- [ ] Producto tiene historial de updates de seguridad
- [ ] Proveedor publica CVEs y patches regularmente
- [ ] Documentación técnica completa disponible

## 3. Evaluación de Riesgo
- [ ] Riesgo de discontinuidad: BAJO/MEDIO/ALTO
- [ ] Riesgo de vulnerabilidades: BAJO/MEDIO/ALTO
- [ ] Riesgo de pérdida de datos: BAJO/MEDIO/ALTO
- [ ] Auditoría requerida: SÍ/NO (justificar)

## 4. Documentación Revisada
- [ ] Manual de usuario revisado
- [ ] Especificaciones técnicas revisadas
- [ ] Requisitos de usuario satisfechos (checklist adjunto)
- [ ] Certificados de calidad recibidos

## 5. Acuerdos Contractuales
- [ ] Quality Agreement firmado
- [ ] NDA firmado (si acceso a datos)
- [ ] SLA definido y aceptado
- [ ] Términos de soporte acordados

**Aprobación:**
- System Owner: _________________ Fecha: _______
- QA: _________________ Fecha: _______
```

#### Archivos Referencia
- ❌ **Vendor Qualification:** docs/proveedores/ (carpeta completa a crear)
- ❌ **Quality Agreements:** docs/proveedores/[VENDOR]/Quality-Agreement.pdf (a crear)

---

## Requisito 4: Validación

### 4.1 Documentación de Validación

**Texto ANMAT:**
> "La documentación de validación y los informes deben cubrir los pasos relevantes del ciclo de vida del sistema. Los fabricantes deben ser capaces de justificar sus estándares, protocolos, criterios de aceptación, procedimientos y registros basados en su evaluación de riesgos."

**Estado: 🔴 NO CUMPLE (20%)**

#### Evidencia en Código Actual

**LO QUE EXISTE:**
```java
// Tests unitarios en src/test/java/com/mb/conitrack/
// Ejemplo: service/cu/AltaIngresoCompraServiceTest.java
@Test
void debe_crearLoteEIngresoCompra_cuandoDatosValidos() {
    // Test unitario, NO es test de validación formal
    // ❌ No está vinculado a especificación de requisitos
    // ❌ No tiene protocolo aprobado por QA
    // ❌ No genera reporte de validación firmado
}
```

**Tests existentes:**
- ✅ Tests unitarios con JUnit y Mockito
- ✅ Cobertura de código con JaCoCo
- ❌ **NO son tests de validación formal GxP**

**LO QUE FALTA - Documentos de Validación:**
```
docs/validacion/
├── PMV-001-Plan-Maestro-Validacion.md
├── ERU-001-Especificaciones-Requerimientos-Usuario.md
├── EDS-001-Especificaciones-Diseño-Sistema.md
├── MTR-001-Matriz-Trazabilidad-Requisitos.xlsx
├── protocolos/
│   ├── IQ-001-Instalacion-Calificacion.md
│   ├── OQ-001-Operacion-Calificacion.md
│   └── PQ-001-Performance-Calificacion.md
├── reportes/
│   ├── IQ-001-REPORTE-Instalacion.pdf
│   ├── OQ-001-REPORTE-Operacion.pdf
│   └── PQ-001-REPORTE-Performance.pdf
└── resumen/
    └── VR-001-Validation-Summary-Report.pdf
```

### 4.2 Registros de Controles de Cambio

**Texto ANMAT:**
> "La documentación de validación debe incluir los registros de controles de cambio - si aplican - y los informes de cualquier desviación observada durante el proceso de validación."

**Estado: 🔴 NO CUMPLE (30%)**

**LO QUE EXISTE:**
```bash
# Git commits como control de versiones
git log --oneline
# 9ae769e Adding tests
# 56336c2 Refactoring
# 517a8af Adding coverage
# ❌ No vinculado a sistema de gestión de cambios formal
# ❌ No documenta impacto en validación
```

**LO QUE FALTA:**
- ❌ No hay sistema formal de Change Control
- ❌ No se documentan desviaciones durante validación
- ❌ No se evalúa si cambio requiere re-validación

### 4.3 Lista Actualizada de Sistemas GxP

**Texto ANMAT:**
> "Debe disponerse de una lista actualizada (inventario) de todos los sistemas relevantes y su funcionalidad en relación con las BPF."

**Estado: 🔴 NO CUMPLE (0%)**

**LO QUE FALTA:**
```markdown
# INVENTARIO-SISTEMAS-GXP.md

## Sistemas Informatizados GxP - Planta Farmacéutica XYZ

| ID Sistema | Nombre | Criticidad | Funcionalidad BPF | Estado Validación | Versión | Propietario |
|------------|--------|------------|-------------------|-------------------|---------|-------------|
| SYS-001 | Conitrack | ALTA | Liberación lotes, trazabilidad, control calidad | En proceso validación | 1.0 | [System Owner] |
| SYS-002 | LIMS (futuro) | ALTA | Resultados de análisis | No implementado | - | - |
| SYS-003 | ERP (si aplica) | MEDIA | Gestión inventario, compras | Validado | 2.5 | [Owner] |

**Para Conitrack (SYS-001):**
- Módulos GxP:
  - Alta Ingreso Compra (ALTA criticidad)
  - Resultado Análisis (ALTA criticidad)
  - Liberación Ventas (ALTA criticidad)
  - Trazado Lote (ALTA criticidad)
  - Muestreo Bulto (MEDIA criticidad)
```

### 4.4 Especificaciones de Requerimientos de Usuario (ERU)

**Texto ANMAT:**
> "Las especificaciones de requerimientos de usuario deben describir las funciones requeridas del sistema informatizado y deben basarse en una evaluación de riesgos documentada."

**Estado: 🔴 NO CUMPLE (0%)**

**LO QUE FALTA:**
Ver sección completa en documento 06 (Documentación Requerida).

Ejemplo de ERU requerido:
```markdown
# ERU-REQ-001: Liberación de Lotes

## Descripción
El sistema debe permitir a las Personas Cualificadas liberar lotes para venta solo cuando:
- Todos los análisis estén completos
- Resultados cumplan especificaciones
- Documentación esté completa

## Criticidad
**ALTA** - Impacto directo en seguridad del paciente

## Requisitos Funcionales
1. RF-LIB-001: Sistema DEBE requerir firma electrónica de Persona Cualificada
2. RF-LIB-002: Sistema DEBE verificar que dictamen = APROBADO
3. RF-LIB-003: Sistema DEBE prevenir liberación si análisis pendientes
4. RF-LIB-004: Sistema DEBE registrar fecha/hora exacta de liberación

## Criterios de Aceptación
- CA-LIB-001: Usuario sin rol "Persona Cualificada" NO puede liberar lote
- CA-LIB-002: Liberación sin firma electrónica es rechazada
- CA-LIB-003: Lote con dictamen RECHAZADO no puede liberarse
```

### 4.5-4.9 Otros Requisitos de Validación

Ver documento 06 (Documentación Requerida) para plantillas completas.

#### Archivos Referencia
- ❌ **Plan Maestro Validación:** docs/validacion/PMV-001-Plan-Maestro-Validacion.md (a crear)
- ❌ **ERU:** docs/validacion/ERU-001-Especificaciones-Requerimientos-Usuario.md (a crear)
- ❌ **Protocolos IQ/OQ/PQ:** docs/validacion/protocolos/ (carpeta completa a crear)
- ✅ **Tests Unitarios:** src/test/java/com/mb/conitrack/ (existen, pero no son tests de validación)

---

## Requisito 5: Datos

**Texto ANMAT:**
> "Los sistemas informatizados que intercambian datos electrónicamente con otros sistemas deben incluir comprobaciones intrínsecas adecuadas de la entrada y el procesado correcto y seguro de datos, de cara a minimizar riesgos."

**Estado: 🟡 CUMPLE PARCIALMENTE (70%)**

#### Evidencia en Código Actual

**LO QUE EXISTE:**

1. **Validaciones de Entrada:**
```java
// dto/LoteDTO.java
@NotNull(message = "El número de lote es obligatorio")
@Size(min = 1, max = 50)
private String numeroLote;

@NotNull(message = "La cantidad es obligatoria")
@DecimalMin(value = "0.0001", message = "La cantidad debe ser mayor a 0")
private BigDecimal cantidad;

// Validación Jakarta Bean Validation
```

2. **Transacciones ACID:**
```java
// service/cu/AltaIngresoCompraService.java
@Transactional
public Lote altaIngresoCompra(AltaIngresoCompraDTO dto) {
    // Toda la operación es atómica
    // Si falla cualquier paso, rollback automático
}
```

3. **Validadores de Negocio:**
```java
// service/cu/validator/CantidadValidator.java
public boolean validarCantidadIngresoContraBultos(
    BigDecimal cantidadIngreso, List<BultoDTO> bultos) {

    BigDecimal sumatoriaBultos = bultos.stream()
        .map(BultoDTO::getCantidad)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Comprobación intrínseca: cantidad total = suma bultos
    return cantidadIngreso.compareTo(sumatoriaBultos) == 0;
}
```

**LO QUE FALTA:**
- ❌ No hay checksums/hashes para validar integridad de datos transferidos
- ❌ No se valida formato de datos en imports/exports (si existen)
- ❌ No hay validación de datos en interfaces con sistemas externos (actualmente no hay, pero podría haberlos)

**Recomendación:**
Si en el futuro se implementan interfaces con:
- LIMS (Laboratory Information Management System)
- ERP externo
- Sistemas de proveedores

Implementar:
```java
// service/DataExchangeValidator.java
public class DataExchangeValidator {

    public boolean validateDataIntegrity(String data, String expectedHash) {
        String actualHash = DigestUtils.sha256Hex(data);
        return actualHash.equals(expectedHash);
    }

    public boolean validateDataFormat(String xmlData, String xsdSchema) {
        // Validar XML contra XSD
    }
}
```

#### Archivos Referencia
- ✅ **Validadores:** src/main/java/com/mb/conitrack/service/cu/validator/*.java
- ✅ **Transacciones:** src/main/java/com/mb/conitrack/service/cu/*.java (@Transactional)
- ✅ **DTO Validation:** src/main/java/com/mb/conitrack/dto/*.java

---

## Requisito 6: Comprobaciones de Exactitud

**Texto ANMAT:**
> "Para la entrada manual de datos críticos, debe existir una comprobación adicional de la exactitud de los datos. Esta comprobación puede realizarse por un segundo operario o por medios electrónicos validados."

**Estado: 🟡 CUMPLE PARCIALMENTE (50%)**

#### Evidencia en Código Actual

**LO QUE EXISTE:**
```java
// Validaciones automáticas en tiempo de entrada
// controller/cu/AltaIngresoCompraController.java
@PostMapping("/guardar")
public String guardar(@Valid AltaIngresoCompraDTO dto, BindingResult result) {
    if (result.hasErrors()) {
        // Validación electrónica automática de campos
        return "cu/ingreso-compra/form";
    }
    // Procesar...
}
```

**LO QUE FALTA:**

1. **No hay doble entrada (double data entry) para datos críticos:**
```java
// A IMPLEMENTAR:
// entity/PendingCriticalData.java
@Entity
public class PendingCriticalData {
    private String dataType;  // RESULTADO_ANALISIS, LIBERACION_LOTE, etc.
    private String jsonData;  // Datos ingresados por primer operario
    private User firstOperator;
    private OffsetDateTime firstEntryTime;

    private User secondOperator;  // Verificador
    private OffsetDateTime verificationTime;
    private Boolean verified;  // true si coinciden las entradas
}
```

2. **No se identifica qué datos son "críticos":**

Datos que DEBERÍAN requerir doble entrada:
- Resultado de análisis (APROBADO/RECHAZADO)
- Liberación de lote para ventas
- Cantidad de ingreso de compra (grandes volúmenes)
- Fecha de expiración de lote

**Implementación Sugerida:**
```java
// enums/CriticalDataType.java
public enum CriticalDataType {
    RESULTADO_ANALISIS("Resultado de análisis", true),  // Requiere doble entrada
    LIBERACION_LOTE("Liberación de lote", true),
    CANTIDAD_INGRESO("Cantidad ingreso >1000kg", true),
    FECHA_EXPIRACION("Fecha de expiración", false);  // Solo validación electrónica

    private final String description;
    private final boolean requiresDoubleEntry;
}
```

#### Archivos Referencia
- ✅ **Validación Electrónica:** controller/cu/*.java (@Valid annotations)
- ❌ **Doble Entrada:** (NO implementado, a crear)

---

## Requisito 7: Archivo de Datos

### 7.1 Aseguramiento de Datos y Accesibilidad

**Texto ANMAT:**
> "Los datos deben asegurarse frente a daños tanto por medios físicos como electrónicos. Para el almacenaje de datos debe comprobarse la accesibilidad, la legibilidad y la exactitud."

**Estado: 🟡 CUMPLE PARCIALMENTE (60%)**

#### Evidencia en Código Actual

**LO QUE EXISTE:**

1. **Volúmenes Docker Persistentes:**
```yaml
# docker-compose.yml
volumes:
  db_data:  # Datos persisten fuera del contenedor
    driver: local
```

2. **Scripts de Backup Manual:**
```bash
# data-base/custom_backup.sh
pg_dumpall -U postgres > "/backups/backup_$(date +%Y-%m-%d_%H-%M-%S).sql"
```

3. **Soft Delete (Protección contra eliminación):**
```java
// entity/Lote.java
@SQLDelete(sql = "UPDATE lote SET activo = false WHERE id = ?")
// Datos nunca se eliminan físicamente
```

**LO QUE FALTA:**

1. **Backup NO es automático:**
```bash
# A IMPLEMENTAR: cron job o scheduled task
# 0 2 * * * /opt/securo/data-base/custom_backup.sh
```

2. **No hay verificación de integridad de backups:**
```bash
# A IMPLEMENTAR: backup_verify.sh
#!/bin/bash
BACKUP_FILE=$1
# Restaurar en base de datos temporal
# Comparar checksum de tablas críticas
# Reportar si backup es válido
```

3. **No hay retención de backups documentada:**
```
Política a definir:
- Backups diarios: retener 30 días
- Backups semanales: retener 1 año
- Backups anuales: retener 5+ años (según regulación)
```

### 7.2 Copias de Seguridad y Re-establecimiento

**Texto ANMAT:**
> "Debe realizarse regularmente copias de seguridad de todos los datos relevantes. La integridad y la exactitud de las copias de seguridad de datos y la capacidad de re-establecer los datos debe comprobarse durante la validación y controlarse periódicamente."

**Estado: 🟡 CUMPLE PARCIALMENTE (50%)**

**LO QUE FALTA:**
- ❌ Backup NO es regular (solo manual)
- ❌ NO se prueba restauración de backups periódicamente
- ❌ NO hay protocolo de validación de backup/restore
- ❌ NO hay registro de pruebas de restauración

**Implementación Requerida:**

1. **Automatización de Backup:**
```yaml
# docker-compose.yml - agregar servicio de backup
services:
  backup-service:
    image: alpine:latest
    volumes:
      - db_data:/data:ro
      - ./backups:/backups
    command: >
      sh -c "
      apk add --no-cache postgresql-client &&
      while true; do
        echo 'Running backup...' &&
        pg_dumpall -h postgres-db -U postgres > /backups/auto_backup_$(date +%Y%m%d_%H%M%S).sql &&
        find /backups -name 'auto_backup_*.sql' -mtime +30 -delete &&
        sleep 86400
      done
      "
```

2. **Protocolo de Validación de Backup/Restore:**
```markdown
# OQ-BACKUP-001: Calificación de Sistema de Backup

## Test Case BC-001: Backup Automático
**Objetivo:** Verificar que backup se ejecuta diariamente a las 2AM

**Procedimiento:**
1. Configurar cron job
2. Esperar 24 horas
3. Verificar archivo de backup creado en /backups
4. Verificar timestamp del backup

**Criterios Aceptación:**
- Backup existe
- Tamaño > 0 bytes
- Timestamp corresponde a 2AM ±5 min

## Test Case BC-002: Restauración de Backup
**Objetivo:** Verificar que datos pueden restaurarse

**Procedimiento:**
1. Crear lote de prueba LOT-TEST-001
2. Ejecutar backup manual
3. Eliminar lote de prueba
4. Restaurar desde backup
5. Verificar lote LOT-TEST-001 existe nuevamente

**Criterios Aceptación:**
- Lote restaurado con datos idénticos
- Sin errores durante restore
- Tiempo de restauración < 1 hora
```

#### Archivos Referencia
- ✅ **Backup Script:** data-base/custom_backup.sh
- ✅ **Docker Volumes:** docker-compose.yml
- ❌ **Backup Automation:** (a implementar en docker-compose.yml o cron)
- ❌ **Backup Validation Protocol:** docs/validacion/protocolos/OQ-BACKUP-001.md (a crear)

---

*Continúa en la siguiente sección con Requisitos 8-17...*

[← Resumen Ejecutivo](./ANMAT-01-RESUMEN-EJECUTIVO.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Gap Analysis →](./ANMAT-03-GAP-ANALYSIS.md)
