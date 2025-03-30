package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovimientoDTO {

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaMovimiento;

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId; // o loteProveedor + nroBulto

    //Cantidades
    private String nroBulto;
    @Positive(message = "La cantidad inicial debe ser mayor a cero")
    private BigDecimal cantidad;
    private UnidadMedidaEnum unidadMedida;

    @FutureOrPresent(message = "La fecha de análisis debe ser presente o futura")
    private LocalDate fechaAnalisis;

    @FutureOrPresent(message = "La fecha de re-análisis debe ser presente o futura")
    private LocalDate fechaReAnalisis;

    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    private LocalDate fechaVencimiento;

    private String descripcion;

    private String ordenProduccion;

    //Para movimientos productivos
    private String observaciones;

    private String nroAnalisis;
    private String nroReAnalisis;

    //Campos de tipo de CU
    private String tipoMovimiento;
    private String motivo;
    private DictamenEnum dictamenInicial;
    private DictamenEnum dictamenFinal;
    private Movimiento movimientoOrigen;

}
