package com.mb.conitrack.service.cu.validator;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

/**
 * Validador especializado para fechas.
 * Maneja validaciones de:
 * - Fechas de proveedor (reanálisis y vencimiento)
 * - Fechas de análisis
 * - Fechas de movimiento
 * - Relaciones temporales entre fechas
 */
public class FechaValidator {

    private FechaValidator() {
        // Utility class, prevent instantiation
    }

    /** Valida fechas de proveedor (reanálisis no puede ser posterior a vencimiento). */
    public static boolean validarFechasProveedor(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (loteDTO.getFechaReanalisisProveedor() != null && loteDTO.getFechaVencimientoProveedor() != null) {
            if (loteDTO.getFechaReanalisisProveedor().isAfter(loteDTO.getFechaVencimientoProveedor())) {
                bindingResult.rejectValue(
                    "fechaReanalisisProveedor",
                    "",
                    "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
                return false;
            }
        }
        return true;
    }

    /** Valida fechas de reanálisis (no puede ser posterior a vencimiento). */
    public static boolean validarFechasReanalisis(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaReanalisis() != null && dto.getFechaVencimiento() != null) {
            if (dto.getFechaReanalisis().isAfter(dto.getFechaVencimiento())) {
                bindingResult.rejectValue(
                    "fechaVencimiento",
                    "",
                    "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
                return false;
            }
        }
        return true;
    }

    /** Valida fecha de análisis (debe ser posterior a ingreso del lote). */
    public static boolean validarFechaAnalisisPosteriorIngresoLote(
            final MovimientoDTO dto,
            final LocalDate fechaIngresoLote,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaRealizadoAnalisis() != null && dto.getFechaRealizadoAnalisis().isBefore(fechaIngresoLote)) {
            bindingResult.rejectValue(
                "fechaRealizadoAnalisis",
                "",
                "La fecha de realizado el analisis no puede ser anterior a la fecha de ingreso del lote");
            return false;
        }
        return true;
    }

    /** Valida fecha de egreso (debe ser posterior a ingreso del lote). */
    public static boolean validarFechaEgresoLoteDtoPosteriorLote(
            final LoteDTO dto,
            final Lote lote,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaEgreso() != null && dto.getFechaEgreso().isBefore(lote.getFechaIngreso())) {
            bindingResult.rejectValue(
                "fechaEgreso",
                "",
                "La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
            return false;
        }
        return true;
    }

    /** Valida fecha de movimiento (debe ser posterior a ingreso del lote). */
    public static boolean validarFechaMovimientoPosteriorIngresoLote(
            final MovimientoDTO dto,
            final LocalDate fechaIngresoLote,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaMovimiento().isBefore(fechaIngresoLote)) {
            bindingResult.rejectValue(
                "fechaMovimiento",
                "",
                "La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
            return false;
        }
        return true;
    }

    /** Valida fecha de movimiento contra movimiento origen. */
    public static boolean validarMovimientoOrigen(
            final MovimientoDTO dto,
            final BindingResult bindingResult,
            final Movimiento movOrigen) {
        if (dto.getFechaMovimiento() != null &&
            dto.getFechaMovimiento().isBefore(movOrigen.getFecha())) {
            bindingResult.rejectValue(
                "fechaMovimiento",
                "",
                "La fecha de devolución no puede ser anterior a la fecha del movimiento de venta.");
            return false;
        }
        return true;
    }
}
