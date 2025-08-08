package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
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
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.enums.UnidadMedidaUtils;
import com.mb.conitrack.repository.LoteRepository;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.entity.EntityUtils.createBultoIngreso;
import static com.mb.conitrack.entity.EntityUtils.createLoteIngreso;

@AllArgsConstructor
@Service
public class LoteService {

    private final LoteRepository loteRepository;

    private final ProveedorService proveedorService;

    private final ProductoService productoService;

    private final MovimientoService movimientoService;

    private final AnalisisService analisisService;

    private final TrazaService trazaService;

    private static void populateInfoProduccion(
        final Lote bultoLote,
        final Producto producto,
        final String timestampLoteDTO,
        final Proveedor conifarma) {
        bultoLote.setCodigoInterno("L-" + producto.getTipoProducto() + "-" + timestampLoteDTO);
        bultoLote.setProducto(producto);
        bultoLote.setProveedor(conifarma);
        bultoLote.setFabricante(conifarma);
        bultoLote.setPaisOrigen(conifarma.getPais());
    }

    //TODO: unificar la logica de activo vs todos para operatoria vs auditoria
    public Lote findLoteBultoByCodigoAndBulto(final String codigoInternoLote, int nroBulto) {
        if (codigoInternoLote == null) {
            throw new IllegalArgumentException("El Codigo Interno no puede ser nulo.");
        }
        return loteRepository.findFirstByCodigoInternoAndNroBultoAndActivoTrue(codigoInternoLote, nroBulto)
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    //TODO: unificar la logica de activo vs todos para operatoria vs auditoria
    public Lote findLoteBultoById(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo.");
        }
        return loteRepository.findById(id)
            .filter(Lote::getActivo)
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    public List<Lote> findLoteListByCodigoInterno(final String codigoInternoLote) {
        if (codigoInternoLote == null) {
            return new ArrayList<>();
        }
        return loteRepository.findAllByCodigoInternoAndActivoTrue(codigoInternoLote);
    }

    public List<Lote> findAllSortByDateAndNroBultoAudit() {
        return loteRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno)
                .thenComparing(Lote::getNroBulto))
            .toList();
    }

    public List<Lote> findAllSortByDateAndNroBulto() {
        return loteRepository.findAll()
            .stream()
            .filter(Lote::getActivo)
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno)
                .thenComparing(Lote::getNroBulto))
            .toList();
    }

    public List<Lote> findAllLotesDictaminados() {
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaVencimientoVigente() != null || lote.getFechaReanalisisVigente() != null)
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno)
                .thenComparing(Lote::getNroBulto))
            .toList();
    }

    public List<Lote> findAllLotesVencidos() {
        final LocalDate now = LocalDate.now();
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaVencimientoVigente() != null &&
                !lote.getFechaVencimientoVigente().isBefore(now))
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno)
                .thenComparing(Lote::getNroBulto))
            .toList();
    }

    public List<Lote> findAllLotesAnalisisExpirado() {
        final LocalDate now = LocalDate.now();
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaReanalisisVigente() != null && !lote.getFechaReanalisisVigente().isBefore(now))
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno)
                .thenComparing(Lote::getNroBulto))
            .toList();
    }

    //Getters para el Front
    public Optional<Lote> save(final Lote lote) {
        throw new UnsupportedOperationException("No se permite la persistencia de Lote desde el Front.");
        //        final Lote nuevoLote = loteRepository.save(lote);
        //        return Optional.of(nuevoLote);
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    public List<Lote> findAllForCuarentena() {
        //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream().filter(l -> EnumSet.of(
            DictamenEnum.RECIBIDO,
            DictamenEnum.APROBADO,
            DictamenEnum.ANALISIS_EXPIRADO,
            DictamenEnum.LIBERADO,
            DictamenEnum.DEVOLUCION_CLIENTES,
            DictamenEnum.RETIRO_MERCADO).contains(l.getDictamen())).toList();
    }

    //***********CUZ MODIFICACION: Reanalisis de Producto Aprobado***********
    public List<Lote> findAllForReanalisisProducto() {
        //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> l.getDictamen() == DictamenEnum.APROBADO)
            .filter(l -> l.getAnalisisList()
                .stream()
                .noneMatch(a -> a.getDictamen() == null && a.getFechaRealizado() == null))
            .toList();
    }

    //***********CU3 BAJA: MUESTREO***********
    public List<Lote> findAllForMuestreo() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.RECIBIDO != l.getDictamen())
            .filter(l -> l.getAnalisisList().stream().anyMatch(a -> a.getNroAnalisis() != null))
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    public List<Lote> findAllForDevolucionCompra() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> EnumSet.of(
                    DictamenEnum.RECIBIDO,
                    DictamenEnum.CUARENTENA,
                    DictamenEnum.APROBADO,
                    DictamenEnum.RECHAZADO)
                .contains(l.getDictamen()))
            .filter(l -> EnumSet.of(
                TipoProductoEnum.API,
                TipoProductoEnum.EXCIPIENTE,
                TipoProductoEnum.ACOND_PRIMARIO,
                TipoProductoEnum.ACOND_SECUNDARIO).contains(l.getProducto().getTipoProducto()))
            .filter(l -> EstadoEnum.DEVUELTO != l.getEstado())
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    public List<Lote> findAllForResultadoAnalisis() {
        final List<Lote> lotes = loteRepository.findAll();
        return lotes.stream()
            .filter(Lote::getActivo)
            .filter(l -> EnumSet.of(DictamenEnum.CUARENTENA).contains(l.getDictamen()))
            .filter(l -> l.getAnalisisList()
                .stream()
                .anyMatch(a -> a.getDictamen() == null && a.getFechaRealizado() == null))
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    //***********CU7 BAJA: CONSUMO PRODUCCION***********
    public List<Lote> findAllForConsumoProduccion() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.APROBADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA != l.getProducto().getTipoProducto())
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    //***********CU11 MODIFICACION: LIBERACIÓN UNIDAD DE VENTA***********
    public List<Lote> findAllForLiberacionProducto() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.APROBADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA == l.getProducto().getTipoProducto())
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    //***********CU11 MODIFICACION: LIBERACIÓN UNIDAD DE VENTA***********
    public List<Lote> findAllForVentaProducto() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.LIBERADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA == l.getProducto().getTipoProducto())
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    //Persistencia

    public Long findMaxNroTraza(Long idProducto) {
        return trazaService.findMaxNroTraza(idProducto);
    }

    //***********CU1 ALTA: COMPRA***********
    @Transactional
    public Lote altaStockPorCompra(LoteDTO loteDTO) {
        Proveedor proveedor = proveedorService.findById(loteDTO.getProveedorId())
            .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));
        Producto producto = productoService.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss");
        String timestamp = loteDTO.getFechaYHoraCreacion().format(formatter);

        Lote lote = createLoteIngreso(loteDTO);
        populateLote(loteDTO, lote, producto, timestamp, proveedor);

        Lote bultoGuardado = loteRepository.save(lote);
        final Movimiento movimiento = movimientoService.persistirMovimientoAltaIngresoCompra(bultoGuardado);
        bultoGuardado.getMovimientos().add(movimiento);

        return bultoGuardado;
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    @Transactional
    public List<Lote> persistirDictamenCuarentena(final MovimientoDTO dto, final List<Lote> lotes) {
        //TODO, eliminar NRO de Reanalisis del DTO
        final String nroAnalisis = StringUtils.isEmpty(dto.getNroReanalisis())
            ? dto.getNroAnalisis()
            : dto.getNroReanalisis();

        Analisis currentAnalisis = analisisService.findByNroAnalisis(nroAnalisis);
        Analisis newAnalisis = null;
        if (currentAnalisis == null) {
            newAnalisis = analisisService.save(DTOUtils.createAnalisis(dto));
        }

        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final String nroAnalisisMovimiento = newAnalisis != null ? newAnalisis.getNroAnalisis() : nroAnalisis;
            final Movimiento movimiento = movimientoService.persistirMovimientoCuarentenaPorAnalisis(
                dto,
                loteBulto,
                nroAnalisisMovimiento);

            loteBulto.setDictamen(movimiento.getDictamenFinal());
            loteBulto.getMovimientos().add(movimiento);

            if (newAnalisis != null) {
                loteBulto.getAnalisisList().add(newAnalisis);
                newAnalisis.setLote(loteBulto);
            }

            result.add(loteRepository.save(loteBulto));
        }
        return result;
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

    //***********CU> MODIFICACION: CUZ Reanalisis de Producto Aprobado***********
    @Transactional
    public List<Lote> persistirReanalisisProducto(final MovimientoDTO dto, final List<Lote> lotes) {
        final Analisis analisis = DTOUtils.createAnalisis(dto);
        final Analisis newAnalisis = analisisService.save(analisis);
        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final Movimiento movimiento = movimientoService.persistirMovimientoReanalisisProducto(
                dto,
                loteBulto,
                newAnalisis.getNroAnalisis());
            loteBulto.getMovimientos().add(movimiento);
            loteBulto.getAnalisisList().add(newAnalisis);
            newAnalisis.setLote(loteBulto);
            result.add(loteRepository.save(loteBulto));
        }
        return result;
    }

    //***********CU3 BAJA: MUESTREO***********
    @Transactional
    //TODO: soportar multimuestreo para simplificar la carga
    public Lote bajaMuestreo(final MovimientoDTO dto, final Lote lote) {

        //Si el producto esta en estado Recibido debo pasarlo a Cuarentena antes
        //Si tengo un numero de reanalisis, es que necesito crear un nuevo analisis para el producto

        final String currentNroAnalisis = lote.getUltimoNroAnalisis();
        if (!currentNroAnalisis.equals(dto.getNroAnalisis())) {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }

        final Movimiento movimiento = movimientoService.persistirMovimientoMuestreo(dto, lote);
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
            dto.setTrazaDTOs(trazas.stream().map(DTOUtils::fromEntity).toList());
        }

        if (lote.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
            lote.setEstado(EstadoEnum.CONSUMIDO);
        } else {
            lote.setEstado(EstadoEnum.EN_USO);
        }

        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional
    public List<Lote> bajaDevolucionCompra(final MovimientoDTO dto, final List<Lote> lotes) {
        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final Movimiento movimiento = movimientoService.persistirMovimientoDevolucionCompra(dto, loteBulto);
            loteBulto.setCantidadActual(BigDecimal.ZERO);
            loteBulto.setEstado(EstadoEnum.DEVUELTO);
            loteBulto.setDictamen(DictamenEnum.RECHAZADO);
            loteBulto.getMovimientos().add(movimiento);
            Lote newLote = loteRepository.save(loteBulto);
            result.add(newLote);
        }
        return result;
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    @Transactional
    public List<Lote> persistirResultadoAnalisis(final MovimientoDTO dto) {
        final Analisis analisis = analisisService.addResultadoAnalisis(dto);
        List<Lote> result = new ArrayList<>();
        final Lote lote = analisis.getLote();
        final Movimiento movimiento = movimientoService.persistirMovimientoResultadoAnalisis(dto, lote);
        lote.setDictamen(movimiento.getDictamenFinal());
        lote.getMovimientos().add(movimiento);
        result.add(loteRepository.save(lote));
        return result;
    }

    //***********CU7 BAJA: CONSUMO PRODUCCION***********
    @Transactional
    public List<Lote> bajaConsumoProduccion(final LoteDTO loteDTO) {
        List<Lote> result = new ArrayList<>();
        for (int nroBulto : loteDTO.getNroBultoList()) {
            final Lote bulto = loteRepository.findFirstByCodigoInternoAndNroBultoAndActivoTrue(
                    loteDTO.getCodigoInternoLote(),
                    nroBulto)
                .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

            final Movimiento movimiento = movimientoService.persistirMovimientoBajaConsumoProduccion(loteDTO, bulto);
            bulto.setCantidadActual(UnidadMedidaUtils.restarMovimientoConvertido(
                DTOUtils.fromEntity(movimiento),
                bulto));

            if (bulto.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
                bulto.setEstado(EstadoEnum.CONSUMIDO);
            } else {
                bulto.setEstado(EstadoEnum.EN_USO);
            }
            bulto.getMovimientos().add(movimiento);
            Lote newLote = loteRepository.save(bulto);
            result.add(newLote);
        }
        return result;
    }

    //***********CU10 ALTA: PRODUCCION INTERNA***********
    @Transactional
    public List<Lote> altaStockPorProduccion(final LoteDTO loteDTO) {
        List<Lote> result = new ArrayList<>();
        Proveedor conifarma = proveedorService.getConifarma();
        Producto producto = productoService.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        int bultosTotales = Math.max(loteDTO.getBultosTotales(), 1);

        String timestampLoteDTO = loteDTO.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        final BigDecimal cantidadInicialLote = loteDTO.getCantidadInicial();

        boolean unidadVenta = producto.getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA;

        List<Traza> trazas = createTrazas(loteDTO, producto, cantidadInicialLote, unidadVenta);

        int idxTrazaActual = 0;

        for (int i = 0; i < bultosTotales; i++) {

            Lote bultoLote = createLoteIngreso(loteDTO);
            populateInfoProduccion(bultoLote, producto, timestampLoteDTO, conifarma);

            //seteo cantidades y unidades de medida para cada bulto del lote
            populateCantidadUdeMLote(loteDTO, bultosTotales, bultoLote, i);

            List<Traza> trazasLocales = new ArrayList<>();
            if (unidadVenta) {
                final int indexTrazaFinal = bultoLote.getCantidadInicial().intValue();
                trazasLocales = new ArrayList<>(trazas.subList(idxTrazaActual, idxTrazaActual + indexTrazaFinal));
                bultoLote.getTrazas().addAll(trazasLocales);
                idxTrazaActual += indexTrazaFinal;
            }

            Lote bultoGuardado = loteRepository.save(bultoLote);
            final Movimiento movimiento = movimientoService.persistirMovimientoAltaIngresoProduccion(bultoGuardado);
            bultoGuardado.getMovimientos().add(movimiento);

            if (!trazasLocales.isEmpty()) {
                for (Traza traza : trazasLocales) {
                    traza.getMovimientos().add(movimiento);
                    traza.setLote(bultoGuardado);
                }
                bultoGuardado.getTrazas().addAll(trazasLocales);
                trazaService.save(trazasLocales);
            }
            result.add(bultoGuardado);
        }

        return result;
    }

    //***********CU9 MODIFICACION: VENCIDO***********
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

    //***********CU9 MODIFICACION: VENCIDO***********
    @Transactional
    public List<Lote> persistirExpiracionAnalisis(final MovimientoDTO dto, final List<Lote> lotes) {
        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final Movimiento movimiento = movimientoService.persistirMovimientoExpiracionAnalisis(dto, loteBulto);
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
            final Lote bulto = loteRepository.findFirstByCodigoInternoAndNroBultoAndActivoTrue(
                    loteDTO.getCodigoInternoLote(),
                    nroBulto)
                .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

            final Movimiento movimiento = movimientoService.persistirMovimientoBajaVenta(loteDTO, bulto);
            bulto.setCantidadActual(UnidadMedidaUtils.restarMovimientoConvertido(
                DTOUtils.fromEntity(movimiento),
                bulto));

            if (bulto.getCantidadActual().compareTo(BigDecimal.ZERO) == 0) {
                bulto.setEstado(EstadoEnum.VENDIDO);
            } else {
                bulto.setEstado(EstadoEnum.EN_USO);
            }
            loteDTO.getTrazaDTOs().addAll(movimiento.getTrazas().stream().map(DTOUtils::fromEntity).toList());
            bulto.getMovimientos().add(movimiento);
            Lote newLote = loteRepository.save(bulto);
            result.add(newLote);
        }
        return result;
    }

    //***********CU13 ALTA: DEVOLUCION VENTA***********
    public List<Lote> findAllForDevolucionVenta() {
        final List<Lote> result = new ArrayList<>();
        for (Lote lote : findAllSortByDateAndNroBulto()) {
            if (lote.getEstado() == EstadoEnum.VENDIDO) {
                result.add(lote);
                continue;
            }
            boolean containsVenta = false;
            final List<Movimiento> movimientos = lote.getMovimientos();
            for (Movimiento movimiento : movimientos) {
                if (movimiento.getMotivo() == MotivoEnum.VENTA) {
                    containsVenta = true;
                    break;
                }
            }
            if (containsVenta) {
                result.add(lote);
            }
        }
        return result;
    }

    //***********CU13 ALTA: DEVOLUCION VENTA***********
    @Transactional
    public List<Lote> altaStockDevolucionVenta(final MovimientoDTO dto) {
        List<Lote> result = new ArrayList<>();
        Lote lote = loteRepository.findById(dto.getLoteId())
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

        Lote loteDevolucion = createLoteDevolucionVenta(lote);
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

    private Lote createLoteDevolucionVenta(final Lote lote) {
        Lote clone = new Lote();
        clone.setEstado(EstadoEnum.DEVUELTO);
        clone.setDictamen(DictamenEnum.DEVOLUCION_CLIENTES);

        clone.setProducto(lote.getProducto());
        clone.setProveedor(lote.getProveedor());
        clone.setFabricante(lote.getFabricante());
        clone.setPaisOrigen(lote.getPaisOrigen());
        clone.setNroBulto(lote.getNroBulto());
        clone.setBultosTotales(lote.getBultosTotales());
        clone.setUnidadMedida(lote.getUnidadMedida());
        clone.setLoteProveedor(lote.getLoteProveedor());
        clone.setFechaReanalisisProveedor(lote.getFechaReanalisisProveedor());
        clone.setFechaVencimientoProveedor(lote.getFechaVencimientoProveedor());
        clone.setAnalisisList(lote.getAnalisisList());
        clone.setTrazas(lote.getTrazas());
        clone.setLoteOrigen(lote);
        clone.setDetalleConservacion(lote.getDetalleConservacion());
        clone.setFechaIngreso(lote.getFechaIngreso());
        clone.setObservaciones("DEVOLUCIÓN de lote " + lote.getCodigoInterno());
        clone.setActivo(true);
        return clone;
    }

    private List<Traza> createTrazas(
        final LoteDTO loteDTO,
        final Producto producto,
        final BigDecimal cantidadInicialLote, boolean unidadVenta) {
        List<Traza> trazas = new ArrayList<>();
        if (unidadVenta) {
            if (loteDTO.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
                throw new IllegalStateException("La traza solo es aplicable a UNIDADES");
            }

            if (cantidadInicialLote.stripTrailingZeros().scale() > 0) {
                throw new IllegalStateException("La cantidad de Unidades debe ser entero");
            }
            for (int i = 0; i < cantidadInicialLote.intValue(); i++) {
                Traza traza = new Traza();
                traza.setNroTraza(loteDTO.getTrazaInicial() + i);
                traza.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
                traza.setProducto(producto);
                traza.setObservaciones("CU7 Traza: " +
                    traza.getNroTraza() +
                    "\n - Producto: " +
                    producto.getCodigoInterno() +
                    " / " +
                    producto.getNombreGenerico());
                traza.setEstado(EstadoEnum.DISPONIBLE);
                traza.setActivo(true);
                trazas.add(traza);
            }
        }
        return trazas;
    }

    private void populateCantidadUdeMBulto(
        final LoteDTO loteDTO,
        final int bultosTotales,
        final Bulto bulto,
        final int i) {
        if (bultosTotales == 1) {
            bulto.setCantidadInicial(loteDTO.getCantidadInicial());
            bulto.setCantidadActual(loteDTO.getCantidadInicial());
            bulto.setUnidadMedida(loteDTO.getUnidadMedida());
        } else {
            bulto.setCantidadInicial(loteDTO.getCantidadesBultos().get(i));
            bulto.setCantidadActual(loteDTO.getCantidadesBultos().get(i));
            bulto.setUnidadMedida(loteDTO.getUnidadMedidaBultos().get(i));
        }
        bulto.setNroBulto(i + 1);
    }

    private void populateCantidadUdeMLote(
        final LoteDTO loteDTO,
        final int bultosTotales,
        final Lote bultoLote,
        final int i) {
        if (bultosTotales == 1) {
            bultoLote.setCantidadInicial(loteDTO.getCantidadInicial());
            bultoLote.setCantidadActual(loteDTO.getCantidadInicial());
            bultoLote.setUnidadMedida(loteDTO.getUnidadMedida());
        } else {
            bultoLote.setCantidadInicial(loteDTO.getCantidadesBultos().get(i));
            bultoLote.setCantidadActual(loteDTO.getCantidadesBultos().get(i));
            bultoLote.setUnidadMedida(loteDTO.getUnidadMedidaBultos().get(i));
        }
        bultoLote.setNroBulto(i + 1);
    }

    private void populateLote(
        final LoteDTO loteDTO,
        final Lote lote,
        final Producto producto,
        final String timestamp,
        final Proveedor proveedor) {
        lote.setCodigoInterno("L-" + producto.getTipoProducto() + "-" + timestamp);
        Optional<Proveedor> fabricante = Optional.empty();
        if (loteDTO.getFabricanteId() != null) {
            fabricante = proveedorService.findById(loteDTO.getFabricanteId());
            fabricante.ifPresent(lote::setFabricante);
        }

        lote.setProducto(producto);
        lote.setProveedor(proveedor);

        if (StringUtils.isEmpty(loteDTO.getPaisOrigen())) {
            if (fabricante.isPresent()) {
                lote.setPaisOrigen(fabricante.get().getPais());
            } else {
                lote.setPaisOrigen(proveedor.getPais());
            }
        }
        int bultosTotales = Math.max(loteDTO.getBultosTotales(), 1);
        for (int i = 0; i < bultosTotales; i++) {
            Bulto bulto = createBultoIngreso(loteDTO);
            populateCantidadUdeMBulto(loteDTO, bultosTotales, bulto, i);
            lote.getBultos().add(bulto);
        }
    }

}

