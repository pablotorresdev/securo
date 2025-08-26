package com.mb.conitrack.repository.maestro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mb.conitrack.entity.maestro.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    @Query("""
            select p
            from Proveedor p
            where lower(p.razonSocial) like '%conifarma%'
            order by p.id asc
        """)
    Optional<Proveedor> findConifarma();

    List<Proveedor> findAllByOrderByRazonSocialAsc();

    // 2) Externos = activos y razón social NO contiene "conifarma" (case-insensitive), sin parámetro
    @Query("""
            select p
            from Proveedor p
            where p.activo = true
              and lower(p.razonSocial) not like '%conifarma%'
            order by p.razonSocial asc
        """)
    List<Proveedor> findProveedoresExternosOrderByRazonSocialAsc();

}
