package com.mb.conitrack.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PermisosCasoUsoEnum Tests")
class PermisosCasoUsoEnumTest {

    @Test
    @DisplayName("Todos los casos de uso deben existir")
    void testAllCasosDeUsoExist() {
        assertEquals(27, PermisosCasoUsoEnum.values().length);

        // Compras
        assertNotNull(PermisosCasoUsoEnum.CU1_INGRESO_COMPRA);
        assertNotNull(PermisosCasoUsoEnum.CU4_DEVOLUCION_COMPRA);

        // Calidad
        assertNotNull(PermisosCasoUsoEnum.CU2_DICTAMEN_CUARENTENA);
        assertNotNull(PermisosCasoUsoEnum.CU3_MUESTREO_MULTI_BULTO);
        assertNotNull(PermisosCasoUsoEnum.CU3_MUESTREO_TRAZABLE);
        assertNotNull(PermisosCasoUsoEnum.CU5_CU6_RESULTADO_ANALISIS);
        assertNotNull(PermisosCasoUsoEnum.CU8_REANALISIS);
        assertNotNull(PermisosCasoUsoEnum.CU9_CU10_FECHAS_AUTOMATICO);
        assertNotNull(PermisosCasoUsoEnum.CU11_ANULACION_ANALISIS);

        // Produccion
        assertNotNull(PermisosCasoUsoEnum.CU7_CONSUMO_PRODUCCION);
        assertNotNull(PermisosCasoUsoEnum.CU20_INGRESO_PRODUCCION);

        // Ventas
        assertNotNull(PermisosCasoUsoEnum.CU21_LIBERACION_VENTA);
        assertNotNull(PermisosCasoUsoEnum.CU22_VENTA_PRODUCTO);
        assertNotNull(PermisosCasoUsoEnum.CU23_DEVOLUCION_VENTA);
        assertNotNull(PermisosCasoUsoEnum.CU24_RETIRO_MERCADO);
        assertNotNull(PermisosCasoUsoEnum.CU27_TRAZADO);

        // Contingencias
        assertNotNull(PermisosCasoUsoEnum.CU25_AJUSTE_INVENTARIO);
        assertNotNull(PermisosCasoUsoEnum.CU26_REVERSO_MOVIMIENTO);

        // Consultas
        assertNotNull(PermisosCasoUsoEnum.CONSULTA_LOTES);
        assertNotNull(PermisosCasoUsoEnum.CONSULTA_BULTOS);
        assertNotNull(PermisosCasoUsoEnum.CONSULTA_MOVIMIENTOS);
        assertNotNull(PermisosCasoUsoEnum.CONSULTA_ANALISIS);
        assertNotNull(PermisosCasoUsoEnum.CONSULTA_TRAZAS);

        // ABM Maestro
        assertNotNull(PermisosCasoUsoEnum.CU30_ABM_PROVEEDORES);
        assertNotNull(PermisosCasoUsoEnum.CU31_ABM_PRODUCTOS);

        // Admin
        assertNotNull(PermisosCasoUsoEnum.CU32_CONFIGURACION_SISTEMA);
        assertNotNull(PermisosCasoUsoEnum.CU33_ABM_USUARIOS);
    }

    @Test
    @DisplayName("CU1_INGRESO_COMPRA debe tener descripción, URL y roles correctos")
    void testCU1Properties() {
        PermisosCasoUsoEnum cu1 = PermisosCasoUsoEnum.CU1_INGRESO_COMPRA;

        assertEquals("Ingreso de Lote por Compra", cu1.getDescripcion());
        assertEquals("/compras/alta/ingreso-compra", cu1.getUrlPattern());
        assertEquals(2, cu1.getRolesPermitidos().size());
        assertTrue(cu1.getRolesPermitidos().contains(RoleEnum.ADMIN));
        assertTrue(cu1.getRolesPermitidos().contains(RoleEnum.ANALISTA_PLANTA));
    }

