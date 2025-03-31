package com.mb.conitrack.enums;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum UnidadMedidaEnum {
    UNIDAD("Unidad", "Generica", "U", 1.0),

    // Unidades de Masa
    KILOGRAMO("Kilogramo", "Masa", "kg", 1000.0),
    GRAMO("Gramo", "Masa", "g", 1.0),
    MILIGRAMO("Miligramo", "Masa", "mg", 0.001),
    MICROGRAMO("Microgramo", "Masa", "µg", 0.000001),

    // Unidades de Volumen
    LITRO("Litro", "Volumen", "L", 1.0),
    DECILITRO("Decilitro", "Volumen", "dL", 0.1),
    CENTILITRO("Centilitro", "Volumen", "cL", 0.01),
    MILILITRO("Mililitro", "Volumen", "mL", 0.001),
    CENTIMETRO_CUBICO("Centimetro cubico", "Volumen", "cm3", 0.001),
    MICROLITRO("Microlitro", "Volumen", "µL", 0.000001),
    MILIMETRO_CUBICO("Milimetro cubico", "Volumen", "mm3", 0.000001),

    // Unidades de Superficie
    METRO_CUADRADO("Metro cuadrado", "Superficie", "m2", 1.0),
    CENTIMETRO_CUADRADO("Centimetro cuadrado", "Superficie", "cm2", 0.0001),
    MILIMETRO_CUADRADO("Milimetro cuadrado", "Superficie", "mm2", 0.000001),

    // Unidades de Longitud
    METRO("Metro", "Longitud", "m", 1.0),
    CENTIMETRO("Centimetro", "Longitud", "cm", 0.01),
    MILIMETRO("Milimetro", "Longitud", "mm", 0.001),
    MICROMETRO("Micrometro", "Longitud", "µm", 0.000001),

    // Unidades porcentuales
    PORCENTAJE("Porcentaje", "Porcentaje", "%", 0.01),
    PARTES_POR_MILLON("Partes por millon", "Porcentaje", "ppm", 0.000001);


    public static List<UnidadMedidaEnum> getUnidadesPorTipo(UnidadMedidaEnum base) {
        String tipo = base.getTipo();
        return Arrays.stream(UnidadMedidaEnum.values())
            .filter(u -> u.getTipo().equals(tipo))
            .sorted(Comparator.comparingDouble(UnidadMedidaEnum::getFactorConversion).reversed()) // ASC
            .collect(Collectors.toList());
    }


        public static List<UnidadMedidaEnum> getUnidadesConvertibles(UnidadMedidaEnum base) {
        List<UnidadMedidaEnum> delMismoTipo = getUnidadesPorTipo(base);

        int index = delMismoTipo.indexOf(base);
        int size = delMismoTipo.size();

        int from, to;

        if (index == 0) {
            from = index;
            to = Math.min(index + 4, size);
        } else if (index == 1) {
            from = index - 1;
            to = Math.min(index + 3, size);
        } else if (index == size - 1) {
            from = Math.max(index - 3, 0);
            to = index + 1;
        } else if (index == size - 2) {
            from = Math.max(index - 2, 0);
            to = Math.min(index + 2, size);
        } else {
            from = Math.max(index - 2, 0);
            to = Math.min(index + 3, size);
        }

        // Sublista compatible
        List<UnidadMedidaEnum> compatibles = delMismoTipo.subList(from, to);

        // 👇 Ahora ordenás de mayor a menor
        compatibles.sort(Comparator.comparingDouble(UnidadMedidaEnum::getFactorConversion).reversed());

        return compatibles;
    }


    public String getTipo() {
        return tipo;
    }

    public double getFactorConversion() {
        return factorConversion;
    }

    private final String nombre;

    private final String tipo;

    private final String simbolo;

    private final double factorConversion;

    UnidadMedidaEnum(String nombre, String tipo, String simbolo, double factorConversion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.simbolo = simbolo;
        this.factorConversion = factorConversion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSimbolo() {
        return simbolo;
    }
}
