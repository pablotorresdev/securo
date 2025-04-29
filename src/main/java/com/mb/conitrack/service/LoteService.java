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
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.enums.UnidadMedidaUtils;
import com.mb.conitrack.repository.LoteRepository;

import lombok.AllArgsConstructor;

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

    public Lote findLoteBultoById(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo.");
        }
        return loteRepository.findById(id).filter(Lote::getActivo).orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    public List<Lote> findLoteListByCodigoInterno(final String codigoInterno) {
        if (codigoInterno == null) {
            return new ArrayList<>();
        }
        return loteRepository.findAllByCodigoInternoAndActivoTrue(codigoInterno);
    }

    public List<Lote> findAllSortByDateAndNroBulto() {
        final List<Lote> lotes = loteRepository.findAll();
        lotes.sort(Comparator.comparing(Lote::getFechaIngreso).thenComparing(Lote::getCodigoInterno).thenComparing(Lote::getNroBulto));
        return lotes;
    }

    public List<Lote> findAllLotesDictaminados() {
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaVencimientoVigente() != null || lote.getFechaReanalisisVigente() != null)
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Lote::getFechaIngreso).thenComparing(Lote::getCodigoInterno).thenComparing(Lote::getNroBulto)).toList();
    }

    public List<Lote> findAllLotesVencidos() {
        final LocalDate now = LocalDate.now();
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaVencimientoVigente() != null && !lote.getFechaVencimientoVigente().isBefore(now))
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Lote::getFechaIngreso).thenComparing(Lote::getCodigoInterno).thenComparing(Lote::getNroBulto)).toList();
    }

    public List<Lote> findAllLotesAnalisisExpirado() {
        final LocalDate now = LocalDate.now();
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaReanalisisVigente() != null && !lote.getFechaReanalisisVigente().isBefore(now))
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Lote::getFechaIngreso).thenComparing(Lote::getCodigoInterno).thenComparing(Lote::getNroBulto)).toList();
    }

    //Getters para el Front
    public Optional<Lote> save(final Lote lote) {
        final Lote nuevoLote = loteRepository.save(lote);
        return Optional.of(nuevoLote);
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    public List<Lote> findAllForCuarentena() {
        //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> EnumSet.of(
                    DictamenEnum.RECIBIDO,
                    DictamenEnum.APROBADO,
                    DictamenEnum.ANALISIS_EXPIRADO,
                    DictamenEnum.LIBERADO,
                    DictamenEnum.DEVOLUCION_CLIENTES,
                    DictamenEnum.RETIRO_MERCADO)
                .contains(l.getDictamen()))
            .toList();
    }

    //***********CU2 MODIFICACION: Reanalisis de Producto Aprobado***********
    public List<Lote> findAllForReanalisisProducto() {
        //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> l.getDictamen() ==
                DictamenEnum.APROBADO)
            .filter(l -> l.getAnalisisList().stream()
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
            .filter(l -> EnumSet.of(DictamenEnum.RECIBIDO, DictamenEnum.CUARENTENA, DictamenEnum.APROBADO, DictamenEnum.RECHAZADO).contains(l.getDictamen()))
            .filter(l -> EnumSet.of(TipoProductoEnum.API, TipoProductoEnum.EXCIPIENTE, TipoProductoEnum.ACOND_PRIMARIO, TipoProductoEnum.ACOND_SECUNDARIO)
                .contains(l.getProducto().getTipoProducto()))
            .filter(l -> EstadoEnum.DEVUELTO != l.getEstado())
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    public List<Lote> findAllForResultadoAnalisis() {
        final List<Lote> lotes = loteRepository.findAll();
        return lotes.stream()
            .filter(l -> EnumSet.of(DictamenEnum.CUARENTENA).contains(l.getDictamen()))
            .filter(l -> l.getAnalisisList().stream().anyMatch(a -> a.getDictamen() == null && a.getFechaRealizado() == null))
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    public List<Lote> findAllForConsumoProduccion() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.APROBADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA != l.getProducto().getTipoProducto())
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Lote::getFechaIngreso).thenComparing(Lote::getCodigoInterno).thenComparing(Lote::getNroBulto))
            .toList();
    }

    public Long findMaxNroTraza(Long idProducto) {
        return trazaService.findMaxNroTraza(idProducto);
    }

    //Persistencia

    //***********CU1 ALTA: COMPRA***********
    @Transactional
    public List<Lote> ingresarStockPorCompra(LoteDTO loteDTO) {
        List<Lote> result = new ArrayList<>();
        Proveedor proveedor = proveedorService.findById(loteDTO.getProveedorId()).orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));
        Producto producto = productoService.findById(loteDTO.getProductoId()).orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
        int bultosTotales = Math.max(loteDTO.getBultosTotales(), 1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss");
        String timestamp = loteDTO.getFechaYHoraCreacion().format(formatter);

        for (int i = 0; i < bultosTotales; i++) {
            Lote bultoLote = createLoteIngreso(loteDTO);
            bultoLote.setCodigoInterno("L-" + producto.getTipoProducto() + "-" + timestamp);

            Optional<Proveedor> fabricante = Optional.empty();
            if (loteDTO.getFabricanteId() != null) {
                fabricante = proveedorService.findById(loteDTO.getFabricanteId());
                fabricante.ifPresent(bultoLote::setFabricante);
            }

            bultoLote.setProducto(producto);
            bultoLote.setProveedor(proveedor);

            if (StringUtils.isEmpty(loteDTO.getPaisOrigen())) {
                if (fabricante.isPresent()) {
                    bultoLote.setPaisOrigen(fabricante.get().getPais());
                } else {
                    bultoLote.setPaisOrigen(proveedor.getPais());
                }
            }

            setBultos(loteDTO, bultosTotales, bultoLote, i);

            Lote bultoGuardado = loteRepository.save(bultoLote);
            final Movimiento movimiento = movimientoService.persistirMovimientoAltaIngresoCompra(bultoGuardado);
            bultoGuardado.getMovimientos().add(movimiento);
            result.add(bultoGuardado);
        }
        return result;
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    @Transactional
    public List<Lote> persistirDictamenCuarentena(final MovimientoDTO dto, final List<Lote> lotes) {
        //TODO, eliminar NRO de Reanalisis del DTO
        final String nroAnalisis = StringUtils.isEmpty(dto.getNroReanalisis()) ? dto.getNroAnalisis() : dto.getNroReanalisis();

        Analisis currentAnalisis = analisisService.findByNroAnalisis(nroAnalisis);
        Analisis newAnalisis = null;
        if (currentAnalisis == null) {
            newAnalisis = analisisService.save(DTOUtils.createAnalisis(dto));
        }

        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final String nroAnalisisMovimiento = newAnalisis != null ? newAnalisis.getNroAnalisis() : nroAnalisis;
            final Movimiento movimiento = movimientoService.persistirMovimientoCuarentenaPorAnalisis(dto, loteBulto, nroAnalisisMovimiento);

            loteBulto.setDictamen(movimiento.getDictamenFinal());
            loteBulto.getMovimientos().add(movimiento);

            if (newAnalisis != null) {
                loteBulto.getAnalisisList().add(newAnalisis);
                newAnalisis.getLotes().add(loteBulto);
            }

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
            final Movimiento movimiento = movimientoService.persistirMovimientoReanalisisProducto(dto, loteBulto, newAnalisis.getNroAnalisis());
            loteBulto.getMovimientos().add(movimiento);
            loteBulto.getAnalisisList().add(newAnalisis);
            newAnalisis.getLotes().add(loteBulto);
            result.add(loteRepository.save(loteBulto));
        }
        return result;
    }

    //***********CU3 BAJA: MUESTREO***********
    @Transactional
    //TODO: soportar multimuestreo para simplificar la carga
    public Lote persistirMuestreo(final MovimientoDTO dto, final Lote lote) {

        //Si el producto esta en estado Recibido debo pasarlo a Cuarentena antes
        //Si tengo un numero de reanalisis, es que necesito crear un nuevo analisis para el producto

        final String currentNroAnalisis = lote.getUltimoNroAnalisis();
        if (!currentNroAnalisis.equals(dto.getNroAnalisis())) {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }

        final Movimiento movimiento = movimientoService.persistirMovimientoMuestreo(dto, lote);
        lote.setCantidadActual(UnidadMedidaUtils.restarMovimientoConvertido(dto, lote));
        lote.setEstado(EstadoEnum.EN_USO);
        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional
    public List<Lote> persistirDevolucionCompra(final MovimientoDTO dto, final List<Lote> lotes) {
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
        for (Lote loteBulto : analisis.getLotes()) {
            final Movimiento movimiento = movimientoService.persistirMovimientoResultadoAnalisis(dto, loteBulto);
            loteBulto.setDictamen(movimiento.getDictamenFinal());
            loteBulto.getMovimientos().add(movimiento);
            result.add(loteRepository.save(loteBulto));
        }
        return result;
    }

    @Transactional
    public List<Lote> registrarConsumoProduccion(final LoteDTO loteDTO) {
        List<Lote> result = new ArrayList<>();
        for (int nroBulto : loteDTO.getNroBultoList()) {
            final Lote bulto = loteRepository.findFirstByCodigoInternoAndNroBultoAndActivoTrue(loteDTO.getCodigoInterno(), nroBulto)
                .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));

            final Movimiento movimiento = movimientoService.persistirMovimientoConsumoProduccion(loteDTO, bulto);
            bulto.setCantidadActual(UnidadMedidaUtils.restarMovimientoConvertido(DTOUtils.fromEntity(movimiento), bulto));

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

    @Transactional
    public List<Lote> ingresarStockPorProduccion(final LoteDTO loteDTO) {
        List<Lote> result = new ArrayList<>();
        Proveedor conifarma = proveedorService.getConifarma();
        Producto producto = productoService.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
        int bultosTotales = Math.max(loteDTO.getBultosTotales(), 1);
        if (loteDTO.getBultosTotales() == 1) {
            loteDTO.setNroBulto(1);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss");
        String timestamp = loteDTO.getFechaYHoraCreacion().format(formatter);

        Optional<Long> trazaActual = Optional.ofNullable(loteDTO.getTrazaInicial());
        final BigDecimal cantidadInicialLote = loteDTO.getCantidadInicial();

        if (trazaActual.isPresent()) {
            if (loteDTO.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
                throw new IllegalArgumentException("La traza solo es aplicable a UNIDADES");
            }

            if (cantidadInicialLote.stripTrailingZeros().scale() > 0) {
                throw new IllegalArgumentException("La cantidad de Unidades debe ser entero");
            }
        }

        for (int i = 0; i < bultosTotales; i++) {
            Lote bultoLote = createLoteIngreso(loteDTO);
            bultoLote.setCodigoInterno("L-" + producto.getTipoProducto() + "-" + timestamp);

            bultoLote.setProducto(producto);
            bultoLote.setProveedor(conifarma);
            bultoLote.setFabricante(conifarma);
            bultoLote.setPaisOrigen(conifarma.getPais());

            setBultos(loteDTO, bultosTotales, bultoLote, i);

            Lote bultoGuardado = loteRepository.save(bultoLote);
            final Movimiento movimiento = movimientoService.persistirMovimientoAltaIngresoProduccion(bultoGuardado);
            bultoGuardado.getMovimientos().add(movimiento);

            if (trazaActual.isPresent()) {
                List<Traza> trazasGuardadas = trazaService.persistirTrazasIngresoProduccion(bultoGuardado, trazaActual.get());
                bultoGuardado.getTrazas().addAll(trazasGuardadas);
                trazaActual = trazasGuardadas.stream()
                    .map(Traza::getNroTraza)
                    .max(Long::compareTo)
                    .map(max -> max + 1);
            }

            result.add(bultoGuardado);
        }

        if (trazaActual.isPresent()) {
            if (trazaActual.get() - loteDTO.getTrazaInicial() != cantidadInicialLote.longValueExact()) {
                throw new IllegalStateException("Error al crear las trazas");
            }
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

    private void setBultos(final LoteDTO loteDTO, final int bultosTotales, final Lote bultoLote, final int i) {
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

}

