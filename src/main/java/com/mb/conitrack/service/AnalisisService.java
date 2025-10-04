package com.mb.conitrack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.repository.AnalisisRepository;

import static com.mb.conitrack.dto.DTOUtils.fromAnalisisEntities;

@Service
public class AnalisisService {

    @Autowired
    private AnalisisRepository analisisRepository;

    @Transactional(readOnly = true)
    public List<AnalisisDTO> findAllAnalisis() {
        return fromAnalisisEntities(analisisRepository.findAllByActivoTrue());
    }

    @Transactional(readOnly = true)
    public List<AnalisisDTO> findAllEnCursoDTOs() {
        return fromAnalisisEntities(analisisRepository.findAllEnCurso());
    }

    // CU5: Resultado QA Aprobado
    // CU6: Resultado QA Rechazado
    @Transactional(readOnly = true)
    public List<AnalisisDTO> findAllEnCursoForLotesCuarentenaDTOs() {
        return fromAnalisisEntities(analisisRepository.findAllEnCursoForLotesCuarentena());
    }

    // CU5: Resultado QA Aprobado
    // CU6: Resultado QA Rechazado
    @Transactional(readOnly = true)
    public Analisis findByNroAnalisis(final String nroAnalisis) {
        return analisisRepository.findByNroAnalisisAndActivoTrue(nroAnalisis);
    }

    @Transactional(readOnly = true)
    public List<AnalisisDTO> findByCodigoLote(final String codigoLote) {
        return fromAnalisisEntities(analisisRepository.findByCodigoLote(codigoLote));
    }

}
