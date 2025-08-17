package com.mb.conitrack.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import lombok.Getter;

import static com.mb.conitrack.enums.UnidadMedidaEnum.UNIDAD;
import static com.mb.conitrack.enums.UnidadMedidaEnum.getUnidadesPorTipo;

/**
 * Utilidades para el manejo y conversión entre distintas unidades de medida.
 */
public class UnidadMedidaUtils {

    @Getter
    private static final UnidadMedidaUtils Instance = new UnidadMedidaUtils();

    private UnidadMedidaUtils() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Convierte una cantidad desde una unidad de origen a una unidad de destino compatible.
     *
     * @param unidadOrigen  Unidad en la que está expresada originalmente la cantidad.
     * @param cantidad      Cantidad a convertir.
     * @param unidadDestino Unidad a la cual se desea convertir la cantidad.
     *
     * @return Cantidad convertida a la unidad de destino.
     */
    public static BigDecimal convertirCantidadEntreUnidades(
        UnidadMedidaEnum unidadOrigen,
        BigDecimal cantidad,
        UnidadMedidaEnum unidadDestino) {
        if (unidadOrigen == unidadDestino) {
            return cantidad;
        }
        final double factorOrigen = unidadOrigen.getFactorConversion();
        final double factorDestino = unidadDestino.getFactorConversion();
        return cantidad.multiply(BigDecimal.valueOf(factorOrigen / factorDestino));
    }

    /**
     * Devuelve la unidad de medida más pequeña (la de menor magnitud) entre dos unidades del mismo tipo.
     *
     * @param unidadUno Primera unidad de medida a comparar.
     * @param unidadDos Segunda unidad de medida a comparar.
     *
     * @return La unidad de menor magnitud (es decir, la que tiene el menor factor de conversión).
     *
     * @throws IllegalArgumentException si las unidades no pertenecen al mismo tipo (masa, volumen, etc.).
     */
    public static UnidadMedidaEnum obtenerMenorUnidadMedida(UnidadMedidaEnum unidadUno, UnidadMedidaEnum unidadDos) {
        if (unidadUno==unidadDos) {
            return unidadUno;
        }
        if (!unidadUno.getTipo().equals(unidadDos.getTipo())) {
            throw new IllegalArgumentException("Las unidades de medida no son compatibles");
        }
        if (unidadUno.getFactorConversion() < unidadDos.getFactorConversion()) {
            return unidadUno;
        } else {
            return unidadDos;
        }
    }

    /**
     * Devuelve la unidad de medida más grande (la de mayor magnitud) entre dos unidades del mismo tipo.
     *
     * @param unidadUno Primera unidad de medida a comparar.
     * @param unidadDos Segunda unidad de medida a comparar.
     *
     * @return La unidad de mayor magnitud (es decir, la que tiene el mayor factor de conversión).
     *
     * @throws IllegalArgumentException si las unidades no pertenecen al mismo tipo (masa, volumen, etc.).
     */
    public static UnidadMedidaEnum obtenerMayorUnidadMedida(UnidadMedidaEnum unidadUno, UnidadMedidaEnum unidadDos) {
        if (unidadUno==unidadDos) {
            return unidadUno;
        }
        if (!unidadUno.getTipo().equals(unidadDos.getTipo())) {
            throw new IllegalArgumentException("Las unidades de medida no son compatibles");
        }
        if (unidadUno.getFactorConversion() > unidadDos.getFactorConversion()) {
            return unidadUno;
        } else {
            return unidadDos;
        }
    }

    /**
     * Calcula la "potencia" base 10 de una cantidad, útil para evaluar magnitudes en logaritmo base 10 sin usar
     * funciones logarítmicas.
     * <p>
     * Por ejemplo, para 1234 -> devuelve 3 (es decir, 10^3).
     *
     * @param value Valor a evaluar.
     *
     * @return Potencia base 10 correspondiente al orden de magnitud de la cantidad.
     */
    public static int ordenDeMagnitudBase10(BigDecimal value) {
        if (value == null || BigDecimal.ZERO.compareTo(value) == 0) {
            return 0;
        }
        int scale = value.stripTrailingZeros().scale();
        int precision = value.stripTrailingZeros().precision();
        return precision - scale - 1;
    }

