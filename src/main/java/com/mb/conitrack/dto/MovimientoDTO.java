package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovimientoDTO {

    private LocalDateTime fechaYHoraCreacion;

    @NotNull(message = "La fecha del movimiento es obligatoria")
    private LocalDate fechaMovimiento;

    private Long loteId; // Id del registro del lote => Idem a (codigoInterno + nroBulto) o (loteProveedor + nroBulto) o (nroAnalisis + nroBulto)

    //CAMPOS ALTA/BAJA
    private String nroBulto;
    @Positive(message = "La cantidad inicial debe ser mayor a cero")
    private BigDecimal cantidad;
    private UnidadMedidaEnum unidadMedida;

    // CAMPOS ANALISIS
    @PastOrPresent(message = "La fecha en que se realizo el analisis no puede ser futura")
    private LocalDate fechaRealizadoAnalisis;
    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    private LocalDate fechaReanalisis;
    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    private LocalDate fechaVencimiento;

    private String nroAnalisis;
    private String nroReanalisis;
    private BigDecimal titulo;

    private DictamenEnum dictamenInicial;
    private DictamenEnum dictamenFinal;

    private String ordenProduccion;
    private Movimiento movimientoOrigen;

    private String observaciones;

    //Campos extra
    private String tipoMovimiento;
    private String motivo;
}
