package com.mb.conitrack.utils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import lombok.Getter;

import static com.mb.conitrack.enums.MotivoEnum.DEVOLUCION_COMPRA;
import static com.mb.conitrack.enums.MotivoEnum.MUESTREO;

public class EntityUtils {

    @Getter
    private static final EntityUtils Instance = new EntityUtils();

    private EntityUtils() {
    }

    public static Movimiento createMovimientoAltaDevolucionVenta(final Lote lote) {
        Movimiento movimiento = createAltaProductoPropio(lote);

        movimiento.setMotivo(MotivoEnum.DEVOLUCION_VENTA);
        movimiento.setObservaciones("_CU13_\n" + lote.getObservaciones());
        return movimiento;
    }

    public static  void addLoteInfoToMovimiento(final Lote lote, final Movimiento movimiento) {
        String timestampLoteDTO = lote.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(lote.getCodigoInterno() + "-" + timestampLoteDTO);
        movimiento.getBultos().addAll(lote.getBultos());
        movimiento.setLote(lote);
        for (Bulto bulto: lote.getBultos()){
            DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(bulto)
                .cantidad(bulto.getCantidadInicial())
                .unidadMedida(bulto.getUnidadMedida())
                .build();
            movimiento.getDetalles().add(det);
            bulto.getDetalles().add(det);
        }
    }

    public static Movimiento createMovimientoAltaIngresoCompra(LoteDTO loteDTO) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setMotivo(MotivoEnum.COMPRA);

        movimiento.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
        movimiento.setFecha(loteDTO.getFechaYHoraCreacion().toLocalDate());
        movimiento.setCantidad(loteDTO.getCantidadInicial());
        movimiento.setUnidadMedida(loteDTO.getUnidadMedida());
        movimiento.setDictamenFinal(loteDTO.getDictamen());
        movimiento.setActivo(true);

        movimiento.setObservaciones("_CU1_\n" + loteDTO.getObservaciones());
        return movimiento;
    }

    public static Movimiento createMovimientoAltaIngresoCompra(final Lote lote, LoteDTO loteDTO) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setMotivo(MotivoEnum.COMPRA);

        movimiento.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
        String timestampLoteDTO = loteDTO.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setFecha(loteDTO.getFechaYHoraCreacion().toLocalDate());
        movimiento.setCantidad(loteDTO.getCantidadInicial());
        movimiento.setUnidadMedida(loteDTO.getUnidadMedida());
        movimiento.setDictamenFinal(loteDTO.getDictamen());
        movimiento.setCodigoInterno(lote.getCodigoInterno() + "-" + timestampLoteDTO);
        movimiento.getBultos().addAll(lote.getBultos());
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        movimiento.setObservaciones("_CU1_\n" + lote.getObservaciones());
        return movimiento;
    }

    public static Movimiento createMovimientoAltaIngresoCompra(final Lote lote) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setMotivo(MotivoEnum.COMPRA);

        movimiento.setFechaYHoraCreacion(lote.getFechaYHoraCreacion());
        String timestampLoteDTO = lote.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(lote.getCodigoInterno() + "-B_" + lote.getNroBulto() + "-" + timestampLoteDTO);
        movimiento.setFecha(lote.getFechaYHoraCreacion().toLocalDate());
        movimiento.setCantidad(lote.getCantidadInicial());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setLote(lote);
        movimiento.getBultos().addAll(lote.getBultos());
        movimiento.setActivo(true);

        movimiento.setObservaciones("_CU1_\n" + lote.getObservaciones());
        return movimiento;
    }

    public static Movimiento createMovimientoAltaIngresoProduccion(final Lote lote) {
        Movimiento movimiento = createAltaProductoPropio(lote);

        movimiento.setMotivo(MotivoEnum.PRODUCCION_PROPIA);
        movimiento.setObservaciones("_CU10_\n" + lote.getObservaciones());
        return movimiento;
    }

    public static Movimiento createMovimientoModificacion(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(lote.getCodigoInterno() + "-" + timestampLoteDTO);
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setObservaciones(dto.getObservaciones());
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        return movimiento;
    }


    public static Movimiento crearMovimientoDevolucionCompra(final MovimientoDTO dto) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(DEVOLUCION_COMPRA);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(dto.getCodigoInternoLote() + "-" + timestampLoteDTO);
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setUnidadMedida(dto.getUnidadMedida());

        movimiento.setDictamenInicial(dto.getDictamenInicial());
        movimiento.setDictamenFinal(DictamenEnum.RECHAZADO);

        movimiento.setFecha(dto.getFechaMovimiento());

        movimiento.setActivo(true);
        movimiento.setObservaciones("_CU4_\n" + dto.getObservaciones());
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

    private static Movimiento createAltaProductoPropio(final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setFechaYHoraCreacion(lote.getFechaYHoraCreacion());
        String timestampLoteDTO = lote.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoInterno(lote.getCodigoInterno() + "-B_" + lote.getNroBulto() + "-" + timestampLoteDTO);
        movimiento.setFecha(lote.getFechaIngreso());
        movimiento.setCantidad(lote.getCantidadInicial());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setLote(lote);
        movimiento.getTrazas().addAll(lote.getTrazas());
        movimiento.setActivo(true);
        return movimiento;
    }

    public Bulto createBultoIngreso(final LoteDTO loteDTO) {
        Bulto bulto = new Bulto();

        //Datos CU1
        bulto.setEstado(EstadoEnum.NUEVO);
        bulto.setActivo(Boolean.TRUE);

        return bulto;
    }

    public Lote createLoteIngreso(final LoteDTO loteDTO) {
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

        return lote;
    }

    public void addDetalle(Movimiento mov, Bulto bulto, BigDecimal cantidad, UnidadMedidaEnum unidad) {
        DetalleMovimiento det = DetalleMovimiento.builder()
            .movimiento(mov)
            .bulto(bulto)
            .cantidad(cantidad)
            .unidadMedida(unidad)
            .build();

        mov.getDetalles().add(det);
        bulto.getDetalles().add(det);

        // mientras mantengas el ManyToMany, sincronizalo:
        mov.getBultos().add(bulto);
    }

    public void removeDetalle(Movimiento mov, DetalleMovimiento det) {
        mov.getDetalles().remove(det);
        det.getBulto().getDetalles().remove(det);
        det.setMovimiento(null);
        det.setBulto(null);
    }

}
