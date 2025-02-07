package com.mb.securo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.TipoProducto;

public interface TipoProductoRepository extends JpaRepository<TipoProducto, Long> {

    Optional<TipoProducto> findByNombre(String testClase);

}
