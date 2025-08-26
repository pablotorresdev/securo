package com.mb.conitrack.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.repository.MovimientoRepository;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.dto.DTOUtils.fromBultoEntities;
import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;
import static com.mb.conitrack.dto.DTOUtils.fromMovimientoEntities;
import static com.mb.conitrack.dto.DTOUtils.fromMovimientoEntity;

@AllArgsConstructor
@Service
public class MovimientoService {

    @Autowired
    private final MovimientoRepository movimientoRepository;

    @Transactional(readOnly = true)
    public List<MovimientoDTO> findAllMovimientosAudit() {
        return fromMovimientoEntities(movimientoRepository.findAllOrderByLoteFechaCreacion());
    }
    @Transactional(readOnly = true)
    public List<MovimientoDTO> findByCodigoLote(final String codigoLote) {
        return fromMovimientoEntities(movimientoRepository.findAllByLoteCodigoLoteOrderByFechaAsc(codigoLote));
    }




    //******************
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
