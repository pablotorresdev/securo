package com.mb.securo.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mb.securo.entity.TipoMovimiento;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class TipoMovimientoRepositoryTest {

    @Autowired
    private TipoMovimientoRepository tipoMovimientoRepository;

    @Test
    public void testDefaultTipoMovimientoData() {
        List<TipoMovimiento> allTipoMovimientos = tipoMovimientoRepository.findAll();
        assertThat(allTipoMovimientos).hasSize(3);
        assertThat(allTipoMovimientos).extracting(TipoMovimiento::getNombre).containsExactlyInAnyOrder(
            "Ingreso", "Egreso", "Transformacion"
        );
    }

}