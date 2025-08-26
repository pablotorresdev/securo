package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.utils.LoteEntityUtils;
import com.mb.conitrack.utils.UnidadMedidaUtils;

import static com.mb.conitrack.enums.EstadoEnum.CONSUMIDO;
import static com.mb.conitrack.enums.EstadoEnum.EN_USO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoMuestreoConAnalisis;
import static java.lang.Integer.parseInt;

@Service
public class BajaMuestreoBultoService extends AbstractCuService {

    private static LoteEntityUtils loteUtils() {
        return LoteEntityUtils.getInstance();
    }

    //***********CU3 BAJA: MUESTREO***********
    //TODO: soportar multimuestreo para simplificar la carga
    @Transactional
    public LoteDTO bajaMuestreo(final MovimientoDTO dto) {

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Bulto bulto = lote.getBultoByNro(parseInt(dto.getNroBulto()));
        final String currentNroAnalisis = lote.getUltimoNroAnalisis();
        if (!currentNroAnalisis.equals(dto.getNroAnalisis())) {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }

        final Movimiento movimiento = persistirMovimientoMuestreo(dto, bulto);

        bulto.setCantidadActual(UnidadMedidaUtils.restarMovimientoConvertido(dto, bulto));
        lote.setCantidadActual(UnidadMedidaUtils.restarMovimientoConvertido(dto, lote));

        boolean unidadVenta = lote.getProducto().getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA;

        if (unidadVenta) {
            final BigDecimal cantidad = movimiento.getCantidad();
            if (movimiento.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
                throw new IllegalStateException("La traza solo es aplicable a UNIDADES");
            }

            if (cantidad.stripTrailingZeros().scale() > 0) {
                throw new IllegalStateException("La cantidad de Unidades debe ser entero");
            }

            final List<Traza> trazas = bulto.getFirstAvailableTrazaList(cantidad.intValue());

            for (Traza traza : trazas) {
                traza.setEstado(CONSUMIDO);
                traza.getDetalles().addAll(movimiento.getDetalles());
            }
            trazaRepository.saveAll(trazas);
            dto.setTrazaDTOs(trazas.stream().map(DTOUtils::fromTrazaEntity).toList());
        }

        if (bulto.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
            bulto.setEstado(CONSUMIDO);
        } else {
            bulto.setEstado(EN_USO);
        }

        boolean todosConsumidos = lote.getBultos().stream()
            .allMatch(b -> b.getEstado() == CONSUMIDO);
        lote.setEstado(todosConsumidos ? CONSUMIDO : EN_USO);

        lote.getMovimientos().add(movimiento);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoMuestreo(final MovimientoDTO dto, Bulto bulto) {
        final List<Analisis> analisisList = bulto.getLote().getAnalisisList();
        if (analisisList.isEmpty()) {
            return crearMovimientoMuestreoConPrimerAnalisis(dto, bulto);
        } else {
            final Optional<Analisis> analisisEnCurso = loteUtils().getAnalisisEnCurso(analisisList);
            if (analisisEnCurso.isPresent()) {
                return crearMovimientoMuestreoConAnalisisEnCurso(dto, bulto, analisisEnCurso);
            } else {
                return crearMovmimientoMuestreoConAnalisisDictaminado(dto, bulto);
            }
        }
    }

    @Transactional
    public Movimiento crearMovimientoMuestreoConPrimerAnalisis(final MovimientoDTO dto, final Bulto bulto) {
        //Si el lote no tiene analisis realizado (Recibido), se crea uno nuevo y se guarda el movimiento
        final Analisis newAnalisis = analisisRepository.save(DTOUtils.createAnalisis(dto));
        return movimientoRepository.save(createMovimientoMuestreoConAnalisis(dto, bulto, newAnalisis));
    }

    @Transactional
    public Movimiento crearMovimientoMuestreoConAnalisisEnCurso(
        final MovimientoDTO dto,
        final Bulto bulto,
        final Optional<Analisis> analisisEnCurso) {
        //Si el lote tiene un analisis en curso, se guarda el movimiento y se asocia al analisis en curso
        //El lote puede tiene n analisis realizados siempre se asocia al analisis en curso
        if (dto.getNroAnalisis()
            .equals(analisisEnCurso.orElseThrow(() -> new IllegalArgumentException("El número de análisis esta vacio"))
                .getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoMuestreoConAnalisis(dto, bulto, analisisEnCurso.get()));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Transactional
    public Movimiento crearMovmimientoMuestreoConAnalisisDictaminado(final MovimientoDTO dto, final Bulto bulto) {
        //Si el lote tiene n analisis realizados, se guarda el movimiento y se asocia al ultimo analisis realizado
        Analisis ultimoAnalisis = bulto.getLote().getUltimoAnalisis();
        if (dto.getNroAnalisis().equals(ultimoAnalisis.getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoMuestreoConAnalisis(dto, bulto, ultimoAnalisis));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Transactional
    public boolean validarMuestreoBultoInput(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (!validarNroAnalisisNotNull(dto, bindingResult)) {
            return false;
        }

        final Optional<LocalDate> fechaIngresoLote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .map(Lote::getFechaIngreso);

        if (fechaIngresoLote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (!validarFechaMovimientoPosteriorIngresoLote(dto, fechaIngresoLote.get(), bindingResult)) {
            return false;
        }

        if (!validarFechaAnalisisPosteriorIngresoLote(dto, fechaIngresoLote.get(), bindingResult)) {
            return false;
        }

        final Optional<Bulto> bulto = bultoRepository.findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue(
            dto.getCodigoLote(),
            parseInt(dto.getNroBulto()));

        if (bulto.isEmpty()) {
            bindingResult.rejectValue("nroBulto", "", "Bulto no encontrado.");
            return false;
        }

        return validarCantidadesMovimiento(dto, bulto.get(), bindingResult);
    }

}
