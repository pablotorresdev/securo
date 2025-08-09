package com.mb.conitrack.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.annotations.SQLDelete;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.utils.UnidadMedidaUtils;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lotes")
@SQLDelete(sql = "UPDATE lotes SET activo = false WHERE id = ?")
@ToString(exclude = {
    "producto", "proveedor", "fabricante", "loteOrigen",
    "bultos", "movimientos", "analisisList", "trazas"   // ⬅️ agregar
})
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
    private List<Bulto> bultos = new ArrayList<>();

    @OneToMany(mappedBy = "lote", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Movimiento> movimientos = new ArrayList<>();

    @OneToMany(
        mappedBy = "lote",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JsonManagedReference
    private List<Analisis> analisisList = new ArrayList<>();

    @OneToMany(mappedBy = "lote", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Traza> trazas = new ArrayList<>();

    @Column(nullable = false)
    private Boolean activo;

    @Deprecated
    @Column(name = "cantidad_inicial", precision = 12, scale = 4)
    private BigDecimal cantidadInicial;

    @Deprecated
    @Column(name = "cantidad_actual", precision = 12, scale = 4)
    private BigDecimal cantidadActual;

    @Deprecated
    @Column(name = "nro_bulto")
    private Integer nroBulto;

    public UnidadMedidaEnum getUnidadMedida() {

        UnidadMedidaEnum uMedActual = null;
        BigDecimal cantidadActual1 = BigDecimal.ZERO;
        BigDecimal cantidadIncial = BigDecimal.ZERO;

        for (Bulto b : bultos) {
            if (cantidadActual1.equals(BigDecimal.ZERO) && uMedActual == null) {
                cantidadIncial = b.getCantidadInicial();
                cantidadActual1 = b.getCantidadActual();
                uMedActual = b.getUnidadMedida();
            } else {
                if (uMedActual.equals(b.getUnidadMedida())) {
                    cantidadIncial = cantidadIncial.add(b.getCantidadInicial());
                    cantidadActual1 = cantidadActual1.add(b.getCantidadActual());
                } else {
                    final UnidadMedidaEnum menorUnidadMedida = UnidadMedidaUtils.obtenerMenorUnidadMedida(
                        uMedActual,
                        b.getUnidadMedida());
                    cantidadIncial = UnidadMedidaUtils.convertirCantidadEntreUnidades(
                        uMedActual,
                        cantidadIncial,
                        menorUnidadMedida).add(
                        UnidadMedidaUtils.convertirCantidadEntreUnidades(
                            b.getUnidadMedida(),
                            b.getCantidadInicial(),
                            menorUnidadMedida)
                    );
                    cantidadActual1 = UnidadMedidaUtils.convertirCantidadEntreUnidades(
                        uMedActual,
                        cantidadActual1,
                        menorUnidadMedida).add(
                        UnidadMedidaUtils.convertirCantidadEntreUnidades(
                            b.getUnidadMedida(),
                            b.getCantidadActual(),
                            menorUnidadMedida)
                    );
                    uMedActual = menorUnidadMedida;
                }
            }
        }

        return uMedActual;
    }

    public void setUnidadMedida(UnidadMedidaEnum unidadMedida) {
        log.warn("setUnidadMedida() is deprecated, use getUnidadMedida() instead.");
    }

    public BigDecimal getCantidadInicial() {
        UnidadMedidaEnum uMedActual = null;

        BigDecimal quantIncial = BigDecimal.ZERO;

        for (Bulto b : bultos) {
            if (quantIncial.equals(BigDecimal.ZERO) && uMedActual == null) {
                quantIncial = b.getCantidadInicial();
                uMedActual = b.getUnidadMedida();
            } else {
                if (uMedActual.equals(b.getUnidadMedida())) {
                    quantIncial = quantIncial.add(b.getCantidadInicial());
                } else {
                    final UnidadMedidaEnum menorUnidadMedida = UnidadMedidaUtils.obtenerMenorUnidadMedida(
                        uMedActual,
                        b.getUnidadMedida());
                    quantIncial = UnidadMedidaUtils.convertirCantidadEntreUnidades(
                        uMedActual,
                        quantIncial,
                        menorUnidadMedida).add(
                        UnidadMedidaUtils.convertirCantidadEntreUnidades(
                            b.getUnidadMedida(),
                            b.getCantidadInicial(),
                            menorUnidadMedida)
                    );
                    uMedActual = menorUnidadMedida;
                }
            }
        }
        return cantidadActual;
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
        if (this.trazas.isEmpty()) {
            return null;
        } else if (this.trazas.size() == 1) {
            return this.trazas.get(0);
        } else {
            return this.trazas.stream()
                .max(Comparator.comparing(Traza::getNroTraza))
                .orElse(null);
        }
    }

    private BigDecimal getCantidadActual(
        UnidadMedidaEnum uMedActual) {

        BigDecimal quantActual = BigDecimal.ZERO;

        for (Bulto b : bultos) {
            if (quantActual.equals(BigDecimal.ZERO) && uMedActual == null) {
                quantActual = b.getCantidadActual();
                uMedActual = b.getUnidadMedida();
            } else {
                if (uMedActual.equals(b.getUnidadMedida())) {
                    quantActual = quantActual.add(b.getCantidadActual());
                } else {
                    final UnidadMedidaEnum menorUnidadMedida = UnidadMedidaUtils.obtenerMenorUnidadMedida(
                        uMedActual,
                        b.getUnidadMedida());
                    quantActual = UnidadMedidaUtils.convertirCantidadEntreUnidades(
                        uMedActual,
                        quantActual,
                        menorUnidadMedida).add(
                        UnidadMedidaUtils.convertirCantidadEntreUnidades(
                            b.getUnidadMedida(),
                            b.getCantidadActual(),
                            menorUnidadMedida)
                    );
                    uMedActual = menorUnidadMedida;
                }
            }
        }
        return quantActual;
    }

}
