package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.Traza;

public interface TrazaRepository extends JpaRepository<Traza, Long> {

    /**
     * Devuelve la Traza con mayor nroTraza para un determinado producto (por su ID).
     */
    Optional<Traza> findTopByProductoIdOrderByNroTrazaDesc(Long productoId);

    List<Traza> findByLoteIdOrderByNroTrazaAsc(Long loteId);

}
