package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.List;

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
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoBajaVenta;

@Service
public class BajaVentaProductoService extends AbstractCuService {

    //***********CU12 BAJA: VENTA***********
    @Transactional
    public Lote bajaVentaProducto(final LoteDTO loteDTO) {
        final Lote lote = loteRepository.findFirstByCodigoLoteAndActivoTrue(
                loteDTO.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final List<Integer> nroBultoList = loteDTO.getNroBultoList();
        final List<BigDecimal> cantidadesBultos = loteDTO.getCantidadesBultos();

        final Movimiento movimiento = persistirMovimientoBajaVenta(loteDTO, lote);

        for (int nroBulto : nroBultoList) {
            final Bulto bultoEntity = lote.getBultoByNro(nroBulto);
            final BigDecimal cantidaConsumoBulto = cantidadesBultos.get(nroBulto - 1);

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

            lote.getTrazas().addAll(bultoEntity.getTrazas());
            bultoRepository.save(bultoEntity);

            loteDTO.getBultosDTOs().add(DTOUtils.fromBultoEntity(bultoEntity));
        }

        boolean todosConsumidos = lote.getBultos().stream()
            .allMatch(b -> b.getEstado() == EstadoEnum.CONSUMIDO);
        lote.setEstado(todosConsumidos ? EstadoEnum.CONSUMIDO : EstadoEnum.EN_USO);

        loteDTO.getTrazaDTOs().addAll(movimiento.getDetalles()
            .stream()
            .flatMap(d -> d.getTrazas().stream().map(DTOUtils::fromTrazaEntity))
            .toList());

        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
    }

    @jakarta.transaction.Transactional
    public Movimiento persistirMovimientoBajaVenta(final LoteDTO loteDTO, final Lote loteEntity) {
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
                .build();
            movimiento.getDetalles().add(det);

            final List<Traza> trazas = bulto.getFirstAvailableTrazaList(cantBulto.intValue());

            for (Traza tr : trazas) {
                tr.setEstado(EstadoEnum.VENDIDO);
            }
            trazaRepository.saveAll(trazas);

            det.getTrazas().addAll(trazas);
        }

        return movimientoRepository.save(movimiento);
    }


    public boolean validarVentaProductoInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        Lote lote = getLoteByCodigoLote(
            loteDTO.getCodigoLote(),
            bindingResult);

        boolean success = lote != null;
        success = success && validarUnidadMedidaVenta(loteDTO, lote, bindingResult);
        success = success && validarFechaEgresoLoteDtoPosteriorLote(loteDTO, lote, bindingResult);
        return success && validarCantidadesPorMedidas(loteDTO, lote, bindingResult);
    }
}
