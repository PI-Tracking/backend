package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.CameraDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.service.CamerasService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cameras")
public class CamerasController {
    private final CamerasService camerasService;

    public CamerasController(CamerasService camerasService) {
        this.camerasService = camerasService;
    }

    @Operation(summary = "Creates a new camera")
    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin()")
    @PostMapping
    public ResponseEntity<Camera> createCamera(@RequestBody @Valid CameraDTO camera) {
        return new ResponseEntity<>(camerasService.create(camera), HttpStatus.CREATED);
    }

    @Operation(summary = "Edits a camera name or location")
    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin()")
    @PutMapping("/{id}")
    public ResponseEntity<Camera> updateCamera(@PathVariable UUID id, @RequestBody @Valid CameraDTO camera) {
        return new ResponseEntity<>(camerasService.update(id, camera), HttpStatus.OK);
    }

    @Operation(summary = "Lists all the cameras")
    @GetMapping
    public ResponseEntity<List<Camera>> allCameras() {
        return ResponseEntity.ok(camerasService.getAllCameras());
    }

    @Operation(summary = "Get information about a specific camera, by its identifier")
    @GetMapping("/{id}")
    public ResponseEntity<Camera> getCameraById(@PathVariable UUID id) {
        Camera camera = camerasService.getCameraById(id);

        if (camera == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(camera);
    }

    @Operation(summary = "Turns a camera active or inactive")
    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin()")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable UUID id) {
        Camera camera = camerasService.getCameraById(id);

        if (camera == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        camerasService.toggleActive(id);

        return ResponseEntity.noContent().build();
    }
}
