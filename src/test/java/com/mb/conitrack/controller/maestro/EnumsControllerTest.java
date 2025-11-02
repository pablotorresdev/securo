package com.mb.conitrack.controller.maestro;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.mb.conitrack.enums.UnidadMedidaEnum;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnumsController Tests")
class EnumsControllerTest {

    @InjectMocks
    private EnumsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("getUnidadesCompatibles debe retornar unidades convertibles para KILOGRAMO")
    void testGetUnidadesCompatibles_Kilogramo() {
        // Act
        List<UnidadMedidaEnum> result = controller.getUnidadesCompatibles(UnidadMedidaEnum.KILOGRAMO);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(UnidadMedidaEnum.GRAMO));
        assertTrue(result.contains(UnidadMedidaEnum.KILOGRAMO));
    }

    @Test
    @DisplayName("getUnidadesCompatibles debe retornar unidades convertibles para LITRO")
    void testGetUnidadesCompatibles_Litro() {
        // Act
        List<UnidadMedidaEnum> result = controller.getUnidadesCompatibles(UnidadMedidaEnum.LITRO);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(UnidadMedidaEnum.LITRO));
        assertTrue(result.contains(UnidadMedidaEnum.MILILITRO));
    }

    @Test
    @DisplayName("getUnidadesCompatibles debe retornar unidades convertibles para UNIDAD")
    void testGetUnidadesCompatibles_Unidad() {
        // Act
        List<UnidadMedidaEnum> result = controller.getUnidadesCompatibles(UnidadMedidaEnum.UNIDAD);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(UnidadMedidaEnum.UNIDAD));
    }

    @Test
    @DisplayName("getSubUnidades debe retornar solo subunidades menores para KILOGRAMO")
    void testGetSubUnidades_Kilogramo() {
        // Act
        List<UnidadMedidaEnum> result = controller.getSubUnidades(UnidadMedidaEnum.KILOGRAMO);

        // Assert
        assertNotNull(result);
        // Debe contener solo unidades con factor de conversión <= al del KILOGRAMO
        assertTrue(result.contains(UnidadMedidaEnum.KILOGRAMO));
        assertTrue(result.contains(UnidadMedidaEnum.GRAMO));
        // No debe contener unidades mayores como TONELADA si existe
        assertTrue(result.stream().allMatch(u ->
            u.getFactorConversion() <= UnidadMedidaEnum.KILOGRAMO.getFactorConversion()));
    }

    @Test
    @DisplayName("getSubUnidades debe retornar solo subunidades menores para LITRO")
    void testGetSubUnidades_Litro() {
        // Act
        List<UnidadMedidaEnum> result = controller.getSubUnidades(UnidadMedidaEnum.LITRO);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(UnidadMedidaEnum.LITRO));
        assertTrue(result.contains(UnidadMedidaEnum.MILILITRO));
        // Verificar que todas las unidades tienen factor <= al del LITRO
        assertTrue(result.stream().allMatch(u ->
            u.getFactorConversion() <= UnidadMedidaEnum.LITRO.getFactorConversion()));
    }

    @Test
    @DisplayName("getSubUnidades debe retornar solo la unidad misma para GRAMO")
    void testGetSubUnidades_Gramo() {
        // Act - GRAMO es la unidad más pequeña de peso
        List<UnidadMedidaEnum> result = controller.getSubUnidades(UnidadMedidaEnum.GRAMO);

        // Assert
        assertNotNull(result);
        // Solo debe contener GRAMO ya que es la más pequeña
        assertTrue(result.contains(UnidadMedidaEnum.GRAMO));
        assertFalse(result.contains(UnidadMedidaEnum.KILOGRAMO)); // No debe contener mayores
    }

    @Test
    @DisplayName("getSubUnidades debe funcionar para UNIDAD")
    void testGetSubUnidades_Unidad() {
        // Act
        List<UnidadMedidaEnum> result = controller.getSubUnidades(UnidadMedidaEnum.UNIDAD);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(UnidadMedidaEnum.UNIDAD));
    }
}
