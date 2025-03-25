package com.mb.conitrack.enums;

import org.junit.jupiter.api.Test;

import com.mb.conitrack.enums.DictamenEnum;

import static org.assertj.core.api.Assertions.assertThat;

public class DictamenEnumTest {

    @Test
    public void testDictamenEnumValues() {
        DictamenEnum[] valores = DictamenEnum.values();
        // Verifica que se tengan los 9 valores esperados
        assertThat(valores).hasSize(8);
        // Verifica que cada constante tenga el valor correcto
        assertThat(valores).extracting(DictamenEnum::getValor).containsExactlyInAnyOrder(
            "Recibido",
            "Cuarentena",
            "Aprobado",
            "Rechazado",
            "Vencido",
            "Liberado",
            "Devolucion clientes",
            "Retiro mercado"
        );
    }

}
