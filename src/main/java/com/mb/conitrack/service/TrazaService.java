package com.mb.conitrack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.repository.TrazaRepository;

@Service
public class TrazaService {

    @Autowired
    private TrazaRepository trazaRepository;

    public Long findMaxNroTraza(Long productoId) {
        return trazaRepository.findMaxNroTraza(productoId);
    }

    public List<Traza> findAllByOrderByNroTrazaAsc() {
        return trazaRepository.findAllByOrderByNroTrazaAsc();
    }

    public List<Traza> findByLoteId(final Long loteId) {
        return trazaRepository.findByLoteIdOrderByNroTrazaAsc(loteId);
    }

}
