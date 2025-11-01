package com.mb.conitrack.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.hibernate.annotations.SQLDelete;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mb.conitrack.enums.DictamenEnum;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Entidad: Análisis de calidad de lote con dictamen y fechas de vencimiento. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "analisis")
@SQLDelete(sql = "UPDATE analisis SET activo = false WHERE id = ?")
@ToString(exclude = { "lote" })
public class Analisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    @EqualsAndHashCode.Include
    private OffsetDateTime fechaYHoraCreacion;

    @Column(name = "nro_analisis", length = 30, nullable = false)
    @EqualsAndHashCode.Include
    private String nroAnalisis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonBackReference
    @EqualsAndHashCode.Include
    private Lote lote;

    @Column(name = "fecha_realizado")
    @PastOrPresent(message = "La fecha de análisis no puede ser futura")
    private LocalDate fechaRealizado;

    @Column(name = "fecha_reanalisis")
    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    private LocalDate fechaReanalisis;

    @Column(name = "fecha_vencimiento")
    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    private DictamenEnum dictamen;

    @Column(name = "titulo", precision = 12, scale = 4)
    @Max(value = 100, message = "El título no puede superar el 100%")
    private BigDecimal titulo;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

}
