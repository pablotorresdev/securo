package com.mb.conitrack.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.EstadoEnum;
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
    @Transactional
    public List<Traza> persistirTrazasIngresoProduccion(final Lote bultoGuardado, Long trazaInicial) {
        long nroTraza = trazaInicial;
        long cantidad = bultoGuardado.getCantidadInicial().longValueExact();
        List<Traza> trazas = new ArrayList<>();
        Producto producto = bultoGuardado.getProducto();
        for (int j = 0; j < cantidad; j++) {
            Traza traza = new Traza();
            traza.setFechaYHoraCreacion(bultoGuardado.getFechaYHoraCreacion());
            traza.setProducto(producto);
            traza.setLote(bultoGuardado);
            traza.setNroTraza(nroTraza);
            traza.setEstado(EstadoEnum.DISPONIBLE);
            //TODO: comentarios?
            traza.setObservaciones("CU7 Traza: " +
                nroTraza +
                "\n - Producto: " +
                producto.getCodigoInterno() +
                " / " +
                producto.getNombreGenerico());
            traza.setActivo(true);

            trazas.add(traza);
            nroTraza++;
        }

        if (nroTraza - trazaInicial != bultoGuardado.getCantidadInicial().longValueExact()) {
            throw new IllegalStateException("Error al crear las trazas para el bulto: " + bultoGuardado.getNroBulto());
        }
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
