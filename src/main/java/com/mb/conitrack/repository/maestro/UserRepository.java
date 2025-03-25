package com.mb.conitrack.repository.maestro;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.conitrack.entity.maestro.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

}
