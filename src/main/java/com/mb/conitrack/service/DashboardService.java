package com.mb.conitrack.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.DashboardMetricsDTO;
import com.mb.conitrack.dto.UserInfoDTO;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para obtener métricas y datos del dashboard principal.
 */
@Service
@Slf4j
public class DashboardService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private AnalisisRepository analisisRepository;

    /**
     * Obtiene las métricas operativas para mostrar en el dashboard.
     *
     * @return DashboardMetricsDTO con las métricas del sistema
     */
    public DashboardMetricsDTO getDashboardMetrics() {
        DashboardMetricsDTO metrics = new DashboardMetricsDTO();

        try {
            // Contar lotes activos
            long lotesActivos = loteRepository.findAll().stream()
                    .filter(lote -> Boolean.TRUE.equals(lote.getActivo()))
                    .count();
            metrics.setLotesActivos(lotesActivos);

            // Contar lotes en cuarentena
            long lotesCuarentena = loteRepository.findAll().stream()
                    .filter(lote -> Boolean.TRUE.equals(lote.getActivo()) &&
                            DictamenEnum.CUARENTENA.equals(lote.getDictamen()))
                    .count();
            metrics.setLotesCuarentena(lotesCuarentena);

            // Contar análisis pendientes (sin dictamen o en cuarentena)
            long analisisPendientes = analisisRepository.findAll().stream()
                    .filter(analisis -> (analisis.getDictamen() == null ||
                            DictamenEnum.CUARENTENA.equals(analisis.getDictamen())) &&
                            Boolean.TRUE.equals(analisis.getActivo()))
                    .count();
            metrics.setAnalisisPendientes(analisisPendientes);

            // Contar movimientos del día
            LocalDate hoy = LocalDate.now();

            long movimientosHoy = movimientoRepository.findAll().stream()
                    .filter(mov -> mov.getFecha() != null &&
                            mov.getFecha().equals(hoy))
                    .count();
            metrics.setMovimientosHoy(movimientosHoy);

            // Calcular stock total (simplificado - suma de cantidades actuales de lotes activos)
            double stockTotal = loteRepository.findAll().stream()
                    .filter(lote -> Boolean.TRUE.equals(lote.getActivo()))
                    .mapToDouble(lote -> lote.getCantidadActual() != null ? lote.getCantidadActual().doubleValue() : 0.0)
                    .sum();
            metrics.setStockTotal(stockTotal);

            // Alertas pendientes (placeholder - puede implementarse según lógica de negocio)
            // Por ahora: lotes próximos a vencer o en estado crítico
            long alertas = loteRepository.findAll().stream()
                    .filter(lote -> Boolean.TRUE.equals(lote.getActivo()) &&
                            lote.getFechaVencimientoProveedor() != null &&
                            lote.getFechaVencimientoProveedor().isBefore(LocalDate.now().plusDays(30)))
                    .count();
            metrics.setAlertasPendientes(alertas);

            log.info("Dashboard metrics calculated: {}", metrics);

        } catch (Exception e) {
            log.error("Error calculating dashboard metrics", e);
            // Retornar métricas en 0 en caso de error
            metrics.setLotesActivos(0L);
            metrics.setLotesCuarentena(0L);
            metrics.setAnalisisPendientes(0L);
            metrics.setMovimientosHoy(0L);
            metrics.setStockTotal(0.0);
            metrics.setAlertasPendientes(0L);
        }

        return metrics;
    }

    /**
     * Convierte un User entity a UserInfoDTO.
     *
     * @param user Usuario actual
     * @return UserInfoDTO con información del usuario
     */
    public UserInfoDTO getUserInfo(User user) {
        if (user == null) {
            return null;
        }

        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setUsername(user.getUsername());
        userInfo.setRoleName(user.getRole() != null ? user.getRole().getName() : "N/A");
        userInfo.setRoleLevel(user.getRole() != null ? user.getRole().getNivel() : 0);
        userInfo.setExpirationDate(user.getFechaExpiracion());
        userInfo.setIsAuditor(user.isAuditor());
        userInfo.setIsExpired(user.isExpired());

        // Fecha de último acceso: por ahora es la fecha actual
        // En el futuro se puede guardar en BD
        userInfo.setLastAccessDate(LocalDateTime.now());

        return userInfo;
    }
}
