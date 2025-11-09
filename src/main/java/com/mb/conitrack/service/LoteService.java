package com.mb.conitrack.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.repository.LoteRepository;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.dto.DTOUtils.fromBultoEntities;
import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;

@AllArgsConstructor
@Service
public class LoteService {

    private final LoteRepository loteRepository;

    //***********CU2 MODIFICACION: CUARENTENA***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForCuarentenaDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForCuarentena());
    }

    //***********CU3 BAJA: MUESTREO***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForMuestreoTrazableDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForMuestreoTrazable());
    }

    //***********CU 3 BAJA: MUESTREO MULTIBULTO***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForMuestreoMultiBultoDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForMuestreoMultiBulto());
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForDevolucionCompraDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForDevolucionCompra());
    }

    //***********CU> MODIFICACION: CU8 Reanalisis de Producto Aprobado***********
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

    //***********CU21 MODIFICACION: LIBERACIÓN UNIDAD DE VENTA***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForLiberacionProductoDTOs() {
        return fromLoteEntities(loteRepository.findAllForLiberacionProducto());
    }

    //***********CU27 MODIFICACION: TRAZADO LOTE***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForTrazadoLoteDTOs() {
        return fromLoteEntities(loteRepository.findAllForTrazadoLote());
    }

    //***********CU22 BAJA: VENTA***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForVentaProductoDTOs() {
        return fromLoteEntities(loteRepository.findAllForVentaProducto());
    }

    //***********CU23 ALTA: DEVOLUCION VENTA***********
    //***********CU24 MODIFICACION: RETIRO MERCADO***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForDevolucionDTOs() {
        return fromLoteEntities(loteRepository.findAllForDevolucion());
    }
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForRecallDTOs() {
        return fromLoteEntities(loteRepository.findAllForRecall());
    }

    //***********CU25 BAJA: AJUSTE STOCK***********
    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForAjusteDTOs() {
        return DTOUtils.fromLoteEntities(loteRepository.findAllForAjuste());
    }

    @Transactional(readOnly = true)
    public List<BultoDTO> findBultosForAjusteByCodigoLote(final String codigoLote) {
        return fromBultoEntities(loteRepository.findBultosForAjusteByCodigoLote(
            codigoLote));
    }

    //****************************COMMON PUBLIC*****************************
    @Transactional(readOnly = true)
    public List<BultoDTO> findBultosForMuestreoByCodigoLote(final String codigoLote) {
        return fromBultoEntities(loteRepository.findBultosForMuestreoByCodigoLote(
            codigoLote));
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


    @Transactional(readOnly = true)
    public List<LoteDTO> findAllForReversoMovimientoDTOs() {
        return fromLoteEntities(loteRepository.findAllForReversoMovimiento());
    }

    @Transactional(readOnly = true)
    public java.util.Optional<com.mb.conitrack.entity.Lote> findByCodigoLote(final String codigoLote) {
        return loteRepository.findByCodigoLoteAndActivoTrue(codigoLote);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<LoteDTO> findDTOByCodigoLote(final String codigoLote) {
        return loteRepository.findByCodigoLoteAndActivoTrue(codigoLote)
                .map(DTOUtils::fromLoteEntity);
    }

}