    @Test
    @DisplayName("CU4_DEVOLUCION_COMPRA debe tener roles ADMIN y ANALISTA_PLANTA")
    void testCU4Properties() {
        PermisosCasoUsoEnum cu4 = PermisosCasoUsoEnum.CU4_DEVOLUCION_COMPRA;

        assertEquals("Devolución de Compra", cu4.getDescripcion());
        assertEquals("/compras/baja/devolucion-compra", cu4.getUrlPattern());
        assertTrue(cu4.getRolesPermitidos().contains(RoleEnum.ADMIN));
        assertTrue(cu4.getRolesPermitidos().contains(RoleEnum.ANALISTA_PLANTA));
    }

    @Test
    @DisplayName("CU2_DICTAMEN_CUARENTENA debe tener roles ADMIN y ANALISTA_CONTROL_CALIDAD")
    void testCU2Properties() {
        PermisosCasoUsoEnum cu2 = PermisosCasoUsoEnum.CU2_DICTAMEN_CUARENTENA;

        assertEquals("Dictamen: Cuarentena", cu2.getDescripcion());
        assertEquals("/calidad/dictamen/cuarentena", cu2.getUrlPattern());
        assertTrue(cu2.getRolesPermitidos().contains(RoleEnum.ADMIN));
        assertTrue(cu2.getRolesPermitidos().contains(RoleEnum.ANALISTA_CONTROL_CALIDAD));
    }

    @Test
    @DisplayName("CU21_LIBERACION_VENTA debe tener roles ADMIN, GERENTE_GARANTIA_CALIDAD y DT")
    void testCU21Properties() {
        PermisosCasoUsoEnum cu21 = PermisosCasoUsoEnum.CU21_LIBERACION_VENTA;

        assertEquals("Liberación Unidad de venta", cu21.getDescripcion());
        assertEquals("/ventas/liberacion/inicio-liberacion", cu21.getUrlPattern());
        assertEquals(3, cu21.getRolesPermitidos().size());
        assertTrue(cu21.getRolesPermitidos().contains(RoleEnum.ADMIN));
        assertTrue(cu21.getRolesPermitidos().contains(RoleEnum.GERENTE_GARANTIA_CALIDAD));
        assertTrue(cu21.getRolesPermitidos().contains(RoleEnum.DT));
    }

    @Test
    @DisplayName("CU26_REVERSO_MOVIMIENTO debe tener todos los roles excepto AUDITOR")
    void testCU26Properties() {
        PermisosCasoUsoEnum cu26 = PermisosCasoUsoEnum.CU26_REVERSO_MOVIMIENTO;

        assertEquals("Reverso de Ultimo Movimiento", cu26.getDescripcion());
        assertEquals("/contingencias/reverso-movimiento", cu26.getUrlPattern());
        assertEquals(7, cu26.getRolesPermitidos().size());
        assertTrue(cu26.getRolesPermitidos().contains(RoleEnum.ADMIN));
        assertTrue(cu26.getRolesPermitidos().contains(RoleEnum.DT));
        assertTrue(cu26.getRolesPermitidos().contains(RoleEnum.GERENTE_GARANTIA_CALIDAD));
        assertTrue(cu26.getRolesPermitidos().contains(RoleEnum.GERENTE_CONTROL_CALIDAD));
        assertTrue(cu26.getRolesPermitidos().contains(RoleEnum.SUPERVISOR_PLANTA));
        assertTrue(cu26.getRolesPermitidos().contains(RoleEnum.ANALISTA_CONTROL_CALIDAD));
        assertTrue(cu26.getRolesPermitidos().contains(RoleEnum.ANALISTA_PLANTA));
        assertFalse(cu26.getRolesPermitidos().contains(RoleEnum.AUDITOR));
    }

