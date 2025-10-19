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
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntity;
import static com.mb.conitrack.enums.EstadoEnum.DISPONIBLE;
import static com.mb.conitrack.enums.EstadoEnum.RECALL;
import static com.mb.conitrack.utils.MovimientoEntityUtils.crearMovimientoModifRecall;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaRecall;
import static java.lang.Boolean.TRUE;

//***********CU24 ALTA/MODIF: RECALL***********
@Service
public class ModifRetiroMercadoService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirRetiroMercado(final MovimientoDTO dto) {

        final Lote lote = loteRepository.findFirstByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        persistirModifTrazasDisponiblesEnStock(dto, lote);
        persistirAltaUnidadesRetiradas(dto, lote);

        return fromLoteEntity(loteRepository.save(lote));
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
    void persistirAltaUnidadesRetiradas(final MovimientoDTO dto, final Lote lote) {
        final Movimiento movimientoAltaRecall = createMovimientoAltaRecall(dto, lote);

        //************RECALL************
        final Map<Integer, List<TrazaDTO>> trazaDTOporBultoMap = dto.getTrazaDTOs().stream()
            .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));

        // 3) Por cada bulto afectado, crear UN DetalleMovimiento y colgar las trazas devueltas
        for (Map.Entry<Integer, List<TrazaDTO>> trazaDTOporBulto : trazaDTOporBultoMap.entrySet()) {
            final Integer trazaDTOnroBulto = trazaDTOporBulto.getKey();
            final List<TrazaDTO> trazasDTOsPorBulto = trazaDTOporBulto.getValue();

            final Bulto bulto = lote.getBultoByNro(trazaDTOnroBulto);

            final BigDecimal cantidad = BigDecimal.valueOf(trazasDTOsPorBulto.size());
            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimientoAltaRecall)
                .bulto(bulto)
                .cantidad(cantidad)
                .unidadMedida(UnidadMedidaEnum.UNIDAD)
                .activo(TRUE)
                .build();

            movimientoAltaRecall.getDetalles().add(det);

            for (TrazaDTO t : trazasDTOsPorBulto) {
                final Traza trazaBulto = bulto.getTrazaByNro(t.getNroTraza());
                trazaBulto.setEstado(RECALL);
                det.getTrazas().add(trazaBulto);
            }
            bulto.setCantidadActual(bulto.getCantidadActual().add(cantidad));
            bultoRepository.save(bulto);
        }

        movimientoAltaRecall.setCantidad(BigDecimal.valueOf(dto.getTrazaDTOs().size()));
        movimientoAltaRecall.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        final Movimiento newMovimiento = movimientoRepository.save(movimientoAltaRecall);
        lote.setCantidadActual(lote.getCantidadActual().add(BigDecimal.valueOf(dto.getTrazaDTOs().size())));
        lote.getMovimientos().add(newMovimiento);
    }

    @Transactional
    void persistirModifTrazasDisponiblesEnStock(final MovimientoDTO dto, final Lote lote) {
        final Movimiento movimientoModifRecall = crearMovimientoModifRecall(dto);
        movimientoModifRecall.setDictamenInicial(lote.getDictamen());
        movimientoModifRecall.setCantidad(lote.getCantidadActual());
        movimientoModifRecall.setLote(lote);

        for (Bulto bulto : lote.getBultos()) {
            final Set<Traza> trazas = bulto.getTrazas();
            final List<Traza> trazasRecall = new ArrayList<>();
            for (Traza tr : trazas) {
                if (tr.getEstado() != DISPONIBLE) {
                    continue;
                }

                tr.setEstado(EstadoEnum.RECALL);
                trazasRecall.add(tr);
            }
            trazaRepository.saveAll(trazasRecall);
        }

        final Movimiento savedMovimiento = movimientoRepository.save(movimientoModifRecall);

        for (Bulto bulto : lote.getBultos()) {
            bulto.setEstado(RECALL);
        }
        bultoRepository.saveAll(lote.getBultos());

        lote.setEstado(RECALL);
        lote.getMovimientos().add(savedMovimiento);

        for (Analisis analisis : lote.getAnalisisList()) {
            if (analisis.getDictamen() == null) {
                analisis.setDictamen(DictamenEnum.ANULADO);
                analisisRepository.save(analisis);
            }
        }
    }

}
