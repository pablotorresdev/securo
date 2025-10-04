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
import com.mb.conitrack.enums.DictamenEnum;

import static com.mb.conitrack.enums.MotivoEnum.ANULACION_ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.RESULTADO_ANALISIS;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;

//***********ANULACION ANALISIS***********
@Service
public class ModifAnulacionAnalisisService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirAnulacionAnalisis(final MovimientoDTO dto) {
        final Analisis analisis = addAnulacionAnalisis(dto);
        final Lote lote = analisis.getLote();
        final Movimiento movimiento = persistirMovimientoAnulacionAnalisis(dto, lote);
        lote.getMovimientos().add(movimiento);
        final List<Movimiento> movModifAnalisisByNro = movimientoRepository.findMovModifAnalisisByNro(dto.getNroAnalisis());
        if (movModifAnalisisByNro.size() != 1) {
            throw new IllegalArgumentException("Existen 2 movimientos de análisis iguales para ese lote");
        }
        lote.setDictamen(movModifAnalisisByNro.get(0).getDictamenInicial());
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoAnulacionAnalisis(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaMovimiento());

        movimiento.setMotivo(ANULACION_ANALISIS);
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(DictamenEnum.ANULADO);

        movimiento.setObservaciones("_CUX_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    @Transactional
    public boolean validarAnulacionAnalisisInput(
        final MovimientoDTO dto,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (!validarDatosMandatoriosAnulacionAnalisisInput(dto, bindingResult)) {
            return false;
        }

        final Optional<LocalDate> fechaIngresoLote = loteRepository
            .findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .map(Lote::getFechaIngreso);

        if (fechaIngresoLote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        return validarFechaMovimientoPosteriorIngresoLote(dto, fechaIngresoLote.get(), bindingResult);
    }

    @Transactional
    Analisis addAnulacionAnalisis(final MovimientoDTO dto) {
        Analisis analisis = analisisRepository.findByNroAnalisisAndActivoTrue(dto.getNroAnalisis());
        if (analisis == null) {
            analisis = DTOUtils.createAnalisis(dto);
        }

        analisis.setFechaRealizado(dto.getFechaRealizadoAnalisis());
        analisis.setDictamen(DictamenEnum.ANULADO);
        analisis.setObservaciones(dto.getObservaciones());
        return analisisRepository.save(analisis);
    }

}
