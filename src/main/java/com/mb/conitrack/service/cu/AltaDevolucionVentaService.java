package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.BultoDTO;
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

//***********CU13 ALTA: DEVOLUCION VENTA***********
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

        // 2) Agrupar trazas seleccionadas por nro de bulto
        final Map<Integer, List<TrazaDTO>> trazaDTOporBultoMap = dto.getTrazaDTOs().stream()
            .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));

        final Lote loteDevolucionGuardado = loteRepository.save(loteAltaDevolucion);

        for (Map.Entry<Integer, List<TrazaDTO>> trazaDTOporBulto : trazaDTOporBultoMap.entrySet()) {

            final Integer trazaDTOEnNroBulto = trazaDTOporBulto.getKey();

            final List<TrazaDTO> trazasDTOsPorBulto = trazaDTOporBulto.getValue();

            final Bulto bultoOriginal = loteOrigen.getBultoByNro(trazaDTOEnNroBulto);

            Bulto bulto = new Bulto();
            bulto.setNroBulto(trazaDTOEnNroBulto);
            bulto.setLote(loteDevolucionGuardado);
            bulto.setCantidadInicial(BigDecimal.valueOf(trazasDTOsPorBulto.size()));
            bulto.setCantidadActual(BigDecimal.valueOf(trazasDTOsPorBulto.size()));
            bulto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            bulto.setEstado(DEVUELTO);
            bulto.setActivo(TRUE);

            loteAltaDevolucion.getBultos().add(bulto);

            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movDevolucionVenta)
                .bulto(bulto)
                // Cantidad = cantidad de trazas devueltas (campo NOT NULL) — no impacta stock
                .cantidad(BigDecimal.valueOf(trazasDTOsPorBulto.size()))
                .unidadMedida(UnidadMedidaEnum.UNIDAD)
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

        if (!validarTrazasDevolucion(dto, bindingResult)) {
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
        clone.setLoteOrigen(original.getRootLote());

        clone.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        clone.setFechaIngreso(dto.getFechaYHoraCreacion().toLocalDate());
        clone.setCodigoLote(original.getRootCodigoLote() +
            "_D_" +
            (original.duplicateNumber() + 1));

        clone.setProducto(original.getProducto());
        clone.setProveedor(original.getProveedor());
        clone.setFabricante(original.getFabricante());
        clone.setPaisOrigen(original.getPaisOrigen());
        clone.setOrdenProduccionOrigen(original.getOrdenProduccionOrigen());
        clone.setLoteProveedor(original.getLoteProveedor());
        clone.setFechaVencimientoProveedor(original.getFechaVencimientoVigente());
        clone.setEstado(EstadoEnum.DEVUELTO);
        clone.setDictamen(DictamenEnum.DEVOLUCION_CLIENTES);
        clone.setDetalleConservacion(original.getDetalleConservacion());
        clone.setActivo(TRUE);

        clone.setCantidadInicial(BigDecimal.valueOf(dto.getTrazaDTOs().size()));
        clone.setCantidadActual(BigDecimal.valueOf(dto.getTrazaDTOs().size())); // o: original.getCantidadInicial()
        clone.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        for (BultoDTO bultoDTO : dto.getBultoDTOS()) {
            Bulto bulto = new Bulto();
            bulto.setNroBulto(bultoDTO.getNroBulto());
            bulto.setCantidadInicial(dto.getCantidad());
            bulto.setCantidadActual(dto.getCantidad());
            bulto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            bulto.setEstado(DEVUELTO);
            bulto.setActivo(TRUE);

            bulto.setLote(clone);
            clone.getBultos().add(bulto);
        }
        clone.setBultosTotales(dto.getBultoDTOS().size());

        return clone;
    }

}
