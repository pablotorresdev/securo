package com.mb.conitrack.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.repository.TrazaRepository;

import jakarta.transaction.Transactional;

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

    //***********CU10 ALTA: Produccion***********
    public List<Traza> save(final List<Traza> trazas) {
        return trazaRepository.saveAll(trazas);
    }

    public List<Traza> findAll() {
        List<Traza> trazas = trazaRepository.findAll();
        trazas.sort(Comparator.comparing(Traza::getNroTraza));
        return trazas;
    }

    public List<Traza> findByLoteId(final Long loteId) {
        return trazaRepository.findByLoteIdOrderByNroTrazaAsc(loteId);
    }

}
