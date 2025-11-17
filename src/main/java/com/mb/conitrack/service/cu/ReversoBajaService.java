package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.*;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.mb.conitrack.enums.EstadoEnum.*;
import static com.mb.conitrack.utils.MovimientoModificacionUtils.createMovimientoReverso;
import static com.mb.conitrack.utils.UnidadMedidaUtils.sumarMovimientoConvertido;
import static java.lang.Boolean.TRUE;

/**
 * Servicio especializado para reversar movimientos de tipo BAJA.
 * Maneja los reversos de:
 * - CU4: Devolución de Compra
 * - CU3: Muestreo de Bulto
 * - CU7: Consumo en Producción
 * - CU22: Venta de Producto
 * - CU28: Ajuste de Stock (Baja)
 */
@Service
public class ReversoBajaService extends AbstractCuService {

    @Transactional
    public LoteDTO reversarBajaDevolucionCompra(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        return reversarBajaGranel(dto, movOrigen, currentUser);
    }

    @Transactional
    public LoteDTO reversarBajaConsumoProduccion(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        return reversarBajaGranel(dto, movOrigen, currentUser);
    }

    @Transactional
    public LoteDTO reversarBajaGranel(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);
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

        if (lote.getUltimoAnalisis() != null && lote.getUltimoAnalisis().getDictamen() == DictamenEnum.CANCELADO) {
            lote.getUltimoAnalisis().setDictamen(null);
            analisisRepository.save(lote.getUltimoAnalisis());
        }

        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public LoteDTO reversarBajaMuestreoBulto(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);
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
    public LoteDTO reversarBajaVentaProducto(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);
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

        // Reverso CU22: Restaurar análisis si fue CANCELADO por la venta
        if (loteOrigen.getUltimoAnalisis() != null && loteOrigen.getUltimoAnalisis().getDictamen() == DictamenEnum.CANCELADO) {
            loteOrigen.getUltimoAnalisis().setDictamen(null);
            analisisRepository.save(loteOrigen.getUltimoAnalisis());
        }

        detalles.forEach(d -> d.setActivo(false));
        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        return DTOUtils.fromLoteEntity(loteRepository.save(loteOrigen));
    }

    @Transactional
    public LoteDTO reversarBajaAjuste(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);
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

        // Reverso CU28: Restaurar análisis si fue CANCELADO por el ajuste
        if (loteOrigen.getUltimoAnalisis() != null && loteOrigen.getUltimoAnalisis().getDictamen() == DictamenEnum.CANCELADO) {
            loteOrigen.getUltimoAnalisis().setDictamen(null);
            analisisRepository.save(loteOrigen.getUltimoAnalisis());
        }

        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        return DTOUtils.fromLoteEntity(loteRepository.save(loteOrigen));
    }
}
