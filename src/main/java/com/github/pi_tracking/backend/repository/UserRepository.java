package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByBadgeId(String badgeId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByBadgeId(String badgeId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
