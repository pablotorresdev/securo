package com.mb.conitrack.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnidadMedidaEnumTest {

    @Test
    public void testDefaultUnidadMedidaDataSize() {
        UnidadMedidaEnum[] unidades = UnidadMedidaEnum.values();
        assertThat(unidades).hasSize(26);
    }

    @Test
    public void testDefaultUnidadMedidaData() {
        // UNIDAD
        assertThat(UnidadMedidaEnum.UNIDAD.getNombre()).isEqualTo("Unidad");
        assertThat(UnidadMedidaEnum.UNIDAD.getTipo()).isEqualTo("Generica");
        assertThat(UnidadMedidaEnum.UNIDAD.getSimbolo()).isEqualTo("U");
        assertThat(UnidadMedidaEnum.UNIDAD.getFactorConversion()).isEqualTo(1.0);

        // Unidades de Masa
        assertThat(UnidadMedidaEnum.MICROGRAMO.getNombre()).isEqualTo("Microgramo");
        assertThat(UnidadMedidaEnum.MICROGRAMO.getTipo()).isEqualTo("Masa");
        assertThat(UnidadMedidaEnum.MICROGRAMO.getSimbolo()).isEqualTo("µg");
        assertThat(UnidadMedidaEnum.MICROGRAMO.getFactorConversion()).isEqualTo(0.000001);

        assertThat(UnidadMedidaEnum.MILIGRAMO.getNombre()).isEqualTo("Miligramo");
        assertThat(UnidadMedidaEnum.MILIGRAMO.getTipo()).isEqualTo("Masa");
        assertThat(UnidadMedidaEnum.MILIGRAMO.getSimbolo()).isEqualTo("mg");
        assertThat(UnidadMedidaEnum.MILIGRAMO.getFactorConversion()).isEqualTo(0.001);

        assertThat(UnidadMedidaEnum.GRAMO.getNombre()).isEqualTo("Gramo");
        assertThat(UnidadMedidaEnum.GRAMO.getTipo()).isEqualTo("Masa");
        assertThat(UnidadMedidaEnum.GRAMO.getSimbolo()).isEqualTo("g");
        assertThat(UnidadMedidaEnum.GRAMO.getFactorConversion()).isEqualTo(1.0);

        assertThat(UnidadMedidaEnum.KILOGRAMO.getNombre()).isEqualTo("Kilogramo");
        assertThat(UnidadMedidaEnum.KILOGRAMO.getTipo()).isEqualTo("Masa");
        assertThat(UnidadMedidaEnum.KILOGRAMO.getSimbolo()).isEqualTo("kg");
        assertThat(UnidadMedidaEnum.KILOGRAMO.getFactorConversion()).isEqualTo(1000.0);

        assertThat(UnidadMedidaEnum.TONELADA.getNombre()).isEqualTo("Tonelada");
        assertThat(UnidadMedidaEnum.TONELADA.getTipo()).isEqualTo("Masa");
        assertThat(UnidadMedidaEnum.TONELADA.getSimbolo()).isEqualTo("t");
        assertThat(UnidadMedidaEnum.TONELADA.getFactorConversion()).isEqualTo(1000000.0);

        // Unidades de Volumen
        assertThat(UnidadMedidaEnum.MICROLITRO.getNombre()).isEqualTo("Microlitro");
        assertThat(UnidadMedidaEnum.MICROLITRO.getTipo()).isEqualTo("Volumen");
        assertThat(UnidadMedidaEnum.MICROLITRO.getSimbolo()).isEqualTo("µL");
        assertThat(UnidadMedidaEnum.MICROLITRO.getFactorConversion()).isEqualTo(0.000001);

        assertThat(UnidadMedidaEnum.MILILITRO.getNombre()).isEqualTo("Mililitro");
        assertThat(UnidadMedidaEnum.MILILITRO.getTipo()).isEqualTo("Volumen");
        assertThat(UnidadMedidaEnum.MILILITRO.getSimbolo()).isEqualTo("mL");
        assertThat(UnidadMedidaEnum.MILILITRO.getFactorConversion()).isEqualTo(0.001);

        assertThat(UnidadMedidaEnum.CENTILITRO.getNombre()).isEqualTo("Centilitro");
        assertThat(UnidadMedidaEnum.CENTILITRO.getTipo()).isEqualTo("Volumen");
        assertThat(UnidadMedidaEnum.CENTILITRO.getSimbolo()).isEqualTo("cL");
        assertThat(UnidadMedidaEnum.CENTILITRO.getFactorConversion()).isEqualTo(0.01);

        assertThat(UnidadMedidaEnum.DECILITRO.getNombre()).isEqualTo("Decilitro");
        assertThat(UnidadMedidaEnum.DECILITRO.getTipo()).isEqualTo("Volumen");
        assertThat(UnidadMedidaEnum.DECILITRO.getSimbolo()).isEqualTo("dL");
        assertThat(UnidadMedidaEnum.DECILITRO.getFactorConversion()).isEqualTo(0.1);

        assertThat(UnidadMedidaEnum.LITRO.getNombre()).isEqualTo("Litro");
        assertThat(UnidadMedidaEnum.LITRO.getTipo()).isEqualTo("Volumen");
        assertThat(UnidadMedidaEnum.LITRO.getSimbolo()).isEqualTo("L");
        assertThat(UnidadMedidaEnum.LITRO.getFactorConversion()).isEqualTo(1.0);

        assertThat(UnidadMedidaEnum.MILIMETRO_CUBICO.getNombre()).isEqualTo("Milimetro cubico");
        assertThat(UnidadMedidaEnum.MILIMETRO_CUBICO.getTipo()).isEqualTo("Volumen");
        assertThat(UnidadMedidaEnum.MILIMETRO_CUBICO.getSimbolo()).isEqualTo("mm3");
        assertThat(UnidadMedidaEnum.MILIMETRO_CUBICO.getFactorConversion()).isEqualTo(0.000001);

        assertThat(UnidadMedidaEnum.CENTIMETRO_CUBICO.getNombre()).isEqualTo("Centimetro cubico");
        assertThat(UnidadMedidaEnum.CENTIMETRO_CUBICO.getTipo()).isEqualTo("Volumen");
        assertThat(UnidadMedidaEnum.CENTIMETRO_CUBICO.getSimbolo()).isEqualTo("cm3");
        assertThat(UnidadMedidaEnum.CENTIMETRO_CUBICO.getFactorConversion()).isEqualTo(0.001);

        assertThat(UnidadMedidaEnum.METRO_CUBICO.getNombre()).isEqualTo("Metro cubico");
        assertThat(UnidadMedidaEnum.METRO_CUBICO.getTipo()).isEqualTo("Volumen");
        assertThat(UnidadMedidaEnum.METRO_CUBICO.getSimbolo()).isEqualTo("m3");
        assertThat(UnidadMedidaEnum.METRO_CUBICO.getFactorConversion()).isEqualTo(1000.0);

        // Unidades de Superficie
        assertThat(UnidadMedidaEnum.MILIMETRO_CUADRADO.getNombre()).isEqualTo("Milimetro cuadrado");
        assertThat(UnidadMedidaEnum.MILIMETRO_CUADRADO.getTipo()).isEqualTo("Superficie");
        assertThat(UnidadMedidaEnum.MILIMETRO_CUADRADO.getSimbolo()).isEqualTo("mm2");
        assertThat(UnidadMedidaEnum.MILIMETRO_CUADRADO.getFactorConversion()).isEqualTo(0.000001);

        assertThat(UnidadMedidaEnum.CENTIMETRO_CUADRADO.getNombre()).isEqualTo("Centimetro cuadrado");
        assertThat(UnidadMedidaEnum.CENTIMETRO_CUADRADO.getTipo()).isEqualTo("Superficie");
        assertThat(UnidadMedidaEnum.CENTIMETRO_CUADRADO.getSimbolo()).isEqualTo("cm2");
        assertThat(UnidadMedidaEnum.CENTIMETRO_CUADRADO.getFactorConversion()).isEqualTo(0.0001);

        assertThat(UnidadMedidaEnum.METRO_CUADRADO.getNombre()).isEqualTo("Metro cuadrado");
        assertThat(UnidadMedidaEnum.METRO_CUADRADO.getTipo()).isEqualTo("Superficie");
        assertThat(UnidadMedidaEnum.METRO_CUADRADO.getSimbolo()).isEqualTo("m2");
        assertThat(UnidadMedidaEnum.METRO_CUADRADO.getFactorConversion()).isEqualTo(1.0);

        assertThat(UnidadMedidaEnum.KILOMETRO_CUADRADO.getNombre()).isEqualTo("Kilometro cuadrado");
        assertThat(UnidadMedidaEnum.KILOMETRO_CUADRADO.getTipo()).isEqualTo("Superficie");
        assertThat(UnidadMedidaEnum.KILOMETRO_CUADRADO.getSimbolo()).isEqualTo("km2");
        assertThat(UnidadMedidaEnum.KILOMETRO_CUADRADO.getFactorConversion()).isEqualTo(1000000.0);

        assertThat(UnidadMedidaEnum.HECTAREA.getNombre()).isEqualTo("Hectarea");
        assertThat(UnidadMedidaEnum.HECTAREA.getTipo()).isEqualTo("Superficie");
        assertThat(UnidadMedidaEnum.HECTAREA.getSimbolo()).isEqualTo("ha");
        assertThat(UnidadMedidaEnum.HECTAREA.getFactorConversion()).isEqualTo(10000.0);

        // Unidades de Longitud
        assertThat(UnidadMedidaEnum.MICROMETRO.getNombre()).isEqualTo("Micrometro");
        assertThat(UnidadMedidaEnum.MICROMETRO.getTipo()).isEqualTo("Longitud");
        assertThat(UnidadMedidaEnum.MICROMETRO.getSimbolo()).isEqualTo("µm");
        assertThat(UnidadMedidaEnum.MICROMETRO.getFactorConversion()).isEqualTo(0.000001);

        assertThat(UnidadMedidaEnum.MILIMETRO.getNombre()).isEqualTo("Milimetro");
        assertThat(UnidadMedidaEnum.MILIMETRO.getTipo()).isEqualTo("Longitud");
        assertThat(UnidadMedidaEnum.MILIMETRO.getSimbolo()).isEqualTo("mm");
        assertThat(UnidadMedidaEnum.MILIMETRO.getFactorConversion()).isEqualTo(0.001);

        assertThat(UnidadMedidaEnum.CENTIMETRO.getNombre()).isEqualTo("Centimetro");
        assertThat(UnidadMedidaEnum.CENTIMETRO.getTipo()).isEqualTo("Longitud");
        assertThat(UnidadMedidaEnum.CENTIMETRO.getSimbolo()).isEqualTo("cm");
        assertThat(UnidadMedidaEnum.CENTIMETRO.getFactorConversion()).isEqualTo(0.01);

        assertThat(UnidadMedidaEnum.METRO.getNombre()).isEqualTo("Metro");
        assertThat(UnidadMedidaEnum.METRO.getTipo()).isEqualTo("Longitud");
        assertThat(UnidadMedidaEnum.METRO.getSimbolo()).isEqualTo("m");
        assertThat(UnidadMedidaEnum.METRO.getFactorConversion()).isEqualTo(1.0);

        assertThat(UnidadMedidaEnum.KILOMETRO.getNombre()).isEqualTo("Kilometro");
        assertThat(UnidadMedidaEnum.KILOMETRO.getTipo()).isEqualTo("Longitud");
        assertThat(UnidadMedidaEnum.KILOMETRO.getSimbolo()).isEqualTo("km");
        assertThat(UnidadMedidaEnum.KILOMETRO.getFactorConversion()).isEqualTo(1000.0);

        // Unidades porcentuales
        assertThat(UnidadMedidaEnum.PORCENTAJE.getNombre()).isEqualTo("Porcentaje");
        assertThat(UnidadMedidaEnum.PORCENTAJE.getTipo()).isEqualTo("Porcentaje");
        assertThat(UnidadMedidaEnum.PORCENTAJE.getSimbolo()).isEqualTo("%");
        assertThat(UnidadMedidaEnum.PORCENTAJE.getFactorConversion()).isEqualTo(0.01);

        assertThat(UnidadMedidaEnum.PARTES_POR_MILLON.getNombre()).isEqualTo("Partes por millon");
        assertThat(UnidadMedidaEnum.PARTES_POR_MILLON.getTipo()).isEqualTo("Porcentaje");
        assertThat(UnidadMedidaEnum.PARTES_POR_MILLON.getSimbolo()).isEqualTo("ppm");
        assertThat(UnidadMedidaEnum.PARTES_POR_MILLON.getFactorConversion()).isEqualTo(0.000001);
    }

}
