package com.mb.conitrack.service.cu;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;

import jakarta.validation.Valid;

import static com.mb.conitrack.enums.DictamenEnum.CUARENTENA;
import static com.mb.conitrack.enums.MotivoEnum.ANALISIS;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;

//***********CU2 MODIFICACION: CUARENTENA***********
@Service
public class ModifDictamenCuarentenaService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirDictamenCuarentena(final MovimientoDTO dto) {

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        //TODO, eliminar NRO de Reanalisis del DTO
        final String nroAnalisis = StringUtils.isEmptyOrWhitespace(dto.getNroReanalisis())
            ? dto.getNroAnalisis()
            : dto.getNroReanalisis();

        Analisis currentAnalisis = analisisRepository.findByNroAnalisisAndActivoTrue(nroAnalisis);
        Analisis newAnalisis = null;

        if (currentAnalisis == null) {
            final Analisis analisis = DTOUtils.createAnalisis(dto);
            analisis.setLote(lote);
            newAnalisis = analisisRepository.save(analisis);
        }
        if (newAnalisis != null) {
            lote.getAnalisisList().add(newAnalisis);
        }

        final String nroAnalisisMovimiento = newAnalisis != null ? newAnalisis.getNroAnalisis() : nroAnalisis;
        Movimiento mov = persistirMovimientoCuarentenaPorAnalisis(dto, lote, nroAnalisisMovimiento);

        lote.getMovimientos().add(mov);
        lote.setDictamen(DictamenEnum.CUARENTENA);

        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoCuarentenaPorAnalisis(final MovimientoDTO dto, Lote lote, String nroAnalisis) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setMotivo(ANALISIS);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(CUARENTENA);
        movimiento.setNroAnalisis(nroAnalisis);

        movimiento.setObservaciones("_CU2_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    @Transactional
    public boolean validarDictamenCuarentena(
        final @Valid MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (!validarNroAnalisisNotNull(movimientoDTO, bindingResult)) {
            return false;
        }

        if (!validarNroAnalisisUnico(movimientoDTO, bindingResult)) {
            return false;
        }

        final Optional<Lote> lote = loteRepository.findByCodigoLoteAndActivoTrue(movimientoDTO.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (!validarFechaMovimientoPosteriorIngresoLote(movimientoDTO, lote.get().getFechaIngreso(), bindingResult)) {
            return false;
        }

        return validarFechaAnalisisPosteriorIngresoLote(movimientoDTO, lote.get().getFechaIngreso(), bindingResult);
    }

}
