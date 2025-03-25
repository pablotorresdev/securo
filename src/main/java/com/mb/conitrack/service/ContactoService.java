package com.mb.conitrack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.maestro.Contacto;
import com.mb.conitrack.repository.maestro.ContactoRepository;

@Service
public class ContactoService {

    @Autowired
    private ContactoRepository contactoRepository;

    public List<Contacto> listAllContactos() {
        return contactoRepository.findAll();
    }

    public List<Contacto> listConifarma() {
        return contactoRepository.findByRazonSocialIgnoreCaseContaining("conifarma");
    }

    public List<Contacto> listAllContactosExternos() {
        return contactoRepository.findByRazonSocialNotIgnoreCaseContaining("conifarma");
    }
}
