# 02 - Análisis Detallado por Requisito (Parte 2)
## Requisitos 8-17

[← Parte 1](./ANMAT-02-ANALISIS-REQUISITOS.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Gap Analysis →](./ANMAT-03-GAP-ANALYSIS.md)

---

## Requisito 8: Impresiones

### 8.1 Copias Impresas Claras

**Texto ANMAT:**
> "Tiene que ser posible obtener copias impresas claras de los datos electrónicos almacenados."

**Estado: 🟢 CUMPLE (85%)**

#### Evidencia en Código Actual

**LO QUE EXISTE:**
```java
// Thymeleaf templates generan HTML que puede imprimirse
// resources/templates/lote/detalle.html
<div class="print-section">
    <h2>Lote: [[${lote.numeroLote}]]</h2>
    <table>
        <tr><td>Producto:</td><td>[[${lote.producto.nombre}]]</td></tr>
        <tr><td>Cantidad:</td><td>[[${lote.cantidadActual}]] [[${lote.unidadMedida}]]</td></tr>
        <tr><td>Dictamen:</td><td>[[${lote.dictamen}]]</td></tr>
    </table>
</div>
```

**CSS para Impresión:**
```css
/* A AGREGAR: static/css/print.css */
@media print {
    .no-print { display: none; }
    .print-section { page-break-inside: avoid; }
    body { font-size: 12pt; color: black; }
}
```

**LO QUE FALTA:**
- ⚠️ No hay botón "Imprimir" explícito en todas las vistas críticas
- ⚠️ No hay estilos CSS específicos para impresión optimizada
- ⚠️ No se genera PDF directamente (aunque se puede imprimir a PDF desde navegador)

**Mejora Sugerida:**
```html
<!-- Agregar en templates críticos -->
<button onclick="window.print()" class="btn btn-primary">
    <i class="fa fa-print"></i> Imprimir Registro
</button>
```

### 8.2 Impresiones con Historial de Cambios

**Texto ANMAT:**
> "Para los registros en los que se basa la liberación de lotes debe ser posible la generación de impresiones que pongan de manifiesto que un dato se ha cambiado respecto de la entrada original."

**Estado: 🔴 NO CUMPLE (30%)**

**LO QUE FALTA:**
- ❌ No se capturan cambios de valores (old/new)
- ❌ Por lo tanto, NO se pueden imprimir historial de cambios
- ❌ Registro de liberación de lote no muestra cambios históricos

**Implementación Requerida:**

Una vez implementado Audit Trail completo (Req. 9), agregar vista de impresión:

```html
<!-- templates/lote/historial-cambios.html -->
<div class="print-section">
    <h2>Historial de Cambios - Lote [[${lote.numeroLote}]]</h2>

    <table class="audit-trail-table">
        <thead>
            <tr>
                <th>Fecha/Hora</th>
                <th>Usuario</th>
                <th>Campo</th>
                <th>Valor Anterior</th>
                <th>Valor Nuevo</th>
                <th>Motivo</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="cambio : ${cambios}">
                <td>[[${cambio.timestamp}]]</td>
                <td>[[${cambio.usuario}]]</td>
                <td>[[${cambio.campo}]]</td>
                <td class="old-value">[[${cambio.valorAnterior}]]</td>
                <td class="new-value">[[${cambio.valorNuevo}]]</td>
                <td>[[${cambio.motivo}]]</td>
            </tr>
        </tbody>
    </table>

    <div class="signatures">
        <p>Liberado por: [[${lote.liberadoPor}]] el [[${lote.fechaLiberacion}]]</p>
        <p>Firma Electrónica: [[${lote.firmaElectronica}]]</p>
    </div>
</div>
```

#### Archivos Referencia
- ✅ **Templates:** src/main/resources/templates/**/*.html
- ❌ **Print CSS:** src/main/resources/static/css/print.css (a crear)
- ❌ **Historial Cambios View:** templates/lote/historial-cambios.html (a crear, depende de Req. 9)

---

