package com.mb.conitrack;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.mockito.MockedStatic;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

/**
 * Test específico para el método main de ConitrackApplication.
 * Este test NO usa @SpringBootTest para evitar conflictos con el contexto ya iniciado.
 * Usa Mockito para mockear SpringApplication.run y poder ejecutar main sin iniciar la aplicación.
 */
@DisplayName("ConitrackApplication Main Method Test")
class ConitrackApplicationMainTest {

    @Test
    @DisplayName("El método main debe configurar timezone y ejecutar SpringApplication.run")
    void testMainMethod() {
        // Guardamos el timezone actual para restaurarlo después
        TimeZone originalTimeZone = TimeZone.getDefault();

        try {
            // Mockeamos SpringApplication.run para evitar iniciar la aplicación real
            try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
                // Configuramos el mock para que no haga nada al llamar a run
                springApplicationMock.when(() -> SpringApplication.run(
                    eq(ConitrackApplication.class),
                    any(String[].class)
                )).thenReturn(null);

                // Ejecutamos el método main
                ConitrackApplication.main(new String[]{});

                // Verificamos que SpringApplication.run fue llamado
                springApplicationMock.verify(() -> SpringApplication.run(
                    eq(ConitrackApplication.class),
                    any(String[].class)
                ));

                // Verificamos que el timezone se configuró correctamente
                assertEquals("America/Argentina/Buenos_Aires", TimeZone.getDefault().getID(),
                    "El método main debe establecer el timezone a America/Argentina/Buenos_Aires");
            }
        } finally {
            // Restauramos el timezone original
            TimeZone.setDefault(originalTimeZone);
        }
    }
}
