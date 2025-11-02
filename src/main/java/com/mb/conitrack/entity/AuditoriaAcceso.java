package com.mb.conitrack.entity;

import java.time.OffsetDateTime;

import com.mb.conitrack.entity.maestro.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad para registrar accesos al sistema, especialmente de auditores externos.
 * Registra cada acción realizada por usuarios AUDITOR para cumplir con requisitos
 * de auditoría y trazabilidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auditoria_accesos")
public class AuditoriaAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "accion", nullable = false, length = 255)
    private String accion;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "metodo_http", length = 10)
    private String metodoHttp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaHora = OffsetDateTime.now();
}
