package com.mb.conitrack.entity.maestro;

import java.util.List;

import com.mb.conitrack.enums.RoleEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Role name is required")
    @Column(unique = true)
    private String name;

    @NotNull(message = "Role nivel is required")
    @Column(nullable = false)
    private Integer nivel;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<User> users;

    public Role(String name) {
        this.name = name;
    }

    public Role(String name, Integer nivel) {
        this.name = name;
        this.nivel = nivel;
    }

    /**
     * Crea Role desde RoleEnum.
     * @param roleEnum Enum con datos del rol
     * @return Role entity con nombre y nivel
     */
    public static Role fromEnum(RoleEnum roleEnum) {
        Role role = new Role(roleEnum.name());
        role.setNivel(roleEnum.getNivel());
        return role;
    }

    /**
     * Obtiene el RoleEnum correspondiente a este rol.
     * @return RoleEnum
     * @throws IllegalArgumentException si el nombre no corresponde a ningún RoleEnum
     */
    public RoleEnum getRoleEnum() {
        return RoleEnum.fromName(this.name);
    }

}

