package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findAllByActivoTrue();

    @Query("""
          select m
          from Movimiento m
          join m.lote l
          order by l.codigoLote asc, m.fecha asc, m.fechaYHoraCreacion asc
        """)
    List<Movimiento> findAllAudit();

    @Query("""
          select m
          from Movimiento m
          join m.lote l
          where l.codigoLote = :codigoLote
            and m.activo = true
          order by m.fecha asc, m.fechaYHoraCreacion asc
        """)
    List<Movimiento> findAllByLoteCodigoLoteOrderByFechaAsc(String codigoLote);

    Optional<Movimiento> findByCodigoMovimientoAndActivoTrue(String codigoMovimiento);

    @Query("""
          select m
          from Movimiento m
          where m.codigoMovimiento = :codigoMovimiento
            and m.activo = true
        """)
    List<Movimiento> findAllByCodigoMovimiento(String codigoMovimiento);

    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM Movimiento m
            WHERE m.lote.codigoLote = :codigoLote
              AND m.tipoMovimiento = com.mb.conitrack.enums.TipoMovimientoEnum.BAJA
              AND m.motivo = com.mb.conitrack.enums.MotivoEnum.MUESTREO
              AND m.nroAnalisis = :nroAnalisis
        """)
    boolean existeMuestreo(@Param("codigoLote") String codigoLote, @Param("nroAnalisis") String nroAnalisis);

    @Query("""
          select distinct m
          from Movimiento m
          join m.lote l
          join m.detalles d
          join d.trazas t
          where l.codigoLote = :codigoLote
            and m.activo = true
            and m.motivo = com.mb.conitrack.enums.MotivoEnum.VENTA
            and t.estado = com.mb.conitrack.enums.EstadoEnum.VENDIDO
          order by m.fecha asc, m.fechaYHoraCreacion asc
        """)
    List<Movimiento> findVentasConTrazasVendidasByCodigoLote(@Param("codigoLote") String codigoLote);

    @Query("""
            select m
            from Movimiento m
            join m.lote l
            where l.codigoLote = :codigoLote
              and m.activo = true
            order by m.fecha desc, m.fechaYHoraCreacion desc
        """)
    List<Movimiento> findLatestByCodigoLote(@Param("codigoLote") String codigoLote);

    @Query("""
            select m
            FROM Movimiento m
            WHERE m.tipoMovimiento = com.mb.conitrack.enums.TipoMovimientoEnum.MODIFICACION
              AND m.motivo = com.mb.conitrack.enums.MotivoEnum.ANALISIS
              AND m.nroAnalisis = :nroAnalisis
        """)
    List<Movimiento> findMovModifAnalisisByNro(@Param("nroAnalisis") String nroAnalisis);

}





