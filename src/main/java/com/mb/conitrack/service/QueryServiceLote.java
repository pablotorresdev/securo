package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.repository.LoteRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class QueryServiceLote {

    //TODO: unificar la logica de activo vs todos para operatoria vs auditoria
    private final LoteRepository loteRepository;

    //***********CU2 MODIFICACION: CUARENTENA***********
    public List<Lote> findAllForCuarentena() {
        //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndCodigoInterno();
        return allSortByDateAndNroBulto.stream().filter(l -> EnumSet.of(
                DictamenEnum.RECIBIDO,
                DictamenEnum.APROBADO,
                DictamenEnum.ANALISIS_EXPIRADO,
                DictamenEnum.LIBERADO,
                DictamenEnum.DEVOLUCION_CLIENTES,
                DictamenEnum.RETIRO_MERCADO).contains(l.getDictamen()))
            .filter(l -> EnumSet.of(
                EstadoEnum.NUEVO,
                EstadoEnum.DISPONIBLE,
                EstadoEnum.EN_USO).contains(l.getEstado()))
            .toList();
    }

    //***********CU3 BAJA: MUESTREO***********
    public List<Lote> findAllForMuestreo() {
        return findAllSortByDateAndCodigoInterno().stream()
            .filter(l -> l.getDictamen() != DictamenEnum.RECIBIDO)
            .filter(l -> l.getAnalisisList().stream().anyMatch(a -> a.getNroAnalisis() != null))
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    public List<Lote> findAllForDevolucionCompra() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndCodigoInterno();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> EnumSet.of(
                    DictamenEnum.RECIBIDO,
                    DictamenEnum.CUARENTENA,
                    DictamenEnum.APROBADO,
                    DictamenEnum.RECHAZADO)
                .contains(l.getDictamen()))
            .filter(l -> EnumSet.of(
                TipoProductoEnum.API,
                TipoProductoEnum.EXCIPIENTE,
                TipoProductoEnum.ACOND_PRIMARIO,
                TipoProductoEnum.ACOND_SECUNDARIO).contains(l.getProducto().getTipoProducto()))
            .filter(l -> EstadoEnum.DEVUELTO != l.getEstado())
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //***********CU> MODIFICACION: CUZ Reanalisis de Producto Aprobado***********
    public List<Lote> findAllForReanalisisProducto() {
        //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndCodigoInterno();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> l.getDictamen() == DictamenEnum.APROBADO)
            .filter(l -> EnumSet.of(
                EstadoEnum.NUEVO,
                EstadoEnum.DISPONIBLE,
                EstadoEnum.EN_USO).contains(l.getEstado()))
            .filter(l -> l.getAnalisisList()
                .stream()
                .noneMatch(a -> a.getDictamen() == null && a.getFechaRealizado() == null))
            .toList();
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    public List<Lote> findAllForResultadoAnalisis() {
        final List<Lote> lotes = loteRepository.findAll();
        return lotes.stream()
            .filter(Lote::getActivo)
            .filter(l -> EnumSet.of(DictamenEnum.CUARENTENA).contains(l.getDictamen()))
            .filter(l -> l.getAnalisisList()
                .stream()
                .anyMatch(a -> a.getDictamen() == null && a.getFechaRealizado() == null))
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //***********CU7 BAJA: CONSUMO PRODUCCION***********
    public List<Lote> findAllForConsumoProduccion() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndCodigoInterno();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.APROBADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA != l.getProducto().getTipoProducto())
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //***********CU8 MODIFICACION: VENCIDO***********
    public List<Lote> findAllLotesVencidos() {
        final LocalDate now = LocalDate.now();
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaVencimientoVigente() != null &&
                !lote.getFechaVencimientoVigente().isBefore(now))
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
    }

    //***********CU9 MODIFICACION: ANALSIS EXPIRADO***********
    public List<Lote> findAllLotesAnalisisExpirado() {
        final LocalDate now = LocalDate.now();
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaReanalisisVigente() != null && !lote.getFechaReanalisisVigente().isBefore(now))
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
    }

    //***********CU11 MODIFICACION: LIBERACIÓN UNIDAD DE VENTA***********
    public List<Lote> findAllForLiberacionProducto() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndCodigoInterno();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.APROBADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA == l.getProducto().getTipoProducto())
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //***********CU12 BAJA: VENTA***********
    public List<Lote> findAllForVentaProducto() {
        final List<Lote> allSortByDateAndNroBulto = findAllSortByDateAndCodigoInterno();
        return allSortByDateAndNroBulto.stream()
            .filter(l -> DictamenEnum.LIBERADO == l.getDictamen())
            .filter(l -> TipoProductoEnum.UNIDAD_VENTA == l.getProducto().getTipoProducto())
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .toList();
    }

    //***********CU13 ALTA: DEVOLUCION VENTA***********
    public List<Lote> findAllForDevolucionVenta() {
        final List<Lote> result = new ArrayList<>();
        for (Lote lote : findAllSortByDateAndCodigoInterno()) {
            boolean containsVenta = false;
            for (Traza traza : lote.getTrazas()) {
                if (traza.getEstado() == EstadoEnum.VENDIDO) {
                    containsVenta = true;
                    break;
                }
            }
            if (containsVenta) {
                result.add(lote);
            }
        }
        return result;
    }

    //****************************COMMON PUBLIC*****************************
    public Lote findLoteBultoById(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo.");
        }
        return loteRepository.findById(id)
            .filter(Lote::getActivo)
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    public Optional<Lote> findLoteByCodigoInterno(final String codigoInternoLote) {
        if (codigoInternoLote == null) {
            return null;
        }
        return loteRepository.findByCodigoInternoAndActivoTrue(codigoInternoLote);
    }

    public List<Lote> findLoteListByCodigoInterno(final String codigoInternoLote) {
        if (codigoInternoLote == null) {
            return new ArrayList<>();
        }
        return loteRepository.findAllByCodigoInternoAndActivoTrue(codigoInternoLote);
    }

    public List<Lote> findAllLotesDictaminados() {
        return loteRepository.findAll()
            .stream()
            .filter(lote -> lote.getFechaVencimientoVigente() != null || lote.getFechaReanalisisVigente() != null)
            .filter(l -> l.getBultos().stream()
                .anyMatch(b -> b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0))
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
    }

    public List<Lote> findAllSortByDateAndCodigoInternoAudit() {
        return loteRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
    }

    //****************************COMMON PRIVATE*****************************
    List<Lote> findAllSortByDateAndCodigoInterno() {
        return loteRepository.findAll()
            .stream()
            .filter(Lote::getActivo)
            .sorted(Comparator.comparing(Lote::getFechaIngreso)
                .thenComparing(Lote::getCodigoInterno))
            .toList();
    }

}
