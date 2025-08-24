package com.mb.conitrack.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.LocalDate;

import com.mb.conitrack.service.cu.FechaValidatorService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FechaValidatorServiceTest {

    @InjectMocks
    FechaValidatorService service;

    @Mock
    LoteService loteService;

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

}
