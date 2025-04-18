package com.mb.conitrack.entity.maestro;

import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proveedores")
@SQLDelete(sql = "UPDATE proveedores SET activo = false WHERE id = ?")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private String cuit;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(nullable = false, length = 100)
    private String pais;

    @Column(length = 50)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(name = "contacto", length = 100)
    private String contacto;

    @Column(length = 300)
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

}