package com.mb.securo.repository.maestro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.maestro.Contacto;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {
}
