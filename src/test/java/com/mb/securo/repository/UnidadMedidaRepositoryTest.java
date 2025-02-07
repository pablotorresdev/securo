package com.mb.securo.repository;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mb.securo.entity.UnidadMedida;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UnidadMedidaRepositoryTest {

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    @Test
    public void testDefaultUnidadMedidaData() {
        List<UnidadMedida> unidadesMedida = unidadMedidaRepository.findAll();
        unidadesMedida.sort(Comparator.comparing(UnidadMedida::getId));

        assertThat(unidadesMedida).hasSize(26);
    }

}