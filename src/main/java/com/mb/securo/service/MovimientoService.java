package com.mb.securo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mb.securo.entity.Movimiento;
import com.mb.securo.repository.MovimientoRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;

    public List<Movimiento> findAll() {
        return movimientoRepository.findAll();
    }

}
