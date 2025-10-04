package com.mb.conitrack.utils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

public class LoteEntityUtils {

    //***********CU1 ALTA: COMPRA***********
    //***********CU20 ALTA: INGRESO PRODUCCION INTERNA***********
    public static Lote createLoteIngreso(final LoteDTO loteDTO) {
        Lote lote = new Lote();

        lote.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
        lote.setEstado(EstadoEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setActivo(Boolean.TRUE);

        //Datos obligatorios comunes
        lote.setPaisOrigen(loteDTO.getPaisOrigen());
        lote.setFechaIngreso(loteDTO.getFechaIngreso());
        lote.setBultosTotales(loteDTO.getBultosTotales());
        lote.setLoteProveedor(loteDTO.getLoteProveedor());
        lote.setOrdenProduccionOrigen(loteDTO.getOrdenProduccion());

        //Datos opcionales comunes
        lote.setFechaReanalisisProveedor(loteDTO.getFechaReanalisisProveedor());
        lote.setFechaVencimientoProveedor(loteDTO.getFechaVencimientoProveedor());
        lote.setNroRemito(loteDTO.getNroRemito());
        lote.setDetalleConservacion(loteDTO.getDetalleConservacion());
        lote.setObservaciones(loteDTO.getObservaciones());

        return lote;
    }

    //***********CU3 BAJA: MUESTREO***********
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

    //***********CU> MODIFICACION: CU8 Reanalisis de Producto Aprobado***********
    public static void populateLoteAltaProduccionPropia(
        final Lote lote,
        final LoteDTO loteDTO,
        final Producto producto,
        final Proveedor conifarma) {

        lote.setCodigoLote("L-" +
            producto.getCodigoProducto() +
            "-" +
            loteDTO.getFechaYHoraCreacion().format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss")));

        lote.setProducto(producto);
        lote.setProveedor(conifarma);
        lote.setFabricante(conifarma);
        lote.setPaisOrigen(conifarma.getPais());

        boolean unidadVenta = producto.getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA;
        List<Traza> trazas = unidadVenta
            ? createTrazas(loteDTO, producto, loteDTO.getCantidadInicial())
            : new ArrayList<>();

        for (Traza t : trazas) {
            t.setLote(lote);
        }

        int idxTrazaActual = 0;
        int bultos = Math.max(loteDTO.getBultosTotales(), 1);
        for (int i = 0; i < bultos; i++) {
            Bulto bulto = createBultoIngreso();
            populateCantidadUdeMBulto(loteDTO, bultos, bulto, i);
            lote.getBultos().add(bulto);
            bulto.setLote(lote);

            if (unidadVenta) {
                final int indexTrazaFinal = bulto.getCantidadInicial().intValue();
                List<Traza> trazasBulto = new ArrayList<>(trazas.subList(
                    idxTrazaActual,
                    idxTrazaActual + indexTrazaFinal));
                for (Traza t : trazasBulto) {
                    t.setBulto(bulto);
                }
                bulto.getTrazas().addAll(trazasBulto);
                idxTrazaActual += indexTrazaFinal;
            }
        }

        lote.getTrazas().addAll(trazas);
        lote.setBultosTotales(bultos);
        lote.setCantidadInicial(loteDTO.getCantidadInicial());
        lote.setCantidadActual(loteDTO.getCantidadInicial());
        lote.setUnidadMedida(loteDTO.getUnidadMedida());
    }

    //***********CU1 ALTA: COMPRA***********
    public static void populateLoteAltaStockCompra(
        final Lote lote,
        final LoteDTO loteDTO,
        final Producto producto,
        final Proveedor proveedor,
        final Proveedor fabricante) {
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

    //***********PRIVATE COMMONS***********
    static Bulto createBultoIngreso() {
        Bulto bulto = new Bulto();
        bulto.setEstado(EstadoEnum.NUEVO);
        bulto.setActivo(Boolean.TRUE);
        return bulto;
    }

    static List<Traza> createTrazas(
        final LoteDTO loteDTO,
        final Producto producto,
        final BigDecimal cantidadInicialLote) {
        List<Traza> trazas = new ArrayList<>();
        if (loteDTO.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
            throw new IllegalStateException("La traza solo es aplicable a UNIDADES");
        }

        if (cantidadInicialLote.stripTrailingZeros().scale() > 0) {
            throw new IllegalStateException("La cantidad de Unidades debe ser entero");
        }
        for (int i = 0; i < cantidadInicialLote.intValue(); i++) {
            Traza traza = new Traza();
            traza.setNroTraza(loteDTO.getTrazaInicial() + i);
            traza.setFechaYHoraCreacion(loteDTO.getFechaYHoraCreacion());
            traza.setProducto(producto);
            traza.setObservaciones("CU7 Traza: " +
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

    static void populateCantidadUdeMBulto(
        final LoteDTO loteDTO,
        final int bultosTotales,
        final Bulto bulto,
        final int i) {
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

}
