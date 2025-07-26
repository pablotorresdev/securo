package com.mb.conitrack.entity;

import java.util.List;
import java.util.Optional;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;

import static com.mb.conitrack.enums.MotivoEnum.MUESTREO;

public class EntityUtils {

    public static Lote createLoteIngreso(final LoteDTO loteDTO) {
        Lote lote = new Lote();

        //Datos CU1
        lote.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
        lote.setEstado(EstadoEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setActivo(Boolean.TRUE);

        //Datos obligatorios comunes
        lote.setPaisOrigen(loteDTO.getPaisOrigen());
        lote.setFechaIngreso(loteDTO.getFechaIngreso());
        lote.setBultosTotales(loteDTO.getBultosTotales());
        lote.setLoteProveedor(loteDTO.getLoteProveedor());

        //Datos opcionales comunes
        lote.setFechaReanalisisProveedor(loteDTO.getFechaReanalisisProveedor());
        lote.setFechaVencimientoProveedor(loteDTO.getFechaVencimientoProveedor());
        lote.setNroRemito(loteDTO.getNroRemito());
        lote.setDetalleConservacion(loteDTO.getDetalleConservacion());
        lote.setObservaciones(loteDTO.getObservaciones());
        lote.setActivo(true);

        return lote;
    }

    public static Movimiento createMovimientoAltaIngresoCompra(final Lote lote) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setMotivo(MotivoEnum.COMPRA);

        movimiento.setFechaYHoraCreacion(lote.getFechaYHoraCreacion());
        movimiento.setFecha(lote.getFechaYHoraCreacion().toLocalDate());
        movimiento.setCantidad(lote.getCantidadInicial());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        movimiento.setObservaciones("_CU1_\n" + lote.getObservaciones());
        return movimiento;
    }

    public static Movimiento createMovimientoAltaIngresoProduccion(final Lote lote) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setMotivo(MotivoEnum.PRODUCCION_PROPIA);

        movimiento.setFechaYHoraCreacion(lote.getFechaYHoraCreacion());
        movimiento.setFecha(lote.getFechaIngreso());
        movimiento.setCantidad(lote.getCantidadInicial());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setLote(lote);
        movimiento.getTrazas().addAll(lote.getTrazas());
        movimiento.setActivo(true);

        movimiento.setObservaciones("_CU10_\n" + lote.getObservaciones());
        return movimiento;
    }

    public static Movimiento createMovimientoModificacion(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setObservaciones(dto.getObservaciones());
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        return movimiento;
    }

    //***********CU3 BAJA: MUESTREO***********
    public static Movimiento createMovimientoPorMuestreo(final MovimientoDTO dto) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MUESTREO);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setUnidadMedida(dto.getUnidadMedida());
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setActivo(true);

        movimiento.setObservaciones("_CU3_\n" + dto.getObservaciones());
        return movimiento;
    }

    public static Optional<Analisis> getAnalisisEnCurso(final List<Analisis> analisisList) {
        List<Analisis> enCurso = analisisList.stream()
            .filter(Analisis::getActivo)
            .filter(analisis -> analisis.getDictamen() == null)
            .filter(analisis -> analisis.getFechaRealizado() == null)
            .toList();
        if (enCurso.isEmpty()) {
            return Optional.empty();
        } else if (enCurso.size() == 1) {
            return Optional.of(enCurso.get(0));
        } else {
            throw new IllegalArgumentException("El lote tiene más de un análisis en curso");
        }
    }

}
