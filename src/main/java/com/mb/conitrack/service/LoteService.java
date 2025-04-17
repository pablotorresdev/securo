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

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaUtils;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.entity.EntityUtils.createLoteIngreso;

@AllArgsConstructor
@Service
public class LoteService {

    private final LoteRepository loteRepository;

    private final ProveedorRepository proveedorRepository;

    private final ProductoRepository productoRepository;

    private final MovimientoService movimientoService;

    private final AnalisisService analisisService;

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
        lotes.sort(Comparator
            .comparing(Lote::getFechaIngreso)
            .thenComparing(Lote::getCodigoInterno)
            .thenComparing(Lote::getNroBulto));
        return lotes;
    }

    public Optional<Lote> save(final Lote lote) {
        final Lote nuevoLote = loteRepository.save(lote);
        return Optional.of(nuevoLote);
    }

    //Getters para el Front

    //***********CU2 MODIFICACION: CUARENTENA***********
    public List<Lote> findAllForCuarentena() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> EnumSet.of(
                DictamenEnum.RECIBIDO,
                DictamenEnum.APROBADO,
                DictamenEnum.ANALISIS_EXPIRADO,
                DictamenEnum.LIBERADO,
                DictamenEnum.DEVOLUCION_CLIENTES,
                DictamenEnum.RETIRO_MERCADO
            ).contains(l.getDictamen())).toList();
    }

    //***********CU3 BAJA: MUESTREO***********
    public List<Lote> findAllForMuestreo() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.RECIBIDO != l.getDictamen())
            .filter(l -> l.getAnalisisList().stream()
                .anyMatch(a -> a.getNroAnalisis() != null))
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
                DictamenEnum.RECHAZADO
            ).contains(l.getDictamen()))
            .filter(l -> EnumSet.of(
                TipoProductoEnum.API,
                TipoProductoEnum.EXCIPIENTE,
                TipoProductoEnum.ACOND_PRIMARIO,
                TipoProductoEnum.ACOND_SECUNDARIO
            ).contains(l.getProducto().getTipoProducto()))
            .filter(l -> EstadoEnum.DEVUELTO != l.getEstado())
            .toList();
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    public List<Lote> findAllForResultadoAnalisis() {
        final List<Lote> lotes = loteRepository.findAll();
        return lotes.stream()
            .filter(l -> EnumSet.of(DictamenEnum.CUARENTENA).contains(l.getDictamen()))
            .filter(l -> l.getAnalisisList().stream()
                .anyMatch(a -> a.getDictamen() == null && a.getFechaRealizado() == null))
            .toList();
    }

    public List<Lote> findAllForConsumoProduccion() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.APROBADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA != l.getProducto().getTipoProducto())
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator
                .comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno)
                .thenComparing(Lote::getNroBulto))
            .toList();
    }

    //Persistencia

    //***********CU1 ALTA: COMPRA***********
    @Transactional
    public List<Lote> ingresarStockPorCompra(LoteDTO loteDTO) {
        List<Lote> result = new ArrayList<>();
        Proveedor proveedor = proveedorRepository.findById(loteDTO.getProveedorId()).orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));
        Producto producto = productoRepository.findById(loteDTO.getProductoId()).orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
        int bultosTotales = Math.max(loteDTO.getBultosTotales(), 1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss");
        String timestamp = loteDTO.getFechaYHoraCreacion().format(formatter);

        for (int i = 0; i < bultosTotales; i++) {
            Lote bultoLote = createLoteIngreso(loteDTO);
            bultoLote.setCodigoInterno("L-" + producto.getTipoProducto() + "-" + timestamp);
            if (loteDTO.getFabricanteId() != null) {
                proveedorRepository.findById(loteDTO.getFabricanteId()).ifPresent(bultoLote::setFabricante);
            }

            bultoLote.setProducto(producto);
            bultoLote.setProveedor(proveedor);

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

            Lote bultoGuardado = loteRepository.save(bultoLote);
            final Movimiento movimiento = movimientoService.persistirMovimientoAltaIngresoCompra(bultoGuardado);
            bultoGuardado.getMovimientos().add(movimiento);
            result.add(bultoGuardado);
        }
        return result;
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    @Transactional
    public List<Lote> persistirDictamenCuarentena(final List<Lote> lotes, final MovimientoDTO dto) {
        //TODO, eliminar NRO de Reanalisis del DTO
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

        //Si el producto esta en estado Recibido debo pasarlo a Cuarentena antes
        //Si tengo un numero de reanalisis, es que necesito crear un nuevo analisis para el producto

        final String currentNroAnalisis = lote.getCurrentNroAnalisis();
        if(!currentNroAnalisis.equals(dto.getNroAnalisis())) {
            throw new IllegalArgumentException("El número de análisis no coincide con el análisis en curso");
        }

        final Movimiento movimiento = movimientoService.persistirMovimientoMuestreo(dto, lote);
        lote.setCantidadActual(UnidadMedidaUtils.restarMovimientoConvertido(dto, lote));
        lote.setEstado(EstadoEnum.EN_USO);
        lote.getMovimientos().add(movimiento);
        return Optional.of(loteRepository.save(lote));
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional
    public List<Lote> persistirDevolucionCompra(final MovimientoDTO dto, final String codigoInterno) {
        final List<Lote> byCodigoInternoAndActivoTrue = loteRepository.findAllByCodigoInternoAndActivoTrue(codigoInterno);

        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : byCodigoInternoAndActivoTrue) {
            final Movimiento movimiento = movimientoService.persistirMovimientoDevolucionCompra(dto, loteBulto);
            loteBulto.setCantidadActual(BigDecimal.ZERO);
            loteBulto.setEstado(EstadoEnum.DEVUELTO);
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

}

