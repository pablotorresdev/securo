package com.mb.conitrack.service.cu;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.mb.conitrack.service.TrazaService;
import com.mb.conitrack.utils.LoteEntityUtils;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoProduccion;

@Service
public class AltaIngresoProduccionService extends AbstractCuService {

    @Autowired
    private TrazaService trazaService;

    private static LoteEntityUtils loteUtils() {
        return LoteEntityUtils.getInstance();
    }

    //***********CU10 ALTA: PRODUCCION INTERNA***********
    @Transactional
    public LoteDTO altaStockPorProduccion(final LoteDTO loteDTO) {
        final Proveedor conifarma = proveedorRepository.findConifarma()
            .orElseThrow(() -> new IllegalArgumentException("El proveedor Conifarma no existe."));
        ;

        final Producto producto = productoRepository.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        final Lote lote = loteUtils().createLoteIngreso(loteDTO);
        loteUtils().populateLoteAltaProduccionPropia(lote, loteDTO, producto, conifarma);

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
        boolean success = validateCantidadIngreso(loteDTO, bindingResult);
        success = success && validarBultos(loteDTO, bindingResult);
        success = success && validarTraza(loteDTO, bindingResult);
        return success;
    }

    @Transactional
    boolean validarTraza(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //TODO: validar que la traza solo se aplique a unidad de venta
        if (loteDTO.getTrazaInicial() != null) {
            if (loteDTO.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
                bindingResult.rejectValue("trazaInicial", "", "El número de traza solo aplica a unidades de venta");
                return false;
            }
            final Long maxNroTraza = trazaService.findMaxNroTraza(loteDTO.getProductoId());
            if (maxNroTraza > 0 && loteDTO.getTrazaInicial() <= maxNroTraza) {
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

