package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    Optional<Lote> findByIdAndActivoTrue(Long id);

    List<Lote> findAllByActivoTrue();

    List<Lote> findAllByOrderByFechaIngresoAscCodigoLoteAsc();

    List<Lote> findAllByCodigoLoteAndActivoTrue(String codigoLote);

    Optional<Lote> findFirstByCodigoLoteAndActivoTrue(String codigoLote);

    Optional<Lote> findByCodigoLoteAndActivoTrue(String codigoLote);

    List<Lote> findByActivoTrueOrderByFechaIngresoAscCodigoLoteAsc();

    @Query("""
        select l
        from Lote l
        where l.activo = true
          and l.dictamen in (
               com.mb.conitrack.enums.DictamenEnum.RECIBIDO,
               com.mb.conitrack.enums.DictamenEnum.APROBADO,
               com.mb.conitrack.enums.DictamenEnum.ANALISIS_EXPIRADO,
               com.mb.conitrack.enums.DictamenEnum.LIBERADO,
               com.mb.conitrack.enums.DictamenEnum.DEVOLUCION_CLIENTES,
               com.mb.conitrack.enums.DictamenEnum.RETIRO_MERCADO
          )
          and l.estado in (
               com.mb.conitrack.enums.EstadoEnum.NUEVO,
               com.mb.conitrack.enums.EstadoEnum.DISPONIBLE,
               com.mb.conitrack.enums.EstadoEnum.EN_USO
          )
        order by case when l.fechaIngreso is null then 1 else 0 end,
                 l.fechaIngreso asc, l.codigoLote asc
    """)
    List<Lote> findAllForCuarentena();

    @Query("""
        select l
        from Lote l
        where l.activo = true
          and (l.dictamen is null or l.dictamen <> com.mb.conitrack.enums.DictamenEnum.RECIBIDO)
          and exists (
              select 1 from Analisis a
              where a.lote = l and a.nroAnalisis is not null
          )
          and exists (
              select 1 from Bulto b
              where b.lote = l and b.cantidadActual > 0
          )
        order by case when l.fechaIngreso is null then 1 else 0 end,
                 l.fechaIngreso asc, l.codigoLote asc
    """)
    List<Lote> findAllForMuestreo();



    @Query("""
        select l
        from Lote l
        where l.activo = true
          and l.dictamen in (
              com.mb.conitrack.enums.DictamenEnum.RECIBIDO,
              com.mb.conitrack.enums.DictamenEnum.CUARENTENA,
              com.mb.conitrack.enums.DictamenEnum.APROBADO,
              com.mb.conitrack.enums.DictamenEnum.RECHAZADO
          )
          and l.producto.tipoProducto in (
              com.mb.conitrack.enums.TipoProductoEnum.API,
              com.mb.conitrack.enums.TipoProductoEnum.EXCIPIENTE,
              com.mb.conitrack.enums.TipoProductoEnum.ACOND_PRIMARIO,
              com.mb.conitrack.enums.TipoProductoEnum.ACOND_SECUNDARIO
          )
          and (l.estado is null or l.estado <> com.mb.conitrack.enums.EstadoEnum.DEVUELTO)
          and exists (
              select 1 from Bulto b
              where b.lote = l and b.cantidadActual > 0
          )
        order by case when l.fechaIngreso is null then 1 else 0 end,
                 l.fechaIngreso asc, l.codigoLote asc
    """)
    List<Lote> findAllForDevolucionCompra();

    @Query("""
        select l
        from Lote l
        where l.activo = true
          and l.dictamen = com.mb.conitrack.enums.DictamenEnum.APROBADO
          and l.estado in (
              com.mb.conitrack.enums.EstadoEnum.NUEVO,
              com.mb.conitrack.enums.EstadoEnum.DISPONIBLE,
              com.mb.conitrack.enums.EstadoEnum.EN_USO
          )
          and not exists (
              select 1
              from Analisis a
              where a.lote = l
                and a.dictamen is null
                and a.fechaRealizado is null
          )
        order by l.fechaIngreso asc, l.codigoLote asc
    """)
    List<Lote> findAllForReanalisisLote();

    @Query("""
        select l
        from Lote l
        where l.activo = true
          and l.dictamen = com.mb.conitrack.enums.DictamenEnum.CUARENTENA
          and exists (
              select 1 from Analisis a
              where a.lote = l
                and a.dictamen is null
                and a.fechaRealizado is null
          )
          and exists (
              select 1 from Bulto b
              where b.lote = l
                and b.cantidadActual > 0
          )
    """)
    List<Lote> findAllForResultadoAnalisis();

    @Query("""
        select l
        from Lote l
        where l.activo = true
          and l.dictamen = com.mb.conitrack.enums.DictamenEnum.APROBADO
          and (l.producto.tipoProducto is null
               or l.producto.tipoProducto <> com.mb.conitrack.enums.TipoProductoEnum.UNIDAD_VENTA)
          and exists (
              select 1 from Bulto b
              where b.lote = l
                and b.cantidadActual > 0
          )
        order by l.fechaIngreso asc, l.codigoLote asc
    """)
    List<Lote> findAllForConsumoProduccion();

    @EntityGraph(attributePaths = "analisisList") // evita N+1 al calcular la fecha vigente
    @Query("""
        select l
        from Lote l
        where exists (
            select 1 from Bulto b
            where b.lote = l and b.cantidadActual > 0
        )
        order by l.fechaIngreso asc, l.codigoLote asc
    """)
    List<Lote> findLotesConStockOrder();

    @Query("""
          select distinct l
          from Lote l
          left join fetch l.analisisList aFetch
          where exists (
            select 1 from Bulto b
            where b.lote = l and b.cantidadActual > 0
          )
          and (
               l.fechaVencimientoProveedor is not null
            or l.fechaReanalisisProveedor  is not null
            or exists (
                select 1
                from Analisis a
                where a.lote = l
                  and a.activo = true
                  and a.dictamen is not null
                  and (a.fechaVencimiento is not null or a.fechaReanalisis is not null)
            )
          )
          order by l.fechaIngreso asc, l.codigoLote asc
        """)
    List<Lote> findLotesDictaminadosConStock();

    @Query("""
        select l
        from Lote l
        where l.activo = true
          and l.dictamen = com.mb.conitrack.enums.DictamenEnum.APROBADO
          and l.producto.tipoProducto = com.mb.conitrack.enums.TipoProductoEnum.UNIDAD_VENTA
          and exists (
              select 1 from Bulto b
              where b.lote = l and b.cantidadActual > 0
          )
        order by l.fechaIngreso asc, l.codigoLote asc
    """)
    List<Lote> findAllForLiberacionProducto();

    @EntityGraph(attributePaths = {"producto"}) // opcional; suma "bultos" si luego los usás
    @Query("""
        select l
        from Lote l
        where l.activo = true
          and l.dictamen = com.mb.conitrack.enums.DictamenEnum.LIBERADO
          and l.producto.tipoProducto = com.mb.conitrack.enums.TipoProductoEnum.UNIDAD_VENTA
          and exists (
              select 1 from Bulto b
              where b.lote = l and b.cantidadActual > 0
          )
        order by l.fechaIngreso asc, l.codigoLote asc
    """)
    List<Lote> findAllForVentaProducto();


    @Query("""
        select l
        from Lote l
        where l.activo = true
          and exists (
              select 1
              from Traza t
              where t.lote = l
                and t.estado = com.mb.conitrack.enums.EstadoEnum.VENDIDO
          )
        order by l.fechaIngreso asc, l.codigoLote asc
    """)
    List<Lote> findAllForDevolucionOrRecall();


    @Query("""
        select distinct b
        from Bulto b
        join b.lote l
        left join fetch b.detalles d
        left join fetch d.movimiento m
        where l.codigoLote = :codigoLote
          and l.activo = true
          and l.dictamen <> com.mb.conitrack.enums.DictamenEnum.RECIBIDO
          and exists (
              select 1 from Analisis a
              where a.lote = l
                and a.nroAnalisis is not null
          )
          and b.activo = true
          and b.cantidadActual is not null
          and b.cantidadActual > 0
        order by b.nroBulto asc
    """)
    List<Bulto> findBultosForMuestreoByCodigoLote(@Param("codigoLote") String codigoLote);
}
