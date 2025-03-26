package com.mb.conitrack.entity.maestro;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.mb.conitrack.enums.TipoProductoEnum;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "productos", uniqueConstraints = @UniqueConstraint(
    columnNames = { "nombreGenerico", "codigoInterno", "tipo_producto" }
))
@SQLDelete(sql = "UPDATE productos SET activo = false WHERE id = ?")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_generico", nullable = false)
    private String nombreGenerico;

    @Column(name = "codigo_interno", length = 50, nullable = false, unique = true)
    private String codigoInterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto", nullable = false, length = 50)
    private TipoProductoEnum tipoProducto;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedida;

    @Column(name = "pais_origen", nullable = false)
    private String paisOrigen;

    //TODO: Obligatorio para: API, Semielaborado, Acond. secundario
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_destino_id")
    private Producto productoDestino;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

}
