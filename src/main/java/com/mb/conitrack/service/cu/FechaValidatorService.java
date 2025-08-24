package com.mb.conitrack.service.cu;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.service.LoteService;

import static com.mb.conitrack.enums.DictamenEnum.ANALISIS_EXPIRADO;
import static com.mb.conitrack.enums.DictamenEnum.VENCIDO;
import static com.mb.conitrack.enums.MotivoEnum.EXPIRACION_ANALISIS;
import static com.mb.conitrack.enums.MotivoEnum.VENCIMIENTO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;

@Service
public class FechaValidatorService extends AbstractCuService {

    @Autowired
    private LoteService loteService;

    @Scheduled(cron = "0 0 5 * * *") // Todos los días a las 5 AM
    public void validarFecha() {
        procesarLotesAnalisisExpirado(loteService.findAllLotesAnalisisExpirado());
        procesarLotesVencidos(loteService.findAllLotesVencidos());
    }

    //***********CU8 MODIFICACION: VENCIDO***********
    @Transactional
    public List<Lote> persistirProductosVencidos(final MovimientoDTO dto, final List<Lote> lotes) {
        //TODO, eliminar NRO de Reanalisis del DTO
        List<Lote> result = new ArrayList<>();
        for (Lote loteBulto : lotes) {
            final Movimiento movimiento = persistirMovimientoProductoVencido(dto, loteBulto);
            loteBulto.setDictamen(movimiento.getDictamenFinal());
            loteBulto.getMovimientos().add(movimiento);
            result.add(loteRepository.save(loteBulto));
        }
        return result;
    }

    //***********CU9 MODIFICACION: ANALSIS EXPIRADO***********
    @Transactional
    public List<Lote> persistirExpiracionAnalisis(final MovimientoDTO dto, final List<Lote> lotes) {
        List<Lote> result = new ArrayList<>();
        for (Lote lote : lotes) {
            final Movimiento movimiento = persistirMovimientoExpiracionAnalisis(dto, lote);
            lote.setDictamen(movimiento.getDictamenFinal());
            lote.getMovimientos().add(movimiento);
            result.add(loteRepository.save(lote));
        }
        return result;
    }

    //***********CU9 MODIFICACION: VENCIDO***********
    @Transactional
    public Movimiento persistirMovimientoExpiracionAnalisis(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaYHoraCreacion().toLocalDate());
        movimiento.setMotivo(EXPIRACION_ANALISIS);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(ANALISIS_EXPIRADO);

        movimiento.setObservaciones("_CU8_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

    //***********CU9 MODIFICACION: VENCIDO***********
    @Transactional
    public Movimiento persistirMovimientoProductoVencido(final MovimientoDTO dto, Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);
        movimiento.setFecha(dto.getFechaYHoraCreacion().toLocalDate());
        movimiento.setMotivo(VENCIMIENTO);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(VENCIDO);

        movimiento.setObservaciones("_CU9_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
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
        dto.setObservaciones("(CU8) ANALISIS EXPIRADO POR FECHA: " + hoy);
        final List<Lote> lotes = persistirExpiracionAnalisis(dto, lotesReanalisis);
        for (Lote lote : lotes) {
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
        dto.setObservaciones("(CU9) VENCIMIENTO AUTOMATICO POR FECHA: " + hoy);
        final List<Lote> lotes = persistirProductosVencidos(dto, lotesVencidos);
        for (Lote lote : lotes) {
            System.out.println("Vencido: " + lote.getLoteProveedor() + " - " + lote.getFechaVencimientoVigente());
        }
    }

}
