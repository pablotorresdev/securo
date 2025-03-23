package com.mb.securo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.mb.securo.entity.maestro.Contacto;
import com.mb.securo.entity.maestro.Producto;
import com.mb.securo.enums.DictamenEnum;
import com.mb.securo.enums.EstadoLoteEnum;
import com.mb.securo.enums.UnidadMedidaEnum;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lotes")
@SQLDelete(sql = "UPDATE lotes SET activo = false WHERE id = ?")
@SQLRestriction("activo = true")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_lote", length = 50, nullable = false)
    private String idLote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_lote", nullable = false)
    private EstadoLoteEnum estadoLote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DictamenEnum dictamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_origen_id")
    private Lote loteOrigen;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "nro_bulto", nullable = false)
    private Integer nroBulto;

    @Column(name = "bultos_totales", nullable = false)
    private Integer bultosTotales;

    @Column(name = "cantidad_inicial", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidadActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedida;

    @Column(name = "nro_remito")
    private String nroRemito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Contacto proveedor;

    @Column(name = "lote_proveedor", nullable = false)
    private String loteProveedor;

    @Column(name = "analisis_proveedor", columnDefinition = "TEXT")
    private String analisisProveedor;

    @Column(name = "orden_elaboracion")
    private String ordenElaboracion;

    @Column(name = "detalle_conservacion", columnDefinition = "TEXT")
    private String detalleConservacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabricante_id", nullable = false)
    private Contacto fabricante;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_reanalisis")
    private LocalDate fechaReanalisis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nro_analisis_id")
    private Analisis nroAnalisis;

    @Column(name = "valoracion_porcentual", precision = 12, scale = 2)
    private BigDecimal valoracionPorcentual;

    @Column
    private String pureza;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

}

