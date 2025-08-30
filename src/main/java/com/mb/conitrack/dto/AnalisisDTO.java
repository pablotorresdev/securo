package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.TipoProductoEnum;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalisisDTO {

    @NotNull
    private String nroAnalisis;

    private String codigoLote;

    private OffsetDateTime fechaYHoraCreacion;

    private LocalDate fechaRealizado;

    private LocalDate fechaReanalisis;

    private LocalDate fechaVencimiento;

    private DictamenEnum dictamen;

    private BigDecimal titulo;

    private boolean esUnidadVenta;

    private String codigoProducto;

    private TipoProductoEnum tipoProducto;

    private String observaciones;

}
