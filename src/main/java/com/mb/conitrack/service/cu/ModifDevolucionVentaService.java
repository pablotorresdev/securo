package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntity;
import static com.mb.conitrack.enums.EstadoEnum.DEVUELTO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;

//***********CU23 ALTA: DEVOLUCION VENTA***********
@Service
public class ModifDevolucionVentaService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirDevolucionVenta(final MovimientoDTO dto) {

        final Lote lote = loteRepository.findFirstByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        // 1) Crear el movimiento (MODIFICACIÓN) y vincular al movimiento de venta origen
        final Movimiento movDevolucionVenta = persistirMovimientoDevolucionVenta(dto, lote);

        // 2) Agrupar trazas seleccionadas por nro de bulto
        final Map<Integer, List<TrazaDTO>> trazaDTOporBultoMap = dto.getTrazaDTOs().stream()
            .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));

        // 3) Por cada bulto afectado, crear UN DetalleMovimiento y colgar las trazas devueltas
        for (Map.Entry<Integer, List<TrazaDTO>> trazaDTOporBulto : trazaDTOporBultoMap.entrySet()) {
            final Integer trazaDTOnroBulto = trazaDTOporBulto.getKey();
            final List<TrazaDTO> trazasDTOsPorBulto = trazaDTOporBulto.getValue();

            final Bulto bulto = lote.getBultoByNro(trazaDTOnroBulto);

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
                final Traza trazaBulto = bulto.getTrazaByNro(t.getNroTraza());
                trazaBulto.setEstado(DEVUELTO);
                det.getTrazas().add(trazaBulto);
            }

            // 4) Si TODAS las trazas del bulto están DEVUELTO ⇒ bulto = DEVUELTO
            final boolean bultoDevuelto = bulto.getTrazas().stream()
                .allMatch(tr -> tr.getEstado() == DEVUELTO);
            if (bultoDevuelto) {
                bulto.setEstado(DEVUELTO);
            }

            // Si no tenés cascade MERGE/UPDATE desde Lote→Bultos, podés guardar el bulto:
            bultoRepository.save(bulto);
        }

        // 5) Si TODOS los bultos del lote están DEVUELTO ⇒ lote = DEVUELTO
        final boolean loteDevuelto = lote.getBultos().stream()
            .allMatch(b -> b.getEstado() == DEVUELTO);
        if (loteDevuelto) {
            lote.setEstado(DEVUELTO);
        }

        movDevolucionVenta.setCantidad(BigDecimal.valueOf(dto.getTrazaDTOs().size()));
        movDevolucionVenta.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        // 6) Persistir cambios:
        //    - Detalles + join table se guardan via cascade al guardar el movimiento
        lote.getMovimientos().add(movimientoRepository.save(movDevolucionVenta));
        //    - Lote/bultos/trazas son entidades administradas en la sesión; igual podés asegurar el flush:
        return fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoDevolucionVenta(MovimientoDTO dto, Lote lote) {
        final Movimiento movimientoDevolucionVenta = createMovimientoModificacion(dto, lote);
        movimientoDevolucionVenta.setFecha(dto.getFechaMovimiento());
        movimientoDevolucionVenta.setMotivo(MotivoEnum.DEVOLUCION_VENTA);

        final Movimiento movimientoOrigen = movimientoRepository.findByCodigoMovimientoAndActivoTrue(
                dto.getCodigoMovimientoOrigen())
            .orElseThrow(() -> new IllegalArgumentException("El movmiento de origen no existe."));
        movimientoDevolucionVenta.setMovimientoOrigen(movimientoOrigen);

        return movimientoRepository.save(movimientoDevolucionVenta);
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

}
