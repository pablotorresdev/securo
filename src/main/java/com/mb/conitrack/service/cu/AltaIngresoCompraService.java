package com.mb.conitrack.service.cu;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.service.SecurityContextService;

import org.springframework.beans.factory.annotation.Autowired;

import static com.mb.conitrack.utils.LoteEntityUtils.createLoteIngreso;
import static com.mb.conitrack.utils.LoteEntityUtils.populateLoteAltaStockCompra;
import static com.mb.conitrack.utils.MovimientoAltaUtils.addLoteInfoToMovimientoAlta;
import static com.mb.conitrack.utils.MovimientoAltaUtils.createMovimientoAltaIngresoCompra;

/** CU1 - Alta Ingreso Compra. Crea lote nuevo desde compra externa. */
//***********CU1 ALTA: COMPRA***********
@Service
public class AltaIngresoCompraService extends AbstractCuService {

    @Autowired
    private SecurityContextService securityContextService;

    /** Crea lote nuevo desde compra. Inicializa bultos y movimiento ALTA/COMPRA. */
    @Transactional
    public LoteDTO altaStockPorCompra(LoteDTO loteDTO) {
        // Obtener usuario actual del contexto de seguridad
        User currentUser = securityContextService.getCurrentUser();

        Proveedor proveedor = proveedorRepository.findById(loteDTO.getProveedorId())
            .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));

        Producto producto = productoRepository.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        Optional<Proveedor> fabricante = loteDTO.getFabricanteId() != null
            ? proveedorRepository.findById(loteDTO.getFabricanteId())
            : Optional.empty();

        Lote lote = createLoteIngreso(loteDTO);
        populateLoteAltaStockCompra(lote, loteDTO, producto, proveedor, fabricante.orElse(null));

        Lote loteGuardado = loteRepository.save(lote);  // Cascade persists bultos

        final Movimiento movimientoAltaIngresoCompra = createMovimientoAltaIngresoCompra(loteGuardado, currentUser);
        addLoteInfoToMovimientoAlta(loteGuardado, movimientoAltaIngresoCompra);

        final Movimiento movimientoGuardado = movimientoRepository.save(movimientoAltaIngresoCompra);
        loteGuardado.getMovimientos().add(movimientoGuardado);

        return DTOUtils.fromLoteEntity(loteGuardado);
    }

    /** Valida datos de entrada para alta de compra (cantidad, fechas, bultos). */
    @Transactional
    public boolean validarIngresoCompraInput(final LoteDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (!validarCantidadIngreso(dto, bindingResult)) {
            return false;
        }
        if (!validarFechasProveedor(dto, bindingResult)) {
            return false;
        }

        return validarBultos(dto, bindingResult);
    }

}