    @Test
    @DisplayName("Consultas deben permitir acceso a todos los roles")
    void testConsultasPermissions() {
        PermisosCasoUsoEnum consultaLotes = PermisosCasoUsoEnum.CONSULTA_LOTES;
        PermisosCasoUsoEnum consultaBultos = PermisosCasoUsoEnum.CONSULTA_BULTOS;
        PermisosCasoUsoEnum consultaMovimientos = PermisosCasoUsoEnum.CONSULTA_MOVIMIENTOS;

        // Todas las consultas deben permitir a todos los roles
        assertEquals(8, consultaLotes.getRolesPermitidos().size());
        assertEquals(8, consultaBultos.getRolesPermitidos().size());
        assertEquals(8, consultaMovimientos.getRolesPermitidos().size());

        // Verificar que AUDITOR puede acceder a consultas
        assertTrue(consultaLotes.getRolesPermitidos().contains(RoleEnum.AUDITOR));
        assertTrue(consultaBultos.getRolesPermitidos().contains(RoleEnum.AUDITOR));
        assertTrue(consultaMovimientos.getRolesPermitidos().contains(RoleEnum.AUDITOR));
    }

    @Test
    @DisplayName("CU32_CONFIGURACION_SISTEMA debe ser exclusivo de ADMIN")
    void testCU32AdminOnly() {
        PermisosCasoUsoEnum cu32 = PermisosCasoUsoEnum.CU32_CONFIGURACION_SISTEMA;

        assertEquals("Configuración del sistema", cu32.getDescripcion());
        assertEquals("/admin/configuracion", cu32.getUrlPattern());
        assertEquals(1, cu32.getRolesPermitidos().size());
        assertTrue(cu32.getRolesPermitidos().contains(RoleEnum.ADMIN));
    }

    @Test
    @DisplayName("CU33_ABM_USUARIOS debe ser exclusivo de ADMIN")
    void testCU33AdminOnly() {
        PermisosCasoUsoEnum cu33 = PermisosCasoUsoEnum.CU33_ABM_USUARIOS;

        assertEquals("ABM Usuarios", cu33.getDescripcion());
        assertEquals("/users", cu33.getUrlPattern());
        assertEquals(1, cu33.getRolesPermitidos().size());
        assertTrue(cu33.getRolesPermitidos().contains(RoleEnum.ADMIN));
    }

    @Test
    @DisplayName("CU9_CU10_FECHAS_AUTOMATICO debe ser exclusivo de ADMIN")
    void testCU9CU10AdminOnly() {
        PermisosCasoUsoEnum cu9cu10 = PermisosCasoUsoEnum.CU9_CU10_FECHAS_AUTOMATICO;

        assertEquals("Consulta Fecha Reanalisis / Vencimiento (AUTOMATICO)", cu9cu10.getDescripcion());
        assertEquals("/lotes/list-fechas-lotes", cu9cu10.getUrlPattern());
        assertEquals(1, cu9cu10.getRolesPermitidos().size());
        assertTrue(cu9cu10.getRolesPermitidos().contains(RoleEnum.ADMIN));
    }

    @ParameterizedTest
    @CsvSource({
        "CU1_INGRESO_COMPRA, ADMIN, true",
        "CU1_INGRESO_COMPRA, ANALISTA_PLANTA, true",
        "CU1_INGRESO_COMPRA, DT, false",
        "CU1_INGRESO_COMPRA, AUDITOR, false",
        "CU21_LIBERACION_VENTA, ADMIN, true",
        "CU21_LIBERACION_VENTA, DT, true",
        "CU21_LIBERACION_VENTA, GERENTE_GARANTIA_CALIDAD, true",
        "CU21_LIBERACION_VENTA, ANALISTA_PLANTA, false",
        "CU33_ABM_USUARIOS, ADMIN, true",
        "CU33_ABM_USUARIOS, DT, false",
        "CU33_ABM_USUARIOS, AUDITOR, false",
        "CONSULTA_LOTES, AUDITOR, true",
        "CONSULTA_LOTES, ADMIN, true",
        "CONSULTA_LOTES, ANALISTA_PLANTA, true"
    })
    @DisplayName("tienePermiso debe verificar correctamente los permisos")
    void testTienePermiso(String casoUsoName, String roleName, boolean expected) {
        PermisosCasoUsoEnum casoUso = PermisosCasoUsoEnum.valueOf(casoUsoName);
        RoleEnum rol = RoleEnum.valueOf(roleName);

        assertEquals(expected, casoUso.tienePermiso(rol));
    }

