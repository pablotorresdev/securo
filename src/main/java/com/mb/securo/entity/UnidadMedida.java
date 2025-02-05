package com.mb.securo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String type; // Masa, Volumen, Longitud, Superficie, Genérico

    @Column(nullable = false, unique = true)
    private String symbol; // Símbolo de la unidad (m, kg, L, etc.)

    @Column(nullable = false)
    private Double conversionFactor; // Factor de conversión a la unidad base

}

