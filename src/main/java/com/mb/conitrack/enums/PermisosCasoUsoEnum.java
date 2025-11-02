package com.mb.conitrack.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Define los permisos de acceso a cada caso de uso según el rol del usuario.
 * Basado en la documentación del sistema y requerimientos de negocio.
 */
public enum PermisosCasoUsoEnum {

    // ==================== COMPRAS ====================
    CU1_INGRESO_COMPRA(
        "Ingreso de Lote por Compra",
        "/compras/alta/ingreso-compra",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.ANALISTA_PLANTA)
    ),
    CU4_DEVOLUCION_COMPRA(
        "Devolución de Compra",
        "/compras/baja/devolucion-compra",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.ANALISTA_PLANTA)
    ),

    // ==================== CALIDAD ====================
    CU2_DICTAMEN_CUARENTENA(
        "Dictamen: Cuarentena",
        "/calidad/dictamen/cuarentena",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.ANALISTA_CONTROL_CALIDAD)
    ),
    CU3_MUESTREO_MULTI_BULTO(
        "Retiro por Muestreo Multi Bulto",
        "/calidad/baja/muestreo-multi-bulto",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.ANALISTA_CONTROL_CALIDAD)
    ),
    CU3_MUESTREO_TRAZABLE(
        "Retiro por Muestreo Trazable",
        "/calidad/baja/muestreo-trazable",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.ANALISTA_CONTROL_CALIDAD)
    ),
    CU5_CU6_RESULTADO_ANALISIS(
        "Resultado QA: Aprobado/Rechazado",
        "/calidad/analisis/resultado-analisis",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.GERENTE_CONTROL_CALIDAD)
    ),
    CU8_REANALISIS(
        "Reanalisis de Producto Aprobado",
        "/calidad/reanalisis/inicio-reanalisis",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.GERENTE_CONTROL_CALIDAD)
    ),
    CU9_CU10_FECHAS_AUTOMATICO(
        "Consulta Fecha Reanalisis / Vencimiento (AUTOMATICO)",
        "/lotes/list-fechas-lotes",
        Arrays.asList(RoleEnum.ADMIN) // Solo admin puede ver el proceso automático
    ),
    CU11_ANULACION_ANALISIS(
        "Anulacion analisis",
        "/calidad/anulacion/anulacion-analisis",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.GERENTE_CONTROL_CALIDAD)
    ),

    // ==================== PRODUCCION ====================
    CU7_CONSUMO_PRODUCCION(
        "Consumo de Producción",
        "/produccion/baja/consumo-produccion",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.SUPERVISOR_PLANTA)
    ),
    CU20_INGRESO_PRODUCCION(
        "Ingreso de stock por Producción Interna",
        "/produccion/alta/ingreso-produccion",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.SUPERVISOR_PLANTA)
    ),

    // ==================== VENTAS ====================
    CU21_LIBERACION_VENTA(
        "Liberación Unidad de venta",
        "/ventas/liberacion/inicio-liberacion",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.GERENTE_GARANTIA_CALIDAD, RoleEnum.DT)
    ),
    CU22_VENTA_PRODUCTO(
        "Venta de Producto Propio",
        "/ventas/baja/venta-producto",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.ANALISTA_PLANTA)
    ),
    CU23_DEVOLUCION_VENTA(
        "Devolución de cliente",
        "/ventas/alta/devolucion-venta",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.GERENTE_GARANTIA_CALIDAD)
    ),
    CU24_RETIRO_MERCADO(
        "Retiro de mercado",
        "/ventas/recall/retiro-mercado",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.GERENTE_GARANTIA_CALIDAD)
    ),
    CU27_TRAZADO(
        "Trazado Unidad de venta",
        "/ventas/trazado/inicio-trazado",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.GERENTE_GARANTIA_CALIDAD, RoleEnum.DT)
    ),

    // ==================== CONTINGENCIAS ====================
    CU25_AJUSTE_INVENTARIO(
        "Ajuste de Inventario",
        "/contingencias/ajuste-stock",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.SUPERVISOR_PLANTA)
    ),
    CU26_REVERSO_MOVIMIENTO(
        "Reverso de Ultimo Movimiento",
        "/contingencias/reverso-movimiento",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.DT, RoleEnum.GERENTE_GARANTIA_CALIDAD,
                     RoleEnum.GERENTE_CONTROL_CALIDAD, RoleEnum.SUPERVISOR_PLANTA,
                     RoleEnum.ANALISTA_CONTROL_CALIDAD, RoleEnum.ANALISTA_PLANTA)
        // Nota: El reverso tiene validación adicional en ReversoAuthorizationService
    ),

    // ==================== CONSULTAS ====================
    CONSULTA_LOTES(
        "Lista de Lotes",
        "/lotes/list-lotes",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.DT, RoleEnum.GERENTE_GARANTIA_CALIDAD,
                     RoleEnum.GERENTE_CONTROL_CALIDAD, RoleEnum.SUPERVISOR_PLANTA,
                     RoleEnum.ANALISTA_CONTROL_CALIDAD, RoleEnum.ANALISTA_PLANTA, RoleEnum.AUDITOR)
    ),
    CONSULTA_BULTOS(
        "Lista de Bultos",
        "/bultos/list-bultos",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.DT, RoleEnum.GERENTE_GARANTIA_CALIDAD,
                     RoleEnum.GERENTE_CONTROL_CALIDAD, RoleEnum.SUPERVISOR_PLANTA,
                     RoleEnum.ANALISTA_CONTROL_CALIDAD, RoleEnum.ANALISTA_PLANTA, RoleEnum.AUDITOR)
    ),
    CONSULTA_MOVIMIENTOS(
        "Lista de Movimientos",
        "/movimientos/list-movimientos",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.DT, RoleEnum.GERENTE_GARANTIA_CALIDAD,
                     RoleEnum.GERENTE_CONTROL_CALIDAD, RoleEnum.SUPERVISOR_PLANTA,
                     RoleEnum.ANALISTA_CONTROL_CALIDAD, RoleEnum.ANALISTA_PLANTA, RoleEnum.AUDITOR)
    ),
    CONSULTA_ANALISIS(
        "Lista de Analisis",
        "/analisis/list-analisis",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.DT, RoleEnum.GERENTE_GARANTIA_CALIDAD,
                     RoleEnum.GERENTE_CONTROL_CALIDAD, RoleEnum.SUPERVISOR_PLANTA,
                     RoleEnum.ANALISTA_CONTROL_CALIDAD, RoleEnum.AUDITOR)
    ),
    CONSULTA_TRAZAS(
        "Lista de Trazas",
        "/trazas/list-trazas",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.DT, RoleEnum.GERENTE_GARANTIA_CALIDAD, RoleEnum.AUDITOR)
    ),

    // ==================== ABM MAESTRO ====================
    CU30_ABM_PROVEEDORES(
        "ABM Proveedores",
        "/proveedores/",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.ANALISTA_PLANTA)
    ),
    CU31_ABM_PRODUCTOS(
        "ABM Productos",
        "/productos/",
        Arrays.asList(RoleEnum.ADMIN, RoleEnum.GERENTE_CONTROL_CALIDAD)
    ),

    // ==================== ADMIN ====================
    CU32_CONFIGURACION_SISTEMA(
        "Configuración del sistema",
        "/admin/configuracion",
        Arrays.asList(RoleEnum.ADMIN)
    ),
    CU33_ABM_USUARIOS(
        "ABM Usuarios",
        "/users",
        Arrays.asList(RoleEnum.ADMIN)
    );

    private final String descripcion;
    private final String urlPattern;
    private final List<RoleEnum> rolesPermitidos;

    PermisosCasoUsoEnum(String descripcion, String urlPattern, List<RoleEnum> rolesPermitidos) {
        this.descripcion = descripcion;
        this.urlPattern = urlPattern;
        this.rolesPermitidos = rolesPermitidos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public List<RoleEnum> getRolesPermitidos() {
        return rolesPermitidos;
    }

    /**
     * Verifica si un rol tiene permiso para acceder a este caso de uso.
     */
    public boolean tienePermiso(RoleEnum rol) {
        return rolesPermitidos.contains(rol);
    }

    /**
     * Obtiene el patron de URL con wildcard para Spring Security.
     * Ejemplo: "/compras/alta/ingreso-compra" -> "/compras/alta/ingreso-compra/**"
     */
    public String getUrlPatternWithWildcard() {
        return urlPattern + "/**";
    }

    /**
     * Convierte la lista de roles a un array de strings para Spring Security.
     * Ejemplo: [ADMIN, ANALISTA_PLANTA] -> ["ADMIN", "ANALISTA_PLANTA"]
     */
    public String[] getRolesAsStringArray() {
        return rolesPermitidos.stream()
            .map(RoleEnum::name)
            .toArray(String[]::new);
    }
}
