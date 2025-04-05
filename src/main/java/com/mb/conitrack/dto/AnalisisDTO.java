package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mb.conitrack.enums.DictamenEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalisisDTO {


    @NotNull
    private String nroAnalisis;

    private LocalDateTime fechaYHoraCreacion;

    private LocalDate fechaRealizado;

    private LocalDate fechaReanalisis;

    private LocalDate fechaVencimiento;

    private DictamenEnum dictamen;

    private BigDecimal titulo;

    private String observaciones;

}
