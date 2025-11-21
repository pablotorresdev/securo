# 03 - Gap Analysis - Brechas Críticas
## Análisis Detallado de Incumplimientos ANMAT

[← Análisis Requisitos](./ANMAT-02-ANALISIS-REQUISITOS.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Plan de Implementación →](./ANMAT-04-PLAN-IMPLEMENTACION.md)

---

## Introducción

Este documento analiza en detalle las **5 brechas críticas** identificadas que impiden el cumplimiento total de ANMAT Anexo 6. Cada gap se analiza con:

1. **Impacto Regulatorio** - ¿Por qué es crítico?
2. **Riesgo Técnico** - ¿Qué puede salir mal?
3. **Solución Propuesta** - ¿Cómo resolverlo?
4. **Esfuerzo de Implementación** - Tiempo y recursos
5. **Dependencias** - ¿Qué se necesita primero?

---

## GAP #1: Firma Electrónica NO Implementada

### 📋 Requisito ANMAT

**Requisito 14 - Firma Electrónica:**
> "Los registros electrónicos pueden firmarse electrónicamente. Respecto de las firmas electrónicas se espera que:
> - a. tengan el mismo impacto que las firmas manuscritas en el ámbito de la compañía,
> - b. estén permanentemente ligadas al respectivo registro,
> - c. incluyan la hora y el día en el que se realizaron."

**Requisito 14.1:**
> "Cada firma electrónica debe ser única para un individuo y no deberá ser rehusada o reasignada a otro usuario. La identidad del individuo debe estar certificada antes de la asignación de la firma electrónica. Para mayor seguridad se debe emplear al menos dos componentes de identificación distintos tales como un código de identificación y una contraseña."

**Requisito 15 - Liberación de Lotes:**
> "Cuando se utiliza un sistema informatizado para registrar la certificación y liberación de lotes, el sistema sólo debe permitir a las Personas Cualificadas certificar la liberación de lotes y debe identificar claramente y registrar la persona que ha liberado o certificado los lotes. Esto debe realizarse usando una firma electrónica."

### ❌ Estado Actual

**Cumplimiento: 0%**

- NO existe ninguna implementación de firma electrónica
- Liberación de lotes NO utiliza firma electrónica
- No hay captura de aprobaciones formales
- No hay binding permanente de firmas a registros

### ⚠️ Impacto Regulatorio

**CRÍTICO - BLOQUEANTE TOTAL**

- **Liberación de lotes SIN firma electrónica NO tiene validez regulatoria**
- Sistema no cumple 21 CFR Part 11 (FDA)
- Sistema no cumple EU GMP Annex 11
- **Auditoría ANMAT rechazaría uso del sistema inmediatamente**

**Escenario de Riesgo:**
```
1. Analista libera lote para venta en sistema
2. Inspector ANMAT pregunta: "¿Dónde está la firma de la Persona Cualificada?"
3. Respuesta: "No tenemos firma electrónica implementada"
4. Inspector: "Este sistema no es válido para liberación de lotes. OBSERVACIÓN CRÍTICA."
5. Consecuencia: Suspensión de liberaciones hasta implementar firma electrónica
```

### 🎯 Solución Propuesta

#### Opción A: Desarrollo Custom

**Ventajas:**
- Control total sobre funcionalidad
- Sin costos de licenciamiento
- Integración nativa con sistema

**Desventajas:**
- Requiere desarrollo especializado (4-6 semanas)
- Requiere validación exhaustiva
- Responsabilidad legal total

**Arquitectura:**

