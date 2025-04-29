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
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.repository.MovimientoRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import static com.mb.conitrack.entity.EntityUtils.createMovimientoAltaIngresoCompra;
import static com.mb.conitrack.entity.EntityUtils.createMovimientoAltaIngresoProduccion;
import static com.mb.conitrack.entity.EntityUtils.createMovimientoModificacion;
import static com.mb.conitrack.entity.EntityUtils.createMovimientoPorMuestreo;
import static com.mb.conitrack.entity.EntityUtils.getAnalisisEnCurso;
import static com.mb.conitrack.enums.DictamenEnum.ANALISIS_EXPIRADO;
import static com.mb.conitrack.enums.DictamenEnum.CUARENTENA;
import static com.mb.conitrack.enums.DictamenEnum.VENCIDO;
import static com.mb.conitrack.enums.MotivoEnum.ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.DEVOLUCION_COMPRA;
import static com.mb.conitrack.enums.MotivoEnum.EXPIRACION_ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.MUESTREO;
import static com.mb.conitrack.enums.MotivoEnum.VENCIMIENTO;

@AllArgsConstructor
@Service
public class MovimientoService {

    @Autowired
    private final AnalisisService analisisService;

    @Autowired
    private final MovimientoRepository movimientoRepository;

    private static Movimiento createMovimientoConAnalisis(final MovimientoDTO dto, final Lote lote, final Analisis ultimoAnalisis) {
        final Movimiento movimientoPorMuestreo = createMovimientoPorMuestreo(dto);
        movimientoPorMuestreo.setLote(lote);
        movimientoPorMuestreo.setNroAnalisis(ultimoAnalisis.getNroAnalisis());
        return movimientoPorMuestreo;
    }

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
        Movimiento movimiento = createMovimientoModificacion(dto, lote);

        movimiento.setMotivo(ANALISIS);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(CUARENTENA);
        movimiento.setNroAnalisis(nroAnalisis);

        movimiento.setObservaciones("_CU2_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CUZ MODIFICACION: CUZ Reanalisis de Producto Aprobado***********
    @Transactional
    public Movimiento persistirMovimientoReanalisisProducto(final MovimientoDTO dto, Lote lote, String nroAnalisis) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);

        movimiento.setMotivo(ANALISIS);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setNroAnalisis(nroAnalisis);

        movimiento.setObservaciones("_CUZ_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU3 BAJA: MUESTREO***********
    @Transactional
    public Movimiento persistirMovimientoMuestreo(final MovimientoDTO dto, Lote lote) {
        final List<Analisis> analisisList = lote.getAnalisisList();
        if (analisisList.isEmpty()) {
            return crearMovimientoConPrimerAnalisis(dto, lote);
        } else {
            final Optional<Analisis> analisisEnCurso = getAnalisisEnCurso(analisisList);
            if (analisisEnCurso.isPresent()) {
                return crearMovimientoConAnalisisEnCurso(dto, lote, analisisEnCurso);
            } else {
                return crearMovmimientoConAnalisisDictaminado(dto, lote);
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

        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(DictamenEnum.RECHAZADO);

        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        movimiento.setObservaciones("_CU4_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    @Transactional
    public Movimiento persistirMovimientoResultadoAnalisis(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);

        movimiento.setMotivo(ANALISIS);
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(dto.getDictamenFinal());

        movimiento.setObservaciones("_CU5/6_\n" + dto.getObservaciones());
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
        movimiento.setLote(bulto);
        movimiento.setActivo(true);
        movimiento.setObservaciones("_CU7_\n" + loteDTO.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU10 ALTA: Produccion***********
    @Transactional
    public Movimiento persistirMovimientoAltaIngresoProduccion(Lote lote) {
        final Movimiento movimientoAltaIngresoProduccion = createMovimientoAltaIngresoProduccion(lote);
        movimientoAltaIngresoProduccion.setLote(lote);
        return movimientoRepository.save(movimientoAltaIngresoProduccion);
    }

    //***********CU9 MODIFICACION: VENCIDO***********
    @Transactional
    public Movimiento persistirMovimientoExpiracionAnalisis(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);

        movimiento.setMotivo(EXPIRACION_ANALISIS);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(ANALISIS_EXPIRADO);

        movimiento.setObservaciones("_CU8_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU9 MODIFICACION: VENCIDO***********
    @Transactional
    public Movimiento persistirMovimientoProductoVencido(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);

        movimiento.setMotivo(VENCIMIENTO);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(VENCIDO);

        //TODO: fix observaciones
        movimiento.setObservaciones("_CU9_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    private Movimiento crearMovimientoConAnalisisEnCurso(final MovimientoDTO dto, final Lote lote, final Optional<Analisis> analisisEnCurso) {
        //Si el lote tiene un analisis en curso, se guarda el movimiento y se asocia al analisis en curso
        //El lote puede tiene n analisis realizados siempre se asocia al analisis en curso
        if (dto.getNroAnalisis().equals(analisisEnCurso.orElseThrow(() -> new IllegalArgumentException("El número de análisis esta vacio")).getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoConAnalisis(dto, lote, analisisEnCurso.get()));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

    private Movimiento crearMovimientoConPrimerAnalisis(final MovimientoDTO dto, final Lote lote) {
        //Si el lote no tiene analisis realizado (Recibido), se crea uno nuevo y se guarda el movimiento
        final Analisis newAnalisis = analisisService.save(DTOUtils.createAnalisis(dto));
        return movimientoRepository.save(createMovimientoConAnalisis(dto, lote, newAnalisis));
    }

    private Movimiento crearMovmimientoConAnalisisDictaminado(final MovimientoDTO dto, final Lote lote) {
        //Si el lote tiene n analisis realizados, se guarda el movimiento y se asocia al ultimo analisis realizado
        Analisis ultimoAnalisis = lote.getUltimoAnalisis();
        if (dto.getNroAnalisis().equals(ultimoAnalisis.getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoConAnalisis(dto, lote, ultimoAnalisis));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

}
