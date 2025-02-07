package com.mb.securo.repository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mb.securo.entity.Clase;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ClaseRepositoryTest {

    @Autowired
    private ClaseRepository claseRepository;

    @Test
    public void testFindByNombre() {
        Clase clase = new Clase();
        clase.setNombre("TestClase");
        claseRepository.save(clase);

        Optional<Clase> foundClase = claseRepository.findByNombre("TestClase");
        assertThat(foundClase).isPresent();
        assertThat(foundClase.get().getNombre()).isEqualTo("TestClase");

        claseRepository.delete(clase);
    }

    @Test
    public void testFindAll() {

        final List<Clase> allClases = claseRepository.findAll();

        assertThat(allClases).hasSize(7);
        assertThat(allClases).extracting(Clase::getNombre).containsExactlyInAnyOrder(
            "API", "EXCIPIENTE", "CAPSULA", "SEMIELABORADO", "ACOND. PRIMARIO", "ACOND. SECUNDARIO", "U. VENTA"
        );
    }

}
