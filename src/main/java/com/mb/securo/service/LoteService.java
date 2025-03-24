package com.mb.securo.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.securo.entity.Lote;
import com.mb.securo.entity.Movimiento;
import com.mb.securo.enums.DictamenEnum;
import com.mb.securo.enums.EstadoLoteEnum;
import com.mb.securo.enums.MotivoEnum;
import com.mb.securo.enums.TipoMovimientoEnum;
import com.mb.securo.repository.LoteRepository;
import com.mb.securo.repository.MovimientoRepository;
import com.mb.securo.repository.maestro.ContactoRepository;
import com.mb.securo.repository.maestro.ProductoRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class LoteService {

    private final LoteRepository loteRepository;

    private final MovimientoRepository movimientoRepository;

    private final ContactoRepository contactoRepository;

    private final ProductoRepository productoRepository;

    @Transactional
    public Lote ingresarStockPorCompra(Lote lote) {
        if (lote.getFechaIngreso().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser posterior al día de hoy.");
        }
        contactoRepository.findById(lote.getProveedor().getId())
            .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));

        if (lote.getFabricante() != null && lote.getFabricante().getId() != null) {
            contactoRepository.findById(lote.getFabricante().getId())
                .orElseThrow(() -> new IllegalArgumentException("El fabricante no existe."));
        }
        productoRepository.findById(lote.getProducto().getId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        lote.setEstadoLote(EstadoLoteEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setIdLote("L-" + System.currentTimeMillis());
        lote.setCantidadActual(lote.getCantidadInicial());
        lote.setActivo(Boolean.TRUE);
        Lote nuevoLote = loteRepository.save(lote);

        // Crear el movimiento de Alta asociado al ingreso por compra
        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(LocalDate.now());
        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setMotivo(MotivoEnum.COMPRA);
        movimiento.setLote(nuevoLote);
        movimiento.setCantidad(nuevoLote.getCantidadInicial());
        movimiento.setUnidadMedida(nuevoLote.getUnidadMedida());
        movimiento.setDescripcion("Ingreso de stock por compra (CU1)");
        movimiento.setDictamenFinal(nuevoLote.getDictamen());
        movimiento.setActivo(Boolean.TRUE);

        movimientoRepository.save(movimiento);
        return nuevoLote;
    }

}

