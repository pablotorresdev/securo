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
import com.mb.conitrack.utils.LoteEntityUtils;

import static com.mb.conitrack.utils.MovimientoEntityUtils.addLoteInfoToMovimientoAlta;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoCompra;

@Service
public class AltaIngresoCompraService extends AbstractCuService {

    private static LoteEntityUtils loteUtils() {
        return LoteEntityUtils.getInstance();
    }

    //***********CU1 ALTA: COMPRA***********
    @Transactional
    public LoteDTO altaStockPorCompra(LoteDTO loteDTO) {
        Proveedor proveedor = proveedorRepository.findById(loteDTO.getProveedorId())
            .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));

        Producto producto = productoRepository.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        Optional<Proveedor> fabricante = loteDTO.getFabricanteId() != null
            ? proveedorRepository.findById(loteDTO.getFabricanteId())
            : Optional.empty();

        Lote lote = loteUtils().createLoteIngreso(loteDTO);
        loteUtils().populateLoteAltaStockCompra(lote, loteDTO, producto, proveedor, fabricante.orElse(null));

        Lote loteGuardado = loteRepository.save(lote);
        bultoRepository.saveAll(loteGuardado.getBultos());

        final Movimiento movimientoAltaIngresoCompra = createMovimientoAltaIngresoCompra(loteGuardado);
        addLoteInfoToMovimientoAlta(loteGuardado, movimientoAltaIngresoCompra);

        final Movimiento movimientoGuardado = movimientoRepository.save(movimientoAltaIngresoCompra);
        loteGuardado.getMovimientos().add(movimientoGuardado);

        return DTOUtils.fromLoteEntity(loteGuardado);
    }

    @Transactional
    public boolean validarIngresoCompra(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (!validateCantidadIngreso(loteDTO, bindingResult)) {
            return false;
        }
        if (!validateFechasProveedor(loteDTO, bindingResult)) {
            return false;
        }

        return validarBultos(loteDTO, bindingResult);
    }

}

