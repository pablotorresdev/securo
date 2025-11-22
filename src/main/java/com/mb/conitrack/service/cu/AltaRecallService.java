package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DetalleMovimientoDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.*;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.mb.conitrack.enums.EstadoEnum.RECALL;
import static com.mb.conitrack.utils.MovimientoAltaUtils.createMovimientoAltaRecall;
import static java.lang.Boolean.TRUE;

/**
 * Servicio especializado para crear lotes de retiro de mercado (recall).
 * Maneja la operación ALTA del CU24 - Retiro de Mercado.
 *
 * Este servicio:
 * - Crea nuevos lotes derivados con sufijo _R_N
 * - Transfiere trazas desde lote de venta original
 * - Maneja productos trazados y no trazados
 */
@Service
public class AltaRecallService extends AbstractCuService {

    /**
     * Procesa la creación de un nuevo lote recall a partir de una venta.
     */
    @Transactional
    public void procesarAltaRecall(
            final MovimientoDTO dto,
            final Lote loteOrigenRecall,
            final Movimiento movimientoVentaOrigen,
            final List<Lote> result,
            User currentUser) {

        Lote loteAltaRecall = crearLoteRecall(loteOrigenRecall, dto);

        final Movimiento movimientoAltaRecall = createMovimientoAltaRecall(dto, loteAltaRecall, currentUser);
        movimientoAltaRecall.setMovimientoOrigen(movimientoVentaOrigen);

        final Lote loteRecallGuardado = loteRepository.save(loteAltaRecall);

        if (TRUE.equals(loteOrigenRecall.getTrazado())) {
            altaRecallUnidadesTrazadas(dto, loteOrigenRecall, loteAltaRecall, movimientoAltaRecall);
        } else {
            altaRecallUnidadesPorBulto(loteAltaRecall, movimientoAltaRecall);
        }

        loteRepository.findById(loteRepository.save(loteRecallGuardado).getId()).ifPresent(result::add);
    }

    /**
     * Procesa recall para productos no trazados (por bulto).
     */
    void altaRecallUnidadesPorBulto(final Lote loteAltaRecall, final Movimiento movimientoAltaRecall) {
        BigDecimal cantidadMovimiento = BigDecimal.ZERO;
        for (Bulto bultoRecall : loteAltaRecall.getBultos()) {

            final DetalleMovimiento det = DetalleMovimiento.builder()
                    .movimiento(movimientoAltaRecall)
                    .bulto(bultoRecall)
                    // Cantidad = cantidad de trazas devueltas (campo NOT NULL) — no impacta stock
                    .cantidad(bultoRecall.getCantidadActual())
                    .unidadMedida(UnidadMedidaEnum.UNIDAD)
                    .activo(TRUE)
                    .build();

            cantidadMovimiento = cantidadMovimiento.add(bultoRecall.getCantidadActual());

            // Colgar el detalle AL movimiento (habilita cascade para insertar detalle + join table)
            movimientoAltaRecall.getDetalles().add(det);

            bultoRepository.save(bultoRecall);
        }
        movimientoAltaRecall.setCantidad(cantidadMovimiento);
        movimientoAltaRecall.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        movimientoRepository.save(movimientoAltaRecall);

        loteAltaRecall.setBultosTotales(loteAltaRecall.getBultos().size());
    }

