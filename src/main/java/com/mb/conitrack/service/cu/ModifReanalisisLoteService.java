package com.mb.conitrack.service.cu;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.service.SecurityContextService;

import static com.mb.conitrack.enums.MotivoEnum.ANALISIS;
import static com.mb.conitrack.utils.MovimientoModificacionUtils.createMovimientoModificacion;

//***********CU8 MODIFICACION: CU8 Reanalisis de Producto Aprobado***********
@Service
public class ModifReanalisisLoteService extends AbstractCuService {

    @Autowired
    private SecurityContextService securityContextService;

    @Transactional
    public LoteDTO persistirReanalisisLote(final MovimientoDTO dto) {
        User currentUser = securityContextService.getCurrentUser();

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Analisis analisis = DTOUtils.createAnalisis(dto);
        analisis.setLote(lote);
        final Analisis newAnalisis = analisisRepository.save(analisis);
        final Movimiento movimiento = persistirMovimientoReanalisisLote(
            dto,
            lote,
            newAnalisis.getNroAnalisis(),
            currentUser);
        lote.getMovimientos().add(movimiento);
        lote.getAnalisisList().add(newAnalisis);
        newAnalisis.setLote(lote);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoReanalisisLote(final MovimientoDTO dto, Lote lote, String nroAnalisis, User currentUser) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote, currentUser);
        movimiento.setFecha(dto.getFechaMovimiento());

        movimiento.setMotivo(ANALISIS);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setNroAnalisis(nroAnalisis);

        movimiento.setObservaciones("_CU8_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    @Transactional
    public boolean validarReanalisisLoteInput(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (!validarNroAnalisisNotNull(dto, bindingResult)) {
            return false;
        }

        if (!validarNroAnalisisUnico(dto, bindingResult)) {
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
