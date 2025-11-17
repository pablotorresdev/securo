package com.mb.conitrack.service.cu.validator;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMenorUnidadMedida;

/**
 * Validador especializado para cantidades y bultos.
 * Maneja validaciones de:
 * - Cantidades iniciales
 * - Cantidades de movimientos
 * - Cantidades por bulto
 * - Suma de cantidades convertidas
 * - Distribución de bultos
 */
public class CantidadValidator {

    private CantidadValidator() {
        // Utility class, prevent instantiation
    }

    /** Valida cantidad y distribución de bultos (suma debe coincidir con total). */
    public static boolean validarBultos(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        return (loteDTO.getBultosTotales() == 1) ||
            (validarTipoDeDato(loteDTO, bindingResult) && validarSumaBultosConvertida(loteDTO, bindingResult));
    }

    /** Valida cantidad inicial (positiva, entera si UNIDAD, >= bultos). */
    public static boolean validarCantidadIngreso(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        BigDecimal cantidad = loteDTO.getCantidadInicial();
        if (cantidad == null) {
            bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial", "La cantidad no puede ser nula.");
            return false;
        }

        if (UnidadMedidaEnum.UNIDAD.equals(loteDTO.getUnidadMedida())) {
            if (cantidad.stripTrailingZeros().scale() > 0) {
                bindingResult.rejectValue(
                    "cantidadInicial",
                    "error.cantidadInicial",
                    "La cantidad debe ser un número entero positivo cuando la unidad es UNIDAD.");
                return false;
            }
            if (cantidad.compareTo(new BigDecimal(loteDTO.getBultosTotales())) < 0) {
                bindingResult.rejectValue(
                    "bultosTotales",
                    "error.bultosTotales",
                    "La cantidad de Unidades (" +
                        cantidad +
                        ") no puede ser menor a la cantidad de  Bultos totales: " +
                        loteDTO.getBultosTotales());
                return false;
            }
        }
        return true;
    }

    /** Valida cantidad de movimiento (positiva, compatible con unidad, no excede stock de bulto). */
    public static boolean validarCantidadesMovimiento(
            final MovimientoDTO dto,
            final Bulto bulto,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        final List<UnidadMedidaEnum> unidadesPorTipo = UnidadMedidaEnum.getUnidadesPorTipo(bulto.getUnidadMedida());

        if (!unidadesPorTipo.contains(dto.getUnidadMedida())) {
            bindingResult.rejectValue("unidadMedida", "", "Unidad no compatible con el producto.");
            return false;
        }

        if (dto.getCantidad() == null || dto.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            bindingResult.rejectValue("cantidad", "", "La cantidad debe ser mayor a 0.");
            return false;
        }

        BigDecimal cantidadConvertida = dto.getCantidad()
            .multiply(BigDecimal.valueOf(dto.getUnidadMedida().getFactorConversion() /
                bulto.getUnidadMedida().getFactorConversion()));

        if (cantidadConvertida.compareTo(bulto.getCantidadActual()) > 0) {
            bindingResult.rejectValue("cantidad", "", "La cantidad excede el stock disponible del bulto.");
            return false;
        }
        return true;
    }