```java
// entity/FirmaElectronica.java
@Entity
@Table(name = "firmas_electronicas")
public class FirmaElectronica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Binding a Registro
    @Column(name = "entity_type", nullable = false)
    private String entityType;  // "Lote", "Analisis", "Movimiento"

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "operation", nullable = false)
    private String operation;  // "LIBERACION_LOTE", "APROBACION_ANALISIS"

    // Usuario que Firma
    @ManyToOne
    @JoinColumn(name = "signed_by", nullable = false)
    private User signedBy;

    // Autenticación Multi-Factor
    @Column(name = "username_at_signing", nullable = false)
    private String usernameAtSigning;  // Capturado en momento de firma

    @Column(name = "password_hash_at_signing", nullable = false)
    private String passwordHashAtSigning;  // Hash BCrypt al momento de firmar

    @Column(name = "second_factor", nullable = false)
    private String secondFactor;  // Código PIN, biométrico, o token

    // Timestamp (Componente c del Req. 14)
    @Column(name = "signed_at", nullable = false)
    private OffsetDateTime signedAt;

    @Column(name = "timezone", nullable = false)
    private String timezone;  // "America/Argentina/Buenos_Aires"

    // Metadata de Contexto
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    // Hash Criptográfico (Componente b: Permanencia)
    @Column(name = "signature_hash", nullable = false, unique = true)
    private String signatureHash;  // SHA-256 de todos los datos

    @Column(name = "data_snapshot", columnDefinition = "TEXT", nullable = false)
    private String dataSnapshot;  // JSON del estado del registro al firmar

    // Significado de la Firma (Componente a)
    @Column(name = "meaning", nullable = false, length = 200)
    private String meaning;  // "Apruebo la liberación de este lote para venta"

    @Column(name = "comments", length = 500)
    private String comments;  // Comentarios opcionales del firmante

    // Validación de Firma
    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = true;

    @Column(name = "invalidated_at")
    private OffsetDateTime invalidatedAt;

    @Column(name = "invalidation_reason")
    private String invalidationReason;

    /**
     * Genera hash SHA-256 único de la firma.
     * Incluye: usuario, timestamp, entity, operation, dataSnapshot
     */
    public String generateSignatureHash() {
        String dataToHash = String.format("%s|%s|%s|%d|%s|%s",
            signedBy.getId(),
            signedAt.toString(),
            entityType,
            entityId,
            operation,
            dataSnapshot
        );
        return DigestUtils.sha256Hex(dataToHash);
    }

    /**
     * Verifica integridad de la firma.
     * Compara hash calculado vs. hash almacenado.
     */
    public boolean verifyIntegrity() {
        String calculatedHash = generateSignatureHash();
        return calculatedHash.equals(this.signatureHash);
    }
}
```

**Servicio de Firma:**

```java
// service/ElectronicSignatureService.java
@Service
public class ElectronicSignatureService {

    @Autowired
    private FirmaElectronicaRepository firmaRepository;

    @Autowired
    private SecurityContextService securityContext;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public FirmaElectronica signRecord(SignatureRequest request) {

        User currentUser = securityContext.getCurrentUser();

        // 1. Verificar que usuario tiene permiso para firmar
        if (!canUserSign(currentUser, request.getOperation())) {
            throw new SecurityException("Usuario no autorizado para realizar esta firma");
        }

        // 2. Verificar segundo factor de autenticación
        if (!verifySecondFactor(currentUser, request.getPassword(), request.getSecondFactor())) {
            throw new AuthenticationException("Autenticación de segundo factor falló");
        }

        // 3. Capturar snapshot del registro
        String dataSnapshot = captureDataSnapshot(request.getEntityType(), request.getEntityId());

        // 4. Crear firma electrónica
        FirmaElectronica firma = new FirmaElectronica();
        firma.setEntityType(request.getEntityType());
        firma.setEntityId(request.getEntityId());
        firma.setOperation(request.getOperation());
        firma.setSignedBy(currentUser);
        firma.setUsernameAtSigning(currentUser.getUsername());
        firma.setPasswordHashAtSigning(passwordEncoder.encode(request.getPassword()));
        firma.setSecondFactor(DigestUtils.sha256Hex(request.getSecondFactor()));
        firma.setSignedAt(OffsetDateTime.now());
        firma.setTimezone(ZoneId.systemDefault().getId());
        firma.setIpAddress(request.getIpAddress());
        firma.setUserAgent(request.getUserAgent());
        firma.setSessionId(request.getSessionId());
        firma.setDataSnapshot(dataSnapshot);
        firma.setMeaning(request.getMeaning());
        firma.setComments(request.getComments());

        // 5. Generar hash criptográfico
        firma.setSignatureHash(firma.generateSignatureHash());

        // 6. Persistir firma
        firma = firmaRepository.save(firma);

        // 7. Actualizar entidad firmada
        updateSignedEntity(request.getEntityType(), request.getEntityId(), firma);

        // 8. Registrar en audit trail
        auditLogger.logSignature(firma);

        return firma;
    }

    private boolean canUserSign(User user, String operation) {
        // Para LIBERACION_LOTE, solo Personas Cualificadas
        if ("LIBERACION_LOTE".equals(operation)) {
            return user.getRole().equals(RoleEnum.DT) ||
                   user.getRole().equals(RoleEnum.GERENTE_GARANTIA_CALIDAD);
        }
        return true;
    }

    private boolean verifySecondFactor(User user, String password, String secondFactor) {
        // Verificar password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return false;
        }

        // Verificar segundo factor (puede ser PIN, OTP, biométrico)
        // Implementación depende de método elegido
        return verifyPIN(user, secondFactor);  // O verifyOTP(user, secondFactor)
    }

    private String captureDataSnapshot(String entityType, Long entityId) {
        // Capturar estado completo del registro en JSON
        Object entity = findEntity(entityType, entityId);
        return objectMapper.writeValueAsString(entity);
    }
}
```

