package com.mb.conitrack.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EstadoEnumTest {

    @Test
    public void testDefaultStatusData() {
        EstadoEnum[] estados = EstadoEnum.values();
        assertThat(estados).hasSize(6);
        assertThat(estados).extracting(EstadoEnum::getValor)
            .containsExactlyInAnyOrder("Nuevo", "En uso", "Consumido", "Vendido", "Devuelto", "Descartado");
    }

}
