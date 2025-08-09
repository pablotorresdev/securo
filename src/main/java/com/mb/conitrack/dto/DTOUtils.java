package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static com.mb.conitrack.utils.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.utils.UnidadMedidaUtils.obtenerMenorUnidadMedida;
import static com.mb.conitrack.utils.UnidadMedidaUtils.sugerirUnidadParaCantidad;

public class DTOUtils {

    public static Analisis createAnalisis(final MovimientoDTO dto) {
        final String nroAnalisis = StringUtils.isEmpty(dto.getNroReanalisis())
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

    public static BultoDTO fromEntity(Bulto entity) {
        if (entity == null) {
            return null;
        }

        BultoDTO dto = new BultoDTO();
        dto.setNroBulto(entity.getNroBulto());
        dto.setCantidadActual(entity.getCantidadActual());
        dto.setCantidadInicial(entity.getCantidadInicial());
        dto.setUnidadMedida(entity.getUnidadMedida());
        dto.setCodigoLote(entity.getLote().getCodigoInterno());
        //dto.setMovimientos(entity.getMovimientos());
        dto.setEstado(entity.getEstado());
        //dto.setTrazas(entity.getTrazas());
        return dto;
    }

    public static MovimientoDTO fromEntity(Movimiento entity) {
        if (entity == null) {
            return null;
        }

        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(entity.getFecha());
        if (entity.getLote() != null) {
            dto.setCodigoInternoLote(entity.getLote().getCodigoInterno());
            //            dto.setNroBulto(entity.getLote().getNroBulto().toString());
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

    public static TrazaDTO fromEntity(Traza entity) {
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

    public static List<LoteDTO> getLotesDtosByCodigoInterno(final List<Lote> loteList) {
        Map<String, List<Lote>> lotesByCodigoInterno = loteList.stream()
            .collect(Collectors.groupingBy(Lote::getCodigoInterno));
        final List<LoteDTO> lotesDtos = new ArrayList<>();

        for (Map.Entry<String, List<Lote>> entry : lotesByCodigoInterno.entrySet()) {
            List<Lote> lotes = entry.getValue();
            lotesDtos.add(DTOUtils.mergeEntities(lotes));
        }
        return lotesDtos;
    }

    public static LoteDTO mergeEntities(List<Lote> entities) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }

        LoteDTO loteDTO = new LoteDTO();
        boolean firstCase = true;

        BigDecimal cantidadInicial = BigDecimal.ZERO;
        BigDecimal cantidadActual = BigDecimal.ZERO;
        UnidadMedidaEnum unidadMedida = null;
        Long trazaInicial = null;

        for (Lote bultoEntity : entities) {
            if (!bultoEntity.getActivo()) {
                continue;
            }
            if (firstCase) {
                loteDTO.setFechaYHoraCreacion(bultoEntity.getFechaYHoraCreacion());

                setProductoLote(bultoEntity, loteDTO);
                setDatosProveedorLote(bultoEntity, loteDTO);
                setDatosDerivadosLote(bultoEntity, loteDTO);
                setListasBultosLote(bultoEntity, loteDTO);
                addMovimientosDTO(bultoEntity, loteDTO);
                addAnalisisDTO(bultoEntity, loteDTO);
                addTrazaDTO(bultoEntity, loteDTO);

                cantidadInicial = bultoEntity.getCantidadInicial();
                cantidadActual = bultoEntity.getCantidadActual();
                unidadMedida = bultoEntity.getUnidadMedida();
                if (bultoEntity.getFirstActiveTraza() != null) {
                    trazaInicial = bultoEntity.getFirstActiveTraza().getNroTraza();
                }

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
                    UnidadMedidaEnum menorUnidadMedida = obtenerMenorUnidadMedida(
                        bultoEntity.getUnidadMedida(),
                        unidadMedida);
                    BigDecimal cantidadActualTemp = convertirCantidadEntreUnidades(
                        unidadMedida,
                        cantidadActual,
                        menorUnidadMedida);
                    cantidadActualTemp = cantidadActualTemp.add(convertirCantidadEntreUnidades(
                        bultoEntity.getUnidadMedida(),
                        bultoEntity.getCantidadActual(),
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
                        bultoEntity.getUnidadMedida(),
                        bultoEntity.getCantidadInicial(),
                        unidadSugerida));
                    unidadMedida = unidadSugerida;
                }
                if (bultoEntity.getFirstActiveTraza() != null) {
                    if (trazaInicial == null) {
                        trazaInicial = bultoEntity.getFirstActiveTraza().getNroTraza();
                    } else {
                        trazaInicial = Math.min(trazaInicial, bultoEntity.getFirstActiveTraza().getNroTraza());
                    }
                }
                loteDTO.setCantidadInicial(cantidadInicial);
                loteDTO.setCantidadActual(cantidadActual);
                loteDTO.setUnidadMedida(unidadMedida);

                final int index = loteDTO.getCantidadesBultos().size();
                loteDTO.getNroBultoList().add(index, bultoEntity.getNroBulto());
                loteDTO.getCantidadesBultos().add(index, bultoEntity.getCantidadActual());
                loteDTO.getUnidadMedidaBultos().add(index, bultoEntity.getUnidadMedida());

                addMovimientosDTO(bultoEntity, loteDTO);
            }
        }

        loteDTO.setTrazaInicial(trazaInicial);
        loteDTO.setBultosActuales(entities.size());
        loteDTO.setCantidadInicial(cantidadInicial);
        loteDTO.setCantidadActual(cantidadActual);
        loteDTO.setUnidadMedida(unidadMedida);
        return loteDTO;
    }

    public static LoteDTO mergeEntities(Lote loteEntity) {
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

        BigDecimal cantidadInicialLote = BigDecimal.ZERO;
        BigDecimal cantidadActualLote = BigDecimal.ZERO;
        UnidadMedidaEnum uMedActual = loteDTO.getBultosDTOs().get(0).getUnidadMedida();

        for (Bulto bultoEntity : loteEntity.getBultos()) {
            if (!bultoEntity.getActivo()) {
                continue;
            }

            // Si el estado de alguno de los bultos es diferente, se asigna el de mayor prioridad
            setEstadoBulto(bultoEntity, loteDTO);

            if (bultoEntity.getUnidadMedida() == uMedActual) {
                //La unidad de medida del bulto coincide con la del DTO
                cantidadActualLote = bultoEntity.getCantidadActual().add(cantidadActualLote);
                cantidadInicialLote = bultoEntity.getCantidadInicial().add(cantidadInicialLote);

                UnidadMedidaEnum uMedSugerida = sugerirUnidadParaCantidad(uMedActual, cantidadActualLote);
                cantidadActualLote = convertirCantidadEntreUnidades(uMedActual, cantidadActualLote, uMedSugerida);
                cantidadInicialLote = convertirCantidadEntreUnidades(uMedActual, cantidadInicialLote, uMedSugerida);

                uMedActual = uMedSugerida;
            } else {
                //Distitnas unidades de medida
                UnidadMedidaEnum menorUMed = obtenerMenorUnidadMedida(bultoEntity.getUnidadMedida(), uMedActual);
                BigDecimal cantidadActualLoteTemp = convertirCantidadEntreUnidades(
                    uMedActual,
                    cantidadActualLote,
                    menorUMed);
                cantidadActualLoteTemp = cantidadActualLoteTemp.add(convertirCantidadEntreUnidades(
                    bultoEntity.getUnidadMedida(),
                    bultoEntity.getCantidadActual(),
                    menorUMed));

                UnidadMedidaEnum uMedSugerida = sugerirUnidadParaCantidad(menorUMed, cantidadActualLoteTemp);
                cantidadActualLote = convertirCantidadEntreUnidades(menorUMed, cantidadActualLoteTemp, uMedSugerida);

                BigDecimal cantidadInicialTemp = convertirCantidadEntreUnidades(
                    uMedActual,
                    cantidadInicialLote,
                    uMedSugerida);
                cantidadInicialLote = cantidadInicialTemp.add(convertirCantidadEntreUnidades(
                    bultoEntity.getUnidadMedida(),
                    bultoEntity.getCantidadInicial(),
                    uMedSugerida));
                uMedActual = uMedSugerida;
            }
            if (bultoEntity.getFirstActiveTraza() != null) {
                if (trazaInicial == null) {
                    trazaInicial = bultoEntity.getFirstActiveTraza().getNroTraza();
                } else {
                    trazaInicial = Math.min(trazaInicial, bultoEntity.getFirstActiveTraza().getNroTraza());
                }
            }

            //Add at the end of the lists
            final int index = loteDTO.getCantidadesBultos().size();
            loteDTO.getNroBultoList().add(index, bultoEntity.getNroBulto());
            loteDTO.getCantidadesBultos().add(index, bultoEntity.getCantidadActual());
            loteDTO.getUnidadMedidaBultos().add(index, bultoEntity.getUnidadMedida());
        }

        loteDTO.setTrazaInicial(trazaInicial);
        loteDTO.setBultosActuales(loteEntity.getBultos().size());
        loteDTO.setCantidadInicial(cantidadInicialLote);
        loteDTO.setCantidadActual(cantidadActualLote);
        loteDTO.setUnidadMedida(uMedActual);
        return loteDTO;
    }

    static void addAnalisisDTO(final Lote entity, final LoteDTO loteDTO) {
        for (Analisis analisis : entity.getAnalisisList()) {
            if (analisis.getActivo()) {
                loteDTO.getAnalisisDTOs().add(DTOUtils.fromEntity(analisis));
            }
        }
    }

    static void addMovimientosDTO(final Bulto entity, final LoteDTO loteDTO) {
        for (Movimiento movimiento : entity.getMovimientos()) {
            if (movimiento.getActivo()) {
                final MovimientoDTO movimientoDTO = DTOUtils.fromEntity(movimiento);
                movimientoDTO.setNroBulto(String.valueOf(entity.getNroBulto()));
                loteDTO.getMovimientoDTOs().add(movimientoDTO);
            }
        }
    }

    static void addMovimientosDTO(final Lote entity, final LoteDTO loteDTO) {
        for (Movimiento movimiento : entity.getMovimientos()) {
            if (movimiento.getActivo()) {
                final MovimientoDTO movimientoDTO = DTOUtils.fromEntity(movimiento);
                movimientoDTO.setNroBulto(String.valueOf(entity.getNroBulto()));
                loteDTO.getMovimientoDTOs().add(movimientoDTO);
            }
        }
    }

    static void addTrazaDTO(final Lote entity, final LoteDTO loteDTO) {
        for (Traza traza : entity.getTrazas()) {
            if (traza.getActivo()) {
                loteDTO.getTrazaDTOs().add(DTOUtils.fromEntity(traza));
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

    static void setEstadoBulto(final Bulto bultoEntity, final LoteDTO loteDTO) {
        final Optional<EstadoEnum> estadoEnum = EstadoEnum.fromValor(loteDTO.getEstado());
        if (estadoEnum.isPresent()) {
            EstadoEnum estado = estadoEnum.get();
            if (estado.getPrioridad() < bultoEntity.getEstado().getPrioridad()) {
                loteDTO.setEstado(bultoEntity.getEstado().getValor());
            }
        } else {
            // Si no se encuentra el estado, se asigna el del bulto
            loteDTO.setEstado(bultoEntity.getEstado().getValor());
        }
    }

    static void setEstadoLote(final Lote bultoEntity, final LoteDTO loteDTO) {
        final Optional<EstadoEnum> estadoEnum = EstadoEnum.fromValor(loteDTO.getEstado());
        if (estadoEnum.isPresent()) {
            EstadoEnum estado = estadoEnum.get();
            if (estado.getPrioridad() < bultoEntity.getEstado().getPrioridad()) {
                loteDTO.setEstado(bultoEntity.getEstado().getValor());
            }
        } else {
            // Si no se encuentra el estado, se asigna el del bulto
            loteDTO.setEstado(bultoEntity.getEstado().getValor());
        }
    }

    static void setListasBultosLote(final Lote entity, final LoteDTO loteDTO) {
        for (Bulto bulto : entity.getBultos()) {
            if (bulto.getActivo()) {
                final BultoDTO bultoDto = DTOUtils.fromEntity(bulto);
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
