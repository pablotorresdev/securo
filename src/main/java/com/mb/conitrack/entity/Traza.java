package com.mb.conitrack.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.EstadoEnum;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Entidad: Traza individual (unidad de venta trazable) dentro de un bulto. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trazas")
@SQLDelete(sql = "UPDATE trazas SET activo = false WHERE id = ?")
@ToString(exclude = { "lote", "bulto", "detalles" })
public class Traza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonBackReference
    @EqualsAndHashCode.Include
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulto_id", nullable = false)
    @JsonBackReference
    @EqualsAndHashCode.Include
    private Bulto bulto;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaYHoraCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @EqualsAndHashCode.Include
    private Producto producto;

    @Column(name = "nro_traza", nullable = false)
    @EqualsAndHashCode.Include
    private Long nroTraza;

    @ManyToMany(mappedBy = "trazas", fetch = FetchType.LAZY)
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    private List<DetalleMovimiento> detalles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEnum estado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

}
