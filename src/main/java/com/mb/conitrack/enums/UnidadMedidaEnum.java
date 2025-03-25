package com.mb.conitrack.enums;

public enum UnidadMedidaEnum {
    UNIDAD("Unidad", "Generica", "U", 1.0),

    // Unidades de Masa
    MICROGRAMO("Microgramo", "Masa", "µg", 0.000001),
    MILIGRAMO("Miligramo", "Masa", "mg", 0.001),
    GRAMO("Gramo", "Masa", "g", 1.0),
    KILOGRAMO("Kilogramo", "Masa", "kg", 1000.0),
    TONELADA("Tonelada", "Masa", "t", 1000000.0),

    // Unidades de Volumen
    MICROLITRO("Microlitro", "Volumen", "µL", 0.000001),
    MILILITRO("Mililitro", "Volumen", "mL", 0.001),
    CENTILITRO("Centilitro", "Volumen", "cL", 0.01),
    DECILITRO("Decilitro", "Volumen", "dL", 0.1),
    LITRO("Litro", "Volumen", "L", 1.0),
    MILIMETRO_CUBICO("Milimetro cubico", "Volumen", "mm3", 0.000001),
    CENTIMETRO_CUBICO("Centimetro cubico", "Volumen", "cm3", 0.001),
    METRO_CUBICO("Metro cubico", "Volumen", "m3", 1000.0),

    // Unidades de Superficie
    MILIMETRO_CUADRADO("Milimetro cuadrado", "Superficie", "mm2", 0.000001),
    CENTIMETRO_CUADRADO("Centimetro cuadrado", "Superficie", "cm2", 0.0001),
    METRO_CUADRADO("Metro cuadrado", "Superficie", "m2", 1.0),
    KILOMETRO_CUADRADO("Kilometro cuadrado", "Superficie", "km2", 1000000.0),
    HECTAREA("Hectarea", "Superficie", "ha", 10000.0),

    // Unidades de Longitud
    MICROMETRO("Micrometro", "Longitud", "µm", 0.000001),
    MILIMETRO("Milimetro", "Longitud", "mm", 0.001),
    CENTIMETRO("Centimetro", "Longitud", "cm", 0.01),
    METRO("Metro", "Longitud", "m", 1.0),
    KILOMETRO("Kilometro", "Longitud", "km", 1000.0),

    // Unidades porcentuales
    PORCENTAJE("Porcentaje", "Porcentaje", "%", 0.01),
    PARTES_POR_MILLON("Partes por millon", "Porcentaje", "ppm", 0.000001);

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

    public String getTipo() {
        return tipo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public double getFactorConversion() {
        return factorConversion;
    }
}
