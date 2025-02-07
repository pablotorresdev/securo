package com.mb.securo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.EspecificacionProducto;
import com.mb.securo.entity.Estado;

public interface EstadoRepository extends JpaRepository<Estado, Long> {

}