## Requisito 9: Registro de Auditoría (Audit Trail)

**Texto ANMAT:**
> "Debe incorporarse, en base a la gestión de riesgos, en el sistema la creación de un registro de todos los cambios y eliminaciones relevantes relacionados con BPF (un registro de auditoría generado por el sistema). Debe documentarse el motivo del cambio o de la eliminación de datos relevantes relacionados con BxP."

**Estado: 🔴 NO CUMPLE (45%)**

### ⚠️ ESTE ES UN GAP CRÍTICO - Ver análisis completo en documento 03 (Gap Analysis)

#### Evidencia en Código Actual

**LO QUE EXISTE:**

1. **AuditoriaAcceso (Solo Accesos):**
```java
// entity/AuditoriaAcceso.java
@Entity
@Table(name = "auditoria_acceso")
public class AuditoriaAcceso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private Long userId;
    private String roleName;
    private String action;  // URL del endpoint
    private String httpMethod;
    private String ipAddress;
    private String userAgent;
    private OffsetDateTime timestamp;

    // ❌ NO captura cambios de valores
    // ❌ NO tiene campo "motivo"
    // ❌ NO identifica entidad/registro afectado
}
```

2. **Tracking de Creador:**
```java
// entity/Movimiento.java
@ManyToOne
@JoinColumn(name = "creado_por")
private User creadoPor;  // ✅ Se registra quién creó

@Column(name = "fecha_y_hora_creacion")
private OffsetDateTime fechaYHoraCreacion;  // ✅ Se registra cuándo

// ❌ Pero NO se registra si se modificó después
// ❌ NO se registra qué cambió
```

**LO QUE FALTA - CRÍTICO:**

1. **Nueva Entidad: AuditoriaCambios**
```java
// entity/AuditoriaCambios.java
@Entity
@Table(name = "auditoria_cambios", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_name,entity_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_user", columnList = "user_id")
})
public class AuditoriaCambios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ¿QUÉ cambió?
    @Column(name = "entity_name", nullable = false)
    private String entityName;  // "Lote", "Movimiento", "Analisis"

    @Column(name = "entity_id", nullable = false)
    private Long entityId;  // ID del registro afectado

    @Column(name = "field_name", nullable = false)
    private String fieldName;  // "dictamen", "cantidadActual", "resultado"

    // ¿CÓMO cambió?
    @Column(name = "old_value", length = 1000)
    private String oldValue;  // Valor anterior

    @Column(name = "new_value", length = 1000)
    private String newValue;  // Valor nuevo

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;  // CREATE, UPDATE, DELETE

    // ¿QUIÉN y CUÁNDO?
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    // ¿POR QUÉ? ← CRÍTICO PARA ANMAT
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;  // Motivo del cambio

    // Contexto adicional
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "is_gxp_relevant", nullable = false)
    private Boolean isGxpRelevant = false;  // ¿Es dato crítico BPF?
}

public enum ChangeType {
    CREATE,   // Alta
    UPDATE,   // Modificación
    DELETE    // Baja/Soft Delete
}
```

