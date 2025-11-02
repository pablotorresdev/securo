package com.mb.conitrack.service.cu.validation;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.cu.AbstractCuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

import static com.mb.conitrack.testdata.TestDataBuilder.unLoteDTO;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para la validación de fechas del proveedor (CU1).
 * Prueba el método validarFechasProveedor() de AbstractCuService.
 *
 * Cobertura:
 * - Ambas fechas nulas (válido)
 * - Solo fecha reanalisis (válido)
 * - Solo fecha vencimiento (válido)
 * - Reanalisis antes de vencimiento (válido)
 * - Reanalisis igual a vencimiento (válido)
 * - Reanalisis después de vencimiento (inválido)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Validación de Fechas del Proveedor - CU1")
class ValidacionFechasProveedorTest {

    private TestableAbstractCuService service;
    private BindingResult bindingResult;

    static class TestableAbstractCuService extends AbstractCuService {
        public boolean testValidarFechasProveedor(LoteDTO dto, BindingResult result) {
            return validarFechasProveedor(dto, result);
        }
    }

    @BeforeEach
    void setUp() {
        service = new TestableAbstractCuService();
    }

    @Test
    @DisplayName("test_ambasFechasNulas_debe_aceptar")
    void test_ambasFechasNulas_debe_aceptar() {
        // Given
        LoteDTO dto = unLoteDTO()
                .withFechaReanalisisProveedor(null)
                .withFechaVencimientoProveedor(null)
                .build();
        bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

        // When
        boolean resultado = service.testValidarFechasProveedor(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("test_soloFechaReanalisis_debe_aceptar")
    void test_soloFechaReanalisis_debe_aceptar() {
        // Given
        LoteDTO dto = unLoteDTO()
                .withFechaReanalisisProveedor(LocalDate.now().plusMonths(6))
                .withFechaVencimientoProveedor(null)
                .build();
        bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

        // When
        boolean resultado = service.testValidarFechasProveedor(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("test_soloFechaVencimiento_debe_aceptar")
    void test_soloFechaVencimiento_debe_aceptar() {
        // Given
        LoteDTO dto = unLoteDTO()
                .withFechaReanalisisProveedor(null)
                .withFechaVencimientoProveedor(LocalDate.now().plusYears(2))
                .build();
        bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

        // When
        boolean resultado = service.testValidarFechasProveedor(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("test_reanalisisAntesDeVencimiento_debe_aceptar")
    void test_reanalisisAntesDeVencimiento_debe_aceptar() {
        // Given
        LocalDate hoy = LocalDate.now();
        LoteDTO dto = unLoteDTO()
                .withFechaReanalisisProveedor(hoy.plusMonths(6))
                .withFechaVencimientoProveedor(hoy.plusYears(2))
                .build();
        bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

        // When
        boolean resultado = service.testValidarFechasProveedor(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("test_reanalisisIgualVencimiento_debe_aceptar")
    void test_reanalisisIgualVencimiento_debe_aceptar() {
        // Given
        LocalDate fecha = LocalDate.now().plusYears(1);
        LoteDTO dto = unLoteDTO()
                .withFechaReanalisisProveedor(fecha)
                .withFechaVencimientoProveedor(fecha)
                .build();
        bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

        // When
        boolean resultado = service.testValidarFechasProveedor(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("test_reanalisisisPosterioreVencimiento_debe_rechazar")
    void test_reanalisisisPosterioreVencimiento_debe_rechazar() {
        // Given
        LocalDate hoy = LocalDate.now();
        LoteDTO dto = unLoteDTO()
                .withFechaReanalisisProveedor(hoy.plusYears(2))  // 2 años
                .withFechaVencimientoProveedor(hoy.plusMonths(6))  // 6 meses
                .build();
        bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

        // When
        boolean resultado = service.testValidarFechasProveedor(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
        assertThat(bindingResult.getFieldError("fechaReanalisisProveedor")).isNotNull();
        // Validar que el mensaje contiene las palabras clave esperadas
        String mensaje = bindingResult.getFieldError("fechaReanalisisProveedor").getDefaultMessage();
        assertThat(mensaje)
                .containsIgnoringCase("fecha")
                .containsIgnoringCase("posterior");
    }
}
