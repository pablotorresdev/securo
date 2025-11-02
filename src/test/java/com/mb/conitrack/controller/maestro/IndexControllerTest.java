package com.mb.conitrack.controller.maestro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IndexController Tests")
class IndexControllerTest {

    @InjectMocks
    private IndexController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("user1Page debe retornar la vista user1")
    void testUser1Page() {
        // Act
        String viewName = controller.user1Page();

        // Assert
        assertEquals("user1", viewName);
    }

    @Test
    @DisplayName("user2Page debe retornar la vista user2")
    void testUser2Page() {
        // Act
        String viewName = controller.user2Page();

        // Assert
        assertEquals("user2", viewName);
    }
}
