package com.mb.conitrack.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.exception.ReversoNotAuthorizedException;

/**
 * Tests para ReversoAuthorizationService.
 *
 * Verifica todas las reglas de autorización de reversos:
 * 1. El usuario que CREÓ el movimiento puede reversarlo
 * 2. Usuarios con nivel jerárquico SUPERIOR pueden reversar
 * 3. Usuarios con mismo nivel o inferior NO pueden reversar (excepto el creador)
 * 4. AUDITOR NUNCA puede reversar
 * 5. Movimientos legacy (sin creador): solo ADMIN puede reversar
 */
class ReversoAuthorizationServiceTest {

    private ReversoAuthorizationService service;

    // Roles
    private Role adminRole;
    private Role dtRole;
    private Role gerenteGarantiaRole;
    private Role gerenteControlRole;
    private Role supervisorRole;
    private Role analistaControlRole;
    private Role analistaPlantaRole;
    private Role auditorRole;

    // Usuarios
    private User adminUser;
    private User dtUser;
    private User gerenteGarantiaUser;
    private User gerenteControlUser;
    private User supervisorUser;
    private User analistaControlUser;
    private User analistaPlantaUser;
    private User auditorUser;

    @BeforeEach
    void setUp() {
        service = new ReversoAuthorizationService();

        // Crear roles con niveles
        adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);

        dtRole = Role.fromEnum(RoleEnum.DT);
        dtRole.setId(2L);

        gerenteGarantiaRole = Role.fromEnum(RoleEnum.GERENTE_GARANTIA_CALIDAD);
        gerenteGarantiaRole.setId(3L);

        gerenteControlRole = Role.fromEnum(RoleEnum.GERENTE_CONTROL_CALIDAD);
        gerenteControlRole.setId(4L);

        supervisorRole = Role.fromEnum(RoleEnum.SUPERVISOR_PLANTA);
        supervisorRole.setId(5L);

        analistaControlRole = Role.fromEnum(RoleEnum.ANALISTA_CONTROL_CALIDAD);
        analistaControlRole.setId(6L);

        analistaPlantaRole = Role.fromEnum(RoleEnum.ANALISTA_PLANTA);
        analistaPlantaRole.setId(7L);

        auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        auditorRole.setId(8L);

        // Crear usuarios
        adminUser = new User("admin", "pass", adminRole);
        adminUser.setId(1L);

        dtUser = new User("dt_user", "pass", dtRole);
        dtUser.setId(2L);

        gerenteGarantiaUser = new User("gerente_garantia", "pass", gerenteGarantiaRole);
        gerenteGarantiaUser.setId(3L);

        gerenteControlUser = new User("gerente_control", "pass", gerenteControlRole);
        gerenteControlUser.setId(4L);

        supervisorUser = new User("supervisor", "pass", supervisorRole);
        supervisorUser.setId(5L);

        analistaControlUser = new User("analista_control", "pass", analistaControlRole);
        analistaControlUser.setId(6L);

        analistaPlantaUser = new User("analista_planta", "pass", analistaPlantaRole);
        analistaPlantaUser.setId(7L);

