package com.mb.conitrack.service;

import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.PermisosCasoUsoEnum;
import com.mb.conitrack.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio para verificar permisos de acceso a casos de uso.
 * Facilita la verificación de permisos en controllers y vistas Thymeleaf.
 */
@Service
public class PermisosCasoUsoService {

    @Autowired
    private SecurityContextService securityContextService;

    /**
     * Verifica si el usuario actual tiene permiso para acceder a un caso de uso.
     */
    public boolean tienePermiso(PermisosCasoUsoEnum casoUso) {
        try {
            User currentUser = securityContextService.getCurrentUser();
            RoleEnum roleEnum = currentUser.getRole().getRoleEnum();
            return casoUso.tienePermiso(roleEnum);
        } catch (Exception e) {
            return false; // Si hay error, negar acceso
        }
    }

    /**
     * Verifica si el usuario actual es ADMIN.
     */
    public boolean esAdmin() {
        try {
            User currentUser = securityContextService.getCurrentUser();
            return "ADMIN".equals(currentUser.getRole().getName());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el usuario actual es AUDITOR.
     */
    public boolean esAuditor() {
        try {
            User currentUser = securityContextService.getCurrentUser();
            return currentUser.isAuditor();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el nombre del rol del usuario actual.
     */
    public String getRolActual() {
        try {
            User currentUser = securityContextService.getCurrentUser();
            return currentUser.getRole().getName();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Obtiene el username del usuario actual.
     */
    public String getUsernameActual() {
        try {
            return securityContextService.getCurrentUsername();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