**UI de Firma:**

```html
<!-- templates/firma/modal-firma-electronica.html -->
<div class="modal" id="modalFirmaElectronica">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Firma Electrónica Requerida</h5>
            </div>
            <div class="modal-body">
                <div class="alert alert-info">
                    <strong>Acción a Firmar:</strong>
                    <p th:text="${signatureRequest.meaning}"></p>
                    <p><strong>Registro:</strong> [[${signatureRequest.entityType}]] ID: [[${signatureRequest.entityId}]]</p>
                </div>

                <form id="formFirma" th:action="@{/firma/firmar}" method="post">
                    <input type="hidden" name="entityType" th:value="${signatureRequest.entityType}" />
                    <input type="hidden" name="entityId" th:value="${signatureRequest.entityId}" />
                    <input type="hidden" name="operation" th:value="${signatureRequest.operation}" />
                    <input type="hidden" name="meaning" th:value="${signatureRequest.meaning}" />

                    <!-- Re-autenticación (Primer Factor) -->
                    <div class="form-group">
                        <label for="password">Confirme su Contraseña:</label>
                        <input type="password" id="password" name="password"
                               class="form-control" required
                               placeholder="Ingrese su contraseña actual" />
                        <small class="form-text text-muted">
                            Re-autenticación requerida para firma electrónica
                        </small>
                    </div>

                    <!-- Segundo Factor -->
                    <div class="form-group">
                        <label for="secondFactor">Código PIN (Segundo Factor):</label>
                        <input type="text" id="secondFactor" name="secondFactor"
                               class="form-control" required maxlength="6"
                               placeholder="Ingrese su PIN de 6 dígitos" />
                        <small class="form-text text-muted">
                            PIN personal asignado a su usuario
                        </small>
                    </div>

                    <!-- Comentarios Opcionales -->
                    <div class="form-group">
                        <label for="comments">Comentarios (Opcional):</label>
                        <textarea id="comments" name="comments" class="form-control" rows="2"
                                  placeholder="Ej: Revisados todos los análisis. Lote cumple especificaciones."></textarea>
                    </div>

                    <!-- Declaración -->
                    <div class="form-check">
                        <input type="checkbox" class="form-check-input" id="declaration" required />
                        <label class="form-check-label" for="declaration">
                            <strong>Declaro que:</strong><br/>
                            • He verificado personalmente la información<br/>
                            • Esta firma tiene el mismo efecto que una firma manuscrita<br/>
                            • Comprendo la responsabilidad de esta acción
                        </label>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button type="submit" form="formFirma" class="btn btn-primary">
                    <i class="fa fa-pen"></i> Firmar Electrónicamente
                </button>
            </div>
        </div>
    </div>
</div>
```

**Visualización de Firmas:**

