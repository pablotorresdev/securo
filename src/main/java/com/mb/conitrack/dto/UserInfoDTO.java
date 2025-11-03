package com.mb.conitrack.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para información del usuario logueado.
 * Se utiliza para mostrar datos en el dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    /**
     * Nombre de usuario
     */
    private String username;

    /**
     * Nombre del rol del usuario
     */
    private String roleName;

    /**
     * Nivel jerárquico del rol
     */
    private Integer roleLevel;

    /**
     * Fecha de último acceso al sistema
     */
    private LocalDateTime lastAccessDate;

    /**
     * Fecha de expiración de la cuenta (si aplica)
     */
    private LocalDate expirationDate;

    /**
     * Indica si el usuario es auditor (solo lectura)
     */
    private Boolean isAuditor;

    /**
     * Indica si la cuenta ha expirado
     */
    private Boolean isExpired;
}
