package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.SecurityContextService;

import static com.mb.conitrack.enums.EstadoEnum.CONSUMIDO;
import static com.mb.conitrack.enums.EstadoEnum.EN_USO;
import static com.mb.conitrack.utils.MovimientoBajaUtils.createMovimientoAjusteStock;
import static com.mb.conitrack.utils.UnidadMedidaUtils.restarMovimientoConvertido;
import static java.lang.Integer.parseInt;

//***********CU25. BAJA: Ajuste de Inventario***********
@Service
public class BajaAjusteStockService extends AbstractCuService {

    @Autowired
    private SecurityContextService securityContextService;

    @Transactional
    public LoteDTO bajaAjusteStock(final MovimientoDTO dto) {
        User currentUser = securityContextService.getCurrentUser();

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Bulto bulto = lote.getBultoByNro(parseInt(dto.getNroBulto()));

        final Movimiento movimiento = persistirMovimientoAjuste(dto, bulto, currentUser);

        bulto.setCantidadActual(restarMovimientoConvertido(dto, bulto));
        lote.setCantidadActual(restarMovimientoConvertido(dto, lote));

        boolean unidadVenta = lote.getProducto().getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA;

        if (unidadVenta) {
            final BigDecimal cantidad = movimiento.getCantidad();
            if (movimiento.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
                throw new IllegalStateException("La traza solo es aplicable a UNIDADES");
            }

            if (cantidad.stripTrailingZeros().scale() > 0) {
                throw new IllegalStateException("La cantidad de Unidades debe ser entero");
            }

            final List<Traza> trazas = new ArrayList<>();
            for (TrazaDTO trazaDTO : dto.getTrazaDTOs()) {
                final Long nroTraza = trazaDTO.getNroTraza();
                for (Traza trazasLote : lote.getActiveTrazas()) {
                    if (trazasLote.getNroTraza().equals(nroTraza)) {
                        trazas.add(trazasLote);
                        break;
                    }
                }
            }

            if (movimiento.getDetalles().size() > 1) {
                throw new IllegalArgumentException("Multiajuste no soportado aun");
            }

            final DetalleMovimiento detalleMovimiento = movimiento.getDetalles().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El detalle del movimiento de ajuste no existe."));

            for (Traza traza : trazas) {
                traza.setEstado(CONSUMIDO);
                traza.getDetalles().addAll(movimiento.getDetalles());
            }
            detalleMovimiento.getTrazas().addAll(trazas);
            trazaRepository.saveAll(trazas);
            dto.setTrazaDTOs(trazas.stream().map(DTOUtils::fromTrazaEntity).toList());
        }

        if (bulto.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
            bulto.setEstado(CONSUMIDO);
        } else {
            bulto.setEstado(EN_USO);
        }

        boolean todosConsumidos = lote.getBultos().stream()
            .allMatch(b -> b.getEstado() == CONSUMIDO);
        lote.setEstado(todosConsumidos ? CONSUMIDO : EN_USO);

        // CU28: Cancelar análisis en curso si el lote queda sin stock (dictamen == null)
        if (lote.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
            if (lote.getUltimoAnalisis() != null && lote.getUltimoAnalisis().getDictamen() == null) {
                lote.getUltimoAnalisis().setDictamen(com.mb.conitrack.enums.DictamenEnum.CANCELADO);
                analisisRepository.save(lote.getUltimoAnalisis());
            }
        }

        lote.getMovimientos().add(movimiento);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoAjuste(final MovimientoDTO dto, Bulto bulto, User currentUser) {
        return movimientoRepository.save(createMovimientoAjusteStock(dto, bulto, currentUser));
    }

    @Transactional
    public boolean validarAjusteStockInput(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        final Optional<Lote> lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (lote.get().getTrazado()) {
            if (dto.getTrazaDTOs() == null || dto.getTrazaDTOs().isEmpty()) {
                bindingResult.rejectValue("trazaDTOs", "", "Debe seleccionar al menos una unidad a muestrear.");
                return false;
            }
        }

        if (!validarFechaMovimientoPosteriorIngresoLote(dto, lote.get().getFechaIngreso(), bindingResult)) {
            return false;
        }

        final Optional<Bulto> bulto = bultoRepository.findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue(
            dto.getCodigoLote(),
            parseInt(dto.getNroBulto()));

        if (bulto.isEmpty()) {
            bindingResult.rejectValue("nroBulto", "", "Bulto no encontrado.");
            return false;
        }

        return validarCantidadesMovimiento(dto, bulto.get(), bindingResult);
    }

}