```html
<!-- templates/lote/detalle.html (extracto) -->
<div class="card mt-3">
    <div class="card-header bg-success text-white">
        <h5><i class="fa fa-check-circle"></i> Liberación del Lote</h5>
    </div>
    <div class="card-body">
        <div th:if="${lote.firmaLiberacion != null}">
            <table class="table table-bordered">
                <tr>
                    <th>Liberado por:</th>
                    <td>[[${lote.firmaLiberacion.signedBy.username}]] ([[${lote.firmaLiberacion.signedBy.role.name}]])</td>
                </tr>
                <tr>
                    <th>Fecha y Hora:</th>
                    <td>[[${#temporals.format(lote.firmaLiberacion.signedAt, 'dd/MM/yyyy HH:mm:ss')}]]</td>
                </tr>
                <tr>
                    <th>Significado:</th>
                    <td>[[${lote.firmaLiberacion.meaning}]]</td>
                </tr>
                <tr>
                    <th>Comentarios:</th>
                    <td>[[${lote.firmaLiberacion.comments ?: 'Sin comentarios'}]]</td>
                </tr>
                <tr>
                    <th>Hash de Firma:</th>
                    <td><code>[[${lote.firmaLiberacion.signatureHash}]]</code></td>
                </tr>
                <tr>
                    <th>Verificación de Integridad:</th>
                    <td>
                        <span th:if="${lote.firmaLiberacion.verifyIntegrity()}"
                              class="badge badge-success">
                            <i class="fa fa-check"></i> Firma Válida
                        </span>
                        <span th:unless="${lote.firmaLiberacion.verifyIntegrity()}"
                              class="badge badge-danger">
                            <i class="fa fa-times"></i> Firma Comprometida
                        </span>
                    </td>
                </tr>
            </table>
        </div>
        <div th:if="${lote.firmaLiberacion == null}">
            <div class="alert alert-warning">
                <i class="fa fa-exclamation-triangle"></i>
                Este lote NO ha sido liberado electrónicamente.
            </div>
            <button th:if="${puedeLiberar}" class="btn btn-success"
                    data-toggle="modal" data-target="#modalFirmaElectronica">
                <i class="fa fa-pen"></i> Liberar Lote
            </button>
        </div>
    </div>
</div>
```

#### Opción B: Integración con Proveedor Externo

**Proveedores Posibles:**
- DocuSign (líder mundial)
- Adobe Sign
- OneSpan Sign
- Zoho Sign

**Ventajas:**
- Solución probada y validada
- Cumplimiento legal garantizado
- Soporte técnico incluido
- Auditoría externa del proveedor

**Desventajas:**
- Costo de licencias ($200-1000/mes)
- Dependencia de tercero
- Integración vía API (complejidad)
- Requiere vendor qualification

**Integración DocuSign (Ejemplo):**

```java
// service/DocuSignIntegrationService.java
@Service
public class DocuSignIntegrationService {

    @Value("${docusign.api.key}")
    private String apiKey;

    @Value("${docusign.account.id}")
    private String accountId;

    private final ApiClient apiClient;

    public String requestSignature(Lote lote, User signer) {
        // 1. Crear documento (envelope) en DocuSign
        EnvelopeDefinition envelope = new EnvelopeDefinition();
        envelope.setEmailSubject("Liberación de Lote " + lote.getNumeroLote());

        // 2. Agregar documento (generado dinámicamente)
        Document document = createLoteDocument(lote);
        envelope.setDocuments(Arrays.asList(document));

        // 3. Agregar firmante
        Signer signerObj = new Signer();
        signerObj.setEmail(signer.getEmail());
        signerObj.setName(signer.getUsername());
        signerObj.setRecipientId("1");

        // 4. Tab de firma
        SignHere signHereTab = new SignHere();
        signHereTab.setDocumentId("1");
        signHereTab.setPageNumber("1");
        signHereTab.setXPosition("100");
        signHereTab.setYPosition("200");

        Tabs tabs = new Tabs();
        tabs.setSignHereTabs(Arrays.asList(signHereTab));
        signerObj.setTabs(tabs);

        envelope.setRecipients(new Recipients().signers(Arrays.asList(signerObj)));

        // 5. Enviar para firma
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
        EnvelopeSummary result = envelopesApi.createEnvelope(accountId, envelope);

        return result.getEnvelopeId();
    }

    public boolean verifySignature(String envelopeId) {
        // Verificar estado de firma en DocuSign
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
        Envelope envelope = envelopesApi.getEnvelope(accountId, envelopeId);

        return "completed".equals(envelope.getStatus());
    }
}
```

### 💰 Esfuerzo de Implementación

