package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.utils.ControllerUtils;

import static com.mb.conitrack.utils.ControllerUtils.validarTipoDeDato;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ControllerUtilsTest {

    private static LoteDTO dto(List<BigDecimal> cantidades, List<UnidadMedidaEnum> unidades) {
        LoteDTO d = new LoteDTO();
        d.setCantidadesBultos(cantidades);
        d.setUnidadMedidaBultos(unidades);
        return d;
    }

    @Test
    @DisplayName("Ambos presentes → true, sin reject")
    void ambosPresentes() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis("A-001");
        dto.setNroReanalisis("R-001");
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance()
            .validarNroAnalisisNotNull(dto, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("Ambos nroAnalisis y nroReanalisis vacíos → rejectValue y false")
    void ambosVacios() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis(null);
        dto.setNroReanalisis(null);
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance()
            .validarNroAnalisisNotNull(dto, br);

        assertFalse(ok);
        verify(br).hasErrors();
        verify(br).rejectValue(
            eq("nroAnalisis"),
            eq(""),
            eq("Debe ingresar un nro de Analisis/Reanalisis")
        );
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("bultosTotales > 1 y ambos validadores = true -> true")
    void ambosValidadoresTrue() {
        LoteDTO dto = new LoteDTO();
        dto.getCantidadesBultos().add(new BigDecimal("2"));
        dto.getUnidadMedidaBultos().add(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(5);
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        try (
            MockedStatic<ControllerUtils> mocked =
                mockStatic(ControllerUtils.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {

            mocked.when(() -> validarTipoDeDato(dto, br)).thenReturn(true);
            mocked.when(() -> ControllerUtils.validarSumaBultosConvertida(dto, br)).thenReturn(true);

            boolean ok = ControllerUtils.getInstance().validarBultos(dto, br);

            assertTrue(ok);
            mocked.verify(() -> validarTipoDeDato(dto, br));
            mocked.verify(() -> ControllerUtils.validarSumaBultosConvertida(dto, br));
        }
    }

    @Test
    @DisplayName("bultosTotales == 1 -> true (short-circuit) y NO llama validadores")
    void bultosIgualUno() {
        LoteDTO dto = new LoteDTO();
        dto.setBultosTotales(1);
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        try (
            MockedStatic<ControllerUtils> mocked =
                mockStatic(ControllerUtils.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {

            boolean ok = ControllerUtils.getInstance().validarBultos(dto, br);

            assertTrue(ok);
            mocked.verify(() -> validarTipoDeDato(any(), any()), never());
            mocked.verify(() -> ControllerUtils.validarSumaBultosConvertida(any(), any()), never());
        }
    }

    @Test
    @DisplayName("Cantidad nula -> rejectValue y false")
    void cantidadNula() {
        LoteDTO d = new LoteDTO();
        d.getUnidadMedidaBultos().add(UnidadMedidaEnum.UNIDAD);
        d.setUnidadMedidaBultos(new ArrayList<>());
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = validarTipoDeDato(d, br);

        assertFalse(ok);
        verify(br).hasErrors();
        verify(br).rejectValue(
            eq("cantidadInicial"),
            eq("error.cantidadInicial"),
            eq("La cantidad del Lote no puede ser nula.")
        );
    }

    @Test
    @DisplayName("Cantidad nula bulto -> rejectValue y false")
    void cantidadNulaBulto() {
        LoteDTO d = new LoteDTO();
        d.getUnidadMedidaBultos().add(UnidadMedidaEnum.UNIDAD);
        d.getCantidadesBultos().add(null);
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = validarTipoDeDato(d, br);

        assertFalse(ok);
        verify(br).hasErrors();
        verify(br).rejectValue(
            eq("cantidadInicial"),
            eq("error.cantidadInicial"),
            eq("La cantidad del Bulto 1 no puede ser nula.")
        );
    }

    @Test
    @DisplayName("cantidadInicial nula -> error en cantidadInicial y false")
    void cantidadNull() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(null);
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD); // da igual en este caso
        dto.setBultosTotales(1);

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = ControllerUtils.getInstance().validateCantidadIngreso(dto, br);

        assertFalse(ok);
        assertTrue(br.hasErrors());
        assertNotNull(br.getFieldError("cantidadInicial"));
        assertEquals("error.cantidadInicial", br.getFieldError("cantidadInicial").getCode());
        assertEquals("La cantidad no puede ser nula.", br.getFieldError("cantidadInicial").getDefaultMessage());
        assertEquals(1, br.getErrorCount());
    }

    @Test
    @DisplayName("hasErrors = true → retorna false sin rechazar campo")
    void conErroresPrevios() {
        MovimientoDTO dto = new MovimientoDTO();
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        boolean ok = ControllerUtils.getInstance()
            .validarNroAnalisisNotNull(dto, br);

        assertFalse(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("Si bindingResult.hasErrors() == true => retorna false (early return)")
    void earlyReturnPorErrores() {
        LoteDTO dto = new LoteDTO();
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        boolean ok = ControllerUtils.getInstance().validateFechasProveedor(dto, br);

        assertFalse(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("Si BindingResult ya tiene errores -> false y NO llama a los otros validadores")
    void earlyReturnPorErroresBultos() {
        LoteDTO dto = new LoteDTO();
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        try (
            MockedStatic<ControllerUtils> mocked =
                mockStatic(ControllerUtils.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {

            boolean ok = ControllerUtils.getInstance().validarBultos(dto, br);

            assertFalse(ok);
            verify(br).hasErrors();
            // No debe invocar los validadores internos
            mocked.verify(() -> validarTipoDeDato(any(), any()), never());
            mocked.verify(() -> ControllerUtils.validarSumaBultosConvertida(any(), any()), never());
        }
    }

    @Test
    @DisplayName("Si BindingResult ya tiene errores -> false y no valida nada")
    void earlyReturnPorErroresTipoDeDato() {
        LoteDTO d = dto(List.of(BigDecimal.ONE), List.of(UnidadMedidaEnum.UNIDAD));
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        boolean ok = validarTipoDeDato(d, br);

        assertFalse(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Ambas fechas nulas => true (no valida relación)")
    void fechasAmbasNulas() {
        LoteDTO dto = new LoteDTO(); // ambas fechas null
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance().validateFechasProveedor(dto, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @Test
    void getCountryList() {
    }

    @Test
    @DisplayName("Si BindingResult ya tiene errores -> return false (early return)")
    void hasErrorsEarlyReturn() {
        LoteDTO dto = new LoteDTO();
        BindingResult br = mock(BindingResult.class);

        when(br.hasErrors()).thenReturn(true);

        boolean ok = ControllerUtils.getInstance().validateCantidadIngreso(dto, br);

        assertFalse(ok);
        verify(br).hasErrors();
        // No debe intentar agregar nuevos errores
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Múltiples ítems: Bulto 1 nulo y Bulto 2 UNIDAD decimal -> 2 errores y false")
    void multiplesItems() {
        LoteDTO d = dto(
            List.of(new BigDecimal("3.10"), new BigDecimal("5")),
            List.of(UnidadMedidaEnum.UNIDAD, UnidadMedidaEnum.LITRO)
        );
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = validarTipoDeDato(d, br);

        assertFalse(ok);
        // Se esperan 2 rechazos sobre el mismo field y code, con mensajes distintos
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(br, times(1)).rejectValue(eq("cantidadInicial"), eq("error.cantidadInicial"), messageCaptor.capture());
        var msgs = messageCaptor.getAllValues();
        assertTrue(msgs.get(0).contains("Bulto 1"));
        assertTrue(msgs.get(0).contains("debe ser un número entero positivo"));
        assertTrue(msgs.get(0).contains("UNIDAD"));
    }

    @Test
    @DisplayName("No-UNIDAD (p.ej. KILOGRAMO) con cantidad decimal -> true y sin errores")
    void noUnidadCantidadDecimalOk() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(new BigDecimal("1.7500"));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        dto.setBultosTotales(999); // irrelevante en este branch

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = ControllerUtils.getInstance().validateCantidadIngreso(dto, br);

        assertTrue(ok);
        assertFalse(br.hasErrors());
        assertEquals(0, br.getErrorCount());
    }

    @Test
    @DisplayName("Otra unidad (KILOGRAMO) con decimales -> true")
    void otraUnidadConDecimales() {
        LoteDTO d = dto(List.of(new BigDecimal("1.75")), List.of(UnidadMedidaEnum.KILOGRAMO));
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = validarTipoDeDato(d, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    void populateAvailableLoteListByCodigoInterno() {
    }

    @Test
    void populateLoteListByCodigoInterno() {
    }

    @Test
    @DisplayName("populateLoteListByCodigoInterno: hasErrors = true => false y no llama al service")
    void populateLoteListByCodigoInterno_hasErrorsEarlyReturn() {
        List<Lote> salida = new ArrayList<>();
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);
        LoteService service = mock(LoteService.class);

        boolean ok = ControllerUtils.getInstance()
            .populateLoteListByCodigoInterno(salida, "L-XYZ", br, service);

        assertFalse(ok);
        assertTrue(salida.isEmpty());
        verify(br).hasErrors();
        verifyNoInteractions(service);
        verify(br, never()).reject(anyString(), anyString());
    }

    @Test
    @DisplayName("populateLoteListByCodigoInterno: service devuelve vacío => reject y false")
    void populateLoteListByCodigoInterno_listaVacia() {
        List<Lote> salida = new ArrayList<>();
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        LoteService service = mock(LoteService.class);
        when(service.findLoteListByCodigoInterno("L-ABC"))
            .thenReturn(new ArrayList<>());

        boolean ok = ControllerUtils.getInstance()
            .populateLoteListByCodigoInterno(salida, "L-ABC", br, service);

        assertFalse(ok);
        assertTrue(salida.isEmpty());
        verify(br).hasErrors();
        verify(service).findLoteListByCodigoInterno("L-ABC");
        verify(br).reject(eq("codigoInternoLote"), eq("Lote bloqueado."));
        verifyNoMoreInteractions(service, br);
    }

    @Test
    @DisplayName("populateLoteListByCodigoInterno: lista con elementos => agrega y true")
    void populateLoteListByCodigoInterno_ok() {
        List<Lote> salida = new ArrayList<>();
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        LoteService service = mock(LoteService.class);
        Lote l1 = new Lote();
        Lote l2 = new Lote();
        when(service.findLoteListByCodigoInterno("L-123"))
            .thenReturn(new ArrayList<>(List.of(l1, l2)));

        boolean ok = ControllerUtils.getInstance()
            .populateLoteListByCodigoInterno(salida, "L-123", br, service);

        assertTrue(ok);
        assertEquals(2, salida.size());
        assertSame(l1, salida.get(0));
        assertSame(l2, salida.get(1));
        verify(br).hasErrors();
        verify(service).findLoteListByCodigoInterno("L-123");
        verify(br, never()).reject(anyString(), anyString());
        verifyNoMoreInteractions(service, br);
    }

    @Test
    @DisplayName("bultosTotales > 1 y validarTipoDeDato = false -> false; NO llama validarSuma")
    void primerValidadorFalse() {
        LoteDTO dto = new LoteDTO();
        dto.getCantidadesBultos().add(new BigDecimal("2"));
        dto.getUnidadMedidaBultos().add(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(3);
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        try (
            MockedStatic<ControllerUtils> mocked =
                mockStatic(ControllerUtils.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {

            mocked.when(() -> validarTipoDeDato(dto, br)).thenReturn(false);

            boolean ok = ControllerUtils.getInstance().validarBultos(dto, br);

            assertFalse(ok);
            mocked.verify(() -> validarTipoDeDato(dto, br));
            mocked.verify(() -> ControllerUtils.validarSumaBultosConvertida(any(), any()), never());
        }
    }

    @Test
    @DisplayName("Reanálisis antes de vencimiento => true")
    void reanalisisAntesDeVencimiento() {
        LoteDTO dto = new LoteDTO();
        dto.setFechaReanalisisProveedor(LocalDate.now().plusDays(5));
        dto.setFechaVencimientoProveedor(LocalDate.now().plusDays(10));

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance().validateFechasProveedor(dto, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("Reanálisis == Vencimiento => true (no es posterior)")
    void reanalisisIgualVencimiento() {
        LocalDate d = LocalDate.now().plusDays(5);
        LoteDTO dto = new LoteDTO();
        dto.setFechaReanalisisProveedor(d);
        dto.setFechaVencimientoProveedor(d);

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance().validateFechasProveedor(dto, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("Reanálisis posterior a vencimiento => rejectValue y false")
    void reanalisisPosteriorAVencimiento() {
        LoteDTO dto = new LoteDTO();
        dto.setFechaReanalisisProveedor(LocalDate.now().plusDays(20));
        dto.setFechaVencimientoProveedor(LocalDate.now().plusDays(10));

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance().validateFechasProveedor(dto, br);

        assertFalse(ok);
        verify(br).hasErrors();
        verify(br).rejectValue(
            eq("fechaReanalisisProveedor"),
            eq("error.fechaReanalisisProveedor"),
            eq("La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.")
        );
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("bultosTotales > 1, validarTipoDeDato = true y validarSuma = false -> false")
    void segundoValidadorFalse() {
        LoteDTO dto = new LoteDTO();
        dto.getCantidadesBultos().add(new BigDecimal("2"));
        dto.getUnidadMedidaBultos().add(UnidadMedidaEnum.UNIDAD);
        dto.getCantidadesBultos().add(new BigDecimal("3"));
        dto.getUnidadMedidaBultos().add(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(2);
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        try (
            MockedStatic<ControllerUtils> mocked =
                mockStatic(ControllerUtils.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {

            mocked.when(() -> validarTipoDeDato(dto, br)).thenReturn(true);
            mocked.when(() -> ControllerUtils.validarSumaBultosConvertida(dto, br)).thenReturn(false);

            boolean ok = ControllerUtils.getInstance().validarBultos(dto, br);

            assertFalse(ok);
            mocked.verify(() -> validarTipoDeDato(dto, br));
            mocked.verify(() -> ControllerUtils.validarSumaBultosConvertida(dto, br));
        }
    }

    @Test
    @DisplayName("Solo nroAnalisis presente → true, sin reject")
    void soloNroAnalisis() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis("A-001");
        dto.setNroReanalisis(null);
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance()
            .validarNroAnalisisNotNull(dto, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @DisplayName("Solo nroReanalisis presente → true, sin reject")
    void soloNroReanalisis() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis(null);
        dto.setNroReanalisis("R-777");
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance()
            .validarNroAnalisisNotNull(dto, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("Solo reanálisis seteada, vencimiento null => true")
    void soloReanalisisSeteada() {
        LoteDTO dto = new LoteDTO();
        dto.setFechaReanalisisProveedor(LocalDate.now());
        dto.setFechaVencimientoProveedor(null);

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance().validateFechasProveedor(dto, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("Solo vencimiento seteada, reanálisis null => true")
    void soloVencimientoSeteada() {
        LoteDTO dto = new LoteDTO();
        dto.setFechaReanalisisProveedor(null);
        dto.setFechaVencimientoProveedor(LocalDate.now().plusDays(10));

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = ControllerUtils.getInstance().validateFechasProveedor(dto, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(br);
    }

    @Test
    @DisplayName("UNIDAD con cantidad < bultosTotales -> error en bultosTotales y false")
    void unidadCantidadMenorQueBultos() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(new BigDecimal("2"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(3);

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = ControllerUtils.getInstance().validateCantidadIngreso(dto, br);

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
    @DisplayName("UNIDAD con cantidad no-entera -> error en cantidadInicial y false")
    void unidadCantidadNoEntera() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(new BigDecimal("1.5"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(1);

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = ControllerUtils.getInstance().validateCantidadIngreso(dto, br);

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
    @DisplayName("UNIDAD con cantidad entera >= bultosTotales -> true y sin errores")
    void unidadCantidadValida() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadInicial(new BigDecimal("5"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(3);

        BeanPropertyBindingResult br = new BeanPropertyBindingResult(dto, "loteDTO");

        boolean ok = ControllerUtils.getInstance().validateCantidadIngreso(dto, br);

        assertTrue(ok);
        assertFalse(br.hasErrors());
        assertEquals(0, br.getErrorCount());
    }

    @Test
    @DisplayName("UNIDAD con decimales -> rejectValue y false")
    void unidadConDecimales() {
        LoteDTO d = dto(List.of(new BigDecimal("1.50")), List.of(UnidadMedidaEnum.UNIDAD));
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = validarTipoDeDato(d, br);

        assertFalse(ok);
        verify(br).hasErrors();
        // el mensaje en el código tiene dos espacios antes de 'debe'; chequeamos con contains para evitar fragilidad
        verify(br).rejectValue(
            eq("cantidadInicial"),
            eq("error.cantidadInicial"),
            argThat(msg -> msg.contains("Bulto 1") &&
                msg.contains("debe ser un número entero positivo") &&
                msg.contains("UNIDAD"))
        );
    }

    @Test
    @DisplayName("UNIDAD entero -> true y sin errores")
    void unidadEntero() {
        LoteDTO d = dto(List.of(new BigDecimal("2")), List.of(UnidadMedidaEnum.UNIDAD));
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = validarTipoDeDato(d, br);

        assertTrue(ok);
        verify(br).hasErrors();
        verify(br, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Unidad Medida nula -> rejectValue y false")
    void unidadMedidaNula() {
        LoteDTO d = new LoteDTO();
        d.getCantidadesBultos().add(BigDecimal.ONE);
        d.setUnidadMedidaBultos(new ArrayList<>());
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        boolean ok = validarTipoDeDato(d, br);

        assertFalse(ok);
        verify(br).hasErrors();
        verify(br).rejectValue(
            eq("cantidadInicial"),
            eq("error.cantidadInicial"),
            eq("Las unidades de medida del Lote no pueden ser nulas.")
        );
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
    void validarValorTitulo() {
    }

}


