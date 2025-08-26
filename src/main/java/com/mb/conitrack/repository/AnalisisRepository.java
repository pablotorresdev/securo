package com.mb.conitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.Analisis;

public interface AnalisisRepository extends JpaRepository<Analisis, Long> {

    @Query("""
          select a
          from Analisis a
          join a.lote l
          order by l.codigoLote asc, a.fechaRealizado asc, a.fechaYHoraCreacion asc
        """)
    List<Analisis> findAllAudit();

    @EntityGraph(attributePaths = "lote")
    Analisis findByNroAnalisisAndActivoTrue(String nroAnalisis);

    Analisis findByNroAnalisisAndDictamenIsNotNullAndActivoTrue(String nroAnalisis);

    @EntityGraph(attributePaths = "lote") // opcional, evita N+1 si usás a.getLote()
    @Query("""
            select a
            from Analisis a
            join a.lote l
            where a.activo = true
              and a.dictamen is null
              and a.fechaRealizado is null
              and l.dictamen = com.mb.conitrack.enums.DictamenEnum.CUARENTENA
            order by case when a.fechaYHoraCreacion is null then 1 else 0 end,
                     a.fechaYHoraCreacion desc
        """)
    List<Analisis> findAllEnCursoForLotesCuarentena();

    @Query("""
            select a
            from Analisis a
            where a.activo = true
            order by case when a.fechaRealizado is null then 1 else 0 end,
                     a.fechaRealizado desc
        """)
    List<Analisis> findActivosOrderByFechaRealizadoDescNullsLast();

    @Query("""
        select a
        from Analisis a
        join a.lote l
        where a.activo = true
        and a.dictamen = com.mb.conitrack.enums.DictamenEnum.APROBADO
        and a.titulo is not null
        and l.codigoLote = :codigoLote
        order by a.fechaYHoraCreacion desc
        """)
    List<Analisis> findUltimoAprobadoConTituloPorCodigoLote(@Param("codigoLote") String codigoLote);

    @Query("""
        select a
        from Analisis a
        join a.lote l
        where a.activo = true
        and l.codigoLote = :codigoLote
        order by a.fechaYHoraCreacion desc
        """)
    List<Analisis> findByCodigoLote(String codigoLote);






}
