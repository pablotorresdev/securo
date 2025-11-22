# 05 - Especificaciones Técnicas
## Diseños Detallados de Implementación

[← Plan de Implementación](./ANMAT-04-PLAN-IMPLEMENTACION.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Documentación Requerida →](./ANMAT-06-DOCUMENTACION-REQUERIDA.md)

---

## Introducción

Este documento contiene las especificaciones técnicas detalladas para implementar las funcionalidades requeridas para cumplimiento ANMAT. Incluye:

- Diseño de base de datos (DDL)
- Modelo de entidades JPA
- Servicios y lógica de negocio
- Controllers y APIs
- Templates HTML
- Configuraciones

Todos los diseños están listos para implementación directa.

---

## 1. Sistema de Audit Trail Completo

### 1.1 Modelo de Base de Datos

```sql
-- V3__create_auditoria_cambios.sql

-- Tabla principal de auditoría de cambios
CREATE TABLE auditoria_cambios (
    id BIGSERIAL PRIMARY KEY,

    -- ¿QUÉ cambió?
    entity_name VARCHAR(100) NOT NULL,  -- 'Lote', 'Movimiento', 'Analisis'
    entity_id BIGINT NOT NULL,          -- ID del registro afectado
    field_name VARCHAR(100) NOT NULL,   -- Campo modificado

    -- ¿CÓMO cambió?
    old_value TEXT,                     -- Valor anterior (NULL si CREATE)
    new_value TEXT,                     -- Valor nuevo (NULL si DELETE)
    change_type VARCHAR(20) NOT NULL,   -- 'CREATE', 'UPDATE', 'DELETE'

    -- ¿QUIÉN y CUÁNDO?
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,      -- Snapshot del username al momento del cambio
    role_name VARCHAR(50) NOT NULL,     -- Snapshot del rol al momento del cambio
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- ¿POR QUÉ? (CRÍTICO para ANMAT)
    reason TEXT NOT NULL,               -- Motivo del cambio (obligatorio)

    -- Contexto adicional
    ip_address VARCHAR(45),             -- IPv4 o IPv6
    user_agent TEXT,                    -- Browser/client info
    session_id VARCHAR(255),

    -- Clasificación GxP
    is_gxp_relevant BOOLEAN NOT NULL DEFAULT FALSE,  -- ¿Es dato crítico BPF?
    gxp_classification VARCHAR(20),     -- 'CRITICAL', 'IMPORTANT', 'STANDARD'

    -- Metadata
    application_version VARCHAR(20),    -- Versión del sistema al momento del cambio

    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Índices para performance
CREATE INDEX idx_audit_entity ON auditoria_cambios(entity_name, entity_id);
CREATE INDEX idx_audit_timestamp ON auditoria_cambios(timestamp DESC);
CREATE INDEX idx_audit_user ON auditoria_cambios(user_id);
CREATE INDEX idx_audit_gxp ON auditoria_cambios(is_gxp_relevant, timestamp DESC) WHERE is_gxp_relevant = TRUE;
CREATE INDEX idx_audit_change_type ON auditoria_cambios(change_type);

-- Índice compuesto para búsquedas comunes
CREATE INDEX idx_audit_entity_field_time ON auditoria_cambios(entity_name, entity_id, field_name, timestamp DESC);

-- Tabla de clasificación de datos GxP
CREATE TABLE gxp_data_classification (
    id SERIAL PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    classification VARCHAR(20) NOT NULL,  -- 'CRITICAL', 'IMPORTANT', 'STANDARD'
    justification TEXT,                   -- Por qué es crítico
    requires_reason BOOLEAN NOT NULL DEFAULT TRUE,  -- ¿Requiere motivo obligatorio?

    UNIQUE(entity_name, field_name)
);

-- Datos iniciales: campos GxP críticos
INSERT INTO gxp_data_classification (entity_name, field_name, classification, justification, requires_reason) VALUES
-- Lote
('Lote', 'dictamen', 'CRITICAL', 'Afecta directamente decisión de liberación de lote', TRUE),
('Lote', 'cantidadActual', 'CRITICAL', 'Integridad de inventario, impacta trazabilidad', TRUE),
('Lote', 'fechaExpiracion', 'CRITICAL', 'Seguridad del paciente', TRUE),
('Lote', 'fechaReanalisis', 'IMPORTANT', 'Control de calidad', TRUE),
('Lote', 'estadoLote', 'CRITICAL', 'Control de disponibilidad', TRUE),
('Lote', 'loteOrigen', 'IMPORTANT', 'Trazabilidad de genealogía', TRUE),

-- Analisis
('Analisis', 'resultado', 'CRITICAL', 'Base de decisión de liberación', TRUE),
('Analisis', 'numeroAnalisis', 'IMPORTANT', 'Trazabilidad de QC', TRUE),
('Analisis', 'fechaAnalisis', 'IMPORTANT', 'Cronología de análisis', TRUE),
('Analisis', 'dictamen', 'CRITICAL', 'Aprobación/rechazo de lote', TRUE),

-- Movimiento
('Movimiento', 'dictamenFinal', 'CRITICAL', 'Cambio de estado de lote', TRUE),
('Movimiento', 'cantidad', 'CRITICAL', 'Trazabilidad de movimientos', TRUE),
('Movimiento', 'motivoMovimiento', 'IMPORTANT', 'Justificación de transacción', TRUE),

-- Campos NO críticos (ejemplos)
('Producto', 'nombre', 'STANDARD', 'Dato maestro, bajo impacto GxP', FALSE),
('Producto', 'descripcion', 'STANDARD', 'Informativo', FALSE),
('Proveedor', 'nombre', 'STANDARD', 'Dato maestro', FALSE),
('User', 'username', 'IMPORTANT', 'Seguridad, no directo GxP', FALSE);

COMMENT ON TABLE auditoria_cambios IS 'Registro de auditoría completo de todos los cambios a datos GxP. Cumple ANMAT Anexo 6 Req. 9.';
COMMENT ON COLUMN auditoria_cambios.reason IS 'Motivo del cambio (obligatorio para cambios GxP). Ej: "Resultados de análisis microbiológico satisfactorios - Certificado #12345"';
COMMENT ON COLUMN auditoria_cambios.is_gxp_relevant IS 'TRUE si el campo afectado es crítico para BPF (Good Manufacturing Practices)';
```

