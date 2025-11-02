package com.mb.conitrack.entity.maestro;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotNull(message = "Password is required")
    @Size(min = 3, message = "Password must be at least 3 characters")
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "fecha_expiracion")
    private LocalDate fechaExpiracion;

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Verifica si el usuario ha expirado.
     * @return true si tiene fecha de expiración y ya pasó
     */
    public boolean isExpired() {
        return fechaExpiracion != null && LocalDate.now().isAfter(fechaExpiracion);
    }

    /**
     * Verifica si el usuario es un AUDITOR.
     * @return true si el rol es AUDITOR
     */
    public boolean isAuditor() {
        return role != null && "AUDITOR".equals(role.getName());
    }

}
