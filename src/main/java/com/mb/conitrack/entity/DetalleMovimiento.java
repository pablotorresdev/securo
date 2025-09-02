package com.mb.conitrack.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "detalle_movimientos")
@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.Builder
@lombok.EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DetalleMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movimiento_id", nullable = false)
    @EqualsAndHashCode.Include
    private Movimiento movimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bulto_id", nullable = false)
    @EqualsAndHashCode.Include
    private Bulto bulto;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 50)
    private UnidadMedidaEnum unidadMedida;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "trazas_detalles",
        joinColumns = @JoinColumn(name = "detalle_id"),
        inverseJoinColumns = @JoinColumn(name = "traza_id")
    )
    @JsonManagedReference
    @Builder.Default
    private Set<Traza> trazas = new HashSet<>();

}

