package com.mb.conitrack.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoLoteEnum;
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
import jakarta.persistence.OneToMany;
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
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_interno", length = 50, nullable = false)
    private String codigoInterno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_lote", nullable = false)
    private EstadoLoteEnum estadoLote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DictamenEnum dictamen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_origen_id")
    private Lote loteOrigen;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "nro_bulto", nullable = false)
    private Integer nroBulto;

    @Column(name = "bultos_totales", nullable = false)
    private Integer bultosTotales;

    @Column(name = "cantidad_inicial", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedida;

    @Column(name = "lote_proveedor", nullable = false)
    private String loteProveedor;

    @Column(name = "nro_remito")
    private String nroRemito;

    @Column(name = "detalle_conservacion")
    private String detalleConservacion;

    @Column(name = "fecha_analisis")
    private LocalDate fechaAnalisis;

    @Column(name = "fecha_reanalisis")
    private LocalDate fechaReanalisis;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @OneToMany(mappedBy = "lote", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Analisis> analisis = new ArrayList<>();

    @Column(name = "titulo", precision = 12, scale = 4)
    private BigDecimal titulo;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "lote", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Movimiento> movimientos = new ArrayList<>();

    @Column(nullable = false)
    private Boolean activo;

}
