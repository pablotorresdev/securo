package com.mb.conitrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.entity.AuditoriaAcceso;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.repository.AuditoriaAccesoRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para registrar accesos de auditores al sistema.
 * Implementa doble registro:
 * 1. Log en archivo de aplicación (vía @Slf4j)
 * 2. Registro en tabla de base de datos (auditoria_accesos)
 */
@Service
@Slf4j
public class AuditorAccessLogger {

    @Autowired
    private AuditoriaAccesoRepository auditoriaAccesoRepository;

    /**
     * Registra un acceso de auditor con información completa.
     * Usa REQUIRES_NEW para garantizar que el log se persista incluso si hay rollback.
     *
     * @param user Usuario que accede (debe ser AUDITOR)
     * @param accion Descripción de la acción realizada
     * @param request HttpServletRequest para obtener datos de la petición
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAccess(User user, String accion, HttpServletRequest request) {
        if (user == null) {
            log.warn("Intento de log de acceso con usuario null");
            return;
        }

        String username = user.getUsername();
        String roleName = user.getRole() != null ? user.getRole().getName() : "UNKNOWN";
        String url = request != null ? request.getRequestURI() : null;
        String metodoHttp = request != null ? request.getMethod() : null;
        String ipAddress = request != null ? getClientIpAddress(request) : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        // 1. Log en archivo
        log.info("AUDITOR_ACCESS | user={} | role={} | action={} | url={} | method={} | ip={}",
            username, roleName, accion, url, metodoHttp, ipAddress);

        // 2. Log en base de datos
        try {
            AuditoriaAcceso registro = AuditoriaAcceso.builder()
                .user(user)
                .username(username)
                .roleName(roleName)
                .accion(accion)
                .url(url)
                .metodoHttp(metodoHttp)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

            auditoriaAccesoRepository.save(registro);
            log.debug("Registro de auditoría guardado en BD: id={}", registro.getId());

        } catch (Exception e) {
            // No fallar si hay error al guardar en BD, al menos quedó el log
            log.error("Error al guardar registro de auditoría en BD: {}", e.getMessage(), e);
        }
    }

    /**
     * Versión simplificada para casos donde no hay HttpServletRequest disponible.
     * Solo registra usuario y acción.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAccess(User user, String accion) {
        logAccess(user, accion, null);
    }

    /**
     * Log específico para auditor consultando reportes.
     */
    public void logReporteAccess(User user, String tipoReporte, HttpServletRequest request) {
        String accion = String.format("Consulta reporte: %s", tipoReporte);
        logAccess(user, accion, request);
    }

    /**
     * Log específico para intentos no autorizados de modificación.
     */
    public void logUnauthorizedModification(User user, String recurso, HttpServletRequest request) {
        String accion = String.format("Intento no autorizado de modificación: %s", recurso);
        log.warn("SECURITY_ALERT | user={} | role={} | action={}",
            user.getUsername(),
            user.getRole() != null ? user.getRole().getName() : "UNKNOWN",
            accion);
        logAccess(user, accion, request);
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies y balanceadores.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
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

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For puede contener múltiples IPs, tomar la primera
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
