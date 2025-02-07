package com.mb.securo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.Movimiento;
import com.mb.securo.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
