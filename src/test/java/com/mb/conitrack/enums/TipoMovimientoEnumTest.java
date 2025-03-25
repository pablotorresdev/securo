package com.mb.conitrack.enums;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class TipoMovimientoEnumTest {

    @Test
    public void testDefaultTipoMovimientoData() {
        TipoMovimientoEnum[] tipos = TipoMovimientoEnum.values();
        assertThat(tipos).hasSize(3);
        assertThat(tipos).extracting(TipoMovimientoEnum::getValor)
            .containsExactlyInAnyOrder("Alta", "Baja", "Modificacion");
    }
}
