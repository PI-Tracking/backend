package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCc(String cc);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByCc(String cc);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
