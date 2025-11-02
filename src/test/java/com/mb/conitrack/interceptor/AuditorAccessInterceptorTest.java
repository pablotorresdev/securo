package com.mb.conitrack.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.service.AuditorAccessLogger;
import com.mb.conitrack.service.SecurityContextService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuditorAccessInterceptor Tests")
class AuditorAccessInterceptorTest {

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private AuditorAccessLogger auditorAccessLogger;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuditorAccessInterceptor interceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("preHandle debe retornar true cuando usuario no está autenticado")
    void testPreHandle_NotAuthenticated() {
        // Arrange
        when(securityContextService.isAuthenticated()).thenReturn(false);

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(securityContextService, times(1)).isAuthenticated();
        verify(securityContextService, times(0)).getCurrentUser(); // No debe llamarse
        verify(auditorAccessLogger, times(0)).logAccess(any(), any(), any()); // No debe registrar
    }

    @Test
    @DisplayName("preHandle debe retornar true cuando usuario autenticado NO es auditor")
    void testPreHandle_NotAuditor() {
        // Arrange
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        User adminUser = new User("admin", "password", adminRole);

        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentUser()).thenReturn(adminUser);

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(securityContextService, times(1)).isAuthenticated();
        verify(securityContextService, times(1)).getCurrentUser();
        verify(auditorAccessLogger, times(0)).logAccess(any(), any(), any()); // No debe registrar
    }

    @Test
    @DisplayName("preHandle debe registrar acceso cuando usuario es AUDITOR con método GET")
    void testPreHandle_AuditorWithGET() {
        // Arrange
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        User auditorUser = new User("auditor", "password", auditorRole);

        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(request.getRequestURI()).thenReturn("/lotes/list-lotes");
        when(request.getMethod()).thenReturn("GET");

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(securityContextService, times(1)).isAuthenticated();
        verify(securityContextService, times(1)).getCurrentUser();
        verify(auditorAccessLogger, times(1)).logAccess(auditorUser, "GET /lotes/list-lotes", request);
        verify(auditorAccessLogger, times(0)).logUnauthorizedModification(any(), any(), any()); // GET no es modificación
    }

    @Test
    @DisplayName("preHandle debe registrar acceso Y warning cuando AUDITOR intenta POST")
    void testPreHandle_AuditorWithPOST() {
        // Arrange
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        User auditorUser = new User("auditor", "password", auditorRole);

        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(request.getRequestURI()).thenReturn("/compras/alta/ingreso-compra");
        when(request.getMethod()).thenReturn("POST");

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(auditorAccessLogger, times(1)).logAccess(auditorUser, "POST /compras/alta/ingreso-compra", request);
        verify(auditorAccessLogger, times(1)).logUnauthorizedModification(
            auditorUser,
            "/compras/alta/ingreso-compra",
            request
        );
    }

    @Test
    @DisplayName("preHandle debe registrar acceso Y warning cuando AUDITOR intenta PUT")
    void testPreHandle_AuditorWithPUT() {
        // Arrange
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        User auditorUser = new User("auditor", "password", auditorRole);

        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(request.getRequestURI()).thenReturn("/productos/update/1");
        when(request.getMethod()).thenReturn("PUT");

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(auditorAccessLogger, times(1)).logAccess(auditorUser, "PUT /productos/update/1", request);
        verify(auditorAccessLogger, times(1)).logUnauthorizedModification(
            auditorUser,
            "/productos/update/1",
            request
        );
    }

    @Test
    @DisplayName("preHandle debe registrar acceso Y warning cuando AUDITOR intenta DELETE")
    void testPreHandle_AuditorWithDELETE() {
        // Arrange
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        User auditorUser = new User("auditor", "password", auditorRole);

        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(request.getRequestURI()).thenReturn("/productos/delete/1");
        when(request.getMethod()).thenReturn("DELETE");

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(auditorAccessLogger, times(1)).logAccess(auditorUser, "DELETE /productos/delete/1", request);
        verify(auditorAccessLogger, times(1)).logUnauthorizedModification(
            auditorUser,
            "/productos/delete/1",
            request
        );
    }

    @Test
    @DisplayName("preHandle debe registrar acceso Y warning cuando AUDITOR intenta PATCH")
    void testPreHandle_AuditorWithPATCH() {
        // Arrange
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        User auditorUser = new User("auditor", "password", auditorRole);

        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(request.getRequestURI()).thenReturn("/lotes/patch/1");
        when(request.getMethod()).thenReturn("PATCH");

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(auditorAccessLogger, times(1)).logAccess(auditorUser, "PATCH /lotes/patch/1", request);
        verify(auditorAccessLogger, times(1)).logUnauthorizedModification(
            auditorUser,
            "/lotes/patch/1",
            request
        );
    }

    @Test
    @DisplayName("preHandle debe retornar true incluso si ocurre excepción")
    void testPreHandle_ExceptionThrown() {
        // Arrange
        when(securityContextService.isAuthenticated()).thenThrow(new RuntimeException("Test exception"));

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result); // Debe continuar con la petición aunque haya error
        verify(auditorAccessLogger, times(0)).logAccess(any(), any(), any()); // No debe registrar
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "post", "Post", "PUT", "put", "Put", "DELETE", "delete", "Delete", "PATCH", "patch", "Patch"})
    @DisplayName("isModificationRequest debe retornar true para métodos de modificación")
    void testIsModificationRequest_ModificationMethods(String method) {
        // Act
        boolean result = interceptor.isModificationRequest(method);

        // Assert
        assertTrue(result, "Método " + method + " debe ser considerado de modificación");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "get", "Get", "HEAD", "head", "OPTIONS", "options", "TRACE", "trace"})
    @DisplayName("isModificationRequest debe retornar false para métodos de solo lectura")
    void testIsModificationRequest_ReadOnlyMethods(String method) {
        // Act
        boolean result = interceptor.isModificationRequest(method);

        // Assert
        assertFalse(result, "Método " + method + " NO debe ser considerado de modificación");
    }

    @Test
    @DisplayName("isModificationRequest debe retornar false para null")
    void testIsModificationRequest_Null() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            boolean result = interceptor.isModificationRequest(null);
            assertFalse(result);
        });
    }

    @Test
    @DisplayName("isModificationRequest debe retornar false para string vacío")
    void testIsModificationRequest_EmptyString() {
        // Act
        boolean result = interceptor.isModificationRequest("");

        // Assert
        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({
        "GET, /lotes/list-lotes, false",
        "POST, /compras/alta/ingreso-compra, true",
        "PUT, /productos/update/1, true",
        "DELETE, /productos/delete/1, true",
        "PATCH, /lotes/patch/1, true"
    })
    @DisplayName("Integración: verificar comportamiento completo del interceptor con diferentes métodos")
    void testPreHandle_Integration(String method, String uri, boolean shouldLogModification) {
        // Arrange
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        User auditorUser = new User("auditor", "password", auditorRole);

        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getMethod()).thenReturn(method);

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(auditorAccessLogger, times(1)).logAccess(auditorUser, method + " " + uri, request);

        if (shouldLogModification) {
            verify(auditorAccessLogger, times(1)).logUnauthorizedModification(auditorUser, uri, request);
        } else {
            verify(auditorAccessLogger, times(0)).logUnauthorizedModification(any(), any(), any());
        }
    }
}