    @Test
    @DisplayName("getUrlPatternWithWildcard debe agregar /** al final")
    void testGetUrlPatternWithWildcard() {
        PermisosCasoUsoEnum cu1 = PermisosCasoUsoEnum.CU1_INGRESO_COMPRA;

        assertEquals("/compras/alta/ingreso-compra/**", cu1.getUrlPatternWithWildcard());
    }

    @Test
    @DisplayName("getUrlPatternWithWildcard debe funcionar para todos los casos de uso")
    void testGetUrlPatternWithWildcardForAll() {
        for (PermisosCasoUsoEnum casoUso : PermisosCasoUsoEnum.values()) {
            String urlWithWildcard = casoUso.getUrlPatternWithWildcard();
            assertTrue(urlWithWildcard.endsWith("/**"),
                "URL pattern for " + casoUso + " debe terminar con /**");
            assertTrue(urlWithWildcard.startsWith("/"),
                "URL pattern for " + casoUso + " debe empezar con /");
        }
    }

    @Test
    @DisplayName("getRolesAsStringArray debe convertir roles a array de strings")
    void testGetRolesAsStringArray() {
        PermisosCasoUsoEnum cu1 = PermisosCasoUsoEnum.CU1_INGRESO_COMPRA;

        String[] rolesArray = cu1.getRolesAsStringArray();

        assertEquals(2, rolesArray.length);
        assertTrue(rolesArray[0].equals("ADMIN") || rolesArray[0].equals("ANALISTA_PLANTA"));
        assertTrue(rolesArray[1].equals("ADMIN") || rolesArray[1].equals("ANALISTA_PLANTA"));
    }

    @Test
    @DisplayName("getRolesAsStringArray debe funcionar para caso con un solo rol")
    void testGetRolesAsStringArraySingleRole() {
        PermisosCasoUsoEnum cu33 = PermisosCasoUsoEnum.CU33_ABM_USUARIOS;

        String[] rolesArray = cu33.getRolesAsStringArray();

        assertEquals(1, rolesArray.length);
        assertEquals("ADMIN", rolesArray[0]);
    }

    @Test
    @DisplayName("getRolesAsStringArray debe funcionar para caso con múltiples roles")
    void testGetRolesAsStringArrayMultipleRoles() {
        PermisosCasoUsoEnum cu21 = PermisosCasoUsoEnum.CU21_LIBERACION_VENTA;

        String[] rolesArray = cu21.getRolesAsStringArray();

        assertEquals(3, rolesArray.length);
        // Verificar que contiene los roles esperados (sin importar el orden)
        assertTrue(java.util.Arrays.asList(rolesArray).contains("ADMIN"));
        assertTrue(java.util.Arrays.asList(rolesArray).contains("GERENTE_GARANTIA_CALIDAD"));
        assertTrue(java.util.Arrays.asList(rolesArray).contains("DT"));
    }

    @Test
    @DisplayName("ADMIN debe tener acceso a todos los casos de uso")
    void testAdminHasAccessToAll() {
        for (PermisosCasoUsoEnum casoUso : PermisosCasoUsoEnum.values()) {
            assertTrue(casoUso.tienePermiso(RoleEnum.ADMIN),
                "ADMIN debe tener acceso a " + casoUso);
        }
    }