### 1.2 Entidad JPA

```java
// src/main/java/com/mb/conitrack/entity/AuditoriaCambios.java
package com.mb.conitrack.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.ChangeType;
import com.mb.conitrack.enums.GxpDataClassification;

@Entity
@Table(name = "auditoria_cambios", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_name,entity_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_gxp", columnList = "is_gxp_relevant,timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaCambios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ¿QUÉ cambió?
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    // ¿CÓMO cambió?
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    // ¿QUIÉN y CUÁNDO?
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "username", nullable = false, length = 50)
    private String username;  // Snapshot

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;  // Snapshot

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    // ¿POR QUÉ?
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    // Contexto
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    // Clasificación GxP
    @Column(name = "is_gxp_relevant", nullable = false)
    private Boolean isGxpRelevant = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "gxp_classification", length = 20)
    private GxpDataClassification gxpClassification;

    // Metadata
    @Column(name = "application_version", length = 20)
    private String applicationVersion;

    /**
     * Genera descripción legible del cambio para reportes.
     */
    public String getChangeDescription() {
        return String.format("%s %s.%s de '%s' a '%s'",
            username,
            changeType == ChangeType.CREATE ? "creó" :
            changeType == ChangeType.UPDATE ? "modificó" : "eliminó",
            fieldName,
            oldValue != null ? oldValue : "N/A",
            newValue != null ? newValue : "N/A"
        );
    }

    /**
     * Verifica si el cambio fue significativo (valores realmente diferentes).
     */
    public boolean isSignificantChange() {
        if (changeType == ChangeType.CREATE || changeType == ChangeType.DELETE) {
            return true;
        }
        return oldValue != null && !oldValue.equals(newValue);
    }
}
```

### 1.3 Enums

