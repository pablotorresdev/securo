package com.mb.securo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.Contacto;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    List<Contacto> findByActivoTrue();

}
