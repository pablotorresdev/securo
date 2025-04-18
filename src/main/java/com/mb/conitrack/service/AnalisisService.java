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
                Analisis::getFechaRealizado,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    public List<Analisis> findAllEnCurso() {
        return analisisRepository.findAll().stream()
            .filter(Analisis::getActivo)
            .filter(analisis -> analisis.getDictamen() == null)
            .filter(analisis -> analisis.getFechaRealizado() == null)
            .sorted(Comparator.comparing(
                Analisis::getFechaYHoraCreacion,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    public Analisis addResultadoAnalisis(final MovimientoDTO dto) {
        Analisis analisis = analisisRepository.findByNroAnalisis(dto.getNroAnalisis()).orElseThrow(
            () -> new IllegalArgumentException("No se encontró el análisis con el número: " + dto.getNroAnalisis()));

        analisis.setFechaRealizado(dto.getFechaRealizadoAnalisis());
        analisis.setFechaReanalisis(dto.getFechaReanalisis());
        analisis.setFechaVencimiento(dto.getFechaVencimiento());
        analisis.setDictamen(dto.getDictamenFinal());
        analisis.setTitulo(dto.getTitulo());
        analisis.setObservaciones(dto.getObservaciones());
        return analisisRepository.save(analisis);
    }

    public Analisis findByNroAnalisis(final String nroAnalisis) {
        return analisisRepository.findByNroAnalisis(nroAnalisis).filter(Analisis::getActivo).orElseThrow(() -> new IllegalArgumentException("El Analisis no existe."));
    }

}
