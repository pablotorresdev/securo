package com.mb.conitrack.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.LoteRequestDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Contacto;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoLoteEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.maestro.ContactoRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class LoteService {

    private final LoteRepository loteRepository;

    private final MovimientoRepository movimientoRepository;

    private final ContactoRepository contactoRepository;

    private final ProductoRepository productoRepository;


    @Transactional
    public Lote ingresarStockPorCompra(LoteRequestDTO dto) {
        // Validar que la fecha de ingreso no sea posterior a la fecha actual
        if (dto.getFechaIngreso().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser posterior al día de hoy.");
        }

        // Verificar existencia del proveedor
        Contacto proveedor = contactoRepository.findById(dto.getProveedorId())
            .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));

        // Verificar existencia del fabricante si se envió (distinto a Conifarma)
        Contacto fabricante = null;
        if (dto.getFabricanteId() != null) {
            fabricante = contactoRepository.findById(dto.getFabricanteId())
                .orElseThrow(() -> new IllegalArgumentException("El fabricante no existe."));
        }

        // Verificar existencia del producto
        Producto producto = productoRepository.findById(dto.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        // Convertir el DTO a entidad Lote y asignar valores fijos
        Lote lote = new Lote();
        Movimiento movimiento = new Movimiento();
        lote.getMovimientos().add(movimiento);

        lote.setIdLote("L-" + System.currentTimeMillis());
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setFechaIngreso(dto.getFechaIngreso());

        //Cantidad
        if(dto.getNroBulto()!=null) {
            lote.setNroBulto(dto.getNroBulto());
        } else {
            lote.setNroBulto(dto.getBultosTotales());
        }
        lote.setBultosTotales(dto.getBultosTotales());
        lote.setCantidadInicial(dto.getCantidadInicial());
        lote.setCantidadActual(dto.getCantidadInicial());
        lote.setUnidadMedida(dto.getUnidadMedida());

        if (fabricante != null) {
            lote.setFabricante(fabricante);
        } else {
            lote.setFabricante(proveedor);
        }
        lote.setLoteProveedor(dto.getLoteProveedor());
        lote.setNroRemito(dto.getNroRemito());
        lote.setDetalleConservacion(dto.getDetalleConservacion());
        lote.setFechaVencimiento(dto.getFechaVencimiento());
        lote.setFechaReanalisis(dto.getFechaReanalisis());
        lote.setTitulo(dto.getTitulo());
        lote.setObservaciones(dto.getObservaciones());

        // Valores fijos del CU1
        lote.setEstadoLote(EstadoLoteEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setActivo(Boolean.TRUE);

        // Persistir el lote
        Lote nuevoLote = loteRepository.save(lote);

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

        movimientoRepository.save(movimiento);

        // (Opcional) Se podría registrar la auditoría y notificar a los departamentos correspondientes

        return nuevoLote;
    }

    public List<Lote> findAll() {
        return loteRepository.findAll();
    }

    public Lote findById(final Long loteId) {
        return loteRepository.findById(loteId)
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

}
