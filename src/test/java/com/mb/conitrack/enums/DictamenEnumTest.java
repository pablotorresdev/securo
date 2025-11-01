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

class DictamenEnumTest {

    private static final Map<DictamenEnum, String> ESPERADOS = Map.ofEntries(
        Map.entry(DictamenEnum.RECIBIDO, "Recibido"),
        Map.entry(DictamenEnum.CUARENTENA, "Cuarentena"),
        Map.entry(DictamenEnum.APROBADO, "Aprobado"),
        Map.entry(DictamenEnum.RECHAZADO, "Rechazado"),
        Map.entry(DictamenEnum.ANULADO, "Anulado"),
        Map.entry(DictamenEnum.CANCELADO, "Cancelado"),
        Map.entry(DictamenEnum.ANALISIS_EXPIRADO, "Analisis expirado"),
        Map.entry(DictamenEnum.VENCIDO, "Vencido"),
        Map.entry(DictamenEnum.LIBERADO, "Liberado"),
        Map.entry(DictamenEnum.DEVOLUCION_CLIENTES, "Devolucion clientes"),
        Map.entry(DictamenEnum.RETIRO_MERCADO, "Retiro mercado")
    );

    static Stream<DictamenEnum> allEnums() {
        return Stream.of(DictamenEnum.values());
    }

    @ParameterizedTest(name = "getValor() de {0} debe ser ''{1}''")
    @MethodSource("allEnums")
    void getValorDeCadaConstante(DictamenEnum constant) {
        String esperado = ESPERADOS.get(constant);
        assertNotNull(esperado, "Debe haber valor esperado para " + constant.name());
        assertEquals(esperado, constant.getValor());
    }

    @Test
    @DisplayName("getValor() no es null/blank y es único por constante")
    void valoresNoNulosYUnicos() {
        var valores = allEnums().map(DictamenEnum::getValor).toList();
        assertTrue(valores.stream().allMatch(v -> v != null && !v.isBlank()), "Valores no deben ser nulos/blank");
        assertEquals(valores.size(), Set.copyOf(valores).size(), "Los valores deben ser únicos");
    }

    @ParameterizedTest(name = "valueOf({0}) devuelve la misma constante")
    @MethodSource("allEnums")
    void valueOfDevuelveConstante(DictamenEnum constant) {
        assertEquals(constant, DictamenEnum.valueOf(constant.name()));
    }

    @Test
    @DisplayName("valueOf con nombre inválido lanza IllegalArgumentException")
    void valueOfNombreInvalido() {
        Executable call = () -> DictamenEnum.valueOf("INVALIDO");
        assertThrows(IllegalArgumentException.class, call);
    }

    @Test
    @DisplayName("values() contiene exactamente las 9 constantes en orden")
    void valuesContentAndOrder() {
        DictamenEnum[] v = DictamenEnum.values();
        assertEquals(11, v.length, "Cantidad de constantes");

        assertEquals(DictamenEnum.RECIBIDO, v[0]);
        assertEquals(DictamenEnum.CUARENTENA, v[1]);
        assertEquals(DictamenEnum.APROBADO, v[2]);
        assertEquals(DictamenEnum.RECHAZADO, v[3]);
        assertEquals(DictamenEnum.ANULADO, v[4]);
        assertEquals(DictamenEnum.CANCELADO, v[5]);
        assertEquals(DictamenEnum.ANALISIS_EXPIRADO, v[6]);
        assertEquals(DictamenEnum.VENCIDO, v[7]);
        assertEquals(DictamenEnum.LIBERADO, v[8]);
        assertEquals(DictamenEnum.DEVOLUCION_CLIENTES, v[9]);
        assertEquals(DictamenEnum.RETIRO_MERCADO, v[10]);

        Set<DictamenEnum> all = EnumSet.allOf(DictamenEnum.class);
        assertEquals(all.size(), v.length);
        assertTrue(all.containsAll(Set.of(v)));
    }

}