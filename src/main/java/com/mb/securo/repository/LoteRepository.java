package com.mb.securo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.Estado;
import com.mb.securo.entity.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {

}
