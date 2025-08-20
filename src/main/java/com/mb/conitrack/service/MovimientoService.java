package com.mb.conitrack.service;

import java.math.BigDecimal;
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
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.utils.LoteEntityUtils;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import static com.mb.conitrack.enums.DictamenEnum.ANALISIS_EXPIRADO;
import static com.mb.conitrack.enums.DictamenEnum.CUARENTENA;
import static com.mb.conitrack.enums.DictamenEnum.LIBERADO;
import static com.mb.conitrack.enums.DictamenEnum.VENCIDO;
import static com.mb.conitrack.enums.MotivoEnum.ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.EXPIRACION_ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.LIBERACION;
import static com.mb.conitrack.enums.MotivoEnum.RESULTADO_ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.VENCIMIENTO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoProduccion;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoBajaProduccion;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoBajaVenta;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoMuestreoConAnalisis;
import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMayorUnidadMedida;

@AllArgsConstructor
@Service
public class MovimientoService {

    @Autowired
    private final TrazaService trazaService;

    @Autowired
    private final AnalisisService analisisService;

    @Autowired
    private final MovimientoRepository movimientoRepository;


    private static LoteEntityUtils loteUtils() {
        return LoteEntityUtils.getInstance();
    }

    public Movimiento save(final Movimiento movimiento) {
        return movimientoRepository.save(movimiento);
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    @Transactional
    public Movimiento persistirMovimientoCuarentenaPorAnalisis(final MovimientoDTO dto, Lote lote, String nroAnalisis) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaMovimiento());
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
        movimiento.setFecha(dto.getFechaMovimiento());

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
            return crearMovimientoMuestreoConPrimerAnalisis(dto, bulto);
        } else {
            final Optional<Analisis> analisisEnCurso = loteUtils().getAnalisisEnCurso(analisisList);
            if (analisisEnCurso.isPresent()) {
                return crearMovimientoMuestreoConAnalisisEnCurso(dto, bulto, analisisEnCurso);
            } else {
                return crearMovmimientoMuestreoConAnalisisDictaminado(dto, bulto);
            }
        }
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    @Transactional
    public Movimiento persistirMovimientoResultadoAnalisis(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaRealizadoAnalisis());

        movimiento.setMotivo(RESULTADO_ANALISIS);
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(dto.getDictamenFinal());

        movimiento.setObservaciones("_CU5/6_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU7: CONSUMO PRODUCCION***********
    @Transactional
    public Movimiento persistirMovimientoBajaConsumoProduccion(final LoteDTO loteDTO, final Lote loteEntity) {
        final Movimiento movimiento = createMovimientoBajaProduccion(loteDTO, loteEntity);

        UnidadMedidaEnum uniMedidaMovimiento = loteDTO.getUnidadMedidaBultos().get(0);

        for (int i = 1; i < loteDTO.getCantidadesBultos().size(); i++) {
            uniMedidaMovimiento = obtenerMayorUnidadMedida(uniMedidaMovimiento, loteDTO.getUnidadMedidaBultos().get(i));
        }

        BigDecimal cantidad = BigDecimal.ZERO;

        for (int i = 0; i < loteDTO.getCantidadesBultos().size(); i++) {
            final BigDecimal montoBulto = convertirCantidadEntreUnidades(
                loteDTO.getUnidadMedidaBultos().get(i),
                loteDTO.getCantidadesBultos().get(i),
                uniMedidaMovimiento);
            cantidad = cantidad.add(montoBulto);
        }

        movimiento.setCantidad(cantidad);
        movimiento.setUnidadMedida(uniMedidaMovimiento);

        for (int i = 0; i < loteDTO.getNroBultoList().size(); i++) {
            final Integer nroBulto = loteDTO.getNroBultoList().get(i);
            if (BigDecimal.ZERO.compareTo(loteDTO.getCantidadesBultos().get(i)) == 0) {
                continue;
            }
            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(loteEntity.getBultoByNro(nroBulto))
                .cantidad(loteDTO.getCantidadesBultos().get(i))
                .unidadMedida( loteDTO.getUnidadMedidaBultos().get(i))
                .build();

            movimiento.getDetalles().add(det);
        }

        return movimientoRepository.save(movimiento);
    }

    //***********CU9: VENTA PRODUCTO PROPIO***********
    @Transactional
    public Movimiento persistirMovimientoBajaVenta(final LoteDTO loteDTO, final Lote loteEntity) {
        final Movimiento movimiento = createMovimientoBajaVenta(loteDTO, loteEntity);

        BigDecimal cantidad = BigDecimal.ZERO;
        for (int i = 0; i < loteDTO.getCantidadesBultos().size(); i++) {
            cantidad = cantidad.add(loteDTO.getCantidadesBultos().get(i));
        }
        movimiento.setCantidad(cantidad);
        movimiento.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        for (int i = 0; i < loteDTO.getNroBultoList().size(); i++) {
            final Integer nroBulto = loteDTO.getNroBultoList().get(i);
            final BigDecimal cantBulto = loteDTO.getCantidadesBultos().get(i);

            if (BigDecimal.ZERO.compareTo(cantBulto) == 0) continue;

            final Bulto bulto = loteEntity.getBultoByNro(nroBulto);

            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(bulto)
                .cantidad(cantBulto)
                .unidadMedida(UnidadMedidaEnum.UNIDAD)
                .build();
            movimiento.getDetalles().add(det);

            final List<Traza> trazas = bulto.getFirstAvailableTrazaList(cantBulto.intValue());

            for (Traza tr : trazas) {
                tr.setEstado(EstadoEnum.VENDIDO);
            }
            trazaService.save(trazas);

            det.getTrazas().addAll(trazas);
        }

        return movimientoRepository.save(movimiento);
    }


    //***********CU10 ALTA: Produccion***********
    @Transactional
    public Movimiento persistirMovimientoAltaIngresoProduccion(Lote lote) {
        final Movimiento movimientoAltaIngresoProduccion = createMovimientoAltaIngresoProduccion(lote);
        return movimientoRepository.save(movimientoAltaIngresoProduccion);
    }

    //***********CU9 MODIFICACION: VENCIDO***********
    @Transactional
    public Movimiento persistirMovimientoExpiracionAnalisis(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaYHoraCreacion().toLocalDate());
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
        movimiento.setFecha(dto.getFechaYHoraCreacion().toLocalDate());
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

        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setMotivo(LIBERACION);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(LIBERADO);

        movimiento.setObservaciones("_CU11_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU13 ALTA: Devolucion***********
    @Transactional
    public Movimiento persistirMovimientoDevolucionVenta( MovimientoDTO dto, Lote lote) {
        final Movimiento movimientoDevolucionVenta = createMovimientoModificacion(dto, lote);
        movimientoDevolucionVenta.setFecha(dto.getFechaMovimiento());
        movimientoDevolucionVenta.setMotivo(MotivoEnum.DEVOLUCION_VENTA);

        final Movimiento movimientoOrigen = movimientoRepository.findByCodigoInternoAndActivoTrue(
            dto.getCodigoMovimientoOrigen()).orElseThrow(() -> new IllegalArgumentException("El movmiento de origen no existe."));
        movimientoDevolucionVenta.setMovimientoOrigen(movimientoOrigen);

        return movimientoRepository.save(movimientoDevolucionVenta);
    }

    @Transactional
    public Movimiento crearMovimientoMuestreoConAnalisisEnCurso(
        final MovimientoDTO dto,
        final Bulto bulto,
        final Optional<Analisis> analisisEnCurso) {
        //Si el lote tiene un analisis en curso, se guarda el movimiento y se asocia al analisis en curso
        //El lote puede tiene n analisis realizados siempre se asocia al analisis en curso
        if (dto.getNroAnalisis()
            .equals(analisisEnCurso.orElseThrow(() -> new IllegalArgumentException("El número de análisis esta vacio"))
                .getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoMuestreoConAnalisis(dto, bulto, analisisEnCurso.get()));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Transactional
    public Movimiento crearMovmimientoMuestreoConAnalisisDictaminado(final MovimientoDTO dto, final Bulto bulto) {
        //Si el lote tiene n analisis realizados, se guarda el movimiento y se asocia al ultimo analisis realizado
        Analisis ultimoAnalisis = bulto.getLote().getUltimoAnalisis();
        if (dto.getNroAnalisis().equals(ultimoAnalisis.getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoMuestreoConAnalisis(dto, bulto, ultimoAnalisis));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Transactional
    public Movimiento crearMovimientoMuestreoConPrimerAnalisis(final MovimientoDTO dto, final Bulto bulto) {
        //Si el lote no tiene analisis realizado (Recibido), se crea uno nuevo y se guarda el movimiento
        final Analisis newAnalisis = analisisService.save(DTOUtils.createAnalisis(dto));
        return movimientoRepository.save(createMovimientoMuestreoConAnalisis(dto, bulto, newAnalisis));
    }


}
