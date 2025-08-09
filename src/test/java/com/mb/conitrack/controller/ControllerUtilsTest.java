package com.mb.conitrack.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static com.mb.conitrack.controller.ControllerUtils.validarBultos;
import static com.mb.conitrack.controller.ControllerUtils.validarSumaBultosConvertida;
import static com.mb.conitrack.controller.ControllerUtils.validarTipoDeDato;
import static com.mb.conitrack.controller.ControllerUtils.validateCantidadIngreso;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllerUtilsTest {

    @Test
    void getCountryList() {
    }

    @Test
    void populateAvailableLoteListByCodigoInterno() {
    }

    @Test
    void populateLoteListByCodigoInterno() {
    }

    @Test
    void validarCantidadesMovimiento() {
    }

    @Test
    void validarCantidadesPorMedidas() {
    }

    @Test
    void validarContraFechasProveedor() {
    }

    @Test
    void validarDatosMandatoriosResultadoAnalisisInput() {
    }

    @Test
    void validarDatosResultadoAnalisisAprobadoInput() {
    }

    @Test
    void validarExisteMuestreoParaAnalisis() {
    }

    @Test
    void validarFechaEgresoLoteDtoPosteriorLote() {
    }

    @Test
    void validarFechaMovimientoPosteriorLote() {
    }

    @Test
    void validarNroAnalisisNotNull() {
    }

    @Test
    void validarValorTitulo() {
    }


    @Test
    void validateFechasProveedor() {
    }

    @Test
    @DisplayName("Si BindingResult ya tiene errores -> return false (early return)")
    void hasErrorsEarlyReturn() {
        LoteDTO dto = new LoteDTO();
        BindingResult br = mock(BindingResult.class);

        when(br.hasErrors()).thenReturn(true);

        boolean ok = validateCantidadIngreso(dto, br);

        assertFalse(ok);
        verify(br).hasErrors();
        // No debe intentar agregar nuevos errores
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("cantidadInicial nula -> error en cantidadInicial y false")
    void cantidadNull() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(null);
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD); // da igual en este caso
        dto.setBultosTotales(1);

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = validateCantidadIngreso(dto, br);

        assertFalse(ok);
        assertTrue(br.hasErrors());
        assertNotNull(br.getFieldError("cantidadInicial"));
        assertEquals("error.cantidadInicial", br.getFieldError("cantidadInicial").getCode());
        assertEquals("La cantidad no puede ser nula.", br.getFieldError("cantidadInicial").getDefaultMessage());
        assertEquals(1, br.getErrorCount());
    }

    @Test
    @DisplayName("UNIDAD con cantidad no-entera -> error en cantidadInicial y false")
    void unidadCantidadNoEntera() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(new BigDecimal("1.5"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(1);

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = validateCantidadIngreso(dto, br);

        assertFalse(ok);
        assertTrue(br.hasErrors());
        assertNotNull(br.getFieldError("cantidadInicial"));
        assertEquals("error.cantidadInicial", br.getFieldError("cantidadInicial").getCode());
        assertEquals(
            "La cantidad debe ser un número entero positivo cuando la unidad es UNIDAD.",
            br.getFieldError("cantidadInicial").getDefaultMessage()
        );
        assertEquals(1, br.getErrorCount());
    }

    @Test
    @DisplayName("UNIDAD con cantidad < bultosTotales -> error en bultosTotales y false")
    void unidadCantidadMenorQueBultos() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(new BigDecimal("2"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(3);

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = validateCantidadIngreso(dto, br);

        assertFalse(ok);
        assertTrue(br.hasErrors());
        assertNotNull(br.getFieldError("bultosTotales"));
        assertEquals("error.bultosTotales", br.getFieldError("bultosTotales").getCode());
        assertEquals(
            "La cantidad de Unidades (2) no puede ser menor a la cantidad de  Bultos totales: 3",
            br.getFieldError("bultosTotales").getDefaultMessage()
        );
        assertEquals(1, br.getErrorCount());
    }

    @Test
    @DisplayName("UNIDAD con cantidad entera >= bultosTotales -> true y sin errores")
    void unidadCantidadValida() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(new BigDecimal("5"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(3);

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = validateCantidadIngreso(dto, br);

        assertTrue(ok);
        assertFalse(br.hasErrors());
        assertEquals(0, br.getErrorCount());
    }

    @Test
    @DisplayName("No-UNIDAD (p.ej. KILOGRAMO) con cantidad decimal -> true y sin errores")
    void noUnidadCantidadDecimalOk() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(new BigDecimal("1.7500"));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        dto.setBultosTotales(999); // irrelevante en este branch

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = validateCantidadIngreso(dto, br);

        assertTrue(ok);
        assertFalse(br.hasErrors());
        assertEquals(0, br.getErrorCount());
    }

//    @Test
//    @DisplayName("Si BindingResult ya tiene errores -> false y no llama a validadores")
//    void earlyReturnPorErrores() {
//        LoteDTO dto = new LoteDTO();
//        BindingResult br = mock(BindingResult.class);
//        when(br.hasErrors()).thenReturn(true);
//
//        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
//            boolean ok = validarBultos(dto, br);
//
//            assertFalse(ok);
//            verify(br).hasErrors();
//            mocked.verifyNoInteractions(); // no se deben invocar los validadores estáticos
//        }
//    }
//
//    @Test
//    @DisplayName("bultosTotales == 1 -> true y no llama a validadores (short-circuit)")
//    void bultosIgualUno() {
//        LoteDTO dto = new LoteDTO();
//        dto.setBultosTotales(1);
//        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");
//
//        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
//            boolean ok = validarBultos(dto, br);
//
//            assertTrue(ok);
//            mocked.verifyNoInteractions(); // no se deben invocar validadores por el OR corto
//        }
//    }
//
//    @Test
//    @DisplayName("bultosTotales > 1 y validarTipoDeDato = false -> false; no llama a validarSumaBultosConvertida")
//    void primerValidadorFalse() {
//        LoteDTO dto = new LoteDTO();
//        dto.setBultosTotales(3);
//        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");
//
//        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
//            mocked.when(() -> ControllerUtils.validarTipoDeDato(dto, br)).thenReturn(false);
//
//            boolean ok = validarBultos(dto, br);
//
//            assertFalse(ok);
//            mocked.verify(() -> ControllerUtils.validarTipoDeDato(dto, br));
//            mocked.verify(() -> ControllerUtils.validarSumaBultosConvertida(dto, br), never());
//        }
//    }
//
//    @Test
//    @DisplayName("bultosTotales > 1, validarTipoDeDato = true y validarSuma = false -> false")
//    void segundoValidadorFalse() {
//        LoteDTO dto = new LoteDTO();
//        dto.setBultosTotales(2);
//        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");
//
//        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
//            mocked.when(() -> ControllerUtils.validarTipoDeDato(dto, br)).thenReturn(true);
//            mocked.when(() -> ControllerUtils.validarSumaBultosConvertida(dto, br)).thenReturn(false);
//
//            boolean ok = validarBultos(dto, br);
//
//            assertFalse(ok);
//            mocked.verify(() -> ControllerUtils.validarTipoDeDato(dto, br));
//            mocked.verify(() -> ControllerUtils.validarSumaBultosConvertida(dto, br));
//        }
//    }
//
//    @Test
//    @DisplayName("bultosTotales > 1 y ambos validadores = true -> true")
//    void ambosValidadoresTrue() {
//        LoteDTO dto = new LoteDTO();
//        dto.setBultosTotales(5);
//        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");
//
//        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
//            mocked.when(() -> ControllerUtils.validarTipoDeDato(dto, br)).thenReturn(true);
//            mocked.when(() -> ControllerUtils.validarSumaBultosConvertida(dto, br)).thenReturn(true);
//
//            boolean ok = validarBultos(dto, br);
//
//            assertTrue(ok);
//            mocked.verify(() -> ControllerUtils.validarTipoDeDato(dto, br));
//            mocked.verify(() -> ControllerUtils.validarSumaBultosConvertida(dto, br));
//        }
//    }


}