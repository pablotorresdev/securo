package com.mb.conitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para métricas del dashboard principal.
 * Contiene información resumida del sistema para mostrar al usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDTO {

    /**
     * Cantidad de lotes activos en el sistema
     */
    private Long lotesActivos;

    /**
     * Cantidad de análisis pendientes o en curso
     */
    private Long analisisPendientes;

    /**
     * Cantidad de movimientos del día actual
     */
    private Long movimientosHoy;

    /**
     * Cantidad de lotes en cuarentena
     */
    private Long lotesCuarentena;

    /**
     * Cantidad de alertas o notificaciones pendientes
     */
    private Long alertasPendientes;

    /**
     * Stock total en unidades base
     */
    private Double stockTotal;
}
