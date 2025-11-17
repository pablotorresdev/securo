package com.mb.conitrack.service.cu.validator;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para FechaValidator.
 * Cobertura 100% de todos los métodos y ramas.
 */
@DisplayName("FechaValidator - Tests")
class FechaValidatorTest {

    @Nested
    @DisplayName("validarFechasProveedor()")
    class ValidarFechasProveedor {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            LoteDTO dto = new LoteDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = FechaValidator.validarFechasProveedor(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando ambas fechas son null")
        void debe_retornarTrue_cuandoAmbasFechasNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaReanalisisProveedor(null);
            dto.setFechaVencimientoProveedor(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasProveedor(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando solo fechaReanalisisProveedor es null")
        void debe_retornarTrue_cuandoSoloFechaReanalisisNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaReanalisisProveedor(null);
            dto.setFechaVencimientoProveedor(LocalDate.of(2025, 12, 31));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasProveedor(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando solo fechaVencimientoProveedor es null")
        void debe_retornarTrue_cuandoSoloFechaVencimientoNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaReanalisisProveedor(LocalDate.of(2025, 6, 1));
            dto.setFechaVencimientoProveedor(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasProveedor(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando fechaReanalisis posterior a fechaVencimiento")
        void debe_retornarFalse_cuandoFechaReanalisisPosteriorAVencimiento() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaReanalisisProveedor(LocalDate.of(2026, 1, 1));
            dto.setFechaVencimientoProveedor(LocalDate.of(2025, 12, 31));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasProveedor(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("fechaReanalisisProveedor")).isNotNull();
            assertThat(binding.getFieldError("fechaReanalisisProveedor").getDefaultMessage())
                .isEqualTo("La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
        }

        @Test
        @DisplayName("debe retornar true cuando fechaReanalisis anterior a fechaVencimiento")
        void debe_retornarTrue_cuandoFechaReanalisisAnteriorAVencimiento() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaReanalisisProveedor(LocalDate.of(2025, 6, 1));
            dto.setFechaVencimientoProveedor(LocalDate.of(2025, 12, 31));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasProveedor(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando fechaReanalisis igual a fechaVencimiento")
        void debe_retornarTrue_cuandoFechaReanalisisIgualAVencimiento() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaReanalisisProveedor(LocalDate.of(2025, 12, 31));
            dto.setFechaVencimientoProveedor(LocalDate.of(2025, 12, 31));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasProveedor(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarFechasReanalisis()")
    class ValidarFechasReanalisis {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = FechaValidator.validarFechasReanalisis(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando ambas fechas son null")
        void debe_retornarTrue_cuandoAmbasFechasNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaReanalisis(null);
            dto.setFechaVencimiento(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasReanalisis(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando solo fechaReanalisis es null")
        void debe_retornarTrue_cuandoSoloFechaReanalisisNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaReanalisis(null);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasReanalisis(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando solo fechaVencimiento es null")
        void debe_retornarTrue_cuandoSoloFechaVencimientoNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaReanalisis(LocalDate.of(2025, 6, 1));
            dto.setFechaVencimiento(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasReanalisis(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando fechaReanalisis posterior a fechaVencimiento")
        void debe_retornarFalse_cuandoFechaReanalisisPosteriorAVencimiento() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaReanalisis(LocalDate.of(2026, 1, 1));
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasReanalisis(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("fechaVencimiento")).isNotNull();
            assertThat(binding.getFieldError("fechaVencimiento").getDefaultMessage())
                .isEqualTo("La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
        }

        @Test
        @DisplayName("debe retornar true cuando fechaReanalisis anterior a fechaVencimiento")
        void debe_retornarTrue_cuandoFechaReanalisisAnteriorAVencimiento() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaReanalisis(LocalDate.of(2025, 6, 1));
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechasReanalisis(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarFechaAnalisisPosteriorIngresoLote()")
    class ValidarFechaAnalisisPosteriorIngresoLote {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            LocalDate fechaIngreso = LocalDate.of(2025, 1, 1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = FechaValidator.validarFechaAnalisisPosteriorIngresoLote(dto, fechaIngreso, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando fechaRealizadoAnalisis es null")
        void debe_retornarTrue_cuandoFechaRealizadoAnalisisNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaRealizadoAnalisis(null);
            LocalDate fechaIngreso = LocalDate.of(2025, 1, 1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaAnalisisPosteriorIngresoLote(dto, fechaIngreso, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando fechaRealizadoAnalisis anterior a fechaIngreso")
        void debe_retornarFalse_cuandoFechaRealizadoAnalisisAnteriorAIngreso() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaRealizadoAnalisis(LocalDate.of(2024, 12, 31));
            LocalDate fechaIngreso = LocalDate.of(2025, 1, 1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaAnalisisPosteriorIngresoLote(dto, fechaIngreso, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("fechaRealizadoAnalisis")).isNotNull();
            assertThat(binding.getFieldError("fechaRealizadoAnalisis").getDefaultMessage())
                .isEqualTo("La fecha de realizado el analisis no puede ser anterior a la fecha de ingreso del lote");
        }

        @Test
        @DisplayName("debe retornar true cuando fechaRealizadoAnalisis igual a fechaIngreso")
        void debe_retornarTrue_cuandoFechaRealizadoAnalisisIgualAIngreso() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaRealizadoAnalisis(LocalDate.of(2025, 1, 1));
            LocalDate fechaIngreso = LocalDate.of(2025, 1, 1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaAnalisisPosteriorIngresoLote(dto, fechaIngreso, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando fechaRealizadoAnalisis posterior a fechaIngreso")
        void debe_retornarTrue_cuandoFechaRealizadoAnalisisPosteriorAIngreso() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaRealizadoAnalisis(LocalDate.of(2025, 1, 15));
            LocalDate fechaIngreso = LocalDate.of(2025, 1, 1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaAnalisisPosteriorIngresoLote(dto, fechaIngreso, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarFechaEgresoLoteDtoPosteriorLote()")
    class ValidarFechaEgresoLoteDtoPosteriorLote {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            LoteDTO dto = new LoteDTO();
            Lote lote = new Lote();
            lote.setFechaIngreso(LocalDate.of(2025, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = FechaValidator.validarFechaEgresoLoteDtoPosteriorLote(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando fechaEgreso es null")
        void debe_retornarTrue_cuandoFechaEgresoNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaEgreso(null);
            Lote lote = new Lote();
            lote.setFechaIngreso(LocalDate.of(2025, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaEgresoLoteDtoPosteriorLote(dto, lote, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando fechaEgreso anterior a fechaIngreso")
        void debe_retornarFalse_cuandoFechaEgresoAnteriorAIngreso() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaEgreso(LocalDate.of(2024, 12, 31));
            Lote lote = new Lote();
            lote.setFechaIngreso(LocalDate.of(2025, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaEgresoLoteDtoPosteriorLote(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("fechaEgreso")).isNotNull();
            assertThat(binding.getFieldError("fechaEgreso").getDefaultMessage())
                .isEqualTo("La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
        }

        @Test
        @DisplayName("debe retornar true cuando fechaEgreso igual a fechaIngreso")
        void debe_retornarTrue_cuandoFechaEgresoIgualAIngreso() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaEgreso(LocalDate.of(2025, 1, 1));
            Lote lote = new Lote();
            lote.setFechaIngreso(LocalDate.of(2025, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaEgresoLoteDtoPosteriorLote(dto, lote, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando fechaEgreso posterior a fechaIngreso")
        void debe_retornarTrue_cuandoFechaEgresoPosteriorAIngreso() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setFechaEgreso(LocalDate.of(2025, 1, 15));
            Lote lote = new Lote();
            lote.setFechaIngreso(LocalDate.of(2025, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaEgresoLoteDtoPosteriorLote(dto, lote, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarFechaMovimientoPosteriorIngresoLote()")
    class ValidarFechaMovimientoPosteriorIngresoLote {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            LocalDate fechaIngreso = LocalDate.of(2025, 1, 1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = FechaValidator.validarFechaMovimientoPosteriorIngresoLote(dto, fechaIngreso, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando fechaMovimiento anterior a fechaIngreso")
        void debe_retornarFalse_cuandoFechaMovimientoAnteriorAIngreso() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaMovimiento(LocalDate.of(2024, 12, 31));
            LocalDate fechaIngreso = LocalDate.of(2025, 1, 1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaMovimientoPosteriorIngresoLote(dto, fechaIngreso, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("fechaMovimiento")).isNotNull();
            assertThat(binding.getFieldError("fechaMovimiento").getDefaultMessage())
                .isEqualTo("La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
        }

        @Test
        @DisplayName("debe retornar true cuando fechaMovimiento igual a fechaIngreso")
        void debe_retornarTrue_cuandoFechaMovimientoIgualAIngreso() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaMovimiento(LocalDate.of(2025, 1, 1));
            LocalDate fechaIngreso = LocalDate.of(2025, 1, 1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaMovimientoPosteriorIngresoLote(dto, fechaIngreso, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando fechaMovimiento posterior a fechaIngreso")
        void debe_retornarTrue_cuandoFechaMovimientoPosteriorAIngreso() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaMovimiento(LocalDate.of(2025, 1, 15));
            LocalDate fechaIngreso = LocalDate.of(2025, 1, 1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarFechaMovimientoPosteriorIngresoLote(dto, fechaIngreso, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarMovimientoOrigen()")
    class ValidarMovimientoOrigen {

        @Test
        @DisplayName("debe retornar true cuando fechaMovimiento es null")
        void debe_retornarTrue_cuandoFechaMovimientoNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaMovimiento(null);
            Movimiento movOrigen = new Movimiento();
            movOrigen.setFecha(LocalDate.of(2025, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarMovimientoOrigen(dto, binding, movOrigen);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando fechaMovimiento anterior a fecha de movOrigen")
        void debe_retornarFalse_cuandoFechaMovimientoAnteriorAOrigen() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaMovimiento(LocalDate.of(2024, 12, 31));
            Movimiento movOrigen = new Movimiento();
            movOrigen.setFecha(LocalDate.of(2025, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarMovimientoOrigen(dto, binding, movOrigen);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("fechaMovimiento")).isNotNull();
            assertThat(binding.getFieldError("fechaMovimiento").getDefaultMessage())
                .isEqualTo("La fecha de devolución no puede ser anterior a la fecha del movimiento de venta.");
        }

        @Test
        @DisplayName("debe retornar true cuando fechaMovimiento igual a fecha de movOrigen")
        void debe_retornarTrue_cuandoFechaMovimientoIgualAOrigen() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaMovimiento(LocalDate.of(2025, 1, 1));
            Movimiento movOrigen = new Movimiento();
            movOrigen.setFecha(LocalDate.of(2025, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarMovimientoOrigen(dto, binding, movOrigen);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando fechaMovimiento posterior a fecha de movOrigen")
        void debe_retornarTrue_cuandoFechaMovimientoPosteriorAOrigen() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaMovimiento(LocalDate.of(2025, 1, 15));
            Movimiento movOrigen = new Movimiento();
            movOrigen.setFecha(LocalDate.of(2025, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = FechaValidator.validarMovimientoOrigen(dto, binding, movOrigen);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }
}
