package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;

public interface BultoRepository extends JpaRepository<Bulto, Long> {

    List<Bulto> findAllByActivoTrue();

    @Query("""
          select b
          from Bulto b
          join b.lote l
          order by l.codigoLote asc, b.nroBulto asc
        """)
    List<Bulto> findAllAudit();

    List<Bulto> findAllByLoteCodigoLoteOrderByNroBultoAsc(String codigoLote);

    Optional<Bulto> findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue(String codigoLote, int nroBulto);

}
