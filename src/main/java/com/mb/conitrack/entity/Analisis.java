package com.mb.conitrack.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.thymeleaf.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.enums.DictamenEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "analisis")
@SQLDelete(sql = "UPDATE analisis SET activo = false WHERE id = ?")
@ToString(exclude = { "lotes" })
public class Analisis {

    public static Analisis createAnalisis(final MovimientoDTO dto, final Lote lote) {
        return createAnalisis(dto);
    }

    public static Analisis createAnalisis(final MovimientoDTO dto) {
        final String nroAnalisis = StringUtils.isEmpty(dto.getNroReAnalisis()) ? dto.getNroAnalisis() : dto.getNroReAnalisis();
        if (nroAnalisis != null) {
            Analisis analisis = new Analisis();
            analisis.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
            analisis.setFechaAnalisis(dto.getFechaAnalisis());
            analisis.setNroAnalisis(nroAnalisis);
            analisis.setObservaciones(dto.getObservaciones());
            analisis.setActivo(true);
            return analisis;
        }
        throw new IllegalArgumentException("El número de análisis es requerido");
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaYHoraCreacion;

    @ManyToMany(mappedBy = "analisisList")
    @JsonBackReference
    private List<Lote> lotes = new ArrayList<>();

    private LocalDate fechaAnalisis;

    @Column(name = "nro_analisis", length = 50, nullable = false)
    private String nroAnalisis;

    @Enumerated(EnumType.STRING)
    private DictamenEnum dictamen;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

}
