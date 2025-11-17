package com.mb.conitrack.service.cu;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;
import com.mb.conitrack.repository.maestro.RoleRepository;
import com.mb.conitrack.repository.maestro.UserRepository;
import com.mb.conitrack.service.cu.validator.AnalisisValidator;
import com.mb.conitrack.service.cu.validator.CantidadValidator;
import com.mb.conitrack.service.cu.validator.FechaValidator;
import com.mb.conitrack.service.cu.validator.TrazaValidator;

import jakarta.validation.Valid;

/**
 * Servicio base con validaciones comunes para CU de stock y movimientos.
 * Actúa como fachada que delega a validadores especializados:
 * - CantidadValidator: Validaciones de cantidades y bultos
 * - FechaValidator: Validaciones de fechas
 * - AnalisisValidator: Validaciones de análisis
 * - TrazaValidator: Validaciones de trazas
 */
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

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    /** Obtiene lista de países para selección en formularios. */
    public List<String> getCountryList() {
        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        for (String code : countryCodes) {
            Locale locale = new Locale("", code);
            countries.add(locale.getDisplayCountry());
        }
        countries.sort(String::compareTo);
        return countries;
    }

    // ========== Delegación a CantidadValidator ==========

    protected boolean validarBultos(final LoteDTO loteDTO, final BindingResult bindingResult) {
        return CantidadValidator.validarBultos(loteDTO, bindingResult);
    }

    protected boolean validarCantidadIngreso(final LoteDTO loteDTO, final BindingResult bindingResult) {
        return CantidadValidator.validarCantidadIngreso(loteDTO, bindingResult);
    }

    boolean validarCantidadesMovimiento(final MovimientoDTO dto, final Bulto bulto, final BindingResult bindingResult) {
        return CantidadValidator.validarCantidadesMovimiento(dto, bulto, bindingResult);
    }

    boolean validarCantidadesPorMedidas(final LoteDTO loteDTO, final Lote lote, final BindingResult bindingResult) {
        return CantidadValidator.validarCantidadesPorMedidas(loteDTO, lote, bindingResult);
    }

    protected boolean validarSumaBultosConvertida(LoteDTO loteDTO, BindingResult bindingResult) {
        return CantidadValidator.validarSumaBultosConvertida(loteDTO, bindingResult);
    }

    protected boolean validarTipoDeDato(final LoteDTO loteDTO, final BindingResult bindingResult) {
        return CantidadValidator.validarTipoDeDato(loteDTO, bindingResult);
    }

    boolean validarUnidadMedidaVenta(final LoteDTO loteDTO, final Lote lote, final BindingResult bindingResult) {
        return CantidadValidator.validarUnidadMedidaVenta(loteDTO, lote, bindingResult);
    }

    // ========== Delegación a FechaValidator ==========

    protected boolean validarFechasProveedor(final LoteDTO loteDTO, final BindingResult bindingResult) {
        return FechaValidator.validarFechasProveedor(loteDTO, bindingResult);
    }

    boolean validarFechasReanalisis(final MovimientoDTO dto, final BindingResult bindingResult) {
        return FechaValidator.validarFechasReanalisis(dto, bindingResult);
    }

    boolean validarFechaAnalisisPosteriorIngresoLote(
            final MovimientoDTO dto,
            final LocalDate fechaIngresoLote,
            final BindingResult bindingResult) {
        return FechaValidator.validarFechaAnalisisPosteriorIngresoLote(dto, fechaIngresoLote, bindingResult);
    }

    boolean validarFechaEgresoLoteDtoPosteriorLote(
            final LoteDTO dto,
            final Lote lote,
            final BindingResult bindingResult) {
        return FechaValidator.validarFechaEgresoLoteDtoPosteriorLote(dto, lote, bindingResult);
    }

    boolean validarFechaMovimientoPosteriorIngresoLote(
            final MovimientoDTO dto,
            final LocalDate fechaIngresoLote,
            final BindingResult bindingResult) {
        return FechaValidator.validarFechaMovimientoPosteriorIngresoLote(dto, fechaIngresoLote, bindingResult);
    }

    boolean validarMovimientoOrigen(
            final MovimientoDTO dto,
            final BindingResult bindingResult,
            final Movimiento movOrigen) {
        return FechaValidator.validarMovimientoOrigen(dto, bindingResult, movOrigen);
    }

    // ========== Delegación a AnalisisValidator ==========

    boolean validarDatosMandatoriosAnulacionAnalisisInput(
            final MovimientoDTO dto,
            final BindingResult bindingResult) {
        return AnalisisValidator.validarDatosMandatoriosAnulacionAnalisisInput(dto, bindingResult);
    }

    boolean validarDatosMandatoriosResultadoAnalisisInput(
            final MovimientoDTO dto,
            final BindingResult bindingResult) {
        return AnalisisValidator.validarDatosMandatoriosResultadoAnalisisInput(dto, bindingResult);
    }

    boolean validarDatosResultadoAnalisisAprobadoInput(
            final MovimientoDTO dto,
            final BindingResult bindingResult) {
        return AnalisisValidator.validarDatosResultadoAnalisisAprobadoInput(dto, bindingResult);
    }

    boolean validarNroAnalisisNotNull(final MovimientoDTO dto, final BindingResult bindingResult) {
        return AnalisisValidator.validarNroAnalisisNotNull(dto, bindingResult);
    }

    boolean validarNroAnalisisUnico(final @Valid MovimientoDTO movimientoDTO, final BindingResult bindingResult) {
        return AnalisisValidator.validarNroAnalisisUnico(movimientoDTO, bindingResult, analisisRepository);
    }

    // ========== Delegación a TrazaValidator ==========

    boolean validarTrazaInicialLote(
            final MovimientoDTO dto,
            final BindingResult bindingResult) {
        return TrazaValidator.validarTrazaInicialLote(dto, bindingResult);
    }

    boolean validarTrazasDevolucion(final MovimientoDTO dto, final BindingResult bindingResult) {
        return TrazaValidator.validarTrazasDevolucion(dto, bindingResult);
    }
}
