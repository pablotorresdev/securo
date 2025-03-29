package com.mb.conitrack.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.LoteDTO;
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

    public List<Lote> findAll() {
        final List<Lote> lotes = loteRepository.findAll();
        lotes.sort(Comparator.comparing(Lote::getIdLote));
        return lotes;
    }

    public Lote findById(final Long loteId) {
        return loteRepository.findById(loteId).orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    @Transactional
    public void actualizarDictamen(final Lote lote, final DictamenEnum dictamen) {
        final List<Lote> allByIdLoteAndActivoTrue = loteRepository.findAllByIdLoteAndActivoTrue(lote.getIdLote());
        for (Lote l : allByIdLoteAndActivoTrue) {
            l.setDictamen(dictamen);
            loteRepository.save(l);
        }
    }

    @Transactional
    public void ingresarStockPorCompra(LoteDTO dto) {
        if (dto.getFechaIngreso().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser posterior al día de hoy.");
        }

        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId()).orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));
        Producto producto = productoRepository.findById(dto.getProductoId()).orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        int bultosTotales = Math.max(dto.getBultosTotales(), 1);
        for (int i = 0; i < bultosTotales; i++) {

            Lote lote = new Lote();
            Movimiento movimiento = new Movimiento();
            lote.getMovimientos().add(movimiento);

            populateLote(dto, lote, producto, proveedor, i);
            Lote nuevoLote = loteRepository.save(lote);

            populateMovimiento(movimiento, lote, nuevoLote);
            movimientoRepository.save(movimiento);
        }
    }

    private static void populateLote(final LoteDTO dto, final Lote lote, final Producto producto, final Proveedor proveedor, final int i) {
        //Datos CU1
        lote.setEstadoLote(EstadoLoteEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setActivo(Boolean.TRUE);

        //Datos obligatorios comunes
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
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
        lote.setIdLote("L-" + timestamp);

        lote.setNroBulto(i + 1);
        lote.setBultosTotales(dto.getBultosTotales());
        lote.setCantidadInicial(dto.getCantidadInicial());
        lote.setCantidadActual(dto.getCantidadInicial());
        lote.setUnidadMedida(dto.getUnidadMedida());
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

}
