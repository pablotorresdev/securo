package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;

public interface BultoRepository extends JpaRepository<Bulto, Long> {

    List<Bulto> findAllByLoteCodigoLoteOrderByNroBultoAsc(String codigoLote);

    List<Bulto> findAllByLoteAndActivoTrue(Lote lote);

    List<Bulto> findAllByActivoTrue();

    Optional<Bulto> findFirstByLoteAndNroBultoAndActivoTrue(Lote lote, int nroBulto);

    Optional<Bulto> findFirstByLoteIdAndActivoTrueOrderByNroBultoAsc(Long loteId);

    List<Bulto> findAllByLoteAndActivoTrueOrderByNroBultoAsc(Lote lote);

    Optional<Bulto> findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue(String codigoLote, int nroBulto);

    List<Bulto> findAllByOrderByIdAsc();

    @Query("select b from Bulto b " +
        "where b.activo = true and b.lote.codigoLote = :codigoLote " +
        "order by b.nroBulto asc")
    List<Bulto> findActivosByLoteCodigoLote(@Param("codigoLote") String codigoLote);

}
