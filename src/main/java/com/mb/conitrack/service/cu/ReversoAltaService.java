package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.*;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.EstadoEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.mb.conitrack.utils.MovimientoModificacionUtils.createMovimientoReverso;
import static java.lang.Boolean.TRUE;

/**
 * Servicio especializado para reversar movimientos de tipo ALTA.
 * Maneja los reversos de:
 * - CU1: Ingreso Compra
 * - CU20: Ingreso Producción
 * - CU23: Devolución de Venta
 * - CU24: Retiro de Mercado (Recall)
 */
@Service
public class ReversoAltaService extends AbstractCuService {

    @Transactional
    public LoteDTO reversarAltaIngresoCompra(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);

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
    public LoteDTO reversarAltaIngresoProduccion(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);

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
    public LoteDTO reversarAltaDevolucionVenta(final MovimientoDTO dto, final Movimiento movDevolucionOrigen, final User currentUser) {
        Movimiento movReverso = createMovimientoReverso(dto, movDevolucionOrigen, currentUser);

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
    public LoteDTO reversarRetiroMercado(final MovimientoDTO dto, final Movimiento movRecallOrigen, final User currentUser) {
        reversarAltaRecall(dto, movRecallOrigen, currentUser);

        // El resto de la lógica maneja la modificación del lote de venta origen
        return reversarModificacionRecall(dto, movRecallOrigen, currentUser);
    }

    /**
     * Reversa el movimiento de ALTA del recall
     */
    void reversarAltaRecall(MovimientoDTO dto, Movimiento movRecallOrigen, User currentUser) {
        Movimiento movReversoAltaRecall = createMovimientoReverso(dto, movRecallOrigen, currentUser);
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

    /**
     * Reversa la modificación del lote de venta origen que fue marcado como RECALL
     */
    LoteDTO reversarModificacionRecall(MovimientoDTO dto, Movimiento movRecallOrigen, User currentUser) {
        Movimiento movimientoVentaOrigen = movRecallOrigen.getMovimientoOrigen();
        final List<Movimiento> allByCodigoMovimiento = movimientoRepository
                .findByMovimientoOrigen(movimientoVentaOrigen.getCodigoMovimiento());

        //Si queda un solo movimiento, es el de Modificacion del lote a Recall
        if (allByCodigoMovimiento.size() == 1) {
            return reversarModificacionRecallInterno(dto, allByCodigoMovimiento.get(0), currentUser);
        } else {
            return DTOUtils.fromLoteEntity(movRecallOrigen.getLote());
        }
    }

    /**
     * Lógica interna para reversar la modificación de estado RECALL en el lote de venta
     */
    LoteDTO reversarModificacionRecallInterno(MovimientoDTO dto, Movimiento movOrigen, User currentUser) {
        if (movOrigen.getTipoMovimiento() != com.mb.conitrack.enums.TipoMovimientoEnum.MODIFICACION) {
            throw new IllegalStateException("El movimiento de venta asociado al recall no es de modificacion.");
        }

        Movimiento movReversoModifRecall = createMovimientoReverso(dto, movOrigen, currentUser);
        movOrigen.setActivo(false);
        movReversoModifRecall.setActivo(false);

        Lote loteOrigen = movOrigen.getLote();
        if (loteOrigen == null) {
            throw new IllegalStateException("No se encontraron movimientos para reversar.");
        }
        loteOrigen.setDictamen(movOrigen.getDictamenInicial());

        // Restaurar estado de bultos
        for (Bulto bulto : loteOrigen.getBultos()) {
            restaurarEstadoBulto(bulto);
        }

        // Restaurar estado del lote
        restaurarEstadoLote(loteOrigen);

        // Restaurar trazas si el lote está trazado
        if (TRUE.equals(loteOrigen.getTrazado())) {
            final List<Traza> trazasLoteOrigen = loteOrigen.getActiveTrazas();
            final List<Traza> list = trazasLoteOrigen.stream()
                    .filter(t -> t.getEstado() == EstadoEnum.RECALL)
                    .toList();
            list.forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));
            trazaRepository.saveAll(list);
        }

        bultoRepository.saveAll(loteOrigen.getBultos());
        movimientoRepository.save(movReversoModifRecall);

        // Restaurar análisis cancelado si existe
        if (loteOrigen.getUltimoAnalisis() != null &&
            loteOrigen.getUltimoAnalisis().getDictamen() == com.mb.conitrack.enums.DictamenEnum.CANCELADO) {
            loteOrigen.getUltimoAnalisis().setDictamen(null);
            analisisRepository.save(loteOrigen.getUltimoAnalisis());
        }

        return DTOUtils.fromLoteEntity(loteRepository.save(loteOrigen));
    }

    /**
     * Restaura el estado de un bulto según su cantidad
     */
    void restaurarEstadoBulto(Bulto bulto) {
        if (bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
            bulto.setEstado(EstadoEnum.NUEVO);
        } else {
            if (java.math.BigDecimal.ZERO.compareTo(bulto.getCantidadActual()) == 0) {
                bulto.setEstado(EstadoEnum.CONSUMIDO);
            } else {
                bulto.setEstado(EstadoEnum.EN_USO);
            }
        }
    }

    /**
     * Restaura el estado de un lote según su cantidad
     */
    void restaurarEstadoLote(Lote lote) {
        if (lote.getCantidadInicial().compareTo(lote.getCantidadActual()) == 0) {
            lote.setEstado(EstadoEnum.NUEVO);
        } else {
            if (java.math.BigDecimal.ZERO.compareTo(lote.getCantidadActual()) == 0) {
                lote.setEstado(EstadoEnum.CONSUMIDO);
            } else {
                lote.setEstado(EstadoEnum.EN_USO);
            }
        }
    }
}