    /** Valida cantidades por bulto (convertidas, positivas, no exceden stock disponible). */
    public static boolean validarCantidadesPorMedidas(
            final LoteDTO loteDTO,
            final Lote lote,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        // Debe venir al menos un movimiento
        final List<Integer> nroBultoList = loteDTO.getNroBultoList();
        final List<BigDecimal> cantidadesBultos = loteDTO.getCantidadesBultos();
        final List<UnidadMedidaEnum> unidadMedidaBultos = loteDTO.getUnidadMedidaBultos();

        if (cantidadesBultos == null || cantidadesBultos.isEmpty()) {
            bindingResult.rejectValue("cantidadesBultos", "", "Debe ingresar las cantidades a consumir");
            return false;
        }

        if (unidadMedidaBultos == null || unidadMedidaBultos.isEmpty()) {
            bindingResult.rejectValue("cantidadesBultos", "", "Debe ingresar las unidades de medida");
            return false;
        }

        for (int i = 0; i < nroBultoList.size(); i++) {
            final BigDecimal cantidaConsumoBulto = cantidadesBultos.get(i);
            if (cantidaConsumoBulto == null) {
                bindingResult.rejectValue("cantidadesBultos", "", "La cantidad no puede ser nula");
                return false;
            }
            if (BigDecimal.ZERO.compareTo(cantidaConsumoBulto) == 0) {
                continue;
            }
            if (BigDecimal.ZERO.compareTo(cantidaConsumoBulto) > 0) {
                bindingResult.rejectValue("cantidadesBultos", "", "La cantidad no puede ser negativa");
                return false;
            }

            final UnidadMedidaEnum uniMedidaConsumoBulto = unidadMedidaBultos.get(i);
            if (uniMedidaConsumoBulto == null) {
                bindingResult.rejectValue("cantidadesBultos", "", "Debe indicar la unidad");
                return false;
            }

            Bulto bultoEntity = lote.getBultoByNro(nroBultoList.get(i));

            if (bultoEntity == null) {
                bindingResult.rejectValue("cantidadesBultos", "", "Bulto no encontrado");
                return false;
            }

            if (bultoEntity.getUnidadMedida() == uniMedidaConsumoBulto) {
                if (cantidaConsumoBulto.compareTo(bultoEntity.getCantidadActual()) > 0) {
                    bindingResult.rejectValue(
                        "cantidadesBultos",
                        "",
                        "La cantidad ingresada (" +
                            cantidaConsumoBulto +
                            " " +
                            uniMedidaConsumoBulto +
                            ") no puede superar el stock actual del bulto " +
                            bultoEntity.getNroBulto() +
                            " (" +
                            bultoEntity.getCantidadActual() +
                            " " +
                            bultoEntity.getUnidadMedida() +
                            ")");
                    return false;
                }
            } else {
                UnidadMedidaEnum menorUnidadMedida = obtenerMenorUnidadMedida(
                    bultoEntity.getUnidadMedida(),
                    uniMedidaConsumoBulto);
                BigDecimal cantidadBultoNormalizada = convertirCantidadEntreUnidades(
                    uniMedidaConsumoBulto,
                    cantidaConsumoBulto,
                    menorUnidadMedida);
                BigDecimal cantidadLoteNormalizada = convertirCantidadEntreUnidades(
                    bultoEntity.getUnidadMedida(),
                    bultoEntity.getCantidadActual(),
                    menorUnidadMedida);
                if (cantidadBultoNormalizada.compareTo(cantidadLoteNormalizada) > 0) {
                    bindingResult.rejectValue(
                        "cantidadesBultos",
                        "",
                        "La cantidad ingresada (" +
                            cantidaConsumoBulto +
                            " " +
                            uniMedidaConsumoBulto +
                            ") no puede superar el stock actual del bulto " +
                            bultoEntity.getNroBulto() +
                            " (" +
                            bultoEntity.getCantidadActual() +
                            " " +
                            bultoEntity.getUnidadMedida() +
                            ")");
                    return false;
                }
            }
        }
        return true;
    }