        auditorUser = new User("auditor", "pass", auditorRole);
        auditorUser.setId(8L);
    }

    // ==================== REGLA 1: MISMO USUARIO ====================

    @Test
    @DisplayName("Regla 1: El creador puede reversar su propio movimiento")
    void testCreadorPuedeReversarPropio() {
        Movimiento mov = crearMovimiento(analistaControlUser);

        boolean resultado = service.puedeReversar(mov, analistaControlUser);

        assertTrue(resultado, "El creador debe poder reversar su propio movimiento");
    }

    @Test
    @DisplayName("Regla 1: El creador puede reversar aunque sea de nivel bajo")
    void testCreadorNivelBajoPuedeReversar() {
        Movimiento mov = crearMovimiento(analistaPlantaUser); // Nivel 2 (bajo)

        boolean resultado = service.puedeReversar(mov, analistaPlantaUser);

        assertTrue(resultado, "El creador siempre puede reversar, sin importar su nivel");
    }

    // ==================== REGLA 2: JERARQUÍA SUPERIOR ====================

    @Test
    @DisplayName("Regla 2: ADMIN puede reversar movimientos de cualquier nivel inferior")
    void testAdminPuedeReversarCualquierNivel() {
        Movimiento mov = crearMovimiento(analistaControlUser);

        boolean resultado = service.puedeReversar(mov, adminUser);

        assertTrue(resultado, "ADMIN debe poder reversar movimientos de niveles inferiores");
    }

    @Test
    @DisplayName("Regla 2: DT puede reversar movimientos de gerentes y analistas")
    void testDTPuedeReversarNivelesInferiores() {
        Movimiento movGerente = crearMovimiento(gerenteGarantiaUser);
        Movimiento movAnalista = crearMovimiento(analistaControlUser);

        assertTrue(service.puedeReversar(movGerente, dtUser),
            "DT debe poder reversar movimientos de gerentes");
        assertTrue(service.puedeReversar(movAnalista, dtUser),
            "DT debe poder reversar movimientos de analistas");
    }

    @Test
    @DisplayName("Regla 2: Gerente puede reversar movimientos de supervisores y analistas")
    void testGerentePuedeReversarNivelesInferiores() {
        Movimiento movSupervisor = crearMovimiento(supervisorUser);
        Movimiento movAnalista = crearMovimiento(analistaControlUser);

        assertTrue(service.puedeReversar(movSupervisor, gerenteGarantiaUser),
            "Gerente Garantía debe poder reversar movimientos de supervisores");
        assertTrue(service.puedeReversar(movAnalista, gerenteGarantiaUser),
            "Gerente Garantía debe poder reversar movimientos de analistas");
    }

    // ==================== REGLA 3: MISMO NIVEL NO PUEDE ====================

    @Test
    @DisplayName("Regla 3: Usuario de mismo nivel NO puede reversar (excepto si es creador)")
    void testMismoNivelNoPuedeReversar() {
        Movimiento mov = crearMovimiento(analistaControlUser);

        boolean resultado = service.puedeReversar(mov, analistaPlantaUser);

        assertFalse(resultado,
            "Usuario de mismo nivel NO debe poder reversar movimientos de otro usuario");
    }

    @Test
    @DisplayName("Regla 3: Gerentes de mismo nivel NO pueden reversar entre sí")
    void testGerentesMismoNivelNoPueden() {
        Movimiento movGerenteControl = crearMovimiento(gerenteControlUser);

        boolean resultado = service.puedeReversar(movGerenteControl, supervisorUser);

        assertFalse(resultado,
            "Gerente Control y Supervisor tienen mismo nivel (3), no deben poder reversar entre sí");
    }

    @Test
    @DisplayName("Regla 3: Nivel inferior NO puede reversar de nivel superior")
    void testNivelInferiorNoPuedeReversarSuperior() {
        Movimiento movGerente = crearMovimiento(gerenteGarantiaUser);

        boolean resultado = service.puedeReversar(movGerente, analistaControlUser);

        assertFalse(resultado,
            "Analista (nivel 2) NO debe poder reversar movimiento de Gerente (nivel 4)");
    }

    // ==================== REGLA 4: AUDITOR NUNCA PUEDE ====================

    @Test
    @DisplayName("Regla 4: AUDITOR NUNCA puede reversar, incluso si es su propio movimiento")
    void testAuditorNuncaPuedeReversar() {
        Movimiento movDelAuditor = crearMovimiento(auditorUser);

        boolean resultado = service.puedeReversar(movDelAuditor, auditorUser);

        assertFalse(resultado, "AUDITOR NUNCA debe poder reversar, ni siquiera sus propios movimientos");
    }

    @Test
    @DisplayName("Regla 4: AUDITOR no puede reversar movimiento de otro usuario")
    void testAuditorNoPuedeReversarOtros() {
        Movimiento mov = crearMovimiento(analistaControlUser);

        boolean resultado = service.puedeReversar(mov, auditorUser);

        assertFalse(resultado, "AUDITOR no debe poder reversar movimientos de otros");
    }

    @Test
    @DisplayName("Regla 4: validarPermisoReverso lanza excepción para AUDITOR")
    void testAuditorLanzaExcepcion() {
        Movimiento mov = crearMovimiento(analistaControlUser);

        assertThrows(ReversoNotAuthorizedException.class,
            () -> service.validarPermisoReverso(mov, auditorUser),
            "Debe lanzar ReversoNotAuthorizedException para AUDITOR");
    }

    // ==================== REGLA 5: MOVIMIENTOS LEGACY ====================

    @Test
    @DisplayName("Regla 5: Solo ADMIN puede reversar movimientos legacy (sin creador)")
    void testSoloAdminPuedeReversarLegacy() {
        Movimiento movLegacy = crearMovimientoLegacy();

        assertTrue(service.puedeReversar(movLegacy, adminUser),
            "ADMIN debe poder reversar movimientos legacy");
    }

    @Test
    @DisplayName("Regla 5: DT NO puede reversar movimientos legacy")
    void testDTNoPuedeReversarLegacy() {
        Movimiento movLegacy = crearMovimientoLegacy();

        boolean resultado = service.puedeReversar(movLegacy, dtUser);

        assertFalse(resultado, "Solo ADMIN puede reversar movimientos legacy");
    }

    @Test
    @DisplayName("Regla 5: Gerente NO puede reversar movimientos legacy")
    void testGerenteNoPuedeReversarLegacy() {
        Movimiento movLegacy = crearMovimientoLegacy();

        boolean resultado = service.puedeReversar(movLegacy, gerenteGarantiaUser);

        assertFalse(resultado, "Solo ADMIN puede reversar movimientos legacy");
    }

    @Test
    @DisplayName("Regla 5: Analista NO puede reversar movimientos legacy")
    void testAnalistaNoPuedeReversarLegacy() {
        Movimiento movLegacy = crearMovimientoLegacy();

        boolean resultado = service.puedeReversar(movLegacy, analistaControlUser);

        assertFalse(resultado, "Solo ADMIN puede reversar movimientos legacy");
    }

    @Test
    @DisplayName("Regla 5: validarPermisoReverso lanza excepción para no-ADMIN en legacy")
    void testNoAdminLanzaExcepcionEnLegacy() {
        Movimiento movLegacy = crearMovimientoLegacy();

        ReversoNotAuthorizedException exception = assertThrows(
            ReversoNotAuthorizedException.class,
            () -> service.validarPermisoReverso(movLegacy, gerenteGarantiaUser),
            "Debe lanzar excepción para no-ADMIN intentando reversar legacy");

        assertTrue(exception.getMessage().contains("legacy"),
            "El mensaje debe mencionar que es un movimiento legacy");
    }

    // ==================== TESTS DE VALIDACIÓN ====================

    @Test
    @DisplayName("validarPermisoReverso NO lanza excepción cuando tiene permiso")
    void testValidarPermisoNoLanzaExcepcionSiTienePermiso() {
        Movimiento mov = crearMovimiento(analistaControlUser);

        assertDoesNotThrow(() -> service.validarPermisoReverso(mov, analistaControlUser),
            "No debe lanzar excepción si el usuario tiene permiso");
    }

    @Test
    @DisplayName("validarPermisoReverso lanza excepción con mensaje descriptivo")
    void testValidarPermisoLanzaExcepcionConMensaje() {
        Movimiento mov = crearMovimiento(gerenteGarantiaUser);

        ReversoNotAuthorizedException exception = assertThrows(
            ReversoNotAuthorizedException.class,
            () -> service.validarPermisoReverso(mov, analistaControlUser));

        String mensaje = exception.getMessage();
        assertTrue(mensaje.contains("permisos"), "El mensaje debe mencionar permisos");
        assertTrue(mensaje.contains("creado por"), "El mensaje debe indicar quién creó el movimiento");
    }

    // ==================== TESTS DE CASOS EDGE ====================

    @Test
    @DisplayName("Edge case: null movimiento lanza NullPointerException")
    void testNullMovimientoLanzaExcepcion() {
        assertThrows(NullPointerException.class,
            () -> service.puedeReversar(null, adminUser));
    }

    @Test
    @DisplayName("Edge case: null usuario lanza NullPointerException")
    void testNullUsuarioLanzaExcepcion() {
        Movimiento mov = crearMovimiento(adminUser);

        assertThrows(NullPointerException.class,
            () -> service.puedeReversar(mov, null));
    }

    // ==================== HELPERS ====================

    private Movimiento crearMovimiento(User creador) {
        Movimiento mov = new Movimiento();
        mov.setId(1L);
        mov.setCreadoPor(creador);
        mov.setLote(new Lote());
        return mov;
    }

    private Movimiento crearMovimientoLegacy() {
        Movimiento mov = new Movimiento();
        mov.setId(2L);
        mov.setCreadoPor(null); // Sin creador = legacy
        mov.setLote(new Lote());
        return mov;
    }
}
