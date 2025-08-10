package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.DetalleMovimientoRepository;
import com.mb.conitrack.repository.MovimientoRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import static com.mb.conitrack.enums.DictamenEnum.ANALISIS_EXPIRADO;
import static com.mb.conitrack.enums.DictamenEnum.CUARENTENA;
import static com.mb.conitrack.enums.DictamenEnum.LIBERADO;
import static com.mb.conitrack.enums.DictamenEnum.VENCIDO;
import static com.mb.conitrack.enums.MotivoEnum.ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.DEVOLUCION_COMPRA;
import static com.mb.conitrack.enums.MotivoEnum.EXPIRACION_ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.LIBERACION;
import static com.mb.conitrack.enums.MotivoEnum.MUESTREO;
import static com.mb.conitrack.enums.MotivoEnum.VENCIMIENTO;
import static com.mb.conitrack.enums.MotivoEnum.VENTA;
import static com.mb.conitrack.utils.EntityUtils.createMovimientoAltaDevolucionVenta;
import static com.mb.conitrack.utils.EntityUtils.createMovimientoAltaIngresoCompra;
import static com.mb.conitrack.utils.EntityUtils.createMovimientoAltaIngresoProduccion;
import static com.mb.conitrack.utils.EntityUtils.createMovimientoModificacion;
import static com.mb.conitrack.utils.EntityUtils.createMovimientoPorMuestreo;
import static com.mb.conitrack.utils.EntityUtils.getAnalisisEnCurso;

@AllArgsConstructor
@Service
public class MovimientoService {

    @Autowired
    private final TrazaService trazaService;

    @Autowired
    private final AnalisisService analisisService;

    @Autowired
    private final MovimientoRepository movimientoRepository;

    @Autowired
    private DetalleMovimientoRepository detalleMovimientoRepository;

    Movimiento createMovimientoConAnalisis(
        final MovimientoDTO dto,
        final Bulto bulto,
        final Analisis ultimoAnalisis) {
        final Movimiento movimiento = createMovimientoPorMuestreo(dto);
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(bulto.getLote().getCodigoInterno() + "-B_" + bulto.getNroBulto() + "-" + timestampLoteDTO);
        movimiento.setLote(bulto.getLote());
        movimiento.getBultos().add(bulto);
        movimiento.setNroAnalisis(ultimoAnalisis.getNroAnalisis());
        return movimiento;
    }

