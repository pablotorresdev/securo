package com.mb.conitrack.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.exception.ReversoNotAuthorizedException;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para autorización de reversos de movimientos.
 *
 * Reglas de negocio:
 * 1. El usuario que CREÓ el movimiento puede reversarlo
 * 2. Usuarios con nivel jerárquico SUPERIOR pueden reversar
 * 3. Usuarios con mismo nivel o inferior NO pueden reversar (excepto el creador)
 * 4. AUDITOR NUNCA puede reversar (es solo lectura)
 * 5. Movimientos legacy (sin creador): solo ADMIN puede reversar
 *
 * Jerarquía:
 * ADMIN (6) > DT (5) > GERENTE_GARANTIA (4) > GERENTE_CONTROL/SUPERVISOR (3) >
 * ANALISTA_CONTROL/ANALISTA_PLANTA (2) > AUDITOR (1)
 */
@Service
@Slf4j
public class ReversoAuthorizationService {

    /**
     * Verifica si un usuario puede reversar un movimiento.
     * @param movimiento Movimiento a reversar
     * @param userActual Usuario que intenta reversar
     * @return true si tiene permiso, false si no
     */
    public boolean puedeReversar(Movimiento movimiento, User userActual) {
        Objects.requireNonNull(movimiento, "movimiento no puede ser null");
        Objects.requireNonNull(userActual, "userActual no puede ser null");

        log.debug("Verificando permiso de reverso: movimiento.id={}, usuario={}",
            movimiento.getId(), userActual.getUsername());

        // Regla 4: AUDITOR NUNCA puede reversar
        if (userActual.isAuditor()) {
            log.warn("AUDITOR {} intentó reversar movimiento {}. DENEGADO.",
                userActual.getUsername(), movimiento.getId());
            return false;
        }

        // Regla 5: Movimientos legacy (sin creador): solo ADMIN
        if (movimiento.getCreadoPor() == null) {
            boolean esAdmin = esAdmin(userActual);
            log.debug("Movimiento legacy sin creador. Usuario {} es ADMIN: {}",
                userActual.getUsername(), esAdmin);
            return esAdmin;
        }

        // Regla 1: Si es el mismo usuario que creó, puede reversar
        if (movimiento.getCreadoPor().getId().equals(userActual.getId())) {
            log.debug("Usuario {} es el creador del movimiento {}. AUTORIZADO.",
                userActual.getUsername(), movimiento.getId());
            return true;
        }

        // Regla 2 y 3: Verificar jerarquía
        Integer nivelCreador = movimiento.getCreadoPor().getRole().getNivel();
        Integer nivelActual = userActual.getRole().getNivel();

        boolean tieneNivelSuperior = nivelActual > nivelCreador;

        log.debug("Verificando jerarquía: {} (nivel {}) vs creador {} (nivel {}). " +
                  "Tiene nivel superior: {}",
            userActual.getUsername(), nivelActual,
            movimiento.getCreadoPor().getUsername(), nivelCreador,
            tieneNivelSuperior);

        return tieneNivelSuperior;
    }

    /**
     * Valida que el usuario tenga permiso para reversar.
     * Lanza excepción si no tiene permiso.
     * @param movimiento Movimiento a reversar
     * @param userActual Usuario que intenta reversar
     * @throws ReversoNotAuthorizedException si no tiene permiso
     */
    public void validarPermisoReverso(Movimiento movimiento, User userActual) {
        if (!puedeReversar(movimiento, userActual)) {
            String mensajeError = construirMensajeError(movimiento, userActual);
            log.warn("Intento no autorizado de reverso: {}", mensajeError);
            throw new ReversoNotAuthorizedException(mensajeError);
        }

        log.info("Autorización de reverso concedida: usuario={}, movimiento={}",
            userActual.getUsername(), movimiento.getId());
    }

    /**
     * Construye mensaje de error descriptivo según el caso.
     */
    private String construirMensajeError(Movimiento movimiento, User userActual) {
        // Caso: AUDITOR
        if (userActual.isAuditor()) {
            return "Los auditores no tienen permisos para reversar movimientos. " +
                   "El rol AUDITOR es de solo lectura.";
        }

        // Caso: Movimiento legacy sin creador
        if (movimiento.getCreadoPor() == null) {
            return String.format(
                "No tiene permisos para reversar este movimiento legacy. " +
                "El movimiento no tiene usuario asignado (datos anteriores al sistema de tracking). " +
                "Solo el administrador puede reversar movimientos legacy. " +
                "Su rol: %s (nivel %d).",
                userActual.getRole().getName(),
                userActual.getRole().getNivel());
        }

        // Caso: Usuario diferente sin jerarquía suficiente
        return String.format(
            "No tiene permisos para reversar este movimiento. " +
            "Fue creado por %s (rol: %s, nivel %d). " +
            "Su rol es %s (nivel %d). " +
            "Solo puede reversarlo el usuario que lo creó o un superior jerárquico.",
            movimiento.getCreadoPor().getUsername(),
            movimiento.getCreadoPor().getRole().getName(),
            movimiento.getCreadoPor().getRole().getNivel(),
            userActual.getRole().getName(),
            userActual.getRole().getNivel());
    }

    /**
     * Verifica si el usuario es ADMIN.
     */
    private boolean esAdmin(User user) {
        return RoleEnum.ADMIN.name().equals(user.getRole().getName());
    }

    /**
     * Obtiene un resumen de los permisos del usuario para logging/auditoría.
     */
    public String obtenerResumenPermisos(User user) {
        if (user == null) {
            return "Usuario null - sin permisos";
        }

        if (user.isAuditor()) {
            return String.format("AUDITOR %s - Solo lectura, sin permisos de reverso",
                user.getUsername());
        }

        return String.format("Usuario %s - Rol: %s (nivel %d) - Puede reversar: movimientos propios y de niveles inferiores",
            user.getUsername(),
            user.getRole().getName(),
            user.getRole().getNivel());
    }
}
