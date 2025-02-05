package com.mb.securo.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mb.securo.entity.Deposito;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class DepositoRepositoryTest {

    @Autowired
    private DepositoRepository depositoRepository;

    @Test
    public void testDefaultDepositoData() {
        List<Deposito> allDepositos = depositoRepository.findAll();
        assertThat(allDepositos).hasSize(6);
        assertThat(allDepositos).extracting(Deposito::getName).containsExactlyInAnyOrder(
            "MATERIA PRIMA", "SEMIELABORADO", "EMPAQUE PRIMARIO", "EMPAQUE SECUNDARIO", "PRODUCTO TERMINADO", "DEPOMAX"
        );
    }

}