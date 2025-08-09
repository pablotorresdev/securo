package com.mb.conitrack.enums;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.utils.UnidadMedidaUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnidadMedidaUtilsTest {

    @Test
    @DisplayName("convertirCantidadEntreUnidades: unidades diferentes")
    void convertir_distintasUnidades() {
        BigDecimal kg = new BigDecimal("1");                  // 1 kg
        BigDecimal g = UnidadMedidaUtils.convertirCantidadEntreUnidades(
            UnidadMedidaEnum.KILOGRAMO, kg, UnidadMedidaEnum.GRAMO);

        // 1 kg → 1000 g  (factor KG/G = 1 / 0.001)
        assertEquals(new BigDecimal("1000.000"), g.setScale(3));
    }

    @Test
    @DisplayName("convertirCantidadEntreUnidades: misma unidad → misma cantidad")
    void convertir_mismaUnidad() {
        BigDecimal in = new BigDecimal("12.34");
        BigDecimal out = UnidadMedidaUtils.convertirCantidadEntreUnidades(
            UnidadMedidaEnum.KILOGRAMO, in, UnidadMedidaEnum.KILOGRAMO);

        assertSame(in, out);  // Devuelve exactamente la misma instancia
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
    @DisplayName("obtenerMenorUnidadMedida: tipos incompatibles → IllegalArgumentException")
    void menorUnidad_incompatible() {
        assertThrows(
            IllegalArgumentException.class, () ->
                UnidadMedidaUtils.obtenerMenorUnidadMedida(
                    UnidadMedidaEnum.GRAMO,
                    UnidadMedidaEnum.LITRO));        // Masa vs Volumen
    }

    @Test
    @DisplayName("obtenerMenorUnidadMedida: devuelve la menor")
    void menorUnidad_ok() {
        UnidadMedidaEnum menor = UnidadMedidaUtils.obtenerMenorUnidadMedida(
            UnidadMedidaEnum.MILIGRAMO, UnidadMedidaEnum.GRAMO);

        assertEquals(UnidadMedidaEnum.MILIGRAMO, menor);
    }

    @Test
    @DisplayName("Devuelve unidadDos cuando su factor es menor que el de unidadUno")
    void menorUnidad_unidadDosEsMenor() {
        // KG (factor 1 000) vs MG (factor 0.001) → MG debe ganar
        UnidadMedidaEnum menor = UnidadMedidaUtils.obtenerMenorUnidadMedida(
            UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.MILIGRAMO);

        assertEquals(UnidadMedidaEnum.MILIGRAMO, menor);   // rama “else”
    }

    @Test
    @DisplayName("restarMovimientoConvertido: descuenta según unidades")
    void restarMovimiento() {
        // Lote con 10 kg
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

    @Nested
    class OrdenDeMagnitud {

        @Test
        void decimalPequenio() {
            // 0.00056  -> -4  (10^-4)
            assertEquals(-4, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("0.00056")));
        }

        @Test
        void nuloOCero() {
            assertEquals(0, UnidadMedidaUtils.ordenDeMagnitudBase10(null));
            assertEquals(0, UnidadMedidaUtils.ordenDeMagnitudBase10(BigDecimal.ZERO));
        }

        @Test
        void positivoGrande() {
            assertEquals(3, UnidadMedidaUtils.ordenDeMagnitudBase10(new BigDecimal("5678")));
        }

    }

    @Nested
    class SugerirUnidad {

        @Test
        @DisplayName("Cantidad y unidad nulos o UNIDAD → devuelve la misma unidad")
        void casoBase() {
            assertEquals(
                UnidadMedidaEnum.UNIDAD,
                UnidadMedidaUtils.sugerirUnidadParaCantidad(
                    UnidadMedidaEnum.UNIDAD, new BigDecimal("5")));
        }

        @Test
        @DisplayName("Cantidad es null")
        void casoCantidadNull() {
            assertEquals(
                UnidadMedidaEnum.GRAMO,
                UnidadMedidaUtils.sugerirUnidadParaCantidad(UnidadMedidaEnum.GRAMO, null));
        }

        @Test
        @DisplayName("UnidadMedida es null")
        void casoUnidadMedidaNull() {
            assertNull(UnidadMedidaUtils.sugerirUnidadParaCantidad(null, new BigDecimal("5")));
        }

        @Test
        @DisplayName("Cantidad intermedia que NO cambia la unidad (rama final)")
        void sinCambioDeUnidad() {
            // 25 kg no dispara ni la rama de “muy pequeña” ni la de “muy grande”
            UnidadMedidaEnum result = UnidadMedidaUtils.sugerirUnidadParaCantidad(
                UnidadMedidaEnum.KILOGRAMO, new BigDecimal("25"));

            assertEquals(UnidadMedidaEnum.KILOGRAMO, result); // se queda igual
        }

        @Test
        @DisplayName("Cantidad intermedia que NO cambia la unidad (rama final)")
        void sinCambioDeUnidad2() {
            // 25 kg no dispara ni la rama de “muy pequeña” ni la de “muy grande”
            UnidadMedidaEnum result = UnidadMedidaUtils.sugerirUnidadParaCantidad(
                UnidadMedidaEnum.GRAMO, new BigDecimal("25000"));

            assertEquals(UnidadMedidaEnum.KILOGRAMO, result); // se queda igual
        }

        @Test
        @DisplayName("Cantidad muy grande y sin decimales → sugiere unidad mayor")
        void sugiereMayor() {
            UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
                UnidadMedidaEnum.MILIGRAMO, new BigDecimal("100000000")); // 100 000 000 mg

            assertTrue(sugerida.getFactorConversion() > UnidadMedidaEnum.MILIGRAMO.getFactorConversion());
        }

        @Test
        @DisplayName("Cantidad muy pequeña → sugiere unidad menor")
        void sugiereMenor() {
            UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
                UnidadMedidaEnum.KILOGRAMO, new BigDecimal("0.25"));   // 0.25 kg

            assertTrue(sugerida.getFactorConversion() < UnidadMedidaEnum.KILOGRAMO.getFactorConversion());
        }

        @Test
        @DisplayName("Cantidad muy pequeña → sugiere unidad menor")
        void sugiereMenor2() {
            UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
                UnidadMedidaEnum.KILOGRAMO, new BigDecimal("9.257"));   // 0.25 kg

            assertTrue(sugerida.getFactorConversion() < UnidadMedidaEnum.KILOGRAMO.getFactorConversion());
        }

        @Test
        @DisplayName("Cantidad muy pequeña → sugiere unidad menor")
        void sugiereMenor3() {
            UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
                UnidadMedidaEnum.GRAMO, new BigDecimal("9.2"));   // 0.25 kg

            assertEquals(sugerida.getFactorConversion(), UnidadMedidaEnum.GRAMO.getFactorConversion());
        }

        @Test
        @DisplayName("Búsqueda de unidad menor: sale por el último índice (condición i == last)")
        void sugiereMenor_enUltimoIntento() {
            // 0.00007 kg (7e-7) ↓ bajamos sucesivamente hasta alcanzar la última unidad (microgramo)
            UnidadMedidaEnum sugerida = UnidadMedidaUtils.sugerirUnidadParaCantidad(
                UnidadMedidaEnum.KILOGRAMO, new BigDecimal("0.0000007"));

            assertEquals(UnidadMedidaEnum.MICROGRAMO, sugerida);
        }

    }

}