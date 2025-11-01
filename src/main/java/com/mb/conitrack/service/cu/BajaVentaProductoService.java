package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static com.mb.conitrack.enums.EstadoEnum.VENDIDO;
import static com.mb.conitrack.utils.MovimientoBajaUtils.createMovimientoBajaVenta;
import static java.lang.Boolean.TRUE;

/** CU22 - Baja Venta Producto. Descuenta stock por venta a clientes. */
@Service
public class BajaVentaProductoService extends AbstractCuService {

    /** Procesa venta de producto descontando stock y marcando trazas como VENDIDO. */
    @Transactional
    public LoteDTO bajaVentaProducto(final LoteDTO loteDTO) {
        final Lote lote = loteRepository.findFirstByCodigoLoteAndActivoTrue(
                loteDTO.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
        final boolean loteTrazado = TRUE.equals(lote.getTrazado());

        final List<Integer> nroBultoList = loteDTO.getNroBultoList();
        final List<BigDecimal> cantidadesBultos = loteDTO.getCantidadesBultos();

        final Movimiento movimiento = persistirMovimientoBajaVenta(loteDTO, lote);

        for (int i = 0; i < nroBultoList.size(); i++) {

            final int nroBulto = nroBultoList.get(i);
            final Bulto bultoEntity = lote.getBultoByNro(nroBulto);
            final BigDecimal cantidaConsumoBulto = cantidadesBultos.get(i);

            if (BigDecimal.ZERO.compareTo(cantidaConsumoBulto) == 0) {
                continue;
            }

            bultoEntity.setCantidadActual(bultoEntity.getCantidadActual().subtract(cantidaConsumoBulto));
            lote.setCantidadActual(lote.getCantidadActual().subtract(cantidaConsumoBulto));

            if (bultoEntity.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
                bultoEntity.setEstado(EstadoEnum.CONSUMIDO);
            } else {
                bultoEntity.setEstado(EstadoEnum.EN_USO);
            }

//            if (loteTrazado) {
//                lote.getTrazas().addAll(bultoEntity.getTrazas());
//            }
            bultoRepository.save(bultoEntity);

            loteDTO.getBultosDTOs().add(DTOUtils.fromBultoEntity(bultoEntity));
        }

        boolean todosConsumidos = lote.getBultos().stream()
            .allMatch(b -> b.getEstado() == EstadoEnum.CONSUMIDO);
        lote.setEstado(todosConsumidos ? EstadoEnum.CONSUMIDO : EstadoEnum.EN_USO);

        // CU22: Cancelar análisis en curso si el lote queda sin stock (dictamen == null)
        if (lote.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
            if (lote.getUltimoAnalisis() != null && lote.getUltimoAnalisis().getDictamen() == null) {
                lote.getUltimoAnalisis().setDictamen(com.mb.conitrack.enums.DictamenEnum.CANCELADO);
                analisisRepository.save(lote.getUltimoAnalisis());
            }
        }

        if (loteTrazado) {
            loteDTO.getTrazaDTOs().addAll(movimiento.getDetalles()
                .stream()
                .flatMap(d -> d.getTrazas().stream().map(DTOUtils::fromTrazaEntity))
                .toList());
        }

        lote.getMovimientos().add(movimiento);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoBajaVenta(final LoteDTO loteDTO, final Lote loteEntity) {
        final boolean loteTrazado = TRUE.equals(loteEntity.getTrazado());
        final Movimiento movimiento = createMovimientoBajaVenta(loteDTO, loteEntity);

        BigDecimal cantidad = BigDecimal.ZERO;
        for (int i = 0; i < loteDTO.getCantidadesBultos().size(); i++) {
            cantidad = cantidad.add(loteDTO.getCantidadesBultos().get(i));
        }
        movimiento.setCantidad(cantidad);
        movimiento.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        for (int i = 0; i < loteDTO.getNroBultoList().size(); i++) {
            final Integer nroBulto = loteDTO.getNroBultoList().get(i);
            final BigDecimal cantBulto = loteDTO.getCantidadesBultos().get(i);

            if (BigDecimal.ZERO.compareTo(cantBulto) == 0) {
                continue;
            }

            final Bulto bulto = loteEntity.getBultoByNro(nroBulto);

            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(bulto)
                .cantidad(cantBulto)
                .unidadMedida(UnidadMedidaEnum.UNIDAD)
                .activo(TRUE)
                .build();
            movimiento.getDetalles().add(det);

            if (loteTrazado) {
                final List<Traza> trazas = bulto.getFirstAvailableTrazaList(cantBulto.intValue());

                if (trazas != null && !trazas.isEmpty()) {
                    for (Traza tr : trazas) {
                        tr.setEstado(VENDIDO);
                    }
                    trazaRepository.saveAll(trazas);

                    det.getTrazas().addAll(trazas);
                }
            }
        }

        return movimientoRepository.save(movimiento);
    }

    @Transactional
    public boolean validarVentaProductoInput(final LoteDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        final Optional<Lote> lote = loteRepository
            .findByCodigoLoteAndActivoTrue(dto.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (!validarUnidadMedidaVenta(dto, lote.get(), bindingResult)) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (!validarFechaEgresoLoteDtoPosteriorLote(dto, lote.get(), bindingResult)) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        return validarCantidadesPorMedidas(dto, lote.get(), bindingResult);
    }

}
