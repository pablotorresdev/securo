package com.mb.conitrack.repository.maestro;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.maestro.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
