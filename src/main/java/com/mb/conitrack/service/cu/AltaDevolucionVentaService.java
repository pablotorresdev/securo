package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DetalleMovimientoDTO;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;
import static com.mb.conitrack.enums.EstadoEnum.DEVUELTO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaDevolucion;
import static java.lang.Boolean.TRUE;

//***********CU23 ALTA: DEVOLUCION VENTA***********
@Service
public class AltaDevolucionVentaService extends AbstractCuService {

    private static Bulto initBulto(final Lote clone) {
        Bulto bulto = new Bulto();
        bulto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        bulto.setEstado(DEVUELTO);
        bulto.setActivo(TRUE);
        bulto.setLote(clone);
        return bulto;
    }

    @Transactional
    public List<LoteDTO> persistirDevolucionVenta(final MovimientoDTO dto) {

        final Lote loteVentaOrigen = loteRepository.findFirstByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        Lote loteAltaDevolucion = crearLoteDevolucion(loteVentaOrigen, dto);

        final Movimiento movDevolucionVenta = createMovimientoAltaDevolucion(dto, loteAltaDevolucion);

        final Movimiento movimientoOrigen = movimientoRepository.findByCodigoMovimientoAndActivoTrue(
                dto.getCodigoMovimientoOrigen())
            .orElseThrow(() -> new IllegalArgumentException("El movmiento de origen no existe."));
        movDevolucionVenta.setMovimientoOrigen(movimientoOrigen);

        final Lote loteDevolucionGuardado = loteRepository.save(loteAltaDevolucion);
        if (TRUE.equals(loteVentaOrigen.getTrazado())) {
            altaDevolucionUnidadesTrazadas(dto, loteVentaOrigen, loteAltaDevolucion, movDevolucionVenta);
        } else {
            altaDevolucionUnidadesPorBulto(loteAltaDevolucion, movDevolucionVenta);
        }

        List<Lote> lotes = new ArrayList<>();
        loteRepository.findById(loteRepository.save(loteDevolucionGuardado).getId()).ifPresent(lotes::add);
        loteRepository.findById(loteVentaOrigen.getId()).ifPresent(lotes::add);
        //TODO: corregir la informacion de confirmacion de la devolucion
        return fromLoteEntities(lotes);
    }

    private void altaDevolucionUnidadesPorBulto(final Lote loteAltaDevolucion, final Movimiento movDevolucionVenta) {
        BigDecimal cantidadMovimiento = BigDecimal.ZERO;
        for (Bulto bultoDevolucion : loteAltaDevolucion.getBultos()) {

            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movDevolucionVenta)
                .bulto(bultoDevolucion)
                // Cantidad = cantidad de trazas devueltas (campo NOT NULL) — no impacta stock
                .cantidad(bultoDevolucion.getCantidadActual())
                .unidadMedida(UnidadMedidaEnum.UNIDAD)
                .activo(TRUE)
                .build();

            cantidadMovimiento = cantidadMovimiento.add(bultoDevolucion.getCantidadActual());

            // Colgar el detalle AL movimiento (habilita cascade para insertar detalle + join table)
            movDevolucionVenta.getDetalles().add(det);

            bultoRepository.save(bultoDevolucion);
        }
        movDevolucionVenta.setCantidad(cantidadMovimiento);
        movDevolucionVenta.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        movimientoRepository.save(movDevolucionVenta);

        loteAltaDevolucion.setBultosTotales(loteAltaDevolucion.getBultos().size());
    }

    private void altaDevolucionUnidadesTrazadas(
        final MovimientoDTO dto,
        final Lote loteVentaOrigen,
        final Lote loteAltaDevolucion,
        final Movimiento movDevolucionVenta) {
        BigDecimal cantidadMovimiento = BigDecimal.ZERO;

        // 2) Agrupar trazas seleccionadas por nro de bulto
        final Map<Integer, List<TrazaDTO>> trazasDTOporBultoMap = dto.getTrazaDTOs().stream()
            .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));

        for (Map.Entry<Integer, List<TrazaDTO>> mapEntry : trazasDTOporBultoMap.entrySet()) {

            final Integer nroBulto = mapEntry.getKey();
            final List<TrazaDTO> trazasDTOsPorNroBulto = mapEntry.getValue();

            final Bulto bultoOriginal = loteVentaOrigen.getBultoByNro(nroBulto);
            Bulto bultoDevolucion = loteAltaDevolucion.getBultoByNro(nroBulto);

            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movDevolucionVenta)
                .bulto(bultoDevolucion)
                // Cantidad = cantidad de trazas devueltas (campo NOT NULL) — no impacta stock
                .cantidad(BigDecimal.valueOf(trazasDTOsPorNroBulto.size()))
                .unidadMedida(UnidadMedidaEnum.UNIDAD)
                .activo(TRUE)
                .build();

            cantidadMovimiento = cantidadMovimiento.add(BigDecimal.valueOf(trazasDTOsPorNroBulto.size()));

            // Colgar el detalle AL movimiento (habilita cascade para insertar detalle + join table)
            movDevolucionVenta.getDetalles().add(det);

            // Marcar trazas como DEVUELTO y vincularlas al detalle (trazas_detalles)
            for (TrazaDTO t : trazasDTOsPorNroBulto) {
                final Traza trazaBulto = bultoOriginal.getTrazaByNro(t.getNroTraza());
                trazaBulto.setLote(loteAltaDevolucion);
                trazaBulto.setBulto(bultoDevolucion);
                trazaBulto.setEstado(DEVUELTO);
                det.getTrazas().add(trazaBulto);
            }

            trazaRepository.saveAll(det.getTrazas());
            bultoRepository.save(bultoDevolucion);
        }
        movDevolucionVenta.setCantidad(cantidadMovimiento);
        movDevolucionVenta.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        movimientoRepository.save(movDevolucionVenta);

        loteAltaDevolucion.setBultosTotales(trazasDTOporBultoMap.size());
    }

    @Transactional
    public boolean validarDevolucionVentaInput(
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

    @Transactional
    public Lote crearLoteDevolucion(Lote original, MovimientoDTO dto) {

        Lote clone = initLoteDevolucion(original, dto);
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

    private Lote initLoteDevolucion(final Lote original, final MovimientoDTO dto) {
        Lote clone = new Lote();

        clone.setId(null);
        clone.setLoteOrigen(original);

        clone.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        clone.setFechaIngreso(dto.getFechaYHoraCreacion().toLocalDate());

        final List<Lote> lotesByLoteOrigen = loteRepository.findLotesByLoteOrigen(original.getCodigoLote());

        clone.setCodigoLote(original.getCodigoLote() +
            "_D_" +
            (lotesByLoteOrigen.size() + 1));

        clone.setTrazado(original.getTrazado());
        clone.setProducto(original.getProducto());
        clone.setProveedor(original.getProveedor());
        clone.setFabricante(original.getFabricante());
        clone.setPaisOrigen(original.getPaisOrigen());
        clone.setOrdenProduccionOrigen(original.getOrdenProduccionOrigen());
        clone.setLoteProveedor(original.getLoteProveedor());
        clone.setFechaVencimientoProveedor(original.getFechaVencimientoVigente());
        clone.setEstado(EstadoEnum.DEVUELTO);
        clone.setDictamen(DictamenEnum.DEVOLUCION_CLIENTES);
        clone.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        clone.setDetalleConservacion(original.getDetalleConservacion());
        clone.setActivo(TRUE);
        return clone;
    }

}
