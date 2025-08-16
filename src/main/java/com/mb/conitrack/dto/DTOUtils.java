package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import lombok.Getter;

import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMenorUnidadMedida;
import static com.mb.conitrack.utils.UnidadMedidaUtils.sugerirUnidadParaCantidad;

public class DTOUtils {

    @Getter
    private static final DTOUtils Instance = new DTOUtils();

    private DTOUtils() {
        // Utility class
    }

    public static Analisis createAnalisis(final MovimientoDTO dto) {
        final String nroAnalisis = StringUtils.isEmptyOrWhitespace(dto.getNroReanalisis())
            ? dto.getNroAnalisis()
            : dto.getNroReanalisis();
        if (nroAnalisis != null) {
            Analisis analisis = new Analisis();
            analisis.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
            analisis.setNroAnalisis(nroAnalisis);
            analisis.setObservaciones(dto.getObservaciones());
            analisis.setActivo(true);
            return analisis;
        }
        throw new IllegalArgumentException("El número de análisis es requerido");
    }

    public static List<AnalisisDTO> fromAnalisisEntities(final List<Analisis> analisisList) {
        final List<AnalisisDTO> analisisDtos = new ArrayList<>();
        for (Analisis entity : analisisList) {
            analisisDtos.add(DTOUtils.fromAnalisisEntity(entity));
        }
        return analisisDtos;
    }

    public static AnalisisDTO fromAnalisisEntity(Analisis entity) {
        if (entity == null) {
            return null;
        }
        AnalisisDTO dto = new AnalisisDTO();
        dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setCodigoInternoLote(entity.getLote().getCodigoInterno());
        dto.setFechaRealizado(entity.getFechaRealizado());
        dto.setFechaReanalisis(entity.getFechaReanalisis());
        dto.setFechaVencimiento(entity.getFechaVencimiento());
        dto.setDictamen(entity.getDictamen());
        dto.setTitulo(entity.getTitulo());
        dto.setObservaciones(entity.getObservaciones());
        return dto;
    }

    public static BultoDTO fromBultoEntity(Bulto entity) {
        if (entity == null) {
            return null;
        }

        BultoDTO dto = new BultoDTO();
        dto.setNroBulto(entity.getNroBulto());
        dto.setCantidadActual(entity.getCantidadActual());
        dto.setCantidadInicial(entity.getCantidadInicial());
        dto.setUnidadMedida(entity.getUnidadMedida());
        dto.setCodigoLote(entity.getLote().getCodigoInterno());
        dto.setEstado(entity.getEstado());

        for (DetalleMovimiento detalleMovimiento : entity.getDetalles()) {
            dto.getDetalleMovimientoDTOs().add(fromDetalleMovimientoEntity(detalleMovimiento));
        }

        return dto;
    }

    public static List<LoteDTO> fromLoteEntities(final List<Lote> loteList) {
        final List<LoteDTO> lotesDtos = new ArrayList<>();
        for (Lote entity : loteList) {
            lotesDtos.add(DTOUtils.fromLoteEntity(entity));
        }
        return lotesDtos;
    }

