package com.mb.conitrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.Analisis;

public interface AnalisisRepository extends JpaRepository<Analisis, Long> {

    Optional<Analisis> findByNroAnalisis(String nroAnalisis);

}
