package com.mb.securo.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mb.securo.entity.Dictamen;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class DictamenRepositoryTest {

    @Autowired
    private DictamenRepository dictamenRepository;

    @Test
    public void testDefaultDictamenData() {
        List<Dictamen> allDictamenes = dictamenRepository.findAll();
        assertThat(allDictamenes).hasSize(9);
        assertThat(allDictamenes).extracting(Dictamen::getEstado).containsExactlyInAnyOrder(
            "Recibido",
            "Cuarentena",
            "Aprobado",
            "Rechazado",
            "Vencido",
            "Destruido",
            "Retiro mercado",
            "Consumido",
            "Depomax"
        );
    }

}