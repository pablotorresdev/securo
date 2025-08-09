package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;

public interface BultoRepository extends JpaRepository<Bulto, Long> {

    List<Bulto> findAllByLoteAndActivoTrue(Lote lote);

    List<Bulto> findAllByActivoTrue();

    /**
     * Spring Data compondrá la siguiente consulta: SELECT l FROM Lote l WHERE
     *      l.lote = ?1 AND l.nroBulto = ?2
     *      AND l.activo = true LIMIT 1
     */
    Optional<Bulto> findFirstByLoteAndNroBultoAndActivoTrue(Lote lote, int nroBulto);

    // ✅ por id del lote + activo, ordenado
    List<Bulto> findAllByLoteIdAndActivoTrueOrderByNroBultoAsc(Long loteId);

    // ✅ “el primero” por id del lote, ordenado (si solo querés 1)
    Optional<Bulto> findFirstByLoteIdAndActivoTrueOrderByNroBultoAsc(Long loteId);

    // ✅ por entidad Lote completa (si ya la tenés)
    List<Bulto> findAllByLoteAndActivoTrueOrderByNroBultoAsc(Lote lote);

    // ✅ el que buscabas antes pero bien tipado: por código interno del lote + nro
    Optional<Bulto> findFirstByLoteCodigoInternoAndNroBultoAndActivoTrue(String codigoInterno, int nroBulto);

}
