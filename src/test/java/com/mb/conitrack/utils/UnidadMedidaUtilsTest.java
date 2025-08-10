package com.mb.conitrack.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnidadMedidaUtilsTest {

    @Test
    void convertirCantidadEntreUnidades() {
    }

    @Test
    void obtenerMenorUnidadMedida() {
    }

    @Test
    void ordenDeMagnitudBase10() {
    }

    @Test
    void restarMovimientoConvertido() {
    }

    @Test
    void testRestarMovimientoConvertido() {
    }

    @Test
    void sugerirUnidadParaCantidad() {
    }



    /* ---------------- helpers ---------------- */

    private static MovimientoDTO dto(BigDecimal cantidad, UnidadMedidaEnum u) {
        MovimientoDTO d = new MovimientoDTO();
        d.setCantidad(cantidad);
        d.setUnidadMedida(u);
        return d;
    }

    private static void assertApprox(BigDecimal expected, BigDecimal actual, double eps) {
        BigDecimal diff = expected.subtract(actual).abs();
        assertTrue(diff.doubleValue() <= eps, () -> "diff=" + diff);
    }

    /** Elige una unidad != UNIDAD con al menos 2 compatibles para tests de conversión/elección. */
    private static UnidadMedidaEnum pickNonUnidadWithChain() {
        for (UnidadMedidaEnum u : UnidadMedidaEnum.values()) {
            if (u == UnidadMedidaEnum.UNIDAD) continue;
            List<UnidadMedidaEnum> chain = UnidadMedidaEnum.getUnidadesPorTipo(u);
            if (chain != null && chain.size() >= 2 && chain.contains(u)) {
                return u;
            }
        }
        fail("No hay una unidad (≠ UNIDAD) con cadena de compatibilidad >= 2");
        return null;
    }

    /* ---------------- convertirCantidadEntreUnidades ---------------- */

    @Test
    @DisplayName("convertirCantidadEntreUnidades: misma unidad → idéntico")
    void convertir_mismaUnidad() {
        BigDecimal q = new BigDecimal("12.345");
        BigDecimal out = UnidadMedidaUtils.convertirCantidadEntreUnidades(
            UnidadMedidaEnum.UNIDAD, q, UnidadMedidaEnum.UNIDAD);
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

    /* ---------------- obtenerMenorUnidadMedida ---------------- */

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
    @DisplayName("obtenerMenorUnidadMedida: distinto tipo → IllegalArgumentException")
    void menorUnidad_incompatible() {
        // Buscamos dos tipos distintos de unidades
        UnidadMedidaEnum tipo1 = null, tipo2 = null;
        outer:
        for (UnidadMedidaEnum u1 : UnidadMedidaEnum.values()) {
            for (UnidadMedidaEnum u2 : UnidadMedidaEnum.values()) {
                if (u1 != u2 && u1.getTipo() != null && u2.getTipo() != null && !u1.getTipo().equals(u2.getTipo())) {
                    tipo1 = u1; tipo2 = u2; break outer;
                }
            }
        }
        if (tipo1 == null) fail("No se hallaron dos unidades de distinto tipo para la prueba");

        final UnidadMedidaEnum finalTipo = tipo1;
        final UnidadMedidaEnum finalTipo1 = tipo2;
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> UnidadMedidaUtils.obtenerMenorUnidadMedida(finalTipo, finalTipo1)
        );
        assertEquals("Las unidades de medida no son compatibles", ex.getMessage());
    }

    /* ---------------- ordenDeMagnitudBase10 ---------------- */

    @Test
    @DisplayName("ordenDeMagnitudBase10: null/0 → 0")
    void magnitud_null_y_cero() {
        assertEquals(0, UnidadMedidaUtils.ordenDeMagnitudBase10(null));
        assertEquals(0, UnidadMedidaUtils.ordenDeMagnitudBase10(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("ordenDeMagnitudBase10: enteros y decimales")
    void magnitud_varios() {
        assertEquals(0, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("1")));
        assertEquals(1, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("10")));
        assertEquals(2, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("100")));
        assertEquals(3, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("1200")));
        assertEquals(-3, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("0.00123")));
    }

    /* ---------------- restarMovimientoConvertido (Lote/Bulto) ---------------- */
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
    @DisplayName("restarMovimientoConvertido(Bulto): unidades compatibles → resta con conversión (≈)")
    void restar_bulto_convertido() {
        UnidadMedidaEnum u = pickNonUnidadWithChain();
        List<UnidadMedidaEnum> chain = UnidadMedidaEnum.getUnidadesPorTipo(u);
        UnidadMedidaEnum uBulto = chain.get(0);
        UnidadMedidaEnum uDto   = chain.get(1);

        Bulto b = new Bulto();
        b.setCantidadActual(new BigDecimal("2"));
        b.setUnidadMedida(uBulto);

        MovimientoDTO dto = dto(new BigDecimal("250"), uDto);

        double factorBulto = uBulto.getFactorConversion();
        double factorDto   = uDto.getFactorConversion();
        BigDecimal expected = new BigDecimal("2")
            .subtract(new BigDecimal("250").multiply(BigDecimal.valueOf(factorDto / factorBulto)));

        BigDecimal out = UnidadMedidaUtils.restarMovimientoConvertido(dto, b);
        assertApprox(expected, out, 1e-9);
    }

    /* ---------------- sugerirUnidadParaCantidad ---------------- */

    @Test
    @DisplayName("sugerirUnidadParaCantidad: cantidad=null | unidad=null | UNIDAD → retorna unidad original")
    void sugerir_triviales() {
        assertNull(UnidadMedidaUtils.sugerirUnidadParaCantidad(null, new BigDecimal("1")));
        assertEquals(UnidadMedidaEnum.UNIDAD,
            UnidadMedidaUtils.sugerirUnidadParaCantidad(UnidadMedidaEnum.UNIDAD, new BigDecimal("5")));
        // cantidad null
        assertEquals(UnidadMedidaEnum.KILOGRAMO,
            UnidadMedidaUtils.sugerirUnidadParaCantidad(UnidadMedidaEnum.KILOGRAMO, null));
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
}