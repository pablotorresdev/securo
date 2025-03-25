package com.mb.conitrack.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TipoProductoEnumTest {

    @Test
    public void testAllEnumValues() {
        TipoProductoEnum[] values = TipoProductoEnum.values();
        assertThat(values).hasSize(6);
        assertThat(values).extracting(TipoProductoEnum::getValor).containsExactlyInAnyOrder(
            "Api",
            "Excipiente",
            "Semielaborado",
            "Acond. primario",
            "Acond. secundario",
            "Unidad venta"
        );
    }

}
