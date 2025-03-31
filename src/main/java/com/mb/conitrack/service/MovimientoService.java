package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.repository.MovimientoRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import static com.mb.conitrack.enums.DictamenEnum.CUARENTENA;
import static com.mb.conitrack.enums.MotivoEnum.MUESTREO;

@AllArgsConstructor
@Service
public class MovimientoService {

    @Autowired
    private final AnalisisService analisisService;

    @Autowired
    private final LoteService loteService;

    @Autowired
    private final MovimientoRepository movimientoRepository;

    public List<Movimiento> findAll() {
        final List<Movimiento> movimientos = movimientoRepository.findAll();
        movimientos.sort(Comparator
            .comparing(Movimiento::getFecha));
        return movimientos;
    }

    public List<Movimiento> findAllMuestreos() {
        return movimientoRepository.findAll().stream()
            .filter(movimiento -> MUESTREO.equals(movimiento.getMotivo()))
            .sorted(Comparator.comparing(Movimiento::getFecha))
            .toList();
    }

    @Transactional
    public boolean persistirMuestreo(final MovimientoDTO dto, Lote lote) {
        try {
            Movimiento movimiento = movimientoRepository.save(createMovimientoPorMuestreo(dto, lote));
            final Analisis analisis = analisisService.save(createAnalisis(dto, lote));

            lote.setCantidadActual(calcularCantidadActual(dto, lote));

            lote.getMovimientos().add(movimiento);
            lote.getAnalisis().add(analisis);

            if (!conciliateLote(lote)) {
                return false;
            }

            loteService.save(lote);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Movimiento createMovimientoPorMuestreo(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MUESTREO);
        movimiento.setLote(lote);
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setUnidadMedida(dto.getUnidadMedida());
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setDescripcion(dto.getObservaciones());
        movimiento.setActivo(true);
        return movimiento;
    }

    private static Analisis createAnalisis(final MovimientoDTO dto, final Lote lote) {
        final String nroAnalisis = dto.getNroReAnalisis() != null ? dto.getNroReAnalisis() : dto.getNroAnalisis();
        if (nroAnalisis != null) {
            Analisis analisis = new Analisis();
            analisis.setLote(lote);
            analisis.setFechaAnalisis(dto.getFechaAnalisis());
            analisis.setNroAnalisis(nroAnalisis);
            analisis.setObservaciones(dto.getObservaciones());
            analisis.setActivo(true);
            return analisis;
        }
        throw new IllegalArgumentException("El número de análisis es requerido");
    }

    private static BigDecimal calcularCantidadActual(final MovimientoDTO dto, final Lote lote) {
        final BigDecimal cantidadLote = lote.getCantidadActual();
        final double factorLote = lote.getUnidadMedida().getFactorConversion();
        final double factorDto = dto.getUnidadMedida().getFactorConversion();

        // Convertimos la cantidad del DTO a la unidad del lote
        BigDecimal cantidadDtoConvertida = dto.getCantidad()
            .multiply(BigDecimal.valueOf(factorDto / factorLote));

        return cantidadLote.subtract(cantidadDtoConvertida);
    }

    private boolean conciliateLote(final Lote lote) {
        System.out.println(lote.toString());
        return true;
    }

    @Transactional
    public void persistirCambioDictamenPorMuestreo(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoCambioDictamen(dto, lote);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setMotivo(MUESTREO);
        movimiento.setDictamenFinal(CUARENTENA);
        final Movimiento nuevoMovimiento = movimientoRepository.save(movimiento);

        lote.setDictamen(CUARENTENA);
        lote.getMovimientos().add(nuevoMovimiento);
        loteService.save(lote);
    }

    private static Movimiento createMovimientoCambioDictamen(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
        movimiento.setLote(lote);
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setDescripcion(dto.getObservaciones());
        movimiento.setActivo(true);
        return movimiento;
    }

}
