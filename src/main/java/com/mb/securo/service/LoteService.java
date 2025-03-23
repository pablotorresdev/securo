package com.mb.securo.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.securo.entity.Lote;
import com.mb.securo.entity.Movimiento;
import com.mb.securo.enums.DictamenEnum;
import com.mb.securo.enums.EstadoLoteEnum;
import com.mb.securo.enums.MotivoEnum;
import com.mb.securo.repository.LoteRepository;
import com.mb.securo.repository.MovimientoRepository;

@Service
public class LoteService {

    private final LoteRepository loteRepository;

    private final MovimientoRepository movimientoRepository;

    public LoteService(final LoteRepository loteRepository, final MovimientoRepository movimientoRepository) {
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional
    public Lote ingresarStockPorCompra(Lote lote) {
        // Validación de la fecha de ingreso
        if (lote.getFechaIngreso().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser posterior al día de hoy.");
        }
        // Validaciones adicionales: existencia del producto, proveedor, etc.

        // Configuración de valores fijos para el CU1
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setIdLote("L-" + System.currentTimeMillis());
        lote.setCantidadActual(lote.getCantidadInicial());
        lote.setEstadoLote(EstadoLoteEnum.NUEVO);

        // Persistir el lote en el inventario
        Lote nuevoLote = loteRepository.save(lote);

        // Crear y persistir el movimiento de Alta asociado
        Movimiento movimiento = new Movimiento();
        //        movimiento.setFechaMov(LocalDate.now());
        //        movimiento.setTipoMov("ALTA");       // Tipo de movimiento
        movimiento.setMotivo(MotivoEnum.COMPRA);        // Motivo del movimiento
        //        movimiento.setLote(nuevoLote);
        //        movimiento.setCantidad(nuevoLote.getCantidadInicial());
        //        movimiento.setObservaciones("Ingreso de stock por compra (CU1)");

        movimientoRepository.save(movimiento);

        // (Opcional) Registrar auditoría de la operación

        return nuevoLote;
    }

}

