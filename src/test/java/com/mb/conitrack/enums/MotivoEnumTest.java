package com.mb.conitrack.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MotivoEnumTest {

    @Test
    public void testDefaultMotivoData() {
        MotivoEnum[] motivos = MotivoEnum.values();
        assertThat(motivos).hasSize(11);
        assertThat(motivos).extracting(MotivoEnum::getValor).containsExactlyInAnyOrder(
            "Compra",
            "Muestreo",
            "Devolucion compra",
            "Analisis",
            "Consumo produccion",
            "Produccion propia",
            "Venta",
            "Expiracion analisis",
            "Vencimiento",
            "Devolucion venta",
            "Ajuste"
        );
    }

}
