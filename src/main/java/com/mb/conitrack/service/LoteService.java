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
import com.mb.conitrack.entity.DetalleMovimiento;
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
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.utils.EntityUtils;
import com.mb.conitrack.utils.UnidadMedidaUtils;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.utils.EntityUtils.addLoteInfoToMovimientoAlta;
import static com.mb.conitrack.utils.EntityUtils.crearMovimientoDevolucionCompra;
import static com.mb.conitrack.utils.EntityUtils.createMovimientoAltaIngresoCompra;
import static com.mb.conitrack.utils.EntityUtils.populateDetalleMovimiento;
import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMenorUnidadMedida;

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

    //***********CU1 ALTA: COMPRA***********
    @Transactional
    public Lote altaStockPorCompra(LoteDTO loteDTO) {
        Proveedor proveedor = proveedorService.findById(loteDTO.getProveedorId())
            .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));

        Producto producto = productoService.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        Lote lote = EntityUtils.getInstance().createLoteIngreso(loteDTO);
        populateLoteAltaStockCompra(lote, loteDTO, producto, proveedor);

        Lote loteGuardado = loteRepository.save(lote);     // ✔ ahora el Lote ya está “managed”
        final List<Bulto> bultosGuardados = loteGuardado.getBultos();
        bultoService.save(bultosGuardados);       // ✔ ahora los Bultos referencian un Lote persistido

        final Movimiento movimientoAltaIngresoCompra = createMovimientoAltaIngresoCompra(loteDTO);
        addLoteInfoToMovimientoAlta(loteGuardado, movimientoAltaIngresoCompra);

        final Movimiento movimientoGuardado = movimientoService.save(movimientoAltaIngresoCompra);
        loteGuardado.getMovimientos().add(movimientoGuardado);

        return loteGuardado;
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    public List<Lote> findAllForCuarentena() {
        //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDate();
        return allSortByDateAndNroBulto.stream().filter(l -> EnumSet.of(
            DictamenEnum.RECIBIDO,
            DictamenEnum.APROBADO,
            DictamenEnum.ANALISIS_EXPIRADO,
            DictamenEnum.LIBERADO,
            DictamenEnum.DEVOLUCION_CLIENTES,
            DictamenEnum.RETIRO_MERCADO).contains(l.getDictamen())).toList();
    }

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
    public List<Lote> findAllForMuestreo() {
        return findAllSortByDate().stream()
            .filter(l -> l.getDictamen() != DictamenEnum.RECIBIDO)
            .filter(l -> l.getAnalisisList().stream().anyMatch(a -> a.getNroAnalisis() != null))
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    @Transactional
    public Lote bajaMuestreo(final MovimientoDTO dto, final Bulto bulto) {

        //Si el producto esta en estado Recibido debo pasarlo a Cuarentena antes
        //Si tengo un numero de reanalisis, es que necesito crear un nuevo analisis para el producto

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
    public List<Lote> findAllForDevolucionCompra() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDate();
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
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

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

        for (Analisis analisis :lote.getAnalisisList()){
            if(analisis.getDictamen() == null){
                analisis.setDictamen(DictamenEnum.ANULADO);
                analisisService.save(analisis);
            }
        }

        return loteRepository.save(lote);
    }

    //************************************************************************

    //TODO: unificar la logica de activo vs todos para operatoria vs auditoria
    public Lote findLoteBultoById(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo.");
        }
        return loteRepository.findById(id)
            .filter(Lote::getActivo)
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    public Optional<Lote> findLoteByCodigoInterno(final String codigoInternoLote) {
        if (codigoInternoLote == null) {
            return null;
        }
        return loteRepository.findByCodigoInternoAndActivoTrue(codigoInternoLote);
    }

    public List<Lote> findLoteListByCodigoInterno(final String codigoInternoLote) {
        if (codigoInternoLote == null) {
            return new ArrayList<>();
        }
        return loteRepository.findAllByCodigoInternoAndActivoTrue(codigoInternoLote);
    }

    public List<Lote> findAllSortByDateAndCodigoInternoAudit() {
        return loteRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Lote> findAllSortByDate() {
        return loteRepository.findAll()
            .stream()
            .filter(Lote::getActivo)
            .sorted(Comparator.comparing(Lote::getFechaIngreso).thenComparing(Lote::getCodigoInterno))
            .toList();
    }

    public List<Lote> findAllLotesDictaminados() {
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaVencimientoVigente() != null || lote.getFechaReanalisisVigente() != null)
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
    }

    public List<Lote> findAllLotesVencidos() {
        final LocalDate now = LocalDate.now();
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaVencimientoVigente() != null &&
                !lote.getFechaVencimientoVigente().isBefore(now))
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
    }

    public List<Lote> findAllLotesAnalisisExpirado() {
        final LocalDate now = LocalDate.now();
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaReanalisisVigente() != null && !lote.getFechaReanalisisVigente().isBefore(now))
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
    }

    //Getters para el Front
    public Optional<Lote> save(final Lote lote) {
        throw new UnsupportedOperationException("No se permite la persistencia de Lote desde el Front.");
        //        final Lote nuevoLote = loteRepository.save(lote);
        //        return Optional.of(nuevoLote);
    }

    //***********CUZ MODIFICACION: Reanalisis de Producto Aprobado***********
    public List<Lote> findAllForReanalisisProducto() {
        //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDate();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> l.getDictamen() == DictamenEnum.APROBADO)
            .filter(l -> l.getAnalisisList()
                .stream()
                .noneMatch(a -> a.getDictamen() == null && a.getFechaRealizado() == null))
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
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //***********CU7 BAJA: CONSUMO PRODUCCION***********
    public List<Lote> findAllForConsumoProduccion() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDate();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.APROBADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA != l.getProducto().getTipoProducto())
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //***********CU11 MODIFICACION: LIBERACIÓN UNIDAD DE VENTA***********
    public List<Lote> findAllForLiberacionProducto() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDate();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.APROBADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA == l.getProducto().getTipoProducto())
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //***********CU11 MODIFICACION: LIBERACIÓN UNIDAD DE VENTA***********
    public List<Lote> findAllForVentaProducto() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDate();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.LIBERADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA == l.getProducto().getTipoProducto())
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //Persistencia

    public Long findMaxNroTraza(Long idProducto) {
        return trazaService.findMaxNroTraza(idProducto);
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
            final BigDecimal cantidaConsumoBulto = cantidadesBultos.get(nroBulto);
            final UnidadMedidaEnum uniMedidaConsumoBulto = unidadMedidaBultos.get(nroBulto);

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
        final Movimiento movimiento = movimientoService.persistirMovimientoBajaConsumoProduccion(loteDTO, lote);
        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
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

        Lote lote = EntityUtils.getInstance().createLoteIngreso(loteDTO);
        populateInfoProduccion(lote, producto, timestampLoteDTO, conifarma);

        for (int i = 0; i < bultosTotales; i++) {
            //seteo cantidades y unidades de medida para cada bulto del lote
            //TODO: complete
            final Bulto bulto = new Bulto();
            populateCantidadUdeMLote(loteDTO, bultosTotales, bulto, i);
            lote.getBultos().add(bulto);

            List<Traza> trazasLocales = new ArrayList<>();
            if (unidadVenta) {
                final int indexTrazaFinal = lote.getCantidadInicial().intValue();
                trazasLocales = new ArrayList<>(trazas.subList(idxTrazaActual, idxTrazaActual + indexTrazaFinal));
                lote.getTrazas().addAll(trazasLocales);
                idxTrazaActual += indexTrazaFinal;
            }

            Lote bultoGuardado = loteRepository.save(lote);
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
    public List<Lote> findAllForDevolucionVenta() {
        final List<Lote> result = new ArrayList<>();
        for (Lote lote : findAllSortByDate()) {
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

    public Bulto findBultoByCodigoAndBulto(final String codigoInternoLote, int nroBulto) {
        if (codigoInternoLote == null) {
            throw new IllegalArgumentException("El Codigo Interno no puede ser nulo.");
        }
        final Optional<Lote> maybeLote = loteRepository.findByCodigoInternoAndActivoTrue(
            codigoInternoLote);
        if (maybeLote.isPresent()) {
            return maybeLote.get().getBultos().stream()
                .filter(b -> b.getNroBulto() == nroBulto)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El bulto no existe."));
        }

        throw new IllegalArgumentException("El lote no existe.");
    }

    //TODO: unificar la logica de activo vs todos para operatoria vs auditoria
    public Lote findLoteBultoByCodigoAndBulto(final String codigoInternoLote, int nroBulto) {
        if (codigoInternoLote == null) {
            throw new IllegalArgumentException("El Codigo Interno no puede ser nulo.");
        }
        //TODO: Fix
        return loteRepository.findFirstByCodigoInternoAndActivoTrue(codigoInternoLote)
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    public void populateLoteAltaStockCompra(
        final Lote lote, final LoteDTO loteDTO,
        final Producto producto,
        final Proveedor proveedor) {
        lote.setCodigoInterno("L-" +
            producto.getCodigoInterno() +
            "-" +
            loteDTO.getFechaYHoraCreacion().format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss")));
        Optional<Proveedor> fabricante = Optional.empty();
        if (loteDTO.getFabricanteId() != null) {
            fabricante = proveedorService.findById(loteDTO.getFabricanteId());
            fabricante.ifPresent(lote::setFabricante);
        }

        lote.setProducto(producto);
        lote.setProveedor(proveedor);

        if (StringUtils.isEmptyOrWhitespace(loteDTO.getPaisOrigen())) {
            if (fabricante.isPresent()) {
                lote.setPaisOrigen(fabricante.get().getPais());
            } else {
                lote.setPaisOrigen(proveedor.getPais());
            }
        }
        int bultosTotales = Math.max(loteDTO.getBultosTotales(), 1);
        for (int i = 0; i < bultosTotales; i++) {
            Bulto bulto = EntityUtils.getInstance().createBultoIngreso(loteDTO);
            populateCantidadUdeMBulto(loteDTO, bultosTotales, bulto, i);
            lote.getBultos().add(bulto);
            bulto.setLote(lote);
        }
        lote.setBultosTotales(bultosTotales);
        lote.setCantidadInicial(loteDTO.getCantidadInicial());
        lote.setCantidadActual(loteDTO.getCantidadInicial());
        lote.setUnidadMedida(loteDTO.getUnidadMedida());
    }

    private Lote createLoteDevolucionVenta(final Lote lote) {
        Lote clone = new Lote();
        clone.setEstado(EstadoEnum.DEVUELTO);
        clone.setDictamen(DictamenEnum.DEVOLUCION_CLIENTES);

        clone.setProducto(lote.getProducto());
        clone.setProveedor(lote.getProveedor());
        clone.setFabricante(lote.getFabricante());
        clone.setPaisOrigen(lote.getPaisOrigen());
        clone.setBultosTotales(lote.getBultosTotales());
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
        final BigDecimal cantidadInicialLote,
        boolean unidadVenta) {
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

}