```java
// src/main/java/com/mb/conitrack/enums/ChangeType.java
package com.mb.conitrack.enums;

public enum ChangeType {
    CREATE("Creación"),
    UPDATE("Modificación"),
    DELETE("Eliminación");

    private final String displayName;

    ChangeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

```java
// src/main/java/com/mb/conitrack/enums/GxpDataClassification.java
package com.mb.conitrack.enums;

public enum GxpDataClassification {
    CRITICAL("Crítico", "Impacto directo en seguridad del paciente o calidad del producto"),
    IMPORTANT("Importante", "Relevante para trazabilidad y control de calidad"),
    STANDARD("Estándar", "Bajo impacto GxP");

    private final String displayName;
    private final String description;

    GxpDataClassification(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
```

### 1.4 Repositorio

```java
// src/main/java/com/mb/conitrack/repository/AuditoriaCambiosRepository.java
package com.mb.conitrack.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mb.conitrack.entity.AuditoriaCambios;
import com.mb.conitrack.enums.ChangeType;

@Repository
public interface AuditoriaCambiosRepository extends JpaRepository<AuditoriaCambios, Long> {

    /**
     * Encuentra historial completo de cambios para una entidad específica.
     */
    List<AuditoriaCambios> findByEntityNameAndEntityIdOrderByTimestampDesc(
        String entityName, Long entityId);

    /**
     * Encuentra solo cambios GxP relevantes para una entidad.
     */
    List<AuditoriaCambios> findByEntityNameAndEntityIdAndIsGxpRelevantTrueOrderByTimestampDesc(
        String entityName, Long entityId);

    /**
     * Busca cambios por usuario en un rango de fechas.
     */
    @Query("SELECT a FROM AuditoriaCambios a WHERE a.user.id = :userId " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditoriaCambios> findByUserAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") OffsetDateTime startDate,
        @Param("endDate") OffsetDateTime endDate,
        Pageable pageable);

    /**
     * Busca cambios por tipo y entidad.
     */
    List<AuditoriaCambios> findByEntityNameAndChangeTypeOrderByTimestampDesc(
        String entityName, ChangeType changeType);

    /**
     * Cuenta cambios GxP en un período (para métricas).
     */
    @Query("SELECT COUNT(a) FROM AuditoriaCambios a WHERE a.isGxpRelevant = TRUE " +
           "AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countGxpChangesBetweenDates(
        @Param("startDate") OffsetDateTime startDate,
        @Param("endDate") OffsetDateTime endDate);

    /**
     * Busca todos los cambios a un campo específico.
     */
    List<AuditoriaCambios> findByEntityNameAndFieldNameOrderByTimestampDesc(
        String entityName, String fieldName);

    /**
     * Búsqueda avanzada con filtros múltiples.
     */
    @Query("SELECT a FROM AuditoriaCambios a WHERE " +
           "(:entityName IS NULL OR a.entityName = :entityName) AND " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:changeType IS NULL OR a.changeType = :changeType) AND " +
           "(:gxpOnly IS FALSE OR a.isGxpRelevant = TRUE) AND " +
           "a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditoriaCambios> searchAuditTrail(
        @Param("entityName") String entityName,
        @Param("userId") Long userId,
        @Param("changeType") ChangeType changeType,
        @Param("gxpOnly") Boolean gxpOnly,
        @Param("startDate") OffsetDateTime startDate,
        @Param("endDate") OffsetDateTime endDate,
        Pageable pageable);
}
```

### 1.5 Servicio de Audit Trail

```java
// src/main/java/com/mb/conitrack/service/AuditTrailService.java
package com.mb.conitrack.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.conitrack.entity.AuditoriaCambios;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.ChangeType;
import com.mb.conitrack.enums.GxpDataClassification;
import com.mb.conitrack.repository.AuditoriaCambiosRepository;
import com.mb.conitrack.repository.GxpDataClassificationRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuditTrailService {

    @Autowired
    private AuditoriaCambiosRepository auditRepository;

    @Autowired
    private GxpDataClassificationRepository gxpClassificationRepository;

    @Autowired
    private SecurityContextService securityContext;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.application.version:1.0}")
    private String applicationVersion;

