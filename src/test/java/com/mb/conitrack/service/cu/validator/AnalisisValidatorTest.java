package com.mb.conitrack.service.cu.validator;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para AnalisisValidator.
 * Cobertura 100% de todos los métodos y ramas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalisisValidator - Tests")
class AnalisisValidatorTest {

    @Mock
    private AnalisisRepository analisisRepository;

    @Nested
    @DisplayName("validarDatosMandatoriosAnulacionAnalisisInput()")
    class ValidarDatosMandatoriosAnulacionAnalisisInput {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosAnulacionAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando nroAnalisis es null")
        void debe_retornarFalse_cuandoNroAnalisisNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosAnulacionAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
            assertThat(binding.getFieldError("nroAnalisis").getDefaultMessage())
                .isEqualTo("El Nro de Análisis es obligatorio");
        }

        @Test
        @DisplayName("debe retornar false cuando nroAnalisis es vacío")
        void debe_retornarFalse_cuandoNroAnalisisVacio() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosAnulacionAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar false cuando nroAnalisis es whitespace")
        void debe_retornarFalse_cuandoNroAnalisisWhitespace() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("   ");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosAnulacionAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar true cuando nroAnalisis es válido")
        void debe_retornarTrue_cuandoNroAnalisisValido() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("AN-2025-001");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosAnulacionAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarDatosMandatoriosResultadoAnalisisInput()")
    class ValidarDatosMandatoriosResultadoAnalisisInput {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando nroAnalisis es vacío")
        void debe_retornarFalse_cuandoNroAnalisisVacio() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar false cuando dictamenFinal es null")
        void debe_retornarFalse_cuandoDictamenFinalNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("dictamenFinal")).isNotNull();
            assertThat(binding.getFieldError("dictamenFinal").getDefaultMessage())
                .isEqualTo("Debe ingresar un Resultado");
        }

        @Test
        @DisplayName("debe retornar false cuando fechaRealizadoAnalisis es null")
        void debe_retornarFalse_cuandoFechaRealizadoAnalisisNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("fechaRealizadoAnalisis")).isNotNull();
            assertThat(binding.getFieldError("fechaRealizadoAnalisis").getDefaultMessage())
                .isEqualTo("Debe ingresar la fecha en la que se realizó el análisis");
        }

        @Test
        @DisplayName("debe retornar true cuando todos los datos son válidos")
        void debe_retornarTrue_cuandoTodosDatosValidos() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosMandatoriosResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarDatosResultadoAnalisisAprobadoInput()")
    class ValidarDatosResultadoAnalisisAprobadoInput {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando dictamen NO es APROBADO")
        void debe_retornarTrue_cuandoDictamenNoEsAprobado() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.RECHAZADO);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando dictamen APROBADO sin fechas")
        void debe_retornarFalse_cuandoAprobadoSinFechas() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(null);
            dto.setFechaReanalisis(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("fechaVencimiento")).isNotNull();
            assertThat(binding.getFieldError("fechaVencimiento").getDefaultMessage())
                .isEqualTo("Debe ingresar una fecha de Re Análisis o Vencimiento");
        }

        @Test
        @DisplayName("debe retornar false cuando fechaReanalisis posterior a fechaVencimiento")
        void debe_retornarFalse_cuandoFechaReanalisisposteriorAVencimiento() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            dto.setFechaReanalisis(LocalDate.of(2026, 1, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("fechaReanalisis")).isNotNull();
            assertThat(binding.getFieldError("fechaReanalisis").getDefaultMessage())
                .isEqualTo("La fecha de reanalisis no puede ser posterior a la fecha de vencimiento");
        }

        @Test
        @DisplayName("debe retornar true cuando fechaReanalisis anterior a fechaVencimiento")
        void debe_retornarTrue_cuandoFechaReanalisisAnteriorAVencimiento() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            dto.setFechaReanalisis(LocalDate.of(2025, 6, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando solo tiene fechaVencimiento")
        void debe_retornarTrue_cuandoSoloTieneFechaVencimiento() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            dto.setFechaReanalisis(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando solo tiene fechaReanalisis")
        void debe_retornarTrue_cuandoSoloTieneFechaReanalisis() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(null);
            dto.setFechaReanalisis(LocalDate.of(2025, 6, 1));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando titulo es null")
        void debe_retornarTrue_cuandoTituloNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            dto.setTitulo(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando titulo mayor a 100")
        void debe_retornarFalse_cuandoTituloMayorA100() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            dto.setTitulo(new BigDecimal("101"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("titulo")).isNotNull();
            assertThat(binding.getFieldError("titulo").getDefaultMessage())
                .isEqualTo("El título no puede ser mayor al 100%");
        }

        @Test
        @DisplayName("debe retornar false cuando titulo es 0")
        void debe_retornarFalse_cuandoTituloEsCero() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            dto.setTitulo(BigDecimal.ZERO);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("titulo")).isNotNull();
            assertThat(binding.getFieldError("titulo").getDefaultMessage())
                .isEqualTo("El título no puede ser menor o igual a 0");
        }

        @Test
        @DisplayName("debe retornar false cuando titulo es negativo")
        void debe_retornarFalse_cuandoTituloNegativo() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            dto.setTitulo(new BigDecimal("-5"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("titulo")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar true cuando titulo es válido (entre 0 y 100)")
        void debe_retornarTrue_cuandoTituloValido() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            dto.setTitulo(new BigDecimal("99.5"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando titulo es exactamente 100")
        void debe_retornarTrue_cuandoTituloEs100() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            dto.setTitulo(new BigDecimal("100"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarNroAnalisisNotNull()")
    class ValidarNroAnalisisNotNull {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisNotNull(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando ambos nros son vacíos")
        void debe_retornarFalse_cuandoAmbosNrosVacios() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("");
            dto.setNroReanalisis("");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisNotNull(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
            assertThat(binding.getFieldError("nroAnalisis").getDefaultMessage())
                .isEqualTo("Ingrese un nro de analisis");
        }

        @Test
        @DisplayName("debe retornar false cuando ambos nros son null")
        void debe_retornarFalse_cuandoAmbosNrosNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis(null);
            dto.setNroReanalisis(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisNotNull(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar true cuando nroAnalisis está presente")
        void debe_retornarTrue_cuandoNroAnalisisPresente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("AN-2025-001");
            dto.setNroReanalisis("");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisNotNull(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando nroReanalisis está presente")
        void debe_retornarTrue_cuandoNroReanalisisPresente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("");
            dto.setNroReanalisis("RAN-2025-001");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisNotNull(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarNroAnalisisUnico()")
    class ValidarNroAnalisisUnico {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisUnico(dto, binding, analisisRepository);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando nroAnalisis no existe")
        void debe_retornarTrue_cuandoNroAnalisisNoExiste() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("AN-2025-001");
            dto.setNroReanalisis("");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(null);

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisUnico(dto, binding, analisisRepository);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando nroAnalisis existe en mismo lote con dictamen")
        void debe_retornarFalse_cuandoNroAnalisisExisteEnMismoLoteConDictamen() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setNroReanalisis("");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            Lote lote = new Lote();
            lote.setCodigoLote("L-001");

            Analisis analisis = new Analisis();
            analisis.setNroAnalisis("AN-2025-001");
            analisis.setLote(lote);
            analisis.setDictamen(DictamenEnum.APROBADO);

            when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisis);

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisUnico(dto, binding, analisisRepository);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
            assertThat(binding.getFieldError("nroAnalisis").getDefaultMessage())
                .isEqualTo("Nro de analisis ya registrado en el mismo lote.");
        }

        @Test
        @DisplayName("debe retornar true cuando nroAnalisis existe en mismo lote sin dictamen")
        void debe_retornarTrue_cuandoNroAnalisisExisteEnMismoLoteSinDictamen() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setNroReanalisis("");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            Lote lote = new Lote();
            lote.setCodigoLote("L-001");

            Analisis analisis = new Analisis();
            analisis.setNroAnalisis("AN-2025-001");
            analisis.setLote(lote);
            analisis.setDictamen(null);

            when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisis);

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisUnico(dto, binding, analisisRepository);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando nroAnalisis existe en otro lote")
        void debe_retornarFalse_cuandoNroAnalisisExisteEnOtroLote() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setNroReanalisis("");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            Lote lote = new Lote();
            lote.setCodigoLote("L-002");

            Analisis analisis = new Analisis();
            analisis.setNroAnalisis("AN-2025-001");
            analisis.setLote(lote);
            analisis.setDictamen(DictamenEnum.APROBADO);

            when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisis);

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisUnico(dto, binding, analisisRepository);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
            assertThat(binding.getFieldError("nroAnalisis").getDefaultMessage())
                .isEqualTo("Nro de analisis ya registrado en otro lote.");
        }

        @Test
        @DisplayName("debe validar nroReanalisis cuando nroAnalisis está vacío")
        void debe_validarNroReanalisis_cuandoNroAnalisisVacio() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-001");
            dto.setNroAnalisis("");
            dto.setNroReanalisis("RAN-2025-001");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            Lote lote = new Lote();
            lote.setCodigoLote("L-002");

            Analisis analisis = new Analisis();
            analisis.setNroAnalisis("RAN-2025-001");
            analisis.setLote(lote);
            analisis.setDictamen(DictamenEnum.APROBADO);

            when(analisisRepository.findByNroAnalisisAndActivoTrue("RAN-2025-001")).thenReturn(analisis);

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisUnico(dto, binding, analisisRepository);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("nroReanalisis")).isNotNull();
            assertThat(binding.getFieldError("nroReanalisis").getDefaultMessage())
                .isEqualTo("Nro de analisis ya registrado en otro lote.");
        }

        @Test
        @DisplayName("debe retornar true cuando nroReanalisis no existe")
        void debe_retornarTrue_cuandoNroReanalisisNoExiste() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-001");
            dto.setNroAnalisis("");
            dto.setNroReanalisis("RAN-2025-001");
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            when(analisisRepository.findByNroAnalisisAndActivoTrue("RAN-2025-001")).thenReturn(null);

            // When
            boolean resultado = AnalisisValidator.validarNroAnalisisUnico(dto, binding, analisisRepository);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }
}
