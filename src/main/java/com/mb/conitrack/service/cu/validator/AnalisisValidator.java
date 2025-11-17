package com.mb.conitrack.service.cu.validator;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.thymeleaf.util.StringUtils;

import java.math.BigDecimal;

/**
 * Validador especializado para análisis.
 * Maneja validaciones de:
 * - Datos obligatorios de análisis
 * - Resultados de análisis
 * - Números de análisis únicos
 * - Fechas de análisis y reanálisis
 */
public class AnalisisValidator {

    private AnalisisValidator() {
        // Utility class, prevent instantiation
    }

    /** Valida datos obligatorios para anulación de análisis. */
    public static boolean validarDatosMandatoriosAnulacionAnalisisInput(
            final MovimientoDTO dto,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        // Verificamos que nroAnalisis no sea vacío
        if (StringUtils.isEmptyOrWhitespace(dto.getNroAnalisis())) {
            bindingResult.rejectValue("nroAnalisis", "", "El Nro de Análisis es obligatorio");
            return false;
        }

        return true;
    }

    /** Valida datos obligatorios para resultado de análisis. */
    public static boolean validarDatosMandatoriosResultadoAnalisisInput(
            final MovimientoDTO dto,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        // Verificamos que nroAnalisis no sea vacío
        if (StringUtils.isEmptyOrWhitespace(dto.getNroAnalisis())) {
            bindingResult.rejectValue("nroAnalisis", "", "El Nro de Análisis es obligatorio");
            return false;
        }

        // Dictamen Final no nulo
        if (dto.getDictamenFinal() == null) {
            bindingResult.rejectValue("dictamenFinal", "", "Debe ingresar un Resultado");
            return false;
        }

        // Fecha Realizado Análisis no nula
        if (dto.getFechaRealizadoAnalisis() == null) {
            bindingResult.rejectValue(
                "fechaRealizadoAnalisis",
                "",
                "Debe ingresar la fecha en la que se realizó el análisis");
            return false;
        }
        return true;
    }

    /** Valida datos específicos para análisis aprobado. */
    public static boolean validarDatosResultadoAnalisisAprobadoInput(
            final MovimientoDTO dto,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (DictamenEnum.APROBADO != dto.getDictamenFinal()) {
            return true;
        }
        // Al menos una de las fechas de reanálisis o vencimiento debe ser ingresada
        if (dto.getFechaVencimiento() == null && dto.getFechaReanalisis() == null) {
            bindingResult.rejectValue("fechaVencimiento", "", "Debe ingresar una fecha de Re Análisis o Vencimiento");
            return false;
        }

        // La fecha de reanálisis no puede ser posterior a la fecha de vencimiento
        if (dto.getFechaVencimiento() != null &&
            dto.getFechaReanalisis() != null &&
            dto.getFechaReanalisis().isAfter(dto.getFechaVencimiento())) {
            bindingResult.rejectValue(
                "fechaReanalisis",
                "",
                "La fecha de reanalisis no puede ser posterior a la fecha de vencimiento");
            return false;
        }

        if (dto.getTitulo() == null) {
            return true;
        }
        // El título no puede ser mayor al 100%
        if (dto.getTitulo().compareTo(BigDecimal.valueOf(100)) > 0) {
            bindingResult.rejectValue("titulo", "", "El título no puede ser mayor al 100%");
            return false;
        }
        if (dto.getTitulo().compareTo(BigDecimal.valueOf(0)) <= 0) {
            bindingResult.rejectValue("titulo", "", "El título no puede ser menor o igual a 0");
            return false;
        }
        return true;
    }

    /** Valida que el número de análisis no sea nulo. */
    public static boolean validarNroAnalisisNotNull(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (StringUtils.isEmptyOrWhitespace(dto.getNroAnalisis()) &&
            StringUtils.isEmptyOrWhitespace(dto.getNroReanalisis())) {
            bindingResult.rejectValue("nroAnalisis", "nroAnalisis.nulo", "Ingrese un nro de analisis");
            return false;
        }
        return true;
    }

    /** Valida que el número de análisis sea único. */
    public static boolean validarNroAnalisisUnico(
            final @Valid MovimientoDTO movimientoDTO,
            final BindingResult bindingResult,
            final AnalisisRepository analisisRepository) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        Analisis analisis;
        final boolean isAnalisis = StringUtils.isEmptyOrWhitespace(movimientoDTO.getNroReanalisis());
        if (isAnalisis) {
            analisis = analisisRepository.findByNroAnalisisAndActivoTrue(movimientoDTO.getNroAnalisis());
        } else {
            analisis = analisisRepository.findByNroAnalisisAndActivoTrue(movimientoDTO.getNroReanalisis());
        }
        if (analisis != null) {
            String field = isAnalisis ? "nroAnalisis" : "nroReanalisis";
            if (analisis.getLote().getCodigoLote().equals(movimientoDTO.getCodigoLote())) {
                if (analisis.getDictamen() != null) {
                    bindingResult.rejectValue(
                        field,
                        "",
                        "Nro de analisis ya registrado en el mismo lote.");
                    return false;
                }
            } else {
                bindingResult.rejectValue(
                    field,
                    "",
                    "Nro de analisis ya registrado en otro lote.");
                return false;
            }
        }
        return true;
    }
}