     Movimiento createMovimientoConAnalisis(
        final MovimientoDTO dto,
        final Lote lote,
        final Analisis ultimoAnalisis) {
        final Movimiento movimiento = createMovimientoPorMuestreo(dto);
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(lote.getCodigoInterno() + "-B_" + "-" + timestampLoteDTO);
        movimiento.setLote(lote);
        movimiento.setNroAnalisis(ultimoAnalisis.getNroAnalisis());
        return movimiento;
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
    public Movimiento persistirMovimientoMuestreo(final MovimientoDTO dto, Bulto bulto) {
        final List<Analisis> analisisList = bulto.getLote().getAnalisisList();
        if (analisisList.isEmpty()) {
            return crearMovimientoConPrimerAnalisis(dto,  bulto);
        } else {
            final Optional<Analisis> analisisEnCurso = getAnalisisEnCurso(analisisList);
            if (analisisEnCurso.isPresent()) {
                return crearMovimientoConAnalisisEnCurso(dto,  bulto, analisisEnCurso);
            } else {
                return crearMovmimientoConAnalisisDictaminado(dto, bulto);
            }
        }
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional
    public Movimiento createMovimientoDevolucionCompra(final MovimientoDTO dto, Bulto bulto) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(DEVOLUCION_COMPRA);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(bulto.getLote().getCodigoInterno() + "-B_" + bulto.getNroBulto() + "-" + timestampLoteDTO);
        movimiento.setCantidad(bulto.getCantidadActual());
        movimiento.setUnidadMedida(bulto.getUnidadMedida());

        movimiento.setDictamenInicial(bulto.getLote().getDictamen());
        movimiento.setDictamenFinal(DictamenEnum.RECHAZADO);

        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.getBultos().add(bulto);
        movimiento.setActivo(true);

        movimiento.setObservaciones("_CU4_\n" + dto.getObservaciones());
        return movimiento;
    }

    @Transactional
    public Movimiento persistirMovimientoDevolucionCompra(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(DEVOLUCION_COMPRA);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(lote.getCodigoInterno() + "-B_" + lote.getNroBulto() + "-" + timestampLoteDTO);
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
    public Movimiento persistirMovimientoBajaConsumoProduccion(final LoteDTO loteDTO, final Lote bulto) {
        final Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MotivoEnum.CONSUMO_PRODUCCION);

        final int i = loteDTO.getNroBultoList().indexOf(bulto.getNroBulto());
        movimiento.setCantidad(loteDTO.getCantidadesBultos().get(i));
        movimiento.setUnidadMedida(loteDTO.getUnidadMedidaBultos().get(i));

        movimiento.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
        String timestampLoteDTO = loteDTO.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(bulto.getCodigoInterno() + "-B_" + bulto.getNroBulto() + "-" + timestampLoteDTO);
        movimiento.setOrdenProduccion(loteDTO.getOrdenProduccion());
        movimiento.setFecha(loteDTO.getFechaEgreso());
        movimiento.setLote(bulto);
        movimiento.setActivo(true);
        movimiento.setObservaciones("_CU7_\n" + loteDTO.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU9: VENTA PRODUCTO PROPIO***********
    @Transactional
    public Movimiento persistirMovimientoBajaVenta(final LoteDTO loteDTO, final Lote bulto) {
        final Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(VENTA);

        final int i = loteDTO.getNroBultoList().indexOf(bulto.getNroBulto());
        movimiento.setCantidad(loteDTO.getCantidadesBultos().get(i));
        movimiento.setUnidadMedida(loteDTO.getUnidadMedidaBultos().get(i));

        boolean unidadVenta = bulto.getProducto().getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA;

        if (!unidadVenta) {
            throw new IllegalStateException("La venta solo puede realizarse en producto terminado");
        }
        final BigDecimal cantidad = movimiento.getCantidad();
        if (movimiento.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
            throw new IllegalStateException("La traza solo es aplicable a UNIDADES");
        }

        if (cantidad.stripTrailingZeros().scale() > 0) {
            throw new IllegalStateException("La cantidad de Unidades debe ser entero");
        }

        final List<Traza> trazas = bulto.getFirstAvailableTrazaList(cantidad.intValue());

        for (Traza traza : trazas) {
            traza.setEstado(EstadoEnum.VENDIDO);
            traza.getMovimientos().add(movimiento);
        }
        trazaService.save(trazas);

        movimiento.setTrazas(new LinkedHashSet<>(trazas));

        movimiento.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
        String timestampLoteDTO = loteDTO.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(bulto.getCodigoInterno() + "-B_" + bulto.getNroBulto() + "-" + timestampLoteDTO);
        movimiento.setOrdenProduccion(loteDTO.getOrdenProduccion());
        movimiento.setFecha(loteDTO.getFechaEgreso());
        movimiento.setLote(bulto);

        movimiento.setActivo(true);
        movimiento.setObservaciones("_CU9_\n" + loteDTO.getObservaciones());
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

        movimiento.setObservaciones("_CU9_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU11 MODIFICACION: LIBERACION PRODUCTO***********
    @Transactional
    public Movimiento persistirMovimientoLiberacionProducto(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setMotivo(LIBERACION);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(LIBERADO);

        movimiento.setObservaciones("_CU11_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU13 ALTA: Devolucion***********
    @Transactional
    public Movimiento persistirMovimientoAltaDevolucionVenta(Lote lote) {
        final Movimiento movimientoAltaDevolucionVenta = createMovimientoAltaDevolucionVenta(lote);
        movimientoAltaDevolucionVenta.setLote(lote);
        return movimientoRepository.save(movimientoAltaDevolucionVenta);
    }

    @Transactional
    public Movimiento crearMovimientoConAnalisisEnCurso(
        final MovimientoDTO dto,
        final Bulto bulto,
        final Optional<Analisis> analisisEnCurso) {
        //Si el lote tiene un analisis en curso, se guarda el movimiento y se asocia al analisis en curso
        //El lote puede tiene n analisis realizados siempre se asocia al analisis en curso
        if (dto.getNroAnalisis()
            .equals(analisisEnCurso.orElseThrow(() -> new IllegalArgumentException("El número de análisis esta vacio"))
                .getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoConAnalisis(dto, bulto, analisisEnCurso.get()));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Transactional
    public Movimiento crearMovmimientoConAnalisisDictaminado(final MovimientoDTO dto, final Bulto bulto) {
        //Si el lote tiene n analisis realizados, se guarda el movimiento y se asocia al ultimo analisis realizado
        Analisis ultimoAnalisis = bulto.getLote().getUltimoAnalisis();
        if (dto.getNroAnalisis().equals(ultimoAnalisis.getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoConAnalisis(dto, bulto, ultimoAnalisis));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Transactional
    public Movimiento crearMovimientoConPrimerAnalisis(final MovimientoDTO dto, final Bulto bulto) {
        //Si el lote no tiene analisis realizado (Recibido), se crea uno nuevo y se guarda el movimiento
        final Analisis newAnalisis = analisisService.save(DTOUtils.createAnalisis(dto));
        return movimientoRepository.save(createMovimientoConAnalisis(dto, bulto, newAnalisis));
    }

}
