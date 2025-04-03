package com.mb.conitrack.dto;

import java.time.LocalDate;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
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

        // 1) Fechas
        dto.setFechaAnalisis(entity.getFechaAnalisis());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setDictamen(entity.getDictamen());
        return dto;
    }

    private LocalDate fechaAnalisis;

    @NotNull
    private String nroAnalisis;

    private DictamenEnum dictamen;

}