    /** Valida suma de cantidades de bultos convertidas a unidad base (debe coincidir con total). */
    public static boolean validarSumaBultosConvertida(LoteDTO loteDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        List<BigDecimal> cantidades = loteDTO.getCantidadesBultos();
        List<UnidadMedidaEnum> unidades = loteDTO.getUnidadMedidaBultos();
        UnidadMedidaEnum unidadBase = loteDTO.getUnidadMedida();
        if (cantidades == null || unidades == null || cantidades.size() != unidades.size()) {
            bindingResult.rejectValue(
                "cantidadesBultos",
                "error.cantidadesBultos",
                "Datos incompletos o inconsistentes.");
            return false;
        }
        BigDecimal sumaConvertida = BigDecimal.ZERO;
        for (int i = 0; i < cantidades.size(); i++) {
            BigDecimal cantidad = cantidades.get(i);
            UnidadMedidaEnum unidadBulto = unidades.get(i);
            if (cantidad == null || unidadBulto == null) {
                continue;
            }

            //assert cantidad > 0
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue(
                    "cantidadesBultos",
                    "error.cantidadesBultos",
                    "La cantidad del Bulto " + (i + 1) + " debe ser mayor a 0.");
                return false;
            }

            double factor = unidadBulto.getFactorConversion() / unidadBase.getFactorConversion();
            BigDecimal cantidadConvertida = cantidad.multiply(BigDecimal.valueOf(factor));
            sumaConvertida = sumaConvertida.add(cantidadConvertida);
        }

        BigDecimal sumaRedondeada = sumaConvertida.setScale(6, RoundingMode.HALF_UP);
        BigDecimal totalEsperado = loteDTO.getCantidadInicial().setScale(6, RoundingMode.HALF_UP);

        if (sumaRedondeada.compareTo(totalEsperado) != 0) {
            bindingResult.rejectValue(
                "cantidadesBultos",
                "error.cantidadesBultos",
                "La suma de las cantidades individuales (" +
                    sumaRedondeada.stripTrailingZeros().toPlainString() +
                    " " +
                    unidadBase.getSimbolo() +
                    ") no coincide con la cantidad total (" +
                    totalEsperado.stripTrailingZeros().toPlainString() +
                    " " +
                    unidadBase.getSimbolo() +
                    ").");
            return false;
        }
        return true;
    }

    /** Valida tipo de dato por bulto (enteros para UNIDAD, cantidades no nulas). */
    public static boolean validarTipoDeDato(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        List<BigDecimal> cantidades = loteDTO.getCantidadesBultos();
        if (cantidades.isEmpty()) {
            bindingResult.rejectValue(
                "cantidadInicial",
                "error.cantidadInicial",
                "La cantidad del Lote no puede ser nula.");
            return false;
        }
        List<UnidadMedidaEnum> unidades = loteDTO.getUnidadMedidaBultos();
        if (unidades.isEmpty()) {
            bindingResult.rejectValue(
                "cantidadInicial",
                "error.cantidadInicial",
                "Las unidades de medida del Lote no pueden ser nulas.");
            return false;
        }
        boolean result = true;
        for (int i = 0; i < cantidades.size(); i++) {
            BigDecimal cantidad = cantidades.get(i);
            if (cantidad == null) {
                bindingResult.rejectValue(
                    "cantidadInicial",
                    "error.cantidadInicial",
                    "La cantidad del Bulto " + (i + 1) + " no puede ser nula.");
                result = false;
            } else {
                if (UnidadMedidaEnum.UNIDAD.equals(unidades.get(i))) {
                    if (cantidad.stripTrailingZeros().scale() > 0) {
                        bindingResult.rejectValue(
                            "cantidadInicial",
                            "error.cantidadInicial",
                            "La cantidad del Bulto " +
                                (i + 1) +
                                "  debe ser un número entero positivo cuando la unidad es UNIDAD.");
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    /** Valida unidad de medida para ventas (solo UNIDAD permitida). */
    public static boolean validarUnidadMedidaVenta(
            final LoteDTO loteDTO,
            final Lote lote,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (lote.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
            bindingResult.rejectValue(
                "cantidadesBultos",
                "error.cantidadesBultos",
                "La venta de producto solo es aplicable a UNIDADES");
            return false;
        }

        for (int i = 0; i < loteDTO.getUnidadMedidaBultos().size(); i++) {
            if (loteDTO.getUnidadMedidaBultos().get(i) != UnidadMedidaEnum.UNIDAD) {
                bindingResult.rejectValue(
                    "cantidadesBultos",
                    "error.cantidadesBultos",
                    "La venta de producto solo es aplicable a UNIDADES");
                return false;
            }
        }

        return true;
    }
}
