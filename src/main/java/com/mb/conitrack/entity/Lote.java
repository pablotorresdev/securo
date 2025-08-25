package com.mb.conitrack.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.annotations.SQLDelete;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lotes")
@SQLDelete(sql = "UPDATE lotes SET activo = false WHERE id = ?")
@ToString(exclude = {
    "producto", "proveedor", "fabricante",
    "bultos", "movimientos", "analisisList", "trazas"
})
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaYHoraCreacion;

    @Column(name = "codigo_lote", length = 50, nullable = false)
    @EqualsAndHashCode.Include
    private String codigoLote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabricante_id")
    private Proveedor fabricante;

    @Column(name = "pais_origen")
    private String paisOrigen;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "bultos_totales", nullable = false)
    private Integer bultosTotales;

    @Column(name = "lote_proveedor", nullable = false)
    private String loteProveedor;

    @Column(name = "fecha_reanal_prov", nullable = false)
    private LocalDate fechaReanalisisProveedor;

    @Column(name = "fecha_vto_prov", nullable = false)
    private LocalDate fechaVencimientoProveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEnum estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DictamenEnum dictamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_origen_id")
    @EqualsAndHashCode.Exclude
    @JsonBackReference
    private Lote loteOrigen;

    @Column(name = "nro_remito")
    private String nroRemito;

    @Column(name = "detalle_conservacion")
    private String detalleConservacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY)
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @OrderBy("nroBulto ASC")
    private List<Bulto> bultos = new ArrayList<>();

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY)
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    private List<Movimiento> movimientos = new ArrayList<>();

    @OneToMany(
        mappedBy = "lote",
        cascade = {PERSIST, MERGE},
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    private List<Analisis> analisisList = new ArrayList<>();

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY)
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    private Set<Traza> trazas = new HashSet<>();

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "cantidad_inicial", precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_actual", precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidadActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedida;


    //****** BULTOS ******//
    public Bulto getBultoByNro(int nroBulto) {
        if (this.bultos == null || this.bultos.isEmpty()) {
            return null;
        }
        if (this.bultos.size() == 1) {
            Bulto unico = this.bultos.get(0);
            return (nroBulto == 1 && unico.getActivo()) ? unico : null;
        }
        return this.bultos.stream()
            .filter(Bulto::getActivo)
            .filter(b -> b.getNroBulto() == nroBulto)
            .findFirst()
            .orElse(null);
    }

    //****** ANALISIS ******//
    public Analisis getUltimoAnalisis() {
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

    public LocalDate getFechaVencimientoVigente() {
        final List<Analisis> list = this.analisisList.stream()
            .filter(Analisis::getActivo)
            .filter(a -> a.getDictamen() != null)
            .filter(a -> a.getFechaVencimiento() != null)
            .toList();
        if (list.isEmpty()) {
            return fechaVencimientoProveedor;
        } else if (list.size() == 1) {
            LocalDate fechaAnalisis = list.get(0).getFechaVencimiento();
            if (fechaAnalisis == null || fechaVencimientoProveedor == null) {
                return fechaAnalisis != null ? fechaAnalisis : fechaVencimientoProveedor;
            }
            LocalDate hoy = LocalDate.now();
            long diffProveedor = Math.abs(ChronoUnit.DAYS.between(hoy, fechaVencimientoProveedor));
            long diffAnalisis = Math.abs(ChronoUnit.DAYS.between(hoy, fechaAnalisis));
            return diffAnalisis <= diffProveedor ? fechaAnalisis : fechaVencimientoProveedor;
        } else {
            throw new IllegalStateException("Hay más de un análisis activo con fecha de vencimiento");
        }
    }

    public LocalDate getFechaReanalisisVigente() {
        Analisis analisis = this.analisisList.stream()
            .filter(Analisis::getActivo)
            .filter(a -> a.getDictamen() != null)
            .filter(a -> a.getFechaReanalisis() != null)
            .min(Comparator.comparing(Analisis::getFechaReanalisis))
            .orElse(null);
        if (analisis == null) {
            return fechaReanalisisProveedor;
        } else {
            LocalDate fechaAnalisis = analisis.getFechaReanalisis();
            if (fechaAnalisis == null || fechaReanalisisProveedor == null) {
                return fechaAnalisis != null ? fechaAnalisis : fechaReanalisisProveedor;
            }
            LocalDate hoy = LocalDate.now();
            long diffProveedor = Math.abs(ChronoUnit.DAYS.between(hoy, fechaReanalisisProveedor));
            long diffAnalisis = Math.abs(ChronoUnit.DAYS.between(hoy, fechaAnalisis));
            return diffAnalisis <= diffProveedor ? fechaAnalisis : fechaReanalisisProveedor;
        }
    }

    public Long getDiasHastaFechaReanalisisVigente() {
        LocalDate fecha = getFechaReanalisisVigente();
        return fecha != null ? ChronoUnit.DAYS.between(LocalDate.now(), fecha) : null;
    }

    public Long getDiasHastaFechaVencimientoVigente() {
        LocalDate fecha = getFechaVencimientoVigente();
        return fecha != null ? ChronoUnit.DAYS.between(LocalDate.now(), fecha) : null;
    }

    public Analisis getUltimoAnalisisDictaminado() {
        return this.analisisList.stream()
            .filter(Analisis::getActivo).filter(a -> a.getDictamen() != null)
            .max(Comparator.comparing(Analisis::getFechaYHoraCreacion))
            .orElse(null);
    }

    public String getNroUltimoAnalisisDictaminado() {
        final Analisis currentAnalisis = getUltimoAnalisisDictaminado();
        if (currentAnalisis == null) {
            return null;
        }
        return currentAnalisis.getNroAnalisis();
    }

    public String getUltimoNroAnalisis() {
        final Analisis currentAnalisis = getUltimoAnalisis();
        if (currentAnalisis == null) {
            return null;
        }
        return currentAnalisis.getNroAnalisis();
    }

    //****** TRAZAS ******//
    public Traza getFirstActiveTraza() {
        if (this.trazas.isEmpty()) {
            return null;
        } else if (this.trazas.size() == 1) {
            return this.trazas.stream().filter(Traza::getActivo).findFirst().orElse(null);
        } else {
            return this.trazas.stream()
                .filter(Traza::getActivo).min(Comparator.comparing(Traza::getNroTraza))
                .orElse(null);
        }
    }

    public List<Traza> getFirstAvailableTrazaList(int size) {
        if (trazas == null || trazas.isEmpty()) {
            return null;
        }

        Stream<Traza> stream = trazas.stream();

        if (trazas.size() > size) {
            stream = stream
                .filter(Traza::getActivo)
                .filter(t -> t.getEstado() == EstadoEnum.DISPONIBLE);
        }

        return stream
            .sorted(Comparator.comparing(Traza::getNroTraza)) // orden ascendente por nroTraza
            .limit(size)                                      // máximo 'size' elementos
            .collect(Collectors.toList());
    }

    public Traza getLastActiveTraza() {
        if (this.trazas.isEmpty()) {
            return null;
        } else if (this.trazas.size() == 1) {
            return this.trazas.stream().filter(Traza::getActivo).findFirst().orElse(null);
        } else {
            return this.trazas.stream()
                .filter(Traza::getActivo).max(Comparator.comparing(Traza::getNroTraza))
                .orElse(null);
        }
    }

    public Traza getTrazaRangeEnd() {
        if (trazas == null || trazas.isEmpty()) {
            return null;
        }
        return trazas.stream()
            .max(Comparator.comparing(Traza::getNroTraza))
            .orElse(null);
    }

    public Traza getTrazaByNro(long nroTraza) {
        if (this.trazas == null || this.trazas.isEmpty()) {
            return null;
        }
        if (this.trazas.size() == 1) {
            Traza unica = this.trazas.stream().filter(Traza::getActivo).findFirst().orElse(null);
            return (unica!=null && unica.getNroTraza() == nroTraza) ? unica : null;
        }
        return this.trazas.stream()
            .filter(Traza::getActivo)
            .filter(t -> t.getNroTraza() == nroTraza)
            .findFirst()
            .orElse(null);
    }

}
