package com.mb.conitrack.entity;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.mb.conitrack.enums.UnidadMedidaEnum;

import static com.mb.conitrack.enums.UnidadMedidaEnum.GRAMO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.KILOGRAMO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.LITRO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.MILIGRAMO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.MILILITRO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.UNIDAD;
import static com.mb.conitrack.enums.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.enums.UnidadMedidaUtils.sugerirUnidadParaCantidad;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MovimientoTest {

    @Test
    void testConversionMismaUnidad() {
        assertEquals(new BigDecimal("10"), convertirCantidadEntreUnidades(GRAMO,new BigDecimal("10"), GRAMO));
    }

    @Test
    void testConversionKgToMg() {
        assertEquals(new BigDecimal("2000000.0"), convertirCantidadEntreUnidades(KILOGRAMO,new BigDecimal("2"),UnidadMedidaEnum.MILIGRAMO));
    }

    @Test
    void testConversionGrToKg() {
        assertEquals(new BigDecimal("0.200"), convertirCantidadEntreUnidades(UnidadMedidaEnum.GRAMO,new BigDecimal("200"), KILOGRAMO));
    }

    @Test
    void testUnidadIdeal_NullValues() {
        assertNull(sugerirUnidadParaCantidad(null, null));
    }

    @Test
    void testUnidadIdeal_Gramos() {
        assertEquals(GRAMO, sugerirUnidadParaCantidad(KILOGRAMO, new BigDecimal("0.85")));
    }

    @Test
    void testUnidadIdeal_Mililitros() {
        assertEquals(MILILITRO, sugerirUnidadParaCantidad(LITRO, new BigDecimal("0.003")));
    }

    @Test
    void testUnidadIdeal_MismoValorYaCorrecto() {
        assertEquals(GRAMO, sugerirUnidadParaCantidad(GRAMO, new BigDecimal("25.00")));
    }

    @Test
    void testUnidadIdeal_Exacta2Decimales() {
        assertEquals(GRAMO, sugerirUnidadParaCantidad(KILOGRAMO, new BigDecimal("0.25")));
    }


    @Test
    void testUnidadIdealUnidadGenerica() {
        assertEquals(UnidadMedidaEnum.UNIDAD, sugerirUnidadParaCantidad(UNIDAD, new BigDecimal("3")));
    }

    @Test
    void testUnidadIdealMasDe2DecimalesRedondeo() {
        assertEquals(MILILITRO, sugerirUnidadParaCantidad(LITRO, new BigDecimal("0.33333")));
    }

    @Test
    void testUnidadIdealNoMejorOpcionDisponible() {
        assertEquals(UnidadMedidaEnum.MICROGRAMO, sugerirUnidadParaCantidad(GRAMO, new BigDecimal("0.0000001")));
    }

    @Test
    void testUnidadIdealYaIdealConDosDecimales() {
        assertEquals(KILOGRAMO, sugerirUnidadParaCantidad(KILOGRAMO, new BigDecimal("123.45")));
    }

    @Test
    void testUnidadIdealMasDe2Decimales_MejorUnidadMenor() {
        assertEquals(UnidadMedidaEnum.MICROGRAMO, sugerirUnidadParaCantidad(MILIGRAMO, new BigDecimal("5.1231")));
    }

    @Test
    void testUnidadIdealMasDe2Decimales_IgualUnidad() {
        assertEquals(UnidadMedidaEnum.MILIGRAMO, sugerirUnidadParaCantidad(MILIGRAMO, new BigDecimal("50.122")));
    }

    @Test
    void testUnidadIdealMasDe2Decimales_IgualUnidad2() {
        assertEquals(LITRO, sugerirUnidadParaCantidad(LITRO, new BigDecimal("50.1")));
    }

    @Test
    void testUnidadIdealMasDe2Decimales_Mayor() {
        assertEquals(LITRO, sugerirUnidadParaCantidad(MILILITRO, new BigDecimal("56780")));
    }

}
