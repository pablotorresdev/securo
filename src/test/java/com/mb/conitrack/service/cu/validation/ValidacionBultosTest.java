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
import java.util.Arrays;

import static com.mb.conitrack.testdata.TestDataBuilder.unLoteDTO;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para la validación de bultos (CU1).
 * Prueba validarBultos(), validarTipoDeDato() y validarSumaBultosConvertida().
 *
 * Cobertura:
 * - Un solo bulto (bypass validación)
 * - Múltiples bultos con suma correcta
 * - Suma incorrecta de bultos
 * - Bulto con cantidad nula
 * - Bulto con cantidad cero
 * - Bulto con cantidad negativa
 * - UNIDAD con decimales en bulto
 * - Conversión entre unidades (kg, g)
 * - Conversión entre volúmenes (L, mL)
 * - Listas de tamaño desigual
 * - Datos incompletos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Validación de Bultos - CU1")
class ValidacionBultosTest {

    private TestableAbstractCuService service;
    private BindingResult bindingResult;

    static class TestableAbstractCuService extends AbstractCuService {
        public boolean testValidarBultos(LoteDTO dto, BindingResult result) {
            return validarBultos(dto, result);
        }

        public boolean testValidarSumaBultosConvertida(LoteDTO dto, BindingResult result) {
            return validarSumaBultosConvertida(dto, result);
        }

        public boolean testValidarTipoDeDato(LoteDTO dto, BindingResult result) {
            return validarTipoDeDato(dto, result);
        }
    }

    @BeforeEach
    void setUp() {
        service = new TestableAbstractCuService();
    }

    @Nested
    @DisplayName("Casos de un solo bulto")
    class UnSoloBulto {

        @Test
        @DisplayName("test_unSoloBulto_debe_bypassearValidacion")
        void test_unSoloBulto_debe_bypassearValidacion() {
            // Given - Con 1 bulto, no valida cantidades individuales
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(1)
                    .withCantidadInicial(new BigDecimal("25.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarBultos(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("Validación de suma de cantidades")
    class ValidacionSuma {

        @Test
        @DisplayName("test_multipleBultosConSumaCorrecta_debe_aceptar")
        void test_multipleBultosConSumaCorrecta_debe_aceptar() {
            // Given - 3 bultos: 10 + 8 + 7 = 25 kg
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(3)
                    .withCantidadInicial(new BigDecimal("25.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("10.0"),
                            new BigDecimal("8.0"),
                            new BigDecimal("7.0")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarBultos(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_sumaIncorrecta_debe_rechazar")
        void test_sumaIncorrecta_debe_rechazar() {
            // Given - 3 bultos: 10 + 8 + 5 = 23 kg (pero total es 25)
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(3)
                    .withCantidadInicial(new BigDecimal("25.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("10.0"),
                            new BigDecimal("8.0"),
                            new BigDecimal("5.0")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarBultos(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(bindingResult.getFieldError("cantidadesBultos").getDefaultMessage())
                    .contains("suma")
                    .contains("no coincide");
        }
    }

    @Nested
    @DisplayName("Validación de tipo de dato")
    class ValidacionTipoDato {

        @Test
        @DisplayName("test_bultoCantidadNula_debe_rechazar")
        void test_bultoCantidadNula_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(2)
                    .withCantidadInicial(new BigDecimal("15.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("10.0"),
                            null  // Cantidad nula
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarTipoDeDato(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadInicial")).isNotNull();
            assertThat(bindingResult.getFieldError("cantidadInicial").getDefaultMessage())
                    .contains("Bulto")
                    .contains("no puede ser nula");
        }

        @Test
        @DisplayName("test_bultoCantidadCero_debe_rechazar")
        void test_bultoCantidadCero_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(2)
                    .withCantidadInicial(new BigDecimal("10.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("10.0"),
                            BigDecimal.ZERO
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarSumaBultosConvertida(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(bindingResult.getFieldError("cantidadesBultos").getDefaultMessage())
                    .contains("debe ser mayor a 0");
        }

        @Test
        @DisplayName("test_bultoCantidadNegativa_debe_rechazar")
        void test_bultoCantidadNegativa_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(2)
                    .withCantidadInicial(new BigDecimal("5.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("10.0"),
                            new BigDecimal("-5.0")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarSumaBultosConvertida(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(bindingResult.getFieldError("cantidadesBultos").getDefaultMessage())
                    .contains("debe ser mayor a 0");
        }

        @Test
        @DisplayName("test_bultoUnidadConDecimales_debe_rechazar")
        void test_bultoUnidadConDecimales_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(2)
                    .withCantidadInicial(new BigDecimal("10"))
                    .withUnidadMedida(UnidadMedidaEnum.UNIDAD)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("5"),
                            new BigDecimal("5.5")  // Decimal en UNIDAD
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.UNIDAD,
                            UnidadMedidaEnum.UNIDAD
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarTipoDeDato(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadInicial")).isNotNull();
            assertThat(bindingResult.getFieldError("cantidadInicial").getDefaultMessage())
                    .contains("entero positivo")
                    .contains("UNIDAD");
        }
    }

    @Nested
    @DisplayName("Conversión entre unidades")
    class ConversionUnidades {

        @Test
        @DisplayName("test_conversionKgAGramos_debe_aceptar")
        void test_conversionKgAGramos_debe_aceptar() {
            // Given - Total: 1.5 kg = Bulto1: 1 kg + Bulto2: 500 g
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(2)
                    .withCantidadInicial(new BigDecimal("1.5"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("1.0"),
                            new BigDecimal("500")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.GRAMO
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarBultos(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_conversionLitroAMililitros_debe_aceptar")
        void test_conversionLitroAMililitros_debe_aceptar() {
            // Given - Total: 2 L = Bulto1: 1500 mL + Bulto2: 0.5 L
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(2)
                    .withCantidadInicial(new BigDecimal("2.0"))
                    .withUnidadMedida(UnidadMedidaEnum.LITRO)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("1500"),
                            new BigDecimal("0.5")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.MILILITRO,
                            UnidadMedidaEnum.LITRO
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarBultos(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_conversionConErrorRedondeo_debe_rechazar")
        void test_conversionConErrorRedondeo_debe_rechazar() {
            // Given - Suma no exacta por conversión
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(2)
                    .withCantidadInicial(new BigDecimal("1.5"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("1.0"),
                            new BigDecimal("499")  // 499 g en lugar de 500 g
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.GRAMO
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarBultos(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validación de datos incompletos")
    class DatosIncompletos {

        @Test
        @DisplayName("test_listasDesiguales_debe_rechazar")
        void test_listasDesiguales_debe_rechazar() {
            // Given - 3 cantidades pero 2 unidades
            LoteDTO dto = unLoteDTO()
                    .withBultosTotales(3)
                    .withCantidadInicial(new BigDecimal("25.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("10.0"),
                            new BigDecimal("8.0"),
                            new BigDecimal("7.0")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                            // Falta la tercera unidad
                    ))
                    .build();
            bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.testValidarSumaBultosConvertida(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(bindingResult.getFieldError("cantidadesBultos").getDefaultMessage())
                    .contains("incompletos")
                    .contains("inconsistentes");
        }
    }
}
