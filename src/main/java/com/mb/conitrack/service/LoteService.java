package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.utils.LoteEntityUtils;
import com.mb.conitrack.utils.UnidadMedidaUtils;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

import static com.mb.conitrack.enums.EstadoEnum.DEVUELTO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.addLoteInfoToMovimientoAlta;
import static com.mb.conitrack.utils.MovimientoEntityUtils.crearMovimientoDevolucionCompra;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoCompra;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoProduccion;
import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;

@AllArgsConstructor
@Service
public class LoteService {

    private final LoteRepository loteRepository;

    private final ProveedorService proveedorService;

    private final ProductoService productoService;

    private final BultoService bultoService;

    private final MovimientoService movimientoService;

    private final AnalisisService analisisService;

    private final TrazaService trazaService;

    private static LoteEntityUtils loteUtils() {
        return LoteEntityUtils.getInstance();
    }

    //***********CU1 ALTA: COMPRA***********
    @Transactional
    public Lote altaStockPorCompra(LoteDTO loteDTO) {
        Proveedor proveedor = proveedorService.findById(loteDTO.getProveedorId())
            .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));

        Producto producto = productoService.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        Optional<Proveedor> fabricante = Optional.empty();
        if (loteDTO.getFabricanteId() != null) {
            fabricante = proveedorService.findById(loteDTO.getFabricanteId());
        }

        Lote lote = loteUtils().createLoteIngreso(loteDTO);
        loteUtils().populateLoteAltaStockCompra(lote, loteDTO, producto, proveedor, fabricante.orElse(null));

        Lote loteGuardado = loteRepository.save(lote);
        final List<Bulto> bultosGuardados = loteGuardado.getBultos();
        bultoService.save(bultosGuardados);

        final Movimiento movimientoAltaIngresoCompra = createMovimientoAltaIngresoCompra(loteGuardado);
        addLoteInfoToMovimientoAlta(loteGuardado, movimientoAltaIngresoCompra);

        final Movimiento movimientoGuardado = movimientoService.save(movimientoAltaIngresoCompra);
        loteGuardado.getMovimientos().add(movimientoGuardado);

        return loteGuardado;
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    @Transactional
    public Lote persistirDictamenCuarentena(final MovimientoDTO dto, Lote lote) {
        //TODO, eliminar NRO de Reanalisis del DTO
        final String nroAnalisis = StringUtils.isEmptyOrWhitespace(dto.getNroReanalisis())
            ? dto.getNroAnalisis()
            : dto.getNroReanalisis();

        Analisis currentAnalisis = analisisService.findByNroAnalisis(nroAnalisis);
        Analisis newAnalisis = null;

        if (currentAnalisis == null) {
            final Analisis analisis = DTOUtils.createAnalisis(dto);
            analisis.setLote(lote);
            newAnalisis = analisisService.save(analisis);
        }
        if (newAnalisis != null) {
            lote.getAnalisisList().add(newAnalisis);
        }

        final String nroAnalisisMovimiento = newAnalisis != null ? newAnalisis.getNroAnalisis() : nroAnalisis;
        Movimiento mov = movimientoService.persistirMovimientoCuarentenaPorAnalisis(dto, lote, nroAnalisisMovimiento);

        lote.getMovimientos().add(mov);
        lote.setDictamen(DictamenEnum.CUARENTENA);

        return loteRepository.save(lote);
    }

    //***********CU3 BAJA: MUESTREO***********
    @Transactional
    public Lote bajaMuestreo(final MovimientoDTO dto, final Bulto bulto) {
        final Lote lote = bulto.getLote();
        final String currentNroAnalisis = lote.getUltimoNroAnalisis();
        if (!currentNroAnalisis.equals(dto.getNroAnalisis())) {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }

        final Movimiento movimiento = movimientoService.persistirMovimientoMuestreo(dto, bulto);

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
                traza.setEstado(EstadoEnum.CONSUMIDO);
                traza.getDetalles().addAll(movimiento.getDetalles());
            }
            trazaService.save(trazas);
            dto.setTrazaDTOs(trazas.stream().map(DTOUtils::fromTrazaEntity).toList());
        }

        if (bulto.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
            bulto.setEstado(EstadoEnum.CONSUMIDO);
        } else {
            bulto.setEstado(EstadoEnum.EN_USO);
        }

        boolean todosConsumidos = lote.getBultos().stream()
            .allMatch(b -> b.getEstado() == EstadoEnum.CONSUMIDO);
        lote.setEstado(todosConsumidos ? EstadoEnum.CONSUMIDO : EstadoEnum.EN_USO);

        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
    }

    @Transactional
    //TODO: soportar multimuestreo para simplificar la carga
    public Lote bajaMultiMuestreo(final MovimientoDTO dto, final Lote lote) {
        Lote result = null;
        for (Bulto bulto : lote.getBultos()) {
            result = bajaMuestreo(dto, bulto);
        }
        return result;
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional
    public Lote bajaBultosDevolucionCompra(final MovimientoDTO dto, final Lote lote) {

        final Movimiento movimiento = crearMovimientoDevolucionCompra(dto);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setCantidad(lote.getCantidadActual());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setLote(lote);

        for (Bulto bulto : lote.getBultos()) {
            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(bulto)
                .cantidad(bulto.getCantidadActual())
                .unidadMedida(bulto.getUnidadMedida())
                .build();

            movimiento.getDetalles().add(det);
        }

        final Movimiento savedMovimiento = movimientoService.save(movimiento);

        for (Bulto bulto : lote.getBultos()) {
            bulto.setCantidadActual(BigDecimal.ZERO);
            bulto.setEstado(DEVUELTO);
            bultoService.save(bulto);
        }

        lote.setEstado(DEVUELTO);
        lote.setCantidadActual(BigDecimal.ZERO);
        lote.getMovimientos().add(savedMovimiento);

        for (Analisis analisis : lote.getAnalisisList()) {
            if (analisis.getDictamen() == null) {
                analisis.setDictamen(DictamenEnum.ANULADO);
                analisisService.save(analisis);
            }
        }

        return loteRepository.save(lote);
    }

    //***********CU> MODIFICACION: CUZ Reanalisis de Producto Aprobado***********
    @Transactional
    public Lote persistirReanalisisProducto(final MovimientoDTO dto, final Lote lote) {
        final Analisis analisis = DTOUtils.createAnalisis(dto);
        analisis.setLote(lote);
        final Analisis newAnalisis = analisisService.save(analisis);
        final Movimiento movimiento = movimientoService.persistirMovimientoReanalisisProducto(
            dto,
            lote,
            newAnalisis.getNroAnalisis());
        lote.getMovimientos().add(movimiento);
        lote.getAnalisisList().add(newAnalisis);
        newAnalisis.setLote(lote);
        return loteRepository.save(lote);
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    @Transactional
    public Lote persistirResultadoAnalisis(final MovimientoDTO dto) {
        final Analisis analisis = analisisService.addResultadoAnalisis(dto);
        final Lote lote = analisis.getLote();
        final Movimiento movimiento = movimientoService.persistirMovimientoResultadoAnalisis(dto, lote);
        lote.setDictamen(movimiento.getDictamenFinal());
        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
    }

    //***********CU7 BAJA: CONSUMO PRODUCCION***********
    @Transactional
    public Lote bajaConsumoProduccion(final LoteDTO loteDTO) {
        final Lote lote = loteRepository.findFirstByCodigoInternoAndActivoTrue(
                loteDTO.getCodigoInternoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

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
        final Movimiento movimiento = movimientoService.persistirMovimientoBajaConsumoProduccion(loteDTO, lote);
        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
    }

    //***********CU8 MODIFICACION: VENCIDO***********
    @Transactional
    public List<Lote> persistirProductosVencidos(final MovimientoDTO dto, final List<Lote> lotes) {
        //TODO, eliminar NRO de Reanalisis del DTO
        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final Movimiento movimiento = movimientoService.persistirMovimientoProductoVencido(dto, loteBulto);
            loteBulto.setDictamen(movimiento.getDictamenFinal());
            loteBulto.getMovimientos().add(movimiento);
            result.add(loteRepository.save(loteBulto));
        }
        return result;
    }

    //***********CU9 MODIFICACION: ANALSIS EXPIRADO***********
    @Transactional
    public List<Lote> persistirExpiracionAnalisis(final MovimientoDTO dto, final List<Lote> lotes) {
        List<Lote> result = new ArrayList<>();
        for (Lote lote : lotes) {
            final Movimiento movimiento = movimientoService.persistirMovimientoExpiracionAnalisis(dto, lote);
            lote.setDictamen(movimiento.getDictamenFinal());
            lote.getMovimientos().add(movimiento);
            result.add(loteRepository.save(lote));
        }
        return result;
    }

    //***********CU10 ALTA: PRODUCCION INTERNA***********
    @Transactional
    public Lote altaStockPorProduccion(final LoteDTO loteDTO) {
        final Proveedor conifarma = proveedorService.getConifarma();

        final Producto producto = productoService.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        final Lote lote = loteUtils().createLoteIngreso(loteDTO);
        loteUtils().populateLoteAltaProduccionPropia(lote, loteDTO, producto, conifarma);

        final Lote loteGuardado = loteRepository.save(lote);
        bultoService.save(loteGuardado.getBultos());

        if (loteGuardado.getTrazas() != null && !loteGuardado.getTrazas().isEmpty()) {
            trazaService.save(loteGuardado.getTrazas());
        }

        final Movimiento movimiento = createMovimientoAltaIngresoProduccion(loteGuardado);
        movimientoService.save(movimiento);

        loteGuardado.getBultos().stream()
            .sorted(Comparator.comparing(Bulto::getNroBulto))
            .forEach(b -> {
                final DetalleMovimiento det = DetalleMovimiento.builder()
                    .movimiento(movimiento)
                    .bulto(b)
                    .cantidad(b.getCantidadInicial())
                    .unidadMedida(b.getUnidadMedida())
                    .build();

                if (b.getTrazas() != null && !b.getTrazas().isEmpty()) {
                    b.getTrazas().stream()
                        .sorted(Comparator.comparing(Traza::getNroTraza))
                        .forEach(det.getTrazas()::add);
                }

                movimiento.getDetalles().add(det);
            });

        movimientoService.save(movimiento);

        return loteGuardado;
    }

    //***********CU11 MODIFICACION: LIBERACION DE PRODUCTO***********
    @Transactional
    public Lote persistirLiberacionProducto(final MovimientoDTO dto, final Lote lote) {

        final Movimiento movimiento = movimientoService.persistirMovimientoLiberacionProducto(dto, lote);

        lote.setFechaReanalisisProveedor(lote.getFechaReanalisisVigente());
        lote.setFechaVencimientoProveedor(lote.getFechaVencimientoVigente());

        lote.setDictamen(movimiento.getDictamenFinal());
        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
    }

    //***********CU12 BAJA: VENTA***********
    @Transactional
    public Lote bajaVentaProducto(final LoteDTO loteDTO) {
        final Lote lote = loteRepository.findFirstByCodigoInternoAndActivoTrue(
                loteDTO.getCodigoInternoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        final List<Integer> nroBultoList = loteDTO.getNroBultoList();
        final List<BigDecimal> cantidadesBultos = loteDTO.getCantidadesBultos();

        final Movimiento movimiento = movimientoService.persistirMovimientoBajaVenta(loteDTO, lote);

        for (int nroBulto : nroBultoList) {
            final Bulto bultoEntity = lote.getBultoByNro(nroBulto);
            final BigDecimal cantidaConsumoBulto = cantidadesBultos.get(nroBulto - 1);

            if (BigDecimal.ZERO.compareTo(cantidaConsumoBulto) == 0) {
                continue;
            }

            bultoEntity.setCantidadActual(bultoEntity.getCantidadActual().subtract(cantidaConsumoBulto));
            lote.setCantidadActual(lote.getCantidadActual().subtract(cantidaConsumoBulto));

            if (bultoEntity.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
                bultoEntity.setEstado(EstadoEnum.CONSUMIDO);
            } else {
                bultoEntity.setEstado(EstadoEnum.EN_USO);
            }

            lote.getTrazas().addAll(bultoEntity.getTrazas());
            bultoService.save(bultoEntity);

            loteDTO.getBultosDTOs().add(DTOUtils.fromBultoEntity(bultoEntity));
        }

        boolean todosConsumidos = lote.getBultos().stream()
            .allMatch(b -> b.getEstado() == EstadoEnum.CONSUMIDO);
        lote.setEstado(todosConsumidos ? EstadoEnum.CONSUMIDO : EstadoEnum.EN_USO);

        loteDTO.getTrazaDTOs().addAll(movimiento.getDetalles()
            .stream()
            .flatMap(d -> d.getTrazas().stream().map(DTOUtils::fromTrazaEntity))
            .toList());

        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
    }

    //***********CU13 ALTA: DEVOLUCION VENTA***********
    @Transactional
    public Lote persistirDevolucionVenta(final MovimientoDTO dto) {

        final Lote lote = loteRepository.findFirstByCodigoInternoAndActivoTrue(dto.getCodigoInternoLote())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        // 1) Crear el movimiento (MODIFICACIÓN) y vincular al movimiento de venta origen
        final Movimiento movDevolucionVenta = movimientoService.persistirMovimientoDevolucionVenta(dto, lote);

        // 2) Agrupar trazas seleccionadas por nro de bulto
        final Map<Integer, List<TrazaDTO>> trazaDTOporBultoMap = dto.getTrazaDTOs().stream()
            .collect(Collectors.groupingBy(TrazaDTO::getNroBulto));

        // 3) Por cada bulto afectado, crear UN DetalleMovimiento y colgar las trazas devueltas
        for (Map.Entry<Integer, List<TrazaDTO>> trazaDTOporBulto : trazaDTOporBultoMap.entrySet()) {
            final Integer trazaDTOnroBulto = trazaDTOporBulto.getKey();
            final List<TrazaDTO> trazasDTOsPorBulto = trazaDTOporBulto.getValue();

            final Bulto bulto = lote.getBultoByNro(trazaDTOnroBulto);

            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movDevolucionVenta)
                .bulto(bulto)
                // Cantidad = cantidad de trazas devueltas (campo NOT NULL) — no impacta stock
                .cantidad(BigDecimal.valueOf(trazasDTOsPorBulto.size()))
                .unidadMedida(UnidadMedidaEnum.UNIDAD)
                .build();

            // Colgar el detalle AL movimiento (habilita cascade para insertar detalle + join table)
            movDevolucionVenta.getDetalles().add(det);

            // Marcar trazas como DEVUELTO y vincularlas al detalle (trazas_detalles)
            for (TrazaDTO t : trazasDTOsPorBulto) {
                final Traza trazaBulto = bulto.getTrazaByNro(t.getNroTraza());
                trazaBulto.setEstado(DEVUELTO);
                det.getTrazas().add(trazaBulto);
            }

            // 4) Si TODAS las trazas del bulto están DEVUELTO ⇒ bulto = DEVUELTO
            final boolean bultoDevuelto = bulto.getTrazas().stream()
                .allMatch(tr -> tr.getEstado() == DEVUELTO);
            if (bultoDevuelto) {
                bulto.setEstado(DEVUELTO);
            }

            // Si no tenés cascade MERGE/UPDATE desde Lote→Bultos, podés guardar el bulto:
             bultoService.save(bulto);
        }

        // 5) Si TODOS los bultos del lote están DEVUELTO ⇒ lote = DEVUELTO
        final boolean loteDevuelto = lote.getBultos().stream()
            .allMatch(b -> b.getEstado() == DEVUELTO);
        if (loteDevuelto) {
            lote.setEstado(DEVUELTO);
        }

        movDevolucionVenta.setCantidad(BigDecimal.valueOf(dto.getTrazaDTOs().size()));
        movDevolucionVenta.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        // 6) Persistir cambios:
        //    - Detalles + join table se guardan via cascade al guardar el movimiento
        lote.getMovimientos().add(movimientoService.save(movDevolucionVenta));
        //    - Lote/bultos/trazas son entidades administradas en la sesión; igual podés asegurar el flush:
        return loteRepository.save(lote);
    }

}

