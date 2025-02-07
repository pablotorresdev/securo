package com.mb.securo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.Clase;

public interface ClaseRepository extends JpaRepository<Clase, Long> {

    Optional<Clase> findByNombre(String name);

}
