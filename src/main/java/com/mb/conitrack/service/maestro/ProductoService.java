package com.mb.conitrack.service.maestro;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.repository.maestro.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> getProductosExternos() {
        return productoRepository.findProductosExternos();
    }

    public List<Producto> getProductosInternos() {
        return productoRepository.findProductosInternos();
    }

    public List<Producto> findByActivoTrueOrderByCodigoProductoAsc() {
        return productoRepository.findByActivoTrueOrderByCodigoProductoAsc();
    }

    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

}