| Opción | Desarrollo | Testing/Validación | Total | Costo Licencias |
|--------|------------|---------------------|-------|-----------------|
| **Opción A: Custom** | 4-6 semanas | 2-3 semanas | **6-9 semanas** | $0 |
| **Opción B: DocuSign** | 2-3 semanas | 1-2 semanas | **3-5 semanas** | $3,000-12,000/año |

**Recomendación:** **Opción A (Desarrollo Custom)** por las siguientes razones:
1. Control total sobre funcionalidad
2. Sin dependencia de terceros
3. Costo cero de licenciamiento
4. Integración nativa con sistema existente
5. Permite customización para necesidades específicas

### 📦 Entregables

1. **Código:**
   - `entity/FirmaElectronica.java`
   - `service/ElectronicSignatureService.java`
   - `controller/FirmaElectronicaController.java`
   - `repository/FirmaElectronicaRepository.java`
   - Templates HTML para UI de firma

2. **Documentación:**
   - ERU-FIRMA-001: Especificación de Requerimientos de Firma Electrónica
   - EDS-FIRMA-001: Diseño del Sistema de Firma Electrónica
   - SOP-FIRMA-001: Procedimiento de Uso de Firma Electrónica

3. **Validación:**
   - OQ-FIRMA-001: Protocolo de Calificación Operacional
   - PQ-FIRMA-001: Protocolo de Calificación de Performance
   - Reporte de Validación de Firma Electrónica

### 🔗 Dependencias

- **Prerequisito:** Req. 9 (Audit Trail) debe estar implementado
- **Prerequisito:** Req. 12 (Seguridad - segundo factor) debe estar listo
- **Bloqueante para:** Liberación de lotes en producción

---

## GAP #2: Audit Trail Incompleto

### 📋 Requisito ANMAT

**Requisito 9 - Registro de Auditoría:**
> "Debe incorporarse, en base a la gestión de riesgos, en el sistema la creación de un registro de todos los cambios y eliminaciones relevantes relacionados con BPF (un registro de auditoría generado por el sistema). Debe documentarse el motivo del cambio o de la eliminación de datos relevantes relacionados con BxP."

### ❌ Estado Actual

**Cumplimiento: 45%**

**LO QUE EXISTE:**
- ✅ Tabla `auditoria_acceso` registra accesos de usuarios
- ✅ Se captura IP, timestamp, usuario, acción
- ✅ Transacciones se registran con `creadoPor` y `fechaCreacion`

**LO QUE FALTA:**
- ❌ **NO se capturan cambios de valores** (old → new)
- ❌ **NO se registra motivo de cambio**
- ❌ NO se identifica qué datos son "GxP relevantes"
- ❌ NO se auditan lecturas de datos críticos

### ⚠️ Impacto Regulatorio

**CRÍTICO**

- No se puede reconstruir historial de decisiones
- Imposible demostrar integridad de datos (ALCOA+)
- No cumple Data Integrity Guidance (EMA, FDA)
- **Auditor no puede verificar que datos no fueron manipulados**

**Ejemplo de Escenario Problemático:**
```
Inspector ANMAT: "Este lote LOT-2024-001 muestra dictamen APROBADO.
¿Estuvo alguna vez en CUARENTENA o RECHAZADO?"

Respuesta actual: "No tenemos esa información. Solo vemos el estado actual."

Inspector: "¿Cómo puedo confiar en que el dictamen no fue cambiado sin justificación?"

Respuesta: "..."

Inspector: "OBSERVACIÓN CRÍTICA: Sistema no cumple requisitos de Data Integrity."
```

### 🎯 Solución Propuesta

Ver implementación completa en documento 02 (Análisis Requisitos), sección Requisito 9.

**Resumen de Solución:**

1. **Nueva entidad `AuditoriaCambios`** que capture:
   - Entidad y campo modificado
   - Valor anterior y valor nuevo
   - Usuario, timestamp, IP
   - **Motivo del cambio (obligatorio)**
   - Flag "GxP Relevante"

2. **Aspect AOP** que intercepte automáticamente:
   - Todos los métodos `@Transactional` en servicios CU
   - Compare estado antes/después
   - Registre cambios en `auditoria_cambios`

