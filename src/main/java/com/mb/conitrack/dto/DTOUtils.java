package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static com.mb.conitrack.enums.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.enums.UnidadMedidaUtils.obtenerMenorUnidadMedida;
import static com.mb.conitrack.enums.UnidadMedidaUtils.sugerirUnidadParaCantidad;

public class DTOUtils {

    public static AnalisisDTO fromEntity(Analisis entity) {
        if (entity == null) {
            return null;
        }
        AnalisisDTO dto = new AnalisisDTO();
        dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setFechaRealizado(entity.getFechaRealizado());
        dto.setFechaReanalisis(entity.getFechaReanalisis());
        dto.setFechaVencimiento(entity.getFechaVencimiento());
        dto.setDictamen(entity.getDictamen());
        dto.setTitulo(entity.getTitulo());
        dto.setObservaciones(entity.getObservaciones());
        return dto;
    }

    public static MovimientoDTO fromEntity(Movimiento entity) {
        if (entity == null) {
            return null;
        }

        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(entity.getFecha());
        if (entity.getLote() != null) {
            dto.setLoteId(entity.getLote().getId());
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

    public static Analisis createAnalisis(final MovimientoDTO dto) {
        final String nroAnalisis = StringUtils.isEmpty(dto.getNroReanalisis()) ? dto.getNroAnalisis() : dto.getNroReanalisis();
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

    public static LoteDTO fromEntities(List<Lote> entities) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }

        LoteDTO loteDTO = new LoteDTO();
        boolean firstCase = true;

        BigDecimal cantidadInicial = BigDecimal.ZERO;
        BigDecimal cantidadActual = BigDecimal.ZERO;
        UnidadMedidaEnum unidadMedida = null;

        for (Lote bultoEntity : entities) {
            if (firstCase) {
                loteDTO.setFechaYHoraCreacion(bultoEntity.getFechaYHoraCreacion());


                setProducto(bultoEntity, loteDTO);
                setDatosProveedor(bultoEntity, loteDTO);
                setDatosDerivados(bultoEntity, loteDTO);
                setListasBultos(bultoEntity, loteDTO);
                addMovimientosDTO(loteDTO, bultoEntity);
                addAnalisisDTO(loteDTO, bultoEntity);

                cantidadInicial = bultoEntity.getCantidadInicial();
                cantidadActual = bultoEntity.getCantidadActual();
                unidadMedida = bultoEntity.getUnidadMedida();
                firstCase = false;
            } else {

                // Si el estado de alguno de los bultos es diferente, se asigna el de mayor prioridad
                setEstadoLote(bultoEntity, loteDTO);

                if (bultoEntity.getUnidadMedida() == unidadMedida) {
                    //La unidad de medida del bulto coincide con la del DTO
                    cantidadActual = bultoEntity.getCantidadActual().add(cantidadActual);
                    cantidadInicial = bultoEntity.getCantidadInicial().add(cantidadInicial);

                    UnidadMedidaEnum unidadSugerida = sugerirUnidadParaCantidad(unidadMedida, cantidadActual);
                    cantidadActual = convertirCantidadEntreUnidades(unidadMedida, cantidadActual, unidadSugerida);
                    cantidadInicial = convertirCantidadEntreUnidades(unidadMedida, cantidadInicial, unidadSugerida);

                    unidadMedida = unidadSugerida;
                } else {
                    //Distitnas unidades de medida
                    UnidadMedidaEnum menorUnidadMedida = obtenerMenorUnidadMedida(bultoEntity.getUnidadMedida(), unidadMedida);
                    BigDecimal cantidadActualTemp = convertirCantidadEntreUnidades(unidadMedida, cantidadActual, menorUnidadMedida);
                    cantidadActualTemp = cantidadActualTemp.add(convertirCantidadEntreUnidades(bultoEntity.getUnidadMedida(), bultoEntity.getCantidadActual(),
                        menorUnidadMedida));

                    UnidadMedidaEnum unidadSugerida = sugerirUnidadParaCantidad(menorUnidadMedida, cantidadActualTemp);
                    cantidadActual = convertirCantidadEntreUnidades(menorUnidadMedida, cantidadActualTemp, unidadSugerida);

                    BigDecimal cantidadInicialTemp = convertirCantidadEntreUnidades(unidadMedida, cantidadInicial, unidadSugerida);
                    cantidadInicial = cantidadInicialTemp.add(convertirCantidadEntreUnidades(bultoEntity.getUnidadMedida(), bultoEntity.getCantidadInicial(), unidadSugerida));
                    unidadMedida = unidadSugerida;
                }
                loteDTO.setCantidadInicial(cantidadInicial);
                loteDTO.setCantidadActual(cantidadActual);
                loteDTO.setUnidadMedida(unidadMedida);

                final int index = loteDTO.getCantidadesBultos().size();
                loteDTO.getNroBultoList().add(index, bultoEntity.getNroBulto());
                loteDTO.getCantidadesBultos().add(index, bultoEntity.getCantidadActual());
                loteDTO.getUnidadMedidaBultos().add(index, bultoEntity.getUnidadMedida());

                addMovimientosDTO(loteDTO, bultoEntity);
            }
        }

        loteDTO.setBultosActuales(entities.size());
        loteDTO.setCantidadInicial(cantidadInicial);
        loteDTO.setCantidadActual(cantidadActual);
        loteDTO.setUnidadMedida(unidadMedida);
        return loteDTO;
    }

    private static void setDatosDerivados(final Lote bultoEntity, final LoteDTO loteDTO) {
        loteDTO.setCodigoInterno(bultoEntity.getCodigoInterno());
        loteDTO.setFechaIngreso(bultoEntity.getFechaIngreso());
        loteDTO.setBultosTotales(bultoEntity.getBultosTotales());
        loteDTO.setEstado(bultoEntity.getEstado().getValor());
        loteDTO.setDictamen(bultoEntity.getDictamen());
        loteDTO.setLoteOrigenId(bultoEntity.getLoteOrigen() != null ? bultoEntity.getLoteOrigen().getId() : null);
        loteDTO.setObservaciones(bultoEntity.getObservaciones());
    }

    private static void setDatosProveedor(final Lote bultoEntity, final LoteDTO loteDTO) {
        loteDTO.setProveedorId(bultoEntity.getProveedor() != null ? bultoEntity.getProveedor().getId() : null);
        loteDTO.setNombreProveedor(bultoEntity.getProveedor() != null ? bultoEntity.getProveedor().getRazonSocial() : null);
        loteDTO.setFabricanteId(bultoEntity.getFabricante() != null ? bultoEntity.getFabricante().getId() : null);
        loteDTO.setNombreFabricante(bultoEntity.getFabricante() != null ? bultoEntity.getFabricante().getRazonSocial() : null);
        loteDTO.setLoteProveedor(bultoEntity.getLoteProveedor());
        loteDTO.setPaisOrigen(bultoEntity.getPaisOrigen());
        loteDTO.setFechaReanalisisProveedor(bultoEntity.getFechaReanalisisProveedor());
        loteDTO.setFechaVencimientoProveedor(bultoEntity.getFechaVencimientoProveedor());
        loteDTO.setNroRemito(bultoEntity.getNroRemito());
        loteDTO.setDetalleConservacion(bultoEntity.getDetalleConservacion());
    }

    private static void setListasBultos(final Lote bultoEntity, final LoteDTO loteDTO) {
        loteDTO.getNroBultoList().add(bultoEntity.getNroBulto());
        loteDTO.getCantidadesBultos().add(bultoEntity.getCantidadActual());
        loteDTO.getUnidadMedidaBultos().add(bultoEntity.getUnidadMedida());
    }

    private static void setProducto(final Lote bultoEntity, final LoteDTO loteDTO) {
        if (bultoEntity.getProducto() != null) {
            final Producto producto = bultoEntity.getProducto();
            loteDTO.setProductoId(producto.getId());
            loteDTO.setNombreProducto(producto.getNombreGenerico());
            loteDTO.setCodigoProducto(producto.getCodigoInterno());
            loteDTO.setTipoProducto(producto.getTipoProducto());
            loteDTO.setProductoDestino(producto.getProductoDestino() != null ? producto.getProductoDestino().getNombreGenerico() : null);
        }
    }

    private static void setEstadoLote(final Lote bultoEntity, final LoteDTO loteDTO) {
        final Optional<EstadoEnum> estadoEnum = EstadoEnum.fromValor(loteDTO.getEstado());
        if (estadoEnum.isPresent()) {
            EstadoEnum estado = estadoEnum.get();
            if (estado.getPrioridad() <
                bultoEntity.getEstado().getPrioridad()) {
                loteDTO.setEstado(bultoEntity.getEstado().getValor());
            }
        } else {
            // Si no se encuentra el estado, se asigna el del bulto
            loteDTO.setEstado(bultoEntity.getEstado().getValor());
        }
    }

    private static void addMovimientosDTO(final LoteDTO loteDTO, final Lote entity) {
        for (Movimiento movimiento : entity.getMovimientos()) {
            final MovimientoDTO movimientoDTO = DTOUtils.fromEntity(movimiento);
            movimientoDTO.setNroBulto(String.valueOf(entity.getNroBulto()));
            loteDTO.getMovimientoDTOs().add(movimientoDTO);
        }
    }

    private static void addAnalisisDTO(final LoteDTO loteDTO, final Lote entity) {
        for (Analisis analisis : entity.getAnalisisList()) {
            loteDTO.getAnalisisDTOs().add(DTOUtils.fromEntity(analisis));
        }
    }

    public static List<LoteDTO> getLotesDtosByCodigoInterno(final List<Lote> lotesCuarentena) {
        Map<String, List<Lote>> lotesAgrupados = lotesCuarentena.stream()
            .collect(Collectors.groupingBy(Lote::getCodigoInterno));
        final List<LoteDTO> lotesDtos = new ArrayList<>();

        for (Map.Entry<String, List<Lote>> entry : lotesAgrupados.entrySet()) {
            List<Lote> lotes = entry.getValue();
            lotesDtos.add(DTOUtils.fromEntities(lotes));
        }
        return lotesDtos;
    }

}
