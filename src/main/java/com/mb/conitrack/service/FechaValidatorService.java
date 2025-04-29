package com.mb.conitrack.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;

@Service
public class FechaValidatorService {

    @Autowired
    LoteService loteService;

    private final LocalDate hoy = LocalDate.now(); // ejemplo

    @Scheduled(cron = "0 0 5 * * *") // Todos los días a las 5 AM
    public void validarFecha() {
        procesarLotesAnalisisExpirado(loteService.findAllLotesAnalisisExpirado());
        procesarLotesVencidos(loteService.findAllLotesVencidos());
    }

    private void procesarLotesAnalisisExpirado(List<Lote> lotesReanalisis) {
        if (lotesReanalisis.isEmpty()) {
            return;
        }
        final MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(hoy);
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        dto.setObservaciones("(CU8) ANALISIS EXPIRADO POR FECHA: " + hoy);
        final List<Lote> lotes = loteService.persistirExpiracionAnalisis(dto, lotesReanalisis);
        for (Lote lote : lotes) {
            System.out.println("Reanalisis expirado: " + lote.getLoteProveedor() + " - " + lote.getFechaReanalisisVigente());
        }
    }

    private void procesarLotesVencidos(List<Lote> lotesVencidos) {
        if (lotesVencidos.isEmpty()) {
            return;
        }
        final MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(hoy);
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        dto.setObservaciones("(CU9) VENCIMIENTO AUTOMATICO POR FECHA: " + hoy);
        final List<Lote> lotes = loteService.persistirProductosVencidos(dto, lotesVencidos);
        for (Lote lote : lotes) {
            System.out.println("Vencido: " + lote.getLoteProveedor() + " - " + lote.getFechaVencimientoVigente());
        }
    }

}
