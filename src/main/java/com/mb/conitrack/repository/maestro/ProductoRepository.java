package com.mb.conitrack.repository.maestro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

}
