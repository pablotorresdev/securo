package com.mb.conitrack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.repository.AnalisisRepository;

import static com.mb.conitrack.dto.DTOUtils.fromAnalisisEntities;

@Service
public class AnalisisService {

    @Autowired
    private AnalisisRepository analisisRepository;

    @Transactional(readOnly = true)
    public List<AnalisisDTO> findAllBultoAudit() {
        return fromAnalisisEntities(analisisRepository.findAllAudit());
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


    public List<Analisis> findAllEnCursoForLotesCuarentena() {
        return analisisRepository.findAllEnCursoForLotesCuarentena();
    }


    @Transactional(readOnly = true)
    public List<AnalisisDTO> findAllByCodigoLoteDTOs() {
        return fromAnalisisEntities(analisisRepository.findAllEnCursoForLotesCuarentena());
    }


    public Analisis findByNroAnalisisAndDictamenNotNull(final String nroAnalisis) {
        return analisisRepository.findByNroAnalisisAndDictamenIsNotNullAndActivoTrue(nroAnalisis);
    }

    public Analisis addResultadoAnalisis(final MovimientoDTO dto) {
        Analisis analisis = analisisRepository.findByNroAnalisisAndActivoTrue(dto.getNroAnalisis());
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

    @Transactional(readOnly = true)
    public List<AnalisisDTO> findByCodigoLote(final String codigoLote) {
        return fromAnalisisEntities(analisisRepository.findByCodigoLote(codigoLote));
    }

}
