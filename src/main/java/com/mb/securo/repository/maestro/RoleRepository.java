package com.mb.securo.repository.maestro;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.maestro.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

}
