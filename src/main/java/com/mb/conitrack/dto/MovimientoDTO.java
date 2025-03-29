package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovimientoDTO {

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaMovimiento;

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId; // o loteProveedor + nroBulto

    private String tipoMovimiento;
    private String motivo; // MUESTREO

    @Positive(message = "La cantidad inicial debe ser mayor a cero")
    private BigDecimal cantidad;

    private UnidadMedidaEnum unidadMedida;
    private String nroAnalisis;
    private String nroReAnalisis;
    private String identificacionBulto;
    private String observaciones;
    private LocalDate fecha;
    private String descripcion;
    private String ordenProduccion;
    private DictamenEnum dictamenInicial;
    private DictamenEnum dictamenFinal;
    private Movimiento movimientoOrigen;

}
