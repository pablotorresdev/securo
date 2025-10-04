package com.mb.conitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.Traza;

public interface TrazaRepository extends JpaRepository<Traza, Long> {

    List<Traza> findAllByActivoTrue();

    List<Traza> findByLoteCodigoLoteOrderByNroTrazaAsc(String codigoLote);

    @Query("""
        select coalesce(max(t.nroTraza), -1)
        from Traza t
        where t.producto.id = :productoId
        and t.activo = true
        """)
    Long findMaxNroTraza(@Param("productoId") Long productoId);

    @Query("""
          select t
          from Traza t
          join t.producto p
          order by p.codigoProducto asc, t.nroTraza asc
        """)
    List<Traza> findAllTrazaAudit();

    @Query("""
            select distinct t
            from Traza t
            join t.detalles d
            join d.movimiento m
            where m.codigoMovimiento = :codigoMovimiento
              and m.activo = true
              and t.estado = com.mb.conitrack.enums.EstadoEnum.VENDIDO
            order by t.nroTraza
        """)
    List<Traza> findVendidasByCodigoMovimiento(String codigoMovimiento);

    //***********CU24 MODIFICACION: RETIRO MERCADO***********
    @Query("""
          select t
          from Traza t
            join t.lote l
          where l.codigoLote = :codigoLote
            and t.estado = com.mb.conitrack.enums.EstadoEnum.VENDIDO
            and t.activo = true
          order by t.nroTraza asc
        """)
    List<Traza> findVendidasByCodigoLote(@Param("codigoLote") String codigoLote);

    @Query("""
        select t
        from Traza t
          join t.lote l
          join t.bulto b
        where l.codigoLote = :codigoLote
          and b.nroBulto   = :nroBulto
          and t.activo = true
          and t.estado = com.mb.conitrack.enums.EstadoEnum.DISPONIBLE
        order by t.nroTraza asc
    """)
    List<Traza> findDisponiblesByCodigoLoteAndNroBulto(@Param("codigoLote") String codigoLote,
        @Param("nroBulto") Integer nroBulto);

}
