package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

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
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntity;
import static com.mb.conitrack.enums.EstadoEnum.CONSUMIDO;
import static com.mb.conitrack.enums.EstadoEnum.RECALL;
import static com.mb.conitrack.enums.EstadoEnum.VENDIDO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.crearMovimientoBajaRecall;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;

//***********CU14 ALTA/MODIF: RECALL***********
@Service
public class ModifRetiroMercadoService extends AbstractCuService {

    //TODO: fix, no aplica a todas las trazas y algunas quedan en disponibles
    @Transactional
    public LoteDTO persistirRetiroMercado(final MovimientoDTO dto) {

        final Lote lote = loteRepository.findFirstByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        persistirModifTrazasDevueltas(dto, lote);
        persistirBajaStockRecall(dto, lote);

        return fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoRetiroMercado(MovimientoDTO dto, Lote lote) {
        final Movimiento movimientoDevolucionVenta = createMovimientoModificacion(dto, lote);
        movimientoDevolucionVenta.setFecha(dto.getFechaMovimiento());
        movimientoDevolucionVenta.setMotivo(MotivoEnum.RETIRO_MERCADO);

        return movimientoRepository.save(movimientoDevolucionVenta);
    }

    @Transactional
    public boolean validarRetiroMercadoInput(
        final @Valid MovimientoDTO dto,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        final Optional<LocalDate> fechaIngresoLote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .map(Lote::getFechaIngreso);

        if (fechaIngresoLote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (!validarFechaMovimientoPosteriorIngresoLote(dto, fechaIngresoLote.get(), bindingResult)) {
            return false;
        }

        return validarTrazasDevolucion(dto, bindingResult);
    }

    @Transactional
    void persistirBajaStockRecall(final MovimientoDTO dto, final Lote lote) {
        final Movimiento movimientoBajaRecall = crearMovimientoBajaRecall(dto);
        movimientoBajaRecall.setDictamenInicial(lote.getDictamen());
        movimientoBajaRecall.setCantidad(lote.getCantidadActual());
        movimientoBajaRecall.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        movimientoBajaRecall.setLote(lote);

        for (Bulto bulto : lote.getBultos()) {

            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimientoBajaRecall)
                .bulto(bulto)
                .cantidad(bulto.getCantidadActual())
                .unidadMedida(bulto.getUnidadMedida())
                .build();

            movimientoBajaRecall.getDetalles().add(det);

            final Set<Traza> trazas = bulto.getTrazas();
            final List<Traza> trazasBaja = new ArrayList<>();

            for (Traza tr : trazas) {
                if (tr.getEstado() == CONSUMIDO || tr.getEstado() == VENDIDO) {
                    continue;
                }

                tr.setEstado(EstadoEnum.RECALL);
                trazasBaja.add(tr);
            }
            trazaRepository.saveAll(trazasBaja);
            det.getTrazas().addAll(trazasBaja);
        }

        final Movimiento savedMovimiento = movimientoRepository.save(movimientoBajaRecall);

        for (Bulto bulto : lote.getBultos()) {
            bulto.setCantidadActual(BigDecimal.ZERO);
            bulto.setEstado(RECALL);
        }
        bultoRepository.saveAll(lote.getBultos());

        lote.setEstado(RECALL);
        lote.setCantidadActual(BigDecimal.ZERO);
        lote.getMovimientos().add(savedMovimiento);

        for (Analisis analisis : lote.getAnalisisList()) {
            if (analisis.getDictamen() == null) {
                analisis.setDictamen(DictamenEnum.ANULADO);
                analisisRepository.save(analisis);
            }
        }
    }

    @Transactional
    void persistirModifTrazasDevueltas(final MovimientoDTO dto, final Lote lote) {
        final Movimiento movimientoRetiroMercado = persistirMovimientoRetiroMercado(dto, lote);

        final Map<Integer, List<TrazaDTO>> trazaDTOporBultoMap = dto.getTrazaDTOs().stream()
            .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));

        // 3) Por cada bulto afectado, crear UN DetalleMovimiento y colgar las trazas devueltas
        for (Map.Entry<Integer, List<TrazaDTO>> trazaDTOporBulto : trazaDTOporBultoMap.entrySet()) {
            final Integer trazaDTOnroBulto = trazaDTOporBulto.getKey();
            final List<TrazaDTO> trazasDTOsPorBulto = trazaDTOporBulto.getValue();

            final Bulto bulto = lote.getBultoByNro(trazaDTOnroBulto);

            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimientoRetiroMercado)
                .bulto(bulto)
                .cantidad(BigDecimal.valueOf(trazasDTOsPorBulto.size()))
                .unidadMedida(UnidadMedidaEnum.UNIDAD)
                .build();

            movimientoRetiroMercado.getDetalles().add(det);

            //TODO: ver como filtrar el estado
            for (TrazaDTO t : trazasDTOsPorBulto) {
                final Traza trazaBulto = bulto.getTrazaByNro(t.getNroTraza());
                trazaBulto.setEstado(RECALL);
                det.getTrazas().add(trazaBulto);
            }
            bultoRepository.save(bulto);
        }

        movimientoRetiroMercado.setCantidad(BigDecimal.valueOf(dto.getTrazaDTOs().size()));
        movimientoRetiroMercado.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        lote.getMovimientos().add(movimientoRepository.save(movimientoRetiroMercado));
    }

}
