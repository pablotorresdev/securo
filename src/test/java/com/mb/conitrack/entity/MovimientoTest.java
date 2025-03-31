package com.mb.conitrack.entity;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.enums.UnidadMedidaUtils;

import static com.mb.conitrack.enums.UnidadMedidaEnum.GRAMO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.KILOGRAMO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.LITRO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.MILIGRAMO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.MILILITRO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.UNIDAD;
import static com.mb.conitrack.enums.UnidadMedidaUtils.getCantidadForUnidadDeMedida;
import static com.mb.conitrack.enums.UnidadMedidaUtils.getUnidadMedidaIdeal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MovimientoTest {

    @Test
    void testConversionMismaUnidad() {
        assertEquals(new BigDecimal("10"), getCantidadForUnidadDeMedida(GRAMO,new BigDecimal("10"), GRAMO));
    }

    @Test
    void testConversionKgToMg() {
        assertEquals(new BigDecimal("2000000.0"), getCantidadForUnidadDeMedida(KILOGRAMO,new BigDecimal("2"),UnidadMedidaEnum.MILIGRAMO));
    }

    @Test
    void testConversionGrToKg() {
        assertEquals(new BigDecimal("0.200"), getCantidadForUnidadDeMedida(UnidadMedidaEnum.GRAMO,new BigDecimal("200"), KILOGRAMO));
    }

    @Test
    void testUnidadIdeal_NullValues() {
        assertNull(getUnidadMedidaIdeal(null, null));
    }

    @Test
    void testUnidadIdeal_Gramos() {
        assertEquals(GRAMO, getUnidadMedidaIdeal(KILOGRAMO, new BigDecimal("0.85")));
    }

    @Test
    void testUnidadIdeal_Mililitros() {
        assertEquals(MILILITRO, getUnidadMedidaIdeal(LITRO, new BigDecimal("0.003")));
    }

    @Test
    void testUnidadIdeal_MismoValorYaCorrecto() {
        assertEquals(GRAMO, getUnidadMedidaIdeal(GRAMO, new BigDecimal("25.00")));
    }

    @Test
    void testUnidadIdeal_Exacta2Decimales() {
        assertEquals(GRAMO, getUnidadMedidaIdeal(KILOGRAMO, new BigDecimal("0.25")));
    }


    @Test
    void testUnidadIdealUnidadGenerica() {
        assertEquals(UnidadMedidaEnum.UNIDAD, getUnidadMedidaIdeal(UNIDAD, new BigDecimal("3")));
    }

    @Test
    void testUnidadIdealMasDe2DecimalesRedondeo() {
        assertEquals(MILILITRO, getUnidadMedidaIdeal(LITRO, new BigDecimal("0.33333")));
    }

    @Test
    void testUnidadIdealNoMejorOpcionDisponible() {
        assertEquals(UnidadMedidaEnum.MICROGRAMO, getUnidadMedidaIdeal(GRAMO, new BigDecimal("0.0000001")));
    }

    @Test
    void testUnidadIdealYaIdealConDosDecimales() {
        assertEquals(KILOGRAMO, getUnidadMedidaIdeal(KILOGRAMO, new BigDecimal("123.45")));
    }

    @Test
    void testUnidadIdealMasDe2Decimales_MejorUnidadMenor() {
        assertEquals(UnidadMedidaEnum.MICROGRAMO, getUnidadMedidaIdeal(MILIGRAMO, new BigDecimal("5.1231")));
    }

    @Test
    void testUnidadIdealMasDe2Decimales_IgualUnidad() {
        assertEquals(UnidadMedidaEnum.MILIGRAMO, getUnidadMedidaIdeal(MILIGRAMO, new BigDecimal("50.122")));
    }

    @Test
    void testUnidadIdealMasDe2Decimales_IgualUnidad2() {
        assertEquals(LITRO, getUnidadMedidaIdeal(LITRO, new BigDecimal("50.1")));
    }

    @Test
    void testUnidadIdealMasDe2Decimales_Mayor() {
        assertEquals(LITRO, getUnidadMedidaIdeal(MILILITRO, new BigDecimal("56780")));
    }

}
