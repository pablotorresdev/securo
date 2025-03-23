package com.mb.securo.repository.maestro;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.securo.entity.maestro.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

}
