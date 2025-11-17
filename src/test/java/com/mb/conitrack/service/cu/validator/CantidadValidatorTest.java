package com.mb.conitrack.service.cu.validator;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para CantidadValidator.
 * Cobertura 100% de todos los métodos y ramas.
 */
@DisplayName("CantidadValidator - Tests")
class CantidadValidatorTest {

    @Nested
    @DisplayName("validarBultos()")
    class ValidarBultos {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            LoteDTO dto = new LoteDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = CantidadValidator.validarBultos(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando bultosTotales es 1")
        void debe_retornarTrue_cuandoBultosTotalesEs1() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setBultosTotales(1);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarBultos(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando bultosTotales > 1 y validaciones pasan")
        void debe_retornarTrue_cuandoBultosTotalesMayorA1YValidacionesPasan() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setBultosTotales(2);
            dto.setCantidadInicial(new BigDecimal("100.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50.00"), new BigDecimal("50.00")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarBultos(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando validarTipoDeDato falla")
        void debe_retornarFalse_cuandoValidarTipoDeDatoFalla() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setBultosTotales(2);
            dto.setCantidadInicial(new BigDecimal("100.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidadesBultos(new ArrayList<>()); // Lista vacía
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarBultos(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("debe retornar false cuando validarSumaBultosConvertida falla")
        void debe_retornarFalse_cuandoValidarSumaBultosConvertidaFalla() {
            // Given - Covers line 38: validarTipoDeDato passes but validarSumaBultosConvertida fails
            LoteDTO dto = new LoteDTO();
            dto.setBultosTotales(2);
            dto.setCantidadInicial(new BigDecimal("100.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("40.00"), new BigDecimal("50.00"))); // Suma != total
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarBultos(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
        }
    }

    @Nested
    @DisplayName("validarCantidadIngreso()")
    class ValidarCantidadIngreso {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            LoteDTO dto = new LoteDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = CantidadValidator.validarCantidadIngreso(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando cantidadInicial es null")
        void debe_retornarFalse_cuandoCantidadInicialNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadIngreso(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadInicial")).isNotNull();
            assertThat(binding.getFieldError("cantidadInicial").getDefaultMessage())
                .isEqualTo("La cantidad no puede ser nula.");
        }

        @Test
        @DisplayName("debe retornar false cuando unidad es UNIDAD y cantidad es decimal")
        void debe_retornarFalse_cuandoUnidadUNIDADYCantidadDecimal() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("10.5"));
            dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadIngreso(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadInicial")).isNotNull();
            assertThat(binding.getFieldError("cantidadInicial").getDefaultMessage())
                .contains("debe ser un número entero positivo");
        }

        @Test
        @DisplayName("debe retornar false cuando unidad es UNIDAD y cantidad menor a bultos")
        void debe_retornarFalse_cuandoUnidadUNIDADYCantidadMenorABultos() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("5"));
            dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            dto.setBultosTotales(10);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadIngreso(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("bultosTotales")).isNotNull();
            assertThat(binding.getFieldError("bultosTotales").getDefaultMessage())
                .contains("no puede ser menor a la cantidad de  Bultos totales");
        }

        @Test
        @DisplayName("debe retornar true cuando unidad es UNIDAD y cantidad es entero válido")
        void debe_retornarTrue_cuandoUnidadUNIDADYCantidadEnteraValida() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("100"));
            dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            dto.setBultosTotales(10);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadIngreso(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando unidad NO es UNIDAD")
        void debe_retornarTrue_cuandoUnidadNoEsUNIDAD() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("50.5"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadIngreso(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarCantidadesMovimiento()")
    class ValidarCantidadesMovimiento {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            Bulto bulto = new Bulto();
            bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = CantidadValidator.validarCantidadesMovimiento(dto, bulto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando unidad no es compatible")
        void debe_retornarFalse_cuandoUnidadNoCompatible() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setUnidadMedida(UnidadMedidaEnum.LITRO);
            dto.setCantidad(new BigDecimal("10"));
            Bulto bulto = new Bulto();
            bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto.setCantidadActual(new BigDecimal("100"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesMovimiento(dto, bulto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("unidadMedida")).isNotNull();
            assertThat(binding.getFieldError("unidadMedida").getDefaultMessage())
                .isEqualTo("Unidad no compatible con el producto.");
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad es null")
        void debe_retornarFalse_cuandoCantidadNull() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidad(null);
            Bulto bulto = new Bulto();
            bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto.setCantidadActual(new BigDecimal("100"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesMovimiento(dto, bulto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidad")).isNotNull();
            assertThat(binding.getFieldError("cantidad").getDefaultMessage())
                .isEqualTo("La cantidad debe ser mayor a 0.");
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad es 0")
        void debe_retornarFalse_cuandoCantidadCero() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidad(BigDecimal.ZERO);
            Bulto bulto = new Bulto();
            bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto.setCantidadActual(new BigDecimal("100"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesMovimiento(dto, bulto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidad")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad negativa")
        void debe_retornarFalse_cuandoCantidadNegativa() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidad(new BigDecimal("-5"));
            Bulto bulto = new Bulto();
            bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto.setCantidadActual(new BigDecimal("100"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesMovimiento(dto, bulto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidad")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad excede stock")
        void debe_retornarFalse_cuandoCantidadExcedeStock() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidad(new BigDecimal("150"));
            Bulto bulto = new Bulto();
            bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto.setCantidadActual(new BigDecimal("100"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesMovimiento(dto, bulto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidad")).isNotNull();
            assertThat(binding.getFieldError("cantidad").getDefaultMessage())
                .isEqualTo("La cantidad excede el stock disponible del bulto.");
        }

        @Test
        @DisplayName("debe retornar true cuando cantidad es válida")
        void debe_retornarTrue_cuandoCantidadValida() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidad(new BigDecimal("50"));
            Bulto bulto = new Bulto();
            bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto.setCantidadActual(new BigDecimal("100"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesMovimiento(dto, bulto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando cantidad convertida es válida")
        void debe_retornarTrue_cuandoCantidadConvertidaValida() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setUnidadMedida(UnidadMedidaEnum.GRAMO);
            dto.setCantidad(new BigDecimal("5000")); // 5 kg
            Bulto bulto = new Bulto();
            bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto.setCantidadActual(new BigDecimal("100"));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesMovimiento(dto, bulto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarCantidadesPorMedidas()")
    class ValidarCantidadesPorMedidas {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            LoteDTO dto = new LoteDTO();
            Lote lote = new Lote();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando cantidadesBultos es null")
        void debe_retornarFalse_cuandoCantidadesBultosNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(null);
            Lote lote = new Lote();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .isEqualTo("Debe ingresar las cantidades a consumir");
        }

        @Test
        @DisplayName("debe retornar false cuando cantidadesBultos es vacía")
        void debe_retornarFalse_cuandoCantidadesBultosVacia() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(new ArrayList<>());
            Lote lote = new Lote();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar false cuando unidadMedidaBultos es null")
        void debe_retornarFalse_cuandoUnidadMedidaBultosNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));
            dto.setUnidadMedidaBultos(null);
            Lote lote = new Lote();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .isEqualTo("Debe ingresar las unidades de medida");
        }

        @Test
        @DisplayName("debe retornar false cuando unidadMedidaBultos es vacía")
        void debe_retornarFalse_cuandoUnidadMedidaBultosVacia() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));
            dto.setUnidadMedidaBultos(new ArrayList<>());
            Lote lote = new Lote();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .isEqualTo("Debe ingresar las unidades de medida");
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad individual es null")
        void debe_retornarFalse_cuandoCantidadIndividualNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList((BigDecimal) null));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO));
            Lote lote = new Lote();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .isEqualTo("La cantidad no puede ser nula");
        }

        @Test
        @DisplayName("debe continuar cuando cantidad es cero")
        void debe_continuar_cuandoCantidadCero() {
            // Given
            Bulto bulto1 = new Bulto();
            bulto1.setNroBulto(1);
            bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto1.setCantidadActual(new BigDecimal("50"));
            bulto1.setActivo(true);

            Lote lote = new Lote();
            lote.setBultos(Arrays.asList(bulto1));

            LoteDTO dto = new LoteDTO();
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(BigDecimal.ZERO));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad es negativa")
        void debe_retornarFalse_cuandoCantidadNegativa() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("-5")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO));
            Lote lote = new Lote();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .isEqualTo("La cantidad no puede ser negativa");
        }

        @Test
        @DisplayName("debe retornar false cuando unidadMedida individual es null")
        void debe_retornarFalse_cuandoUnidadMedidaIndividualNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));
            dto.setUnidadMedidaBultos(Arrays.asList((UnidadMedidaEnum) null));
            Lote lote = new Lote();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .isEqualTo("Debe indicar la unidad");
        }

        @Test
        @DisplayName("debe retornar false cuando bulto no encontrado")
        void debe_retornarFalse_cuandoBultoNoEncontrado() {
            // Given
            Lote lote = new Lote();
            lote.setBultos(new ArrayList<>());

            LoteDTO dto = new LoteDTO();
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .isEqualTo("Bulto no encontrado");
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad excede stock (misma unidad)")
        void debe_retornarFalse_cuandoCantidadExcedeStock_mismaUnidad() {
            // Given
            Bulto bulto1 = new Bulto();
            bulto1.setNroBulto(1);
            bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto1.setCantidadActual(new BigDecimal("50"));
            bulto1.setActivo(true);

            Lote lote = new Lote();
            lote.setBultos(Arrays.asList(bulto1));

            LoteDTO dto = new LoteDTO();
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("100")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .contains("no puede superar el stock actual del bulto");
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad excede stock (unidades diferentes)")
        void debe_retornarFalse_cuandoCantidadExcedeStock_unidadesDiferentes() {
            // Given
            Bulto bulto1 = new Bulto();
            bulto1.setNroBulto(1);
            bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto1.setCantidadActual(new BigDecimal("50"));
            bulto1.setActivo(true);

            Lote lote = new Lote();
            lote.setBultos(Arrays.asList(bulto1));

            LoteDTO dto = new LoteDTO();
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("100000"))); // 100kg en gramos
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.GRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar true cuando todas las validaciones pasan (misma unidad)")
        void debe_retornarTrue_cuandoValidacionesPasan_mismaUnidad() {
            // Given
            Bulto bulto1 = new Bulto();
            bulto1.setNroBulto(1);
            bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto1.setCantidadActual(new BigDecimal("50"));
            bulto1.setActivo(true);

            Lote lote = new Lote();
            lote.setBultos(Arrays.asList(bulto1));

            LoteDTO dto = new LoteDTO();
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("30")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando todas las validaciones pasan (unidades diferentes)")
        void debe_retornarTrue_cuandoValidacionesPasan_unidadesDiferentes() {
            // Given
            Bulto bulto1 = new Bulto();
            bulto1.setNroBulto(1);
            bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            bulto1.setCantidadActual(new BigDecimal("50"));
            bulto1.setActivo(true);

            Lote lote = new Lote();
            lote.setBultos(Arrays.asList(bulto1));

            LoteDTO dto = new LoteDTO();
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10000"))); // 10kg en gramos
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.GRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarCantidadesPorMedidas(dto, lote, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarSumaBultosConvertida()")
    class ValidarSumaBultosConvertida {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            LoteDTO dto = new LoteDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando cantidades es null")
        void debe_retornarFalse_cuandoCantidadesNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(null);
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .isEqualTo("Datos incompletos o inconsistentes.");
        }

        @Test
        @DisplayName("debe retornar false cuando unidades es null")
        void debe_retornarFalse_cuandoUnidadesNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50")));
            dto.setUnidadMedidaBultos(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar false cuando tamaños no coinciden")
        void debe_retornarFalse_cuandoTamanosNoCoinciden() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50"), new BigDecimal("50")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO)); // Solo 1 elemento
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
        }

        @Test
        @DisplayName("debe omitir elementos null al sumar")
        void debe_omitirElementosNull_alSumar() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("50.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50.00"), null));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe omitir elementos cuando unidadBulto es null")
        void debe_omitirElementos_cuandoUnidadBultoNull() {
            // Given - Covers line 226-227 (continue when unidadBulto is null)
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("50.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50.00"), new BigDecimal("10.00")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, null));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad individual es 0 o negativa")
        void debe_retornarFalse_cuandoCantidadIndividualCeroONegativa() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("100.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50.00"), new BigDecimal("0")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .contains("debe ser mayor a 0");
        }

        @Test
        @DisplayName("debe retornar false cuando suma no coincide con total")
        void debe_retornarFalse_cuandoSumaNoCoincideConTotal() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("100.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("40.00"), new BigDecimal("50.00")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .contains("no coincide con la cantidad total");
        }

        @Test
        @DisplayName("debe retornar true cuando suma coincide con total (misma unidad)")
        void debe_retornarTrue_cuandoSumaCoincideConTotal_mismaUnidad() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("100.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50.00"), new BigDecimal("50.00")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando suma coincide con total (unidades diferentes)")
        void debe_retornarTrue_cuandoSumaCoincideConTotal_unidadesDiferentes() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadInicial(new BigDecimal("100.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50.00"), new BigDecimal("50000.00")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.GRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarSumaBultosConvertida(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarTipoDeDato()")
    class ValidarTipoDeDato {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            LoteDTO dto = new LoteDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = CantidadValidator.validarTipoDeDato(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando cantidades está vacía")
        void debe_retornarFalse_cuandoCantidadesVacia() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(new ArrayList<>());
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarTipoDeDato(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadInicial")).isNotNull();
            assertThat(binding.getFieldError("cantidadInicial").getDefaultMessage())
                .contains("La cantidad del Lote no puede ser nula");
        }

        @Test
        @DisplayName("debe retornar false cuando unidades está vacía")
        void debe_retornarFalse_cuandoUnidadesVacia() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50")));
            dto.setUnidadMedidaBultos(new ArrayList<>());
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarTipoDeDato(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadInicial")).isNotNull();
            assertThat(binding.getFieldError("cantidadInicial").getDefaultMessage())
                .contains("Las unidades de medida del Lote no pueden ser nulas");
        }

        @Test
        @DisplayName("debe retornar false cuando cantidad individual es null")
        void debe_retornarFalse_cuandoCantidadIndividualNull() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(Arrays.asList(null, new BigDecimal("50")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarTipoDeDato(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadInicial")).isNotNull();
            assertThat(binding.getFieldError("cantidadInicial").getDefaultMessage())
                .contains("La cantidad del Bulto 1 no puede ser nula");
        }

        @Test
        @DisplayName("debe retornar false cuando unidad es UNIDAD y cantidad es decimal")
        void debe_retornarFalse_cuandoUnidadUNIDADYCantidadDecimal() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10.5")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.UNIDAD));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarTipoDeDato(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadInicial")).isNotNull();
            assertThat(binding.getFieldError("cantidadInicial").getDefaultMessage())
                .contains("debe ser un número entero positivo cuando la unidad es UNIDAD");
        }

        @Test
        @DisplayName("debe retornar true cuando todas las cantidades son válidas")
        void debe_retornarTrue_cuandoTodasCantidadesValidas() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50.5"), new BigDecimal("49.5")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarTipoDeDato(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("debe retornar true cuando unidad es UNIDAD y cantidad es entera")
        void debe_retornarTrue_cuandoUnidadUNIDADYCantidadEntera() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.UNIDAD));
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarTipoDeDato(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("validarUnidadMedidaVenta()")
    class ValidarUnidadMedidaVenta {

        @Test
        @DisplayName("debe retornar false cuando binding tiene errores")
        void debe_retornarFalse_cuandoBindingTieneErrores() {
            // Given
            LoteDTO dto = new LoteDTO();
            Lote lote = new Lote();
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");
            binding.reject("error", "error previo");

            // When
            boolean resultado = CantidadValidator.validarUnidadMedidaVenta(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("debe retornar false cuando lote NO es UNIDAD")
        void debe_retornarFalse_cuandoLoteNoEsUNIDAD() {
            // Given
            LoteDTO dto = new LoteDTO();
            Lote lote = new Lote();
            lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarUnidadMedidaVenta(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
            assertThat(binding.getFieldError("cantidadesBultos").getDefaultMessage())
                .isEqualTo("La venta de producto solo es aplicable a UNIDADES");
        }

        @Test
        @DisplayName("debe retornar false cuando algún bulto NO es UNIDAD")
        void debe_retornarFalse_cuandoAlgunBultoNoEsUNIDAD() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.UNIDAD, UnidadMedidaEnum.KILOGRAMO));
            Lote lote = new Lote();
            lote.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarUnidadMedidaVenta(dto, lote, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.getFieldError("cantidadesBultos")).isNotNull();
        }

        @Test
        @DisplayName("debe retornar true cuando lote y todos los bultos son UNIDAD")
        void debe_retornarTrue_cuandoLoteYTodosBultosUNIDAD() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.UNIDAD, UnidadMedidaEnum.UNIDAD));
            Lote lote = new Lote();
            lote.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            BindingResult binding = new BeanPropertyBindingResult(dto, "dto");

            // When
            boolean resultado = CantidadValidator.validarUnidadMedidaVenta(dto, lote, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }
}
