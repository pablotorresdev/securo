package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.UnidadMedidaEnum;
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

    @Transactional(readOnly = true)
    public List<Integer> calcularMaximoDevolucionPorBulto(final String codigoMovimiento) {

        return calcularMaximoRetornoPorBulto(codigoMovimiento);
    }

    @Transactional(readOnly = true)
    public List<Integer> calcularMaximoRecallPorBulto(final String codigoMovimiento) {
        return calcularMaximoRetornoPorBulto(codigoMovimiento);
    }

    private ArrayList<Integer> calcularMaximoRetornoPorBulto(final String codigoMovimiento) {
        final Optional<Movimiento> movVentaMaybe = movimientoRepository.findMovimientosVentaByCodigoMovimiento(
            codigoMovimiento);

        if (movVentaMaybe.isEmpty()) {
            return new ArrayList<>();
        }
        final Movimiento movVenta = movVentaMaybe.get();

        final List<Movimiento> movimientosDevolucion = movimientoRepository.findByMovimientoOrigen(
            movVenta.getCodigoMovimiento());

        Map<Integer, BigDecimal> devolucionesPorBulto = new HashMap<>();
        final Set<DetalleMovimiento> detallesVenta = movVenta.getDetalles();
        for (DetalleMovimiento detalleVenta : detallesVenta) {
            if (UnidadMedidaEnum.UNIDAD != detalleVenta.getUnidadMedida()) {
                throw new IllegalStateException("La unidad de medida del movimiento de venta debe ser UNIDAD.");
            }
            devolucionesPorBulto.put(detalleVenta.getBulto().getNroBulto(), detalleVenta.getCantidad());
        }

        for (Movimiento movDevolucion : movimientosDevolucion) {
            for (DetalleMovimiento detalleDevolucion : movDevolucion.getDetalles()) {
                if (UnidadMedidaEnum.UNIDAD != detalleDevolucion.getUnidadMedida()) {
                    throw new IllegalStateException("La unidad de medida del movimiento de devolucion debe ser UNIDAD.");
                }

                if (BigDecimal.ZERO.compareTo(detalleDevolucion.getCantidad()) == 0) {
                    continue;
                }

                final Integer bultoDevolucion = detalleDevolucion.getBulto().getNroBulto();
                final BigDecimal saldoCantidad = devolucionesPorBulto.get(bultoDevolucion)
                    .subtract(detalleDevolucion.getCantidad());
                devolucionesPorBulto.put(bultoDevolucion, saldoCantidad);
            }
        }

        final ArrayList<Integer> integers = new ArrayList<>();
        Integer maxKey = Collections.max(devolucionesPorBulto.keySet());
        for (int i = 1; i <= maxKey; i++) {
            if (devolucionesPorBulto.containsKey(i)) {
                final int unidadesSaldoBulto = devolucionesPorBulto.get(i).intValue();
                if (unidadesSaldoBulto < 0) {
                    throw new IllegalStateException("La cantidad de devuelta no puede ser negativa.");
                }
                integers.add(unidadesSaldoBulto);
            } else {
                integers.add(0);
            }
        }

        return integers;
    }

}
