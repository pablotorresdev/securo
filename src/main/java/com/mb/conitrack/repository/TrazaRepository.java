package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.Traza;

public interface TrazaRepository extends JpaRepository<Traza, Long> {

    Optional<Traza> findTopByProductoIdOrderByNroTrazaDesc(Long productoId);

    List<Traza> findByLoteIdOrderByNroTrazaAsc(Long loteId);

    @Query("select coalesce(max(t.nroTraza), -1) " +
        "from Traza t " +
        "where t.producto.id = :productoId")
    Long findMaxNroTraza(@Param("productoId") Long productoId);

    List<Traza> findAllByOrderByNroTrazaAsc();
}
