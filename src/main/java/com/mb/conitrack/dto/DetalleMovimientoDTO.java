package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class DetalleMovimientoDTO {

    @NotNull
    private String codigoInternoMovimiento;

    @NotNull
    private Integer nroBulto;

    private BigDecimal cantidad;

    private UnidadMedidaEnum unidadMedida;

}
