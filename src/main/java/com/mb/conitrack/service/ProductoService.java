package com.mb.conitrack.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.repository.maestro.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    //TipoProductoEnum != SEMIELABORADO && TipoProductoEnum != UNIDAD_VENTA
    public List<Producto> listAllProductosExternosActive() {
        return productoRepository.findAll()
            .stream()
            .filter(Producto::getActivo)
            .filter(p -> p.getTipoProducto() != TipoProductoEnum.SEMIELABORADO &&
                p.getTipoProducto() != TipoProductoEnum.UNIDAD_VENTA)
            .collect(Collectors.toList());
    }

    //TipoProductoEnum == SEMIELABORADO || TipoProductoEnum == UNIDAD_VENTA
    public List<Producto> getProductosDestinoActive() {
        return productoRepository.findAll()
            .stream()
            .filter(Producto::getActivo)
            .filter(p -> p.getTipoProducto() == TipoProductoEnum.SEMIELABORADO ||
                p.getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA)
            .collect(Collectors.toList());
    }

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public List<Producto> findAllActive() {
        return productoRepository.findByActivoTrue();
    }

    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }


}
