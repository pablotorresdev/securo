package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.service.ReversoAuthorizationService;
import com.mb.conitrack.service.SecurityContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio coordinador para el reverso de movimientos (CU - Reverso).
 * Delega la lógica específica de reverso a servicios especializados:
 * - ReversoAltaService: Para movimientos de tipo ALTA
 * - ReversoBajaService: Para movimientos de tipo BAJA
 * - ReversoModificacionService: Para movimientos de tipo MODIFICACION
 */
@Service
public class ModifReversoMovimientoService extends AbstractCuService {

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private ReversoAuthorizationService reversoAuthorizationService;

    @Autowired
    private ReversoAltaService reversoAltaService;

    @Autowired
    private ReversoBajaService reversoBajaService;

    @Autowired
    private ReversoModificacionService reversoModificacionService;

    @Transactional
    public LoteDTO persistirReversoMovmiento(final MovimientoDTO dto) {
        // Obtener usuario actual del contexto de seguridad
        User currentUser = securityContextService.getCurrentUser();

        final List<Movimiento> allByCodigoMovimiento = movimientoRepository
                .findAllByCodigoMovimiento(dto.getCodigoMovimientoOrigen());

        if (allByCodigoMovimiento.isEmpty()) {
            throw new IllegalArgumentException("El Movimmiento no existe.");
        } else if (allByCodigoMovimiento.size() == 1) {
            final Movimiento movOrigen = allByCodigoMovimiento.get(0);

            // VALIDACIÓN DE AUTORIZACIÓN: Verificar si el usuario puede reversar
            reversoAuthorizationService.validarPermisoReverso(movOrigen, currentUser);

            switch (movOrigen.getTipoMovimiento()) {
                case ALTA -> {
                    return delegarReversoAlta(dto, movOrigen, currentUser);
                }
                case MODIFICACION -> {
                    return delegarReversoModificacion(dto, movOrigen, currentUser);
                }
                case BAJA -> {
                    return delegarReversoBaja(dto, movOrigen, currentUser);
                }
            }
        } else {
            throw new IllegalArgumentException("Cantidad incorrecta de movimientos");
        }

        return new LoteDTO();
    }

    /**
     * Delega el reverso de movimientos ALTA al servicio especializado
     */
    private LoteDTO delegarReversoAlta(MovimientoDTO dto, Movimiento movOrigen, User currentUser) {
        if (movOrigen.getMotivo() == MotivoEnum.COMPRA) { //CU1
            return reversoAltaService.reversarAltaIngresoCompra(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.PRODUCCION_PROPIA) { //CU20
            return reversoAltaService.reversarAltaIngresoProduccion(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_VENTA) { //CU23
            return reversoAltaService.reversarAltaDevolucionVenta(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.RETIRO_MERCADO) { //CU24
            return reversoAltaService.reversarRetiroMercado(dto, movOrigen, currentUser);
        }
        throw new IllegalStateException("Motivo de ALTA no soportado para reverso: " + movOrigen.getMotivo());
    }

    /**
     * Delega el reverso de movimientos MODIFICACION al servicio especializado
     */
    private LoteDTO delegarReversoModificacion(MovimientoDTO dto, Movimiento movOrigen, User currentUser) {
        if (movOrigen.getMotivo() == MotivoEnum.ANALISIS) { //CU2/CU8
            return reversoModificacionService.reversarModifDictamenCuarentena(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.RESULTADO_ANALISIS) { //CU5/6
            return reversoModificacionService.reversarModifResultadoAnalisis(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.LIBERACION) { //CU21
            return reversoModificacionService.reversarModifLiberacionProducto(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.TRAZADO) { //CU27
            return reversoModificacionService.reversarModifTrazadoLote(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.ANULACION_ANALISIS) {//CU11
            return reversoModificacionService.reversarAnulacionAnalisis(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.VENCIMIENTO) {//CU10
            throw new IllegalStateException(
                    "No se puede reversar un vencimiento de lote (CU10). El vencimiento es un proceso automático irreversible.");
        }
        if (movOrigen.getMotivo() == MotivoEnum.EXPIRACION_ANALISIS) {//CU9
            throw new IllegalStateException(
                    "No se puede reversar una expiración de análisis (CU9). La expiración es un proceso automático.");
        }
        if (movOrigen.getMotivo() == MotivoEnum.RETIRO_MERCADO) {//CU24
            throw new IllegalStateException(
                    "El lote origen tiene un recall asociado, no se puede reversar el movimiento.");
        }
        throw new IllegalStateException("Motivo de MODIFICACION no soportado para reverso: " + movOrigen.getMotivo());
    }

    /**
     * Delega el reverso de movimientos BAJA al servicio especializado
     */
    private LoteDTO delegarReversoBaja(MovimientoDTO dto, Movimiento movOrigen, User currentUser) {
        if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_COMPRA) { //CU4
            return reversoBajaService.reversarBajaDevolucionCompra(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.MUESTREO) { //CU3
            return reversoBajaService.reversarBajaMuestreoBulto(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.CONSUMO_PRODUCCION) {//CU7
            return reversoBajaService.reversarBajaConsumoProduccion(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.VENTA) { //CU22
            return reversoBajaService.reversarBajaVentaProducto(dto, movOrigen, currentUser);
        }
        if (movOrigen.getMotivo() == MotivoEnum.AJUSTE) { //CU28
            return reversoBajaService.reversarBajaAjuste(dto, movOrigen, currentUser);
        }
        throw new IllegalStateException("Motivo de BAJA no soportado para reverso: " + movOrigen.getMotivo());
    }
}
