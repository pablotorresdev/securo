package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BultoDTO {

    @NotNull
    private String codigoLote;

    @NotNull
    private Integer nroBulto;

    private BigDecimal cantidadInicial;

    private BigDecimal cantidadActual;

    private UnidadMedidaEnum unidadMedida;

    private EstadoEnum estado;

    protected List<DetalleMovimientoDTO> detalleMovimientoDTOs = new ArrayList<>();

    private List<TrazaDTO> trazaDTOs = new ArrayList<>();

}
