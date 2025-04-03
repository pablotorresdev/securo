package com.mb.conitrack.enums;

import java.math.BigDecimal;
import java.util.List;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;

import static com.mb.conitrack.enums.UnidadMedidaEnum.UNIDAD;
import static com.mb.conitrack.enums.UnidadMedidaEnum.getUnidadesPorTipo;

public class UnidadMedidaUtils {

    public static BigDecimal getCantidadForUnidadDeMedida(UnidadMedidaEnum unidadOrigen, BigDecimal cantidad, UnidadMedidaEnum unidadDestino) {
        if (unidadOrigen.equals(unidadDestino)) {
            return cantidad;
        }
        final double factorOrigen = unidadOrigen.getFactorConversion();
        final double factorDestino = unidadDestino.getFactorConversion();
        return cantidad.multiply(BigDecimal.valueOf(factorOrigen / factorDestino));
    }

    public static UnidadMedidaEnum getUnidadMedidaIdeal(UnidadMedidaEnum unidadMedida, BigDecimal cantidad) {
        if (cantidad == null || unidadMedida == null || UNIDAD.equals(unidadMedida)) {
            return unidadMedida;
        }

        List<UnidadMedidaEnum> unidadesCompatibles = getUnidadesPorTipo(unidadMedida);
        int indexActual = unidadesCompatibles.indexOf(unidadMedida);
        if ((cantidad.compareTo(BigDecimal.ONE) < 0) || ((cantidad.compareTo(BigDecimal.TEN) < 0 && cantidad.stripTrailingZeros().scale() > 2))) {
            for (int i = indexActual + 1; i < unidadesCompatibles.size(); i++) {
                UnidadMedidaEnum menor = unidadesCompatibles.get(i);
                double factor = unidadMedida.getFactorConversion() / menor.getFactorConversion();
                BigDecimal convertida = cantidad.multiply(BigDecimal.valueOf(factor)).setScale(4, BigDecimal.ROUND_HALF_UP);
                if (convertida.compareTo(BigDecimal.ONE) > 0 && convertida.stripTrailingZeros().scale() <= 2 || i == unidadesCompatibles.size() - 1) {
                    return menor;
                }
            }
        }

        final int potenciaBase10 = getPotenciaBase10(cantidad);
        if (potenciaBase10 > 2 && cantidad.stripTrailingZeros().scale() < 1) {
            for (int i = indexActual - 1; i >= 0; i--) {
                UnidadMedidaEnum mayor = unidadesCompatibles.get(i);
                double factor = unidadMedida.getFactorConversion() / mayor.getFactorConversion();
                BigDecimal convertida = cantidad.multiply(BigDecimal.valueOf(factor));
                if (convertida.compareTo(new BigDecimal(100)) < 0 && convertida.stripTrailingZeros().scale() <= 3 || i == 0) {
                    return mayor;
                }
            }
        }

        return unidadMedida;
    }

    public static int getPotenciaBase10(BigDecimal value) {
        if (value == null || BigDecimal.ZERO.compareTo(value) == 0) {
            return 0;
        }
        int scale = value.stripTrailingZeros().scale();
        int precision = value.stripTrailingZeros().precision();
        return precision - scale - 1;
    }

    public static BigDecimal calcularCantidadActual(final MovimientoDTO dto, final Lote lote) {
        final BigDecimal cantidadLote = lote.getCantidadActual();
        final double factorLote = lote.getUnidadMedida().getFactorConversion();
        final double factorDto = dto.getUnidadMedida().getFactorConversion();

        // Convertimos la cantidad del DTO a la unidad del lote
        BigDecimal cantidadDtoConvertida = dto.getCantidad()
            .multiply(BigDecimal.valueOf(factorDto / factorLote));

        return cantidadLote.subtract(cantidadDtoConvertida);
    }

}
