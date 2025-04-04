package com.mb.conitrack.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.enums.DictamenEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalisisDTO {

    @NotNull
    private String nroAnalisis;

    private LocalDate fechaAnalisis;

    private LocalDate fechaReanalisis;

    private LocalDate fechaVencimiento;

    private DictamenEnum dictamen;

    private LocalDateTime fechaYHoraCreacion;

}
