package com.mb.conitrack.service;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.PermisosCasoUsoEnum;
import com.mb.conitrack.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PermisosCasoUsoService.
 * Cobertura completa de verificación de permisos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - PermisosCasoUsoService")
class PermisosCasoUsoServiceTest {

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private PermisosCasoUsoService service;

    private User adminUser;
    private User auditorUser;
    private User qaUser;

    @BeforeEach
    void setUp() {
        // Usuario ADMIN
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        adminUser = new User("admin", "password", adminRole);
        adminUser.setId(1L);

        // Usuario AUDITOR
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        auditorRole.setId(2L);
        auditorUser = new User("auditor", "password", auditorRole);
        auditorUser.setId(2L);

        // Usuario ANALISTA_CONTROL_CALIDAD
        Role analistaRole = Role.fromEnum(RoleEnum.ANALISTA_CONTROL_CALIDAD);
        analistaRole.setId(3L);
        qaUser = new User("analista", "password", analistaRole);
        qaUser.setId(3L);
    }

    @Test
    @DisplayName("test_tienePermiso_adminTienePermiso_debe_retornarTrue")
    void test_tienePermiso_adminTienePermiso_debe_retornarTrue() {
        // Given
        when(securityContextService.getCurrentUser()).thenReturn(adminUser);

        // When
        boolean resultado = service.tienePermiso(PermisosCasoUsoEnum.CU1_INGRESO_COMPRA);

        // Then
        assertThat(resultado).isTrue();
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_tienePermiso_usuarioSinPermiso_debe_retornarFalse")
    void test_tienePermiso_usuarioSinPermiso_debe_retornarFalse() {
        // Given - Auditor no tiene permisos de compras
        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);

        // When
        boolean resultado = service.tienePermiso(PermisosCasoUsoEnum.CU1_INGRESO_COMPRA);

        // Then
        assertThat(resultado).isFalse();
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_tienePermiso_excepcion_debe_retornarFalse")
    void test_tienePermiso_excepcion_debe_retornarFalse() {
        // Given
        when(securityContextService.getCurrentUser()).thenThrow(new RuntimeException("Error"));

        // When
        boolean resultado = service.tienePermiso(PermisosCasoUsoEnum.CU1_INGRESO_COMPRA);

        // Then
        assertThat(resultado).isFalse();
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_esAdmin_usuarioAdmin_debe_retornarTrue")
    void test_esAdmin_usuarioAdmin_debe_retornarTrue() {
        // Given
        when(securityContextService.getCurrentUser()).thenReturn(adminUser);

        // When
        boolean resultado = service.esAdmin();

        // Then
        assertThat(resultado).isTrue();
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_esAdmin_usuarioNoAdmin_debe_retornarFalse")
    void test_esAdmin_usuarioNoAdmin_debe_retornarFalse() {
        // Given
        when(securityContextService.getCurrentUser()).thenReturn(qaUser);

        // When
        boolean resultado = service.esAdmin();

        // Then
        assertThat(resultado).isFalse();
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_esAdmin_excepcion_debe_retornarFalse")
    void test_esAdmin_excepcion_debe_retornarFalse() {
        // Given
        when(securityContextService.getCurrentUser()).thenThrow(new RuntimeException("Error"));

        // When
        boolean resultado = service.esAdmin();

        // Then
        assertThat(resultado).isFalse();
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_esAuditor_usuarioAuditor_debe_retornarTrue")
    void test_esAuditor_usuarioAuditor_debe_retornarTrue() {
        // Given
        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);

        // When
        boolean resultado = service.esAuditor();

        // Then
        assertThat(resultado).isTrue();
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_esAuditor_usuarioNoAuditor_debe_retornarFalse")
    void test_esAuditor_usuarioNoAuditor_debe_retornarFalse() {
        // Given
        when(securityContextService.getCurrentUser()).thenReturn(adminUser);

        // When
        boolean resultado = service.esAuditor();

        // Then
        assertThat(resultado).isFalse();
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_esAuditor_excepcion_debe_retornarFalse")
    void test_esAuditor_excepcion_debe_retornarFalse() {
        // Given
        when(securityContextService.getCurrentUser()).thenThrow(new RuntimeException("Error"));

        // When
        boolean resultado = service.esAuditor();

        // Then
        assertThat(resultado).isFalse();
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_getRolActual_debe_retornarNombreRol")
    void test_getRolActual_debe_retornarNombreRol() {
        // Given
        when(securityContextService.getCurrentUser()).thenReturn(adminUser);

        // When
        String resultado = service.getRolActual();

        // Then
        assertThat(resultado).isEqualTo("ADMIN");
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_getRolActual_excepcion_debe_retornarUNKNOWN")
    void test_getRolActual_excepcion_debe_retornarUNKNOWN() {
        // Given
        when(securityContextService.getCurrentUser()).thenThrow(new RuntimeException("Error"));

        // When
        String resultado = service.getRolActual();

        // Then
        assertThat(resultado).isEqualTo("UNKNOWN");
        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("test_getUsernameActual_debe_retornarUsername")
    void test_getUsernameActual_debe_retornarUsername() {
        // Given
        when(securityContextService.getCurrentUsername()).thenReturn("testuser");

        // When
        String resultado = service.getUsernameActual();

        // Then
        assertThat(resultado).isEqualTo("testuser");
        verify(securityContextService).getCurrentUsername();
    }

    @Test
    @DisplayName("test_getUsernameActual_excepcion_debe_retornarUnknown")
    void test_getUsernameActual_excepcion_debe_retornarUnknown() {
        // Given
        when(securityContextService.getCurrentUsername()).thenThrow(new RuntimeException("Error"));

        // When
        String resultado = service.getUsernameActual();

        // Then
        assertThat(resultado).isEqualTo("unknown");
        verify(securityContextService).getCurrentUsername();
    }
}
