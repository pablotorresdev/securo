package com.mb.conitrack.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimientos")
@SQLDelete(sql = "UPDATE movimientos SET activo = false WHERE id = ?")
@ToString(exclude = { "lote", "movimientoOrigen", "detalles" })
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo_movimiento", length = 100, nullable = false)
    @EqualsAndHashCode.Include
    private String codigoMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonBackReference
    private Lote lote;

    @OneToMany(
        mappedBy = "movimiento", fetch = FetchType.LAZY,
        cascade = { PERSIST, MERGE }, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private Set<DetalleMovimiento> detalles = new HashSet<>();

    //TODO: unificar con fecha de movimiento
    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaYHoraCreacion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimientoEnum tipoMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false)
    private MotivoEnum motivo;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida")
    private UnidadMedidaEnum unidadMedida;

    @Column(name = "nro_analisis", length = 50)
    private String nroAnalisis;

    @Column(name = "orden_produccion", length = 50)
    private String ordenProduccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "dictamen_inicial", nullable = false)
    private DictamenEnum dictamenInicial;

    @Enumerated(EnumType.STRING)
    @Column(name = "dictamen_final", nullable = false)
    private DictamenEnum dictamenFinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_origen_id")
    @EqualsAndHashCode.Exclude
    @JsonBackReference
    private Movimiento movimientoOrigen;

    @Column(nullable = false)
    private Boolean activo;

}
