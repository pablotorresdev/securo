package com.mb.conitrack.enums;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UnidadMedidaEnumTest {

    /* -------------------------------------------------
     * 1. getUnidadesPorTipo
     * ------------------------------------------------- */
    @Test
    @DisplayName("getUnidadesPorTipo devuelve unidades del mismo tipo y ordenadas desc.")
    void unidadesPorTipo() {
        List<UnidadMedidaEnum> masa = UnidadMedidaEnum.getUnidadesPorTipo(UnidadMedidaEnum.GRAMO);

        // Todas son tipo Masa
        assertTrue(masa.stream().allMatch(u -> "Masa".equals(u.getTipo())));

        // Orden descendente por factorConversion
        for (int i = 0; i < masa.size() - 1; i++) {
            assertTrue(masa.get(i).getFactorConversion() >= masa.get(i + 1).getFactorConversion());
        }
    }

    /* -------------------------------------------------
     * 2. getUnidadesConvertibles – cubrimos TODAS las ramas
     * ------------------------------------------------- */
    @Nested
    class UnidadesConvertibles {

        // (a) index == 0  ➜ rama 1
        @Test
        void indexCero() {
            List<UnidadMedidaEnum> list = UnidadMedidaEnum.getUnidadesConvertibles(UnidadMedidaEnum.KILOGRAMO);
            // El primer elemento debe ser KILOGRAMO (mayor factor)
            assertEquals(UnidadMedidaEnum.KILOGRAMO, list.get(0));
            assertTrue(list.contains(UnidadMedidaEnum.MICROGRAMO)); // incluye extremo opuesto
        }

        // (b) index == 1  ➜ rama 2
        @Test
        void indexUno() {
            List<UnidadMedidaEnum> list = UnidadMedidaEnum.getUnidadesConvertibles(UnidadMedidaEnum.GRAMO);
            assertEquals(UnidadMedidaEnum.KILOGRAMO, list.get(0)); // sigue estando ordenado desc
        }

        // (c) index == size-1 ➜ rama 3
        @Test
        void indexUltimo() {
            List<UnidadMedidaEnum> list = UnidadMedidaEnum.getUnidadesConvertibles(UnidadMedidaEnum.MICROGRAMO);
            // Último elemento de Masa desplazado a fondo de lista convertibles
            assertEquals(UnidadMedidaEnum.MICROGRAMO, list.get(list.size() - 1));
        }

        // (d) index == size-2 ➜ rama 4
        @Test
        void indexPenultimo() {
            List<UnidadMedidaEnum> list = UnidadMedidaEnum.getUnidadesConvertibles(UnidadMedidaEnum.MILIGRAMO);
            assertTrue(list.contains(UnidadMedidaEnum.GRAMO));
        }

        // (e) caso “central” (else) con lista grande (Volumen, index 3 de 7)
        @Test
        void indexCentral() {
            List<UnidadMedidaEnum> vol = UnidadMedidaEnum.getUnidadesPorTipo(UnidadMedidaEnum.LITRO);
            // LITRO(0), DECILITRO(1), CENTILITRO(2), MILILITRO(3) …
            List<UnidadMedidaEnum> convertibles =
                UnidadMedidaEnum.getUnidadesConvertibles(UnidadMedidaEnum.MILILITRO);

            // El rango obtenido debe incluir MILILITRO y quedar ordenado desc.
            assertTrue(convertibles.contains(UnidadMedidaEnum.MILILITRO));
            for (int i = 0; i < convertibles.size() - 1; i++) {
                assertTrue(convertibles.get(i).getFactorConversion() >=
                    convertibles.get(i + 1).getFactorConversion());
            }

            // Rango acotado (no todas las 7 unidades)
            assertTrue(convertibles.size() < vol.size());
        }
    }

    /* -------------------------------------------------
     * 3. Getters – recorremos TODAS las constantes
     * ------------------------------------------------- */
    @Test
    @DisplayName("Getters devuelven datos consistentes en todas las constantes")
    void getters() {
        for (UnidadMedidaEnum u : UnidadMedidaEnum.values()) {
            assertNotNull(u.getNombre());
            assertNotNull(u.getSimbolo());
            assertFalse(u.getNombre().isBlank());
            assertFalse(u.getSimbolo().isBlank());
            assertTrue(u.getFactorConversion() > 0);
        }
    }
}
