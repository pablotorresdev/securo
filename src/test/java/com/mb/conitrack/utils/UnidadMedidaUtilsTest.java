package com.mb.conitrack.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static com.mb.conitrack.enums.UnidadMedidaEnum.GRAMO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.KILOGRAMO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.LITRO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.MILIGRAMO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.MILILITRO;
import static com.mb.conitrack.enums.UnidadMedidaEnum.UNIDAD;
import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.sugerirUnidadParaCantidad;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class UnidadMedidaUtilsTest {

    private static void assertApprox(BigDecimal expected, BigDecimal actual, double eps) {
        BigDecimal diff = expected.subtract(actual).abs();
        assertTrue(diff.doubleValue() <= eps, () -> "diff=" + diff);
    }

    private static MovimientoDTO dto(BigDecimal cantidad, UnidadMedidaEnum u) {
        MovimientoDTO d = new MovimientoDTO();
        d.setCantidad(cantidad);
        d.setUnidadMedida(u);
        return d;
    }

    /**
     * Elige una unidad != UNIDAD con al menos 2 compatibles para tests de conversión/elección.
     */
    private static UnidadMedidaEnum pickNonUnidadWithChain() {
        for (UnidadMedidaEnum u : UnidadMedidaEnum.values()) {
            if (u == UnidadMedidaEnum.UNIDAD) {
                continue;
            }
            List<UnidadMedidaEnum> chain = UnidadMedidaEnum.getUnidadesPorTipo(u);
            if (chain != null && chain.size() >= 2 && chain.contains(u)) {
                return u;
            }
        }
        fail("No hay una unidad (≠ UNIDAD) con cadena de compatibilidad >= 2");
        return null;
    }

    @Test
    @DisplayName("Cantidad y unidad nulos o UNIDAD → devuelve la misma unidad")
    void casoBase() {
        assertEquals(
            UnidadMedidaEnum.UNIDAD,
            UnidadMedidaUtils.sugerirUnidadParaCantidad(UnidadMedidaEnum.UNIDAD, new BigDecimal("5")));
    }

    @Test
    @DisplayName("Cantidad es null")
    void casoCantidadNull() {
        assertEquals(UnidadMedidaEnum.GRAMO, UnidadMedidaUtils.sugerirUnidadParaCantidad(UnidadMedidaEnum.GRAMO, null));
    }

    @Test
    @DisplayName("UnidadMedida es null")
    void casoUnidadMedidaNull() {
        assertNull(UnidadMedidaUtils.sugerirUnidadParaCantidad(null, new BigDecimal("5")));
    }

    @Test
    @DisplayName("convertirCantidadEntreUnidades: unidades diferentes")
    void convertir_distintasUnidades() {
        BigDecimal kg = new BigDecimal("1");                  // 1 kg
        BigDecimal g = UnidadMedidaUtils.convertirCantidadEntreUnidades(
            UnidadMedidaEnum.KILOGRAMO,
            kg,
            UnidadMedidaEnum.GRAMO);

        // 1 kg → 1000 g  (factor KG/G = 1 / 0.001)
        assertEquals(new BigDecimal("1000.000"), g.setScale(3));
    }

    @Test
    @DisplayName("convertirCantidadEntreUnidades: misma unidad → misma cantidad")
    void convertir_mismaUnidad() {
        BigDecimal in = new BigDecimal("12.34");
        BigDecimal out = UnidadMedidaUtils.convertirCantidadEntreUnidades(
            UnidadMedidaEnum.KILOGRAMO,
            in,
            UnidadMedidaEnum.KILOGRAMO);

        assertSame(in, out);  // Devuelve exactamente la misma instancia
    }

    @Test
    @DisplayName("convertirCantidadEntreUnidades: misma unidad → idéntico")
    void convertir_mismaUnidad2() {
        BigDecimal q = new BigDecimal("12.345");
        BigDecimal out = UnidadMedidaUtils.convertirCantidadEntreUnidades(
            UnidadMedidaEnum.UNIDAD,
            q,
            UnidadMedidaEnum.UNIDAD);
        assertSame(q, out, "Debe devolver la misma instancia si la unidad coincide");
    }

    @Test
    @DisplayName("convertirCantidadEntreUnidades: ida y vuelta conserva valor (≈)")
    void convertir_roundTrip() {
        UnidadMedidaEnum u = pickNonUnidadWithChain();
        List<UnidadMedidaEnum> chain = UnidadMedidaEnum.getUnidadesPorTipo(u);
        // Tomamos dos unidades distintas del mismo tipo
        UnidadMedidaEnum u0 = chain.get(0);
        UnidadMedidaEnum u1 = chain.get(1);

        BigDecimal q = new BigDecimal("123.456");

        BigDecimal toU1 = UnidadMedidaUtils.convertirCantidadEntreUnidades(u0, q, u1);
        BigDecimal back = UnidadMedidaUtils.convertirCantidadEntreUnidades(u1, toU1, u0);

        // por uso de double internamente, comparamos con tolerancia
        assertApprox(q, back, 1e-9);
    }

    @Test
    @DisplayName("Con factores iguales devuelve unidadDos (rama else)")
    void menorUnidad_factoresIguales() {
        // Dos unidades idénticas (o factor igual) → se toma unidadDos por diseño
        UnidadMedidaEnum menor = UnidadMedidaUtils.obtenerMenorUnidadMedida(
            UnidadMedidaEnum.GRAMO,
            UnidadMedidaEnum.GRAMO);   // mismos factores

        assertEquals(UnidadMedidaEnum.GRAMO, menor);
    }

    @Test
    @DisplayName("obtenerMenorUnidadMedida: distinto tipo → IllegalArgumentException")
    void menorUnidad_incompatible() {
        // Buscamos dos tipos distintos de unidades
        UnidadMedidaEnum tipo1 = null, tipo2 = null;
        outer:
        for (UnidadMedidaEnum u1 : UnidadMedidaEnum.values()) {
            for (UnidadMedidaEnum u2 : UnidadMedidaEnum.values()) {
                if (u1 != u2 && u1.getTipo() != null && u2.getTipo() != null && !u1.getTipo().equals(u2.getTipo())) {
                    tipo1 = u1;
                    tipo2 = u2;
                    break outer;
                }
            }
        }
        if (tipo1 == null) {
            fail("No se hallaron dos unidades de distinto tipo para la prueba");
        }

        final UnidadMedidaEnum finalTipo = tipo1;
        final UnidadMedidaEnum finalTipo1 = tipo2;
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> UnidadMedidaUtils.obtenerMenorUnidadMedida(finalTipo, finalTipo1));
        assertEquals("Las unidades de medida no son compatibles", ex.getMessage());
    }

    @Test
    @DisplayName("obtenerMenorUnidadMedida: tipos incompatibles → IllegalArgumentException")
    void menorUnidad_incompatible2() {
        assertThrows(
            IllegalArgumentException.class,
            () -> UnidadMedidaUtils.obtenerMenorUnidadMedida(
                UnidadMedidaEnum.GRAMO,
                UnidadMedidaEnum.LITRO));        // Masa vs Volumen
    }

    @Test
    @DisplayName("obtenerMenorUnidadMedida: mismo tipo → devuelve la de menor factor")
    void menorUnidad_ok() {
        UnidadMedidaEnum base = pickNonUnidadWithChain();
        List<UnidadMedidaEnum> chain = UnidadMedidaEnum.getUnidadesPorTipo(base);

        UnidadMedidaEnum a = chain.get(0);
        UnidadMedidaEnum b = chain.get(1);

        UnidadMedidaEnum expected = a.getFactorConversion() < b.getFactorConversion() ? a : b;
        UnidadMedidaEnum out = UnidadMedidaUtils.obtenerMenorUnidadMedida(a, b);
        assertEquals(expected, out);
    }

    @Test
    @DisplayName("obtenerMenorUnidadMedida: devuelve la menor")
    void menorUnidad_ok2() {
        UnidadMedidaEnum menor = UnidadMedidaUtils.obtenerMenorUnidadMedida(
            UnidadMedidaEnum.MILIGRAMO,
            UnidadMedidaEnum.GRAMO);

        assertEquals(UnidadMedidaEnum.MILIGRAMO, menor);
    }

    @Test
    @DisplayName("Devuelve unidadDos cuando su factor es menor que el de unidadUno")
    void menorUnidad_unidadDosEsMenor() {
        // KG (factor 1 000) vs MG (factor 0.001) → MG debe ganar
        UnidadMedidaEnum menor = UnidadMedidaUtils.obtenerMenorUnidadMedida(
            UnidadMedidaEnum.KILOGRAMO,
            UnidadMedidaEnum.MILIGRAMO);

        assertEquals(UnidadMedidaEnum.MILIGRAMO, menor);   // rama “else”
    }

    @Test
    @DisplayName("ordenDeMagnitudBase10: null/0 → 0")
    void ordenDeMagnitudBase10_null_y_cero() {
        assertEquals(0, UnidadMedidaUtils.ordenDeMagnitudBase10(null));
        assertEquals(0, UnidadMedidaUtils.ordenDeMagnitudBase10(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("ordenDeMagnitudBase10: enteros y decimales")
    void ordenDeMagnitudBase10_varios() {

        assertEquals(0, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("1")));
        assertEquals(1, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("10")));
        assertEquals(2, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("100")));
        assertEquals(3, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("1200")));
        assertEquals(-3, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("0.00123")));
        assertEquals(-4, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("0.00056")));
    }

    @Test
    @DisplayName("restarMovimientoConvertido(Bulto): descuenta según unidades")
    void restarMovimiento() {
        // Bulto con 10 kg
        Bulto bulto = new Bulto();
        bulto.setCantidadActual(new BigDecimal("10"));
        bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        // Movimiento: 500 g  (0.5 kg)
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCantidad(new BigDecimal("500"));
        dto.setUnidadMedida(UnidadMedidaEnum.GRAMO);

        BigDecimal result = UnidadMedidaUtils.restarMovimientoConvertido(dto, bulto);

        assertEquals(new BigDecimal("9.5"), result.stripTrailingZeros());
    }

    @Test
    @DisplayName("restarMovimientoConvertido(Lote): descuenta según unidades")
    void restarMovimiento_lote() {
        // Lote con 10 kg
        Lote lote = new Lote();
        lote.setCantidadActual(new BigDecimal("10"));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        // Movimiento: 500 g  (0.5 kg)
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCantidad(new BigDecimal("500"));
        dto.setUnidadMedida(UnidadMedidaEnum.GRAMO);

        BigDecimal result = UnidadMedidaUtils.restarMovimientoConvertido(dto, lote);

        assertEquals(new BigDecimal("9.5"), result.stripTrailingZeros());
    }

    @Test
    @DisplayName("restarMovimientoConvertido(Lote): misma unidad")
    void restarMovimiento_lote_mismaUnidad() {
        // Lote con 100 unidades
        Lote lote = new Lote();
        lote.setCantidadActual(new BigDecimal("100"));
        lote.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        // Movimiento: 25 unidades
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCantidad(new BigDecimal("25"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        BigDecimal result = UnidadMedidaUtils.restarMovimientoConvertido(dto, lote);

        assertEquals(new BigDecimal("75"), result.stripTrailingZeros());
    }

    @Test
    @DisplayName("restarMovimientoConvertido(Bulto): unidades compatibles → resta con conversión (≈)")
    void restar_bulto_convertido() {
        UnidadMedidaEnum u = pickNonUnidadWithChain();
        List<UnidadMedidaEnum> chain = UnidadMedidaEnum.getUnidadesPorTipo(u);
        UnidadMedidaEnum uBulto = chain.get(0);
        UnidadMedidaEnum uDto = chain.get(1);

        Bulto b = new Bulto();
        b.setCantidadActual(new BigDecimal("2"));
        b.setUnidadMedida(uBulto);

        MovimientoDTO dto = dto(new BigDecimal("250"), uDto);

        double factorBulto = uBulto.getFactorConversion();
        double factorDto = uDto.getFactorConversion();
        BigDecimal expected = new BigDecimal("2").subtract(new BigDecimal("250").multiply(BigDecimal.valueOf(factorDto /
            factorBulto)));

        BigDecimal out = UnidadMedidaUtils.restarMovimientoConvertido(dto, b);
        assertApprox(expected, out, 1e-9);
    }

    @Test
    @DisplayName("restarMovimientoConvertido(Bulto): misma unidad → resta directa")
    void restar_bulto_mismaUnidad() {
        Bulto b = new Bulto();
        b.setCantidadActual(new BigDecimal("8"));
        b.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        MovimientoDTO dto = dto(new BigDecimal("2"), UnidadMedidaEnum.UNIDAD);
        BigDecimal out = UnidadMedidaUtils.restarMovimientoConvertido(dto, b);

        assertEquals(new BigDecimal("6.0"), out);
    }

    @Test
    @DisplayName("Cantidad intermedia que NO cambia la unidad (rama final)")
    void sinCambioDeUnidad() {
        // 25 kg no dispara ni la rama de “muy pequeña” ni la de “muy grande”
        UnidadMedidaEnum result = UnidadMedidaUtils.sugerirUnidadParaCantidad(
            UnidadMedidaEnum.KILOGRAMO,
            new BigDecimal("25"));

        assertEquals(UnidadMedidaEnum.KILOGRAMO, result); // se queda igual
    }

    @Test
    @DisplayName("Cantidad intermedia que NO cambia la unidad (rama final)")
    void sinCambioDeUnidad2() {
        // 25 kg no dispara ni la rama de “muy pequeña” ni la de “muy grande”
        UnidadMedidaEnum result = UnidadMedidaUtils.sugerirUnidadParaCantidad(
            UnidadMedidaEnum.GRAMO,
            new BigDecimal("25000"));

        assertEquals(UnidadMedidaEnum.KILOGRAMO, result); // se queda igual
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad muy grande entera → sugiere unidad mayor (índice menor)")
    void sugerir_cantidadGrandeEntera() {
        UnidadMedidaEnum u = pickNonUnidadWithChain();
        List<UnidadMedidaEnum> chain = UnidadMedidaEnum.getUnidadesPorTipo(u);

        // Tomamos alguna relativamente “menor” (si hay al menos 2, usar la última para forzar subida)
        UnidadMedidaEnum base = chain.get(chain.size() - 1);
        int idxBase = chain.indexOf(base);

        UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(base, new BigDecimal("100000"));
        assertTrue(chain.contains(sugerida));
        // si hay “mayores” disponibles debería subir (índice menor). Si no, al menos permanece.
        assertTrue(chain.indexOf(sugerida) <= idxBase);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad pequeña/decimal → sugiere unidad menor (índice mayor)")
    void sugerir_cantidadPequena() {
        UnidadMedidaEnum u = pickNonUnidadWithChain();
        List<UnidadMedidaEnum> chain = UnidadMedidaEnum.getUnidadesPorTipo(u);
        UnidadMedidaEnum base = chain.get(0); // alguna “mayor”
        int idxBase = chain.indexOf(base);

        UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(base, new BigDecimal("0.5"));
        assertTrue(chain.contains(sugerida));
        assertTrue(chain.indexOf(sugerida) >= idxBase, "Debe moverse a igual o menor (índice mayor)");
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: 5.1234 (<10 y >2 decimales) → busca unidad menor")
    void sugerir_decimalesMuchos() {
        UnidadMedidaEnum u = pickNonUnidadWithChain();
        List<UnidadMedidaEnum> chain = UnidadMedidaEnum.getUnidadesPorTipo(u);
        UnidadMedidaEnum base = chain.get(0);
        int idxBase = chain.indexOf(base);

        UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(base, new BigDecimal("5.1234"));
        assertTrue(chain.contains(sugerida));
        assertTrue(chain.indexOf(sugerida) >= idxBase);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad=null | unidad=null | UNIDAD → retorna unidad original")
    void sugerir_triviales() {
        assertNull(UnidadMedidaUtils.sugerirUnidadParaCantidad(null, new BigDecimal("1")));
        assertEquals(
            UnidadMedidaEnum.UNIDAD,
            UnidadMedidaUtils.sugerirUnidadParaCantidad(UnidadMedidaEnum.UNIDAD, new BigDecimal("5")));
        // cantidad null
        assertEquals(
            UnidadMedidaEnum.KILOGRAMO,
            UnidadMedidaUtils.sugerirUnidadParaCantidad(UnidadMedidaEnum.KILOGRAMO, null));
    }

    @Test
    @DisplayName("Cantidad muy grande y sin decimales → sugiere unidad mayor")
    void sugiereMayor() {
        UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
            UnidadMedidaEnum.MILIGRAMO,
            new BigDecimal("100000000")); // 100 000 000 mg

        assertTrue(sugerida.getFactorConversion() > UnidadMedidaEnum.MILIGRAMO.getFactorConversion());
    }

    @Test
    @DisplayName("Cantidad muy pequeña → sugiere unidad menor GRAMO")
    void sugiereMenor() {
        UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
            UnidadMedidaEnum.KILOGRAMO,
            new BigDecimal("0.00025"));   // 0.25 kg

        assertSame(sugerida, MILIGRAMO);
    }

    @Test
    @DisplayName("Cantidad muy pequeña → sugiere unidad menor LITRO")
    void sugiereMenor2() {
        UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
            UnidadMedidaEnum.LITRO,
            new BigDecimal("0.09"));   // 0.25 kg

        assertEquals(sugerida.getFactorConversion(), UnidadMedidaEnum.CENTILITRO.getFactorConversion());
    }

    @Test
    @DisplayName("Cantidad muy pequeña → sugiere unidad menor")
    void sugiereMenor3() {
        UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
            UnidadMedidaEnum.LITRO,
            new BigDecimal("0.000000000002"));   // 0.25 kg

        assertEquals(sugerida, UnidadMedidaEnum.MILIMETRO_CUBICO);
    }

    @Test
    @DisplayName("Búsqueda de unidad menor: sale por el último índice (condición i == last)")
    void sugiereMenor_enUltimoIntento() {
        // 0.00007 kg (7e-7) ↓ bajamos sucesivamente hasta alcanzar la última unidad (microgramo)
        UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
            UnidadMedidaEnum.KILOGRAMO,
            new BigDecimal("0.0000007"));

        assertEquals(UnidadMedidaEnum.MICROGRAMO, sugerida);
    }

    @Test
    void testConversionGrToKg() {
        assertEquals(
            new BigDecimal("0.200"),
            convertirCantidadEntreUnidades(UnidadMedidaEnum.GRAMO, new BigDecimal("200"), KILOGRAMO));
    }

    @Test
    void testConversionKgToMg() {
        assertEquals(
            new BigDecimal("2000000.0"),
            convertirCantidadEntreUnidades(KILOGRAMO, new BigDecimal("2"), UnidadMedidaEnum.MILIGRAMO));
    }

    @Test
    void testConversionMismaUnidad() {
        assertEquals(new BigDecimal("10"), convertirCantidadEntreUnidades(GRAMO, new BigDecimal("10"), GRAMO));
    }

    @Test
    void testUnidadIdealMasDe2DecimalesRedondeo() {
        assertEquals(MILILITRO, sugerirUnidadParaCantidad(LITRO, new BigDecimal("0.33333")));
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

    @Test
    void testUnidadIdealMasDe2Decimales_MejorUnidadMenor() {
        assertEquals(UnidadMedidaEnum.MICROGRAMO, sugerirUnidadParaCantidad(MILIGRAMO, new BigDecimal("5.1231")));
    }

    @Test
    void testUnidadIdealNoMejorOpcionDisponible() {
        assertEquals(UnidadMedidaEnum.MICROGRAMO, sugerirUnidadParaCantidad(GRAMO, new BigDecimal("0.0000001")));
    }

    @Test
    void testUnidadIdealUnidadGenerica() {
        assertEquals(UnidadMedidaEnum.UNIDAD, sugerirUnidadParaCantidad(UNIDAD, new BigDecimal("3")));
    }

    @Test
    void testUnidadIdealYaIdealConDosDecimales() {
        assertEquals(KILOGRAMO, sugerirUnidadParaCantidad(KILOGRAMO, new BigDecimal("123.45")));
    }

    @Test
    void testUnidadIdeal_Exacta2Decimales() {
        assertEquals(GRAMO, sugerirUnidadParaCantidad(KILOGRAMO, new BigDecimal("0.25")));
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
    void testUnidadIdeal_NullValues() {
        assertNull(sugerirUnidadParaCantidad(null, null));
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad extremadamente pequeña → fuerza último índice (MICROGRAMO)")
    void sugerirUnidad_cantidadExtremadamentePequena_fuerzaUltimoIndice() {
        // Caso extremo: cantidad tan pequeña que llega hasta la unidad más pequeña
        // 0.000000001 mg = 0.001 µg (muy pequeño, pero llega a microgramo por ser el último)
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.MILIGRAMO,
            new BigDecimal("0.000000001")
        );

        assertEquals(UnidadMedidaEnum.MICROGRAMO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad extremadamente grande → fuerza primer índice (KILOGRAMO)")
    void sugerirUnidad_cantidadExtremadamenteGrande_fuerzaPrimerIndice() {
        // Caso extremo: cantidad tan grande desde GRAMO que llega hasta KILOGRAMO (i == 0)
        // 100000000 g = 100000 kg
        // Debería forzar el retorno de KILOGRAMO cuando i == 0
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.GRAMO,
            new BigDecimal("100000000")
        );

        assertEquals(UnidadMedidaEnum.KILOGRAMO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: volumen extremadamente pequeño → fuerza unidad más pequeña")
    void sugerirUnidad_volumenExtremadamentePequeno() {
        // 0.0000001 mL → debería llegar hasta la unidad más pequeña (último índice)
        // Puede ser MICROLITRO o MILIMETRO_CUBICO (ambos tienen mismo factor de conversión)
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.MILILITRO,
            new BigDecimal("0.0000001")
        );

        // Accept either MICROLITRO or MILIMETRO_CUBICO since they have the same conversion factor
        assertTrue(
            sugerida == UnidadMedidaEnum.MICROLITRO || sugerida == UnidadMedidaEnum.MILIMETRO_CUBICO,
            "Expected MICROLITRO or MILIMETRO_CUBICO but got " + sugerida
        );
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: volumen muy grande → fuerza LITRO")
    void sugerirUnidad_volumenMuyGrande() {
        // 100000000 mL = 100000 L (debería forzar retorno de LITRO cuando i == 0)
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.MILILITRO,
            new BigDecimal("100000000")
        );

        assertEquals(UnidadMedidaEnum.LITRO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: 0.00001 g → debe explorar hasta encontrar mejor unidad")
    void sugerirUnidad_cantidadPequenaExploraHastaEncontrar() {
        // 0.00001 g = 0.01 mg = 10 µg
        // Debería sugerir MICROGRAMO (10 µg es legible)
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.GRAMO,
            new BigDecimal("0.00001")
        );

        assertEquals(UnidadMedidaEnum.MICROGRAMO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: ya está en unidad más pequeña → sin cambio")
    void sugerirUnidad_yaEnUnidadMasPequena_noCambia() {
        // MICROGRAMO es la unidad de masa más pequeña
        // Con cantidad pequeña, no puede buscar unidad menor, retorna misma unidad
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.MICROGRAMO,
            new BigDecimal("0.5")
        );

        assertEquals(UnidadMedidaEnum.MICROGRAMO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: ya está en unidad más grande → sin cambio")
    void sugerirUnidad_yaEnUnidadMasGrande_noCambia() {
        // KILOGRAMO es la unidad de masa más grande
        // Con cantidad muy grande, no puede buscar unidad mayor, retorna misma unidad
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.KILOGRAMO,
            new BigDecimal("999999")
        );

        assertEquals(UnidadMedidaEnum.KILOGRAMO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad grande con decimales → sin conversión")
    void sugerirUnidad_cantidadGrandeConDecimales_sinConversion() {
        // 1000.5 g tiene potenciaBase10 > 2 pero tiene decimales (scale >= 1)
        // No debe buscar unidad mayor porque tiene decimales
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.GRAMO,
            new BigDecimal("1000.5")
        );

        assertEquals(UnidadMedidaEnum.GRAMO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad [1,10) con muchos decimales → busca menor")
    void sugerirUnidad_cantidadMediaConMuchosDecimales_buscaMenor() {
        // 5.123 g está entre 1 y 10 y tiene más de 2 decimales (scale = 3)
        // Debería buscar unidad menor para mejor legibilidad
        // 5.123 g = 5123 mg (scale 0 después de strip), cumple condiciones
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.GRAMO,
            new BigDecimal("5.123")
        );

        // Debería sugerir miligramo (5123 mg es más legible)
        assertEquals(UnidadMedidaEnum.MILIGRAMO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad >= 10 con pocos decimales → no busca menor")
    void sugerirUnidad_cantidadMayorIgual10_noBuscaMenor() {
        // 15.5 g es >= 10 y tiene solo 1 decimal (scale = 1)
        // No cumple condiciones para buscar unidad menor
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.GRAMO,
            new BigDecimal("15.5")
        );

        // Debe mantener GRAMO
        assertEquals(UnidadMedidaEnum.GRAMO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad < 10 pero scale <= 2 → no busca menor")
    void sugerirUnidad_cantidadMenor10PeroPocoDecimales_noBuscaMenor() {
        // 5.12 g es < 10 pero tiene solo 2 decimales (scale = 2, NO > 2)
        // La condición (cantidad < 10 && scale > 2) es false
        // Por lo tanto, no busca unidad menor
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.GRAMO,
            new BigDecimal("5.12")  // scale = 2, not > 2
        );

        // Debe mantener GRAMO
        assertEquals(UnidadMedidaEnum.GRAMO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad grande → explora múltiples unidades mayores")
    void sugerirUnidad_cantidadGrande_exploraMayores() {
        // 10000 cL es una cantidad grande (potencia = 4)
        // 10000 cL = 100 L (exactamente 100, debería cumplir < 100 es false o justo en el límite)
        // Esto prueba el recorrido del loop buscando la unidad mayor adecuada
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.CENTILITRO,
            new BigDecimal("10000")  // 10000 cL = 100 L
        );

        // Debería sugerir LITRO
        assertEquals(UnidadMedidaEnum.LITRO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: conversión grande con decimals en escala intermedia")
    void sugerirUnidad_conversionGrandeConDecimalesIntermedios() {
        // 50000 mL es una cantidad grande sin decimales
        // 50000 mL = 50 L (< 100, scale = 0 ≤ 3) -> debería sugerir LITRO
        // Este test ayuda a cubrir el camino donde la conversión SÍ cumple las condiciones
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.MILILITRO,
            new BigDecimal("50000")
        );

        assertEquals(UnidadMedidaEnum.LITRO, sugerida);
    }

    @Test
    @DisplayName("sugerirUnidadParaCantidad: 100001 µg → explora múltiples conversiones con escala alta")
    void sugerirUnidad_microgramos_exploraConversionesConEscalaAlta() {
        // 100001 µg es una cantidad grande (potenciaBase10 = 5 > 2, scale = 0 < 1)
        // Al buscar unidad mayor:
        // - A mg: 100001 * 0.001 = 100.001 mg (>= 100, no cumple)
        // - A g:  100001 * 0.000001 = 0.100001 g (< 100 ✓ pero scale = 6 > 3, NO cumple por escala)
        // - A kg: i == 0, fuerza retorno
        // Este test cubre el branch donde convertida < 100 pero scale > 3
        UnidadMedidaEnum sugerida = sugerirUnidadParaCantidad(
            UnidadMedidaEnum.MICROGRAMO,
            new BigDecimal("100001")
        );

        // Debería llegar hasta KILOGRAMO (forzado por i==0)
        assertEquals(UnidadMedidaEnum.KILOGRAMO, sugerida);
    }

    // ================ Constructor Test ================

    @Test
    @DisplayName("Constructor lanza UnsupportedOperationException")
    void constructor_lanzaExcepcion() throws Exception {
        Constructor<UnidadMedidaUtils> constructor = UnidadMedidaUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }

    // ================ Null Check Tests ================

    @Test
    @DisplayName("convertirCantidadEntreUnidades lanza NullPointerException si unidadOrigen es null")
    void convertir_unidadOrigenNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.convertirCantidadEntreUnidades(null, BigDecimal.TEN, KILOGRAMO)
        );
    }

    @Test
    @DisplayName("convertirCantidadEntreUnidades lanza NullPointerException si cantidad es null")
    void convertir_cantidadNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.convertirCantidadEntreUnidades(KILOGRAMO, null, GRAMO)
        );
    }

    @Test
    @DisplayName("convertirCantidadEntreUnidades lanza NullPointerException si unidadDestino es null")
    void convertir_unidadDestinoNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.convertirCantidadEntreUnidades(KILOGRAMO, BigDecimal.TEN, null)
        );
    }

    @Test
    @DisplayName("obtenerMenorUnidadMedida lanza NullPointerException si unidadUno es null")
    void obtenerMenor_unidadUnoNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.obtenerMenorUnidadMedida(null, GRAMO)
        );
    }

    @Test
    @DisplayName("obtenerMenorUnidadMedida lanza NullPointerException si unidadDos es null")
    void obtenerMenor_unidadDosNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.obtenerMenorUnidadMedida(GRAMO, null)
        );
    }

    @Test
    @DisplayName("obtenerMayorUnidadMedida lanza NullPointerException si unidadUno es null")
    void obtenerMayor_unidadUnoNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.obtenerMayorUnidadMedida(null, GRAMO)
        );
    }

    @Test
    @DisplayName("obtenerMayorUnidadMedida lanza NullPointerException si unidadDos es null")
    void obtenerMayor_unidadDosNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.obtenerMayorUnidadMedida(GRAMO, null)
        );
    }

    @Test
    @DisplayName("restarMovimientoConvertido(Lote) lanza NullPointerException si dto es null")
    void restarLote_dtoNull_lanzaExcepcion() {
        Lote lote = new Lote();
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.restarMovimientoConvertido(null, lote)
        );
    }

    @Test
    @DisplayName("restarMovimientoConvertido(Lote) lanza NullPointerException si lote es null")
    void restarLote_loteNull_lanzaExcepcion() {
        MovimientoDTO dto = new MovimientoDTO();
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.restarMovimientoConvertido(dto, (Lote) null)
        );
    }

    @Test
    @DisplayName("sumarMovimientoConvertido(Lote) lanza NullPointerException si dto es null")
    void sumarLote_dtoNull_lanzaExcepcion() {
        Lote lote = new Lote();
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.sumarMovimientoConvertido(null, lote)
        );
    }

    @Test
    @DisplayName("sumarMovimientoConvertido(Lote) lanza NullPointerException si lote es null")
    void sumarLote_loteNull_lanzaExcepcion() {
        MovimientoDTO dto = new MovimientoDTO();
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.sumarMovimientoConvertido(dto, (Lote) null)
        );
    }

    @Test
    @DisplayName("restarMovimientoConvertido(Bulto) lanza NullPointerException si dto es null")
    void restarBulto_dtoNull_lanzaExcepcion() {
        Bulto bulto = new Bulto();
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.restarMovimientoConvertido(null, bulto)
        );
    }

    @Test
    @DisplayName("restarMovimientoConvertido(Bulto) lanza NullPointerException si bulto es null")
    void restarBulto_bultoNull_lanzaExcepcion() {
        MovimientoDTO dto = new MovimientoDTO();
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.restarMovimientoConvertido(dto, (Bulto) null)
        );
    }

    @Test
    @DisplayName("sumarMovimientoConvertido(Bulto) lanza NullPointerException si dto es null")
    void sumarBulto_dtoNull_lanzaExcepcion() {
        Bulto bulto = new Bulto();
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.sumarMovimientoConvertido(null, bulto)
        );
    }

    @Test
    @DisplayName("sumarMovimientoConvertido(Bulto) lanza NullPointerException si bulto es null")
    void sumarBulto_bultoNull_lanzaExcepcion() {
        MovimientoDTO dto = new MovimientoDTO();
        assertThrows(NullPointerException.class, () ->
            UnidadMedidaUtils.sumarMovimientoConvertido(dto, (Bulto) null)
        );
    }

    // ================ Additional Coverage Tests ================

    @Test
    @DisplayName("sumarMovimientoConvertido(Lote): suma con conversión de unidades")
    void sumarLote_conConversion() {
        // Lote con 10 kg
        Lote lote = new Lote();
        lote.setCantidadActual(new BigDecimal("10"));
        lote.setUnidadMedida(KILOGRAMO);

        // Movimiento: 500 g (0.5 kg)
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCantidad(new BigDecimal("500"));
        dto.setUnidadMedida(GRAMO);

        BigDecimal result = UnidadMedidaUtils.sumarMovimientoConvertido(dto, lote);

        assertEquals(new BigDecimal("10.5"), result.stripTrailingZeros());
    }

    @Test
    @DisplayName("sumarMovimientoConvertido(Bulto): suma con conversión de unidades")
    void sumarBulto_conConversion() {
        // Bulto con 5 kg
        Bulto bulto = new Bulto();
        bulto.setCantidadActual(new BigDecimal("5"));
        bulto.setUnidadMedida(KILOGRAMO);

        // Movimiento: 2000 g (2 kg)
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCantidad(new BigDecimal("2000"));
        dto.setUnidadMedida(GRAMO);

        BigDecimal result = UnidadMedidaUtils.sumarMovimientoConvertido(dto, bulto);

        assertEquals(new BigDecimal("7"), result.stripTrailingZeros());
    }

    @Test
    @DisplayName("obtenerMayorUnidadMedida: devuelve la mayor")
    void obtenerMayor_ok() {
        UnidadMedidaEnum mayor = UnidadMedidaUtils.obtenerMayorUnidadMedida(KILOGRAMO, GRAMO);

        assertEquals(KILOGRAMO, mayor);
    }

    @Test
    @DisplayName("obtenerMayorUnidadMedida: misma unidad devuelve esa unidad")
    void obtenerMayor_mismaUnidad() {
        UnidadMedidaEnum mayor = UnidadMedidaUtils.obtenerMayorUnidadMedida(KILOGRAMO, KILOGRAMO);

        assertEquals(KILOGRAMO, mayor);
    }

    @Test
    @DisplayName("obtenerMayorUnidadMedida: tipos incompatibles lanza IllegalArgumentException")
    void obtenerMayor_incompatibles() {
        assertThrows(IllegalArgumentException.class, () ->
            UnidadMedidaUtils.obtenerMayorUnidadMedida(KILOGRAMO, LITRO)
        );
    }

    @Test
    @DisplayName("obtenerMayorUnidadMedida: devuelve unidadDos cuando es mayor")
    void obtenerMayor_unidadDosEsMayor() {
        UnidadMedidaEnum mayor = UnidadMedidaUtils.obtenerMayorUnidadMedida(MILIGRAMO, KILOGRAMO);

        assertEquals(KILOGRAMO, mayor);
    }

}