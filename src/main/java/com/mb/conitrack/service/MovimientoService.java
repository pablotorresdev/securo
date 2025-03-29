package com.mb.conitrack.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.repository.MovimientoRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;

    public List<Movimiento> findAll() {
        final List<Movimiento> movimientos = movimientoRepository.findAll();
        movimientos.sort(Comparator
            .comparing(Movimiento::getFecha));
        return movimientos;
    }

    public void registrarMuestreo(final MovimientoDTO dto) {

    }

}
