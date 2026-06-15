package com.gregory.vehicleRentalAPI.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // Spring Data genera el SQL automáticamente por el nombre del metodo
    // SELECT * FROM usuarios WHERE email = ?
}
