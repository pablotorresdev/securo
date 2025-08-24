package com.mb.conitrack.repository.maestro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mb.conitrack.entity.maestro.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();


    @Query("""
        select p
        from Producto p
        where p.activo = true
          and p.tipoProducto is not null
          and p.tipoProducto in (
              com.mb.conitrack.enums.TipoProductoEnum.API,
              com.mb.conitrack.enums.TipoProductoEnum.EXCIPIENTE,
              com.mb.conitrack.enums.TipoProductoEnum.ACOND_PRIMARIO,
              com.mb.conitrack.enums.TipoProductoEnum.ACOND_SECUNDARIO
          )
        order by p.codigoProducto asc
    """)
    List<Producto> findProductosExternos();


    // Internos = todos los "SemiElab" + UNIDAD_VENTA
    @Query("""
        select p
        from Producto p
        where p.activo = true
          and p.tipoProducto in (
              com.mb.conitrack.enums.TipoProductoEnum.SEMIELABORADO,
              com.mb.conitrack.enums.TipoProductoEnum.GRANEL_MEZCLA_POLVO,
              com.mb.conitrack.enums.TipoProductoEnum.GRANEL_CAPSULAS,
              com.mb.conitrack.enums.TipoProductoEnum.GRANEL_COMPRIMIDOS,
              com.mb.conitrack.enums.TipoProductoEnum.GRANEL_FRASCOS,
              com.mb.conitrack.enums.TipoProductoEnum.UNIDAD_VENTA
          )
        order by p.codigoProducto asc
    """)
    List<Producto> findProductosInternos();

    List<Producto> findByActivoTrueOrderByCodigoProductoAsc();

}
