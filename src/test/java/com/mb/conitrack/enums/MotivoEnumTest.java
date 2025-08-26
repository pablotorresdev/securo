package com.mb.conitrack.enums;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotivoEnumTest {

    static Stream<MotivoEnum> allEnums() {
        return Stream.of(MotivoEnum.values());
    }

    @Test
    @DisplayName("getValor() no es null/blank y es único por constante")
    void valoresNoNulosYUnicos() {
        var valores = allEnums().map(MotivoEnum::getValor).toList();
        assertTrue(valores.stream().allMatch(v -> v != null && !v.isBlank()), "Valores no deben ser nulos/blank");
        assertEquals(valores.size(), Set.copyOf(valores).size(), "Los valores deben ser únicos");
    }

    @ParameterizedTest(name = "valueOf({0}) devuelve la misma constante")
    @MethodSource("allEnums")
    void valueOfDevuelveConstante(MotivoEnum constant) {
        assertEquals(constant, MotivoEnum.valueOf(constant.name()));
    }

    @Test
    @DisplayName("valueOf con nombre inválido lanza IllegalArgumentException")
    void valueOfNombreInvalido() {
        Executable call = () -> MotivoEnum.valueOf("INEXISTENTE");
        assertThrows(IllegalArgumentException.class, call);
    }

    @Test
    @DisplayName("values() contiene exactamente las 12 constantes en orden")
    void valuesContentAndOrder() {
        MotivoEnum[] v = MotivoEnum.values();
        assertEquals(14, v.length, "Cantidad de constantes");

        assertEquals(MotivoEnum.COMPRA, v[0]);
        assertEquals(MotivoEnum.MUESTREO, v[1]);
        assertEquals(MotivoEnum.DEVOLUCION_COMPRA, v[2]);
        assertEquals(MotivoEnum.ANALISIS, v[3]);
        assertEquals(MotivoEnum.RESULTADO_ANALISIS, v[4]);
        assertEquals(MotivoEnum.CONSUMO_PRODUCCION, v[5]);
        assertEquals(MotivoEnum.PRODUCCION_PROPIA, v[6]);
        assertEquals(MotivoEnum.LIBERACION, v[7]);
        assertEquals(MotivoEnum.VENTA, v[8]);
        assertEquals(MotivoEnum.EXPIRACION_ANALISIS, v[9]);
        assertEquals(MotivoEnum.VENCIMIENTO, v[10]);
        assertEquals(MotivoEnum.DEVOLUCION_VENTA, v[11]);
        assertEquals(MotivoEnum.RETIRO_MERCADO, v[12]);
        assertEquals(MotivoEnum.AJUSTE, v[13]);

        Set<MotivoEnum> all = EnumSet.allOf(MotivoEnum.class);
        assertEquals(all.size(), v.length);
        assertTrue(all.containsAll(Set.of(v)));
    }

}
