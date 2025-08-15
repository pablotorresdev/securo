package com.mb.conitrack.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.repository.AnalisisRepository;

import static com.mb.conitrack.enums.DictamenEnum.CUARENTENA;

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

    public List<Analisis> findAllEnCursoForLotesCuarentena() {
        return analisisRepository.findAll().stream()
            .filter(Analisis::getActivo)
            .filter(analisis -> analisis.getDictamen() == null)
            .filter(analisis -> analisis.getFechaRealizado() == null)
            .filter(analisis -> analisis.getLote().getDictamen() == CUARENTENA)
            .sorted(Comparator.comparing(
                Analisis::getFechaYHoraCreacion,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    public Analisis addResultadoAnalisis(final MovimientoDTO dto) {
        Analisis analisis = findByNroAnalisis(dto.getNroAnalisis());
        if (analisis == null) {
            analisis = DTOUtils.createAnalisis(dto);
        }

        analisis.setFechaRealizado(dto.getFechaRealizadoAnalisis());
        analisis.setDictamen(dto.getDictamenFinal());
        if (dto.getDictamenFinal() == DictamenEnum.APROBADO) {
            analisis.setFechaReanalisis(dto.getFechaReanalisis());
            analisis.setFechaVencimiento(dto.getFechaVencimiento());
            analisis.setTitulo(dto.getTitulo());
        }
        analisis.setObservaciones(dto.getObservaciones());
        return analisisRepository.save(analisis);
    }

    public Analisis findByNroAnalisis(final String nroAnalisis) {
        return analisisRepository.findByNroAnalisisAndActivoTrue(nroAnalisis);
    }

    public Analisis findByNroAnalisisAndDictamenNotNull(final String nroAnalisis) {
        return analisisRepository.findByNroAnalisisAndDictamenIsNotNullAndActivoTrue(nroAnalisis);
    }

}
