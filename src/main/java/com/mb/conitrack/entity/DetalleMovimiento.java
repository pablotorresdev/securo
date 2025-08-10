package com.mb.conitrack.entity;

import java.math.BigDecimal;

import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

@Entity
@Table(
    name = "detalle_movimientos",
    uniqueConstraints = @UniqueConstraint(columnNames = { "movimiento_id", "bulto_id" })
)
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
    private Movimiento movimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bulto_id", nullable = false)
    private Bulto bulto;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 50)
    private UnidadMedidaEnum unidadMedida;

}

