package com.mb.conitrack.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.MovimientoDTO;
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
        return analisisRepository.findAll().stream()
            .filter(Analisis::getActivo)
            .sorted(Comparator.comparing(
                Analisis::getFechaAnalisis,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    public List<Analisis> findAllEnCurso() {
        return analisisRepository.findAll().stream()
            .filter(Analisis::getActivo)
            .filter(analisis -> analisis.getDictamen() == null)
            .filter(analisis -> analisis.getFechaAnalisis() == null)
            .sorted(Comparator.comparing(
                Analisis::getFechaYHoraCreacion,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    public Analisis addDictamenResultado(final MovimientoDTO dto) {
        Analisis analisis = analisisRepository.findByNroAnalisis(dto.getNroAnalisis()).orElseThrow(
            () -> new IllegalArgumentException("No se encontró el análisis con el número: " + dto.getNroAnalisis()));

        analisis.setDictamen(dto.getDictamenFinal());
        analisis.setFechaAnalisis(dto.getFechaAnalisis());
        analisis.setObservaciones(dto.getObservaciones());
        return analisisRepository.save(analisis);
    }

}
