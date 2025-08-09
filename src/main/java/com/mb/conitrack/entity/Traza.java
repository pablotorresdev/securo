package com.mb.conitrack.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.EstadoEnum;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "trazas",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_traza_producto_traza",
        columnNames = { "producto_id", "nro_traza" }
    )
)
@SQLDelete(sql = "UPDATE traza SET activo = false WHERE id = ?")
@ToString(exclude = { "lote", "bulto", "movimientos" }) // ⬅️ agregar "bulto"
public class Traza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonBackReference
    private Lote lote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bulto_id", nullable = false)
    @JsonBackReference
    private Bulto bulto;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaYHoraCreacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "nro_traza", nullable = false)
    private Long nroTraza;

    @ManyToMany(mappedBy = "trazas", fetch = FetchType.EAGER)
    @JsonBackReference
    private Set<Movimiento> movimientos = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEnum estado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

}
