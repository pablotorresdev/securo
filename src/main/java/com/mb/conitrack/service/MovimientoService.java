package com.mb.conitrack.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.repository.MovimientoRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import static com.mb.conitrack.entity.EntityUtils.createMovimientoAltaIngresoCompra;
import static com.mb.conitrack.entity.EntityUtils.createMovimientoCambioDictamen;
import static com.mb.conitrack.entity.EntityUtils.createMovimientoPorMuestreo;
import static com.mb.conitrack.entity.EntityUtils.getAnalisisEnCurso;
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

    //***********CU2 MODIFICACION: CUARENTENA***********
    @Transactional
    public Movimiento persistirMovimientoCuarentenaPorAnalisis(final MovimientoDTO dto, Lote lote, String nroAnalisis) {
        Movimiento movimiento = createMovimientoCambioDictamen(dto, lote);

        movimiento.setMotivo(ANALISIS);
        movimiento.setDictamenFinal(CUARENTENA);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setNroAnalisis(nroAnalisis);

        movimiento.setObservaciones(lote.getObservaciones() + "\nMovimiento Cuarentena Analisis (CU2):\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU3 BAJA: MUESTREO***********
    @Transactional
    public Movimiento persistirMovimientoMuestreo(final MovimientoDTO dto, Lote lote) {
        final List<Analisis> analisisList = lote.getAnalisisList();
        //Si el lote no tiene analisis realizado (Recibido), se crea uno nuevo y se guarda el movimiento
        if (analisisList.isEmpty()) {
            final Analisis analisis = DTOUtils.createAnalisis(dto);
            final Analisis newAnalisis = analisisService.save(analisis);
            final Movimiento movimientoPorMuestreo = createMovimientoPorMuestreo(dto, lote);
            movimientoPorMuestreo.setNroAnalisis(newAnalisis.getNroAnalisis());
            return movimientoRepository.save(movimientoPorMuestreo);
        } else {
            final Optional<Analisis> analisisEnCurso = getAnalisisEnCurso(analisisList);
            //Si el lote tiene un analisis en curso, se guarda el movimiento y se asocia al analisis en curso
            //El lote puede tiene n analisis realizados siempre se asocia al analisis en curso
            if (analisisEnCurso.isPresent()) {
                final Analisis analisis = analisisEnCurso.get();
                if (dto.getNroAnalisis().equals(analisis.getNroAnalisis())) {
                    final Movimiento movimientoPorMuestreo = createMovimientoPorMuestreo(dto, lote);
                    movimientoPorMuestreo.setNroAnalisis(analisis.getNroAnalisis());
                    return movimientoRepository.save(movimientoPorMuestreo);
                } else {
                    throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
                }
            } else {
                //Si el lote tiene n analisis realizados, se guarda el movimiento y se asocia al ultimo analisis realizado
                Analisis ultimoAnalisis = lote.getCurrentAnalisis();
                if (dto.getNroAnalisis().equals(ultimoAnalisis.getNroAnalisis())) {
                    final Movimiento movimientoPorMuestreo = createMovimientoPorMuestreo(dto, lote);
                    movimientoPorMuestreo.setNroAnalisis(ultimoAnalisis.getNroAnalisis());
                    return movimientoRepository.save(movimientoPorMuestreo);
                } else {
                    throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
                }
            }
        }
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional
    public Movimiento persistirMovimientoDevolucionCompra(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(DEVOLUCION_COMPRA);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setCantidad(lote.getCantidadActual());
        movimiento.setUnidadMedida(lote.getUnidadMedida());

        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        movimiento.setObservaciones(lote.getObservaciones() + "\nMovimiento Devolucion Compra (CU4):\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    @Transactional
    public Movimiento persistirMovimientoResultadoAnalisis(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = createMovimientoCambioDictamen(dto, lote);

        movimiento.setMotivo(ANALISIS);
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(dto.getDictamenFinal());

        movimiento.setObservaciones(lote.getObservaciones() + "\nMovimiento Resultado de Analisis (CU5/6):\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU7: CONSUMO PRODUCCION***********
    @Transactional
    public Movimiento persistirMovimientoConsumoProduccion(final LoteDTO loteDTO, final Lote bulto) {
        final Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MotivoEnum.CONSUMO_PRODUCCION);

        final int i = loteDTO.getNroBultoList().indexOf(bulto.getNroBulto());
        movimiento.setCantidad(loteDTO.getCantidadesBultos().get(i));
        movimiento.setUnidadMedida(loteDTO.getUnidadMedidaBultos().get(i));

        movimiento.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
        movimiento.setOrdenProduccion(loteDTO.getOrdenProduccion());
        movimiento.setFecha(loteDTO.getFechaEgreso());
        movimiento.setObservaciones(loteDTO.getObservaciones());
        movimiento.setLote(bulto);
        movimiento.setActivo(true);
        movimiento.setObservaciones(bulto.getObservaciones() + "\nMovimiento Consumo produccion (CU7):\n" + loteDTO.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

}
