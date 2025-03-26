package com.mb.conitrack.repository.maestro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.maestro.Contacto;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    List<Contacto> findByRazonSocialIgnoreCaseContaining(String razonSocial);

    List<Contacto> findByRazonSocialNotIgnoreCaseContaining(String razonSocial);

    @Query("SELECT c FROM Contacto c WHERE c.activo = true AND LOWER(c.razonSocial) <> LOWER(:razonSocial)")
    List<Contacto> findByActivoTrueAndRazonSocialNotIgnoreCase(@Param("razonSocial") String razonSocial);


}
