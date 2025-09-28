package com.mb.conitrack.service;

import java.util.List;

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
    @Transactional(readOnly = true)
    public List<BultoDTO> findBultosForMuestreoByCodigoLote(final String codigoLote) {
        return fromBultoEntities(loteRepository.findBultosForMuestreoByCodigoLote(
            codigoLote));
    }

    @Transactional(readOnly = true)
    public List<Lote> findLoteListByCodigoLote(final String codigoLote) {
        return loteRepository.findAllByCodigoLoteAndActivoTrue(codigoLote);
    }

    @Transactional(readOnly = true)
    public List<LoteDTO> findLotesDictaminadosConStock() {
        return fromLoteEntities(loteRepository.findLotesDictaminadosConStock());
    }

    @Transactional(readOnly = true)
    public List<LoteDTO> findAllLotesAudit() {
        return fromLoteEntities(loteRepository.findAllByOrderByFechaIngresoAscCodigoLoteAsc());
    }

    @Transactional(readOnly = true)
    public List<LoteDTO> findAllLotes() {
        return fromLoteEntities(loteRepository.findAllByActivoTrue());
    }

    //***********CU BAJA: MUESTREO MULTIBULTO***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForMuestreoMultiBultoDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForMuestreoMultiBulto());
    }

}

