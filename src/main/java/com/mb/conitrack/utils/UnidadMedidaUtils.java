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

/**
 * Utility class for handling and converting between different units of measure.
 * <p>
 * This class provides static methods for:
 * <ul>
 *   <li>Converting quantities between compatible units (kg ↔ g, L ↔ mL, etc.)</li>
 *   <li>Finding minimum/maximum units between two compatible measures</li>
 *   <li>Calculating order of magnitude for BigDecimal values</li>
 *   <li>Applying movement conversions to lots and bultos</li>
 *   <li>Suggesting optimal units for displaying quantities</li>
 * </ul>
 * </p>
 * <p>
 * All conversions use the {@link UnidadMedidaEnum#getFactorConversion()} for calculations.
 * Note that conversions may introduce small floating-point precision errors due to
 * internal use of double arithmetic.
 * </p>
 * <p>
 * This is a utility class and cannot be instantiated.
 * </p>
 *
 * @see UnidadMedidaEnum
 */
public class UnidadMedidaUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     *
     * @throws UnsupportedOperationException always thrown when called
     */
    private UnidadMedidaUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a quantity from a source unit to a compatible target unit.
     * <p>
     * If the units are the same, returns the quantity unchanged. Otherwise, performs
     * conversion using the conversion factors from both units.
     * </p>
     *
     * @param unidadOrigen  Source unit in which the quantity is expressed (must not be null)
     * @param cantidad      Quantity to convert (must not be null)
     * @param unidadDestino Target unit to convert to (must not be null)
     * @return Quantity converted to the target unit
     * @throws NullPointerException if any parameter is null
     */
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

    /**
     * Returns the smaller (lower magnitude) unit between two units of the same type.
     * <p>
     * The smaller unit is the one with the lower conversion factor (e.g., mg is smaller than g).
     * </p>
     *
     * @param unidadUno First unit to compare (must not be null)
     * @param unidadDos Second unit to compare (must not be null)
     * @return The unit with lower magnitude (lower conversion factor)
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if the units are not of the same type (mass, volume, etc.)
     */
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

    /**
     * Returns the larger (higher magnitude) unit between two units of the same type.
     * <p>
     * The larger unit is the one with the higher conversion factor (e.g., kg is larger than g).
     * </p>
     *
     * @param unidadUno First unit to compare (must not be null)
     * @param unidadDos Second unit to compare (must not be null)
     * @return The unit with higher magnitude (higher conversion factor)
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if the units are not of the same type (mass, volume, etc.)
     */
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
     * Subtracts a movement quantity from a lot's current quantity, converting units as needed.
     * <p>
     * The movement's quantity is converted from its unit to the lot's unit before subtraction.
     * </p>
     *
     * @param dto  Movement to apply, expressed in its own unit (must not be null)
     * @param lote Affected lot with its current quantity and unit (must not be null)
     * @return New resulting quantity for the lot after the movement
     * @throws NullPointerException if any parameter is null
     */
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

    /**
     * Adds a movement quantity to a lot's current quantity, converting units as needed.
     * <p>
     * The movement's quantity is converted from its unit to the lot's unit before addition.
     * </p>
     *
     * @param dto  Movement to apply, expressed in its own unit (must not be null)
     * @param lote Affected lot with its current quantity and unit (must not be null)
     * @return New resulting quantity for the lot after the movement
     * @throws NullPointerException if any parameter is null
     */
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