    public static LoteDTO fromLoteEntity(Lote loteEntity) {
        if (loteEntity == null) {
            return null;
        }

        LoteDTO loteDTO = new LoteDTO();

        loteDTO.setFechaYHoraCreacion(loteEntity.getFechaYHoraCreacion());

        setProductoLote(loteEntity, loteDTO);
        setDatosProveedorLote(loteEntity, loteDTO);
        setDatosDerivadosLote(loteEntity, loteDTO);
        setListasBultosLote(loteEntity, loteDTO);
        addMovimientosDTO(loteEntity, loteDTO);
        addAnalisisDTO(loteEntity, loteDTO);
        addTrazaDTO(loteEntity, loteDTO);

        Long trazaInicial = null;
        if (loteEntity.getFirstActiveTraza() != null) {
            trazaInicial = loteEntity.getFirstActiveTraza().getNroTraza();
        }

        for (int i = 1; i <= loteEntity.getBultos().size(); i++) {
            final Bulto bultoEntity = loteEntity.getBultoByNro(i);
            if (!bultoEntity.getActivo()) {
                continue;
            }

            if (bultoEntity.getFirstActiveTraza() != null) {
                if (trazaInicial == null) {
                    trazaInicial = bultoEntity.getFirstActiveTraza().getNroTraza();
                } else {
                    trazaInicial = Math.min(trazaInicial, bultoEntity.getFirstActiveTraza().getNroTraza());
                }
            }

            final int index = loteDTO.getCantidadesBultos().size();
            loteDTO.getNroBultoList().add(index, bultoEntity.getNroBulto());
            loteDTO.getCantidadesBultos().add(index, bultoEntity.getCantidadActual());
            loteDTO.getUnidadMedidaBultos().add(index, bultoEntity.getUnidadMedida());
        }

        loteDTO.setEstado(loteEntity.getEstado().getValor());
        loteDTO.setTrazaInicial(trazaInicial);
        loteDTO.setCantidadInicial(loteEntity.getCantidadInicial());
        loteDTO.setCantidadActual(loteEntity.getCantidadActual());
        loteDTO.setUnidadMedida(loteEntity.getUnidadMedida());
        return loteDTO;
    }

    public static MovimientoDTO fromMovimientoEntity(Movimiento entity) {
        if (entity == null) {
            return null;
        }

        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(entity.getFecha());
        if (entity.getLote() != null) {
            dto.setCodigoInternoLote(entity.getLote().getCodigoInterno());
            dto.setLoteId(entity.getLote().getId());
            for(Bulto bulto : entity.getLote().getBultos()){
                for (DetalleMovimiento detalleMovimiento : bulto.getDetalles()){
                    dto.getDetalleMovimientoDTOs().add(fromDetalleMovimientoEntity(detalleMovimiento));
                }
            }
        }

        dto.setCantidad(entity.getCantidad());
        dto.setUnidadMedida(entity.getUnidadMedida());

        dto.setObservaciones(entity.getObservaciones());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setOrdenProduccion(entity.getOrdenProduccion());

        if (entity.getTipoMovimiento() != null) {
            dto.setTipoMovimiento(entity.getTipoMovimiento().name());
        }
        if (entity.getMotivo() != null) {
            dto.setMotivo(entity.getMotivo().name());
        }

        dto.setDictamenInicial(entity.getDictamenInicial());
        dto.setDictamenFinal(entity.getDictamenFinal());

        return dto;
    }


    public static DetalleMovimientoDTO fromDetalleMovimientoEntity(DetalleMovimiento entity) {
        if (entity == null) {
            return null;
        }

        DetalleMovimientoDTO dto = new DetalleMovimientoDTO();
        dto.setCodigoInternoMovimiento(entity.getMovimiento().getCodigoInterno());
        dto.setNroBulto(entity.getBulto().getNroBulto());
        dto.setCantidad(entity.getCantidad());
        dto.setUnidadMedida(entity.getUnidadMedida());

        return dto;
    }

    public static TrazaDTO fromTrazaEntity(Traza entity) {
        if (entity == null) {
            return null;
        }
        TrazaDTO dto = new TrazaDTO();
        dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
        dto.setCodigoProducto(entity.getProducto().getCodigoInterno());
        dto.setEstado(entity.getEstado());
        dto.setNroTraza(entity.getNroTraza());
        dto.setObservaciones(entity.getObservaciones());
        return dto;
    }