2. **Interceptor de Cambios (AOP):**
```java
// aspect/AuditTrailAspect.java
@Aspect
@Component
public class AuditTrailAspect {

    @Autowired
    private AuditoriaCambiosRepository auditRepository;

    @Autowired
    private SecurityContextService securityContext;

    // Interceptar todos los @Transactional de servicios CU
    @Around("execution(* com.mb.conitrack.service.cu..*Service.*(..))")
    public Object auditChanges(ProceedingJoinPoint joinPoint) throws Throwable {

        // Capturar estado ANTES
        Object[] args = joinPoint.getArgs();
        Map<String, Object> oldValues = captureOldValues(args);

        // Ejecutar método (modifica datos)
        Object result = joinPoint.proceed();

        // Capturar estado DESPUÉS
        Map<String, Object> newValues = captureNewValues(result);

        // Comparar y registrar cambios
        recordChanges(oldValues, newValues, joinPoint.getSignature());

        return result;
    }

    void recordChanges(Map<String, Object> oldValues,
                               Map<String, Object> newValues,
                               Signature signature) {

        User currentUser = securityContext.getCurrentUser();

        oldValues.forEach((field, oldValue) -> {
            Object newValue = newValues.get(field);

            if (!Objects.equals(oldValue, newValue)) {
                AuditoriaCambios audit = new AuditoriaCambios();
                audit.setFieldName(field);
                audit.setOldValue(String.valueOf(oldValue));
                audit.setNewValue(String.valueOf(newValue));
                audit.setUser(currentUser);
                audit.setTimestamp(OffsetDateTime.now());
                audit.setChangeType(ChangeType.UPDATE);

                // ¿Es campo GxP relevante?
                audit.setIsGxpRelevant(isGxpRelevantField(field));

                // TODO: Solicitar motivo al usuario
                audit.setReason("Modificación vía " + signature.getName());

                auditRepository.save(audit);
            }
        });
    }

    private boolean isGxpRelevantField(String fieldName) {
        // Campos críticos para BPF
        Set<String> gxpFields = Set.of(
            "dictamen", "resultado", "cantidadActual",
            "fechaExpiracion", "estadoLote", "liberado"
        );
        return gxpFields.contains(fieldName);
    }
}
```

3. **Captura de Motivo en UI:**
```html
<!-- templates/cu/modificacion-form.html -->
<form th:action="@{/cu/modificar-lote}" method="post">

    <!-- Campos normales de modificación -->
    <input type="hidden" name="loteId" th:value="${lote.id}" />

    <select name="nuevoDictamen" required>
        <option value="APROBADO">Aprobado</option>
        <option value="RECHAZADO">Rechazado</option>
        <option value="CUARENTENA">Cuarentena</option>
    </select>

    <!-- ¡CAMPO OBLIGATORIO! -->
    <div class="form-group">
        <label for="motivoCambio">Motivo del Cambio (Requerido):</label>
        <textarea name="motivoCambio" id="motivoCambio"
                  class="form-control" rows="3" required
                  placeholder="Ej: Resultados de análisis microbiológico satisfactorios"></textarea>
        <small class="form-text text-muted">
            Este motivo será registrado en el audit trail y es requerido por BPF.
        </small>
    </div>

    <button type="submit">Guardar Cambio</button>
</form>
```

4. **Controller que captura motivo:**
```java
// controller/cu/ModifResultadoAnalisisController.java
@PostMapping("/guardar")
public String guardar(@Valid ModifResultadoAnalisisDTO dto,
                     @RequestParam String motivoCambio,  // ← NUEVO
                     BindingResult result) {

    if (StringUtils.isBlank(motivoCambio)) {
        result.rejectValue("motivoCambio", "error.motivoCambio",
            "El motivo del cambio es obligatorio para cambios GxP");
        return "cu/resultado-analisis/form";
    }

    // Pasar motivo al servicio
    service.modificarResultadoAnalisis(dto, motivoCambio);

    return "redirect:/cu/resultado-analisis/exito";
}
```

**Clasificación de Datos GxP Relevantes:**

| Entidad | Campo | GxP Relevante | Justificación |
|---------|-------|---------------|---------------|
| Lote | dictamen | ✅ SÍ | Afecta liberación |
| Lote | cantidadActual | ✅ SÍ | Integridad inventario |
| Lote | fechaExpiracion | ✅ SÍ | Seguridad paciente |
| Lote | numeroLote | ⚠️ NO (inmutable) | No debería cambiar |
| Analisis | resultado | ✅ SÍ | Decisión liberación |
| Analisis | fechaAnalisis | ✅ SÍ | Trazabilidad |
| Movimiento | dictamenFinal | ✅ SÍ | Cambio de estado |
| Producto | nombre | ❌ NO | Dato maestro |
| User | password | ❌ NO | Seguridad, no GxP |

