package com.mb.conitrack.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoProductoEnumTest {

    /* -------------------------------------------------
     * 1. esSemiElaborado()
     * ------------------------------------------------- */
    @Test
    @DisplayName("esSemiElaborado devuelve true solo para grupo SemiElab")
    void esSemiElaborado_ramas() {
        // Rama TRUE
        assertTrue(TipoProductoEnum.SEMIELABORADO.esSemiElaborado());

        // Rama FALSE
        assertFalse(TipoProductoEnum.API.esSemiElaborado());
    }

    /* -------------------------------------------------
     * 2. Getters – recorremos TODAS las constantes
     * ------------------------------------------------- */
    @Test
    @DisplayName("Getters devuelven valores coherentes en todas las constantes")
    void getters() {
        for (TipoProductoEnum tipo : TipoProductoEnum.values()) {
            // valor y grupo no vacíos
            assertNotNull(tipo.getValor());
            assertFalse(tipo.getValor().isBlank());
            assertNotNull(tipo.getGrupo());
            assertFalse(tipo.getGrupo().isBlank());

            // código positivo
            assertTrue(tipo.getCodigo() > 0);

            // requiereProductoDestino coherente con la definición manual (no null)
            assertNotNull(tipo.isRequiereProductoDestino());
        }
    }

}
