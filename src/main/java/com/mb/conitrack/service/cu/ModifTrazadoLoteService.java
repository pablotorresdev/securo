package com.mb.conitrack.service.cu;

import java.time.LocalDate;
import java.util.List;
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
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.service.SecurityContextService;

import static com.mb.conitrack.enums.DictamenEnum.LIBERADO;
import static com.mb.conitrack.enums.MotivoEnum.LIBERACION;
import static com.mb.conitrack.enums.MotivoEnum.TRAZADO;
import static com.mb.conitrack.utils.LoteEntityUtils.addTrazasToLote;
import static com.mb.conitrack.utils.MovimientoModificacionUtils.createMovimientoModificacion;

/** CU28 - Trazado de Lote. Asigna números de traza únicos a unidades de venta. */
@Service
public class ModifTrazadoLoteService extends AbstractCuService {

    @Autowired
    private SecurityContextService securityContextService;

    /** Agrega trazas al lote UNIDAD_VENTA distribuyéndolas entre bultos. */
    @Transactional
    public LoteDTO persistirTrazadoLote(final MovimientoDTO dto) {
        User currentUser = securityContextService.getCurrentUser();

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        if (lote.getProducto().getTipoProducto() != TipoProductoEnum.UNIDAD_VENTA) {
            throw new IllegalArgumentException("El lote debe ser UNIDAD_VENTA para poder trazarse");
        }
        addTrazasToLote(lote, dto);

        trazaRepository.saveAll(lote.getTrazas());

        final Movimiento movimiento = persistirMovimientoTrazadoLote(dto, lote, currentUser);
        lote.getMovimientos().add(movimiento);

        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoTrazadoLote(final MovimientoDTO dto, final Lote lote, User currentUser) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote, currentUser);

        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setMotivo(TRAZADO);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(lote.getDictamen());

        movimiento.setObservaciones("_CU28_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    @Transactional
    public boolean validarTrazadoLoteInput(final MovimientoDTO dto, final BindingResult bindingResult) {
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

        return validarTrazaInicialLote(dto, bindingResult);
    }

}
