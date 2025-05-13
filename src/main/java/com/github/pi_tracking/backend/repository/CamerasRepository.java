package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface CamerasRepository extends JpaRepository<Camera, UUID> {
    boolean existsByName(String name);
}
