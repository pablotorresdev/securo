package com.mb.conitrack.service.cu;

import java.time.LocalDate;
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

import lombok.AllArgsConstructor;

import static com.mb.conitrack.enums.MotivoEnum.ANALISIS;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;

//***********CU> MODIFICACION: CUZ Reanalisis de Producto Aprobado***********
@Service
public class ModifReanalisisProductoService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirReanalisisProducto(final MovimientoDTO dto) {

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Analisis analisis = DTOUtils.createAnalisis(dto);
        analisis.setLote(lote);
        final Analisis newAnalisis = analisisRepository.save(analisis);
        final Movimiento movimiento = persistirMovimientoReanalisisProducto(
            dto,
            lote,
            newAnalisis.getNroAnalisis());
        lote.getMovimientos().add(movimiento);
        lote.getAnalisisList().add(newAnalisis);
        newAnalisis.setLote(lote);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoReanalisisProducto(final MovimientoDTO dto, Lote lote, String nroAnalisis) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaMovimiento());

        movimiento.setMotivo(ANALISIS);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setNroAnalisis(nroAnalisis);

        movimiento.setObservaciones("_CUZ_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }


    public boolean validarReanalisisProducto(final MovimientoDTO movimientoDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (!validarNroAnalisisNotNull(movimientoDTO, bindingResult)) {
            return false;
        }

        if (!validarNroAnalisisUnico(movimientoDTO, bindingResult)) {
            return false;
        }

        final Optional<LocalDate> fechaIngresoLote = loteRepository.findByCodigoLoteAndActivoTrue(movimientoDTO.getCodigoLote())
            .map(Lote::getFechaIngreso);

        if (fechaIngresoLote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (!validarFechaMovimientoPosteriorIngresoLote(movimientoDTO, fechaIngresoLote.get(), bindingResult)) {
            return false;
        }

        return validarFechaAnalisisPosteriorIngresoLote(movimientoDTO, fechaIngresoLote.get(), bindingResult);
    }

}
