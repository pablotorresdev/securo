package com.mb.conitrack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.repository.TrazaRepository;

import static com.mb.conitrack.dto.DTOUtils.fromTrazaEntities;

@Service
public class TrazaService {

    @Autowired
    private TrazaRepository trazaRepository;

    @Transactional(readOnly = true)
    public List<TrazaDTO> findAllTrazaAudit() {
        return fromTrazaEntities(trazaRepository.findAllTrazaAudit());
    }

    @Transactional(readOnly = true)
    public List<TrazaDTO> findByCodigoLote(final String codigoLote) {
        return fromTrazaEntities(trazaRepository.findByLoteCodigoLoteOrderByNroTrazaAsc(codigoLote));
    }

    //***********CU13 MODIF: DEVOLUCION VENTA***********
    @Transactional(readOnly = true)
    public List<TrazaDTO> getTrazasVendidasByCodigoMovimiento(final String codigoMovimiento) {
        return fromTrazaEntities(trazaRepository.findVendidasByCodigoMovimiento(codigoMovimiento));
    }

    //***********CU14 MODIF: RECALL***********
    @Transactional(readOnly = true)
    public List<TrazaDTO> getTrazasVendidasByCodigoLote(final String codigoLote) {
        return fromTrazaEntities(trazaRepository.findVendidasByCodigoLote(codigoLote));
    }

}
