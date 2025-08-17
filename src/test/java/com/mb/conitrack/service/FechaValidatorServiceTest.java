package com.mb.conitrack.service;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FechaValidatorServiceTest {

    @Mock
    LoteService loteService;

    @InjectMocks
    FechaValidatorService service;

    @Mock
    QueryServiceLote queryServiceLote;

    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void captureStdout() {
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    /** Utilidad: lee por reflection el valor del campo privado 'hoy' para usarlo en los asserts */
    private LocalDate hoyDelServicio() {
        try {
            Field f = FechaValidatorService.class.getDeclaredField("hoy");
            f.setAccessible(true);
            return (LocalDate) f.get(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("@Scheduled presente con el cron esperado")
    void tieneScheduledConCronEsperado() throws Exception {
        Scheduled sch = FechaValidatorService.class
            .getMethod("validarFecha")
            .getAnnotation(Scheduled.class);

        String expected = "0 0 5 * * *";
        assertEquals(expected, sch.cron());
    }

    @Test
    @DisplayName("validarFecha: si ambas listas están vacías no persiste ni imprime")
    void validarFecha_listasVacias() {
        when(queryServiceLote.findAllLotesAnalisisExpirado()).thenReturn(List.of());
        when(queryServiceLote.findAllLotesVencidos()).thenReturn(List.of());

        service.validarFecha();

        verify(queryServiceLote).findAllLotesAnalisisExpirado();
        verify(queryServiceLote).findAllLotesVencidos();
        verify(loteService, never()).persistirExpiracionAnalisis(any(), anyList());
        verify(loteService, never()).persistirProductosVencidos(any(), anyList());

        assertTrue(outContent.toString().isBlank(), "No debería escribir en stdout");
    }

    @Test
    @DisplayName("validarFecha: procesa lotes de reanálisis expirado y vencidos, arma DTO y escribe en stdout")
    void validarFecha_procesaAmbos() {
        // Lotes “encontrados”
        Lote loteRe1 = mock(Lote.class);
        Lote loteRe2 = mock(Lote.class);
        Lote loteV1  = mock(Lote.class);

        when(queryServiceLote.findAllLotesAnalisisExpirado()).thenReturn(List.of(loteRe1, loteRe2));
        when(queryServiceLote.findAllLotesVencidos()).thenReturn(List.of(loteV1));

        // Lo que devuelve la persistencia (podría ser los mismos)
        when(loteService.persistirExpiracionAnalisis(any(MovimientoDTO.class), eq(List.of(loteRe1, loteRe2))))
            .thenReturn(List.of(loteRe1, loteRe2));
        when(loteService.persistirProductosVencidos(any(MovimientoDTO.class), eq(List.of(loteV1))))
            .thenReturn(List.of(loteV1));

        // Stubs mínimos para los println
        when(loteRe1.getLoteProveedor()).thenReturn("LP-RE1");
        when(loteRe1.getFechaReanalisisVigente()).thenReturn(LocalDate.of(2030, 1, 1));
        when(loteRe2.getLoteProveedor()).thenReturn("LP-RE2");
        when(loteRe2.getFechaReanalisisVigente()).thenReturn(LocalDate.of(2030, 2, 2));

        when(loteV1.getLoteProveedor()).thenReturn("LP-V1");
        when(loteV1.getFechaVencimientoVigente()).thenReturn(LocalDate.of(2031, 3, 3));

        // Ejecutar
        service.validarFecha();

        // --- verificaciones de llamadas
        verify(queryServiceLote).findAllLotesAnalisisExpirado();
        verify(queryServiceLote).findAllLotesVencidos();

        // Capturar los DTOs para verificar su contenido
        ArgumentCaptor<MovimientoDTO> dtoReCap = ArgumentCaptor.forClass(MovimientoDTO.class);
        ArgumentCaptor<MovimientoDTO> dtoVCap  = ArgumentCaptor.forClass(MovimientoDTO.class);

        verify(loteService).persistirExpiracionAnalisis(dtoReCap.capture(), eq(List.of(loteRe1, loteRe2)));
        verify(loteService).persistirProductosVencidos(dtoVCap.capture(), eq(List.of(loteV1)));

        LocalDate hoy = hoyDelServicio();

        // DTO reanálisis
        MovimientoDTO dtoRe = dtoReCap.getValue();
        assertEquals(hoy, dtoRe.getFechaMovimiento());
        assertNotNull(dtoRe.getFechaYHoraCreacion());
        assertTrue(dtoRe.getObservaciones().startsWith("(CU8) ANALISIS EXPIRADO POR FECHA: " + hoy));

        // DTO vencidos
        MovimientoDTO dtoV = dtoVCap.getValue();
        assertEquals(hoy, dtoV.getFechaMovimiento());
        assertNotNull(dtoV.getFechaYHoraCreacion());
        assertTrue(dtoV.getObservaciones().startsWith("(CU9) VENCIMIENTO AUTOMATICO POR FECHA: " + hoy));

        // --- verificaciones de salida por consola
        String out = outContent.toString();
        assertTrue(out.contains("Reanalisis expirado: LP-RE1 - 2030-01-01"));
        assertTrue(out.contains("Reanalisis expirado: LP-RE2 - 2030-02-02"));
        assertTrue(out.contains("Vencido: LP-V1 - 2031-03-03"));
    }
}
