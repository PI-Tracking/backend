package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.dto.CameraDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.repository.CamerasRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CamerasService {
    private final CamerasRepository camerasRepository;

    public CamerasService(CamerasRepository camerasRepository) {
        this.camerasRepository = camerasRepository;
    }

    public Camera create(CameraDTO camera) {
        if (camerasRepository.existsByName(camera.getName())) {
            throw new IllegalArgumentException("A camera with that name already exists!");
        }

        Camera cam = Camera.builder()
                .name(camera.getName())
                .latitude(camera.getLatitude())
                .longitude(camera.getLongitude())
                .build();

        return camerasRepository.save(cam);
    }

    public Camera update(UUID cameraId, CameraDTO camera) {
        Optional<Camera> optionalCamera = camerasRepository.findById(cameraId);

        if (optionalCamera.isEmpty()) {
            throw new IllegalArgumentException("Invalid camera id!");
        }

        Camera cam = optionalCamera.get();

        if (!cam.getName().equals(camera.getName()) && camerasRepository.existsByName(camera.getName())) {
            throw new IllegalArgumentException("A camera with that name already exists!");
        }

        cam.setName(camera.getName());
        cam.setLatitude(camera.getLatitude());
        cam.setLongitude(camera.getLongitude());

        return camerasRepository.save(cam);
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
