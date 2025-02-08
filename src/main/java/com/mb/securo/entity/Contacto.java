package com.mb.securo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contactos")
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "fax", length = 50)
    private String fax;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "referente", length = 100)
    private String referente;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

}
