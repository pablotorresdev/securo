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
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoLoteEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaUtils;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class LoteService {

    private final LoteRepository loteRepository;

    private final ProveedorRepository proveedorRepository;

    private final ProductoRepository productoRepository;

    private final MovimientoService movimientoService;

    private final AnalisisService analisisService;

    //Getters
    public Lote findLoteBultoById(final Long id) {
        return loteRepository.findById(id).filter(Lote::getActivo).orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    public List<Lote> findLoteListById(final Long id) {
        Lote lote = loteRepository.findById(id).filter(Lote::getActivo).orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
        return loteRepository.findAllByCodigoInternoAndActivoTrue(lote.getCodigoInterno());
    }

    public List<Lote> findAllForMuestreo() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> EnumSet.of(
                DictamenEnum.RECIBIDO,
                DictamenEnum.CUARENTENA,
                DictamenEnum.DEVOLUCION_CLIENTES,
                DictamenEnum.RETIRO_MERCADO
            ).contains(l.getDictamen()))
            .toList();
    }

    //Getters
    public List<Lote> findAllSortByDateAndNroBulto() {
        final List<Lote> lotes = loteRepository.findAll();
        lotes.sort(Comparator
            .comparing(Lote::getFechaIngreso)
            .thenComparing(Lote::getCodigoInterno)
            .thenComparing(Lote::getNroBulto));
        return lotes;
    }

    //*****************************************************************

    public List<Lote> findAllForCuarentena() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> EnumSet.of(
                DictamenEnum.RECIBIDO,
                DictamenEnum.APROBADO,
                DictamenEnum.CUARENTENA,
                DictamenEnum.LIBERADO,
                DictamenEnum.DEVOLUCION_CLIENTES,
                DictamenEnum.RETIRO_MERCADO
            ).contains(l.getDictamen()))
            .toList();
    }

    public List<Lote> findAllForDevolucionCompra() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> EnumSet.of(
                DictamenEnum.RECIBIDO,
                DictamenEnum.CUARENTENA,
                DictamenEnum.APROBADO,
                DictamenEnum.RECHAZADO
            ).contains(l.getDictamen()))
            .filter(l -> EnumSet.of(
                TipoProductoEnum.API,
                TipoProductoEnum.EXCIPIENTE,
                TipoProductoEnum.ACOND_PRIMARIO,
                TipoProductoEnum.ACOND_SECUNDARIO
            ).contains(l.getProducto().getTipoProducto()))
            .toList();
    }

    public Optional<Lote> save(final Lote lote) {
        final Lote nuevoLote = loteRepository.save(lote);
        return Optional.of(nuevoLote);
    }

    public List<Lote> findAllForResultadoAnalisis() {
        final List<Lote> lotes = loteRepository.findAll();
        return lotes.stream()
            .filter(l -> EnumSet.of(DictamenEnum.CUARENTENA).contains(l.getDictamen()))
            .filter(l -> l.getAnalisisList()
                .stream()
                .filter(analisis -> analisis.getDictamen() != null)
                .filter(analisis -> analisis.getFechaRealizado() != null)
                .toList()
                .isEmpty())
            .toList();
    }

    //***********CU1 ALTA: COMPRA***********
    @Transactional
    public List<Lote> ingresarStockPorCompra(LoteDTO dto) {
        if (dto.getFechaIngreso().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser posterior al día de hoy.");
        }
        List<Lote> result = new ArrayList<>();
        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId()).orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));
        Producto producto = productoRepository.findById(dto.getProductoId()).orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
        int bultosTotales = Math.max(dto.getBultosTotales(), 1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd_HH");
        String timestamp = dto.getFechaYHoraCreacion().format(formatter);

        for (int i = 0; i < bultosTotales; i++) {
            Lote lote = createLoteIngreso(dto);
            lote.setCodigoInterno("L-" + producto.getTipoProducto() + "-" + timestamp);
            if (dto.getFabricanteId() != null) {
                proveedorRepository.findById(dto.getFabricanteId()).ifPresent(lote::setFabricante);
            }

            lote.setProducto(producto);
            lote.setProveedor(proveedor);

            if (bultosTotales == 1) {
                lote.setCantidadInicial(dto.getCantidadInicial());
                lote.setCantidadActual(dto.getCantidadInicial());
                lote.setUnidadMedida(dto.getUnidadMedida());
            } else {
                lote.setCantidadInicial(dto.getCantidadesBultos().get(i));
                lote.setCantidadActual(dto.getCantidadesBultos().get(i));
                lote.setUnidadMedida(dto.getUnidadMedidaBultos().get(i));
            }
            lote.setNroBulto(i + 1);

            Lote loteGuardado = loteRepository.save(lote);
            final Movimiento movimiento = movimientoService.persistirMovimientoAltaIngresoCompra(loteGuardado);
            loteGuardado.getMovimientos().add(movimiento);
            result.add(loteGuardado);
        }
        return result;
    }

    private static Lote createLoteIngreso(final LoteDTO dto) {
        Lote lote = new Lote();

        //Datos CU1
        lote.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        lote.setEstadoLote(EstadoLoteEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setActivo(Boolean.TRUE);

        //Datos obligatorios comunes
        lote.setPaisOrigen(dto.getPaisOrigen());
        lote.setFechaIngreso(dto.getFechaIngreso());
        lote.setBultosTotales(dto.getBultosTotales());
        lote.setLoteProveedor(dto.getLoteProveedor());

        //Datos opcionales comunes
        lote.setFechaReanalisisProveedor(dto.getFechaReanalisisProveedor());
        lote.setFechaVencimientoProveedor(dto.getFechaVencimientoProveedor());
        lote.setNroRemito(dto.getNroRemito());
        lote.setDetalleConservacion(dto.getDetalleConservacion());
        lote.setObservaciones(dto.getObservaciones());

        return lote;
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    @Transactional
    public List<Lote> persistirDictamenCuarentena(final List<Lote> lotes, final MovimientoDTO dto) {
        final Analisis analisis = DTOUtils.createAnalisis(dto);
        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final Movimiento movimiento = movimientoService.persistirMovimientoCuarentenaPorAnalisis(dto, loteBulto, analisis.getNroAnalisis());

            loteBulto.setDictamen(movimiento.getDictamenFinal());
            loteBulto.getMovimientos().add(movimiento);

            loteBulto.getAnalisisList().add(analisis);
            analisis.getLotes().add(loteBulto);
            analisisService.save(analisis);

            result.add(loteRepository.save(loteBulto));
        }
        return result;
    }

    //***********CU3 BAJA: MUESTREO***********
    @Transactional
    public Optional<Lote> persistirMuestreo(final MovimientoDTO dto, final Lote lote) {

        //Si el producto esta en estado Recibido debo crear un Analisis y persistir Analisis, Movimiento y Lote
        //Si tengo un numero de reanalisis, es que necesito crear un nuevo analisis para el producto
        if (DictamenEnum.RECIBIDO.equals(lote.getDictamen()) || !StringUtils.isEmpty(dto.getNroReanalisis())) {

            final boolean esNuevoAnalisis = lote.getAnalisisList().stream()
                .noneMatch(a -> a.getNroAnalisis().equalsIgnoreCase(dto.getNroAnalisis()));

            Analisis analisis = null;
            if (esNuevoAnalisis) {
                analisis = DTOUtils.createAnalisis(dto);
                analisisService.save(analisis);
            }

            final List<Lote> allBultosById = loteRepository.findAllByCodigoInternoAndActivoTrue(lote.getCodigoInterno());
            for (Lote loteBulto : allBultosById) {

                final Movimiento movimiento = movimientoService.persistirMovimientoCuarentenaPorMuestreo(dto, loteBulto);
                loteBulto.setDictamen(movimiento.getDictamenFinal());
                loteBulto.setEstadoLote(EstadoLoteEnum.EN_USO);
                loteBulto.getMovimientos().add(movimiento);

                if (esNuevoAnalisis) {
                    loteBulto.getAnalisisList().add(analisis);
                }

                loteRepository.save(loteBulto);
            }
        }

        final Movimiento movimiento = movimientoService.persistirMovimientoMuestreo(dto, lote);
        lote.setCantidadActual(UnidadMedidaUtils.calcularCantidadActual(dto, lote));
        lote.setEstadoLote(EstadoLoteEnum.EN_USO);
        lote.getMovimientos().add(movimiento);
        return Optional.of(loteRepository.save(lote));
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional
    public List<Lote> persistirDevolucionCompra(final MovimientoDTO dto, final Lote lote) {
        final List<Lote> byCodigoInternoAndActivoTrue = loteRepository.findAllByCodigoInternoAndActivoTrue(lote.getCodigoInterno());

        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : byCodigoInternoAndActivoTrue) {
            final Movimiento movimiento = movimientoService.persistirMovimientoDevolucionCompra(dto, loteBulto);
            loteBulto.setCantidadActual(BigDecimal.ZERO);
            loteBulto.setEstadoLote(EstadoLoteEnum.DEVUELTO);
            loteBulto.getMovimientos().add(movimiento);
            Lote newLote = loteRepository.save(loteBulto);
            result.add(newLote);
        }
        return result;
    }

    @Transactional
    public List<Lote> persistirDictamenResultado(final List<Lote> lotes, final MovimientoDTO dto) {
        analisisService.addDictamenResultado(dto);
        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final Movimiento movimiento = movimientoService.persistirMovimientoResultadoAnalisis(dto, loteBulto);
            loteBulto.setDictamen(movimiento.getDictamenFinal());
            loteBulto.getMovimientos().add(movimiento);
            result.add(loteRepository.save(loteBulto));
        }
        return result;
    }

}

