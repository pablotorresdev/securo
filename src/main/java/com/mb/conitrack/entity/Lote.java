package com.mb.conitrack.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.SQLDelete;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
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

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaYHoraCreacion;

    @Column(name = "codigo_interno", length = 50, nullable = false)
    private String codigoInterno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fabricante_id")
    private Proveedor fabricante;

    @Column(name = "pais_origen")
    private String paisOrigen;

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

    @Column(name = "fecha_reanal_prov", nullable = false)
    private LocalDate fechaReanalisisProveedor;

    @Column(name = "fecha_vto_prov", nullable = false)
    private LocalDate fechaVencimientoProveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_lote", nullable = false)
    private EstadoLoteEnum estadoLote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DictamenEnum dictamen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_origen_id")
    private Lote loteOrigen;

    @Column(name = "nro_remito")
    private String nroRemito;

    @Column(name = "detalle_conservacion")
    private String detalleConservacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "lote", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Movimiento> movimientos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "lote_analisis",
        joinColumns = @JoinColumn(name = "lote_id"),
        inverseJoinColumns = @JoinColumn(name = "analisis_id")
    )
    @JsonBackReference
    private List<Analisis> analisisList = new ArrayList<>();

    @Column(nullable = false)
    private Boolean activo;

    public Analisis getCurrentAnalisis() {
        if (this.analisisList.isEmpty()) {
            return null;
        } else if (this.analisisList.size() == 1) {
            return this.analisisList.get(0);
        } else {
            return this.analisisList.stream()
                .filter(Analisis::getActivo).max(Comparator.comparing(Analisis::getFechaYHoraCreacion))
                .orElse(null);
        }
    }

}
