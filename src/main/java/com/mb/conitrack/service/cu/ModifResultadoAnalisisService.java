package com.mb.conitrack.service.cu;

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

import static com.mb.conitrack.enums.MotivoEnum.RESULTADO_ANALISIS;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;

//***********CU5/6: RESULTADO ANALISIS***********
@Service
public class ModifResultadoAnalisisService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirResultadoAnalisis(final MovimientoDTO dto) {
        final Analisis analisis = addResultadoAnalisis(dto);
        final Lote lote = analisis.getLote();
        final Movimiento movimiento = persistirMovimientoResultadoAnalisis(dto, lote);
        lote.setDictamen(movimiento.getDictamenFinal());
        lote.getMovimientos().add(movimiento);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoResultadoAnalisis(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaRealizadoAnalisis());

        movimiento.setMotivo(RESULTADO_ANALISIS);
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(dto.getDictamenFinal());

        movimiento.setObservaciones("_CU5/6_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    @Transactional
    public boolean validarResultadoAnalisisInput(
        final MovimientoDTO dto,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (!validarDatosMandatoriosResultadoAnalisisInput(dto, bindingResult)) {
            return false;
        }

        if (!validarDatosResultadoAnalisisAprobadoInput(dto, bindingResult)) {
            return false;
        }

        if (!movimientoRepository.existeMuestreo(dto.getCodigoLote(), dto.getNroAnalisis())) {
            bindingResult.rejectValue(
                "nroAnalisis",
                "",
                "No se encontró un MUESTREO realizado para ese Nro de Análisis " + dto.getNroAnalisis());
            return false;
        }

        final Optional<Lote> lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        Lote loteActual = lote.get();

        if (!validarFechaMovimientoPosteriorIngresoLote(dto, loteActual.getFechaIngreso(), bindingResult)) {
            return false;
        }

        if (!validarFechaAnalisisPosteriorIngresoLote(dto, loteActual.getFechaIngreso(), bindingResult)) {
            return false;
        }

        if (!validarFechasReanalisis(dto, bindingResult)) {
            return false;
        }

        if (DictamenEnum.RECHAZADO == dto.getDictamenFinal()) {
            return true;
        }

        final List<Analisis> analisisList = analisisRepository.findUltimoAprobadoConTituloPorCodigoLote(
            dto.getCodigoLote());

        if (analisisList.isEmpty()) {
            return true;
        }

        return validarValorTitulo(dto, analisisList.get(0), bindingResult);
    }

    @Transactional
    Analisis addResultadoAnalisis(final MovimientoDTO dto) {
        Analisis analisis = analisisRepository.findByNroAnalisisAndActivoTrue(dto.getNroAnalisis());
        if (analisis == null) {
            analisis = DTOUtils.createAnalisis(dto);
        }

        analisis.setFechaRealizado(dto.getFechaRealizadoAnalisis());
        analisis.setDictamen(dto.getDictamenFinal());
        if (dto.getDictamenFinal() == DictamenEnum.APROBADO) {
            analisis.setFechaReanalisis(dto.getFechaReanalisis());
            analisis.setFechaVencimiento(dto.getFechaVencimiento());
            analisis.setTitulo(dto.getTitulo());
        }
        analisis.setObservaciones(dto.getObservaciones());
        return analisisRepository.save(analisis);
    }

}
