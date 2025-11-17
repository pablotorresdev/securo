package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.service.SecurityContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

/**
 * Servicio coordinador para CU3 - Baja Muestreo.
 * Descuenta stock por muestreo para análisis de calidad.
 * <p>
 * Delega la lógica específica a servicios especializados:
 * - MuestreoTrazableService: Muestreo de productos trazables (unidades individuales)
 * - MuestreoMultiBultoService: Muestreo distribuido entre múltiples bultos
 * </p>
 */
@Service
public class BajaMuestreoBultoService extends AbstractCuService {

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private MuestreoTrazableService muestreoTrazableService;

    @Autowired
    private MuestreoMultiBultoService muestreoMultiBultoService;

    /**
     * Procesa muestreo de producto trazable asociándolo a análisis.
     * Marca trazas como CONSUMIDO.
     *
     * @param dto datos del movimiento de muestreo trazable
     * @return DTO del lote actualizado
     */
    @Transactional
    public LoteDTO bajaMuestreoTrazable(final MovimientoDTO dto) {
        User currentUser = securityContextService.getCurrentUser();
        return muestreoTrazableService.procesarMuestreoTrazable(dto, currentUser);
    }

    /**
     * Valida la entrada para muestreo trazable.
     * Verifica lote, análisis, fechas, bulto y cantidades.
     *
     * @param dto datos del movimiento a validar
     * @param bindingResult resultado de validación
     * @return true si la validación es exitosa
     */
    @Transactional
    public boolean validarMuestreoTrazableInput(final MovimientoDTO dto, final BindingResult bindingResult) {
        return muestreoTrazableService.validarMuestreoTrazableInput(dto, bindingResult);
    }

    /**
     * Procesa muestreo distribuido entre múltiples bultos.
     * Descuenta cantidades de cada bulto especificado.
     *
     * @param loteDTO datos del lote con cantidades por bulto
     * @return DTO del lote actualizado
     */
    @Transactional
    public LoteDTO bajamuestreoMultiBulto(final LoteDTO loteDTO) {
        User currentUser = securityContextService.getCurrentUser();
        return muestreoMultiBultoService.procesarMuestreoMultiBulto(loteDTO, currentUser);
    }

    /**
     * Valida la entrada para muestreo multi-bulto.
     * Verifica lote, análisis, fechas y cantidades por bulto.
     *
     * @param loteDTO datos del lote a validar
     * @param bindingResult resultado de validación
     * @return true si la validación es exitosa
     */
    @Transactional
    public boolean validarmuestreoMultiBultoInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        return muestreoMultiBultoService.validarMuestreoMultiBultoInput(loteDTO, bindingResult);
    }

    // ========== Métodos de Delegación para Compatibilidad con Tests ==========

    /**
     * Delega a MuestreoTrazableService.
     * Expuesto para testing.
     */
    @Transactional
    public com.mb.conitrack.entity.Movimiento persistirMovimientoMuestreo(
            final MovimientoDTO dto,
            com.mb.conitrack.entity.Bulto bulto,
            User currentUser) {
        return muestreoTrazableService.persistirMovimientoMuestreo(dto, bulto, currentUser);
    }

    /**
     * Delega a MuestreoTrazableService.
     * Expuesto para testing.
     */
    @Transactional
    public com.mb.conitrack.entity.Movimiento crearMovimientoMuestreoConAnalisisEnCurso(
            final MovimientoDTO dto,
            final com.mb.conitrack.entity.Bulto bulto,
            final java.util.Optional<com.mb.conitrack.entity.Analisis> analisisEnCurso,
            User currentUser) {
        return muestreoTrazableService.crearMovimientoMuestreoConAnalisisEnCurso(dto, bulto, analisisEnCurso, currentUser);
    }

    /**
     * Delega a MuestreoTrazableService.
     * Expuesto para testing.
     */
    @Transactional
    public com.mb.conitrack.entity.Movimiento crearMovmimientoMuestreoConAnalisisDictaminado(
            final MovimientoDTO dto,
            final com.mb.conitrack.entity.Bulto bulto,
            User currentUser) {
        return muestreoTrazableService.crearMovimientoMuestreoConAnalisisDictaminado(dto, bulto, currentUser);
    }

    /**
     * Delega a MuestreoMultiBultoService.
     * Expuesto para testing.
     */
    @Transactional
    public com.mb.conitrack.entity.Movimiento persistirMovimientoBajaMuestreoMultiBulto(
            final LoteDTO loteDTO,
            final com.mb.conitrack.entity.Lote loteEntity,
            User currentUser) {
        return muestreoMultiBultoService.persistirMovimientoBajaMuestreoMultiBulto(loteDTO, loteEntity, currentUser);
    }
}
