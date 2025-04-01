package com.mb.conitrack.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoLoteEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class LoteService {

    private final LoteRepository loteRepository;

    private final MovimientoRepository movimientoRepository;

    private final ProveedorRepository proveedorRepository;

    private final ProductoRepository productoRepository;

    //Getters
    public List<Lote> findAllSortByDateAndNroBulto() {
        final List<Lote> lotes = loteRepository.findAll();
        lotes.sort(Comparator
            .comparing(Lote::getFechaIngreso)
            .thenComparing(Lote::getCodigoInterno)
            .thenComparing(Lote::getNroBulto));
        return lotes;
    }

    public Lote findById(final Long id) {
        return loteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    public List<Lote> findAllByCodigoInternoAndActivoTrue(final String codigoInterno) {
        return loteRepository.findAllByCodigoInternoAndActivoTrue(codigoInterno);
    }

    public List<Lote> findNroAnalisis(final Analisis analisis) {
        return loteRepository.findAllByAnalisisAndActivoTrue(analisis);
    }

    public List<Lote> findByLoteProveedor(final String loteProveedor) {
        return loteRepository.findAllByLoteProveedorAndActivoTrue(loteProveedor);
    }

    public List<Lote> findAllByDictamenRecibido() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndNroBulto();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> EnumSet.of(
                DictamenEnum.RECIBIDO
            ).contains(l.getDictamen()))
            .toList();
    }

    public List<Lote> findAllMuestreable() {
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

    //Setters
    //CU1
    @Transactional
    public List<Lote> ingresarStockPorCompra(LoteDTO dto) {

        if (dto.getFechaIngreso().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser posterior al día de hoy.");
        }
        List<Lote> result = new ArrayList<>();
        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId()).orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));
        Producto producto = productoRepository.findById(dto.getProductoId()).orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
        int bultosTotales = Math.max(dto.getBultosTotales(), 1);
        for (int i = 0; i < bultosTotales; i++) {

            Lote lote = createLoteFromDto(dto);
            lote.setProducto(producto);
            lote.setProveedor(proveedor);

            Movimiento movimiento = new Movimiento();
            lote.getMovimientos().add(movimiento);

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

            final Lote nuevoLote = loteRepository.save(lote);
            populateMovimiento(movimiento, lote, nuevoLote);
            movimientoRepository.save(movimiento);
            result.add(nuevoLote);
        }
        return result;
    }

    private static Lote createLoteFromDto(final LoteDTO dto) {
        Lote lote = new Lote();
        //Datos CU1
        lote.setEstadoLote(EstadoLoteEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setActivo(Boolean.TRUE);

        //Datos obligatorios comunes
        lote.setBultosTotales(dto.getBultosTotales());
        lote.setFechaIngreso(dto.getFechaIngreso());
        lote.setLoteProveedor(dto.getLoteProveedor());

        //Datos opcionales comunes
        lote.setNroRemito(dto.getNroRemito());
        lote.setDetalleConservacion(dto.getDetalleConservacion());
        lote.setFechaReanalisis(dto.getFechaReanalisis());
        lote.setFechaVencimiento(dto.getFechaVencimiento());
        lote.setTitulo(dto.getTitulo());
        lote.setObservaciones(dto.getObservaciones());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        lote.setCodigoInterno("L-" + timestamp);
        return lote;
    }

    private static void populateMovimiento(final Movimiento movimiento, final Lote lote, final Lote nuevoLote) {
        // Crear el movimiento de ALTA asociado al ingreso por compra
        movimiento.setFecha(LocalDate.now());
        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setMotivo(MotivoEnum.COMPRA);
        movimiento.setCantidad(lote.getCantidadInicial());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setDescripcion("Ingreso de stock por compra (CU1)");
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setActivo(Boolean.TRUE);
        movimiento.setLote(nuevoLote);
    }

    @Transactional
    public void actualizarDictamenLoteCompleto(final Lote lote, final DictamenEnum dictamen) {
        final List<Lote> allByCodigoInternoAndActivoTrue = loteRepository.findAllByCodigoInternoAndActivoTrue(lote.getCodigoInterno());
        for (Lote l : allByCodigoInternoAndActivoTrue) {
            l.setDictamen(dictamen);
            loteRepository.save(l);
        }
    }

    public Optional<Lote> save(final Lote lote) {
        final Lote nuevoLote = loteRepository.save(lote);
        return Optional.of(nuevoLote);
    }

}
