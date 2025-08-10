package com.mb.conitrack.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoMovimientoEnumTest {

    private static final Map<TipoMovimientoEnum, String> EXPECTED_VALORES = Map.of(
        TipoMovimientoEnum.ALTA, "Alta",
        TipoMovimientoEnum.BAJA, "Baja",
        TipoMovimientoEnum.MODIFICACION, "Modificacion"
    );

    static Stream<TipoMovimientoEnum> allEnums() {
        return Stream.of(TipoMovimientoEnum.values());
    }

    @ParameterizedTest(name = "getValor() de {0} debe ser ''{1}''")
    @MethodSource("allEnums")
    void getValorDeCadaConstante(TipoMovimientoEnum constant) {
        String esperado = EXPECTED_VALORES.get(constant);
        assertNotNull(esperado, "Debe haber un valor esperado para " + constant.name());
        assertEquals(esperado, constant.getValor());
    }

    @Test
    @DisplayName("getValor() nunca es null y los valores son únicos")
    void valoresNoNulosYNounicos() {
        var valores = allEnums().map(TipoMovimientoEnum::getValor).toList();
        assertTrue(
            valores.stream().allMatch(v -> v != null && !v.isBlank()),
            "Todos los valores deben ser no nulos/blank");
        assertEquals(valores.size(), Set.copyOf(valores).size(), "Los valores deben ser únicos");
    }

    @ParameterizedTest(name = "valueOf({0}) devuelve la misma constante")
    @MethodSource("allEnums")
    void valueOfDevuelveConstante(TipoMovimientoEnum constant) {
        assertEquals(constant, TipoMovimientoEnum.valueOf(constant.name()));
    }

    @Test
    @DisplayName("valueOf con nombre inválido lanza IllegalArgumentException")
    void valueOfNombreInvalido() {
        Executable call = () -> TipoMovimientoEnum.valueOf("INEXISTENTE");
        assertThrows(IllegalArgumentException.class, call);
    }

    @Test
    @DisplayName("values() contiene exactamente las 3 constantes en orden")
    void valuesContentAndOrder() {
        TipoMovimientoEnum[] values = TipoMovimientoEnum.values();
        assertEquals(3, values.length, "Cantidad de constantes");

        assertEquals(TipoMovimientoEnum.ALTA, values[0]);
        assertEquals(TipoMovimientoEnum.BAJA, values[1]);
        assertEquals(TipoMovimientoEnum.MODIFICACION, values[2]);

        // También validar que el set coincide con EnumSet.allOf
        Set<TipoMovimientoEnum> all = EnumSet.allOf(TipoMovimientoEnum.class);
        assertEquals(all.size(), values.length);
        assertTrue(all.containsAll(Set.of(values)));
    }

}