3. **UI modificada** para requerir motivo:
   - Campo "Motivo del Cambio" obligatorio en forms
   - Textarea con mínimo 20 caracteres
   - Ejemplo: "Resultados de análisis micro satisfactorios - Certificado #12345"

4. **Clasificación de Datos GxP:**
```java
public enum GxpDataClassification {
    CRITICAL,  // dictamen, resultado, cantidad, fechaExpiracion
    IMPORTANT, // fechaIngreso, proveedor, loteOrigen
    STANDARD   // nombre, descripción, comentarios
}
```

### 💰 Esfuerzo de Implementación

| Tarea | Esfuerzo | Recursos |
|-------|----------|----------|
| Diseño entidad + repositorio | 3 días | 1 dev |
| Aspect AOP + lógica captura | 5 días | 1 dev senior |
| Modificación UI (forms) | 4 días | 1 dev frontend |
| Testing unitario + integración | 3 días | 1 QA |
| Documentación técnica | 2 días | 1 dev |
| Validación (protocolos) | 5 días | 1 validación specialist |
| **TOTAL** | **22 días (4.4 semanas)** | **2-3 personas** |

### 📦 Entregables

1. **Código:**
   - `entity/AuditoriaCambios.java`
   - `aspect/AuditTrailAspect.java`
   - `service/AuditTrailService.java`
   - Modificación de todos los forms CU para incluir campo `motivoCambio`

2. **Documentación:**
   - ERU-AUDIT-001: Requerimientos de Audit Trail
   - EDS-AUDIT-001: Diseño Técnico de Audit Trail
   - SOP-AUDIT-001: Revisión de Audit Trail

3. **Validación:**
   - OQ-AUDIT-001: Calificación Operacional de Audit Trail
   - PQ-AUDIT-001: Calificación de Performance
   - Test Cases: Verificar captura de cambios en todos los escenarios críticos

### 🔗 Dependencias

