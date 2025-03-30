package com.mb.conitrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.repository.AnalisisRepository;

@Service
public class AnalisisService {

    @Autowired
    private AnalisisRepository analisisRepository;

    public void save(Analisis analisis) {
        analisisRepository.save(analisis);
    }

}
