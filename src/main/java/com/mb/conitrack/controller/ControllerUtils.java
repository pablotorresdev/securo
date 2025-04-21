package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.enums.UnidadMedidaEnum;

public class ControllerUtils {

    public static void validateCantidadIngreso(final LoteDTO loteDTO, final BindingResult bindingResult) {
        BigDecimal cantidad = loteDTO.getCantidadInicial();
        if (cantidad == null) {
            bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial", "La cantidad no puede ser nula.");
        } else {
            if (UnidadMedidaEnum.UNIDAD.equals(loteDTO.getUnidadMedida())) {
                if (cantidad.stripTrailingZeros().scale() > 0) {
                    bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial",
                        "La cantidad debe ser un número entero positivo cuando la unidad es UNIDAD.");
                }
                if (cantidad.compareTo(new BigDecimal(loteDTO.getBultosTotales())) < 0) {
                    bindingResult.rejectValue("bultosTotales", "error.bultosTotales",
                        "La cantidad de Unidades (" + cantidad + ") no puede ser menor a la cantidad de  Bultos totales: " + loteDTO.getBultosTotales());
                }
            }
        }
    }

    public static void validateFechasProveedor(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (loteDTO.getFechaReanalisisProveedor() != null && loteDTO.getFechaVencimientoProveedor() != null) {
            if (loteDTO.getFechaReanalisisProveedor().isAfter(loteDTO.getFechaVencimientoProveedor())) {
                bindingResult.rejectValue("fechaReanalisisProveedor", "error.fechaReanalisisProveedor",
                    "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
            }
        }
    }

    public static void validarBultos(final LoteDTO loteDTO, final BindingResult bindingResult) {
        // Si se ingresan más de 1 bulto, se valida la distribución
        if (loteDTO.getBultosTotales() > 1) {
            validarTipoDeDato(loteDTO, bindingResult);
            validarSumaBultosConvertida(loteDTO, bindingResult);
        } else {
            loteDTO.setNroBulto(1);
        }
    }

    private static void validarTipoDeDato(final LoteDTO loteDTO, final BindingResult bindingResult) {
        List<BigDecimal> cantidades = loteDTO.getCantidadesBultos();
        List<UnidadMedidaEnum> unidades = loteDTO.getUnidadMedidaBultos();
        for (int i = 0; i < cantidades.size(); i++) {
            BigDecimal cantidad = cantidades.get(i);
            if (cantidad == null) {
                bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial", "La cantidad del Bulto " + (i + 1) + " no puede ser nula.");
            } else {
                if (UnidadMedidaEnum.UNIDAD.equals(unidades.get(i))) {
                    if (cantidad.stripTrailingZeros().scale() > 0) {
                        bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial",
                            "La cantidad del Bulto " + (i + 1) + "  debe ser un número entero positivo cuando la unidad es UNIDAD.");
                    }
                }
            }
        }
    }

    private static void validarSumaBultosConvertida(LoteDTO loteDTO, BindingResult bindingResult) {
        List<BigDecimal> cantidades = loteDTO.getCantidadesBultos();
        List<UnidadMedidaEnum> unidades = loteDTO.getUnidadMedidaBultos();
        UnidadMedidaEnum unidadBase = loteDTO.getUnidadMedida();

        if (cantidades == null || unidades == null || cantidades.size() != unidades.size()) {
            bindingResult.rejectValue("cantidadesBultos", "error.cantidadesBultos", "Datos incompletos o inconsistentes.");
            return;
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
                bindingResult.rejectValue("cantidadesBultos", "error.cantidadesBultos", "La cantidad del Bulto " + (i + 1) + " debe ser mayor a 0.");
                return;
            }

            double factor = unidadBulto.getFactorConversion() / unidadBase.getFactorConversion();
            BigDecimal cantidadConvertida = cantidad.multiply(BigDecimal.valueOf(factor));
            sumaConvertida = sumaConvertida.add(cantidadConvertida);
        }

        //TODO: ver tema suma de cantidades
        // Redondear la suma a 3 decimales para comparar y mostrar
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
                    ")."
            );
        }
    }

}
