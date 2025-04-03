package com.mb.conitrack.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.repository.AnalisisRepository;

@Service
public class AnalisisService {

    @Autowired
    private AnalisisRepository analisisRepository;

    public Analisis save(Analisis analisis) {
        return analisisRepository.save(analisis);
    }

    public List<Analisis> findAll() {
        List<Analisis> analisis = analisisRepository.findAll();
        analisis.sort(Comparator
            .comparing(Analisis::getFechaAnalisis,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return analisis;
    }

}
