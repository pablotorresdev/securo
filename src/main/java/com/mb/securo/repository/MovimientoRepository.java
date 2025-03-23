package com.mb.securo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

}
