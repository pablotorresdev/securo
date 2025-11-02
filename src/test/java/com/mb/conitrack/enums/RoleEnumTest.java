package com.mb.conitrack.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoleEnum Tests")
class RoleEnumTest {

    @Test
    @DisplayName("Todos los roles deben existir")
    void testAllRolesExist() {
        assertEquals(8, RoleEnum.values().length);
        assertNotNull(RoleEnum.ADMIN);
        assertNotNull(RoleEnum.DT);
        assertNotNull(RoleEnum.GERENTE_GARANTIA_CALIDAD);
        assertNotNull(RoleEnum.GERENTE_CONTROL_CALIDAD);
        assertNotNull(RoleEnum.SUPERVISOR_PLANTA);
        assertNotNull(RoleEnum.ANALISTA_CONTROL_CALIDAD);
        assertNotNull(RoleEnum.ANALISTA_PLANTA);
        assertNotNull(RoleEnum.AUDITOR);
    }

    @Test
    @DisplayName("ADMIN debe tener nivel 6 y permisos completos")
    void testAdminProperties() {
        RoleEnum admin = RoleEnum.ADMIN;
        assertEquals(6, admin.getNivel());
        assertEquals("Administrador", admin.getDisplayName());
        assertTrue(admin.isCanView());
        assertTrue(admin.isCanModify());
        assertFalse(admin.isReadOnly());
    }

    @Test
    @DisplayName("DT debe tener nivel 5 y permisos de modificación")
    void testDTProperties() {
        RoleEnum dt = RoleEnum.DT;
        assertEquals(5, dt.getNivel());
        assertEquals("Director Técnico", dt.getDisplayName());
        assertTrue(dt.isCanView());
        assertTrue(dt.isCanModify());
        assertFalse(dt.isReadOnly());
    }

    @Test
    @DisplayName("GERENTE_GARANTIA_CALIDAD debe tener nivel 4")
    void testGerenteGarantiaCalidadProperties() {
        RoleEnum gerente = RoleEnum.GERENTE_GARANTIA_CALIDAD;
        assertEquals(4, gerente.getNivel());
        assertEquals("Gerente de Garantía de Calidad", gerente.getDisplayName());
        assertTrue(gerente.isCanView());
        assertTrue(gerente.isCanModify());
        assertFalse(gerente.isReadOnly());
    }

    @Test
    @DisplayName("GERENTE_CONTROL_CALIDAD debe tener nivel 3")
    void testGerenteControlCalidadProperties() {
        RoleEnum gerente = RoleEnum.GERENTE_CONTROL_CALIDAD;
        assertEquals(3, gerente.getNivel());
        assertEquals("Gerente de Control de Calidad", gerente.getDisplayName());
        assertTrue(gerente.isCanView());
        assertTrue(gerente.isCanModify());
        assertFalse(gerente.isReadOnly());
    }

    @Test
    @DisplayName("SUPERVISOR_PLANTA debe tener nivel 3")
    void testSupervisorPlantaProperties() {
        RoleEnum supervisor = RoleEnum.SUPERVISOR_PLANTA;
        assertEquals(3, supervisor.getNivel());
        assertEquals("Supervisor de Planta", supervisor.getDisplayName());
        assertTrue(supervisor.isCanView());
        assertTrue(supervisor.isCanModify());
        assertFalse(supervisor.isReadOnly());
    }

    @Test
    @DisplayName("ANALISTA_CONTROL_CALIDAD debe tener nivel 2")
    void testAnalistaControlCalidadProperties() {
        RoleEnum analista = RoleEnum.ANALISTA_CONTROL_CALIDAD;
        assertEquals(2, analista.getNivel());
        assertEquals("Analista de Control de Calidad", analista.getDisplayName());
        assertTrue(analista.isCanView());
        assertTrue(analista.isCanModify());
        assertFalse(analista.isReadOnly());
    }

    @Test
    @DisplayName("ANALISTA_PLANTA debe tener nivel 2")
    void testAnalistaPlantaProperties() {
        RoleEnum analista = RoleEnum.ANALISTA_PLANTA;
        assertEquals(2, analista.getNivel());
        assertEquals("Analista de Planta", analista.getDisplayName());
        assertTrue(analista.isCanView());
        assertTrue(analista.isCanModify());
        assertFalse(analista.isReadOnly());
    }

    @Test
    @DisplayName("AUDITOR debe tener nivel 1 y ser read-only")
    void testAuditorProperties() {
        RoleEnum auditor = RoleEnum.AUDITOR;
        assertEquals(1, auditor.getNivel());
        assertEquals("Auditor", auditor.getDisplayName());
        assertTrue(auditor.isCanView());
        assertFalse(auditor.isCanModify());
        assertTrue(auditor.isReadOnly());
    }

