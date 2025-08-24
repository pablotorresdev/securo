package com.mb.conitrack.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
            eq("nroAnalisis.nulo"),
            eq("Ingrese un nro de analisis")
        );
        verifyNoMoreInteractions(br);
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
    @DisplayName("populateLoteListByCodigoLote: hasErrors = true => false y no llama al service")
    void populateLoteListByCodigoLote_hasErrorsEarlyReturn() {
        List<Lote> salida = new ArrayList<>();
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);
        LoteService service = mock(LoteService.class);

        boolean ok = ControllerUtils.getInstance()
            .populateLoteListByCodigoLote(salida, "L-XYZ", br, service);

        assertFalse(ok);
        assertTrue(salida.isEmpty());
        verify(br).hasErrors();
        verifyNoInteractions(service);
        verify(br, never()).reject(anyString(), anyString());
    }

    @Test
    @DisplayName("populateLoteListByCodigoLote: service devuelve vacío => reject y false")
    void populateLoteListByCodigoLote_listaVacia() {
        List<Lote> salida = new ArrayList<>();
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        LoteService service = mock(LoteService.class);
        when(service.findLoteListByCodigoLote("L-ABC"))
            .thenReturn(new ArrayList<>());

        boolean ok = ControllerUtils.getInstance()
            .populateLoteListByCodigoLote(salida, "L-ABC", br, service);

        assertFalse(ok);
        assertTrue(salida.isEmpty());
        verify(br).hasErrors();
        verify(service).findLoteListByCodigoLote("L-ABC");
        verify(br).rejectValue(
            eq("codigoLote"),
            eq(""),
            eq("Lote no encontrado.")
        );
        verifyNoMoreInteractions(service, br);
    }

    @Test
    @DisplayName("populateLoteListByCodigoLote: lista con elementos => agrega y true")
    void populateLoteListByCodigoLote_ok() {
        List<Lote> salida = new ArrayList<>();
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        LoteService service = mock(LoteService.class);
        Lote l1 = new Lote();
        Lote l2 = new Lote();
        when(service.findLoteListByCodigoLote("L-123"))
            .thenReturn(new ArrayList<>(List.of(l1, l2)));

        boolean ok = ControllerUtils.getInstance()
            .populateLoteListByCodigoLote(salida, "L-123", br, service);

        assertTrue(ok);
        assertEquals(2, salida.size());
        assertSame(l1, salida.get(0));
        assertSame(l2, salida.get(1));
        verify(br).hasErrors();
        verify(service).findLoteListByCodigoLote("L-123");
        verify(br, never()).reject(anyString(), anyString());
        verifyNoMoreInteractions(service, br);
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

}


