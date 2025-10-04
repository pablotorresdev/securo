package com.mb.conitrack.service.cu;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;

import static com.mb.conitrack.enums.DictamenEnum.ANALISIS_EXPIRADO;
import static com.mb.conitrack.enums.DictamenEnum.VENCIDO;
import static com.mb.conitrack.enums.MotivoEnum.EXPIRACION_ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.VENCIMIENTO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;

@Service
public class FechaValidatorService extends AbstractCuService {

    @Scheduled(cron = "0 0 5 * * *") // Todos los días a las 5 AM
    @Transactional
    public void validarFecha() {
        procesarLotesAnalisisExpirado(findAllLotesAnalisisExpirado());
        procesarLotesVencidos(findAllLotesVencidos());
    }

    //***********CU10 MODIFICACION: ANALSIS EXPIRADO***********
    @Transactional(readOnly = true)
    List<Lote> findAllLotesAnalisisExpirado() {
        LocalDate hoy = LocalDate.now();
        return loteRepository.findLotesConStockOrder().stream()
            .filter(l -> {
                LocalDate f = l.getFechaReanalisisVigente();
                return f != null && !f.isBefore(hoy); // >= hoy
            })
            .toList();
    }

    //***********CU9 MODIFICACION: VENCIDO***********
    @Transactional(readOnly = true)
    List<Lote> findAllLotesVencidos() { // OJO: devuelve NO vencidos como tu versión previa
        LocalDate hoy = LocalDate.now();
        return loteRepository.findLotesConStockOrder().stream()
            .filter(l -> {
                LocalDate f = l.getFechaVencimientoVigente();
                return f != null && !f.isBefore(hoy); // >= hoy
            })
            .toList(); // ya viene ordenado desde la DB
    }

    //***********CU10 MODIFICACION: ANALSIS EXPIRADO***********
    @Transactional
    List<Lote> persistirExpiracionAnalisis(final MovimientoDTO dto, final List<Lote> lotes) {
        List<Lote> result = new ArrayList<>();
        for (Lote lote : lotes) {
            final Movimiento movimiento = persistirMovimientoExpiracionAnalisis(dto, lote);
            lote.setDictamen(movimiento.getDictamenFinal());
            lote.getMovimientos().add(movimiento);
            result.add(loteRepository.save(lote));
        }
        return result;
    }

    //***********CU10 MODIFICACION: VENCIDO***********
    @Transactional
    Movimiento persistirMovimientoExpiracionAnalisis(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaYHoraCreacion().toLocalDate());
        movimiento.setMotivo(EXPIRACION_ANALISIS);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(ANALISIS_EXPIRADO);

        movimiento.setObservaciones("_CU9_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU10 MODIFICACION: VENCIDO***********
    @Transactional
    Movimiento persistirMovimientoProductoVencido(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaYHoraCreacion().toLocalDate());
        movimiento.setMotivo(VENCIMIENTO);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(VENCIDO);

        movimiento.setObservaciones("_CU10_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU9 MODIFICACION: VENCIDO***********
    @Transactional
    List<Lote> persistirProductosVencidos(final MovimientoDTO dto, final List<Lote> lotes) {
        //TODO, eliminar NRO de Reanalisis del DTO
        List<Lote> result = new ArrayList<>();
        for (Lote lote : lotes) {
            final Movimiento movimiento = persistirMovimientoProductoVencido(dto, lote);
            lote.setDictamen(movimiento.getDictamenFinal());
            lote.getMovimientos().add(movimiento);
            result.add(loteRepository.save(lote));
        }
        return result;
    }

    @Transactional
    void procesarLotesAnalisisExpirado(List<Lote> lotesReanalisis) {
        final LocalDate hoy = LocalDate.now();
        if (lotesReanalisis.isEmpty()) {
            return;
        }
        final MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(hoy);
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("(CU9) ANALISIS EXPIRADO POR FECHA: " + hoy);
        final List<Lote> lotes = persistirExpiracionAnalisis(dto, lotesReanalisis);
        for (Lote lote : lotes) {
            //log
            System.out.println("Reanalisis expirado: " +
                lote.getLoteProveedor() +
                " - " +
                lote.getFechaReanalisisVigente());
        }
    }

    @Transactional
    void procesarLotesVencidos(List<Lote> lotesVencidos) {
        final LocalDate hoy = LocalDate.now();
        if (lotesVencidos.isEmpty()) {
            return;
        }
        final MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(hoy);
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("(CU10) VENCIMIENTO AUTOMATICO POR FECHA: " + hoy);
        final List<Lote> lotes = persistirProductosVencidos(dto, lotesVencidos);
        for (Lote lote : lotes) {
            //log
            System.out.println("Vencido: " + lote.getLoteProveedor() + " - " + lote.getFechaVencimientoVigente());
        }
    }

}
