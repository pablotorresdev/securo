package com.mb.conitrack.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.repository.maestro.ProveedorRepository;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Devuelve los proveedores activos que son externos a la empresa (no son Conifarma)
     * @return Lista de proveedores
     */
    public List<Proveedor> getProveedoresExternos() {
        final List<Proveedor> proveedoresExternos = proveedorRepository.findByActivoTrueAndRazonSocialNotIgnoreCase("conifarma");
        proveedoresExternos.sort(Comparator
            .comparing(Proveedor::getRazonSocial));
        return proveedoresExternos;
    }

    /**
     * Devuelve los proveedores activos que son Conifarma
     * @return Lista de proveedores
     */
    public List<Proveedor> getConifarma() {
        return proveedorRepository.findByRazonSocialIgnoreCaseContaining("conifarma");
    }

    /**
     * Devuelve los proveedores activos e inactivos que son externos a la empresa (no son Conifarma)
     * @return Lista de proveedores
     */
    public List<Proveedor> listProveedoresExternos() {
        final List<Proveedor> proveedoresExternos = proveedorRepository.findByRazonSocialNotIgnoreCaseContaining("conifarma");
        proveedoresExternos.sort(Comparator
            .comparing(Proveedor::getRazonSocial));
        return proveedoresExternos;
    }

    public List<Proveedor> findAll() {
        final List<Proveedor> proveedores = proveedorRepository.findAll();
        proveedores.sort(Comparator
            .comparing(Proveedor::getRazonSocial));
        return proveedores;
    }

    public Proveedor save(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    public Optional<Proveedor> findById(Long id) {
        return proveedorRepository.findById(id);
    }

}
