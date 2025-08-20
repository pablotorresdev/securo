package com.mb.conitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findAllByActivoTrue();

    Optional<Movimiento> findByCodigoInternoAndActivoTrue(String codigoInternoMov);

}
