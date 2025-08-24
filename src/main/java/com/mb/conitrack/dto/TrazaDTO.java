package com.mb.conitrack.dto;

import java.time.OffsetDateTime;

import com.mb.conitrack.enums.EstadoEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrazaDTO {

    @NotNull
    private String codigoLote;

    @NotNull
    private Integer nroBulto;

    @NotNull
    private Long nroTraza;

    protected String codigoProducto;

    private OffsetDateTime fechaYHoraCreacion;

    private EstadoEnum estado;

    private String observaciones;

}