    /**
     * Registra un cambio en el audit trail.
     * Usa REQUIRES_NEW para garantizar persistencia incluso si transacción padre falla.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditoriaCambios recordChange(
            String entityName,
            Long entityId,
            String fieldName,
            Object oldValue,
            Object newValue,
            ChangeType changeType,
            String reason) {

        User currentUser = securityContext.getCurrentUser();

        // Verificar si campo es GxP relevante
        GxpDataClassification classification = gxpClassificationRepository
            .findByEntityNameAndFieldName(entityName, fieldName)
            .map(c -> c.getClassification())
            .orElse(null);

        boolean isGxpRelevant = classification != null &&
                                (classification == GxpDataClassification.CRITICAL ||
                                 classification == GxpDataClassification.IMPORTANT);

        // Construir registro de auditoría
        AuditoriaCambios audit = AuditoriaCambios.builder()
            .entityName(entityName)
            .entityId(entityId)
            .fieldName(fieldName)
            .oldValue(serializeValue(oldValue))
            .newValue(serializeValue(newValue))
            .changeType(changeType)
            .user(currentUser)
            .username(currentUser.getUsername())
            .roleName(currentUser.getRole().getName())
            .timestamp(OffsetDateTime.now())
            .reason(reason)
            .ipAddress(getClientIpAddress())
            .userAgent(request.getHeader("User-Agent"))
            .sessionId(request.getSession().getId())
            .isGxpRelevant(isGxpRelevant)
            .gxpClassification(classification)
            .applicationVersion(applicationVersion)
            .build();

        audit = auditRepository.save(audit);

        log.info("Audit trail recorded: {} {} {}.{} by {}",
            changeType, entityName, entityId, fieldName, currentUser.getUsername());

        return audit;
    }

    /**
     * Obtiene historial completo de cambios para una entidad.
     */
    @Transactional(readOnly = true)
    public List<AuditoriaCambios> getAuditHistory(String entityName, Long entityId) {
        return auditRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(
            entityName, entityId);
    }

    /**
     * Obtiene solo cambios GxP relevantes para una entidad.
     */
    @Transactional(readOnly = true)
    public List<AuditoriaCambios> getGxpAuditHistory(String entityName, Long entityId) {
        return auditRepository.findByEntityNameAndEntityIdAndIsGxpRelevantTrueOrderByTimestampDesc(
            entityName, entityId);
    }

    /**
     * Serializa valor a String para almacenamiento.
     * Maneja tipos complejos convirtiéndolos a JSON.
     */
    private String serializeValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (value.getClass().isPrimitive() ||
            value instanceof Number ||
            value instanceof Boolean) {
            return String.valueOf(value);
        }

