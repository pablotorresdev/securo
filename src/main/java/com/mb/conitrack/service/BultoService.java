package com.mb.conitrack.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.DetalleMovimientoRepository;

@Service
public class BultoService {

    @Autowired
    private BultoRepository bultoRepository;

    public List<Bulto> findAllByOrderByIdAsc() {
        return bultoRepository.findAllByOrderByIdAsc();
    }

    public List<Bulto> findByLoteId(final Long loteId) {
        return bultoRepository.findAllByLoteIdAndActivoTrueOrderByNroBultoAsc(loteId);
    }

    public List<Bulto> findActivosByLoteCodigoLote(final String codigoLote) {
        return bultoRepository.findActivosByLoteCodigoLote(codigoLote);
    }

    public Optional<Bulto> findActivosByLoteCodigoLoteAndNroBulto(final String codigoLote, final Integer nroBulto) {
        return bultoRepository.findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue(codigoLote,nroBulto);
    }

}
