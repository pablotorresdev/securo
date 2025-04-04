package com.mb.conitrack.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.repository.MovimientoRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import static com.mb.conitrack.enums.DictamenEnum.CUARENTENA;
import static com.mb.conitrack.enums.MotivoEnum.ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.DEVOLUCION_COMPRA;
import static com.mb.conitrack.enums.MotivoEnum.MUESTREO;

@AllArgsConstructor
@Service
public class MovimientoService {

    @Autowired
    private final AnalisisService analisisService;

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

    public Movimiento save(final Movimiento movimiento) {
        return movimientoRepository.save(movimiento);
    }

    //***********CU1 ALTA: COMPRA***********
    @Transactional
    public Movimiento persistirMovimientoAltaIngresoCompra(Lote lote) {
        final Movimiento movimientoAltaIngresoCompra = createMovimientoAltaIngresoCompra(lote);
        movimientoAltaIngresoCompra.setLote(lote);
        return movimientoRepository.save(movimientoAltaIngresoCompra);
    }

    private Movimiento createMovimientoAltaIngresoCompra(final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(LocalDate.now());
        movimiento.setFechaYHoraCreacion(lote.getFechaYHoraCreacion());
        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setMotivo(MotivoEnum.COMPRA);
        movimiento.setCantidad(lote.getCantidadInicial());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setObservaciones("Ingreso de stock por compra (CU1)");
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setActivo(Boolean.TRUE);
        movimiento.setLote(lote);
        return movimiento;
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    @Transactional
    public Movimiento persistirCambioDictamenCuarentena(final MovimientoDTO dto, Lote lote) {
        final Analisis analisis = DTOUtils.createAnalisis(dto);
        final Analisis newAnalisis = analisisService.save(analisis);
        final Movimiento movimientoPorMuestreo = createMovimientoPorMuestreo(dto, lote);
        movimientoPorMuestreo.setNroAnalisis(newAnalisis.getNroAnalisis());
        return movimientoRepository.save(movimientoPorMuestreo);
    }

    //***********CU3 BAJA: MUESTREO***********
    private static Movimiento createMovimientoPorMuestreo(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MUESTREO);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setLote(lote);
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setUnidadMedida(dto.getUnidadMedida());
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setObservaciones(dto.getObservaciones());
        movimiento.setActivo(true);
        return movimiento;
    }

    @Transactional
    public Movimiento persistirMovimientoMuestreo(final MovimientoDTO dto, Lote lote) {

        final List<Analisis> analisisList = lote.getAnalisisList();
        if (analisisList.isEmpty()) {
            final Analisis analisis = DTOUtils.createAnalisis(dto);
            final Analisis newAnalisis = analisisService.save(analisis);
            final Movimiento movimientoPorMuestreo = createMovimientoPorMuestreo(dto, lote);
            movimientoPorMuestreo.setNroAnalisis(newAnalisis.getNroAnalisis());
            return movimientoRepository.save(movimientoPorMuestreo);
        } else {
            final Optional<Analisis> analisisEnCurso = getAnalisisEnCurso(analisisList);
            if (analisisEnCurso.isPresent()) {
                final Analisis analisis = analisisEnCurso.get();
                if(dto.getNroAnalisis().equals(analisis.getNroAnalisis())) {
                    final Movimiento movimientoPorMuestreo = createMovimientoPorMuestreo(dto, lote);
                    movimientoPorMuestreo.setNroAnalisis(analisis.getNroAnalisis());
                    return movimientoRepository.save(movimientoPorMuestreo);
                } else {
                    throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
                }
            } else {
                throw new IllegalArgumentException("El lote tiene más de un análisis");
            }
        }
    }

    private Optional<Analisis> getAnalisisEnCurso(final List<Analisis> analisisList) {
        List<Analisis> enCurso = analisisList.stream()
            .filter(analisis -> analisis.getDictamen() == null)
            .filter(analisis -> analisis.getFechaAnalisis() == null)
            .toList();
        if (enCurso.isEmpty()) {
            return Optional.empty();
        } else if (enCurso.size() == 1) {
            return Optional.of(enCurso.get(0));
        } else {
            throw new IllegalArgumentException("El lote tiene más de un análisis en curso");
        }
    }

    @Transactional
    public Movimiento persistirMovimientoCuarentenaPorMuestreo(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoCambioDictamen(dto, lote);

        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setMotivo(MUESTREO);
        movimiento.setDictamenFinal(CUARENTENA);

        return movimientoRepository.save(movimiento);
    }

    private static Movimiento createMovimientoCambioDictamen(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setLote(lote);
        movimiento.setObservaciones(dto.getObservaciones());
        movimiento.setActivo(true);
        return movimiento;
    }

    @Transactional
    public Movimiento persistirMovimientoCuarentenaPorAnalisis(final MovimientoDTO dto, Lote lote, String nroAnalisis) {
        Movimiento movimiento = createMovimientoCambioDictamen(dto, lote);

        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setNroAnalisis(nroAnalisis);
        movimiento.setMotivo(ANALISIS);
        movimiento.setDictamenFinal(CUARENTENA);

        return movimientoRepository.save(movimiento);
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional
    public Movimiento persistirMovimientoDevolucionCompra(final MovimientoDTO dto, Lote lote) {
        return movimientoRepository.save(createMovimientoDevolucionCompra(dto, lote));
    }

    private static Movimiento createMovimientoDevolucionCompra(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(DEVOLUCION_COMPRA);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setCantidad(lote.getCantidadActual());
        movimiento.setUnidadMedida(lote.getUnidadMedida());

        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setLote(lote);
        movimiento.setObservaciones(dto.getObservaciones());
        movimiento.setActivo(true);
        return movimiento;
    }

    public Movimiento persistirMovimientoResultadoAnalisis(final MovimientoDTO dto, final Lote loteBulto) {
        return movimientoRepository.save(createMovimientoResultadoAnalisis(dto, loteBulto));
    }

    private static Movimiento createMovimientoResultadoAnalisis(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = createMovimientoCambioDictamen(dto, lote);
        movimiento.setMotivo(ANALISIS);
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(dto.getDictamenFinal());
        return movimiento;
    }
}