        if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        }

        // Tipos complejos: serializar a JSON
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize value of type {}: {}",
                value.getClass().getName(), e.getMessage());
            return value.toString();
        }
    }

    /**
     * Obtiene IP del cliente, considerando proxies/balanceadores.
     */
    private String getClientIpAddress() {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For puede contener múltiples IPs (tomar primera)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
```

### 1.6 Aspect AOP para Captura Automática

```java
// src/main/java/com/mb/conitrack/aspect/AuditTrailAspect.java
package com.mb.conitrack.aspect;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mb.conitrack.annotation.Auditable;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.enums.ChangeType;
import com.mb.conitrack.service.AuditTrailService;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditTrailAspect {

    @Autowired
    private AuditTrailService auditTrailService;

    /**
     * Intercepta todos los métodos de servicios CU anotados con @Auditable.
     * Captura estado antes/después y registra cambios.
     */
    @Around("@annotation(auditable)")
    public Object auditChanges(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

        String reason = extractReason(joinPoint);
        Map<String, Object> beforeState = captureState(joinPoint);

        // Ejecutar método (puede modificar entidades)
        Object result = joinPoint.proceed();

        Map<String, Object> afterState = captureState(result);

        // Comparar y registrar cambios
        recordChanges(beforeState, afterState, auditable.changeType(), reason);

        return result;
    }

    /**
     * Extrae el motivo del cambio de los argumentos del método.
     * Busca parámetro llamado "motivoCambio" o similar.
     */
    private String extractReason(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature())
            .getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals("motivoCambio") || paramNames[i].equals("reason")) {
                return (String) args[i];
            }
        }

        return "Cambio realizado vía " + joinPoint.getSignature().getName();
    }

    /**
     * Captura estado de entidades antes/después del cambio.
     */
    private Map<String, Object> captureState(Object obj) {
        Map<String, Object> state = new HashMap<>();

        if (obj == null) {
            return state;
        }

        Class<?> clazz = obj.getClass();

        // Solo capturar si es entidad JPA
        if (!clazz.isAnnotationPresent(Entity.class)) {
            return state;
        }

        // Obtener ID de la entidad
        Long entityId = getEntityId(obj);
        state.put("_entityId", entityId);
        state.put("_entityName", clazz.getSimpleName());

        // Capturar valores de todos los campos
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                state.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                log.warn("Could not access field {}: {}", field.getName(), e.getMessage());
            }
        }

        return state;
    }

    /**
     * Compara estados y registra cambios.
     */
    void recordChanges(
            Map<String, Object> beforeState,
            Map<String, Object> afterState,
            ChangeType changeType,
            String reason) {

        if (afterState.isEmpty()) {
            return;
        }

        String entityName = (String) afterState.get("_entityName");
        Long entityId = (Long) afterState.get("_entityId");

        // Comparar cada campo
        beforeState.forEach((fieldName, oldValue) -> {
            if (fieldName.startsWith("_")) {
                return; // Skip metadata
            }

            Object newValue = afterState.get(fieldName);

            // Detectar cambio
            boolean changed = (oldValue == null && newValue != null) ||
                             (oldValue != null && !oldValue.equals(newValue));

            if (changed) {
                auditTrailService.recordChange(
                    entityName,
                    entityId,
                    fieldName,
                    oldValue,
                    newValue,
                    changeType,
                    reason
                );
            }
        });
    }

    /**
     * Obtiene ID de una entidad JPA.
     */
    private Long getEntityId(Object entity) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                try {
                    Object id = field.get(entity);
                    return id instanceof Long ? (Long) id : null;
                } catch (IllegalAccessException e) {
                    log.error("Could not access ID field: {}", e.getMessage());
                }
            }
        }
        return null;
    }
}
```

### 1.7 Anotación @Auditable

```java
// src/main/java/com/mb/conitrack/annotation/Auditable.java
package com.mb.conitrack.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mb.conitrack.enums.ChangeType;

/**
 * Marca métodos que deben ser auditados automáticamente.
 * El Aspect capturará cambios antes/después de la ejecución.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Tipo de cambio que realiza este método.
     */
    ChangeType changeType() default ChangeType.UPDATE;

    /**
     * Descripción opcional del cambio.
     */
    String description() default "";
}
```

### 1.8 Uso en Servicio CU (Ejemplo)

```java
// src/main/java/com/mb/conitrack/service/cu/ModifResultadoAnalisisService.java
package com.mb.conitrack.service.cu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.annotation.Auditable;
import com.mb.conitrack.dto.ModifResultadoAnalisisDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.ChangeType;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;

@Service
public class ModifResultadoAnalisisService extends AbstractCuService {

    @Autowired
    private AnalisisRepository analisisRepository;

    @Autowired
    private LoteRepository loteRepository;

    /**
     * Modifica resultado de análisis.
     * ¡AUDITADO AUTOMÁTICAMENTE!
     */
    @Transactional
    @Auditable(changeType = ChangeType.UPDATE, description = "Modificación de resultado de análisis")
    public Analisis modificarResultadoAnalisis(
            ModifResultadoAnalisisDTO dto,
            String motivoCambio) {  // ← Motivo es obligatorio

        // Validar motivo no vacío
        if (motivoCambio == null || motivoCambio.trim().length() < 20) {
            throw new ValidationException(
                "El motivo del cambio debe tener al menos 20 caracteres");
        }

        // Obtener análisis
        Analisis analisis = analisisRepository.findById(dto.getAnalisisId())
            .orElseThrow(() -> new NotFoundException("Análisis no encontrado"));

        // Obtener lote asociado
        Lote lote = analisis.getLote();

        // MODIFICAR (Aspect capturará old/new values automáticamente)
        analisis.setResultado(dto.getResultado());
        analisis.setDictamen(dto.getDictamen());
        analisis.setObservaciones(dto.getObservaciones());

        // Actualizar dictamen de lote si corresponde
        if (dto.getDictamen() == DictamenEnum.APROBADO) {
            lote.setDictamen(DictamenEnum.APROBADO);
        } else if (dto.getDictamen() == DictamenEnum.RECHAZADO) {
            lote.setDictamen(DictamenEnum.RECHAZADO);
        }

        analisisRepository.save(analisis);
        loteRepository.save(lote);

        // Aspect registrará cambios en audit trail con el motivo proporcionado
        return analisis;
    }
}
```

### 1.9 Modificación de Controller

```java
// src/main/java/com/mb/conitrack/controller/cu/ModifResultadoAnalisisController.java

