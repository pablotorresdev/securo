package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.*;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.mb.conitrack.enums.EstadoEnum.*;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoReverso;
import static com.mb.conitrack.utils.UnidadMedidaUtils.sumarMovimientoConvertido;
import static java.lang.Boolean.TRUE;

//***********CU23 ALTA: DEVOLUCION VENTA***********
@Service
public class ModifReversoMovimientoService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirReversoMovmiento(final MovimientoDTO dto) {

        final List<Movimiento> allByCodigoMovimiento = movimientoRepository
                .findAllByCodigoMovimiento(dto.getCodigoMovimientoOrigen());

        if (allByCodigoMovimiento.isEmpty()) {
            throw new IllegalArgumentException("El Movimmiento no existe.");
        } else if (allByCodigoMovimiento.size() == 1) {
            final Movimiento movOrigen = allByCodigoMovimiento.get(0);
            switch (movOrigen.getTipoMovimiento()) {
                case ALTA -> {
                    if (movOrigen.getMotivo() == MotivoEnum.COMPRA) { //CU1
                        return reversarAltaIngresoCompra(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.PRODUCCION_PROPIA) { //CU20
                        return reversarAltaIngresoProduccion(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_VENTA) { //CU23
                        return reversarAltaDevolucionVenta(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.RETIRO_MERCADO) { //CU23
                        return reversarRetiroMercado(dto, movOrigen);
                    }
                }
                case MODIFICACION -> {
                    if (movOrigen.getMotivo() == MotivoEnum.ANALISIS) { //CU2/CU8?
                        return reversarModifDictamenCuarentena(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.RESULTADO_ANALISIS) { //CU5/6
                        return reversarModifResultadoAnalisis(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.LIBERACION) { //CU21
                        return reversarModifLiberacionProducto(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.TRAZADO) { //CU27
                        return reversarModifTrazadoLote(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.ANULACION_ANALISIS) {//CU11
                        return reversarAnulacionAnalisis(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.RETIRO_MERCADO) {//CU11
                        throw new IllegalStateException(
                                "El lote origen tiene un recall asociado, no se puede reversar el movimiento.");
                    }
                }
                case BAJA -> {
                    if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_COMPRA) { //CU4
                        return reversarBajaDevolucionCompra(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.MUESTREO) { //CU3
                        return reversarBajaMuestreoBulto(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.CONSUMO_PRODUCCION) {//CU7
                        return reversarBajaConsumoProduccion(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.VENTA) { //CU22
                        return reversarBajaVentaProducto(dto, movOrigen);
                    }
                    if (movOrigen.getMotivo() == MotivoEnum.AJUSTE) { //CU22
                        return reversarBajaAjuste(dto, movOrigen);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Cantidad incorrecta de movimientos");
        }

        return new LoteDTO();
    }

    public boolean validarReversoMovmientoInput(
            final @Valid MovimientoDTO movimientoDTO,
            final BindingResult bindingResult) {
        return !bindingResult.hasErrors();
        //TODO: implementar si es necesario validar los campos de entrada
    }

    @Transactional
    public LoteDTO reversarModifLiberacionProducto(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        final Lote lote = movOrigen.getLote();

        lote.setDictamen(movOrigen.getDictamenInicial());

        movimiento.setDictamenInicial(movOrigen.getDictamenFinal());
        movimiento.setDictamenFinal(movOrigen.getDictamenInicial());
        lote.setFechaVencimientoProveedor(null);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movimientoRepository.save(movOrigen);
        movimientoRepository.save(movimiento);

        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    LoteDTO reversarAltaDevolucionVenta(final MovimientoDTO dto, final Movimiento movDevolucionOrigen) {
        Movimiento movReverso = createMovimientoReverso(dto, movDevolucionOrigen);

        movDevolucionOrigen.setActivo(false);
        movReverso.setActivo(false);

        final Lote loteAltaDevolucion = movDevolucionOrigen.getLote();
        loteAltaDevolucion.setActivo(false);
        final List<Bulto> bultosDevolucion = loteAltaDevolucion.getBultos();
        bultosDevolucion.forEach(b -> b.setActivo(false));
        bultosDevolucion.forEach(b -> b.getDetalles().forEach(d -> d.setActivo(false)));

        final Lote loteVentaOrigen = loteAltaDevolucion.getLoteOrigen();

        if (TRUE.equals(loteVentaOrigen.getTrazado())) {
            final Set<DetalleMovimiento> detallesAltaDevolucion = movDevolucionOrigen.getDetalles();
            for (DetalleMovimiento detalleAltaDevolucion : detallesAltaDevolucion) {
                final Set<Traza> trazasMovimento = detalleAltaDevolucion.getTrazas();
                trazasMovimento.forEach(t -> t.setEstado(EstadoEnum.VENDIDO));
                final Bulto bultoVentaOrigen = loteVentaOrigen.getBultoByNro(detalleAltaDevolucion.getBulto()
                        .getNroBulto());
                trazasMovimento.forEach(t -> t.setBulto(bultoVentaOrigen));
                trazasMovimento.forEach(t -> t.setLote(loteVentaOrigen));
                trazaRepository.saveAll(trazasMovimento);
            }
        }

        bultoRepository.saveAll(bultosDevolucion);
        movimientoRepository.save(movReverso);
        movimientoRepository.save(movDevolucionOrigen);
        return DTOUtils.fromLoteEntity(loteRepository.save(loteAltaDevolucion));
    }


    @Transactional
    LoteDTO reversarRetiroMercado(final MovimientoDTO dto, final Movimiento movRecallOrigen) {
        reversarAltaRecall(dto, movRecallOrigen);

        Movimiento movimientoVentaOrigen = movRecallOrigen.getMovimientoOrigen();
        final List<Movimiento> allByCodigoMovimiento = movimientoRepository
                .findByMovimientoOrigen(movimientoVentaOrigen.getCodigoMovimiento());

        //Si queda un solo movimiento, es el de Modificacion del lote a Recall
        if (allByCodigoMovimiento.size() == 1) {
            Movimiento movOrigen = allByCodigoMovimiento.get(0);
            if (movOrigen.getTipoMovimiento() != TipoMovimientoEnum.MODIFICACION) {
                throw new IllegalStateException("El movimiento de venta asociado al recall no es de modificacion.");
            }
            Movimiento movReversoModifRecall = createMovimientoReverso(dto, movOrigen);
            movOrigen.setActivo(false);
            movReversoModifRecall.setActivo(false);

            Lote loteOrigen = movOrigen.getLote();
            if (loteOrigen == null) {
                throw new IllegalStateException("No se encontraron movimientos para reversar.");
            }
            loteOrigen.setDictamen(movOrigen.getDictamenInicial());

            for (Bulto bulto : loteOrigen.getBultos()) {
                if (bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
                    bulto.setEstado(NUEVO);
                } else {
                    if (BigDecimal.ZERO.compareTo(bulto.getCantidadActual()) == 0) {
                        bulto.setEstado(CONSUMIDO);
                    } else {
                        bulto.setEstado(EN_USO);
                    }
                }
            }

            if (loteOrigen.getCantidadInicial().compareTo(loteOrigen.getCantidadActual()) == 0) {
                loteOrigen.setEstado(NUEVO);
            } else {
                if (BigDecimal.ZERO.compareTo(loteOrigen.getCantidadActual()) == 0) {
                    loteOrigen.setEstado(CONSUMIDO);
                } else {
                    loteOrigen.setEstado(EN_USO);
                }
            }

            if (TRUE.equals(loteOrigen.getTrazado())) {
                final List<Traza> trazasLoteOrigen = loteOrigen.getActiveTrazas();
                final List<Traza> list = trazasLoteOrigen.stream().filter(t -> t.getEstado() == RECALL).toList();
                list.forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));
                trazaRepository.saveAll(list);
            }
            bultoRepository.saveAll(loteOrigen.getBultos());
            movimientoRepository.save(movReversoModifRecall);
            return DTOUtils.fromLoteEntity(loteRepository.save(loteOrigen));
        } else {
            return DTOUtils.fromLoteEntity(movRecallOrigen.getLote());
        }

    }

    private void reversarAltaRecall(MovimientoDTO dto, Movimiento movRecallOrigen) {
        Movimiento movReversoAltaRecall = createMovimientoReverso(dto, movRecallOrigen);
        movRecallOrigen.setActivo(false);
        movReversoAltaRecall.setActivo(false);
        final Lote loteAltaRecall = movRecallOrigen.getLote();
        loteAltaRecall.setActivo(false);

        final List<Bulto> bultosRecall = loteAltaRecall.getBultos();
        bultosRecall.forEach(b -> b.setActivo(false));
        bultosRecall.forEach(b -> b.getDetalles().forEach(d -> d.setActivo(false)));

        final Lote loteVentaOrigen = loteAltaRecall.getLoteOrigen();

        if (TRUE.equals(loteVentaOrigen.getTrazado())) {
            final Set<DetalleMovimiento> detallesAltaRecall = movRecallOrigen.getDetalles();
            for (DetalleMovimiento detalleAltaRecall : detallesAltaRecall) {
                final Set<Traza> trazasMovimento = detalleAltaRecall.getTrazas();
                trazasMovimento.forEach(t -> t.setEstado(EstadoEnum.VENDIDO));
                final Bulto bultoVentaOrigen = loteVentaOrigen.getBultoByNro(detalleAltaRecall.getBulto()
                        .getNroBulto());
                trazasMovimento.forEach(t -> t.setBulto(bultoVentaOrigen));
                trazasMovimento.forEach(t -> t.setLote(loteVentaOrigen));
                trazaRepository.saveAll(trazasMovimento);
            }
        }

        bultoRepository.saveAll(bultosRecall);
        movimientoRepository.save(movReversoAltaRecall);
        movimientoRepository.save(movRecallOrigen);
        loteRepository.save(loteAltaRecall);
    }


    @Transactional
    LoteDTO reversarAltaIngresoCompra(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movOrigen.getLote().setActivo(false);

        movOrigen.getLote().getBultos().forEach(b -> b.setActivo(false));
        movOrigen.getLote().getBultos().forEach(b -> b.getDetalles().forEach(d -> d.setActivo(false)));
        bultoRepository.saveAll(movOrigen.getLote().getBultos());

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        loteRepository.save(movOrigen.getLote());

        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

    @Transactional
    LoteDTO reversarAltaIngresoProduccion(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movOrigen.getLote().setActivo(false);

        movOrigen.getLote().getBultos().forEach(b -> b.setActivo(false));
        movOrigen.getLote().getBultos().forEach(b -> b.getDetalles().forEach(d -> d.setActivo(false)));
        bultoRepository.saveAll(movOrigen.getLote().getBultos());

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        loteRepository.save(movOrigen.getLote());

        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

    @Transactional
    LoteDTO reversarAltaRetiroMercado(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        final Lote loteOrigen = movOrigen.getLote();
        final List<Lote> lotesByLoteOrigen = loteRepository.findLotesByLoteOrigen(loteOrigen.getCodigoLote());
        if (!lotesByLoteOrigen.isEmpty()) {
            throw new IllegalStateException(
                    "El lote origen tiene una devolucion asociada, no se puede reversar el movimiento.");
        }

        movimiento.setCantidad(movOrigen.getCantidad());
        movimiento.setUnidadMedida(movOrigen.getUnidadMedida());

        final Set<DetalleMovimiento> detalles = movOrigen.getDetalles();

        for (DetalleMovimiento detalleMovimiento : detalles) {
            final Bulto bulto = detalleMovimiento.getBulto();
            dto.setCantidad(detalleMovimiento.getCantidad());
            dto.setUnidadMedida(detalleMovimiento.getUnidadMedida());
            bulto.setCantidadActual(sumarMovimientoConvertido(dto, bulto));
            if (bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
                bulto.setEstado(NUEVO);
            } else {
                bulto.setEstado(EN_USO);
            }
            if (TRUE.equals(loteOrigen.getTrazado())) {
                detalleMovimiento.getTrazas().forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));
                trazaRepository.saveAll(detalleMovimiento.getTrazas());
            }
            bultoRepository.save(bulto);
        }

        dto.setCantidad(movOrigen.getCantidad());
        dto.setUnidadMedida(movOrigen.getUnidadMedida());
        loteOrigen.setCantidadActual(sumarMovimientoConvertido(dto, loteOrigen));

        if (loteOrigen.getCantidadInicial().compareTo(loteOrigen.getCantidadActual()) == 0) {
            loteOrigen.setEstado(NUEVO);
        } else {
            loteOrigen.setEstado(EN_USO);
        }

        detalles.forEach(d -> d.setActivo(false));
        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        return DTOUtils.fromLoteEntity(loteRepository.save(loteOrigen));
    }

    @Transactional
    LoteDTO reversarAnulacionAnalisis(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        final Lote lote = movOrigen.getLote();
        final Analisis ultimoAnalisis = lote.getUltimoAnalisis();

        if (ultimoAnalisis.getDictamen() != DictamenEnum.ANULADO) {
            throw new IllegalArgumentException("El ultimo analisis no esta anulado");
        }

        ultimoAnalisis.setDictamen(null);
        analisisRepository.save(ultimoAnalisis);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);

        lote.setDictamen(movOrigen.getDictamenInicial());
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    LoteDTO reversarBajaAjuste(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        final Lote loteOrigen = movOrigen.getLote();
        final List<Lote> lotesByLoteOrigen = loteRepository.findLotesByLoteOrigen(loteOrigen.getCodigoLote());
        if (!lotesByLoteOrigen.isEmpty()) {
            throw new IllegalStateException(
                    "El lote origen tiene un ajuste asociado, no se puede reversar el movimiento.");
        }

        movimiento.setCantidad(movOrigen.getCantidad());
        movimiento.setUnidadMedida(movOrigen.getUnidadMedida());

        final Set<DetalleMovimiento> detalles = movOrigen.getDetalles();

        for (DetalleMovimiento detalleMovimiento : detalles) {
            final Bulto bulto = detalleMovimiento.getBulto();
            dto.setCantidad(detalleMovimiento.getCantidad());
            dto.setUnidadMedida(detalleMovimiento.getUnidadMedida());
            bulto.setCantidadActual(sumarMovimientoConvertido(dto, bulto));
            if (bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
                bulto.setEstado(NUEVO);
            } else {
                bulto.setEstado(EN_USO);
            }

            final Set<Traza> trazas = detalleMovimiento.getTrazas();
            if (!trazas.isEmpty()) {
                trazas.forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));
                trazaRepository.saveAll(trazas);
            }
            bultoRepository.save(bulto);
        }

        dto.setCantidad(movOrigen.getCantidad());
        dto.setUnidadMedida(movOrigen.getUnidadMedida());
        loteOrigen.setCantidadActual(sumarMovimientoConvertido(dto, loteOrigen));

        if (loteOrigen.getCantidadInicial().compareTo(loteOrigen.getCantidadActual()) == 0) {
            loteOrigen.setEstado(NUEVO);
        } else {
            loteOrigen.setEstado(EN_USO);
        }

        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        return DTOUtils.fromLoteEntity(loteRepository.save(loteOrigen));
    }

    @Transactional
    LoteDTO reversarBajaConsumoProduccion(final MovimientoDTO dto, final Movimiento movOrigen) {
        return reversarBajaGranel(dto, movOrigen);
    }

    @Transactional
    LoteDTO reversarBajaDevolucionCompra(final MovimientoDTO dto, final Movimiento movOrigen) {
        return reversarBajaGranel(dto, movOrigen);
    }

    @Transactional
    LoteDTO reversarBajaGranel(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        movimiento.setCantidad(movOrigen.getCantidad());
        movimiento.setUnidadMedida(movOrigen.getUnidadMedida());

        final Set<DetalleMovimiento> detalles = movOrigen.getDetalles();

        for (DetalleMovimiento detalleMovimiento : detalles) {
            final Bulto bulto = detalleMovimiento.getBulto();
            dto.setCantidad(detalleMovimiento.getCantidad());
            dto.setUnidadMedida(detalleMovimiento.getUnidadMedida());
            bulto.setCantidadActual(sumarMovimientoConvertido(dto, bulto));
            if (bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
                bulto.setEstado(NUEVO);
            } else {
                bulto.setEstado(EN_USO);
            }
            bultoRepository.save(bulto);
        }

        final Lote lote = movOrigen.getLote();

        dto.setCantidad(movOrigen.getCantidad());
        dto.setUnidadMedida(movOrigen.getUnidadMedida());
        lote.setCantidadActual(sumarMovimientoConvertido(dto, lote));

        if (lote.getCantidadInicial().compareTo(lote.getCantidadActual()) == 0) {
            lote.setEstado(NUEVO);
        } else {
            lote.setEstado(EN_USO);
        }

        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    LoteDTO reversarBajaMuestreoBulto(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        movimiento.setCantidad(movOrigen.getCantidad());
        movimiento.setUnidadMedida(movOrigen.getUnidadMedida());

        final Lote lote = movOrigen.getLote();
        final Set<DetalleMovimiento> detalles = movOrigen.getDetalles();
        if (detalles.size() > 1) {

            for (DetalleMovimiento detalleMovimiento : detalles) {
                final Bulto bulto = detalleMovimiento.getBulto();
                dto.setCantidad(detalleMovimiento.getCantidad());
                dto.setUnidadMedida(detalleMovimiento.getUnidadMedida());
                bulto.setCantidadActual(sumarMovimientoConvertido(dto, bulto));
                if (bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
                    bulto.setEstado(NUEVO);
                } else {
                    bulto.setEstado(EN_USO);
                }
                bultoRepository.save(bulto);

                lote.setCantidadActual(sumarMovimientoConvertido(dto, lote));
            }

            if (lote.getCantidadInicial().compareTo(lote.getCantidadActual()) == 0) {
                lote.setEstado(NUEVO);
            } else {
                lote.setEstado(EN_USO);
            }

            movOrigen.setActivo(false);
            movimiento.setActivo(false);
        } else {
            final DetalleMovimiento detalleMovimiento = detalles.stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El detalle del movimiento a reversar no existe."));

            final Bulto bulto = detalleMovimiento.getBulto();
            detalleMovimiento.getTrazas().forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));
            dto.setCantidad(movOrigen.getCantidad());
            dto.setUnidadMedida(movOrigen.getUnidadMedida());

            bulto.setCantidadActual(sumarMovimientoConvertido(dto, bulto));
            if (bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
                bulto.setEstado(NUEVO);
            } else {
                bulto.setEstado(EN_USO);
            }

            lote.setCantidadActual(sumarMovimientoConvertido(dto, lote));

            if (lote.getCantidadInicial().compareTo(lote.getCantidadActual()) == 0) {
                lote.setEstado(NUEVO);
            } else {
                lote.setEstado(EN_USO);
            }

            movOrigen.setActivo(false);
            movimiento.setActivo(false);

            trazaRepository.saveAll(detalleMovimiento.getTrazas());
            bultoRepository.save(bulto);
        }

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);

        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    LoteDTO reversarBajaVentaProducto(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        final Lote loteOrigen = movOrigen.getLote();
        final List<Lote> lotesByLoteOrigen = loteRepository.findLotesByLoteOrigen(loteOrigen.getCodigoLote());
        if (!lotesByLoteOrigen.isEmpty()) {
            throw new IllegalStateException(
                    "El lote origen tiene una devolucion asociada, no se puede reversar el movimiento.");
        }

        movimiento.setCantidad(movOrigen.getCantidad());
        movimiento.setUnidadMedida(movOrigen.getUnidadMedida());

        final Set<DetalleMovimiento> detalles = movOrigen.getDetalles();

        for (DetalleMovimiento detalleMovimiento : detalles) {
            final Bulto bulto = detalleMovimiento.getBulto();
            dto.setCantidad(detalleMovimiento.getCantidad());
            dto.setUnidadMedida(detalleMovimiento.getUnidadMedida());
            bulto.setCantidadActual(sumarMovimientoConvertido(dto, bulto));
            if (bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
                bulto.setEstado(NUEVO);
            } else {
                bulto.setEstado(EN_USO);
            }
            if (TRUE.equals(loteOrigen.getTrazado())) {
                detalleMovimiento.getTrazas().forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));
                trazaRepository.saveAll(detalleMovimiento.getTrazas());
            }
            bultoRepository.save(bulto);
        }

        dto.setCantidad(movOrigen.getCantidad());
        dto.setUnidadMedida(movOrigen.getUnidadMedida());
        loteOrigen.setCantidadActual(sumarMovimientoConvertido(dto, loteOrigen));

        if (loteOrigen.getCantidadInicial().compareTo(loteOrigen.getCantidadActual()) == 0) {
            loteOrigen.setEstado(NUEVO);
        } else {
            loteOrigen.setEstado(EN_USO);
        }

        detalles.forEach(d -> d.setActivo(false));
        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        return DTOUtils.fromLoteEntity(loteRepository.save(loteOrigen));
    }

    @Transactional
    LoteDTO reversarModifDictamenCuarentena(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        final Lote lote = movOrigen.getLote();
        lote.setDictamen(movOrigen.getDictamenInicial());

        movimiento.setDictamenFinal(movOrigen.getDictamenInicial());
        movimiento.setDictamenInicial(movOrigen.getDictamenFinal());

        final Analisis ultimoAnalisis = lote.getUltimoAnalisis();

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        ultimoAnalisis.setActivo(false);

        analisisRepository.save(ultimoAnalisis);
        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);

        loteRepository.save(lote);

        return DTOUtils.fromLoteEntity(lote);
    }

    @Transactional
    LoteDTO reversarModifResultadoAnalisis(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        final Lote lote = movOrigen.getLote();
        lote.setDictamen(movOrigen.getDictamenInicial());

        movimiento.setDictamenFinal(movOrigen.getDictamenInicial());
        movimiento.setDictamenInicial(movOrigen.getDictamenFinal());

        final Analisis ultimoAnalisis = lote.getUltimoAnalisis();

        ultimoAnalisis.setFechaRealizado(null);
        ultimoAnalisis.setDictamen(null);
        ultimoAnalisis.setFechaReanalisis(null);
        ultimoAnalisis.setFechaVencimiento(null);
        ultimoAnalisis.setTitulo(null);
        ultimoAnalisis.setObservaciones(null);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        analisisRepository.save(ultimoAnalisis);
        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);

        loteRepository.save(lote);

        return DTOUtils.fromLoteEntity(lote);
    }


    private LoteDTO reversarModifTrazadoLote(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        final Lote lote = movOrigen.getLote();

        final List<Traza> trazasLote = lote.getActiveTrazas();
        for (Traza t : trazasLote) {
            t.setActivo(false);
            t.setEstado(EstadoEnum.DESCARTADO);
            t.getDetalles().forEach(d -> d.setActivo(false));
        }
        trazaRepository.saveAll(trazasLote);
        lote.setTrazado(false);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movimientoRepository.save(movOrigen);
        movimientoRepository.save(movimiento);

        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

}
