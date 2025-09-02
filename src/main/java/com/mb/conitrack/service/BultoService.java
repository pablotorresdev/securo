package com.mb.conitrack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.repository.BultoRepository;

import static com.mb.conitrack.dto.DTOUtils.fromBultoEntities;

@Service
public class BultoService {

    @Autowired
    private BultoRepository bultoRepository;

    @Transactional(readOnly = true)
    public List<BultoDTO> findByCodigoLote(final String codigoLote) {
        return fromBultoEntities(bultoRepository.findAllByLoteCodigoLoteOrderByNroBultoAsc(codigoLote));
    }

    @Transactional(readOnly = true)
    public List<BultoDTO> findAllBultos() {
        return fromBultoEntities(bultoRepository.findAllByActivoTrue());
    }

}
