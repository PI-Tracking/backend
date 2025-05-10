package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.dto.CameraDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.repository.CamerasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamerasServiceTest {

    @Mock
    private CamerasRepository camerasRepository;

    private CamerasService camerasService;

    @BeforeEach
    void setUp() {
        camerasService = new CamerasService(camerasRepository);
    }

    @Test
    void create_WithValidData_ShouldCreateCamera() {
        CameraDTO dto = new CameraDTO("Test Camera", 10.0, 20.0);

        Camera expectedCamera = Camera.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .active(true)
                .build();

        when(camerasRepository.existsByName(dto.getName())).thenReturn(false);
        when(camerasRepository.save(any(Camera.class))).thenReturn(expectedCamera);

        Camera result = camerasService.create(dto);

        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getLatitude(), result.getLatitude());
        assertEquals(dto.getLongitude(), result.getLongitude());
        verify(camerasRepository).save(any(Camera.class));
    }

    @Test
    void create_WithExistingName_ShouldThrowException() {
        CameraDTO dto = new CameraDTO("Existing Camera", 10.0, 20.0);

        when(camerasRepository.existsByName(dto.getName())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> camerasService.create(dto));
    }

    @Test
    void update_WithValidData_ShouldUpdateCamera() {
        UUID cameraId = UUID.randomUUID();
        CameraDTO dto = new CameraDTO("Updated Camera", 30.0, 40.0);

        Camera existingCamera = Camera.builder()
                .id(cameraId)
                .name("Old Camera")
                .latitude(10.0)
                .longitude(20.0)
                .active(true)
                .build();

        when(camerasRepository.findById(cameraId)).thenReturn(Optional.of(existingCamera));
        when(camerasRepository.existsByName(dto.getName())).thenReturn(false);
        when(camerasRepository.save(any(Camera.class))).thenReturn(existingCamera);

        Camera result = camerasService.update(cameraId, dto);

        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getLatitude(), result.getLatitude());
        assertEquals(dto.getLongitude(), result.getLongitude());
        verify(camerasRepository).save(any(Camera.class));
    }

    @Test
    void getAllCameras_ShouldReturnAllCameras() {
        List<Camera> expectedCameras = Arrays.asList(
            Camera.builder().id(UUID.randomUUID()).name("Camera 1").build(),
            Camera.builder().id(UUID.randomUUID()).name("Camera 2").build()
        );
        when(camerasRepository.findAll()).thenReturn(expectedCameras);

        List<Camera> result = camerasService.getAllCameras();

        assertEquals(expectedCameras.size(), result.size());
        verify(camerasRepository).findAll();
    }

    @Test
    void toggleActive_ShouldToggleCameraStatus() {
        UUID cameraId = UUID.randomUUID();
        Camera camera = Camera.builder()
                .id(cameraId)
                .name("Test Camera")
                .active(true)
                .build();

        when(camerasRepository.findById(cameraId)).thenReturn(Optional.of(camera));
        when(camerasRepository.save(any(Camera.class))).thenReturn(camera);

        camerasService.toggleActive(cameraId);

        assertFalse(camera.isActive());
        verify(camerasRepository).save(camera);
    }
} 