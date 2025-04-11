package com.mb.conitrack.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;

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
        for (Lote entity : entities) {
            if (firstCase) {
                loteDTO.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());

                if (entity.getProducto() != null) {
                    final Producto producto = entity.getProducto();
                    loteDTO.setProductoId(producto.getId());
                    loteDTO.setNombreProducto(producto.getNombreGenerico());
                    loteDTO.setCodigoProducto(producto.getCodigoInterno());
                    loteDTO.setTipoProducto(producto.getTipoProducto());
                    loteDTO.setProductoDestino(producto.getProductoDestino() != null ? producto.getProductoDestino().getNombreGenerico() : null);
                }

                loteDTO.setProveedorId(entity.getProveedor() != null ? entity.getProveedor().getId() : null);
                loteDTO.setNombreProveedor(entity.getProveedor() != null ? entity.getProveedor().getRazonSocial() : null);

                loteDTO.setFabricanteId(entity.getFabricante() != null ? entity.getFabricante().getId() : null);
                loteDTO.setNombreFabricante(entity.getFabricante() != null ? entity.getFabricante().getRazonSocial() : null);

                loteDTO.setCodigoInterno(entity.getCodigoInterno());
                loteDTO.setPaisOrigen(entity.getPaisOrigen());

                loteDTO.setFechaIngreso(entity.getFechaIngreso());

                loteDTO.setBultosTotales(entity.getBultosTotales());

                loteDTO.setCantidadInicial(entity.getCantidadInicial());
                loteDTO.setUnidadMedida(entity.getUnidadMedida());

                loteDTO.setLoteProveedor(entity.getLoteProveedor());

                loteDTO.setFechaReanalisisProveedor(entity.getFechaReanalisisProveedor());
                loteDTO.setFechaVencimientoProveedor(entity.getFechaVencimientoProveedor());
                loteDTO.setEstadoLote(entity.getEstadoLote().getValor());
                loteDTO.setDictamen(entity.getDictamen());
                loteDTO.setLoteOrigenId(entity.getLoteOrigen()!=null ? entity.getLoteOrigen().getId() : null);

                loteDTO.setNroRemito(entity.getNroRemito());
                loteDTO.setDetalleConservacion(entity.getDetalleConservacion());
                loteDTO.setObservaciones(entity.getObservaciones());

                loteDTO.getCantidadesBultos().add(entity.getCantidadInicial());
                loteDTO.getUnidadMedidaBultos().add(entity.getUnidadMedida());

                addMovimientosDTO(loteDTO, entity);
                addAnalisisDTO(loteDTO, entity);

                firstCase = false;
            } else {
                //TODO: Hacer suma por bulto
                //loteDTO.getCantidadInicial().add(entity.getCantidadInicial().from(entity.getUnidadMedida()).to(loteDTO.getUnidadMedida()))

                loteDTO.getCantidadesBultos().add(entity.getNroBulto() - 1, entity.getCantidadInicial());
                loteDTO.getUnidadMedidaBultos().add(entity.getNroBulto() - 1, entity.getUnidadMedida());

                addMovimientosDTO(loteDTO, entity);
            }
        }
        return loteDTO;
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
