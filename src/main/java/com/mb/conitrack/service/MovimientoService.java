package com.mb.conitrack.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Movimiento;
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

    //***********CU23 MODIF: DEVOLUCION VENTA***********    @Transactional(readOnly = true)
    public List<MovimientoDTO> getMovimientosVentaByCodigolote(final String codigoLote) {
        return fromMovimientoEntities(movimientoRepository.findMovimientosVentaByCodigoLote(codigoLote));
    }

    public MovimientoDTO getUltimoMovimientosCodigolote(final String codigoLote) {
        return fromMovimientoEntity(movimientoRepository.findLatestByCodigoLote(codigoLote).get(0));
    }

    public List<Integer> calcularMaximoDevolucionPorBulto(final String codigoMovimiento) {

        final Optional<Movimiento> byCodigoMovimientoAndActivoTrue = movimientoRepository.findByCodigoMovimientoAndActivoTrue(
            codigoMovimiento);

        if(byCodigoMovimientoAndActivoTrue.isEmpty()) {
            return new ArrayList<>();
        }

        final Movimiento movVenta = byCodigoMovimientoAndActivoTrue.get();



        final ArrayList<Integer> integers = new ArrayList<>();
        integers.add(0);
        integers.add(1);
        integers.add(1);
        return integers;
    }

}
