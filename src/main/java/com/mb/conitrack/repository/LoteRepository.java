package com.mb.conitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findAllByIdLoteAndActivoTrue(String idLote);

    List<Lote> findAllByAnalisisAndActivoTrue(Analisis analisis);

    List<Lote> findAllByLoteProveedorAndActivoTrue(String lote);

}
