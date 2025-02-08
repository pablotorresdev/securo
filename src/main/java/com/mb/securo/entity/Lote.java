package com.mb.securo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "especificacion_producto_id", nullable = false)
    private EspecificacionProducto especificacionProducto;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "id_lote", nullable = false, unique = true, length = 50)
    private String idLote;

    @Column(name = "fecha_elaboracion", nullable = false)
    private LocalDate fechaElaboracion;

    @Column(name = "fecha_caducidad", nullable = false)
    private LocalDate fechaCaducidad;

    @ManyToOne
    @JoinColumn(name = "unidad_medida_id", nullable = false)
    private UnidadMedida unidadMedida;

    @Column(name = "bultos_totales", nullable = false)
    private Integer bultosTotales;

    @Column(name = "nro_bulto", nullable = false)
    private Integer nroBulto;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Contacto proveedor;

    @ManyToOne
    @JoinColumn(name = "fabricante_id", nullable = false)
    private Contacto fabricante;

    @Column(name = "conservacion", columnDefinition = "TEXT")
    private String conservacion;

    @Column(name = "pureza", precision = 5, scale = 2)
    private BigDecimal pureza;

    @Column(name = "estado", nullable = false, length = 10)
    private String estado;  // Valores esperados: 'activo' o 'inactivo'

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "id_analisis_qa", length = 50)
    private String idAnalisisQa;

    @Column(name = "fecha_reanalisis")
    private LocalDate fechaReanalisis;

    @Column(name = "dictamen", columnDefinition = "TEXT")
    private String dictamen;

}
