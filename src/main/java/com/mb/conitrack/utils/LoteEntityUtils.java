package com.mb.conitrack.utils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

/** Utilidades para creación y población de entidades Lote (CU1, CU20, CU28). */
public class LoteEntityUtils {

    private LoteEntityUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** Crea lote nuevo para ingreso (compra/producción) con campos base inicializados. */
    public static Lote createLoteIngreso(final LoteDTO loteDTO) {
        Objects.requireNonNull(loteDTO, "loteDTO cannot be null");

        Lote lote = new Lote();

        lote.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
        lote.setEstado(EstadoEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setActivo(Boolean.TRUE);

        // Required common fields
        lote.setPaisOrigen(loteDTO.getPaisOrigen());
        lote.setFechaIngreso(loteDTO.getFechaIngreso());
        lote.setBultosTotales(loteDTO.getBultosTotales());
        lote.setLoteProveedor(loteDTO.getLoteProveedor());
        lote.setOrdenProduccionOrigen(loteDTO.getOrdenProduccion());

        // Optional common fields
        lote.setFechaReanalisisProveedor(loteDTO.getFechaReanalisisProveedor());
        lote.setFechaVencimientoProveedor(loteDTO.getFechaVencimientoProveedor());
        lote.setNroRemito(loteDTO.getNroRemito());
        lote.setDetalleConservacion(loteDTO.getDetalleConservacion());
        lote.setObservaciones(loteDTO.getObservaciones());

        return lote;
    }

    /** Busca análisis en curso (activo, sin dictamen, sin fecha realizado). */
    public static Optional<Analisis> getAnalisisEnCurso(final List<Analisis> analisisList) {
        Objects.requireNonNull(analisisList, "analisisList cannot be null");

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

    /** Puebla lote para ingreso por producción interna (CU20). Genera código, crea bultos. */
    public static void populateLoteAltaProduccionPropia(
        final Lote lote,
        final LoteDTO loteDTO,
        final Producto producto,
        final Proveedor conifarma) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Objects.requireNonNull(loteDTO, "loteDTO cannot be null");
        Objects.requireNonNull(producto, "producto cannot be null");
        Objects.requireNonNull(conifarma, "conifarma cannot be null");

        lote.setCodigoLote("L-" +
            producto.getCodigoProducto() +
            "-" +
            loteDTO.getFechaYHoraCreacion().format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss")));

        lote.setProducto(producto);
        lote.setProveedor(conifarma);
        lote.setFabricante(conifarma);
        lote.setPaisOrigen(conifarma.getPais());
        int bultos = Math.max(loteDTO.getBultosTotales(), 1);

        for (int i = 0; i < bultos; i++) {
            Bulto bulto = createBultoIngreso();
            populateCantidadUdeMBulto(loteDTO, bultos, bulto, i);
            lote.getBultos().add(bulto);
            bulto.setLote(lote);
        }

        lote.setBultosTotales(bultos);
        lote.setCantidadInicial(loteDTO.getCantidadInicial());
        lote.setCantidadActual(loteDTO.getCantidadInicial());
        lote.setUnidadMedida(loteDTO.getUnidadMedida());
    }

    /** Puebla lote para ingreso por compra (CU1). Genera código, determina país origen, crea bultos. */
    public static void populateLoteAltaStockCompra(
        final Lote lote,
        final LoteDTO loteDTO,
        final Producto producto,
        final Proveedor proveedor,
        final Proveedor fabricante) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Objects.requireNonNull(loteDTO, "loteDTO cannot be null");
        Objects.requireNonNull(producto, "producto cannot be null");
        Objects.requireNonNull(proveedor, "proveedor cannot be null");

        lote.setCodigoLote("L-" +
            producto.getCodigoProducto() +
            "-" +
            loteDTO.getFechaYHoraCreacion().format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss")));

        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setFabricante(fabricante);

        if (StringUtils.isEmptyOrWhitespace(loteDTO.getPaisOrigen())) {
            if (fabricante != null) {
                lote.setPaisOrigen(fabricante.getPais());
            } else {
                lote.setPaisOrigen(proveedor.getPais());
            }
        } else {
            lote.setPaisOrigen(loteDTO.getPaisOrigen());
        }

        int bultosTotales = Math.max(loteDTO.getBultosTotales(), 1);
        for (int i = 0; i < bultosTotales; i++) {
            Bulto bulto = createBultoIngreso();
            populateCantidadUdeMBulto(loteDTO, bultosTotales, bulto, i);
            lote.getBultos().add(bulto);
            bulto.setLote(lote);
        }
        lote.setBultosTotales(bultosTotales);
        lote.setCantidadInicial(loteDTO.getCantidadInicial());
        lote.setCantidadActual(loteDTO.getCantidadInicial());
        lote.setUnidadMedida(loteDTO.getUnidadMedida());
    }

    /** Crea bulto para operaciones de ingreso (estado NUEVO, activo). */
    static Bulto createBultoIngreso() {
        Bulto bulto = new Bulto();
        bulto.setEstado(EstadoEnum.NUEVO);
        bulto.setActivo(Boolean.TRUE);
        return bulto;
    }

    /** Crea trazas individuales para lote trazable (solo UNIDAD, cantidad entera). */
    static List<Traza> createTrazas(
        final MovimientoDTO movimientoDto,
        final Lote lote) {
        Objects.requireNonNull(movimientoDto, "movimientoDto cannot be null");
        Objects.requireNonNull(lote, "lote cannot be null");

        final Producto producto = lote.getProducto();

        List<Traza> trazas = new ArrayList<>();
        if (lote.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
            throw new IllegalStateException("La traza solo es aplicable a UNIDADES");
        }

        if (lote.getCantidadActual().stripTrailingZeros().scale() > 0) {
            throw new IllegalStateException("La cantidad de Unidades debe ser entero");
        }
        for (int i = 0; i < lote.getCantidadActual().intValue(); i++) {
            Traza traza = new Traza();
            traza.setNroTraza(movimientoDto.getTrazaInicial() + i);
            traza.setFechaYHoraCreacion(movimientoDto.getFechaYHoraCreacion());
            traza.setProducto(producto);
            traza.setObservaciones("CU28 Traza: " +
                traza.getNroTraza() +
                "\n - Producto: " +
                producto.getCodigoProducto() +
                " / " +
                producto.getNombreGenerico());
            traza.setEstado(EstadoEnum.DISPONIBLE);
            traza.setActivo(true);
            trazas.add(traza);
        }
        return trazas;
    }

    /** Asigna cantidad y unidad de medida al bulto según cantidad total y distribución. */
    static void populateCantidadUdeMBulto(
        final LoteDTO loteDTO,
        final int bultosTotales,
        final Bulto bulto,
        final int i) {
        Objects.requireNonNull(loteDTO, "loteDTO cannot be null");
        Objects.requireNonNull(bulto, "bulto cannot be null");

        if (bultosTotales == 1) {
            bulto.setCantidadInicial(loteDTO.getCantidadInicial());
            bulto.setCantidadActual(loteDTO.getCantidadInicial());
            bulto.setUnidadMedida(loteDTO.getUnidadMedida());
        } else {
            bulto.setCantidadInicial(loteDTO.getCantidadesBultos().get(i));
            bulto.setCantidadActual(loteDTO.getCantidadesBultos().get(i));
            bulto.setUnidadMedida(loteDTO.getUnidadMedidaBultos().get(i));
        }
        bulto.setNroBulto(i + 1);
    }

    /** CU28 - Agrega trazas al lote y las distribuye entre bultos. Marca lote como trazado. */
    public static void addTrazasToLote(
        final Lote lote,
        final MovimientoDTO movimientoDto) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Objects.requireNonNull(movimientoDto, "movimientoDto cannot be null");

        List<Traza> trazas = createTrazas(movimientoDto, lote);

        for (Traza t : trazas) {
            t.setLote(lote);
        }
        int idxTrazaActual = 0;
        for (Bulto bulto : lote.getBultos()) {
            final int indexTrazaFinal = bulto.getCantidadActual().intValue();
            List<Traza> trazasBulto = new ArrayList<>(trazas.subList(
                idxTrazaActual,
                idxTrazaActual + indexTrazaFinal));
            for (Traza t : trazasBulto) {
                t.setBulto(bulto);
            }
            bulto.getTrazas().addAll(trazasBulto);
            idxTrazaActual += indexTrazaFinal;
        }
        lote.getTrazas().addAll(trazas);
        lote.setTrazado(true);
    }

}
