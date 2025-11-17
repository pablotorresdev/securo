package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.service.SecurityContextService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;
import static java.lang.Boolean.TRUE;

/**
 * Servicio coordinador para el retiro de mercado (CU24 - Recall).
 * Delega la lógica específica a servicios especializados:
 * - AltaRecallService: Para crear lotes de retiro de mercado
 * - ModificacionRecallService: Para modificar estado de lotes a RECALL
 */
@Service
public class ModifRetiroMercadoService extends AbstractCuService {

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private AltaRecallService altaRecallService;

    @Autowired
    private ModificacionRecallService modificacionRecallService;

    //***********CU24 ALTA/MODIF: RECALL***********

    /**
     * Método principal que coordina el proceso de retiro de mercado (recall).
     * Ejecuta dos operaciones:
     * 1. ALTA: Crea un nuevo lote recall derivado del lote de venta
     * 2. MODIFICACION: Cambia el estado del lote de venta original a RECALL
     */
    @Transactional
    public List<LoteDTO> persistirRetiroMercado(final MovimientoDTO dto) {
        User currentUser = securityContextService.getCurrentUser();
        List<Lote> result = new ArrayList<>();

        final Lote loteVentaOrigen = loteRepository.findFirstByCodigoLoteAndActivoTrue(dto.getCodigoLote())
                .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Movimiento movimientoVentaOrigen = movimientoRepository.findByCodigoMovimientoAndActivoTrue(
                        dto.getCodigoMovimientoOrigen())
                .orElseThrow(() -> new IllegalArgumentException("El movmiento de origen no existe."));

        //************ALTA RECALL************
        altaRecallService.procesarAltaRecall(dto, loteVentaOrigen, movimientoVentaOrigen, result, currentUser);

        //************MODIFICACION RECALL************
        modificacionRecallService.procesarModificacionRecall(dto, loteVentaOrigen, movimientoVentaOrigen, result, currentUser);

        return fromLoteEntities(result);
    }

    /**
     * Valida los datos de entrada para el retiro de mercado.
     * Verifica lote, fecha, trazas y movimiento origen.
     */
    @Transactional
    public boolean validarRetiroMercadoInput(
            final @Valid MovimientoDTO dto,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        final Optional<Lote> lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (!validarFechaMovimientoPosteriorIngresoLote(dto, lote.get().getFechaIngreso(), bindingResult)) {
            return false;
        }

        if (TRUE.equals(lote.get().getTrazado()) && !validarTrazasDevolucion(dto, bindingResult)) {
            return false;
        }

        final Optional<Movimiento> movOrigen = movimientoRepository.findByCodigoMovimientoAndActivoTrue(
                dto.getCodigoMovimientoOrigen());

        if (movOrigen.isEmpty()) {
            bindingResult.rejectValue("codigoMovimientoOrigen", "", "No se encontro el movimiento de venta origen");
            return true;
        }

        return validarMovimientoOrigen(dto, bindingResult, movOrigen.get());
    }
}
