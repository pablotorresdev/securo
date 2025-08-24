package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MovimientoDTO {

    //TODO: completar las validaciones segun el CU, definir las interfaces de validacion
    //Dato del back
    private OffsetDateTime fechaYHoraCreacion;

    //Datos Comunes de ingreso
    @NotNull(message = "La fecha del movimiento es obligatoria")
    @PastOrPresent(message = "La fecha del movimiento no puede ser futura")
    private LocalDate fechaMovimiento;

    private DictamenEnum dictamenInicial;

    private DictamenEnum dictamenFinal;

    private String observaciones;

    // Datos de ingreso por Muestreo
    @Positive(message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    private UnidadMedidaEnum unidadMedida;

    @Size(max = 20, message = "El número de analisis no debe superar 30 caracteres")
    private String nroAnalisis;

    //CAMPOS ALTA/BAJA
    private String nroBulto;

    // CAMPOS ANALISIS
    @PastOrPresent(message = "La fecha en que se realizo el analisis no puede ser futura")
    private LocalDate fechaRealizadoAnalisis;

    @Future(message = "La fecha de reanalisis debe ser futura")
    private LocalDate fechaReanalisis;

    @Future(message = "La fecha de vencimiento debe ser futura")
    private LocalDate fechaVencimiento;

    @Min(value = 0, message = "El resultado del analisis no puede ser menor a 0%")
    @Max(value = 100, message = "El resultado del analisis no puede ser mayor a 100%")
    private BigDecimal titulo;

    //Campos extra
    private String tipoMovimiento;

    private String motivo;

    private Long loteId;

    private String codigoLote;

    @Size(max = 20, message = "El número de re analisis no debe superar 30 caracteres")
    private String nroReanalisis;

    private String codigoMovimiento;

    // Campos no usados aun
    private String codigoMovimientoOrigen;

    private String ordenProduccion;

    protected List<BultoDTO> bultoDTOS = new ArrayList<>();

    protected List<DetalleMovimientoDTO> detalleMovimientoDTOs = new ArrayList<>();

    protected List<TrazaDTO> trazaDTOs = new ArrayList<>();

}
