package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.repository.CamerasRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CamerasService {
    private final CamerasRepository camerasRepository;

    public CamerasService(CamerasRepository camerasRepository) {
        this.camerasRepository = camerasRepository;
    }

    public Camera create(Camera camera) {
        if (camerasRepository.existsByName(camera.getName())) {
            throw new IllegalArgumentException("A camera with that name already exists!");
        }

        return camerasRepository.save(camera);
    }

    public List<Camera> getAllCameras() {
        return camerasRepository.findAll();
    }

    public Camera getCameraById(UUID id) {
         return camerasRepository.findById(id).orElse(null);
    }

    public void toggleActive(UUID id) {
        Camera camera = camerasRepository.findById(id).orElseThrow();
        camera.setActive(!camera.isActive());
        camerasRepository.save(camera);
    }
}
