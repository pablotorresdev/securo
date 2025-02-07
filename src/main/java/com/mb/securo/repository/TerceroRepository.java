package com.mb.securo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.Producto;
import com.mb.securo.entity.Tercero;

public interface TerceroRepository extends JpaRepository<Tercero, Long> {

}