#### Archivos Referencia
- ✅ **Audit Actual (limitado):** src/main/java/com/mb/conitrack/entity/AuditoriaAcceso.java
- ❌ **Audit Completo:** src/main/java/com/mb/conitrack/entity/AuditoriaCambios.java (a crear)
- ❌ **Aspect AOP:** src/main/java/com/mb/conitrack/aspect/AuditTrailAspect.java (a crear)
- ❌ **UI Motivo:** templates/cu/**-form.html (modificar para agregar campo motivo)

---

## Requisito 10: Gestión de Cambios y Configuración

**Texto ANMAT:**
> "Cualquier cambio a un sistema informatizado incluyendo las configuraciones de sistema sólo debe realizarse de manera controlada de acuerdo con un procedimiento definido. Antes de la aprobación de un cambio el mismo debe ser adecuadamente evaluado por las áreas impactadas."

**Estado: 🔴 NO CUMPLE (40%)**

#### Evidencia en Código Actual

**LO QUE EXISTE:**

1. **Control de Versiones (Git):**
```bash
# Historial de commits
git log --oneline --graph
# ✅ Trazabilidad de cambios técnicos
# ❌ NO vinculado a sistema formal de gestión de cambios
```

2. **Migraciones de BD (Flyway):**
```sql
-- src/main/resources/db/migration/V2__add_user_hierarchy_and_tracking.sql
ALTER TABLE users ADD COLUMN fecha_expiracion DATE;
-- ✅ Cambios de esquema versionados
-- ❌ NO documenta impacto en validación
```

**LO QUE FALTA - CRÍTICO:**

1. **Sistema de Change Control:**

```java
// entity/ChangeRequest.java
@Entity
@Table(name = "change_requests")
public class ChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "change_number", unique = true, nullable = false)
    private String changeNumber;  // CR-2024-001

    @Column(name = "title", nullable = false)
    private String title;  // "Agregar validación de fecha de expiración"

    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeRequestType type;  // BUGFIX, ENHANCEMENT, CONFIGURATION

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;  // CRITICAL, HIGH, MEDIUM, LOW

    @Enumerated(EnumType.STRING)
    @Column(name = "impact_level", nullable = false)
    private ImpactLevel impactLevel;  // MAJOR, MINOR, COSMETIC

    // Evaluación de Impacto
    @Column(name = "requires_revalidation", nullable = false)
    private Boolean requiresRevalidation = false;

    @Column(name = "gxp_impact_assessment", length = 2000)
    private String gxpImpactAssessment;

    @Column(name = "affected_modules", length = 500)
    private String affectedModules;  // "Liberación Lotes, Análisis"

    // Workflow de Aprobación
    @ManyToOne
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requestor;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @ManyToOne
    @JoinColumn(name = "system_owner_approver_id")
    private User systemOwnerApprover;

    @Column(name = "system_owner_approval_date")
    private LocalDate systemOwnerApprovalDate;

    @ManyToOne
    @JoinColumn(name = "qa_approver_id")
    private User qaApprover;

    @Column(name = "qa_approval_date")
    private LocalDate qaApprovalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChangeRequestStatus status;  // DRAFT, PENDING_APPROVAL, APPROVED, IMPLEMENTED, REJECTED

    // Implementación
    @Column(name = "implementation_date")
    private LocalDate implementationDate;

    @Column(name = "git_commit_hash")
    private String gitCommitHash;  // Link a commit

    @Column(name = "validation_reference")
    private String validationReference;  // Si requiere protocolo de validación

    // Testing
    @Column(name = "test_results", length = 1000)
    private String testResults;

    @Column(name = "rollback_plan", length = 1000)
    private String rollbackPlan;
}

public enum ChangeRequestType {
    BUGFIX,           // Corrección de error
    ENHANCEMENT,      // Mejora funcional
    CONFIGURATION,    // Cambio de configuración
    SECURITY_PATCH,   // Parche de seguridad
    REGULATORY        // Cambio por req. regulatorio
}

public enum ChangeRequestStatus {
    DRAFT,                  // Borrador
    PENDING_APPROVAL,       // Esperando aprobación
    APPROVED,               // Aprobado, listo para implementar
    IN_IMPLEMENTATION,      // En desarrollo
    IMPLEMENTED,            // Implementado en producción
    REJECTED,               // Rechazado
    CANCELLED              // Cancelado
}
```

2. **Workflow de Aprobación:**

```java
// service/ChangeControlService.java
@Service
public class ChangeControlService {

    @Transactional
    public void submitForApproval(ChangeRequest cr) {
        // Validar que tiene impact assessment
        if (cr.getGxpImpactAssessment() == null) {
            throw new ValidationException("Impact assessment es obligatorio");
        }

        cr.setStatus(ChangeRequestStatus.PENDING_APPROVAL);
        cr.setRequestDate(LocalDate.now());

        // Notificar a System Owner y QA
        notificationService.notifyApprovers(cr);

        repository.save(cr);
    }

    @Transactional
    public void approveBySystemOwner(Long crId, User approver) {
        ChangeRequest cr = repository.findById(crId)
            .orElseThrow(() -> new NotFoundException("CR no encontrado"));

        // Validar que approver es System Owner
        if (!approver.getRole().equals(RoleEnum.ADMIN) &&
            !approver.getRole().equals(RoleEnum.DT)) {
            throw new SecurityException("Solo System Owner puede aprobar");
        }

        cr.setSystemOwnerApprover(approver);
        cr.setSystemOwnerApprovalDate(LocalDate.now());

        // Si requiere re-validación, necesita aprobación QA también
        if (cr.getRequiresRevalidation()) {
            notificationService.notifyQA(cr);
        } else {
            cr.setStatus(ChangeRequestStatus.APPROVED);
        }

        repository.save(cr);
    }

    @Transactional
    public void approveByQA(Long crId, User qaApprover, String validationPlan) {
        ChangeRequest cr = repository.findById(crId)
            .orElseThrow(() -> new NotFoundException("CR no encontrado"));

        // Validar que approver es QA
        if (!qaApprover.getRole().equals(RoleEnum.GERENTE_GARANTIA_CALIDAD)) {
            throw new SecurityException("Solo QA puede aprobar validación");
        }

        cr.setQaApprover(qaApprover);
        cr.setQaApprovalDate(LocalDate.now());
        cr.setValidationReference(validationPlan);
        cr.setStatus(ChangeRequestStatus.APPROVED);

        repository.save(cr);
    }
}
```

3. **Procedimiento Documentado:**

```markdown
# SOP-IT-001: Gestión de Cambios en Sistema Conitrack

## 1. Propósito
Definir el procedimiento para gestionar cambios al sistema Conitrack de manera controlada y conforme a BPF.

## 2. Alcance
Aplica a todos los cambios: código, configuración, base de datos, infraestructura.

## 3. Roles y Responsabilidades
- **Solicitante:** Identifica necesidad de cambio, crea Change Request
- **System Owner:** Evalúa impacto técnico, aprueba implementación
- **QA:** Evalúa impacto en validación, aprueba cambios GxP
- **Desarrollador:** Implementa cambio según especificación aprobada

## 4. Procedimiento

### 4.1 Creación de Change Request
1. Solicitante accede a módulo Change Control
2. Completa formulario:
   - Título y descripción del cambio
   - Tipo de cambio (Bugfix, Enhancement, etc.)
   - Prioridad (Critical, High, Medium, Low)
   - Impacto (Major, Minor, Cosmetic)
3. Realiza evaluación de impacto GxP:
   - ¿Afecta datos críticos?
   - ¿Afecta liberación de lotes?
   - ¿Requiere re-validación?
4. Envía CR para aprobación

### 4.2 Evaluación y Aprobación
1. System Owner recibe notificación
2. Revisa impacto técnico
3. Si aprueba:
   - Marca aprobación en sistema
   - Si requiere re-validación → notifica QA
   - Si no requiere re-validación → CR pasa a APPROVED
4. QA (si aplica):
   - Evalúa impacto en estado de validación
   - Define si requiere protocolo de validación
   - Aprueba o rechaza

### 4.3 Implementación
1. Desarrollador asignado recibe CR aprobado
2. Implementa cambio en ambiente de desarrollo
3. Ejecuta testing (unit tests + integration tests)
4. Crea Pull Request en Git, referenciando CR-NUMBER
5. Realiza deployment a ambiente de pruebas
6. Ejecuta testing de validación (si aplica)
7. System Owner aprueba deployment a producción
8. Desarrollador registra Git commit hash en CR
9. Marca CR como IMPLEMENTED

### 4.4 Post-Implementación
1. Si requiere re-validación:
   - QA ejecuta protocolo de validación
   - Genera reporte de validación
   - Vincula reporte a CR
2. System Owner verifica sistema en producción
3. CR se cierra

## 5. Criterios de Re-validación

| Tipo de Cambio | Requiere Re-validación |
|----------------|------------------------|
| Nueva funcionalidad GxP | SÍ - Validación completa |
| Modificación funcionalidad GxP existente | SÍ - Validación parcial (PQ) |
| Bugfix en funcionalidad GxP | SÍ - Testing de regresión documentado |
| Bugfix en funcionalidad no-GxP | NO - Testing estándar |
| Cambio de configuración (parámetros) | SÍ - Verificación documentada |
| Upgrade de infraestructura (BD, servidor) | SÍ - IQ/OQ |
| Parche de seguridad | DEPENDE - Evaluar impacto |

## 6. Registros
- Change Requests (en sistema)
- Aprobaciones (firmas electrónicas en sistema)
- Reportes de validación (si aplica)
- Git commits (referenciando CR)
```

#### Archivos Referencia
- ✅ **Git:** Control de versiones existente
- ✅ **Flyway:** src/main/resources/db/migration/*.sql
- ❌ **Change Request Entity:** src/main/java/com/mb/conitrack/entity/ChangeRequest.java (a crear)
- ❌ **Change Control Service:** src/main/java/com/mb/conitrack/service/ChangeControlService.java (a crear)
- ❌ **SOP Change Control:** docs/procedimientos/SOP-IT-001-Gestion-Cambios.md (a crear)

---

## Requisito 11: Evaluación Periódica

**Texto ANMAT:**
> "Los sistemas informatizados deben evaluarse periódicamente para confirmar que se mantienen en un estado válido y que cumplen con las BPF."

**Estado: 🔴 NO CUMPLE (20%)**

#### Lo que Falta - CRÍTICO

**No existe ningún proceso formal de evaluación periódica.**

**Implementación Requerida:**

1. **Entidad para Evaluaciones:**
```java
// entity/SystemPeriodicReview.java
@Entity
@Table(name = "system_periodic_reviews")
public class SystemPeriodicReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_number", unique = true)
    private String reviewNumber;  // PR-2024-Q1

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type")
    private ReviewType reviewType;  // QUARTERLY, ANNUAL, AD_HOC

    // Estado del Sistema
    @Column(name = "system_version")
    private String systemVersion;

    @Column(name = "validation_status")
    private String validationStatus;  // "Validado", "Re-validación pendiente"

    @Column(name = "last_change_date")
    private LocalDate lastChangeDate;

    // Métricas del Período
    @Column(name = "total_transactions")
    private Long totalTransactions;

    @Column(name = "total_deviations")
    private Integer totalDeviations;

    @Column(name = "total_incidents")
    private Integer totalIncidents;

    @Column(name = "total_changes")
    private Integer totalChanges;

    @Column(name = "system_uptime_percentage")
    private BigDecimal systemUptimePercentage;

    // Auditoría
    @Column(name = "audit_trail_reviewed")
    private Boolean auditTrailReviewed;

    @Column(name = "backup_restore_tested")
    private Boolean backupRestoreTested;

    @Column(name = "security_patches_up_to_date")
    private Boolean securityPatchesUpToDate;

    // Hallazgos
    @Column(name = "findings", length = 2000)
    private String findings;

    @Column(name = "corrective_actions", length = 2000)
    private String correctiveActions;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    // Aprobación
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;
}
```

2. **Servicio de Generación de Métricas:**
```java
// service/SystemMetricsService.java
@Service
public class SystemMetricsService {

    public SystemMetrics generateMetrics(LocalDate startDate, LocalDate endDate) {
        SystemMetrics metrics = new SystemMetrics();

        // Total de transacciones
        metrics.setTotalTransactions(movimientoRepository.countBetweenDates(startDate, endDate));

        // Incidentes/Errores
        metrics.setTotalIncidents(incidentRepository.countByDateRange(startDate, endDate));

        // Cambios realizados
        metrics.setTotalChanges(changeRequestRepository.countImplementedBetween(startDate, endDate));

        // Uptime (si hay monitoreo)
        metrics.setSystemUptime(calculateUptime(startDate, endDate));

        // Análisis de Audit Trail
        metrics.setAuditTrailRecords(auditRepository.countBetweenDates(startDate, endDate));

        // Backups exitosos
        metrics.setSuccessfulBackups(backupLogRepository.countSuccessful(startDate, endDate));

        return metrics;
    }
}
```

3. **Procedimiento de Evaluación Periódica:**
```markdown
# SOP-QA-002: Evaluación Periódica del Sistema Conitrack

## 1. Frecuencia
- **Revisión Trimestral:** Cada 3 meses (Enero, Abril, Julio, Octubre)
- **Revisión Anual:** Cada diciembre (más exhaustiva)
- **Ad-hoc:** Después de cambios mayores o incidentes críticos

## 2. Checklist de Revisión

### 2.1 Estado de Validación
- [ ] Sistema sigue en estado validado
- [ ] No hay cambios que requieran re-validación pendiente
- [ ] Documentación de validación actualizada

### 2.2 Desvios e Incidentes
- [ ] Revisar todos los incidentes del período
- [ ] Verificar que CAPAs están implementadas
- [ ] Identificar tendencias o problemas recurrentes

### 2.3 Cambios Implementados
- [ ] Todos los cambios tienen CR aprobado
- [ ] Cambios GxP tienen validación documentada
- [ ] No hay cambios sin aprobar en producción

### 2.4 Audit Trail
- [ ] Audit trail está funcionando correctamente
- [ ] No hay gaps en registros de auditoría
- [ ] Revisión de muestra de registros críticos

### 2.5 Seguridad
- [ ] Patches de seguridad aplicados y actualizados
- [ ] No hay vulnerabilidades críticas pendientes
- [ ] Revisión de accesos de usuarios
- [ ] Contraseñas expiradas/usuarios inactivos removidos

### 2.6 Backup y Recuperación
- [ ] Backups automáticos funcionando
- [ ] Test de restauración ejecutado exitosamente
- [ ] Tiempo de recuperación dentro de SLA

### 2.7 Performance
- [ ] Tiempos de respuesta aceptables
- [ ] No hay degradación de performance
- [ ] Capacidad de almacenamiento adecuada

## 3. Reporte de Evaluación
- Completar formulario de evaluación en sistema
- Documentar hallazgos y acciones correctivas
- Aprobar por QA y System Owner
- Programar próxima revisión
```

#### Archivos Referencia
- ❌ **Periodic Review Entity:** src/main/java/com/mb/conitrack/entity/SystemPeriodicReview.java (a crear)
- ❌ **Metrics Service:** src/main/java/com/mb/conitrack/service/SystemMetricsService.java (a crear)
- ❌ **SOP Evaluación:** docs/procedimientos/SOP-QA-002-Evaluacion-Periodica.md (a crear)

---

*Continúa con Requisitos 12-17...*

[← Parte 1](./ANMAT-02-ANALISIS-REQUISITOS.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Gap Analysis →](./ANMAT-03-GAP-ANALYSIS.md)
