package com.mb.conitrack.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.validation.BindingResult;
import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;

import jakarta.validation.Valid;
import lombok.Getter;

import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMenorUnidadMedida;

public class ControllerUtils {

    @Getter
    private static final ControllerUtils Instance = new ControllerUtils();

    private ControllerUtils() {
        // Utility class
    }

    public static List<String> getCountryList() {
        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        for (String code : countryCodes) {
            Locale locale = new Locale("", code);
            countries.add(locale.getDisplayCountry());
        }
        countries.sort(String::compareTo);
        return countries;
    }

    public static boolean populateAvailableLoteListByCodigoInterno(
        final List<Lote> lotesList,
        String codigoInternoLote,
        BindingResult bindingResult,
        final LoteService loteService) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        final List<Lote> loteListByCodigoInterno = loteService.findLoteListByCodigoInterno(codigoInternoLote)
            .stream()
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
        if (loteListByCodigoInterno.isEmpty()) {
            bindingResult.rejectValue("codigoInternoLote", "Lote inexistente.");
            return false;
        }
        lotesList.addAll(loteListByCodigoInterno);
        return true;
    }

    public boolean validarBultos(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        return (loteDTO.getBultosTotales() == 1) || (validarTipoDeDato(loteDTO, bindingResult) &&
            validarSumaBultosConvertida(loteDTO, bindingResult));
    }

    public boolean validarCantidadesMovimiento(
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

    public boolean validarCantidadesMovimiento(
        final MovimientoDTO dto,
        final Lote lote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        final List<UnidadMedidaEnum> unidadesPorTipo = UnidadMedidaEnum.getUnidadesPorTipo(lote.getUnidadMedida());

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
                lote.getUnidadMedida().getFactorConversion()));

        if (cantidadConvertida.compareTo(lote.getCantidadActual()) > 0) {
            bindingResult.rejectValue("cantidad", "", "La cantidad excede el stock disponible del lote.");
            return false;
        }
        return true;
    }

    public static boolean validarCantidadesPorMedidas(
        final LoteDTO loteDTO,
        final List<Lote> lotes,
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
            return true;
        }

        if (unidadMedidaBultos == null || unidadMedidaBultos.isEmpty()) {
            bindingResult.rejectValue("cantidadesBultos", "", "Debe ingresar las unidades de medida");
            return true;
        }

        for (int i = 0; i < nroBultoList.size(); i++) {
            final BigDecimal cantidadBulto = cantidadesBultos.get(i);
            if (cantidadBulto == null) {
                bindingResult.rejectValue("cantidadesBultos", "", "La cantidad no puede ser nula");
                return true;
            }
            if (BigDecimal.ZERO.compareTo(cantidadBulto) == 0) {
                continue;
            }
            if (BigDecimal.ZERO.compareTo(cantidadBulto) <= 0) {
                bindingResult.rejectValue("cantidadesBultos", "", "La cantidad no puede ser negativa");
                return true;
            }

            final UnidadMedidaEnum uniMedidaBulto = unidadMedidaBultos.get(i);
            if (uniMedidaBulto == null) {
                bindingResult.rejectValue("cantidadesBultos", "", "Debe indicar la unidad");
                return true;
            }
            final Integer nroBulto = nroBultoList.get(i);
            final Lote lote = lotes.stream()
                //.filter(l -> l.getNroBulto().equals(nroBulto))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado para el número de bulto: " +
                    nroBulto));
            if (lote.getUnidadMedida() == uniMedidaBulto) {
                if (cantidadBulto.compareTo(lote.getCantidadActual()) > 0) {
                    bindingResult.rejectValue("cantidadesBultos", "", "La cantidad no puede superar el stock actual");
                    return true;
                }
            } else {
                UnidadMedidaEnum menorUnidadMedida = obtenerMenorUnidadMedida(lote.getUnidadMedida(), uniMedidaBulto);
                BigDecimal cantidadBultoNormalizada = convertirCantidadEntreUnidades(
                    uniMedidaBulto,
                    cantidadBulto,
                    menorUnidadMedida);
                BigDecimal cantidadLoteNormalizada = convertirCantidadEntreUnidades(
                    lote.getUnidadMedida(),
                    lote.getCantidadActual(),
                    menorUnidadMedida);
                if (cantidadBultoNormalizada.compareTo(cantidadLoteNormalizada) > 0) {
                    bindingResult.rejectValue("cantidadesBultos", "", "La cantidad no puede superar el stock actual");
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean validarContraFechasProveedor(
        final MovimientoDTO movimientoDTO,
        Lote lote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        // 1 La fecha de vencimiento de QC no puede ser posterior a la fecha de vencimiento del proveedor
        if (movimientoDTO.getFechaVencimiento() != null &&
            lote.getFechaVencimientoProveedor() != null &&
            movimientoDTO.getFechaVencimiento().isAfter(lote.getFechaVencimientoProveedor())) {
            bindingResult.rejectValue(
                "fechaVencimiento",
                "",
                "La fecha de vencimiento no puede ser posterior a la fecha de vencimiento del proveedor");
            return false;
        }

        if (movimientoDTO.getFechaReanalisis() != null) {
            long analisisAprobados = lote.getAnalisisList()
                .stream()
                .filter(a -> a.getDictamen() == DictamenEnum.APROBADO)
                .count();
            if (lote.getFechaVencimientoProveedor() != null &&
                movimientoDTO.getFechaReanalisis().isAfter(lote.getFechaVencimientoProveedor())) {
                bindingResult.rejectValue(
                    "fechaReanalisis",
                    "",
                    "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento del proveedor: " +
                        lote.getFechaVencimientoProveedor());
                return false;
            }
            if (analisisAprobados == 0) {
                // - Si el Lote NO fue analizado antes con dictamen APROBADO => la fechaReanálisis no puede pasar la fechaReanálisisProveedor NI la fechaVencimientoProveedor
                if (lote.getFechaReanalisisProveedor() != null &&
                    movimientoDTO.getFechaReanalisis().isAfter(lote.getFechaReanalisisProveedor())) {
                    bindingResult.rejectValue(
                        "fechaReanalisis",
                        "",
                        "La primera fecha de reanálisis no puede ser posterior a la fecha de reanálisis del proveedor: " +
                            lote.getFechaReanalisisProveedor());
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean validarDatosMandatoriosResultadoAnalisisInput(
        final MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        // Verificamos que nroAnalisis no sea vacío
        if (StringUtils.isEmpty(movimientoDTO.getNroAnalisis())) {
            bindingResult.rejectValue("nroAnalisis", "", "El Nro de Análisis es obligatorio");
            return false;
        }

        // Dictamen Final no nulo
        if (movimientoDTO.getDictamenFinal() == null) {
            bindingResult.rejectValue("dictamenFinal", "", "Debe ingresar un Resultado");
            return false;
        }

        // Fecha Realizado Análisis no nula
        if (movimientoDTO.getFechaRealizadoAnalisis() == null) {
            bindingResult.rejectValue(
                "fechaRealizadoAnalisis",
                "",
                "Debe ingresar la fecha en la que se realizó el análisis");
            return false;
        }
        return true;
    }

    public static boolean validarDatosResultadoAnalisisAprobadoInput(
        final MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (DictamenEnum.APROBADO != movimientoDTO.getDictamenFinal()) {
            return true;
        }
        // Al menos una de las fechas de reanálisis o vencimiento debe ser ingresada
        if (movimientoDTO.getFechaVencimiento() == null && movimientoDTO.getFechaReanalisis() == null) {
            bindingResult.rejectValue("fechaVencimiento", "", "Debe ingresar una fecha de Re Análisis o Vencimiento");
            return false;
        }

        // La fecha de reanálisis no puede ser posterior a la fecha de vencimiento
        if (movimientoDTO.getFechaVencimiento() != null &&
            movimientoDTO.getFechaReanalisis() != null &&
            movimientoDTO.getFechaReanalisis().isAfter(movimientoDTO.getFechaVencimiento())) {
            bindingResult.rejectValue(
                "fechaReanalisis",
                "",
                "La fecha de reanalisis no puede ser posterior a la fecha de vencimiento");
            return false;
        }

        // El título es obligatorio y no puede ser mayor al 100%
        if (movimientoDTO.getTitulo() == null) {
            bindingResult.rejectValue("titulo", "", "Debe ingresar el valor de título del Análisis");
            return false;
        } else if (movimientoDTO.getTitulo().compareTo(BigDecimal.valueOf(100)) > 0) {
            bindingResult.rejectValue("titulo", "", "El título no puede ser mayor al 100%");
            return false;
        }
        if (movimientoDTO.getTitulo().compareTo(BigDecimal.valueOf(0)) <= 0) {
            bindingResult.rejectValue("titulo", "", "El título no puede ser menor o igual a 0");
            return false;
        }
        return true;
    }

    public static boolean validarExisteMuestreoParaAnalisis(
        final MovimientoDTO movimientoDTO,
        final List<Lote> lotesList,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        boolean existeMuestreo = false;
        //Al menos necesitamos haber hecho un muestreo para poder hacer un análisis
        for (Lote lote : lotesList) {
            existeMuestreo |= lote.getMovimientos()
                .stream()
                .anyMatch(m -> m.getTipoMovimiento() == TipoMovimientoEnum.BAJA &&
                    m.getMotivo() == MotivoEnum.MUESTREO &&
                    movimientoDTO.getNroAnalisis().equals(m.getNroAnalisis()));
        }

        if (!existeMuestreo) {
            bindingResult.rejectValue(
                "nroAnalisis",
                "",
                "No se encontró un MUESTREO realizado para ese Nro de Análisis " + movimientoDTO.getNroAnalisis());
            return false;
        }
        return true;
    }

    public static boolean validarFechaEgresoLoteDtoPosteriorLote(
        final LoteDTO dto,
        final Lote lote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaEgreso() != null && dto.getFechaEgreso().isBefore(lote.getFechaIngreso())) {
            bindingResult.rejectValue(
                "fechaMovimiento",
                "",
                "La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
            return false;
        }
        return true;
    }

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
                    ").");
            return false;
        }
        return true;
    }

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

    public boolean validateCantidadIngreso(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        BigDecimal cantidad = loteDTO.getCantidadInicial();
        if (cantidad == null) {
            bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial", "La cantidad no puede ser nula.");
            return false;
        } else {
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
        }
        return true;
    }

    public boolean validateFechasProveedor(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (loteDTO.getFechaReanalisisProveedor() != null && loteDTO.getFechaVencimientoProveedor() != null) {
            if (loteDTO.getFechaReanalisisProveedor().isAfter(loteDTO.getFechaVencimientoProveedor())) {
                bindingResult.rejectValue(
                    "fechaReanalisisProveedor",
                    "error.fechaReanalisisProveedor",
                    "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
                return false;
            }
        }
        return true;
    }

    public Lote getLoteByCodigoInterno(
        String codigoInternoLote,
        BindingResult bindingResult,
        final LoteService loteService) {
        if (bindingResult.hasErrors()) {
            return null;
        }

        final Optional<Lote> loteByCodigoInterno = loteService.findLoteByCodigoInterno(codigoInternoLote);
        if (!loteByCodigoInterno.isPresent()) {
            bindingResult.rejectValue("codigoInternoLote", "","Lote bloqueado.");
            return null;
        }
        return loteByCodigoInterno.get();
    }

    public boolean populateLoteListByCodigoInterno(
        final List<Lote> lotesList,
        String codigoInternoLote,
        BindingResult bindingResult,
        final LoteService loteService) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        final List<Lote> loteListByCodigoInterno = loteService.findLoteListByCodigoInterno(codigoInternoLote);
        if (loteListByCodigoInterno.isEmpty()) {
            bindingResult.rejectValue("codigoInternoLote", "","Lote bloqueado.");
            return false;
        }
        lotesList.addAll(loteListByCodigoInterno);
        return true;
    }

    public boolean validarFechaAnalisisPosteriorIngresoLote(
        final MovimientoDTO dto,
        final Lote lote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaRealizadoAnalisis() != null &&
            dto.getFechaRealizadoAnalisis().isBefore(lote.getFechaIngreso())) {
            bindingResult.rejectValue(
                "fechaRealizadoAnalisis",
                "",
                "La fecha de realizado el analisis no puede ser anterior a la fecha de ingreso del lote");
            return false;
        }
        return true;
    }

    public boolean validarFechaMovimientoPosteriorIngresoLote(
        final MovimientoDTO dto,
        final Lote lote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaMovimiento().isBefore(lote.getFechaIngreso())) {
            bindingResult.rejectValue(
                "fechaMovimiento",
                "",
                "La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
            return false;
        }
        return true;
    }

    public boolean validarNroAnalisisNotNull(final MovimientoDTO movimientoDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (movimientoDTO.getNroAnalisis()==null) {
            bindingResult.rejectValue("nroAnalisis", "nroAnalisis.nulo","Nro de analisis no puede ser nulo");
            return false;
        }
        return true;
    }

    public boolean validarValorTitulo(
        final MovimientoDTO movimientoDTO,
        final List<Lote> lotesList,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        // (5) El valor del título no puede ser mayor al valor del título del último análisis aprobado
        //     (si existe un último análisis con dictamen APROBADO)
        Analisis ultimoAprobado = lotesList.get(0)
            .getAnalisisList()
            .stream()
            .filter(a -> a.getDictamen() == DictamenEnum.APROBADO && a.getTitulo() != null)
            .max(Comparator.comparing(Analisis::getFechaYHoraCreacion))
            .orElse(null);

        if (ultimoAprobado != null && movimientoDTO.getTitulo().compareTo(ultimoAprobado.getTitulo()) > 0) {
            bindingResult.rejectValue(
                "titulo",
                "",
                "El valor del título no puede ser mayor al del último análisis aprobado (" +
                    ultimoAprobado.getTitulo() +
                    ")");
            return false;
        }
        return true;
    }

    public boolean validarNroAnalisisUnico(final @Valid MovimientoDTO movimientoDTO, final BindingResult bindingResult, AnalisisService analisisService) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        final Analisis analisis = analisisService.findByNroAnalisis(movimientoDTO.getNroAnalisis());
        if (analisis!=null) {
            bindingResult.rejectValue("nroAnalisis", "nroAnalisis.duplicado","Nro de analisis ya registrado.");
            return false;
        }
        return true;

    }

}
