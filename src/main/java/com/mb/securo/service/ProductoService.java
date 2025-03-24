package com.mb.securo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.securo.entity.maestro.Producto;
import com.mb.securo.enums.TipoProductoEnum;
import com.mb.securo.repository.maestro.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    //TipoProductoEnum != SEMIELABORADO && TipoProductoEnum != UNIDAD_VENTA
    public List<Producto> listAllProductosExternos() {
        return productoRepository.findAll()
            .stream()
            .filter(p -> p.getTipoProducto() != TipoProductoEnum.SEMIELABORADO &&
                p.getTipoProducto() != TipoProductoEnum.UNIDAD_VENTA)
            .collect(Collectors.toList());
    }

    //TipoProductoEnum == SEMIELABORADO || TipoProductoEnum == UNIDAD_VENTA
    public List<Producto> listAllProductosPropios() {
        return productoRepository.findAll()
            .stream()
            .filter(p -> p.getTipoProducto() == TipoProductoEnum.SEMIELABORADO ||
                p.getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA)
            .collect(Collectors.toList());
    }

}
