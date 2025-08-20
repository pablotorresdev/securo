package com.mb.conitrack.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.repository.MovimientoRepository;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.enums.MotivoEnum.MUESTREO;

@AllArgsConstructor
@Service
public class QueryServiceMovimiento {

    @Autowired
    private final MovimientoRepository movimientoRepository;

    public List<Movimiento> findAll() {
        final List<Movimiento> movimientos = movimientoRepository.findAllByActivoTrue();
        movimientos.sort(Comparator.comparing(Movimiento::getFecha));
        return movimientos;
    }

    public List<Movimiento> findAllMuestreos() {
        return movimientoRepository.findAllByActivoTrue()
            .stream()
            .filter(movimiento -> MUESTREO.equals(movimiento.getMotivo()))
            .sorted(Comparator.comparing(Movimiento::getFecha))
            .toList();
    }

    public Optional<Movimiento> findMovimientoByCodigoInterno(final String codigoInternoMov) {
        if (codigoInternoMov == null) {
            return null;
        }
        return movimientoRepository.findByCodigoInternoAndActivoTrue(codigoInternoMov);
    }



}
