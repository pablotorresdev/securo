package com.mb.conitrack.service.cu;

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
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;

import jakarta.validation.Valid;

import static com.mb.conitrack.enums.EstadoEnum.EN_USO;
import static com.mb.conitrack.enums.EstadoEnum.NUEVO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoReverso;
import static com.mb.conitrack.utils.UnidadMedidaUtils.sumarMovimientoConvertido;

//***********CU23 ALTA: DEVOLUCION VENTA***********
@Service
public class ModifReversoMovimientoService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirReversoMovmiento(final MovimientoDTO dto) {

        final Movimiento movOrigen = movimientoRepository
            .findByCodigoMovimientoAndActivoTrue(dto.getCodigoMovimientoOrigen())
            .orElseThrow(() -> new IllegalArgumentException("El Movimmiento no existe."));

        switch (movOrigen.getTipoMovimiento()) {
            case ALTA -> {
                if (movOrigen.getMotivo() == MotivoEnum.COMPRA) {
                    return reversarAltaIngresoCompra(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.PRODUCCION_PROPIA) {
                    return reversarAltaIngresoProduccion(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_VENTA) {
                    //TODO: implementar
                    return reversarAltaDevolucionVenta(dto, movOrigen);
                }
            }
            case MODIFICACION -> {
                if (movOrigen.getMotivo() == MotivoEnum.ANALISIS) {
                    return reversarModifDictamenCuarentena(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.RESULTADO_ANALISIS) {
                    return reversarModifResultadoAnalisis(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.LIBERACION) {
                    return reversarModifLiberacionProducto(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_VENTA) {
                    //TODO: implementar
                    return reversarModifDevolucionVenta(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.RETIRO_MERCADO) {
                    //TODO: implementar
                    return reversarModifRetiroMercado(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.ANULACION_ANALISIS) {
                    //TODO: implementar
                    return reversarAnulacionAnalisis(dto, movOrigen);
                }
            }
            case BAJA -> {
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_COMPRA) {
                    return reversarBajaDevolucionCompra(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.MUESTREO) {
                    return reversarBajaMuestreoBulto(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.CONSUMO_PRODUCCION) {
                    return reversarBajaConsumoProduccion(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.VENTA) {
                    return reversarBajaVentaProducto(dto, movOrigen);
                }
            }
        }
        return new LoteDTO();
    }

    public boolean validarReversoMovmientoInput(
        final @Valid MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //TODO: implementar
        return true;
    }

    @Transactional
    LoteDTO reversarBajaConsumoProduccion(final MovimientoDTO dto, final Movimiento movOrigen) {
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
    LoteDTO reversarBajaDevolucionCompra(final MovimientoDTO dto, final Movimiento movOrigen) {
        return reversarBajaGranel(dto, movOrigen);
    }

    @Transactional
    LoteDTO reversarAltaDevolucionVenta(final MovimientoDTO dto, final Movimiento movOrigen) {
        //TODO: implementar
        return DTOUtils.fromLoteEntity(movOrigen.getLote());
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

        movOrigen.getLote().getTrazas().forEach(t -> t.setActivo(false));
        trazaRepository.saveAll(movOrigen.getLote().getTrazas());

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        loteRepository.save(movOrigen.getLote());

        return DTOUtils.fromLoteEntity(movOrigen.getLote());
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
    LoteDTO reversarModifDevolucionVenta(final MovimientoDTO dto, final Movimiento movOrigen) {
        //TODO: implementar
        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

    @Transactional
    LoteDTO reversarBajaMuestreoBulto(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        movimiento.setCantidad(movOrigen.getCantidad());
        movimiento.setUnidadMedida(movOrigen.getUnidadMedida());

        if (movOrigen.getDetalles().size() > 1) {
            throw new IllegalArgumentException("Multimuestreo no soportado aun");
        }

        final DetalleMovimiento detalleMovimiento = movOrigen.getDetalles().stream()
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

        final Lote lote = movOrigen.getLote();
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

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        loteRepository.save(lote);

        return DTOUtils.fromLoteEntity(movOrigen.getLote());
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
    LoteDTO reversarModifRetiroMercado(final MovimientoDTO dto, final Movimiento movOrigen) {
        //TODO: implementar
        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

    @Transactional
    LoteDTO reversarAnulacionAnalisis(final MovimientoDTO dto, final Movimiento movOrigen) {
        //TODO: implementar
        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

    @Transactional
    LoteDTO reversarBajaVentaProducto(final MovimientoDTO dto, final Movimiento movOrigen) {
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
            detalleMovimiento.getTrazas().forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));
            trazaRepository.saveAll(detalleMovimiento.getTrazas());
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

}
