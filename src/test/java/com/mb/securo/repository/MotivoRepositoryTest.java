package com.mb.securo.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mb.securo.entity.Motivo;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class MotivoRepositoryTest {

    @Autowired
    private MotivoRepository motivoRepository;

    @Test
    public void testDefaultMotivoData() {
        List<Motivo> allMotivos = motivoRepository.findAll();
        assertThat(allMotivos).hasSize(10);
        assertThat(allMotivos).extracting(Motivo::getDescripcion).containsExactlyInAnyOrder(
            "Consumo Produccion", "Vencido", "Reanalizado", "Ajuste", "Rechazado", "Muestreo", "Compra", "Devolucion", "Saldo Inicial", "Desarrollo"
        );
    }

}