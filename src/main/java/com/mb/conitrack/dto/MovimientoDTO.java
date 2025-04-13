package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovimientoDTO {

    //TODO: completar las validaciones segun el CU, definir las interfaces de validacion
    //Dato del back
    private LocalDateTime fechaYHoraCreacion;

    //Datos Comunes de ingreso
    @NotNull(message = "La fecha del movimiento es obligatoria")
    private LocalDate fechaMovimiento;
    private DictamenEnum dictamenInicial;
    private DictamenEnum dictamenFinal;
    private String observaciones;

    // Datos de ingreso por Muestreo
    @Positive(message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;
    private UnidadMedidaEnum unidadMedida;
    private String nroAnalisis;

    //CAMPOS ALTA/BAJA
    private String nroBulto;

    // CAMPOS ANALISIS
    @PastOrPresent(message = "La fecha en que se realizo el analisis no puede ser futura")
    private LocalDate fechaRealizadoAnalisis;
    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    private LocalDate fechaReanalisis;
    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    private LocalDate fechaVencimiento;
    @Min(value = 0, message = "El resultado del analisis no puede ser menor a 0%")
    @Max(value = 100, message = "El resultado del analisis no puede ser mayor a 100%")
    private BigDecimal titulo;

    //Campos extra
    private String tipoMovimiento;
    private String motivo;
    private Long loteId; // Id del registro del lote => Idem a (codigoInterno + nroBulto) o (loteProveedor + nroBulto) o (nroAnalisis + nroBulto)
    private String codigoInterno; // Id del registro del lote => Idem a (codigoInterno + nroBulto) o (loteProveedor + nroBulto) o (nroAnalisis + nroBulto)
    private String nroReanalisis;

    // Campos no usados aun
    private Movimiento movimientoOrigen;
    private String ordenProduccion;



}
