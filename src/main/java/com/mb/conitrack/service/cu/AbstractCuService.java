package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;
import com.mb.conitrack.service.LoteService;

import jakarta.validation.Valid;

import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMenorUnidadMedida;

public abstract class AbstractCuService {

    @Autowired
    LoteRepository loteRepository;

    @Autowired
    ProductoRepository productoRepository;

    @Autowired
    ProveedorRepository proveedorRepository;

    @Autowired
    BultoRepository bultoRepository;

    @Autowired
    MovimientoRepository movimientoRepository;

    @Autowired
    AnalisisRepository analisisRepository;

    @Autowired
    TrazaRepository trazaRepository;

    List<String> getCountryList() {
        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        for (String code : countryCodes) {
            Locale locale = new Locale("", code);
            countries.add(locale.getDisplayCountry());
        }
        countries.sort(String::compareTo);
        return countries;
    }

    Lote getLoteByCodigoLote(String codigoLote, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return null;
        }

        final Optional<Lote> lote = loteRepository.findByCodigoLoteAndActivoTrue(codigoLote);
        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return null;
        }
        return lote.get();
    }

    boolean populateLoteListByCodigoLote(
        final List<Lote> lotesList,
        String codigoLote,
        BindingResult bindingResult,
        final LoteService loteService) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        final List<Lote> lote = loteService.findLoteListByCodigoLote(codigoLote);
        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }
        lotesList.addAll(lote);
        return true;
    }

    boolean validarBultos(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        return (loteDTO.getBultosTotales() == 1) ||
            (validarTipoDeDato(loteDTO, bindingResult) && validarSumaBultosConvertida(loteDTO, bindingResult));
    }

    boolean validarCantidadesMovimiento(final MovimientoDTO dto, final Bulto bulto, final BindingResult bindingResult) {
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

    boolean validarCantidadesPorMedidas(final LoteDTO loteDTO, final Lote lote, final BindingResult bindingResult) {
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

    boolean validarCantidadesPorMedidas(
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

    boolean validarContraFechaVencimientoProveedor(
        final MovimientoDTO dto,
        Lote lote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        // 1 La fecha de vencimiento de QC no puede ser posterior a la fecha de vencimiento del proveedor
        if (dto.getFechaVencimiento() != null &&
            lote.getFechaVencimientoProveedor() != null &&
            dto.getFechaVencimiento().isAfter(lote.getFechaVencimientoProveedor())) {
            bindingResult.rejectValue(
                "fechaVencimiento",
                "",
                "La fecha de vencimiento no puede ser posterior a la fecha de vencimiento del proveedor");
            return false;
        }
        return true;
    }

    boolean validarContraFechaVencimientoProveedor(
        final MovimientoDTO dto,
        Lote lote,
        List<Analisis> analisisList,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (dto.getFechaReanalisis() != null) {
            long analisisAprobados = analisisList.stream()
                .filter(a -> a.getDictamen() == DictamenEnum.APROBADO)
                .count();
            if (lote.getFechaVencimientoProveedor() != null &&
                dto.getFechaReanalisis().isAfter(lote.getFechaVencimientoProveedor())) {
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
                    dto.getFechaReanalisis().isAfter(lote.getFechaReanalisisProveedor())) {
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

    boolean validarDatosMandatoriosResultadoAnalisisInput(
        final MovimientoDTO dto,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        // Verificamos que nroAnalisis no sea vacío
        if (StringUtils.isEmptyOrWhitespace(dto.getNroAnalisis())) {
            bindingResult.rejectValue("nroAnalisis", "", "El Nro de Análisis es obligatorio");
            return false;
        }

        // Dictamen Final no nulo
        if (dto.getDictamenFinal() == null) {
            bindingResult.rejectValue("dictamenFinal", "", "Debe ingresar un Resultado");
            return false;
        }

        // Fecha Realizado Análisis no nula
        if (dto.getFechaRealizadoAnalisis() == null) {
            bindingResult.rejectValue(
                "fechaRealizadoAnalisis",
                "",
                "Debe ingresar la fecha en la que se realizó el análisis");
            return false;
        }
        return true;
    }

    boolean validarDatosResultadoAnalisisAprobadoInput(
        final MovimientoDTO dto,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (DictamenEnum.APROBADO != dto.getDictamenFinal()) {
            return true;
        }
        // Al menos una de las fechas de reanálisis o vencimiento debe ser ingresada
        if (dto.getFechaVencimiento() == null && dto.getFechaReanalisis() == null) {
            bindingResult.rejectValue("fechaVencimiento", "", "Debe ingresar una fecha de Re Análisis o Vencimiento");
            return false;
        }

        // La fecha de reanálisis no puede ser posterior a la fecha de vencimiento
        if (dto.getFechaVencimiento() != null &&
            dto.getFechaReanalisis() != null &&
            dto.getFechaReanalisis().isAfter(dto.getFechaVencimiento())) {
            bindingResult.rejectValue(
                "fechaReanalisis",
                "",
                "La fecha de reanalisis no puede ser posterior a la fecha de vencimiento");
            return false;
        }

        // El título es obligatorio y no puede ser mayor al 100%
        if (dto.getTitulo() == null) {
            bindingResult.rejectValue("titulo", "", "Debe ingresar el valor de título del Análisis");
            return false;
        }
        if (dto.getTitulo().compareTo(BigDecimal.valueOf(100)) > 0) {
            bindingResult.rejectValue("titulo", "", "El título no puede ser mayor al 100%");
            return false;
        }
        if (dto.getTitulo().compareTo(BigDecimal.valueOf(0)) <= 0) {
            bindingResult.rejectValue("titulo", "", "El título no puede ser menor o igual a 0");
            return false;
        }
        return true;
    }

    boolean validarExisteMuestreoParaAnalisis(
        final MovimientoDTO dto,
        final List<Movimiento> movimientos,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //Al menos necesitamos haber hecho un muestreo para poder hacer un análisis
        boolean existeMuestreo = movimientos.stream()
            .anyMatch(m -> m.getTipoMovimiento() == TipoMovimientoEnum.BAJA &&
                m.getMotivo() == MotivoEnum.MUESTREO &&
                dto.getNroAnalisis().equals(m.getNroAnalisis()));

        if (!existeMuestreo) {
            bindingResult.rejectValue(
                "nroAnalisis",
                "",
                "No se encontró un MUESTREO realizado para ese Nro de Análisis " + dto.getNroAnalisis());
            return false;
        }
        return true;
    }

    boolean validarExisteMuestreoParaAnalisis(
        final MovimientoDTO dto,
        final Lote lote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //Al menos necesitamos haber hecho un muestreo para poder hacer un análisis
        boolean existeMuestreo = lote.getMovimientos()
            .stream()
            .anyMatch(m -> m.getTipoMovimiento() == TipoMovimientoEnum.BAJA &&
                m.getMotivo() == MotivoEnum.MUESTREO &&
                dto.getNroAnalisis().equals(m.getNroAnalisis()));

        if (!existeMuestreo) {
            bindingResult.rejectValue(
                "nroAnalisis",
                "",
                "No se encontró un MUESTREO realizado para ese Nro de Análisis " + dto.getNroAnalisis());
            return false;
        }
        return true;
    }

    boolean validarFechaAnalisisPosteriorIngresoLote(
        final MovimientoDTO dto,
        final LocalDate fechaIngresoLote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaRealizadoAnalisis() != null && dto.getFechaRealizadoAnalisis().isBefore(fechaIngresoLote)) {
            bindingResult.rejectValue(
                "fechaRealizadoAnalisis",
                "",
                "La fecha de realizado el analisis no puede ser anterior a la fecha de ingreso del lote");
            return false;
        }
        return true;
    }

    boolean validarFechaAnalisisPosteriorIngresoLote(
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

    boolean validarFechaDevolucionLoteDtoPosteriorLote(
        final MovimientoDTO dto,
        final Lote lote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaMovimiento() != null && dto.getFechaMovimiento().isBefore(lote.getFechaIngreso())) {
            bindingResult.rejectValue(
                "fechaMovimiento",
                "",
                "La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
            return false;
        }
        return true;
    }

    boolean validarFechaEgresoLoteDtoPosteriorLote(
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

    boolean validarFechaMovimientoPosteriorIngresoLote(
        final MovimientoDTO dto,
        final LocalDate fechaIngresoLote,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaMovimiento().isBefore(fechaIngresoLote)) {
            bindingResult.rejectValue(
                "fechaMovimiento",
                "",
                "La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
            return false;
        }
        return true;
    }

    boolean validarFechaMovimientoPosteriorIngresoLote(
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

    boolean validarMovimientoOrigen(
        final MovimientoDTO dto,
        final BindingResult bindingResult,
        final Movimiento movOrigen) {
        if (dto.getFechaMovimiento() != null &&
            dto.getFechaMovimiento().isBefore(movOrigen.getFecha())) {
            bindingResult.rejectValue(
                "fechaMovimiento",
                "",
                "La fecha de devolución no puede ser anterior a la fecha del movimiento de venta.");
            return false;
        }
        return true;
    }

    boolean validarNroAnalisisNotNull(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (StringUtils.isEmptyOrWhitespace(dto.getNroAnalisis()) &&
            StringUtils.isEmptyOrWhitespace(dto.getNroReanalisis())) {
            bindingResult.rejectValue("nroAnalisis", "nroAnalisis.nulo", "Ingrese un nro de analisis");
            return false;
        }
        return true;
    }

    boolean validarNroAnalisisUnico(final @Valid MovimientoDTO movimientoDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        //TODO: fix validacion Analisis (usar codigo lote)
        Analisis analisis;
        final boolean isAnalisis = StringUtils.isEmptyOrWhitespace(movimientoDTO.getNroReanalisis());
        if (isAnalisis) {
            analisis = analisisRepository.findByNroAnalisisAndActivoTrue(movimientoDTO.getNroAnalisis());
        } else {
            analisis = analisisRepository.findByNroAnalisisAndActivoTrue(movimientoDTO.getNroReanalisis());
        }
        if (analisis != null) {
            String field = isAnalisis ? "nroAnalisis" : "nroReanalisis";
            if (analisis.getLote().getCodigoLote().equals(movimientoDTO.getCodigoLote())) {
                if (analisis.getDictamen() != null) {
                    bindingResult.rejectValue(
                        field,
                        "",
                        "Nro de analisis ya registrado en el mismo lote.");
                    return false;
                }
            } else {
                bindingResult.rejectValue(
                    field,
                    "",
                    "Nro de analisis ya registrado en otro lote.");
                return false;
            }
        }
        return true;
    }

    boolean validarSumaBultosConvertida(LoteDTO loteDTO, BindingResult bindingResult) {
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

    boolean validarTipoDeDato(final LoteDTO loteDTO, final BindingResult bindingResult) {
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

    boolean validarTrazasDevolucion(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (dto.getTrazaDTOs() == null || dto.getTrazaDTOs().isEmpty()) {
            bindingResult.rejectValue("trazaDTOs", "", "Debe seleccionar al menos una traza para devolver.");
            return false;
        }
        return true;
    }

    boolean validarUnidadMedidaVenta(final LoteDTO loteDTO, final Lote lote, final BindingResult bindingResult) {
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

    boolean validarValorTitulo(
        final MovimientoDTO dto,
        final Analisis ultimoAprobado,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (ultimoAprobado != null && dto.getTitulo().compareTo(ultimoAprobado.getTitulo()) > 0) {
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

    boolean validateCantidadIngreso(final LoteDTO loteDTO, final BindingResult bindingResult) {
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

    boolean validateFechasAnalisis(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (dto.getFechaReanalisis() != null && dto.getFechaVencimiento() != null) {
            if (dto.getFechaReanalisis().isAfter(dto.getFechaVencimiento())) {
                bindingResult.rejectValue(
                    "fechaVencimiento",
                    "",
                    "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
                return false;
            }
        }
        return true;
    }

    boolean validateFechasProveedor(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (loteDTO.getFechaReanalisisProveedor() != null && loteDTO.getFechaVencimientoProveedor() != null) {
            if (loteDTO.getFechaReanalisisProveedor().isAfter(loteDTO.getFechaVencimientoProveedor())) {
                bindingResult.rejectValue(
                    "fechaReanalisisProveedor",
                    "",
                    "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
                return false;
            }
        }
        return true;
    }

}
