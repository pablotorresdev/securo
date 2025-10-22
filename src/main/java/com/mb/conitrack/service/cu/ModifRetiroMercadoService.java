package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DetalleMovimientoDTO;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;
import static com.mb.conitrack.enums.EstadoEnum.DISPONIBLE;
import static com.mb.conitrack.enums.EstadoEnum.RECALL;
import static com.mb.conitrack.utils.MovimientoEntityUtils.crearMovimientoModifRecall;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaRecall;
import static java.lang.Boolean.TRUE;

@Service
public class ModifRetiroMercadoService extends AbstractCuService {

    private static Bulto initBulto(final Lote clone) {
        Bulto bulto = new Bulto();
        bulto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        bulto.setEstado(RECALL);
        bulto.setActivo(TRUE);
        bulto.setLote(clone);
        return bulto;
    }

    //***********CU24 ALTA/MODIF: RECALL***********

    @Transactional
    public List<LoteDTO> persistirRetiroMercado(final MovimientoDTO dto) {
        List<Lote> result = new ArrayList<>();

        final Lote loteOrigenRecall = loteRepository.findFirstByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Movimiento movimientoVentaOrigen = movimientoRepository.findByCodigoMovimientoAndActivoTrue(
                dto.getCodigoMovimientoOrigen())
            .orElseThrow(() -> new IllegalArgumentException("El movmiento de origen no existe."));

        //************ALTA RECALL************
        procesarAltaRecall(dto, loteOrigenRecall, movimientoVentaOrigen, result);

        //************MODIFICACION RECALL************
        procesarModificacionRecall(dto, loteOrigenRecall, movimientoVentaOrigen, result);
        return fromLoteEntities(result);
    }

    private void procesarModificacionRecall(
        final MovimientoDTO dto,
        final Lote loteOrigenRecall,
        final Movimiento movimientoVentaOrigen,
        final List<Lote> result) {
        final Movimiento movimientoModifRecall = crearMovimientoModifRecall(dto);
        movimientoModifRecall.setDictamenInicial(loteOrigenRecall.getDictamen());
        movimientoModifRecall.setMovimientoOrigen(movimientoVentaOrigen);
        movimientoModifRecall.setLote(loteOrigenRecall);

        if (loteOrigenRecall.getEstado() != RECALL) {
            if (TRUE.equals(loteOrigenRecall.getTrazado())) {
                for (Bulto bulto : loteOrigenRecall.getBultos()) {
                    final Set<Traza> trazas = bulto.getTrazas();
                    final List<Traza> trazasRecall = new ArrayList<>();
                    boolean recall = false;
                    for (Traza tr : trazas) {
                        if (tr.getEstado() != DISPONIBLE) {
                            continue;
                        }

                        tr.setEstado(RECALL);
                        trazasRecall.add(tr);
                        recall = true;
                    }
                    if (recall) {
                        bulto.setEstado(RECALL);
                    }
                    trazaRepository.saveAll(trazasRecall);
                }
            } else {
                for (Bulto bultoRecall : loteOrigenRecall.getBultos()) {
                    if (bultoRecall.getCantidadActual().compareTo(BigDecimal.ZERO) > 0) {
                        bultoRecall.setEstado(RECALL);
                    }
                }
            }

            final Movimiento savedMovModifRecall = movimientoRepository.save(movimientoModifRecall);

            bultoRepository.saveAll(loteOrigenRecall.getBultos());

            loteOrigenRecall.setEstado(RECALL);
            loteOrigenRecall.getMovimientos().add(savedMovModifRecall);

            for (Analisis analisis : loteOrigenRecall.getAnalisisList()) {
                if (analisis.getDictamen() == null) {
                    analisis.setDictamen(DictamenEnum.ANULADO);
                    analisisRepository.save(analisis);
                }
            }
        }
        loteRepository.findById(loteRepository.save(loteOrigenRecall).getId()).ifPresent(result::add);
    }

    private void procesarAltaRecall(
        final MovimientoDTO dto,
        final Lote loteOrigenRecall,
        final Movimiento movimientoVentaOrigen,
        final List<Lote> result) {
        Lote loteAltaRecall = crearLoteRecall(loteOrigenRecall, dto);

        final Movimiento movimientoAltaRecall = createMovimientoAltaRecall(dto, loteOrigenRecall);
        movimientoAltaRecall.setMovimientoOrigen(movimientoVentaOrigen);

        final Lote loteRecallGuardado = loteRepository.save(loteAltaRecall);

        if (TRUE.equals(loteOrigenRecall.getTrazado())) {
            altaUnidadesTrazadas(dto, loteOrigenRecall, loteAltaRecall, movimientoAltaRecall);
        } else {
            altaUnidadesPorBulto(loteAltaRecall, movimientoAltaRecall);
        }

        loteRepository.findById(loteRepository.save(loteRecallGuardado).getId()).ifPresent(result::add);
    }

    private void altaUnidadesPorBulto(final Lote loteAltaRecall, final Movimiento movimientoAltaRecall) {
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

    private void altaUnidadesTrazadas(
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
            clone.setCantidadActual(cantidad); // o: original.getCantidadInicial()
        }

        clone.setBultosTotales(clone.getBultos().size());
        return clone;
    }

    @Transactional
    public boolean validarRetiroMercadoInput(
        final @Valid MovimientoDTO dto,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        final Optional<Lote> lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (!validarFechaMovimientoPosteriorIngresoLote(dto, lote.get().getFechaIngreso(), bindingResult)) {
            return false;
        }

        if (TRUE.equals(lote.get().getTrazado()) && !validarTrazasDevolucion(dto, bindingResult)) {
            return false;
        }

        final Optional<Movimiento> movOrigen = movimientoRepository.findByCodigoMovimientoAndActivoTrue(
            dto.getCodigoMovimientoOrigen());

        if (movOrigen.isEmpty()) {
            bindingResult.rejectValue("codigoMovimientoOrigen", "", "No se encontro el movimiento de venta origen");
            return true;
        }

        return validarMovimientoOrigen(dto, bindingResult, movOrigen.get());
    }

    private Lote initLoteRecall(final Lote original, final MovimientoDTO dto) {
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
