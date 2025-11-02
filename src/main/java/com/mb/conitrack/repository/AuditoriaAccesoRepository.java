package com.mb.conitrack.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mb.conitrack.entity.AuditoriaAcceso;

/**
 * Repositorio para acceder a registros de auditoría de accesos.
 */
@Repository
public interface AuditoriaAccesoRepository extends JpaRepository<AuditoriaAcceso, Long> {

    /**
     * Busca accesos por username.
     */
    List<AuditoriaAcceso> findByUsernameOrderByFechaHoraDesc(String username);

    /**
     * Busca accesos por rol.
     */
    List<AuditoriaAcceso> findByRoleNameOrderByFechaHoraDesc(String roleName);

    /**
     * Busca accesos en un rango de fechas.
     */
    List<AuditoriaAcceso> findByFechaHoraBetweenOrderByFechaHoraDesc(
        OffsetDateTime inicio, OffsetDateTime fin);

    /**
     * Busca accesos de un usuario específico en un rango de fechas.
     */
    List<AuditoriaAcceso> findByUsernameAndFechaHoraBetweenOrderByFechaHoraDesc(
        String username, OffsetDateTime inicio, OffsetDateTime fin);
}
