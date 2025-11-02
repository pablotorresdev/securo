package com.mb.conitrack.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum de roles del sistema con jerarquía definida.
 * El nivel indica la jerarquía: mayor número = mayor autoridad.
 *
 * Jerarquía:
 * 1. ADMIN (nivel 6) - Administrador del sistema
 * 2. DT (nivel 5) - Director Técnico
 * 3. GERENTE_GARANTIA_CALIDAD (nivel 4) - Gerente de Garantía de Calidad
 * 4. GERENTE_CONTROL_CALIDAD (nivel 3) - Gerente de Control de Calidad
 * 5. SUPERVISOR_PLANTA (nivel 3) - Supervisor de Planta
 * 6. ANALISTA_CONTROL_CALIDAD (nivel 2) - Analista de Control de Calidad
 * 7. ANALISTA_PLANTA (nivel 2) - Analista de Planta
 * 8. AUDITOR (nivel 1) - Auditor externo (solo lectura, sin permisos de modificación)
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {
    ADMIN(6, "Administrador", true, true, false),
    DT(5, "Director Técnico", true, true, false),
    GERENTE_GARANTIA_CALIDAD(4, "Gerente de Garantía de Calidad", true, true, false),
    GERENTE_CONTROL_CALIDAD(3, "Gerente de Control de Calidad", true, true, false),
    SUPERVISOR_PLANTA(3, "Supervisor de Planta", true, true, false),
    ANALISTA_CONTROL_CALIDAD(2, "Analista de Control de Calidad", true, true, false),
    ANALISTA_PLANTA(2, "Analista de Planta", true, true, false),
    AUDITOR(1, "Auditor", true, false, true);

    /** Nivel jerárquico (1-6). Mayor número = mayor autoridad */
    private final int nivel;

    /** Nombre descriptivo para mostrar en UI */
    private final String displayName;

    /** Puede ver datos del sistema */
    private final boolean canView;

    /** Puede modificar datos (crear, editar, eliminar) */
    private final boolean canModify;

    /** Es rol de solo lectura (auditor externo) */
    private final boolean isReadOnly;

    /**
     * Verifica si este rol tiene nivel superior o igual a otro rol.
     * @param otroRol Rol a comparar
     * @return true si este rol tiene nivel >= al otro rol
     */
    public boolean tieneNivelSuperiorOIgual(RoleEnum otroRol) {
        return this.nivel >= otroRol.nivel;
    }

    /**
     * Verifica si este rol tiene nivel estrictamente superior a otro rol.
     * @param otroRol Rol a comparar
     * @return true si este rol tiene nivel > al otro rol
     */
    public boolean tieneNivelSuperior(RoleEnum otroRol) {
        return this.nivel > otroRol.nivel;
    }

    /**
     * Obtiene el enum desde el nombre del rol.
     * @param name Nombre del rol (ej: "ADMIN", "DT")
     * @return RoleEnum correspondiente
     * @throws IllegalArgumentException si el nombre no corresponde a ningún rol
     */
    public static RoleEnum fromName(String name) {
        try {
            return RoleEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol no válido: " + name, e);
        }
    }
}
