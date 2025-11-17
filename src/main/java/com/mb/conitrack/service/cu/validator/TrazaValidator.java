package com.mb.conitrack.service.cu.validator;

import com.mb.conitrack.dto.MovimientoDTO;
import org.springframework.validation.BindingResult;

/**
 * Validador especializado para trazas.
 * Maneja validaciones de:
 * - Traza inicial de lote
 * - Trazas de devolución
 */
public class TrazaValidator {

    private TrazaValidator() {
        // Utility class, prevent instantiation
    }

    /** Valida traza inicial del lote. */
    public static boolean validarTrazaInicialLote(
            final MovimientoDTO dto,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getTrazaInicial() == null || dto.getTrazaInicial() <= 0) {
            bindingResult.rejectValue(
                "trazaInicial",
                "",
                "Ingrese un valor válido para la traza inicial del lote");
            return false;
        }
        return true;
    }

    /** Valida trazas para devolución. */
    public static boolean validarTrazasDevolucion(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (dto.getTrazaDTOs() == null || dto.getTrazaDTOs().isEmpty()) {
            bindingResult.rejectValue("trazaDTOs", "", "Debe seleccionar al menos una traza para devolver.");
            return false;
        }
        return true;
    }
}