    /**
     * Procesa recall para productos trazados (transferencia de trazas individuales).
     */
    void altaRecallUnidadesTrazadas(
            final MovimientoDTO dto,
            final Lote loteOrigenRecall,
            final Lote loteAltaRecall,
            final Movimiento movimientoAltaRecall) {
        BigDecimal cantidadMovimiento = BigDecimal.ZERO;
        final Map<Integer, List<TrazaDTO>> trazasDTOporBultoMap = dto.getTrazaDTOs().stream()
                .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));

        for (Entry<Integer, List<TrazaDTO>> mapEntry : trazasDTOporBultoMap.entrySet()) {

            final Integer nroBulto = mapEntry.getKey();
            final List<TrazaDTO> trazasDTOsPorNroBulto = mapEntry.getValue();

            final Bulto bultoOriginal = loteOrigenRecall.getBultoByNro(nroBulto);
            final Bulto bultoRecall = loteAltaRecall.getBultoByNro(nroBulto);

            final DetalleMovimiento det = DetalleMovimiento.builder()
                    .movimiento(movimientoAltaRecall)
                    .bulto(bultoRecall)
                    .cantidad(BigDecimal.valueOf(trazasDTOsPorNroBulto.size()))
                    .unidadMedida(UnidadMedidaEnum.UNIDAD)
                    .activo(TRUE)
                    .build();

            cantidadMovimiento = cantidadMovimiento.add(BigDecimal.valueOf(trazasDTOsPorNroBulto.size()));

            movimientoAltaRecall.getDetalles().add(det);

            for (TrazaDTO t : trazasDTOsPorNroBulto) {
                final Traza trazaBulto = bultoOriginal.getTrazaByNro(t.getNroTraza());
                trazaBulto.setLote(loteAltaRecall);
                trazaBulto.setBulto(bultoRecall);
                trazaBulto.setEstado(RECALL);
                det.getTrazas().add(trazaBulto);
            }
            trazaRepository.saveAll(det.getTrazas());
            bultoRepository.save(bultoRecall);
        }
        movimientoAltaRecall.setCantidad(cantidadMovimiento);
        movimientoAltaRecall.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        movimientoRepository.save(movimientoAltaRecall);

        loteAltaRecall.setBultosTotales(trazasDTOporBultoMap.size());
    }

    /**
     * Crea un nuevo lote recall clonando datos del lote original de venta.
     * Asigna código con sufijo _R_N y configura estado RECALL.
     */
    @Transactional
    public Lote crearLoteRecall(final Lote original, final MovimientoDTO dto) {

        Lote clone = initLoteRecall(original, dto);
        if (TRUE.equals(original.getTrazado())) {
            final Map<Integer, List<TrazaDTO>> trazaDTOporBultoMap = dto.getTrazaDTOs().stream()
                    .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));
            for (Map.Entry<Integer, List<TrazaDTO>> trazaDTOporBulto : trazaDTOporBultoMap.entrySet()) {

                final Integer trazaDTOEnNroBulto = trazaDTOporBulto.getKey();

                final List<TrazaDTO> trazasDTOsPorBulto = trazaDTOporBulto.getValue();

                Bulto bulto = initBulto(clone);
                bulto.setNroBulto(trazaDTOEnNroBulto);
                bulto.setCantidadInicial(BigDecimal.valueOf(trazasDTOsPorBulto.size()));
                bulto.setCantidadActual(BigDecimal.valueOf(trazasDTOsPorBulto.size()));

                clone.getBultos().add(bulto);
            }
            clone.setCantidadInicial(BigDecimal.valueOf(dto.getTrazaDTOs().size()));
            clone.setCantidadActual(BigDecimal.valueOf(dto.getTrazaDTOs().size()));
        } else {
            final List<DetalleMovimientoDTO> detalleMovimientoDTOs = dto.getDetalleMovimientoDTOs();
            BigDecimal cantidad = BigDecimal.ZERO;

            for (DetalleMovimientoDTO detalleMovimientoDTO : detalleMovimientoDTOs) {

                BigDecimal cantidadDetalle = detalleMovimientoDTO.getCantidad() != null
                        ? detalleMovimientoDTO.getCantidad()
                        : BigDecimal.ZERO;

                if (BigDecimal.ZERO.compareTo(cantidadDetalle) == 0) {
                    continue;
                }

                cantidad = cantidad.add(cantidadDetalle);

                Bulto bulto = initBulto(clone);
                bulto.setNroBulto(detalleMovimientoDTO.getNroBulto());
                bulto.setCantidadInicial(cantidadDetalle);
                bulto.setCantidadActual(cantidadDetalle);

                clone.getBultos().add(bulto);
            }
            clone.setCantidadInicial(cantidad);
            clone.setCantidadActual(cantidad);
        }

        clone.setBultosTotales(clone.getBultos().size());
        return clone;
    }

    /**
     * Inicializa un bulto para el lote recall.
     */
    static Bulto initBulto(final Lote clone) {
        Bulto bulto = new Bulto();
        bulto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        bulto.setEstado(RECALL);
        bulto.setActivo(TRUE);
        bulto.setLote(clone);
        return bulto;
    }

    /**
     * Inicializa el lote recall con datos base clonados del original.
     */
    Lote initLoteRecall(final Lote original, final MovimientoDTO dto) {
        Lote clone = new Lote();

        clone.setId(null);
        clone.setLoteOrigen(original);

        clone.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        clone.setFechaIngreso(dto.getFechaYHoraCreacion().toLocalDate());

        final List<Lote> lotesByLoteOrigen = loteRepository.findLotesByLoteOrigen(original.getCodigoLote());

        clone.setCodigoLote(original.getCodigoLote() +
                "_R_" +
                (lotesByLoteOrigen.size() + 1));

        clone.setTrazado(original.getTrazado());
        clone.setProducto(original.getProducto());
        clone.setProveedor(original.getProveedor());
        clone.setFabricante(original.getFabricante());
        clone.setPaisOrigen(original.getPaisOrigen());
        clone.setOrdenProduccionOrigen(original.getOrdenProduccionOrigen());
        clone.setLoteProveedor(original.getLoteProveedor());
        clone.setFechaVencimientoProveedor(original.getFechaVencimientoVigente());
        clone.setEstado(RECALL);
        clone.setDictamen(DictamenEnum.RETIRO_MERCADO);
        clone.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        clone.setDetalleConservacion(original.getDetalleConservacion());
        clone.setActivo(TRUE);
        return clone;
    }
}
