package com.mb.conitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findAllByCodigoInternoAndActivoTrue(String codigoInterno);

    List<Lote> findByActivoTrue();

}
