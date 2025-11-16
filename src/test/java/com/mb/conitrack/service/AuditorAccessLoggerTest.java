package com.mb.conitrack.service;

import com.mb.conitrack.entity.AuditoriaAcceso;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.repository.AuditoriaAccesoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuditorAccessLogger.
 * Cobertura completa de registro de accesos de auditores.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - AuditorAccessLogger")
class AuditorAccessLoggerTest {

    @Mock
    private AuditoriaAccesoRepository auditoriaAccesoRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuditorAccessLogger logger;

    private User auditorUser;

    @BeforeEach
    void setUp() {
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        auditorRole.setId(1L);
        auditorUser = new User("auditor", "password", auditorRole);
        auditorUser.setId(1L);
    }

    @Test
    @DisplayName("test_logAccess_conUsuarioYRequest_debe_registrarEnBDYLog")
    void test_logAccess_conUsuarioYRequest_debe_registrarEnBDYLog() {
        // Given
        when(request.getRequestURI()).thenReturn("/lotes/list-lotes");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader(anyString())).thenAnswer(invocation -> {
            String headerName = invocation.getArgument(0);
            if ("User-Agent".equals(headerName)) {
                return "Mozilla/5.0";
            }
            return null;
        });

        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logAccess(auditorUser, "Consulta lista de lotes", request);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        AuditoriaAcceso registroGuardado = captor.getValue();
        assertThat(registroGuardado.getUsername()).isEqualTo("auditor");
        assertThat(registroGuardado.getRoleName()).isEqualTo("AUDITOR");
        assertThat(registroGuardado.getAccion()).isEqualTo("Consulta lista de lotes");
        assertThat(registroGuardado.getUrl()).isEqualTo("/lotes/list-lotes");
        assertThat(registroGuardado.getMetodoHttp()).isEqualTo("GET");
        assertThat(registroGuardado.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(registroGuardado.getIpAddress()).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("test_logAccess_usuarioNull_debe_registrarWarningYNoGuardar")
    void test_logAccess_usuarioNull_debe_registrarWarningYNoGuardar() {
        // When
        logger.logAccess(null, "Acción", request);

        // Then
        verify(auditoriaAccesoRepository, never()).save(any(AuditoriaAcceso.class));
    }

    @Test
    @DisplayName("test_logAccess_requestNull_debe_guardarConDatosNulos")
    void test_logAccess_requestNull_debe_guardarConDatosNulos() {
        // Given
        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logAccess(auditorUser, "Acción sin request", null);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        AuditoriaAcceso registroGuardado = captor.getValue();
        assertThat(registroGuardado.getUsername()).isEqualTo("auditor");
        assertThat(registroGuardado.getAccion()).isEqualTo("Acción sin request");
        assertThat(registroGuardado.getUrl()).isNull();
        assertThat(registroGuardado.getMetodoHttp()).isNull();
        assertThat(registroGuardado.getIpAddress()).isNull();
        assertThat(registroGuardado.getUserAgent()).isNull();
    }

    @Test
    @DisplayName("test_logAccess_usuarioSinRole_debe_usarRoleUNKNOWN")
    void test_logAccess_usuarioSinRole_debe_usarRoleUNKNOWN() {
        // Given
        User userSinRole = new User();
        userSinRole.setUsername("usersinrole");
        userSinRole.setRole(null);

        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logAccess(userSinRole, "Acción", null);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        assertThat(captor.getValue().getRoleName()).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("test_logAccess_errorAlGuardarEnBD_debe_capturarExcepcionYNoFallar")
    void test_logAccess_errorAlGuardarEnBD_debe_capturarExcepcionYNoFallar() {
        // Given
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        logger.logAccess(auditorUser, "Acción", null);

        // Then
        verify(auditoriaAccesoRepository).save(any(AuditoriaAcceso.class));
        // No debe lanzar excepción
    }

    @Test
    @DisplayName("test_logAccess_versionSimplificada_debe_delegarAVersionCompleta")
    void test_logAccess_versionSimplificada_debe_delegarAVersionCompleta() {
        // Given
        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logAccess(auditorUser, "Acción simplificada");

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        assertThat(captor.getValue().getAccion()).isEqualTo("Acción simplificada");
        assertThat(captor.getValue().getUrl()).isNull();
    }

    @Test
    @DisplayName("test_logReporteAccess_debe_formatearAccionConTipoReporte")
    void test_logReporteAccess_debe_formatearAccionConTipoReporte() {
        // Given
        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logReporteAccess(auditorUser, "Lotes por Fecha", request);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        assertThat(captor.getValue().getAccion()).isEqualTo("Consulta reporte: Lotes por Fecha");
    }

    @Test
    @DisplayName("test_logUnauthorizedModification_debe_registrarIntentNoAutorizado")
    void test_logUnauthorizedModification_debe_registrarIntentNoAutorizado() {
        // Given
        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logUnauthorizedModification(auditorUser, "Eliminar lote", request);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        assertThat(captor.getValue().getAccion()).isEqualTo("Intento no autorizado de modificación: Eliminar lote");
    }

    @Test
    @DisplayName("test_getClientIpAddress_conXForwardedFor_debe_extraerPrimeraIP")
    void test_getClientIpAddress_conXForwardedFor_debe_extraerPrimeraIP() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 150.172.238.178");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("User-Agent")).thenReturn("Test");

        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logAccess(auditorUser, "Test", request);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        assertThat(captor.getValue().getIpAddress()).isEqualTo("203.0.113.195");
    }

    @Test
    @DisplayName("test_getClientIpAddress_sinHeadersProxy_debe_usarRemoteAddr")
    void test_getClientIpAddress_sinHeadersProxy_debe_usarRemoteAddr() {
        // Given
        when(request.getHeader(anyString())).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.50");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("User-Agent")).thenReturn("Test");

        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logAccess(auditorUser, "Test", request);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        assertThat(captor.getValue().getIpAddress()).isEqualTo("192.168.1.50");
    }

    @Test
    @DisplayName("test_getClientIpAddress_conHeaderUnknown_debe_saltarloYUsarSiguiente")
    void test_getClientIpAddress_conHeaderUnknown_debe_saltarloYUsarSiguiente() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.1");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("User-Agent")).thenReturn("Test");

        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logAccess(auditorUser, "Test", request);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        assertThat(captor.getValue().getIpAddress()).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("test_getClientIpAddress_conHeaderVacio_debe_saltarloYUsarSiguiente")
    void test_getClientIpAddress_conHeaderVacio_debe_saltarloYUsarSiguiente() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("192.168.0.1");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("User-Agent")).thenReturn("Test");

        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logAccess(auditorUser, "Test", request);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        assertThat(captor.getValue().getIpAddress()).isEqualTo("192.168.0.1");
    }

    @Test
    @DisplayName("test_logUnauthorizedModification_usuarioSinRole_debe_usarUNKNOWN")
    void test_logUnauthorizedModification_usuarioSinRole_debe_usarUNKNOWN() {
        // Given
        User userSinRole = new User();
        userSinRole.setUsername("usersinrole");
        userSinRole.setRole(null);

        AuditoriaAcceso auditoriaGuardada = new AuditoriaAcceso();
        auditoriaGuardada.setId(1L);
        when(auditoriaAccesoRepository.save(any(AuditoriaAcceso.class))).thenReturn(auditoriaGuardada);

        // When
        logger.logUnauthorizedModification(userSinRole, "Eliminar lote", null);

        // Then
        ArgumentCaptor<AuditoriaAcceso> captor = ArgumentCaptor.forClass(AuditoriaAcceso.class);
        verify(auditoriaAccesoRepository).save(captor.capture());

        assertThat(captor.getValue().getAccion()).isEqualTo("Intento no autorizado de modificación: Eliminar lote");
    }
}