    //TODO: esto va a terminar borrandose cuando termine el refactor
    public static LoteDTO mergeLoteEntities(List<Lote> entities) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }

        LoteDTO loteDTO = new LoteDTO();
        boolean firstCase = true;

        BigDecimal cantidadInicial = BigDecimal.ZERO;
        BigDecimal cantidadActual = BigDecimal.ZERO;
        UnidadMedidaEnum unidadMedida = null;
        Long trazaInicial = null;

        for (Lote loteEntity : entities) {
            if (!loteEntity.getActivo()) {
                continue;
            }
            if (firstCase) {
                loteDTO.setFechaYHoraCreacion(loteEntity.getFechaYHoraCreacion());

                setProductoLote(loteEntity, loteDTO);
                setDatosProveedorLote(loteEntity, loteDTO);
                setDatosDerivadosLote(loteEntity, loteDTO);
                setListasBultosLote(loteEntity, loteDTO);
                addMovimientosDTO(loteEntity, loteDTO);
                addAnalisisDTO(loteEntity, loteDTO);
                addTrazaDTO(loteEntity, loteDTO);

                cantidadInicial = loteEntity.getCantidadInicial();
                cantidadActual = loteEntity.getCantidadActual();
                unidadMedida = loteEntity.getUnidadMedida();
                if (loteEntity.getFirstActiveTraza() != null) {
                    trazaInicial = loteEntity.getFirstActiveTraza().getNroTraza();
                }

                firstCase = false;
            } else {

                // Si el estado de alguno de los lotes es diferente, se asigna el de mayor prioridad
                setEstadoLote(loteEntity, loteDTO);

                if (loteEntity.getUnidadMedida() == unidadMedida) {
                    //La unidad de medida del bulto coincide con la del DTO
                    cantidadActual = loteEntity.getCantidadActual().add(cantidadActual);
                    cantidadInicial = loteEntity.getCantidadInicial().add(cantidadInicial);

                    UnidadMedidaEnum unidadSugerida = sugerirUnidadParaCantidad(unidadMedida, cantidadActual);
                    cantidadActual = convertirCantidadEntreUnidades(unidadMedida, cantidadActual, unidadSugerida);
                    cantidadInicial = convertirCantidadEntreUnidades(unidadMedida, cantidadInicial, unidadSugerida);

                    unidadMedida = unidadSugerida;
                } else {
                    //Distitnas unidades de medida
                    UnidadMedidaEnum menorUnidadMedida = obtenerMenorUnidadMedida(
                        loteEntity.getUnidadMedida(),
                        unidadMedida);
                    BigDecimal cantidadActualTemp = convertirCantidadEntreUnidades(
                        unidadMedida,
                        cantidadActual,
                        menorUnidadMedida);
                    cantidadActualTemp = cantidadActualTemp.add(convertirCantidadEntreUnidades(
                        loteEntity.getUnidadMedida(),
                        loteEntity.getCantidadActual(),
                        menorUnidadMedida));

                    UnidadMedidaEnum unidadSugerida = sugerirUnidadParaCantidad(menorUnidadMedida, cantidadActualTemp);
                    cantidadActual = convertirCantidadEntreUnidades(
                        menorUnidadMedida,
                        cantidadActualTemp,
                        unidadSugerida);

                    BigDecimal cantidadInicialTemp = convertirCantidadEntreUnidades(
                        unidadMedida,
                        cantidadInicial,
                        unidadSugerida);
                    cantidadInicial = cantidadInicialTemp.add(convertirCantidadEntreUnidades(
                        loteEntity.getUnidadMedida(),
                        loteEntity.getCantidadInicial(),
                        unidadSugerida));
                    unidadMedida = unidadSugerida;
                }
                if (loteEntity.getFirstActiveTraza() != null) {
                    if (trazaInicial == null) {
                        trazaInicial = loteEntity.getFirstActiveTraza().getNroTraza();
                    } else {
                        trazaInicial = Math.min(trazaInicial, loteEntity.getFirstActiveTraza().getNroTraza());
                    }
                }
                loteDTO.setCantidadInicial(cantidadInicial);
                loteDTO.setCantidadActual(cantidadActual);
                loteDTO.setUnidadMedida(unidadMedida);

                addMovimientosDTO(loteEntity, loteDTO);
            }
        }

        loteDTO.setTrazaInicial(trazaInicial);
        loteDTO.setCantidadInicial(cantidadInicial);
        loteDTO.setCantidadActual(cantidadActual);
        loteDTO.setUnidadMedida(unidadMedida);
        return loteDTO;
    }

    static void addAnalisisDTO(final Lote entity, final LoteDTO loteDTO) {
        for (Analisis analisis : entity.getAnalisisList()) {
            if (analisis.getActivo()) {
                loteDTO.getAnalisisDTOs().add(DTOUtils.fromAnalisisEntity(analisis));
            }
        }
    }

    static void addMovimientosDTO(final Lote entity, final LoteDTO loteDTO) {
        for (Movimiento movimiento : entity.getMovimientos()) {
            if (movimiento.getActivo()) {
                final MovimientoDTO movimientoDTO = DTOUtils.fromMovimientoEntity(movimiento);
                //movimientoDTO.setNroBulto(String.valueOf(entity.getNroBulto()));
                loteDTO.getMovimientoDTOs().add(movimientoDTO);
            }
        }
    }

    static void addTrazaDTO(final Lote entity, final LoteDTO loteDTO) {
        for (Traza traza : entity.getTrazas()) {
            if (traza.getActivo()) {
                loteDTO.getTrazaDTOs().add(DTOUtils.fromTrazaEntity(traza));
            }
        }
    }

    static void setDatosDerivadosLote(final Lote bultoEntity, final LoteDTO loteDTO) {
        loteDTO.setCodigoInternoLote(bultoEntity.getCodigoInterno());
        loteDTO.setFechaIngreso(bultoEntity.getFechaIngreso());
        loteDTO.setBultosTotales(bultoEntity.getBultosTotales());
        loteDTO.setEstado(bultoEntity.getEstado().getValor());
        loteDTO.setDictamen(bultoEntity.getDictamen());
        loteDTO.setLoteOrigenId(bultoEntity.getLoteOrigen() != null ? bultoEntity.getLoteOrigen().getId() : null);
        loteDTO.setObservaciones(bultoEntity.getObservaciones());
    }

    static void setDatosProveedorLote(final Lote bultoEntity, final LoteDTO loteDTO) {
        loteDTO.setProveedorId(bultoEntity.getProveedor() != null ? bultoEntity.getProveedor().getId() : null);
        loteDTO.setNombreProveedor(bultoEntity.getProveedor() != null
            ? bultoEntity.getProveedor().getRazonSocial()
            : null);
        loteDTO.setFabricanteId(bultoEntity.getFabricante() != null ? bultoEntity.getFabricante().getId() : null);
        loteDTO.setNombreFabricante(bultoEntity.getFabricante() != null
            ? bultoEntity.getFabricante().getRazonSocial()
            : null);
        loteDTO.setLoteProveedor(bultoEntity.getLoteProveedor());
        loteDTO.setPaisOrigen(bultoEntity.getPaisOrigen());
        loteDTO.setFechaReanalisisProveedor(bultoEntity.getFechaReanalisisProveedor());
        loteDTO.setFechaVencimientoProveedor(bultoEntity.getFechaVencimientoProveedor());
        loteDTO.setNroRemito(bultoEntity.getNroRemito());
        loteDTO.setDetalleConservacion(bultoEntity.getDetalleConservacion());
    }

    static void setEstadoLote(final Lote loteEntity, final LoteDTO loteDTO) {
        final Optional<EstadoEnum> estadoEnum = EstadoEnum.fromValor(loteDTO.getEstado());
        if (estadoEnum.isPresent()) {
            EstadoEnum estado = estadoEnum.get();
            if (estado.getPrioridad() < loteEntity.getEstado().getPrioridad()) {
                loteDTO.setEstado(loteEntity.getEstado().getValor());
            }
        } else {
            // Si no se encuentra el estado, se asigna el del bulto
            loteDTO.setEstado(loteEntity.getEstado().getValor());
        }
    }

    static void setListasBultosLote(final Lote entity, final LoteDTO loteDTO) {
        for (Bulto bulto : entity.getBultos()) {
            if (bulto.getActivo()) {
                final BultoDTO bultoDto = DTOUtils.fromBultoEntity(bulto);
                loteDTO.getBultosDTOs().add(bultoDto);
            }
        }
    }

    static void setProductoLote(final Lote loteEntity, final LoteDTO loteDTO) {
        if (loteEntity.getProducto() != null) {
            final Producto producto = loteEntity.getProducto();
            loteDTO.setProductoId(producto.getId());
            loteDTO.setNombreProducto(producto.getNombreGenerico());
            loteDTO.setCodigoProducto(producto.getCodigoInterno());
            loteDTO.setTipoProducto(producto.getTipoProducto());
            loteDTO.setProductoDestino(producto.getProductoDestino() != null ? producto.getProductoDestino() : null);
        }
    }

}
