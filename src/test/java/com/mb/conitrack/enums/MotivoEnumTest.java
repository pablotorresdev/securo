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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotivoEnumTest {

    private static final Map<MotivoEnum, String> ESPERADOS_VALOR = Map.ofEntries(
        Map.entry(MotivoEnum.COMPRA, "Compra"),
        Map.entry(MotivoEnum.MUESTREO, "Muestreo"),
        Map.entry(MotivoEnum.DEVOLUCION_COMPRA, "Devolucion compra"),
        Map.entry(MotivoEnum.ANALISIS, "Analisis"),
        Map.entry(MotivoEnum.CONSUMO_PRODUCCION, "Consumo produccion"),
        Map.entry(MotivoEnum.PRODUCCION_PROPIA, "Produccion propia"),
        Map.entry(MotivoEnum.LIBERACION, "Liberacion"),
        Map.entry(MotivoEnum.VENTA, "Venta"),
        Map.entry(MotivoEnum.EXPIRACION_ANALISIS, "Expiracion analisis"),
        Map.entry(MotivoEnum.VENCIMIENTO, "Vencimiento"),
        Map.entry(MotivoEnum.DEVOLUCION_VENTA, "Devolucion venta"),
        Map.entry(MotivoEnum.AJUSTE, "Ajuste")
    );

    static Stream<MotivoEnum> allEnums() {
        return Stream.of(MotivoEnum.values());
    }

    @ParameterizedTest(name = "getValor() de {0} debe ser ''{1}''")
    @MethodSource("allEnums")
    void getValorDeCadaConstante(MotivoEnum constant) {
        assertEquals(ESPERADOS_VALOR.get(constant), constant.getValor());
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
        assertEquals(12, v.length, "Cantidad de constantes");

        assertEquals(MotivoEnum.COMPRA, v[0]);
        assertEquals(MotivoEnum.MUESTREO, v[1]);
        assertEquals(MotivoEnum.DEVOLUCION_COMPRA, v[2]);
        assertEquals(MotivoEnum.ANALISIS, v[3]);
        assertEquals(MotivoEnum.CONSUMO_PRODUCCION, v[4]);
        assertEquals(MotivoEnum.PRODUCCION_PROPIA, v[5]);
        assertEquals(MotivoEnum.LIBERACION, v[6]);
        assertEquals(MotivoEnum.VENTA, v[7]);
        assertEquals(MotivoEnum.EXPIRACION_ANALISIS, v[8]);
        assertEquals(MotivoEnum.VENCIMIENTO, v[9]);
        assertEquals(MotivoEnum.DEVOLUCION_VENTA, v[10]);
        assertEquals(MotivoEnum.AJUSTE, v[11]);

        Set<MotivoEnum> all = EnumSet.allOf(MotivoEnum.class);
        assertEquals(all.size(), v.length);
        assertTrue(all.containsAll(Set.of(v)));
    }

}
