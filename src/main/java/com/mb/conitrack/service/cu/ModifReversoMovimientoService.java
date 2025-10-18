package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

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

import jakarta.validation.Valid;

import static com.mb.conitrack.enums.EstadoEnum.CONSUMIDO;
import static com.mb.conitrack.enums.EstadoEnum.EN_USO;
import static com.mb.conitrack.enums.EstadoEnum.NUEVO;
import static com.mb.conitrack.enums.EstadoEnum.RECALL;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoReverso;
import static com.mb.conitrack.utils.UnidadMedidaUtils.sumarMovimientoConvertido;

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
                }
            }
        } else if (allByCodigoMovimiento.size() == 2) {
            if (allByCodigoMovimiento.get(0).getMotivo() == MotivoEnum.RETIRO_MERCADO &&
                allByCodigoMovimiento.get(1).getMotivo() == MotivoEnum.RETIRO_MERCADO) { //CU24 ?
                return reversarRetiroMercado(dto, allByCodigoMovimiento);
            }
        } else {
            throw new IllegalArgumentException("Cantidad incorrecta de movimientos");
        }

        return new LoteDTO();
    }

    private LoteDTO reversarModifTrazadoLote(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        final Lote lote = movOrigen.getLote();

        final List<Traza> trazasLote = lote.getActiveTrazas();
        for (Traza t : trazasLote) {
            t.setActivo(false);
            t.setEstado(EstadoEnum.DESCARTADO);
        }
        trazaRepository.saveAll(trazasLote);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movimientoRepository.save(movOrigen);
        movimientoRepository.save(movimiento);

        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    public boolean validarReversoMovmientoInput(
        final @Valid MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        return !bindingResult.hasErrors();
        //TODO: implementar
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
    LoteDTO reversarAltaDevolucionVenta(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movReverso = createMovimientoReverso(dto, movOrigen);

        movOrigen.setActivo(false);
        movReverso.setActivo(false);
        final Lote loteOrigen = movOrigen.getLote();
        loteOrigen.setActivo(false);

        final Set<DetalleMovimiento> detalles = movOrigen.getDetalles();

        for (DetalleMovimiento detalleMovimiento : detalles) {
            final Set<Traza> trazas = detalleMovimiento.getTrazas();
            trazas.forEach(t -> t.setEstado(EstadoEnum.VENDIDO));
            trazaRepository.saveAll(trazas);
        }

        final List<Bulto> bultos = loteOrigen.getBultos();
        bultos.forEach(b -> b.setActivo(false));
        bultoRepository.saveAll(bultos);

        movimientoRepository.save(movReverso);
        movimientoRepository.save(movOrigen);
        return DTOUtils.fromLoteEntity(loteRepository.save(loteOrigen));
    }

    @Transactional
    LoteDTO reversarAltaIngresoCompra(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movOrigen.getLote().setActivo(false);

        movOrigen.getLote().getBultos().forEach(b -> b.setActivo(false));
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
        bultoRepository.saveAll(movOrigen.getLote().getBultos());

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        loteRepository.save(movOrigen.getLote());

        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

    @Transactional
    LoteDTO reversarAltaRetiroMercado(final MovimientoDTO dto, final Movimiento movOrigen) {
        //TODO: implementar
        return DTOUtils.fromLoteEntity(movOrigen.getLote());
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
            detalleMovimiento.getTrazas().forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));
            trazaRepository.saveAll(detalleMovimiento.getTrazas());
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

    @Transactional
    LoteDTO reversarRetiroMercado(final MovimientoDTO dto, final List<Movimiento> allByCodigoMovimiento) {
        Lote loteOrigen = null;
        for (Movimiento movOrigen : allByCodigoMovimiento) {
            if (movOrigen.getTipoMovimiento() == TipoMovimientoEnum.ALTA) {
                Movimiento movReverso = createMovimientoReverso(dto, movOrigen);

                movOrigen.setActivo(false);
                movReverso.setActivo(false);
                if (loteOrigen == null) {
                    loteOrigen = movOrigen.getLote();
                }

                final Set<DetalleMovimiento> detalles = movOrigen.getDetalles();

                for (DetalleMovimiento detalleMovimiento : detalles) {
                    final Bulto bulto = detalleMovimiento.getBulto();
                    bulto.setCantidadActual(bulto.getCantidadActual().subtract(detalleMovimiento.getCantidad()));
                    if (bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
                        bulto.setEstado(NUEVO);
                    } else {
                        if (bulto.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
                            bulto.setEstado(CONSUMIDO);
                        } else {
                            bulto.setEstado(EN_USO);
                        }
                    }

                    final Set<Traza> trazas = detalleMovimiento.getTrazas();
                    trazas.forEach(t -> t.setEstado(EstadoEnum.VENDIDO));
                    trazaRepository.saveAll(trazas);
                }

                loteOrigen.setCantidadActual(loteOrigen.getCantidadActual().subtract(movOrigen.getCantidad()));
                if (loteOrigen.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
                    loteOrigen.setEstado(CONSUMIDO);
                } else {
                    loteOrigen.setEstado(EN_USO);
                }

                movimientoRepository.save(movReverso);
                movimientoRepository.save(movOrigen);
            } else if (movOrigen.getTipoMovimiento() == TipoMovimientoEnum.MODIFICACION) {
                Movimiento movReverso = createMovimientoReverso(dto, movOrigen);
                movOrigen.setActivo(false);
                movReverso.setActivo(false);

                if (loteOrigen == null) {
                    loteOrigen = movOrigen.getLote();
                }
                loteOrigen.setDictamen(movOrigen.getDictamenInicial());
                loteOrigen.setEstado(EN_USO);

                final List<Traza> trazasLoteOrigen = loteOrigen.getActiveTrazas();
                final List<Traza> list = trazasLoteOrigen.stream().filter(t -> t.getEstado() == RECALL).toList();
                list.forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));
                trazaRepository.saveAll(list);
            }
        }
        if (loteOrigen == null) {
            throw new IllegalStateException("No se encontraron movimientos para reversar.");
        }

        return DTOUtils.fromLoteEntity(loteRepository.save(loteOrigen));
    }

}