    /**
     * Calcula la nueva cantidad actual del lote luego de aplicar el movimiento especificado. Internamente convierte la
     * cantidad del movimiento a la unidad del lote.
     *
     * @param dto  Movimiento a aplicar, expresado en su propia unidad.
     * @param lote Lote afectado, con su cantidad y unidad actual.
     *
     * @return Nueva cantidad resultante del lote después del movimiento.
     */
    public static BigDecimal restarMovimientoConvertido(final MovimientoDTO dto, final Lote lote) {
        final BigDecimal cantidadLote = lote.getCantidadActual();
        final double factorLote = lote.getUnidadMedida().getFactorConversion();
        final double factorDto = dto.getUnidadMedida().getFactorConversion();

        // Convertimos la cantidad del DTO a la unidad del lote
        BigDecimal cantidadDtoConvertida = dto.getCantidad().multiply(BigDecimal.valueOf(factorDto / factorLote));

        return cantidadLote.subtract(cantidadDtoConvertida);
    }

    /**
     * Calcula la nueva cantidad actual del lote luego de aplicar el movimiento especificado. Internamente convierte la
     * cantidad del movimiento a la unidad del lote.
     *
     * @param dto   Movimiento a aplicar, expresado en su propia unidad.
     * @param bulto Bulto afectado, con su cantidad y unidad actual.
     *
     * @return Nueva cantidad resultante del lote después del movimiento.
     */
    public static BigDecimal restarMovimientoConvertido(final MovimientoDTO dto, final Bulto bulto) {
        final BigDecimal cantidadLote = bulto.getCantidadActual();
        final double factorLote = bulto.getUnidadMedida().getFactorConversion();
        final double factorDto = dto.getUnidadMedida().getFactorConversion();

        // Convertimos la cantidad del DTO a la unidad del lote
        BigDecimal cantidadDtoConvertida = dto.getCantidad().multiply(BigDecimal.valueOf(factorDto / factorLote));

        return cantidadLote.subtract(cantidadDtoConvertida);
    }

    /**
     * Determina la unidad de medida más adecuada para representar una cantidad determinada, considerando la legibilidad
     * del número (evita cantidades muy pequeñas o muy grandes).
     *
     * @param unidadMedida Unidad actual asociada a la cantidad.
     * @param cantidad     Cantidad que se desea evaluar.
     *
     * @return Unidad de medida ideal para representar la cantidad, sin perder precisión.
     */
    public static UnidadMedidaEnum sugerirUnidadParaCantidad(UnidadMedidaEnum unidadMedida, BigDecimal cantidad) {
        if (cantidad == null || unidadMedida == null || UNIDAD.equals(unidadMedida)) {
            return unidadMedida;
        }

        List<UnidadMedidaEnum> unidadesCompatibles = getUnidadesPorTipo(unidadMedida);
        int indexActual = unidadesCompatibles.indexOf(unidadMedida);

        // Si la cantidad es pequeña o tiene demasiados decimales, se busca una unidad menor
        if ((cantidad.compareTo(BigDecimal.ONE) < 0) ||
            ((cantidad.compareTo(BigDecimal.TEN) < 0 && cantidad.stripTrailingZeros().scale() > 2))) {
            for (int i = indexActual + 1; i < unidadesCompatibles.size(); i++) {
                UnidadMedidaEnum menor = unidadesCompatibles.get(i);
                double factor = unidadMedida.getFactorConversion() / menor.getFactorConversion();
                BigDecimal convertida = cantidad.multiply(BigDecimal.valueOf(factor)).setScale(4, RoundingMode.HALF_UP);
                if (convertida.compareTo(BigDecimal.ONE) > 0 && convertida.stripTrailingZeros().scale() <= 2 ||
                    i == unidadesCompatibles.size() - 1) {
                    return menor;
                }
            }
        }

        // Si la cantidad es muy grande y sin decimales, se busca una unidad mayor
        final int potenciaBase10 = ordenDeMagnitudBase10(cantidad);
        if (potenciaBase10 > 2 && cantidad.stripTrailingZeros().scale() < 1) {
            for (int i = indexActual - 1; i >= 0; i--) {
                UnidadMedidaEnum mayor = unidadesCompatibles.get(i);
                double factor = unidadMedida.getFactorConversion() / mayor.getFactorConversion();
                BigDecimal convertida = cantidad.multiply(BigDecimal.valueOf(factor));
                if (convertida.compareTo(new BigDecimal(100)) < 0 && convertida.stripTrailingZeros().scale() <= 3 ||
                    i == 0) {
                    return mayor;
                }
            }
        }

        return unidadMedida;
    }

}
