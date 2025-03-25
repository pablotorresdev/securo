package com.mb.conitrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

}
