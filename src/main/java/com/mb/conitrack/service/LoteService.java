package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
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

import lombok.AllArgsConstructor;

import static com.mb.conitrack.utils.MovimientoEntityUtils.addLoteInfoToMovimientoAlta;
import static com.mb.conitrack.utils.MovimientoEntityUtils.crearMovimientoDevolucionCompra;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoCompra;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoProduccion;
import static com.mb.conitrack.utils.MovimientoEntityUtils.populateDetalleMovimientoAlta;
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

            final List<Traza> trazas = lote.getFirstAvailableTrazaList(cantidad.intValue());

            for (Traza traza : trazas) {
                traza.setEstado(EstadoEnum.CONSUMIDO);
                traza.getMovimientos().add(movimiento);
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
            bulto.setEstado(EstadoEnum.DEVUELTO);
            bultoService.save(bulto);
        }

        lote.setEstado(EstadoEnum.DEVUELTO);
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

        // 1) Construir el agregado (lote, bultos, trazas). AÚN SIN DETALLES.
        final Lote lote = loteUtils().createLoteIngreso(loteDTO);
        loteUtils().populateLoteAltaProduccionPropia(lote, loteDTO, producto, conifarma);

        // 2) Persistir Lote y Bultos (para que tengan ID)
        final Lote loteGuardado = loteRepository.save(lote);
        bultoService.save(loteGuardado.getBultos());

        // 3) Crear Movimiento con Lote ya managed (SIN detalles todavía) y persistirlo
        final Movimiento movimiento = createMovimientoAltaIngresoProduccion(loteGuardado);
        movimientoService.save(movimiento); // movimiento con ID

        // 4) Crear DetalleMovimiento SOLO en la colección del Movimiento (UNA SOLA RUTA)
        //    ¡NO agregues a bulto.getDetalles()!
        for (Bulto bultoPersistido : loteGuardado.getBultos()) {
            final DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(bultoPersistido)
                .cantidad(bultoPersistido.getCantidadInicial())
                .unidadMedida(bultoPersistido.getUnidadMedida())
                .build();

            movimiento.getDetalles().add(det); // <-- solo en movimiento
            // NO: bultoPersistido.getDetalles().add(det);
        }

        // Persistir detalles vía cascade desde Movimiento (o usa detalleService.saveAll si no hay cascade)
        movimientoService.save(movimiento);

        // 5) Trazas (asegurar FK de bulto set, ver fix previo) + vínculo al movimiento
        for (Traza traza : loteGuardado.getTrazas()) {
            traza.setLote(loteGuardado);
            traza.getMovimientos().add(movimiento);
            // IMPORTANTE (si aplica a tu modelo): traza.setBulto(...) ya se hace
            // en populateLoteAltaProduccionPropia cuando se reparten a bultos.
        }
        trazaService.save(loteGuardado.getTrazas());

        return loteGuardado;
    }

    //***********CU11 MODIFICACION: LIBERACION DE PRODUCTO***********
    @Transactional
    public List<Lote> persistirLiberacionProducto(final MovimientoDTO dto, final List<Lote> lotes) {

        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final Movimiento movimiento = movimientoService.persistirMovimientoLiberacionProducto(dto, loteBulto);

            loteBulto.setFechaReanalisisProveedor(loteBulto.getFechaReanalisisVigente());
            loteBulto.setFechaVencimientoProveedor(loteBulto.getFechaVencimientoVigente());

            loteBulto.setDictamen(movimiento.getDictamenFinal());
            loteBulto.getMovimientos().add(movimiento);
            result.add(loteRepository.save(loteBulto));
        }
        return result;
    }

    //***********CU12 BAJA: VENTA***********
    @Transactional
    public List<Lote> bajaVentaProducto(final LoteDTO loteDTO) {
        List<Lote> result = new ArrayList<>();

        for (int nroBulto : loteDTO.getNroBultoList()) {
            //TODO: Fix
            final Lote bulto = loteRepository.findFirstByCodigoInternoAndActivoTrue(
                    loteDTO.getCodigoInternoLote())
                .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

            final Movimiento movimiento = movimientoService.persistirMovimientoBajaVenta(loteDTO, bulto);
            bulto.setCantidadActual(UnidadMedidaUtils.restarMovimientoConvertido(
                DTOUtils.fromMovimientoEntity(movimiento),
                bulto));

            if (bulto.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
                bulto.setEstado(EstadoEnum.VENDIDO);
            } else {
                bulto.setEstado(EstadoEnum.EN_USO);
            }
            loteDTO.getTrazaDTOs().addAll(movimiento.getTrazas().stream().map(DTOUtils::fromTrazaEntity).toList());
            bulto.getMovimientos().add(movimiento);
            Lote newLote = loteRepository.save(bulto);
            result.add(newLote);
        }
        return result;
    }

    //***********CU13 ALTA: DEVOLUCION VENTA***********
    @Transactional
    public List<Lote> altaStockDevolucionVenta(final MovimientoDTO dto) {
        List<Lote> result = new ArrayList<>();
        Lote lote = loteRepository.findById(dto.getLoteId())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        Lote loteDevolucion = loteUtils().createLoteDevolucionVenta(lote);
        loteDevolucion.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        loteDevolucion.setCodigoInterno("L-" +
            loteDevolucion.getProducto().getTipoProducto() +
            "-" +
            loteDevolucion.getFechaYHoraCreacion());

        //
        //        String timestampLoteDTO = loteDTO.getFechaYHoraCreacion()
        //            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        //        final BigDecimal cantidadInicialLote = loteDTO.getCantidadInicial();
        //
        //        boolean unidadVenta = producto.getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA;
        //
        //        List<Traza> trazas = createTrazas(loteDTO, producto, cantidadInicialLote, unidadVenta);
        //
        //        int idxTrazaActual = 0;
        //
        //        for (int i = 0; i < bultosTotales; i++) {
        //
        //            Lote bultoLote = createLoteIngreso(loteDTO);
        //            populateInfoProduccion(bultoLote, producto, timestampLoteDTO, conifarma);
        //
        //            //seteo cantidades y unidades de medida para cada bulto del lote
        //            populateCantidadUdeMLote(loteDTO, bultosTotales, bultoLote, i);
        //
        //            List<Traza> trazasLocales = new ArrayList<>();
        //            if (unidadVenta) {
        //                final int indexTrazaFinal = bultoLote.getCantidadInicial().intValue();
        //                trazasLocales = new ArrayList<>(trazas.subList(idxTrazaActual, idxTrazaActual + indexTrazaFinal));
        //                bultoLote.getTrazas().addAll(trazasLocales);
        //                idxTrazaActual += indexTrazaFinal;
        //            }
        //
        //            Lote bultoGuardado = loteRepository.save(bultoLote);
        //            final Movimiento movimiento = movimientoService.persistirMovimientoAltaIngresoProduccion(bultoGuardado);
        //            bultoGuardado.getMovimientos().add(movimiento);
        //
        //            if (!trazasLocales.isEmpty()) {
        //                for (Traza traza : trazasLocales) {
        //                    traza.getMovimientos().add(movimiento);
        //                    traza.setLote(bultoGuardado);
        //                }
        //                bultoGuardado.getTrazas().addAll(trazasLocales);
        //                trazaService.save(trazasLocales);
        //            }
        //            result.add(bultoGuardado);
        //        }
        result.add(loteDevolucion);
        return result;
    }

}