    @ParameterizedTest
    @CsvSource({
        "ADMIN, DT, true",
        "ADMIN, AUDITOR, true",
        "DT, ADMIN, false",
        "GERENTE_GARANTIA_CALIDAD, GERENTE_CONTROL_CALIDAD, true",
        "GERENTE_CONTROL_CALIDAD, GERENTE_GARANTIA_CALIDAD, false",
        "ANALISTA_PLANTA, AUDITOR, true",
        "AUDITOR, ANALISTA_PLANTA, false",
        "GERENTE_CONTROL_CALIDAD, SUPERVISOR_PLANTA, true" // Mismo nivel
    })
    @DisplayName("tieneNivelSuperiorOIgual debe funcionar correctamente")
    void testTieneNivelSuperiorOIgual(String rol1Name, String rol2Name, boolean expected) {
        RoleEnum rol1 = RoleEnum.valueOf(rol1Name);
        RoleEnum rol2 = RoleEnum.valueOf(rol2Name);
        assertEquals(expected, rol1.tieneNivelSuperiorOIgual(rol2));
    }

    @ParameterizedTest
    @CsvSource({
        "ADMIN, DT, true",
        "ADMIN, AUDITOR, true",
        "DT, ADMIN, false",
        "GERENTE_GARANTIA_CALIDAD, GERENTE_CONTROL_CALIDAD, true",
        "GERENTE_CONTROL_CALIDAD, GERENTE_GARANTIA_CALIDAD, false",
        "ANALISTA_PLANTA, AUDITOR, true",
        "AUDITOR, ANALISTA_PLANTA, false",
        "GERENTE_CONTROL_CALIDAD, SUPERVISOR_PLANTA, false" // Mismo nivel - debe ser false
    })
    @DisplayName("tieneNivelSuperior debe funcionar correctamente")
    void testTieneNivelSuperior(String rol1Name, String rol2Name, boolean expected) {
        RoleEnum rol1 = RoleEnum.valueOf(rol1Name);
        RoleEnum rol2 = RoleEnum.valueOf(rol2Name);
        assertEquals(expected, rol1.tieneNivelSuperior(rol2));
    }

    @Test
    @DisplayName("fromName debe retornar el rol correcto para nombre válido")
    void testFromNameValid() {
        assertEquals(RoleEnum.ADMIN, RoleEnum.fromName("ADMIN"));
        assertEquals(RoleEnum.DT, RoleEnum.fromName("DT"));
        assertEquals(RoleEnum.GERENTE_GARANTIA_CALIDAD, RoleEnum.fromName("GERENTE_GARANTIA_CALIDAD"));
        assertEquals(RoleEnum.GERENTE_CONTROL_CALIDAD, RoleEnum.fromName("GERENTE_CONTROL_CALIDAD"));
        assertEquals(RoleEnum.SUPERVISOR_PLANTA, RoleEnum.fromName("SUPERVISOR_PLANTA"));
        assertEquals(RoleEnum.ANALISTA_CONTROL_CALIDAD, RoleEnum.fromName("ANALISTA_CONTROL_CALIDAD"));
        assertEquals(RoleEnum.ANALISTA_PLANTA, RoleEnum.fromName("ANALISTA_PLANTA"));
        assertEquals(RoleEnum.AUDITOR, RoleEnum.fromName("AUDITOR"));
    }

    @Test
    @DisplayName("fromName debe lanzar excepción para nombre inválido")
    void testFromNameInvalid() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> RoleEnum.fromName("INVALID_ROLE")
        );
        assertTrue(exception.getMessage().contains("Rol no válido"));
        assertTrue(exception.getMessage().contains("INVALID_ROLE"));
    }

    @Test
    @DisplayName("ADMIN debe tener nivel superior a todos los demás roles")
    void testAdminHasHighestLevel() {
        RoleEnum admin = RoleEnum.ADMIN;
        for (RoleEnum role : RoleEnum.values()) {
            if (role != RoleEnum.ADMIN) {
                assertTrue(admin.tieneNivelSuperior(role),
                    "ADMIN debe tener nivel superior a " + role);
            }
        }
    }

    @Test
    @DisplayName("AUDITOR debe tener nivel inferior a todos los demás roles")
    void testAuditorHasLowestLevel() {
        RoleEnum auditor = RoleEnum.AUDITOR;
        for (RoleEnum role : RoleEnum.values()) {
            if (role != RoleEnum.AUDITOR) {
                assertFalse(auditor.tieneNivelSuperior(role),
                    "AUDITOR no debe tener nivel superior a " + role);
                assertFalse(auditor.tieneNivelSuperiorOIgual(role),
                    "AUDITOR no debe tener nivel superior o igual a " + role);
            }
        }
    }

    @Test
    @DisplayName("Solo AUDITOR debe ser read-only")
    void testOnlyAuditorIsReadOnly() {
        for (RoleEnum role : RoleEnum.values()) {
            if (role == RoleEnum.AUDITOR) {
                assertTrue(role.isReadOnly(), "AUDITOR debe ser read-only");
                assertFalse(role.isCanModify(), "AUDITOR no debe poder modificar");
            } else {
                assertFalse(role.isReadOnly(), role + " no debe ser read-only");
                assertTrue(role.isCanModify(), role + " debe poder modificar");
            }
        }
    }

    @Test
    @DisplayName("Todos los roles deben poder ver")
    void testAllRolesCanView() {
        for (RoleEnum role : RoleEnum.values()) {
            assertTrue(role.isCanView(), role + " debe poder ver");
        }
    }
}
