package com.mb.conitrack.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.repository.MovimientoRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class MovimientoService {

    @Autowired
    private final MovimientoRepository movimientoRepository;

    public List<Movimiento> findAllOrderByFechaAsc() {
        return movimientoRepository.findByActivoTrueOrderByFechaAsc();
    }

    public List<Movimiento> findAllOrderByFechaAscNullsLast() {
        return movimientoRepository.findMuestreosActivosOrderByFechaAscNullsLast();
    }

    public Optional<Movimiento> findMovimientoByCodigoMovimiento(final String codigoMovimiento) {
        return movimientoRepository.findByCodigoMovimientoAndActivoTrue(codigoMovimiento);
    }


    public List<Movimiento> findMovimientoByCodigoLote(final String codigoLote) {
        return movimientoRepository.findByLote_CodigoLoteAndActivoTrue(codigoLote);
    }

    public boolean existeMuestreo( final MovimientoDTO movimientoDTO) {
        return movimientoRepository.existeMuestreo(movimientoDTO.getCodigoLote(), movimientoDTO.getNroAnalisis());
    }



}
