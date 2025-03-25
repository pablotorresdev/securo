package com.mb.conitrack.entity.maestro;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

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
@Table(name = "contactos")
@SQLDelete(sql = "UPDATE contactos SET activo = false WHERE id = ?")
@SQLRestriction("activo = true")
public class Contacto {

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

    @Column(length = 50)
    private String fax;

    @Column(length = 100)
    private String email;

    @Column(name = "persona_contacto", length = 100)
    private String personaContacto;

    @Column(length = 300)
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

}