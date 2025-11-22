package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.thymeleaf.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.mb.conitrack.utils.MovimientoBajaUtils.createMovimientoPorMuestreoMultiBulto;
import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMayorUnidadMedida;
import static java.lang.Boolean.TRUE;

/**
 * Servicio especializado para muestreo de múltiples bultos.
 * Maneja el flujo completo de muestreo distribuido entre varios bultos de un lote.
 * - Procesamiento de muestreo multi-bulto
 * - Descuento de cantidades por bulto con conversión de unidades
 * - Asociación con análisis del lote
 * - Validación de entrada para muestreo multi-bulto
 */
@Service
public class MuestreoMultiBultoService extends AbstractCuService {

    /**
     * Procesa muestreo distribuido entre múltiples bultos.
     * Descuenta cantidades de cada bulto y actualiza estados.
     */
    @Transactional
    public LoteDTO procesarMuestreoMultiBulto(final LoteDTO loteDTO, final User currentUser) {
        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(loteDTO.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Movimiento movimientoMultiBulto = persistirMovimientoBajaMuestreoMultiBulto(loteDTO, lote, currentUser);

        procesarDescontarCantidadesPorBulto(loteDTO, lote);
        actualizarEstadosLoteYBultos(loteDTO, lote);

        lote.getMovimientos().add(movimientoMultiBulto);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    /**
     * Persiste el movimiento de baja por muestreo multi-bulto.
     */
    @Transactional
    public Movimiento persistirMovimientoBajaMuestreoMultiBulto(final LoteDTO loteDTO, final Lote loteEntity, User currentUser) {
        final Movimiento movimiento = createMovimientoPorMuestreoMultiBulto(loteDTO, loteEntity, currentUser);

        Analisis ultimoAnalisis = loteEntity.getUltimoAnalisis();
        if (ultimoAnalisis == null) {
            throw new IllegalStateException("No hay Analisis con al que asociar el muestreo");
        }

        movimiento.setNroAnalisis(ultimoAnalisis.getNroAnalisis());

        UnidadMedidaEnum uniMedidaMovimiento = calcularUnidadMedidaMovimiento(loteDTO);
        BigDecimal cantidad = calcularCantidadTotalMovimiento(loteDTO, uniMedidaMovimiento);

        movimiento.setCantidad(cantidad);
        movimiento.setUnidadMedida(uniMedidaMovimiento);

        agregarDetallesMovimiento(loteDTO, loteEntity, movimiento);

        return movimientoRepository.save(movimiento);
    }

    /**
     * Valida la entrada para muestreo multi-bulto.
     */
    @Transactional
    public boolean validarMuestreoMultiBultoInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        final Optional<Lote> lote = loteRepository
            .findByCodigoLoteAndActivoTrue(loteDTO.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (StringUtils.isEmptyOrWhitespace(lote.get().getUltimoNroAnalisis())) {
            bindingResult.rejectValue("codigoLote", "", "El lote no tiene Analisis asociado.");
            return false;
        }

        if (!validarFechaEgresoLoteDtoPosteriorLote(loteDTO, lote.get(), bindingResult)) {
            return false;
        }

        return validarCantidadesPorMedidas(loteDTO, lote.get(), bindingResult);
    }

    // ========== Métodos Privados de Soporte ==========

    /**
     * Procesa el descuento de cantidades en cada bulto con conversión de unidades.
     */
    void procesarDescontarCantidadesPorBulto(final LoteDTO loteDTO, final Lote lote) {
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

            descontarCantidadBulto(bultoEntity, cantidaConsumoBulto, uniMedidaConsumoBulto);
            descontarCantidadLote(lote, cantidaConsumoBulto, uniMedidaConsumoBulto);
        }
    }

    /**
     * Descuenta cantidad del bulto con conversión de unidades si es necesario.
     */
    void descontarCantidadBulto(final Bulto bulto, final BigDecimal cantidad, final UnidadMedidaEnum unidad) {
        if (bulto.getUnidadMedida() == unidad) {
            bulto.setCantidadActual(bulto.getCantidadActual().subtract(cantidad));
        } else {
            final BigDecimal cantidadConvertida = convertirCantidadEntreUnidades(
                unidad,
                cantidad,
                bulto.getUnidadMedida());
            bulto.setCantidadActual(bulto.getCantidadActual().subtract(cantidadConvertida));
        }
    }

    /**
     * Descuenta cantidad del lote con conversión de unidades si es necesario.
     */
    void descontarCantidadLote(final Lote lote, final BigDecimal cantidad, final UnidadMedidaEnum unidad) {
        if (lote.getUnidadMedida() == unidad) {
            lote.setCantidadActual(lote.getCantidadActual().subtract(cantidad));
        } else {
            final BigDecimal cantidadConvertida = convertirCantidadEntreUnidades(
                unidad,
                cantidad,
                lote.getUnidadMedida());
            lote.setCantidadActual(lote.getCantidadActual().subtract(cantidadConvertida));
        }
    }

    /**
     * Actualiza el estado del lote y sus bultos según cantidades actuales.
     */
    void actualizarEstadosLoteYBultos(final LoteDTO loteDTO, final Lote lote) {
        final List<Integer> nroBultoList = loteDTO.getNroBultoList();

        for (int nroBulto : nroBultoList) {
            final Bulto bultoEntity = lote.getBultoByNro(nroBulto);
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
    }

    /**
     * Calcula la unidad de medida del movimiento (la mayor entre todos los bultos).
     */
    UnidadMedidaEnum calcularUnidadMedidaMovimiento(final LoteDTO loteDTO) {
        UnidadMedidaEnum uniMedidaMovimiento = loteDTO.getUnidadMedidaBultos().get(0);

        for (int i = 1; i < loteDTO.getCantidadesBultos().size(); i++) {
            uniMedidaMovimiento = obtenerMayorUnidadMedida(uniMedidaMovimiento, loteDTO.getUnidadMedidaBultos().get(i));
        }

        return uniMedidaMovimiento;
    }

    /**
     * Calcula la cantidad total del movimiento sumando todas las cantidades convertidas.
     */
    BigDecimal calcularCantidadTotalMovimiento(final LoteDTO loteDTO, final UnidadMedidaEnum uniMedidaMovimiento) {
        BigDecimal cantidad = BigDecimal.ZERO;

        for (int i = 0; i < loteDTO.getCantidadesBultos().size(); i++) {
            final BigDecimal montoBulto = convertirCantidadEntreUnidades(
                loteDTO.getUnidadMedidaBultos().get(i),
                loteDTO.getCantidadesBultos().get(i),
                uniMedidaMovimiento);
            cantidad = cantidad.add(montoBulto);
        }

        return cantidad;
    }

    /**
     * Agrega los detalles de movimiento por cada bulto muestreado.
     */
    void agregarDetallesMovimiento(final LoteDTO loteDTO, final Lote loteEntity, final Movimiento movimiento) {
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
    }
}
