package com.mb.conitrack.service.cu.validator;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para TrazaValidator.
 * Cobertura 100% de todos los métodos y ramas.
 */
@DisplayName("TrazaValidator - Tests")
class TrazaValidatorTest {

    @Nested
    @DisplayName("validarTrazaInicialLote()")
    class ValidarTrazaInicialLote {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = TrazaValidator.validarTrazaInicialLote(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando trazaInicial es null")
        void debe_retornarFalse_cuandoTrazaInicialNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setTrazaInicial(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = TrazaValidator.validarTrazaInicialLote(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("trazaInicial")).isNotNull();
            assertThat(binding.getFieldError("trazaInicial").getDefaultMessage())
                .isEqualTo("Ingrese un valor válido para la traza inicial del lote");
        }

        @Test
        @DisplayName("debe retornar false cuando trazaInicial es 0")
        void debe_retornarFalse_cuandoTrazaInicialCero() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setTrazaInicial(0L);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = TrazaValidator.validarTrazaInicialLote(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("trazaInicial")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar false cuando trazaInicial es negativa")
        void debe_retornarFalse_cuandoTrazaInicialNegativa() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setTrazaInicial(-5L);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = TrazaValidator.validarTrazaInicialLote(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("trazaInicial")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar true cuando trazaInicial es válida")
        void debe_retornarTrue_cuandoTrazaInicialValida() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setTrazaInicial(1L);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = TrazaValidator.validarTrazaInicialLote(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando trazaInicial es un número grande")
        void debe_retornarTrue_cuandoTrazaInicialEsGrande() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setTrazaInicial(999999L);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = TrazaValidator.validarTrazaInicialLote(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarTrazasDevolucion()")
    class ValidarTrazasDevolucion {

        @Test
        @DisplayName("debe retornar false cuando trazaDTOs es null")
        void debe_retornarFalse_cuandoTrazaDTOsNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setTrazaDTOs(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = TrazaValidator.validarTrazasDevolucion(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("trazaDTOs")).isNotNull();
            assertThat(binding.getFieldError("trazaDTOs").getDefaultMessage())
                .isEqualTo("Debe seleccionar al menos una traza para devolver.");
        }

        @Test
        @DisplayName("debe retornar false cuando trazaDTOs es lista vacía")
        void debe_retornarFalse_cuandoTrazaDTOsVacia() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setTrazaDTOs(new ArrayList<>());
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = TrazaValidator.validarTrazasDevolucion(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("trazaDTOs")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar true cuando trazaDTOs tiene elementos")
        void debe_retornarTrue_cuandoTrazaDTOsTieneElementos() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            TrazaDTO traza = new TrazaDTO();
            traza.setNroTraza(1L);
            dto.setTrazaDTOs(Arrays.asList(traza));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = TrazaValidator.validarTrazasDevolucion(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando trazaDTOs tiene múltiples elementos")
        void debe_retornarTrue_cuandoTrazaDTOsTieneVariosElementos() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            TrazaDTO traza1 = new TrazaDTO();
            traza1.setNroTraza(1L);
            TrazaDTO traza2 = new TrazaDTO();
            traza2.setNroTraza(2L);
            dto.setTrazaDTOs(Arrays.asList(traza1, traza2));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = TrazaValidator.validarTrazasDevolucion(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }
}
