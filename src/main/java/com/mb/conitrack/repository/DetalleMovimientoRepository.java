package com.mb.conitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.DetalleMovimiento;

public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Long> {

    List<DetalleMovimiento> findByMovimientoId(Long movimientoId);

    List<DetalleMovimiento> findByBultoId(Long bultoId);

}

