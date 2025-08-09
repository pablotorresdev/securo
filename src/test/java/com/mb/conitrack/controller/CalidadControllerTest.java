package com.mb.conitrack.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.dto.LoteDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class CalidadControllerTest {

    @Spy
    @InjectMocks
    CalidadController controller;

    LoteDTO dto;

    @Test
    @DisplayName("cancel")
    void cancel() {
        final String s = controller.cancelar();
        assertEquals("redirect:/", s);
    }

    @Test
    @DisplayName("exitoDictamenCuarentena")
    void exitoDictamenCuarentena() {
        final String s = controller.exitoDictamenCuarentena(dto);
        assertEquals("calidad/cuarentena-ok", s);
    }

    @Test
    void exitoMuestreo() {
    }

    @Test
    void exitoReanalisisProducto() {
    }

    @Test
    void exitoResultadoAnalisis() {
    }

    @Test
    void procesarDictamenCuarentena() {
    }

    @Test
    void procesarMuestreoBulto() {
    }

    @Test
    void procesarReanalisisProducto() {
    }

    @Test
    void procesarResultadoAnalisis() {
    }

    @BeforeEach
    void setUp() {
        openMocks(this);   // inicializa @Mock y @InjectMocks
        dto = new LoteDTO();
    }

    @Test
    void showDictamenCuarentenaForm() {
    }

    @Test
    void showMuestreoBultoForm() {
    }

    @Test
    void showReanalisisProductoForm() {
    }

    @Test
    void showResultadoAnalisisForm() {
    }

}