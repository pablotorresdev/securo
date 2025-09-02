package com.mb.conitrack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.repository.MovimientoRepository;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.dto.DTOUtils.fromMovimientoEntities;
import static com.mb.conitrack.dto.DTOUtils.fromMovimientoEntity;

@AllArgsConstructor
@Service
public class MovimientoService {

    @Autowired
    private final MovimientoRepository movimientoRepository;

    @Transactional(readOnly = true)
    public List<MovimientoDTO> findAllMovimientos() {
        return fromMovimientoEntities(movimientoRepository.findAllByActivoTrue());
    }
    @Transactional(readOnly = true)
    public List<MovimientoDTO> findAllMovimientosAudit() {
        return fromMovimientoEntities(movimientoRepository.findAllAudit());
    }

    @Transactional(readOnly = true)
    public List<MovimientoDTO> findByCodigoLote(final String codigoLote) {
        return fromMovimientoEntities(movimientoRepository.findAllByLoteCodigoLoteOrderByFechaAsc(codigoLote));
    }

    //***********CU13 MODIF: DEVOLUCION VENTA***********    @Transactional(readOnly = true)
    public List<MovimientoDTO> getMovimientosVentaByCodigolote(final String codigoLote) {
        return fromMovimientoEntities(movimientoRepository.findVentasConTrazasVendidasByCodigoLote(codigoLote));
    }

    public MovimientoDTO getUltimoMovimientosCodigolote(final String codigoLote) {
        return fromMovimientoEntity(movimientoRepository.findLatestByCodigoLote(codigoLote).get(0));
    }

}
