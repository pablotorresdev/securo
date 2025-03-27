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

    /**
     * Devuelve los contactos activos que son externos a la empresa (no son Conifarma)
     * @return Lista de contactos
     */
    public List<Contacto> getContactosExternos() {
        return contactoRepository.findByActivoTrueAndRazonSocialNotIgnoreCase("conifarma");
    }

    /**
     * Devuelve los contactos activos que son Conifarma
     * @return Lista de contactos
     */
    public List<Contacto> getConifarma() {
        return contactoRepository.findByRazonSocialIgnoreCaseContaining("conifarma");
    }

    /**
     * Devuelve los contactos activos e inactivos que son externos a la empresa (no son Conifarma)
     * @return Lista de contactos
     */
    public List<Contacto> listContactosExternos() {
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