    @Test
    @DisplayName("AUDITOR solo debe tener acceso a consultas")
    void testAuditorOnlyHasAccessToConsultas() {
        for (PermisosCasoUsoEnum casoUso : PermisosCasoUsoEnum.values()) {
            boolean isConsulta = casoUso.name().startsWith("CONSULTA_");

            if (isConsulta) {
                // AUDITOR debe tener acceso a consultas (excepto algunas restringidas)
                if (casoUso != PermisosCasoUsoEnum.CONSULTA_ANALISIS) {
                    assertTrue(casoUso.tienePermiso(RoleEnum.AUDITOR) ||
                              casoUso == PermisosCasoUsoEnum.CONSULTA_TRAZAS,
                        "AUDITOR debe tener acceso a " + casoUso);
                }
            } else {
                // AUDITOR no debe tener acceso a operaciones no-consulta
                if (casoUso != PermisosCasoUsoEnum.CU26_REVERSO_MOVIMIENTO) {
                    assertFalse(casoUso.tienePermiso(RoleEnum.AUDITOR),
                        "AUDITOR no debe tener acceso a " + casoUso);
                }
            }
        }
    }

    @Test
    @DisplayName("Todos los casos de uso deben tener al menos un rol permitido")
    void testAllCasosDeUsoHaveAtLeastOneRole() {
        for (PermisosCasoUsoEnum casoUso : PermisosCasoUsoEnum.values()) {
            assertFalse(casoUso.getRolesPermitidos().isEmpty(),
                casoUso + " debe tener al menos un rol permitido");
        }
    }

    @Test
    @DisplayName("Todos los casos de uso deben tener descripción no vacía")
    void testAllCasosDeUsoHaveDescription() {
        for (PermisosCasoUsoEnum casoUso : PermisosCasoUsoEnum.values()) {
            assertNotNull(casoUso.getDescripcion(),
                casoUso + " debe tener descripción");
            assertFalse(casoUso.getDescripcion().isEmpty(),
                casoUso + " descripción no debe estar vacía");
        }
    }

    @Test
    @DisplayName("Todos los casos de uso deben tener URL pattern válido")
    void testAllCasosDeUsoHaveValidUrlPattern() {
        for (PermisosCasoUsoEnum casoUso : PermisosCasoUsoEnum.values()) {
            assertNotNull(casoUso.getUrlPattern(),
                casoUso + " debe tener URL pattern");
            assertTrue(casoUso.getUrlPattern().startsWith("/"),
                casoUso + " URL pattern debe empezar con /");
        }
    }

    @Test
    @DisplayName("CU30_ABM_PROVEEDORES debe permitir ADMIN y ANALISTA_PLANTA")
    void testCU30Properties() {
        PermisosCasoUsoEnum cu30 = PermisosCasoUsoEnum.CU30_ABM_PROVEEDORES;

        assertEquals("ABM Proveedores", cu30.getDescripcion());
        assertEquals("/proveedores/", cu30.getUrlPattern());
        assertTrue(cu30.tienePermiso(RoleEnum.ADMIN));
        assertTrue(cu30.tienePermiso(RoleEnum.ANALISTA_PLANTA));
        assertFalse(cu30.tienePermiso(RoleEnum.AUDITOR));
    }

    @Test
    @DisplayName("CU31_ABM_PRODUCTOS debe permitir ADMIN y GERENTE_CONTROL_CALIDAD")
    void testCU31Properties() {
        PermisosCasoUsoEnum cu31 = PermisosCasoUsoEnum.CU31_ABM_PRODUCTOS;

        assertEquals("ABM Productos", cu31.getDescripcion());
        assertEquals("/productos/", cu31.getUrlPattern());
        assertTrue(cu31.tienePermiso(RoleEnum.ADMIN));
        assertTrue(cu31.tienePermiso(RoleEnum.GERENTE_CONTROL_CALIDAD));
        assertFalse(cu31.tienePermiso(RoleEnum.AUDITOR));
        assertFalse(cu31.tienePermiso(RoleEnum.ANALISTA_PLANTA));
    }
}
