package com.mb.conitrack.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static com.mb.conitrack.enums.UnidadMedidaEnum.UNIDAD;
import static com.mb.conitrack.enums.UnidadMedidaEnum.getUnidadesPorTipo;

/** Conversión entre unidades de medida (masa, volumen, longitud, superficie). */
public class UnidadMedidaUtils {

    private UnidadMedidaUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** Convierte cantidad entre unidades compatibles usando factores de conversión. */
    public static BigDecimal convertirCantidadEntreUnidades(
        final UnidadMedidaEnum unidadOrigen,
        final BigDecimal cantidad,
        final UnidadMedidaEnum unidadDestino) {
        Objects.requireNonNull(unidadOrigen, "unidadOrigen cannot be null");
        Objects.requireNonNull(cantidad, "cantidad cannot be null");
        Objects.requireNonNull(unidadDestino, "unidadDestino cannot be null");

        if (unidadOrigen == unidadDestino) {
            return cantidad;
        }
        final double factorOrigen = unidadOrigen.getFactorConversion();
        final double factorDestino = unidadDestino.getFactorConversion();
        return cantidad.multiply(BigDecimal.valueOf(factorOrigen / factorDestino));
    }

    /** Obtiene la unidad menor (menor factor de conversión) entre dos unidades del mismo tipo. */
    public static UnidadMedidaEnum obtenerMenorUnidadMedida(
        final UnidadMedidaEnum unidadUno,
        final UnidadMedidaEnum unidadDos) {
        Objects.requireNonNull(unidadUno, "unidadUno cannot be null");
        Objects.requireNonNull(unidadDos, "unidadDos cannot be null");

        if (unidadUno == unidadDos) {
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

    /** Obtiene la unidad mayor (mayor factor de conversión) entre dos unidades del mismo tipo. */
    public static UnidadMedidaEnum obtenerMayorUnidadMedida(
        final UnidadMedidaEnum unidadUno,
        final UnidadMedidaEnum unidadDos) {
        Objects.requireNonNull(unidadUno, "unidadUno cannot be null");
        Objects.requireNonNull(unidadDos, "unidadDos cannot be null");

        if (unidadUno == unidadDos) {
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

    /** Calcula orden de magnitud base 10 (ej: 1234 -> 3). */
    public static int ordenDeMagnitudBase10(BigDecimal value) {
        if (value == null || BigDecimal.ZERO.compareTo(value) == 0) {
            return 0;
        }
        int scale = value.stripTrailingZeros().scale();
        int precision = value.stripTrailingZeros().precision();
        return precision - scale - 1;
    }

    /** Resta cantidad de movimiento a lote (convirtiendo unidades automáticamente). */
    public static BigDecimal restarMovimientoConvertido(final MovimientoDTO dto, final Lote lote) {
        Objects.requireNonNull(dto, "dto cannot be null");
        Objects.requireNonNull(lote, "lote cannot be null");

        final BigDecimal cantidadLote = lote.getCantidadActual();
        final double factorLote = lote.getUnidadMedida().getFactorConversion();
        final double factorDto = dto.getUnidadMedida().getFactorConversion();

        // Convert the DTO quantity to the lot's unit
        BigDecimal cantidadDtoConvertida = dto.getCantidad().multiply(BigDecimal.valueOf(factorDto / factorLote));

        return cantidadLote.subtract(cantidadDtoConvertida);
    }

    /** Suma cantidad de movimiento a lote (convirtiendo unidades automáticamente). */
    public static BigDecimal sumarMovimientoConvertido(final MovimientoDTO dto, final Lote lote) {
        Objects.requireNonNull(dto, "dto cannot be null");
        Objects.requireNonNull(lote, "lote cannot be null");

        final BigDecimal cantidadLote = lote.getCantidadActual();
        final double factorLote = lote.getUnidadMedida().getFactorConversion();
        final double factorDto = dto.getUnidadMedida().getFactorConversion();

        // Convert the DTO quantity to the lot's unit
        BigDecimal cantidadDtoConvertida = dto.getCantidad().multiply(BigDecimal.valueOf(factorDto / factorLote));

        return cantidadLote.add(cantidadDtoConvertida);
    }

    /**
     * Subtracts a movement quantity from a bulto's current quantity, converting units as needed.
     * <p>
     * The movement's quantity is converted from its unit to the bulto's unit before subtraction.
     * </p>
     *
     * @param dto   Movement to apply, expressed in its own unit (must not be null)
     * @param bulto Affected bulto with its current quantity and unit (must not be null)
     * @return New resulting quantity for the bulto after the movement
     * @throws NullPointerException if any parameter is null
     */
    public static BigDecimal restarMovimientoConvertido(final MovimientoDTO dto, final Bulto bulto) {
        Objects.requireNonNull(dto, "dto cannot be null");
        Objects.requireNonNull(bulto, "bulto cannot be null");

        final BigDecimal cantidadBulto = bulto.getCantidadActual();
        final double factorBulto = bulto.getUnidadMedida().getFactorConversion();
        final double factorDto = dto.getUnidadMedida().getFactorConversion();

        // Convert the DTO quantity to the bulto's unit
        BigDecimal cantidadDtoConvertida = dto.getCantidad().multiply(BigDecimal.valueOf(factorDto / factorBulto));

        return cantidadBulto.subtract(cantidadDtoConvertida);
    }

    /**
     * Adds a movement quantity to a bulto's current quantity, converting units as needed.
     * <p>
     * The movement's quantity is converted from its unit to the bulto's unit before addition.
     * </p>
     *
     * @param dto   Movement to apply, expressed in its own unit (must not be null)
     * @param bulto Affected bulto with its current quantity and unit (must not be null)
     * @return New resulting quantity for the bulto after the movement
     * @throws NullPointerException if any parameter is null
     */
    public static BigDecimal sumarMovimientoConvertido(final MovimientoDTO dto, final Bulto bulto) {
        Objects.requireNonNull(dto, "dto cannot be null");
        Objects.requireNonNull(bulto, "bulto cannot be null");

        final BigDecimal cantidadBulto = bulto.getCantidadActual();
        final double factorBulto = bulto.getUnidadMedida().getFactorConversion();
        final double factorDto = dto.getUnidadMedida().getFactorConversion();

        // Convert the DTO quantity to the bulto's unit
        BigDecimal cantidadDtoConvertida = dto.getCantidad().multiply(BigDecimal.valueOf(factorDto / factorBulto));

        return cantidadBulto.add(cantidadDtoConvertida);
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
