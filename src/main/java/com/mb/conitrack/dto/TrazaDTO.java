package com.mb.conitrack.dto;

import java.time.LocalDateTime;

import com.mb.conitrack.enums.EstadoEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrazaDTO {

    @NotNull
    private Long nroTraza;

    protected String codigoProducto;

    private LocalDateTime fechaYHoraCreacion;

    private EstadoEnum estado;

    private String observaciones;

}
