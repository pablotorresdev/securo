package com.mb.conitrack.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.enums.DictamenEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalisisDTO {

    public static AnalisisDTO fromEntity(Analisis entity) {
        if (entity == null) {
            return null;
        }
        AnalisisDTO dto = new AnalisisDTO();
        dto.setFechaAnalisis(entity.getFechaAnalisis());
        dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setDictamen(entity.getDictamen());
        return dto;
    }

    @NotNull
    private String nroAnalisis;

    private LocalDate fechaAnalisis;

    private DictamenEnum dictamen;

    private LocalDateTime fechaYHoraCreacion;

}
