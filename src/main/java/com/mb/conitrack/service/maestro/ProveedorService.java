package com.mb.conitrack.service.maestro;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.repository.maestro.ProveedorRepository;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> getProveedoresExternos() {
        return proveedorRepository.findProveedoresExternosOrderByRazonSocialAsc();
    }

    public Proveedor getConifarma() {
        return proveedorRepository.findConifarma().orElseThrow(
            () -> new IllegalArgumentException("No se encontró el proveedor Conifarma"));
    }

    public List<Proveedor> findAllByOrderByRazonSocialAsc() {
        return proveedorRepository.findAllByOrderByRazonSocialAsc();
    }

    public Optional<Proveedor> findById(Long id) {
        return proveedorRepository.findById(id);
    }

    public Proveedor save(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

}
