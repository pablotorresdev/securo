package com.mb.conitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mb.conitrack.entity.Analisis;

public interface AnalisisRepository extends JpaRepository<Analisis, Long> {


}
