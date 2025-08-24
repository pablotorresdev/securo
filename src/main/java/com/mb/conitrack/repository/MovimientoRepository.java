package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findAllByActivoTrue();

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
}