@PostMapping("/guardar")
public String guardar(
        @Valid @ModelAttribute ModifResultadoAnalisisDTO dto,
        BindingResult result,
        @RequestParam String motivoCambio,  // ← NUEVO: campo obligatorio
        RedirectAttributes redirectAttributes) {

    // Validar motivo
    if (StringUtils.isBlank(motivoCambio) || motivoCambio.trim().length() < 20) {
        result.rejectValue("motivoCambio", "error.motivoCambio",
            "El motivo del cambio es obligatorio y debe tener al menos 20 caracteres");
        return "cu/resultado-analisis/form";
    }

    if (result.hasErrors()) {
        return "cu/resultado-analisis/form";
    }

    try {
        // Pasar motivo al servicio
        service.modificarResultadoAnalisis(dto, motivoCambio);

        redirectAttributes.addFlashAttribute("mensaje",
            "Resultado de análisis modificado exitosamente");
        return "redirect:/cu/resultado-analisis/exito";

    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/cu/resultado-analisis/form";
    }
}
```

### 1.10 Template HTML con Campo Motivo

```html
<!-- templates/cu/resultado-analisis/form.html -->
<form th:action="@{/cu/resultado-analisis/guardar}" method="post" th:object="${dto}">

    <input type="hidden" th:field="*{analisisId}" />

    <!-- Campos normales -->
    <div class="form-group">
        <label for="resultado">Resultado:</label>
        <select th:field="*{resultado}" class="form-control" required>
            <option value="">Seleccione...</option>
            <option value="SATISFACTORIO">Satisfactorio</option>
            <option value="NO_SATISFACTORIO">No Satisfactorio</option>
        </select>
    </div>

    <div class="form-group">
        <label for="dictamen">Dictamen:</label>
        <select th:field="*{dictamen}" class="form-control" required>
            <option value="">Seleccione...</option>
            <option value="APROBADO">Aprobado</option>
            <option value="RECHAZADO">Rechazado</option>
            <option value="CUARENTENA">Cuarentena</option>
        </select>
    </div>

    <!-- ¡NUEVO! Campo obligatorio de motivo -->
    <div class="form-group">
        <label for="motivoCambio">
            <strong>Motivo del Cambio (Obligatorio):</strong>
            <span class="text-danger">*</span>
        </label>
        <textarea name="motivoCambio" id="motivoCambio"
                  class="form-control" rows="3" required minlength="20"
                  placeholder="Ej: Resultados de análisis microbiológico satisfactorios según certificado #12345. Todos los parámetros dentro de especificación."></textarea>
        <small class="form-text text-muted">
            <i class="fa fa-info-circle"></i>
            Mínimo 20 caracteres. Este motivo será registrado en el audit trail
            y es requerido por normativa ANMAT (Buenas Prácticas de Fabricación).
        </small>
        <span th:if="${#fields.hasErrors('motivoCambio')}"
              th:errors="*{motivoCambio}"
              class="text-danger"></span>
    </div>

    <!-- Observaciones (opcional) -->
    <div class="form-group">
        <label for="observaciones">Observaciones (Opcional):</label>
        <textarea th:field="*{observaciones}" class="form-control" rows="2"></textarea>
    </div>

    <div class="form-group mt-3">
        <button type="submit" class="btn btn-primary">
            <i class="fa fa-save"></i> Guardar Cambios
        </button>
        <a th:href="@{/cu/resultado-analisis/lista}" class="btn btn-secondary">
            <i class="fa fa-times"></i> Cancelar
        </a>
    </div>
