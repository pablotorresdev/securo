package com.mb.conitrack.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.DetalleMovimientoRepository;

@Service
public class BultoService {

    @Autowired
    private BultoRepository bultoRepository;

    @Autowired
    private DetalleMovimientoRepository detalleMovimientoRepository;

    public List<Bulto> save(final List<Bulto> bultos) {
        return bultoRepository.saveAll(bultos);
    }

    public Bulto save(final Bulto bulto) {
        return bultoRepository.save(bulto);
    }

    public List<Bulto> findAll() {
        List<Bulto> bultos = bultoRepository.findAll();
        bultos.sort(Comparator.comparing(Bulto::getId));
        return bultos;
    }

    public List<Bulto> findByLoteId(final Long loteId) {
        return bultoRepository.findAllByLoteIdAndActivoTrueOrderByNroBultoAsc(loteId);
    }

    public DetalleMovimiento save(final DetalleMovimiento detalle) {
        return detalleMovimientoRepository.save(detalle);
    }
}