- **Prerequisito:** Ninguno (puede implementarse independientemente)
- **Bloqueado por:** Ninguno
- **Bloqueante para:** Firma Electrónica (Gap #1)

---

## GAP #3: Sistema NO Validado Formalmente

*(Ver detalles completos en documento 06 - Documentación Requerida)*

### 📋 Requisito ANMAT

**Requisito 4.1:**
> "La documentación de validación y los informes deben cubrir los pasos relevantes del ciclo de vida del sistema."

### ❌ Estado Actual

**Cumplimiento: 20%**

- Tests unitarios existen, pero NO son tests de validación
- NO hay documentación de validación (PMV, ERU, protocolos)
- NO hay estado de "sistema validado"

### ⚠️ Impacto Regulatorio

**CRÍTICO - BLOQUEANTE TOTAL**

- Sistema NO puede usarse en entorno GxP sin validación formal
- Tests de desarrollo != Tests de validación
- Auditor rechazará sistema inmediatamente

### 🎯 Solución Propuesta

**Fase 1: Documentación (8-12 semanas)**

1. **Plan Maestro de Validación (PMV)** - 2 semanas
2. **Especificaciones de Requerimientos de Usuario (ERU)** - 3 semanas
3. **Especificaciones de Diseño (EDS)** - 2 semanas
4. **Matriz de Trazabilidad de Requisitos (MTR)** - 1 semana
5. **Protocolos IQ/OQ/PQ** - 4 semanas

**Fase 2: Ejecución (4-6 semanas)**

1. **IQ (Installation Qualification)** - 1 semana
2. **OQ (Operational Qualification)** - 2 semanas
3. **PQ (Performance Qualification)** - 2 semanas
4. **Reporte de Validación** - 1 semana

### 💰 Esfuerzo de Implementación

| Fase | Duración | Recursos | Horas |
|------|----------|----------|-------|
| Fase 1: Documentación | 12 semanas | 1 Validation Specialist + 1 Process Owner | 480 hrs |
| Fase 2: Ejecución | 6 semanas | 1 Validation Specialist + 1 QA + 1 System Owner | 360 hrs |
| **TOTAL** | **18 semanas (4.5 meses)** | **3-4 personas** | **840 hrs** |

**Costo Estimado:** $42,000 - $60,000 (asumiendo $50-70/hr para especialistas)

### 📦 Entregables

Ver documento 06 (Documentación Requerida) para plantillas completas.

**Principales:**
1. PMV-001-Plan-Maestro-Validacion.pdf
2. ERU-001-Especificaciones-Requerimientos-Usuario.pdf
3. IQ-001-Protocolo-Instalacion.pdf
4. OQ-001-Protocolo-Operacion.pdf
5. PQ-001-Protocolo-Performance.pdf
6. VR-001-Validation-Summary-Report.pdf

### 🔗 Dependencias

- **Prerequisito:** Gaps #1 y #2 deben estar implementados ANTES de validar
- **Prerequisito:** Sistema debe estar en estado "feature-complete"
- **Bloqueante para:** Uso en producción GxP

---

## GAP #4: Políticas de Contraseña Débiles

*(Ver análisis completo en documento 05 - Especificaciones Técnicas)*

### 📋 Requisito ANMAT

**Requisito 12.1:**
> "Para mayor seguridad se debe emplear al menos dos componentes de identificación distintos tales como un código de identificación y una contraseña."

**Implícito:** Contraseñas deben cumplir estándares modernos de seguridad.

### ❌ Estado Actual

**Cumplimiento: 50%**

- ✅ BCrypt hashing (fuerte)
- ✅ Username + Password
- ❌ **Contraseña mínima: solo 3 caracteres**
- ❌ No requiere complejidad
- ❌ No hay expiración
- ❌ No hay historial (reutilización permitida)
- ❌ No hay lockout tras intentos fallidos
- ❌ No hay MFA

### ⚠️ Impacto Regulatorio

**ALTO**

- Contraseña "abc" es actualmente válida
- Vulnerable a ataques de fuerza bruta
- No cumple NIST SP 800-63B
- No cumple estándares OWASP

### 🎯 Solución Propuesta

**Implementaciones Requeridas:**

1. **Política de Contraseñas Robusta:**
```java
// service/PasswordPolicyService.java
@Service
public class PasswordPolicyService {

    private static final int MIN_LENGTH = 12;
    private static final int MIN_UPPERCASE = 1;
    private static final int MIN_LOWERCASE = 1;
    private static final int MIN_DIGITS = 1;
    private static final int MIN_SPECIAL_CHARS = 1;
    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final int PASSWORD_HISTORY_SIZE = 5;

    public PasswordValidationResult validatePassword(String password, User user) {
        List<String> errors = new ArrayList<>();

        // Longitud
        if (password.length() < MIN_LENGTH) {
            errors.add("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }

        // Complejidad
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Debe contener al menos una mayúscula");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("Debe contener al menos una minúscula");
        }
        if (!password.matches(".*\\d.*")) {
            errors.add("Debe contener al menos un número");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            errors.add("Debe contener al menos un carácter especial");
        }

        // No contener username
        if (password.toLowerCase().contains(user.getUsername().toLowerCase())) {
            errors.add("La contraseña no puede contener su nombre de usuario");
        }

        // Historial (no reutilizar últimas 5 contraseñas)
        if (isPasswordInHistory(password, user)) {
            errors.add("No puede reutilizar las últimas " + PASSWORD_HISTORY_SIZE + " contraseñas");
        }

        return new PasswordValidationResult(errors.isEmpty(), errors);
    }
}
```

2. **Account Lockout:**
```java
// service/AccountLockoutService.java
@Service
public class AccountLockoutService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    public void recordFailedLogin(String username) {
        FailedLoginAttempt attempt = new FailedLoginAttempt();
        attempt.setUsername(username);
        attempt.setAttemptTime(OffsetDateTime.now());
        attemptRepository.save(attempt);

        // Contar intentos en últimos 15 minutos
        long recentAttempts = attemptRepository.countRecentAttempts(
            username,
            OffsetDateTime.now().minusMinutes(15)
        );

        if (recentAttempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(username);
        }
    }

    private void lockAccount(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setAccountLocked(true);
        user.setLockoutUntil(OffsetDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));

        userRepository.save(user);

        // Notificar a admin y usuario
        notificationService.notifyAccountLockout(user);
    }
}
```

3. **Expiración de Contraseña:**
```java
// entity/maestro/User.java (agregar campos)
@Column(name = "password_changed_at")
private LocalDate passwordChangedAt;

@Column(name = "password_expires_at")
private LocalDate passwordExpiresAt;

@Column(name = "account_locked")
private Boolean accountLocked = false;

@Column(name = "lockout_until")
private OffsetDateTime lockoutUntil;

public boolean isPasswordExpired() {
    return passwordExpiresAt != null && LocalDate.now().isAfter(passwordExpiresAt);
}

public boolean isAccountLocked() {
    if (!Boolean.TRUE.equals(accountLocked)) {
        return false;
    }

    // Auto-unlock si pasó el tiempo de lockout
    if (lockoutUntil != null && OffsetDateTime.now().isAfter(lockoutUntil)) {
        accountLocked = false;
        lockoutUntil = null;
        return false;
    }

    return true;
}
```

### 💰 Esfuerzo de Implementación

| Tarea | Esfuerzo |
|-------|----------|
| Password Policy Service | 3 días |
| Account Lockout | 2 días |
| Password Expiry | 2 días |
| Password History | 2 días |
| UI para cambio de contraseña | 2 días |
| Testing | 2 días |
| Validación | 3 días |
| **TOTAL** | **16 días (3.2 semanas)** |

### 🔗 Dependencias

- **Prerequisito:** Ninguno
- **Bloqueante para:** Firma Electrónica (necesita segundo factor robusto)

---

## GAP #5: Gestión de Cambios Manual

*(Ver análisis completo en documento 02, Requisito 10)*

### 📋 Requisito ANMAT

**Requisito 10:**
> "Cualquier cambio a un sistema informatizado incluyendo las configuraciones de sistema sólo debe realizarse de manera controlada de acuerdo con un procedimiento definido."

### ❌ Estado Actual

**Cumplimiento: 40%**

- ✅ Git control de versiones
- ✅ Flyway migraciones BD
- ❌ NO hay workflow formal de aprobación
- ❌ NO se documenta impacto en validación
- ❌ NO hay clasificación de cambios

### ⚠️ Impacto Regulatorio

**ALTO**

- Estado de validación puede invalidarse sin saberlo
- Cambios pueden introducir errores no detectados
- No se puede demostrar control regulatorio

### 🎯 Solución Propuesta

**Sistema de Change Control con:**

1. Change Request Entity (ver documento 02)
2. Workflow de Aprobación (System Owner → QA)
3. Evaluación de Impacto en Validación
4. Link entre CR y Git commits
5. SOP de Gestión de Cambios

### 💰 Esfuerzo de Implementación

| Fase | Duración |
|------|----------|
| Desarrollo entidad + servicio | 2 semanas |
| Workflow de aprobación | 2 semanas |
| UI de change control | 2 semanas |
| SOP y documentación | 1 semana |
| Validación | 1 semana |
| **TOTAL** | **8 semanas** |

### 🔗 Dependencias

- **Prerequisito:** Req. 4 (Validación) debe existir primero
- **Prerequisito:** Req. 11 (Evaluación Periódica) relacionado

---

## Resumen de Priorización

| Gap | Criticidad | Esfuerzo | Prioridad | Orden |
|-----|------------|----------|-----------|-------|
| #1 Firma Electrónica | 🔴 CRÍTICA | 6-9 semanas | **P0** | 2° |
| #2 Audit Trail | 🔴 CRÍTICA | 4 semanas | **P0** | 1° |
| #3 Validación Formal | 🔴 CRÍTICA | 18 semanas | **P1** | 3° |
| #4 Políticas Contraseña | 🟡 ALTA | 3 semanas | **P1** | 4° |
| #5 Gestión Cambios | 🟡 ALTA | 8 semanas | **P1** | 5° |

**Ruta Crítica Recomendada:**
1. **Mes 1-2:** Implementar Audit Trail (Gap #2)
2. **Mes 2-3:** Implementar Firma Electrónica (Gap #1)
3. **Mes 3:** Implementar Políticas Contraseña (Gap #4)
4. **Mes 4-8:** Ejecutar Validación Formal (Gap #3)
5. **Mes 6-8:** Implementar Gestión de Cambios (Gap #5)

---

[← Análisis Requisitos](./ANMAT-02-ANALISIS-REQUISITOS.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Plan de Implementación →](./ANMAT-04-PLAN-IMPLEMENTACION.md)