</form>

<!-- JavaScript para contador de caracteres -->
<script>
document.getElementById('motivoCambio').addEventListener('input', function() {
    const minLength = 20;
    const currentLength = this.value.length;
    const counterEl = document.getElementById('charCounter');

    if (!counterEl) {
        const counter = document.createElement('small');
        counter.id = 'charCounter';
        counter.className = 'form-text';
        this.parentElement.appendChild(counter);
    }

    const remaining = minLength - currentLength;
    document.getElementById('charCounter').innerHTML =
        remaining > 0
            ? `<span class="text-warning">Faltan ${remaining} caracteres</span>`
            : `<span class="text-success">✓ ${currentLength} caracteres</span>`;
});
</script>
```

---

## 2. Vista de Historial de Audit Trail

```html
<!-- templates/auditoria/historial.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" layout:decorate="~{layout/main}">
<head>
    <title>Historial de Cambios</title>
    <style>
        @media print {
            .no-print { display: none; }
            body { font-size: 10pt; }
        }
        .old-value { background-color: #ffcccc; }
        .new-value { background-color: #ccffcc; }
        .gxp-badge { background-color: #ffc107; color: #000; }
    </style>
</head>
<body>
    <div layout:fragment="content">
        <h2>
            <i class="fa fa-history"></i>
            Historial de Cambios - [[${entityName}]] ID: [[${entityId}]]
        </h2>

        <!-- Botones de acción -->
        <div class="mb-3 no-print">
            <button onclick="window.print()" class="btn btn-primary">
                <i class="fa fa-print"></i> Imprimir
            </button>
            <a th:href="@{/auditoria/exportar(entityName=${entityName},entityId=${entityId})}"
               class="btn btn-success">
                <i class="fa fa-file-excel"></i> Exportar a Excel
            </a>
            <button onclick="toggleOnlyGxp()" class="btn btn-warning">
                <i class="fa fa-filter"></i> Solo cambios GxP
            </button>
        </div>

        <!-- Filtros -->
        <div class="card mb-3 no-print">
            <div class="card-body">
                <h5>Filtros</h5>
                <form method="get">
                    <input type="hidden" name="entityName" th:value="${entityName}" />
                    <input type="hidden" name="entityId" th:value="${entityId}" />

                    <div class="row">
                        <div class="col-md-3">
                            <label>Desde:</label>
                            <input type="date" name="startDate" class="form-control"
                                   th:value="${startDate}" />
                        </div>
                        <div class="col-md-3">
                            <label>Hasta:</label>
                            <input type="date" name="endDate" class="form-control"
                                   th:value="${endDate}" />
                        </div>
                        <div class="col-md-3">
                            <label>Usuario:</label>
                            <select name="userId" class="form-control">
                                <option value="">Todos</option>
                                <option th:each="user : ${usuarios}"
                                        th:value="${user.id}"
                                        th:text="${user.username}"
                                        th:selected="${user.id == userId}"></option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label>&nbsp;</label>
                            <button type="submit" class="btn btn-primary form-control">
                                <i class="fa fa-search"></i> Buscar
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <!-- Tabla de historial -->
        <div class="card">
            <div class="card-body">
                <table class="table table-bordered table-striped table-sm">
                    <thead class="thead-dark">
                        <tr>
                            <th>Fecha/Hora</th>
                            <th>Usuario</th>
                            <th>Rol</th>
                            <th>Campo</th>
                            <th>Valor Anterior</th>
                            <th>Valor Nuevo</th>
                            <th>Motivo</th>
                            <th class="no-print">GxP</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr th:each="audit : ${cambios}"
                            th:classappend="${audit.isGxpRelevant} ? 'table-warning' : ''">

                            <!-- Fecha/Hora -->
                            <td>
                                [[${#temporals.format(audit.timestamp, 'dd/MM/yyyy HH:mm:ss')}]]
                            </td>

                            <!-- Usuario -->
                            <td>
                                [[${audit.username}]]
                                <br/>
                                <small class="text-muted">IP: [[${audit.ipAddress}]]</small>
                            </td>

                            <!-- Rol -->
                            <td>
                                <span class="badge badge-info">[[${audit.roleName}]]</span>
                            </td>

                            <!-- Campo -->
                            <td>
                                <strong>[[${audit.fieldName}]]</strong>
                                <br/>
                                <small class="text-muted">
                                    <span th:text="${audit.changeType.displayName}"></span>
                                </small>
                            </td>

                            <!-- Valor Anterior -->
                            <td class="old-value">
                                <code th:text="${audit.oldValue ?: 'N/A'}"></code>
                            </td>

                            <!-- Valor Nuevo -->
                            <td class="new-value">
                                <code th:text="${audit.newValue ?: 'N/A'}"></code>
                            </td>

                            <!-- Motivo -->
                            <td>
                                <span th:text="${audit.reason}"></span>
                            </td>

                            <!-- Badge GxP -->
                            <td class="text-center no-print">
                                <span th:if="${audit.isGxpRelevant}"
                                      class="badge gxp-badge"
                                      title="Dato crítico GxP">
                                    GxP
                                </span>
                            </td>
                        </tr>
                    </tbody>
                </table>

                <!-- Paginación -->
                <nav th:if="${cambios.totalPages > 1}" class="no-print">
                    <ul class="pagination">
                        <li class="page-item" th:classappend="${cambios.first} ? 'disabled'">
                            <a class="page-link" th:href="@{''(page=${cambios.number - 1})}">Anterior</a>
                        </li>
                        <li class="page-item" th:each="i : ${#numbers.sequence(0, cambios.totalPages - 1)}"
                            th:classappend="${i == cambios.number} ? 'active'">
                            <a class="page-link" th:href="@{''(page=${i})}" th:text="${i + 1}"></a>
                        </li>
                        <li class="page-item" th:classappend="${cambios.last} ? 'disabled'">
                            <a class="page-link" th:href="@{''(page=${cambios.number + 1})}">Siguiente</a>
                        </li>
                    </ul>
                </nav>

                <!-- Total de cambios -->
                <div class="mt-3">
                    <p class="text-muted">
                        Total de cambios registrados: [[${cambios.totalElements}]]
                        <span th:if="${gxpCount != null}">
                            (Cambios GxP: [[${gxpCount}]])
                        </span>
                    </p>
                </div>
            </div>
        </div>

        <!-- Firmas electrónicas (si existen) -->
        <div class="card mt-3" th:if="${firmas != null && !firmas.isEmpty()}">
            <div class="card-header bg-success text-white">
                <h5><i class="fa fa-pen"></i> Firmas Electrónicas</h5>
            </div>
            <div class="card-body">
                <table class="table table-sm">
                    <thead>
                        <tr>
                            <th>Firmante</th>
                            <th>Fecha/Hora</th>
                            <th>Significado</th>
                            <th>Hash</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr th:each="firma : ${firmas}">
                            <td>[[${firma.signedBy.username}]] ([[${firma.signedBy.role.name}]])</td>
                            <td>[[${#temporals.format(firma.signedAt, 'dd/MM/yyyy HH:mm:ss')}]]</td>
                            <td>[[${firma.meaning}]]</td>
                            <td><code th:text="${#strings.substring(firma.signatureHash, 0, 16)}"></code>...</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script>
    function toggleOnlyGxp() {
        const rows = document.querySelectorAll('tbody tr');
        rows.forEach(row => {
            if (!row.classList.contains('table-warning')) {
                row.style.display = row.style.display === 'none' ? '' : 'none';
            }
        });
    }
    </script>
</body>
</html>
```

---

*Este documento continúa en la siguiente sección con Firma Electrónica, Políticas de Contraseña, etc.*

**Nota:** El documento es extenso. Para mantener el token limit manejable, voy a dividir las especificaciones técnicas en secciones y continuar con los siguientes documentos (06 y 07).

[← Plan de Implementación](./ANMAT-04-PLAN-IMPLEMENTACION.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Documentación Requerida →](./ANMAT-06-DOCUMENTACION-REQUERIDA.md)
