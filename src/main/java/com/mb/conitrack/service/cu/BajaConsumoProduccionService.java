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
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoBajaProduccion;
import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMayorUnidadMedida;
import static java.lang.Boolean.TRUE;

//***********CU7 BAJA: CONSUMO PRODUCCION***********
@Service
public class BajaConsumoProduccionService extends AbstractCuService {

    @Transactional
    public LoteDTO bajaConsumoProduccion(final LoteDTO loteDTO) {
        final Lote lote = loteRepository.findFirstByCodigoLoteAndActivoTrue(
                loteDTO.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final List<Integer> nroBultoList = loteDTO.getNroBultoList();
        final List<BigDecimal> cantidadesBultos = loteDTO.getCantidadesBultos();
        final List<UnidadMedidaEnum> unidadMedidaBultos = loteDTO.getUnidadMedidaBultos();

        for (int nroBulto : nroBultoList) {
            final Bulto bultoEntity = lote.getBultoByNro(nroBulto);
            final BigDecimal cantidaConsumoBulto = cantidadesBultos.get(nroBulto - 1);
            final UnidadMedidaEnum uniMedidaConsumoBulto = unidadMedidaBultos.get(nroBulto - 1);

            if (BigDecimal.ZERO.compareTo(cantidaConsumoBulto) == 0) {
                continue;
            }

            if (bultoEntity.getUnidadMedida() == uniMedidaConsumoBulto) {
                bultoEntity.setCantidadActual(bultoEntity.getCantidadActual().subtract(cantidaConsumoBulto));
                if (lote.getUnidadMedida() == uniMedidaConsumoBulto) {
                    lote.setCantidadActual(lote.getCantidadActual().subtract(cantidaConsumoBulto));
                } else {
                    final BigDecimal cantidadConsumoLoteConvertida = convertirCantidadEntreUnidades(
                        uniMedidaConsumoBulto,
                        cantidaConsumoBulto,
                        lote.getUnidadMedida());
                    lote.setCantidadActual(lote.getCantidadActual().subtract(cantidadConsumoLoteConvertida));
                }
            } else {
                final BigDecimal cantidadConsumoBultoConvertida = convertirCantidadEntreUnidades(
                    uniMedidaConsumoBulto,
                    cantidaConsumoBulto,
                    bultoEntity.getUnidadMedida());
                bultoEntity.setCantidadActual(bultoEntity.getCantidadActual().subtract(cantidadConsumoBultoConvertida));

                if (lote.getUnidadMedida() == uniMedidaConsumoBulto) {
                    lote.setCantidadActual(lote.getCantidadActual().subtract(cantidaConsumoBulto));
                } else {
                    final BigDecimal cantidadConsumoLoteConvertida = convertirCantidadEntreUnidades(
                        uniMedidaConsumoBulto,
                        cantidaConsumoBulto,
                        lote.getUnidadMedida());
                    lote.setCantidadActual(lote.getCantidadActual().subtract(cantidadConsumoLoteConvertida));
                }
            }
            if (bultoEntity.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
                bultoEntity.setEstado(EstadoEnum.CONSUMIDO);
            } else {
                bultoEntity.setEstado(EstadoEnum.EN_USO);
            }
        }
        if (lote.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
            lote.setEstado(EstadoEnum.CONSUMIDO);
        } else {
            lote.setEstado(EstadoEnum.EN_USO);
        }
        final Movimiento movimiento = persistirMovimientoBajaConsumoProduccion(loteDTO, lote);
        lote.getMovimientos().add(movimiento);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoBajaConsumoProduccion(final LoteDTO loteDTO, final Lote loteEntity) {
        final Movimiento movimiento = createMovimientoBajaProduccion(loteDTO, loteEntity);

        UnidadMedidaEnum uniMedidaMovimiento = loteDTO.getUnidadMedidaBultos().get(0);

        for (int i = 1; i < loteDTO.getCantidadesBultos().size(); i++) {
            uniMedidaMovimiento = obtenerMayorUnidadMedida(uniMedidaMovimiento, loteDTO.getUnidadMedidaBultos().get(i));
        }

        BigDecimal cantidad = BigDecimal.ZERO;

        for (int i = 0; i < loteDTO.getCantidadesBultos().size(); i++) {
            final BigDecimal montoBulto = convertirCantidadEntreUnidades(
                loteDTO.getUnidadMedidaBultos().get(i),
                loteDTO.getCantidadesBultos().get(i),
                uniMedidaMovimiento);
            cantidad = cantidad.add(montoBulto);
        }

        movimiento.setCantidad(cantidad);
        movimiento.setUnidadMedida(uniMedidaMovimiento);

        for (int i = 0; i < loteDTO.getNroBultoList().size(); i++) {
            final Integer nroBulto = loteDTO.getNroBultoList().get(i);
            if (BigDecimal.ZERO.compareTo(loteDTO.getCantidadesBultos().get(i)) == 0) {
                continue;
            }
            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(loteEntity.getBultoByNro(nroBulto))
                .cantidad(loteDTO.getCantidadesBultos().get(i))
                .unidadMedida(loteDTO.getUnidadMedidaBultos().get(i))
                .activo(TRUE)
                .build();

            movimiento.getDetalles().add(det);
        }

        return movimientoRepository.save(movimiento);
    }

    @Transactional
    public boolean validarConsumoProduccionInput(final LoteDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //TODO: caso donde el lote 2/3 se haya usado, pero el 1/3 no ni el 3/3
        final Optional<Lote> lote = loteRepository
            .findByCodigoLoteAndActivoTrue(dto.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (!validarFechaEgresoLoteDtoPosteriorLote(dto, lote.get(), bindingResult)) {
            return false;
        }

        return validarCantidadesPorMedidas(dto, lote.get(), bindingResult);
    }

}
