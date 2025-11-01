package com.mb.conitrack.service.cu;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;

import static com.mb.conitrack.enums.DictamenEnum.LIBERADO;
import static com.mb.conitrack.enums.MotivoEnum.LIBERACION;
import static com.mb.conitrack.utils.MovimientoModificacionUtils.createMovimientoModificacion;

//***********CU21 MODIFICACION: LIBERACION DE PRODUCTO***********
@Service
public class ModifLiberacionVentasService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirLiberacionProducto(final MovimientoDTO dto) {

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Movimiento movimiento = persistirMovimientoLiberacionProducto(dto, lote);
        final List<Analisis> list = lote.getAnalisisConFechaVenciminentoList();

        if (list.isEmpty()) {
            throw new IllegalStateException("NO hay Analisis con fecha de vencimiento para el lote");
        } else if (list.size() == 1) {
            lote.setFechaVencimientoProveedor(list.get(0).getFechaVencimiento());
        } else {
            throw new IllegalStateException("Hay más de un análisis activo con fecha de vencimiento");
        }

        lote.setDictamen(movimiento.getDictamenFinal());
        lote.getMovimientos().add(movimiento);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoLiberacionProducto(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);

        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setMotivo(LIBERACION);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(LIBERADO);

        movimiento.setObservaciones("_CU21_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    @Transactional
    public boolean validarLiberacionProductoInput(final MovimientoDTO dto, final BindingResult bindingResult) {
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

        return validarFechaAnalisisPosteriorIngresoLote(dto, fechaIngresoLote.get(), bindingResult);
    }

}
