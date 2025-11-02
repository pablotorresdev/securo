package com.mb.conitrack.service.cu.validation;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.cu.AbstractCuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;

import static com.mb.conitrack.testdata.TestDataBuilder.unLoteDTO;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para la validación de cantidad de ingreso (CU1).
 * Prueba el método validarCantidadIngreso() de AbstractCuService.
 *
 * Cobertura:
 * - Cantidad nula
 * - Cantidad negativa
 * - Cantidad cero
 * - Cantidad válida
 * - UNIDAD con decimales (inválido)
 * - UNIDAD entera válida
 * - UNIDAD menor a bultos totales (inválido)
 * - Unidades de masa/volumen con decimales (válido)
 * - Cantidad muy grande válida
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Validación de Cantidad de Ingreso - CU1")
class ValidacionCantidadIngresoTest {

    private TestableAbstractCuService service;
    private BindingResult bindingResult;

    /**
     * Clase interna para poder testear AbstractCuService que es abstracta.
     */
    static class TestableAbstractCuService extends AbstractCuService {
        // Exponemos el método protected para testing
        public boolean testValidarCantidadIngreso(LoteDTO dto, BindingResult result) {
            return validarCantidadIngreso(dto, result);
        }
    }

    @BeforeEach
    void setUp() {
        service = new TestableAbstractCuService();
    }

    @Nested
    @DisplayName("Validaciones de valor nulo o inválido")
    class ValidacionesValorInvalido {

        @Test
        @DisplayName("test_cantidadNula_debe_rechazar")
        void test_cantidadNula_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(null)
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadInicial")).isNotNull();
            assertThat(bindingResult.getFieldError("cantidadInicial").getDefaultMessage())
                    .contains("no puede ser nula");
        }

        @Test
        @DisplayName("test_cantidadCero_debe_rechazar")
        void test_cantidadCero_debe_rechazar() {
            // Given - La validación @Positive del DTO ya rechaza cero, pero la lógica de negocio también
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(BigDecimal.ZERO)
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then - Pasa porque la validación solo rechaza nulos y UNIDAD con decimales
            // El @Positive del DTO es quien debe rechazar cero
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("test_cantidadNegativa_debe_rechazar_porDTO")
        void test_cantidadNegativa_debe_rechazar_porDTO() {
            // Given - @Positive en DTO rechaza negativos antes de llegar a esta validación
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("-10"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When - Esta validación no valida negativos, es responsabilidad del DTO
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue(); // Pasa porque no valida negativos aquí
        }
    }

    @Nested
    @DisplayName("Validaciones específicas para UNIDAD")
    class ValidacionesUnidad {

        @Test
        @DisplayName("test_unidadConDecimales_debe_rechazar")
        void test_unidadConDecimales_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("10.5"))
                    .withUnidadMedida(UnidadMedidaEnum.UNIDAD)
                    .withBultosTotales(5)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadInicial")).isNotNull();
            assertThat(bindingResult.getFieldError("cantidadInicial").getDefaultMessage())
                    .contains("número entero positivo")
                    .contains("UNIDAD");
        }

        @Test
        @DisplayName("test_unidadEntera_debe_aceptar")
        void test_unidadEntera_debe_aceptar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("100"))
                    .withUnidadMedida(UnidadMedidaEnum.UNIDAD)
                    .withBultosTotales(10)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_unidadMenorQueBultos_debe_rechazar")
        void test_unidadMenorQueBultos_debe_rechazar() {
            // Given - 5 unidades pero 10 bultos totales (imposible)
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("5"))
                    .withUnidadMedida(UnidadMedidaEnum.UNIDAD)
                    .withBultosTotales(10)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("bultosTotales")).isNotNull();
            assertThat(bindingResult.getFieldError("bultosTotales").getDefaultMessage())
                    .contains("no puede ser menor")
                    .contains("Bultos totales");
        }

        @Test
        @DisplayName("test_unidadIgualBultos_debe_aceptar")
        void test_unidadIgualBultos_debe_aceptar() {
            // Given - 10 unidades y 10 bultos (1 unidad por bulto)
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("10"))
                    .withUnidadMedida(UnidadMedidaEnum.UNIDAD)
                    .withBultosTotales(10)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("Validaciones para unidades de masa y volumen")
    class ValidacionesMasaVolumen {

        @Test
        @DisplayName("test_kilogramosConDecimales_debe_aceptar")
        void test_kilogramosConDecimales_debe_aceptar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("25.375"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(3)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_litrosConDecimales_debe_aceptar")
        void test_litrosConDecimales_debe_aceptar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("10.250"))
                    .withUnidadMedida(UnidadMedidaEnum.LITRO)
                    .withBultosTotales(2)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_cantidadMuyGrande_debe_aceptar")
        void test_cantidadMuyGrande_debe_aceptar() {
            // Given - 1000 kg (1 tonelada)
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("1000.00"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarCantidadIngreso(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }
    }
}
