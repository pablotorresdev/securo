package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetalleMovimientoDTO {

    @NotNull
    private String codigoMovimiento;

    @NotNull
    private Integer nroBulto;

    private BigDecimal cantidad;

    private UnidadMedidaEnum unidadMedida;

    private List<TrazaDTO> trazaDTOs = new ArrayList<>();

}
