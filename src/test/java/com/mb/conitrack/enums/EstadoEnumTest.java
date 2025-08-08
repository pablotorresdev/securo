package com.mb.conitrack.enums;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoEnumTest {

    @Test
    @DisplayName("fromValor ignora mayúsculas/minúsculas")
    void fromValor_caseInsensitive() {
        Optional<EstadoEnum> opt = EstadoEnum.fromValor("dEsCaRtAdO");
        assertTrue(opt.isPresent());
        assertEquals(EstadoEnum.DESCARTADO, opt.get());
    }

    @Test
    @DisplayName("fromValor devuelve Optional con coincidencia exacta")
    void fromValor_exactMatch() {
        Optional<EstadoEnum> opt = EstadoEnum.fromValor("Devuelto");
        assertTrue(opt.isPresent());
        assertEquals(EstadoEnum.DEVUELTO, opt.get());
    }

    @Test
    @DisplayName("fromValor devuelve Optional.empty cuando no hay coincidencia")
    void fromValor_noMatch() {
        assertTrue(EstadoEnum.fromValor("INEXISTENTE").isEmpty());
    }

    @Test
    @DisplayName("getValor y getPrioridad devuelven los datos correctos")
    void getters() {
        for (EstadoEnum estado : EstadoEnum.values()) {
            // El valor legible debe coincidir (no null / no vacío)
            assertNotNull(estado.getValor());
            assertFalse(estado.getValor().isBlank());

            // Prioridad dentro del rango conocido [0-2]
            assertTrue(estado.getPrioridad() >= 0 && estado.getPrioridad() <= 2);
        }
    }

}
