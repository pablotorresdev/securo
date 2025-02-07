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

import com.mb.securo.entity.TipoProducto;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class TipoProductoRepositoryTest {

    @Autowired
    private TipoProductoRepository tipoProductoRepository;

    @Test
    public void testFindByNombre() {
        TipoProducto clase = new TipoProducto();
        clase.setNombre("TestClase");
        tipoProductoRepository.save(clase);

        Optional<TipoProducto> foundClase = tipoProductoRepository.findByNombre("TestClase");
        assertThat(foundClase).isPresent();
        assertThat(foundClase.get().getNombre()).isEqualTo("TestClase");

        tipoProductoRepository.delete(clase);
    }

    @Test
    public void testFindAll() {

        final List<TipoProducto> allClases = tipoProductoRepository.findAll();

        assertThat(allClases).hasSize(7);
        assertThat(allClases).extracting(TipoProducto::getNombre).containsExactlyInAnyOrder(
            "Api",
            "Excipiente",
            "Capsula",
            "Semielaborado",
            "Acond. primario",
            "Acond. secundario",
            "Unidad venta"
        );
    }

}
