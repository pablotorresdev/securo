package com.mb.conitrack.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.repository.LoteRepository;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.dto.DTOUtils.fromBultoEntities;
import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;

@AllArgsConstructor
@Service
public class LoteService {

    //TODO: unificar la logica de activo vs todos para operatoria vs auditoria
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public Optional<LocalDate> findFechaIngresoLote(final String codigoLote) {
        if (codigoLote == null || codigoLote.isBlank()) {
            return Optional.empty();
        }
        return loteRepository
            .findByCodigoLoteAndActivoTrue(codigoLote.trim())
            .map(Lote::getFechaIngreso);
    }

    @Transactional(readOnly = true)
    public Optional<LocalDate> findFechaVencimientoProveedor(final String codigoLote) {
        if (codigoLote == null || codigoLote.isBlank()) {
            return Optional.empty();
        }
        return loteRepository
            .findByCodigoLoteAndActivoTrue(codigoLote.trim())
            .map(Lote::getFechaVencimientoProveedor);
    }

    @Transactional(readOnly = true)
    public LoteDTO getLoteDTOByCodigoLote(String codigoLote) {
        Lote lote = loteRepository.findByCodigoLoteAndActivoTrue(codigoLote)
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
        return DTOUtils.fromLoteEntity(lote);
    }

    @Transactional(readOnly = true)
    public LoteDTO getLoteDTOByLoteId(Long id) {
        Lote lote = loteRepository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
        return DTOUtils.fromLoteEntity(lote);
    }

    //***********CU2 MODIFICACION: CUARENTENA***********
    //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForCuarentenaDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForCuarentena());
    }

    //***********CU3 BAJA: MUESTREO***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForMuestreoDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForMuestreo());
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForDevolucionCompraDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForDevolucionCompra());
    }

    //***********CU> MODIFICACION: CUZ Reanalisis de Producto Aprobado***********
    //TODO: se debe filtrar por aquellos que no tengan analisis con fecha de vencimiento?
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForReanalisisLoteDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForReanalisisLote());
    }

    //***********CU5/6: RESULTADO ANALISIS***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForResultadoAnalisisDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForResultadoAnalisis());
    }

    //***********CU7 BAJA: CONSUMO PRODUCCION***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForConsumoProduccionDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForConsumoProduccion());
    }

    //***********CU8 MODIFICACION: VENCIDO***********
    @Transactional(readOnly = true)
    public List<Lote> findAllLotesVencidos() { // OJO: devuelve NO vencidos como tu versión previa
        LocalDate hoy = LocalDate.now();
        return loteRepository.findLotesConStockOrder().stream()
            .filter(l -> {
                LocalDate f = l.getFechaVencimientoVigente();
                return f != null && !f.isBefore(hoy); // >= hoy
            })
            .toList(); // ya viene ordenado desde la DB
    }

    //***********CU9 MODIFICACION: ANALSIS EXPIRADO***********
    @Transactional(readOnly = true)
    public List<Lote> findAllLotesAnalisisExpirado() {
        LocalDate hoy = LocalDate.now();
        return loteRepository.findLotesConStockOrder().stream()
            .filter(l -> {
                LocalDate f = l.getFechaReanalisisVigente();
                return f != null && !f.isBefore(hoy); // >= hoy
            })
            .toList();
    }

    //***********CU11 MODIFICACION: LIBERACIÓN UNIDAD DE VENTA***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForLiberacionProductoDTOs() {
        return fromLoteEntities(loteRepository.findAllForLiberacionProducto());
    }

    //***********CU12 BAJA: VENTA***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForVentaProductoDTOs() {
        return fromLoteEntities(loteRepository.findAllForVentaProducto());
    }

    //***********CU13 ALTA: DEVOLUCION VENTA***********
    //***********CU14 MODIFICACION: RETIRO MERCADO***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForDevolucionOrRecallDTOs() {
        return fromLoteEntities(loteRepository.findAllForDevolucionOrRecall());
    }

    //****************************COMMON PUBLIC*****************************
    public Lote findLoteById(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo.");
        }
        return loteRepository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
    }

    @Transactional(readOnly = true)
    public Optional<Lote> findLoteByCodigoLote(final String codigoLote) {
        return loteRepository.findByCodigoLoteAndActivoTrue(
            codigoLote);
    }

    @Transactional(readOnly = true)
    public List<BultoDTO> findBultosForMuestreoByCodigoLote(final String codigoLote) {
        return fromBultoEntities(loteRepository.findBultosForMuestreoByCodigoLote(
            codigoLote));
    }

    @Transactional(readOnly = true)
    public List<Lote> findLoteListByCodigoLote(final String codigoLote) {
        if (codigoLote == null) {
            return new ArrayList<>();
        }
        return loteRepository.findAllByCodigoLoteAndActivoTrue(codigoLote);
    }

    @Transactional(readOnly = true)
    public List<LoteDTO> findLotesDictaminadosConStock() {
        return fromLoteEntities(loteRepository.findLotesDictaminadosConStock());
    }

    public List<Lote> findAllSortByDateAndCodigoLoteAudit() {
        return loteRepository.findAllByOrderByFechaIngresoAscCodigoLoteAsc();
    }

}
