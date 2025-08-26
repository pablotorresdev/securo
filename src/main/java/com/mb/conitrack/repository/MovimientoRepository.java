package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

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
          order by m.fecha asc, m.fechaYHoraCreacion asc
        """)
    List<Movimiento> findAllByLoteCodigoLoteOrderByFechaAsc(String codigoLote);

    Optional<Movimiento> findByCodigoMovimientoAndActivoTrue(String codigoMovimiento);

    List<Movimiento> findByActivoTrueOrderByFechaAsc();

    @Query("""
            select m
            from Movimiento m
            where m.activo = true
              and m.motivo = com.mb.conitrack.enums.MotivoEnum.MUESTREO
            order by case when m.fecha is null then 1 else 0 end, m.fecha asc
        """)
    List<Movimiento> findMuestreosActivosOrderByFechaAscNullsLast();

    List<Movimiento> findByLote_CodigoLote(String codigoLote);

    // (si usás borrado lógico) sólo activos
    List<Movimiento> findByLote_CodigoLoteAndActivoTrue(String codigoLote);

    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM Movimiento m
            WHERE m.lote.codigoLote = :codigoLote
              AND m.tipoMovimiento = com.mb.conitrack.enums.TipoMovimientoEnum.BAJA
              AND m.motivo = com.mb.conitrack.enums.MotivoEnum.MUESTREO
              AND m.nroAnalisis = :nroAnalisis
        """)
    boolean existeMuestreo(
        @Param("codigoLote") String codigoLote,
        @Param("nroAnalisis") String nroAnalisis
    );

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
          select distinct t
          from DetalleMovimiento d
            join d.movimiento m
            join d.trazas t
          where m.codigoMovimiento = :codigoMovimiento
            and m.activo = true
            and t.estado = com.mb.conitrack.enums.EstadoEnum.VENDIDO
          order by t.bulto.nroBulto asc, t.nroTraza asc
        """)
    List<Traza> findTrazasVendidasByCodigoMovimiento(@Param("codigoMovimiento") String codigoMovimiento);

}



