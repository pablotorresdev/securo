package com.mb.conitrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.repository.TrazaRepository;

@Service
public class TrazaService {

    @Autowired
    private TrazaRepository trazaRepository;

    public Long findMaxNroTraza(Long productoId) {
        return trazaRepository
            .findTopByProductoIdOrderByNroTrazaDesc(productoId)
            .map(Traza::getNroTraza)
            .orElse(-1L);
    }

}
