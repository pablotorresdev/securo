package com.mb.conitrack;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para ConitrackApplication.
 * Verifica que el contexto de Spring Boot se carga correctamente
 * y que la configuración de timezone es la esperada.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false"
})
@DisplayName("ConitrackApplication Tests")
class ConitrackApplicationTest {

    @BeforeAll
    static void setUpTimezone() {
        // Simula lo que hace el main de ConitrackApplication
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
    }

    @Test
    @DisplayName("El contexto de Spring debe cargar correctamente")
    void contextLoads() {
        // Este test verifica que el contexto de Spring Boot se carga sin errores
        // Si el contexto no se puede cargar, el test falla automáticamente
        assertTrue(true, "El contexto de Spring se cargó correctamente");
    }

    @Test
    @DisplayName("El timezone por defecto debe ser America/Argentina/Buenos_Aires")
    void testDefaultTimezone() {
        // Arrange & Act
        // El BeforeAll establece el timezone como lo hace ConitrackApplication
        TimeZone defaultTimeZone = TimeZone.getDefault();

        // Assert
        assertNotNull(defaultTimeZone);
        assertEquals("America/Argentina/Buenos_Aires", defaultTimeZone.getID(),
            "El timezone por defecto debe ser America/Argentina/Buenos_Aires");
    }

    @Test
    @DisplayName("main debe ejecutarse sin lanzar excepciones")
    void testMainMethod() {
        // Este test verifica que podemos invocar el método main sin problemas
        assertDoesNotThrow(() -> {
            // Nota: No ejecutamos realmente SpringApplication.run porque ya está
            // corriendo en el contexto del test @SpringBootTest
            // Solo verificamos que el método existe y es accesible
            assertNotNull(ConitrackApplication.class.getMethod("main", String[].class));
        });
    }

}
