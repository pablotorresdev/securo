package com.mb.securo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "orden_produccion", length = 50)
    private String ordenProduccion;

    @Column(name = "id_analisis_qa", length = 50)
    private String idAnalisisQa;

    @Column(name = "nro_analisis", length = 50)
    private String nroAnalisis;

    @ManyToOne
    @JoinColumn(name = "ref_lote_origen", nullable = false)
    private Lote loteOrigen;

    @ManyToOne
    @JoinColumn(name = "ref_lote_destino", nullable = false)
    private Lote loteDestino;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @ManyToOne
    @JoinColumn(name = "unidad_medida_id", nullable = false)
    private UnidadMedida unidadMedida;

    @ManyToOne
    @JoinColumn(name = "tipo_mpovimiento_id", nullable = false)
    private TipoMovimiento tipo;

}
