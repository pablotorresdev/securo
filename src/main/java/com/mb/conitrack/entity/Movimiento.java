package com.mb.conitrack.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimientos")
@SQLDelete(sql = "UPDATE movimientos SET activo = false WHERE id = ?")
@ToString(exclude = { "lote" })
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonBackReference
    private Lote lote;

    //TODO: unificar con fecha de movimiento
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaYHoraCreacion;

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
    @JoinColumn(name = "dictamen_inicial", nullable = false)
    private DictamenEnum dictamenInicial;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "dictamen_final", nullable = false)
    private DictamenEnum dictamenFinal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movimiento_origen_id")
    private Movimiento movimientoOrigen;

    @Column(nullable = false)
    private Boolean activo;

}
