package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findAllByCodigoInternoAndActivoTrue(String codigoInternoLote);

    List<Lote> findAllByActivoTrue();

    /**
     * Spring Data compondrá la siguiente consulta: SELECT l FROM Lote l WHERE l.codigoInternoLote = ?1 AND l.nroBulto
     * = ?2 AND l.activo        = true LIMIT 1
     */
    Optional<Lote> findFirstByCodigoInternoAndNroBultoAndActivoTrue(
        String codigoInternoLote,
        int nroBulto
    );

}
