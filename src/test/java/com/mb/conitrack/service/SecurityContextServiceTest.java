package com.mb.conitrack.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.repository.maestro.UserRepository;

/**
 * Tests para SecurityContextService.
 * Verifica la obtención correcta del usuario autenticado desde Spring Security.
 */
@ExtendWith(MockitoExtension.class)
class SecurityContextServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityContextService service;

    private User testUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);

        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("getCurrentUser retorna usuario autenticado correctamente")
    void testGetCurrentUserSuccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User result = service.getCurrentUser();

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(adminRole, result.getRole());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("getCurrentUser lanza excepción si no hay autenticación")
    void testGetCurrentUserNoAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(SecurityException.class, () -> service.getCurrentUser());
    }

    @Test
    @DisplayName("getCurrentUser lanza excepción si usuario no está autenticado")
    void testGetCurrentUserNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(SecurityException.class, () -> service.getCurrentUser());
    }

    @Test
    @DisplayName("getCurrentUser lanza excepción si usuario no existe en BD")
    void testGetCurrentUserNotInDatabase() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        SecurityException exception = assertThrows(SecurityException.class,
            () -> service.getCurrentUser());

        assertTrue(exception.getMessage().contains("no encontrado"));
    }

    @Test
    @DisplayName("getCurrentUsername retorna username correctamente")
    void testGetCurrentUsername() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");

        String username = service.getCurrentUsername();

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("isAuthenticated retorna true si hay autenticación válida")
    void testIsAuthenticatedTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");

        boolean result = service.isAuthenticated();

        assertTrue(result);
    }

    @Test
    @DisplayName("isAuthenticated retorna false si no hay autenticación")
    void testIsAuthenticatedFalse() {
        when(securityContext.getAuthentication()).thenReturn(null);

        boolean result = service.isAuthenticated();

        assertFalse(result);
    }

    @Test
    @DisplayName("isAuthenticated retorna false para anonymousUser")
    void testIsAuthenticatedAnonymous() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        boolean result = service.isAuthenticated();

        assertFalse(result);
    }

    @Test
    @DisplayName("hasRole retorna true si usuario tiene el rol")
    void testHasRoleTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = service.hasRole("ADMIN");

        assertTrue(result);
    }

    @Test
    @DisplayName("hasRole retorna false si usuario no tiene el rol")
    void testHasRoleFalse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = service.hasRole("AUDITOR");

        assertFalse(result);
    }
}
