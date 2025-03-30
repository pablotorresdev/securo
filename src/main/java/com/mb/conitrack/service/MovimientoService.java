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
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.repository.MovimientoRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

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
            .filter(movimiento -> MotivoEnum.MUESTREO.equals(movimiento.getMotivo()))
            .sorted(Comparator.comparing(Movimiento::getFecha))
            .toList();
    }

    @Transactional
    public void persistirMuestreo(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoMuestreo(dto, lote);
        lote.getMovimientos().add(movimiento);

        Analisis analisis = createAnalisis(dto, lote);
        lote.getAnalisis().add(analisis);

        lote.setCantidadActual(calcularCantidadActual(dto, lote));

        analisisService.save(analisis);
        loteService.save(lote);
        movimientoRepository.save(movimiento);
    }

    @Transactional
    public void persistirCmbioDictamenMuestreo(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoCambioDictamen(dto, lote);
        movimiento.setMotivo(MotivoEnum.MUESTREO);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(DictamenEnum.CUARENTENA);
        lote.setDictamen(DictamenEnum.CUARENTENA);
        lote.getMovimientos().add(movimiento);

        loteService.save(lote);
        movimientoRepository.save(movimiento);
    }

    private static Movimiento createMovimientoCambioDictamen(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
        movimiento.setLote(lote);
        movimiento.setDescripcion(dto.getObservaciones());
        movimiento.setActivo(true);
        return movimiento;
    }

    private static Movimiento createMovimientoMuestreo(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MotivoEnum.MUESTREO);
        movimiento.setLote(lote);
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setUnidadMedida(dto.getUnidadMedida());
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setDescripcion(dto.getObservaciones());
        movimiento.setActivo(true);
        return movimiento;
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

    private static Analisis createAnalisis(final MovimientoDTO dto, final Lote lote) {
        final String nroAnalisis;
        if(dto.getNroReAnalisis() != null) {
            nroAnalisis =  dto.getNroReAnalisis();
        } else {
            nroAnalisis = dto.getNroAnalisis();
        }
        if(nroAnalisis == null) {
            throw new IllegalArgumentException("El número de análisis es requerido");
        }
        Analisis analisis = new Analisis();
        analisis.setLote(lote);
        analisis.setFechaAnalisis(dto.getFechaAnalisis());
        analisis.setNroAnalisis(nroAnalisis);
        analisis.setObservaciones(dto.getObservaciones());
        analisis.setActivo(true);
        return analisis;
    }



}
