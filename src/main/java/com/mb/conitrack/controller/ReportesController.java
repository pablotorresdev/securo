package com.mb.conitrack.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.service.AuditorAccessLogger;
import com.mb.conitrack.service.SecurityContextService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para reportes y consultas de solo lectura.
 * Diseñado específicamente para usuarios con rol AUDITOR.
 *
 * Características:
 * - Solo endpoints de lectura (GET)
 * - Acceso restringido por Spring Security
 * - Logging automático de todos los accesos
 * - Retorna datos incluyendo registros con baja lógica (activo=false)
 */
@RestController
@RequestMapping("/api/reportes")
@Slf4j
public class ReportesController {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private AuditorAccessLogger auditorAccessLogger;

    /**
     * Obtiene todos los lotes (incluye bajas lógicas).
     * Endpoint de solo lectura para auditores.
     */
    @GetMapping("/lotes")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'DT', 'GERENTE_GARANTIA_CALIDAD', 'GERENTE_CONTROL_CALIDAD', 'SUPERVISOR_PLANTA', 'ANALISTA_CONTROL_CALIDAD', 'ANALISTA_PLANTA')")
    public ResponseEntity<List<Lote>> getAllLotes(HttpServletRequest request) {
        User currentUser = securityContextService.getCurrentUser();

        // Log de acceso
        auditorAccessLogger.logReporteAccess(currentUser, "Consulta todos los lotes", request);

        log.info("Usuario {} consultó todos los lotes", currentUser.getUsername());

        // Retorna TODOS los lotes, incluyendo los con activo=false
        List<Lote> lotes = loteRepository.findAll();
        return ResponseEntity.ok(lotes);
    }

    /**
     * Obtiene un lote específico por ID (incluye baja lógica).
     */
    @GetMapping("/lotes/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'DT', 'GERENTE_GARANTIA_CALIDAD', 'GERENTE_CONTROL_CALIDAD', 'SUPERVISOR_PLANTA', 'ANALISTA_CONTROL_CALIDAD', 'ANALISTA_PLANTA')")
    public ResponseEntity<Lote> getLoteById(
            @PathVariable Long id,
            HttpServletRequest request) {

        User currentUser = securityContextService.getCurrentUser();
        auditorAccessLogger.logReporteAccess(currentUser,
            "Consulta lote por ID: " + id, request);

        log.info("Usuario {} consultó lote con ID {}", currentUser.getUsername(), id);

        return loteRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene todos los movimientos (incluye bajas lógicas).
     */
    @GetMapping("/movimientos")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'DT', 'GERENTE_GARANTIA_CALIDAD', 'GERENTE_CONTROL_CALIDAD', 'SUPERVISOR_PLANTA', 'ANALISTA_CONTROL_CALIDAD', 'ANALISTA_PLANTA')")
    public ResponseEntity<List<Movimiento>> getAllMovimientos(HttpServletRequest request) {
        User currentUser = securityContextService.getCurrentUser();
        auditorAccessLogger.logReporteAccess(currentUser, "Consulta todos los movimientos", request);

        log.info("Usuario {} consultó todos los movimientos", currentUser.getUsername());

        List<Movimiento> movimientos = movimientoRepository.findAll();
        return ResponseEntity.ok(movimientos);
    }

    /**
     * Obtiene movimientos de un lote específico.
     */
    @GetMapping("/movimientos/lote/{loteId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'DT', 'GERENTE_GARANTIA_CALIDAD', 'GERENTE_CONTROL_CALIDAD', 'SUPERVISOR_PLANTA', 'ANALISTA_CONTROL_CALIDAD', 'ANALISTA_PLANTA')")
    public ResponseEntity<List<Movimiento>> getMovimientosByLote(
            @PathVariable Long loteId,
            HttpServletRequest request) {

        User currentUser = securityContextService.getCurrentUser();
        auditorAccessLogger.logReporteAccess(currentUser,
            "Consulta movimientos del lote ID: " + loteId, request);

        log.info("Usuario {} consultó movimientos del lote ID {}",
            currentUser.getUsername(), loteId);

        return loteRepository.findById(loteId)
            .map(lote -> {
                // Filtrar movimientos del lote
                List<Movimiento> movimientos = movimientoRepository.findAll().stream()
                    .filter(m -> m.getLote() != null && m.getLote().getId().equals(loteId))
                    .toList();
                return ResponseEntity.ok(movimientos);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene todos los movimientos (sin filtro de fecha - simplificado).
     * Para filtros avanzados se pueden agregar métodos adicionales según necesidad.
     */

    /**
     * Obtiene lotes activos (filtro por activo=true).
     * Nota: findAll() ya retorna todos, este endpoint filtra por activo=true
     */
    @GetMapping("/lotes/activos")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'DT', 'GERENTE_GARANTIA_CALIDAD', 'GERENTE_CONTROL_CALIDAD', 'SUPERVISOR_PLANTA', 'ANALISTA_CONTROL_CALIDAD', 'ANALISTA_PLANTA')")
    public ResponseEntity<List<Lote>> getLotesActivos(HttpServletRequest request) {
        User currentUser = securityContextService.getCurrentUser();
        auditorAccessLogger.logReporteAccess(currentUser, "Consulta lotes activos", request);

        log.info("Usuario {} consultó lotes activos", currentUser.getUsername());

        // Filtrar en memoria (alternativa simple sin agregar método al repository)
        List<Lote> lotes = loteRepository.findAll().stream()
            .filter(l -> Boolean.TRUE.equals(l.getActivo()))
            .toList();
        return ResponseEntity.ok(lotes);
    }

    /**
     * Obtiene resumen de permisos del usuario actual (para debugging/auditoría).
     */
    @GetMapping("/usuario/permisos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getResumenPermisos(HttpServletRequest request) {
        User currentUser = securityContextService.getCurrentUser();
        auditorAccessLogger.logReporteAccess(currentUser, "Consulta permisos propios", request);

        // Este método está en ReversoAuthorizationService para propósitos de logging
        // Lo simulamos aquí
        String resumen = String.format(
            "Usuario: %s, Rol: %s (nivel %d), Puede reversar: movimientos propios y de niveles inferiores",
            currentUser.getUsername(),
            currentUser.getRole().getName(),
            currentUser.getRole().getNivel()
        );

        return ResponseEntity.ok(resumen);
    }
}
