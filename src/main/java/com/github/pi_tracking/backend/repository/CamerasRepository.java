package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CamerasRepository extends JpaRepository<Camera, UUID> {
    boolean existsByName(String name);
}
