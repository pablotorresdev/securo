package com.mb.conitrack.service.cu;

import java.util.Comparator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static com.mb.conitrack.utils.LoteEntityUtils.createLoteIngreso;
import static com.mb.conitrack.utils.LoteEntityUtils.populateLoteAltaProduccionPropia;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoProduccion;

//***********CU20 ALTA: INGRESO PRODUCCION INTERNA***********
@Service
public class AltaIngresoProduccionService extends AbstractCuService {

    @Transactional
    public LoteDTO altaStockPorProduccion(final LoteDTO loteDTO) {
        final Proveedor conifarma = proveedorRepository.findConifarma()
            .orElseThrow(() -> new IllegalArgumentException("El proveedor Conifarma no existe."));

        final Producto producto = productoRepository.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        final Lote lote = createLoteIngreso(loteDTO);
        populateLoteAltaProduccionPropia(lote, loteDTO, producto, conifarma);

        final Lote loteGuardado = loteRepository.save(lote);
        bultoRepository.saveAll(loteGuardado.getBultos());

        if (loteGuardado.getTrazas() != null && !loteGuardado.getTrazas().isEmpty()) {
            trazaRepository.saveAll(loteGuardado.getTrazas());
        }

        final Movimiento movimiento = createMovimientoAltaIngresoProduccion(loteGuardado);
        movimientoRepository.save(movimiento);

        loteGuardado.getBultos().stream()
            .sorted(Comparator.comparing(Bulto::getNroBulto))
            .forEach(b -> {
                final DetalleMovimiento det = DetalleMovimiento.builder()
                    .movimiento(movimiento)
                    .bulto(b)
                    .cantidad(b.getCantidadInicial())
                    .unidadMedida(b.getUnidadMedida())
                    .build();

                if (b.getTrazas() != null && !b.getTrazas().isEmpty()) {
                    b.getTrazas().stream()
                        .sorted(Comparator.comparing(Traza::getNroTraza))
                        .forEach(det.getTrazas()::add);
                }

                movimiento.getDetalles().add(det);
            });

        movimientoRepository.save(movimiento);

        return DTOUtils.fromLoteEntity(loteGuardado);
    }

    @Transactional
    public boolean validarIngresoProduccionInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (!validarCantidadIngreso(loteDTO, bindingResult)) {
            return false;
        }
        if (!validarBultos(loteDTO, bindingResult)) {
            return false;
        }
        return validarTraza(loteDTO, bindingResult);
    }

    @Transactional
    boolean validarTraza(final LoteDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //TODO: validar que la traza solo se aplique a unidad de venta
        if (dto.getTrazaInicial() != null) {
            if (dto.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
                bindingResult.rejectValue("trazaInicial", "", "El número de traza solo aplica a unidades de venta");
                return false;
            }
            final Long maxNroTraza = trazaRepository.findMaxNroTraza(dto.getProductoId());
            if (maxNroTraza > 0 && dto.getTrazaInicial() <= maxNroTraza) {
                bindingResult.rejectValue(
                    "trazaInicial",
                    "",
                    "El número de traza debe ser mayor al último registrado. " + maxNroTraza);
                return false;
            }
        }
        return true;
    }

}

