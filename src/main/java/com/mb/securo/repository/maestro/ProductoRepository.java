package com.mb.securo.repository.maestro;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.maestro.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
