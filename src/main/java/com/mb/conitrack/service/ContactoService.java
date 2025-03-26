package com.mb.conitrack.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.maestro.Contacto;
import com.mb.conitrack.repository.maestro.ContactoRepository;

@Service
public class ContactoService {

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private LoteService loteService;

    public List<Contacto> listAllContactos() {
        return contactoRepository.findAll();
    }

    public List<Contacto> listAllContactosExternosActive() {
        return contactoRepository.findByActivoTrueAndRazonSocialNotIgnoreCase("conifarma");
    }

    public List<Contacto> listConifarma() {
        return contactoRepository.findByRazonSocialIgnoreCaseContaining("conifarma");
    }

    public List<Contacto> listAllContactosExternos() {
        return contactoRepository.findByRazonSocialNotIgnoreCaseContaining("conifarma");
    }


    public List<Contacto> findAll() {
         return contactoRepository.findAll();
    }

    public Contacto save(Contacto contacto) {
        return contactoRepository.save(contacto);
    }

    public Optional<Contacto> findById(Long id) {
        return contactoRepository.findById(id);
    }

}
