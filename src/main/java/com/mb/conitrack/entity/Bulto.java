package com.mb.conitrack.entity;

import java.math.BigDecimal;
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
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bultos")
@SQLDelete(sql = "UPDATE bultos SET activo = false WHERE id = ?")
public class Bulto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonBackReference
    private Lote lote;

    @Column(name = "nro_bulto", nullable = false)
    private Integer nroBulto;

    @Column(name = "cantidad_inicial", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedida;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEnum estado;

    @ManyToMany(mappedBy = "bultos")
    @JsonBackReference
    private Set<Movimiento> movimientos = new HashSet<>();

    @OneToMany(mappedBy = "bulto", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Traza> trazas = new ArrayList<>();

    @Column(nullable = false)
    private Boolean activo;

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

}
