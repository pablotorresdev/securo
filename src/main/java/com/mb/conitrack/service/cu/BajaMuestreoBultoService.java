package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.SecurityContextService;

import static com.mb.conitrack.enums.EstadoEnum.CONSUMIDO;
import static com.mb.conitrack.enums.EstadoEnum.EN_USO;
import static com.mb.conitrack.utils.LoteEntityUtils.getAnalisisEnCurso;
import static com.mb.conitrack.utils.MovimientoBajaUtils.createMovimientoMuestreoConAnalisis;
import static com.mb.conitrack.utils.MovimientoBajaUtils.createMovimientoPorMuestreoMultiBulto;
import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMayorUnidadMedida;
import static com.mb.conitrack.utils.UnidadMedidaUtils.restarMovimientoConvertido;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.parseInt;

/** CU3 - Baja Muestreo. Descuenta stock por muestreo para análisis de calidad. */
@Service
public class BajaMuestreoBultoService extends AbstractCuService {

    @Autowired
    private SecurityContextService securityContextService;

    /** Procesa muestreo de producto trazable asociándolo a análisis. Marca trazas como CONSUMIDO. */
    @Transactional
    public LoteDTO bajaMuestreoTrazable(final MovimientoDTO dto) {
        User currentUser = securityContextService.getCurrentUser();

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Bulto bulto = lote.getBultoByNro(parseInt(dto.getNroBulto()));
        final String currentNroAnalisis = lote.getUltimoNroAnalisis();
        if (!currentNroAnalisis.equals(dto.getNroAnalisis())) {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }

        final Movimiento movimiento = persistirMovimientoMuestreo(dto, bulto, currentUser);

        bulto.setCantidadActual(restarMovimientoConvertido(dto, bulto));
        lote.setCantidadActual(restarMovimientoConvertido(dto, lote));

        boolean unidadVenta = lote.getProducto().getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA;

        if (unidadVenta) {
            final BigDecimal cantidad = movimiento.getCantidad();
            if (movimiento.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
                throw new IllegalStateException("La traza solo es aplicable a UNIDADES");
            }

            if (cantidad.stripTrailingZeros().scale() > 0) {
                throw new IllegalStateException("La cantidad de Unidades debe ser entero");
            }

            final List<Traza> trazas = new ArrayList<>();
            for (TrazaDTO trazaDTO : dto.getTrazaDTOs()) {
                final Long nroTraza = trazaDTO.getNroTraza();
                for (Traza trazasLote : lote.getActiveTrazas()) {
                    if (trazasLote.getNroTraza().equals(nroTraza)) {
                        trazas.add(trazasLote);
                        break;
                    }
                }
            }

            if (movimiento.getDetalles().size() > 1) {
                throw new IllegalArgumentException("Multimuestreo no soportado aun");
            }

            final DetalleMovimiento detalleMovimiento = movimiento.getDetalles().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El detalle del movimiento de muestreo no existe."));

            for (Traza traza : trazas) {
                traza.setEstado(CONSUMIDO);
                traza.getDetalles().addAll(movimiento.getDetalles());
            }
            detalleMovimiento.getTrazas().addAll(trazas);
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
    public Movimiento persistirMovimientoMuestreo(final MovimientoDTO dto, Bulto bulto, User currentUser) {
        final List<Analisis> analisisList = bulto.getLote().getAnalisisList();
        if (analisisList.isEmpty()) {
            throw new IllegalStateException("No hay Analisis con al que asociar el muestreo");
        } else {
            final Optional<Analisis> analisisEnCurso = getAnalisisEnCurso(analisisList);
            if (analisisEnCurso.isPresent()) {
                // Muestreo de un producto con Analisis en Curso
                return crearMovimientoMuestreoConAnalisisEnCurso(dto, bulto, analisisEnCurso, currentUser);
            } else {
                // Muestreo un producto con Analisis Dictaminado
                return crearMovmimientoMuestreoConAnalisisDictaminado(dto, bulto, currentUser);
            }
        }
    }

    @Transactional
    public Movimiento crearMovimientoMuestreoConAnalisisEnCurso(
        final MovimientoDTO dto,
        final Bulto bulto,
        final Optional<Analisis> analisisEnCurso,
        User currentUser) {
        //Si el lote tiene un analisis en curso, se guarda el movimiento y se asocia al analisis en curso
        //El lote puede tiene n analisis realizados siempre se asocia al analisis en curso
        if (dto.getNroAnalisis()
            .equals(analisisEnCurso.orElseThrow(() -> new IllegalArgumentException("El número de análisis esta vacio"))
                .getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoMuestreoConAnalisis(dto, bulto, analisisEnCurso.get(), currentUser));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Transactional
    public Movimiento crearMovmimientoMuestreoConAnalisisDictaminado(final MovimientoDTO dto, final Bulto bulto, User currentUser) {
        //Si el lote tiene n analisis realizados, se guarda el movimiento y se asocia al ultimo analisis realizado
        Analisis ultimoAnalisis = bulto.getLote().getUltimoAnalisis();
        if (dto.getNroAnalisis().equals(ultimoAnalisis.getNroAnalisis())) {
            return movimientoRepository.save(createMovimientoMuestreoConAnalisis(dto, bulto, ultimoAnalisis, currentUser));
        } else {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Transactional
    public boolean validarMuestreoTrazableInput(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (!validarNroAnalisisNotNull(dto, bindingResult)) {
            return false;
        }

        final Optional<Lote> lote = loteRepository.findByCodigoLoteAndActivoTrue(dto.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        final boolean esUnidadVenta = lote.get().getProducto().getTipoProducto() ==
            TipoProductoEnum.UNIDAD_VENTA; // crea este helper si no lo tenés

        if (esUnidadVenta) {
            if (dto.getTrazaDTOs() == null || dto.getTrazaDTOs().isEmpty()) {
                bindingResult.rejectValue("trazaDTOs", "", "Debe seleccionar al menos una unidad a muestrear.");
                return false;
            }
        }

        if (!validarFechaMovimientoPosteriorIngresoLote(dto, lote.get().getFechaIngreso(), bindingResult)) {
            return false;
        }

        if (!validarFechaAnalisisPosteriorIngresoLote(dto, lote.get().getFechaIngreso(), bindingResult)) {
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

    @Transactional
    public boolean validarmuestreoMultiBultoInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        final Optional<Lote> lote = loteRepository
            .findByCodigoLoteAndActivoTrue(loteDTO.getCodigoLote());

        if (lote.isEmpty()) {
            bindingResult.rejectValue("codigoLote", "", "Lote no encontrado.");
            return false;
        }

        if (StringUtils.isEmptyOrWhitespace(lote.get().getUltimoNroAnalisis())) {
            bindingResult.rejectValue("codigoLote", "", "El lote no tiene Analisis asociado.");
            return false;
        }

        if (!validarFechaEgresoLoteDtoPosteriorLote(loteDTO, lote.get(), bindingResult)) {
            return false;
        }

        return validarCantidadesPorMedidas(loteDTO, lote.get(), bindingResult);
    }

    @Transactional
    public LoteDTO bajamuestreoMultiBulto(final LoteDTO loteDTO) {
        User currentUser = securityContextService.getCurrentUser();

        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(loteDTO.getCodigoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final Movimiento movimientoMultiBulto = persistirMovimientoBajaMuestreoMultiBulto(loteDTO, lote, currentUser);

        final List<Integer> nroBultoList = loteDTO.getNroBultoList();
        final List<BigDecimal> cantidadesBultos = loteDTO.getCantidadesBultos();
        final List<UnidadMedidaEnum> unidadMedidaBultos = loteDTO.getUnidadMedidaBultos();

        for (int nroBulto : nroBultoList) {
            final Bulto bultoEntity = lote.getBultoByNro(nroBulto);
            final BigDecimal cantidaConsumoBulto = cantidadesBultos.get(nroBulto - 1);
            final UnidadMedidaEnum uniMedidaConsumoBulto = unidadMedidaBultos.get(nroBulto - 1);

            if (BigDecimal.ZERO.compareTo(cantidaConsumoBulto) == 0) {
                continue;
            }

            if (bultoEntity.getUnidadMedida() == uniMedidaConsumoBulto) {
                bultoEntity.setCantidadActual(bultoEntity.getCantidadActual().subtract(cantidaConsumoBulto));
                if (lote.getUnidadMedida() == uniMedidaConsumoBulto) {
                    lote.setCantidadActual(lote.getCantidadActual().subtract(cantidaConsumoBulto));
                } else {
                    final BigDecimal cantidadConsumoLoteConvertida = convertirCantidadEntreUnidades(
                        uniMedidaConsumoBulto,
                        cantidaConsumoBulto,
                        lote.getUnidadMedida());
                    lote.setCantidadActual(lote.getCantidadActual().subtract(cantidadConsumoLoteConvertida));
                }
            } else {
                final BigDecimal cantidadConsumoBultoConvertida = convertirCantidadEntreUnidades(
                    uniMedidaConsumoBulto,
                    cantidaConsumoBulto,
                    bultoEntity.getUnidadMedida());
                bultoEntity.setCantidadActual(bultoEntity.getCantidadActual().subtract(cantidadConsumoBultoConvertida));

                if (lote.getUnidadMedida() == uniMedidaConsumoBulto) {
                    lote.setCantidadActual(lote.getCantidadActual().subtract(cantidaConsumoBulto));
                } else {
                    final BigDecimal cantidadConsumoLoteConvertida = convertirCantidadEntreUnidades(
                        uniMedidaConsumoBulto,
                        cantidaConsumoBulto,
                        lote.getUnidadMedida());
                    lote.setCantidadActual(lote.getCantidadActual().subtract(cantidadConsumoLoteConvertida));
                }
            }
            if (bultoEntity.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
                bultoEntity.setEstado(EstadoEnum.CONSUMIDO);
            } else {
                bultoEntity.setEstado(EstadoEnum.EN_USO);
            }
        }
        if (lote.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
            lote.setEstado(EstadoEnum.CONSUMIDO);
        } else {
            lote.setEstado(EstadoEnum.EN_USO);
        }

        lote.getMovimientos().add(movimientoMultiBulto);
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public Movimiento persistirMovimientoBajaMuestreoMultiBulto(final LoteDTO loteDTO, final Lote loteEntity, User currentUser) {
        final Movimiento movimiento = createMovimientoPorMuestreoMultiBulto(loteDTO, loteEntity, currentUser);

        Analisis ultimoAnalisis = loteEntity.getUltimoAnalisis();
        if (ultimoAnalisis == null) {
            throw new IllegalStateException("No hay Analisis con al que asociar el muestreo");
        }

        movimiento.setNroAnalisis(ultimoAnalisis.getNroAnalisis());

        UnidadMedidaEnum uniMedidaMovimiento = loteDTO.getUnidadMedidaBultos().get(0);

        for (int i = 1; i < loteDTO.getCantidadesBultos().size(); i++) {
            uniMedidaMovimiento = obtenerMayorUnidadMedida(uniMedidaMovimiento, loteDTO.getUnidadMedidaBultos().get(i));
        }

        BigDecimal cantidad = BigDecimal.ZERO;

        for (int i = 0; i < loteDTO.getCantidadesBultos().size(); i++) {
            final BigDecimal montoBulto = convertirCantidadEntreUnidades(
                loteDTO.getUnidadMedidaBultos().get(i),
                loteDTO.getCantidadesBultos().get(i),
                uniMedidaMovimiento);
            cantidad = cantidad.add(montoBulto);
        }

        movimiento.setCantidad(cantidad);
        movimiento.setUnidadMedida(uniMedidaMovimiento);

        for (int i = 0; i < loteDTO.getNroBultoList().size(); i++) {
            final Integer nroBulto = loteDTO.getNroBultoList().get(i);
            if (BigDecimal.ZERO.compareTo(loteDTO.getCantidadesBultos().get(i)) == 0) {
                continue;
            }
            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(loteEntity.getBultoByNro(nroBulto))
                .cantidad(loteDTO.getCantidadesBultos().get(i))
                .unidadMedida(loteDTO.getUnidadMedidaBultos().get(i))
                .activo(TRUE)
                .build();

            movimiento.getDetalles().add(det);
        }

        return movimientoRepository.save(movimiento);
    }

}
