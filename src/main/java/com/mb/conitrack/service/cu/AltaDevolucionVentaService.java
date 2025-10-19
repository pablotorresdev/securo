package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
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

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntity;
import static com.mb.conitrack.enums.EstadoEnum.DEVUELTO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoDevolucion;
import static java.lang.Boolean.TRUE;

//***********CU23 ALTA: DEVOLUCION VENTA***********
@Service
public class AltaDevolucionVentaService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirDevolucionVenta(final MovimientoDTO dto) {

        final Lote loteOrigen = loteRepository.findFirstByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        Lote loteAltaDevolucion = crearLoteDevolucion(loteOrigen, dto);

        final Movimiento movDevolucionVenta = createMovimientoAltaIngresoDevolucion(dto, loteAltaDevolucion);

        final Movimiento movimientoOrigen = movimientoRepository.findByCodigoMovimientoAndActivoTrue(
                dto.getCodigoMovimientoOrigen())
            .orElseThrow(() -> new IllegalArgumentException("El movmiento de origen no existe."));
        movDevolucionVenta.setMovimientoOrigen(movimientoOrigen);

        final Lote loteDevolucionGuardado = loteRepository.save(loteAltaDevolucion);

        if (TRUE.equals(loteOrigen.getTrazado())) {

            // 2) Agrupar trazas seleccionadas por nro de bulto
            final Map<Integer, List<TrazaDTO>> trazaDTOporBultoMap = dto.getTrazaDTOs().stream()
                .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));

            for (Map.Entry<Integer, List<TrazaDTO>> trazaDTOporBulto : trazaDTOporBultoMap.entrySet()) {

                final Integer trazaDTOEnNroBulto = trazaDTOporBulto.getKey();

                final List<TrazaDTO> trazasDTOsPorBulto = trazaDTOporBulto.getValue();

                final Bulto bultoOriginal = loteOrigen.getBultoByNro(trazaDTOEnNroBulto);

                Bulto bulto = loteAltaDevolucion.getBultoByNro(trazaDTOEnNroBulto);
                final DetalleMovimiento det = DetalleMovimiento.builder()
                    .movimiento(movDevolucionVenta)
                    .bulto(bulto)
                    // Cantidad = cantidad de trazas devueltas (campo NOT NULL) — no impacta stock
                    .cantidad(BigDecimal.valueOf(trazasDTOsPorBulto.size()))
                    .unidadMedida(UnidadMedidaEnum.UNIDAD)
                    .activo(TRUE)
                    .build();

                // Colgar el detalle AL movimiento (habilita cascade para insertar detalle + join table)
                movDevolucionVenta.getDetalles().add(det);

                // Marcar trazas como DEVUELTO y vincularlas al detalle (trazas_detalles)
                for (TrazaDTO t : trazasDTOsPorBulto) {
                    final Traza trazaBulto = bultoOriginal.getTrazaByNro(t.getNroTraza());
                    trazaBulto.setLote(loteAltaDevolucion);
                    trazaBulto.setBulto(bulto);
                    trazaBulto.setEstado(DEVUELTO);
                    det.getTrazas().add(trazaBulto);
                }

                trazaRepository.saveAll(det.getTrazas());
                bultoRepository.save(bulto);
            }
            movimientoRepository.save(movDevolucionVenta);

            loteAltaDevolucion.setBultosTotales(trazaDTOporBultoMap.size());
        } else {

            // 2) Agrupar trazas seleccionadas por nro de bulto
            for (Bulto bultoDevolucion : loteAltaDevolucion.getBultos()) {

                final DetalleMovimiento det = DetalleMovimiento.builder()
                    .movimiento(movDevolucionVenta)
                    .bulto(bultoDevolucion)
                    // Cantidad = cantidad de trazas devueltas (campo NOT NULL) — no impacta stock
                    .cantidad(bultoDevolucion.getCantidadActual())
                    .unidadMedida(UnidadMedidaEnum.UNIDAD)
                    .activo(TRUE)
                    .build();

                // Colgar el detalle AL movimiento (habilita cascade para insertar detalle + join table)
                movDevolucionVenta.getDetalles().add(det);

                bultoRepository.save(bultoDevolucion);
            }
            movimientoRepository.save(movDevolucionVenta);

            loteAltaDevolucion.setBultosTotales(loteAltaDevolucion.getBultos().size());
        }

        return fromLoteEntity(loteRepository.save(loteDevolucionGuardado));
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

        //TODO: Esto hay que cambiarlo
        if (TRUE.equals(original.getTrazado())) {

            final Map<Integer, List<TrazaDTO>> trazaDTOporBultoMap = dto.getTrazaDTOs().stream()
                .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));

            for (Map.Entry<Integer, List<TrazaDTO>> trazaDTOporBulto : trazaDTOporBultoMap.entrySet()) {

                final Integer trazaDTOEnNroBulto = trazaDTOporBulto.getKey();

                final List<TrazaDTO> trazasDTOsPorBulto = trazaDTOporBulto.getValue();

                Bulto bulto = new Bulto();
                bulto.setNroBulto(trazaDTOEnNroBulto);
                bulto.setCantidadInicial(BigDecimal.valueOf(trazasDTOsPorBulto.size()));
                bulto.setCantidadActual(BigDecimal.valueOf(trazasDTOsPorBulto.size()));
                bulto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
                bulto.setEstado(DEVUELTO);
                bulto.setActivo(TRUE);
                bulto.setLote(clone);

                clone.getBultos().add(bulto);

            }
            clone.setCantidadInicial(BigDecimal.valueOf(dto.getTrazaDTOs().size()));
            clone.setCantidadActual(BigDecimal.valueOf(dto.getTrazaDTOs().size()));
        } else {
            final List<DetalleMovimientoDTO> detalleMovimientoDTOs = dto.getDetalleMovimientoDTOs();
            BigDecimal cantidad = BigDecimal.ZERO;
            for (DetalleMovimientoDTO detalleMovimientoDTO : detalleMovimientoDTOs) {
                cantidad = cantidad.add(detalleMovimientoDTO.getCantidad() != null
                    ? detalleMovimientoDTO.getCantidad()
                    : BigDecimal.ZERO);

                Bulto bulto = new Bulto();
                bulto.setNroBulto(detalleMovimientoDTO.getNroBulto());
                bulto.setCantidadInicial(detalleMovimientoDTO.getCantidad() != null
                    ? detalleMovimientoDTO.getCantidad()
                    : BigDecimal.ZERO);
                bulto.setCantidadActual(detalleMovimientoDTO.getCantidad() != null
                    ? detalleMovimientoDTO.getCantidad()
                    : BigDecimal.ZERO);
                bulto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
                bulto.setEstado(DEVUELTO);
                bulto.setActivo(TRUE);
                bulto.setLote(clone);

                clone.getBultos().add(bulto);
            }
            clone.setCantidadInicial(cantidad);
            clone.setCantidadActual(cantidad); // o: original.getCantidadInicial()
        }

        clone.setBultosTotales(dto.getDetalleMovimientoDTOs().size());
        return clone;
    }

}
