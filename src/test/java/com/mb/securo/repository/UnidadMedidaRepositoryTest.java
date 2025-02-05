package com.mb.securo.repository;

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
        List<UnidadMedida> allUnidadMedidas = unidadMedidaRepository.findAll();
        assertThat(allUnidadMedidas).hasSize(24);

        // Unidades generales
        assertUnidad(allUnidadMedidas, "Unidad", "Generica", "U", 1.0);

        // Unidades de Masa
        assertUnidad(allUnidadMedidas, "Microgramo", "Masa", "µg", 0.000001);
        assertUnidad(allUnidadMedidas, "Miligramo", "Masa", "mg", 0.001);
        assertUnidad(allUnidadMedidas, "Gramo", "Masa", "g", 1.0);
        assertUnidad(allUnidadMedidas, "Kilogramo", "Masa", "kg", 1000.0);
        assertUnidad(allUnidadMedidas, "Tonelada", "Masa", "t", 1000000.0);

        // Unidades de Volumen
        assertUnidad(allUnidadMedidas, "Microlitro", "Volumen", "µL", 0.000001);
        assertUnidad(allUnidadMedidas, "Mililitro", "Volumen", "mL", 0.001);
        assertUnidad(allUnidadMedidas, "Centilitro", "Volumen", "cL", 0.01);
        assertUnidad(allUnidadMedidas, "Decilitro", "Volumen", "dL", 0.1);
        assertUnidad(allUnidadMedidas, "Litro", "Volumen", "L", 1.0);
        assertUnidad(allUnidadMedidas, "Milímetro cúbico", "Volumen", "mm3", 0.000001);
        assertUnidad(allUnidadMedidas, "Centímetro cúbico", "Volumen", "cm3", 0.001);
        assertUnidad(allUnidadMedidas, "Metro cúbico", "Volumen", "m3", 1000.0);

        // Unidades de Superficie
        assertUnidad(allUnidadMedidas, "Milímetro cuadrado", "Superficie", "mm2", 0.000001);
        assertUnidad(allUnidadMedidas, "Centímetro cuadrado", "Superficie", "cm2", 0.0001);
        assertUnidad(allUnidadMedidas, "Metro cuadrado", "Superficie", "m2", 1.0);
        assertUnidad(allUnidadMedidas, "Kilómetro cuadrado", "Superficie", "km2", 1000000.0);
        assertUnidad(allUnidadMedidas, "Hectárea", "Superficie", "ha", 10000.0);

        // Unidades de Longitud
        assertUnidad(allUnidadMedidas, "Micrometro", "Longitud", "µm", 0.000001);
        assertUnidad(allUnidadMedidas, "Milímetro", "Longitud", "mm", 0.001);
        assertUnidad(allUnidadMedidas, "Centímetro", "Longitud", "cm", 0.01);
        assertUnidad(allUnidadMedidas, "Metro", "Longitud", "m", 1.0);
        assertUnidad(allUnidadMedidas, "Kilómetro", "Longitud", "km", 1000.0);
    }

    private void assertUnidad(List<UnidadMedida> allUnidadMedidas, String name, String type, String symbol, double conversionFactor) {
        assertThat(allUnidadMedidas).anySatisfy(unidadMedida -> {
            assertThat(unidadMedida.getName()).isEqualTo(name);
            assertThat(unidadMedida.getType()).isEqualTo(type);
            assertThat(unidadMedida.getSymbol()).isEqualTo(symbol);
            assertThat(unidadMedida.getConversionFactor()).isEqualTo(conversionFactor);
        });
    }

}