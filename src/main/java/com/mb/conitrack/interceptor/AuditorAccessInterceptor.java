package com.mb.conitrack.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.service.AuditorAccessLogger;
import com.mb.conitrack.service.SecurityContextService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor que registra automáticamente todos los accesos de usuarios AUDITOR.
 *
 * Se ejecuta ANTES de que el controller procese la petición, permitiendo:
 * - Logging de todos los accesos de auditores
 * - Detección de intentos no autorizados
 * - Auditoría completa de actividad de auditores externos
 *
 * El interceptor NO bloquea peticiones, solo registra.
 * El bloqueo se maneja en SecurityConfig y @PreAuthorize.
 */
@Component
@Slf4j
public class AuditorAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private AuditorAccessLogger auditorAccessLogger;

    /**
     * Se ejecuta ANTES del controller.
     * Registra el acceso si el usuario es AUDITOR.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Verificar si hay usuario autenticado
            if (!securityContextService.isAuthenticated()) {
                return true; // Dejar que Spring Security maneje la autenticación
            }

            User currentUser = securityContextService.getCurrentUser();

            // Si es AUDITOR, registrar el acceso
            if (currentUser.isAuditor()) {
                String url = request.getRequestURI();
                String method = request.getMethod();

                // Construir descripción de la acción
                String accion = String.format("%s %s", method, url);

                // Log del acceso
                auditorAccessLogger.logAccess(currentUser, accion, request);

                // Detectar intentos de modificación (POST, PUT, DELETE, PATCH)
                if (isModificationRequest(method)) {
                    auditorAccessLogger.logUnauthorizedModification(
                        currentUser,
                        url,
                        request
                    );
                    log.warn("AUDITOR {} intentó realizar operación de modificación: {} {}",
                        currentUser.getUsername(), method, url);
                }
            }

        } catch (Exception e) {
            // No fallar la petición si hay error en el logging
            log.error("Error en AuditorAccessInterceptor: {}", e.getMessage(), e);
        }

        return true; // Continuar con la petición
    }

    /**
     * Verifica si el método HTTP es de modificación.
     */
    private boolean isModificationRequest(String method) {
        return "POST".equalsIgnoreCase(method) ||
               "PUT".equalsIgnoreCase(method) ||
               "DELETE".equalsIgnoreCase(method) ||
               "PATCH".equalsIgnoreCase(method);
    }
}
