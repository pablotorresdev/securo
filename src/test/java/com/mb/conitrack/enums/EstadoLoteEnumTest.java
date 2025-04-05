package com.mb.conitrack.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EstadoLoteEnumTest {

    @Test
    public void testDefaultStatusData() {
        EstadoLoteEnum[] estados = EstadoLoteEnum.values();
        assertThat(estados).hasSize(6);
        assertThat(estados).extracting(EstadoLoteEnum::getValor)
            .containsExactlyInAnyOrder("Nuevo", "En uso", "Consumido", "Vendido", "Devuelto", "Descartado");
    }

}
