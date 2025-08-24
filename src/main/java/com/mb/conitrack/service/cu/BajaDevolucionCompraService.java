package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.utils.ControllerUtils;

import static com.mb.conitrack.enums.EstadoEnum.DEVUELTO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.crearMovimientoDevolucionCompra;

//***********CU4 BAJA: DEVOLUCION COMPRA***********
@Service
public class BajaDevolucionCompraService extends AbstractCuService {

    private static ControllerUtils controllerUtils() {
        return ControllerUtils.getInstance();
    }

    @Transactional
    public LoteDTO bajaBultosDevolucionCompra(final MovimientoDTO dto) {

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Movimiento movimiento = crearMovimientoDevolucionCompra(dto);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setCantidad(lote.getCantidadActual());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setLote(lote);

        for (Bulto bulto : lote.getBultos()) {
            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(bulto)
                .cantidad(bulto.getCantidadActual())
                .unidadMedida(bulto.getUnidadMedida())
                .build();

            movimiento.getDetalles().add(det);
        }

        final Movimiento savedMovimiento = movimientoRepository.save(movimiento);

        for (Bulto bulto : lote.getBultos()) {
            bulto.setCantidadActual(BigDecimal.ZERO);
            bulto.setEstado(DEVUELTO);
        }
        bultoRepository.saveAll(lote.getBultos());

        lote.setEstado(DEVUELTO);
        lote.setCantidadActual(BigDecimal.ZERO);
        lote.getMovimientos().add(savedMovimiento);

        for (Analisis analisis : lote.getAnalisisList()) {
            if (analisis.getDictamen() == null) {
                analisis.setDictamen(DictamenEnum.ANULADO);
                analisisRepository.save(analisis);
            }
        }

        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    public boolean validarDevolucionCompra(final MovimientoDTO movimientoDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        final Optional<Lote> lote = loteRepository
            .findByCodigoLoteAndActivoTrue(movimientoDTO.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        return controllerUtils().validarFechaMovimientoPosteriorIngresoLote(
            movimientoDTO, lote.get().getFechaIngreso(), bindingResult);
    }

}
