package com.mb.securo.repository.maestro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.maestro.Contacto;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    // Contiene "conifarma" sin importar mayúsculas/minúsculas
    List<Contacto> findByRazonSocialIgnoreCaseContaining(String razonSocial);

    // No contiene "conifarma" sin importar mayúsculas/minúsculas
    List<Contacto> findByRazonSocialNotIgnoreCaseContaining(String razonSocial);
}
