package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.config.TestContainersConfig;
import com.github.pi_tracking.backend.entity.Camera;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
class CamerasRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CamerasRepository camerasRepository;

    @Test
    void existsByName_WithExistingCamera_ShouldReturnTrue() {
        // Arrange
        Camera camera = Camera.builder()
                .name("Test Camera")
                .latitude(10.0)
                .longitude(20.0)
                .build();
        camerasRepository.save(camera);

        // Act
        boolean exists = camerasRepository.existsByName(camera.getName());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByName_WithNonExistingCamera_ShouldReturnFalse() {
        // Act
        boolean exists = camerasRepository.existsByName("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_WithValidCamera_ShouldPersistCamera() {
        // Arrange
        Camera camera = Camera.builder()
                .name("Test Camera")
                .latitude(10.0)
                .longitude(20.0)
                .build();

        // Act
        Camera saved = camerasRepository.save(camera);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(camera.getName(), saved.getName());
        assertEquals(camera.getLatitude(), saved.getLatitude());
        assertEquals(camera.getLongitude(), saved.getLongitude());
    }

    @Test
    void findById_WithExistingCamera_ShouldReturnCamera() {
        // Arrange
        Camera camera = Camera.builder()
                .name("Test Camera")
                .latitude(10.0)
                .longitude(20.0)
                .build();
        camerasRepository.save(camera);

        // Act
        Camera found = camerasRepository.findById(camera.getId()).orElse(null);

        // Assert
        assertNotNull(found);
        assertEquals(camera.getId(), found.getId());
        assertEquals(camera.getName(), found.getName());
    }

    @Test
    void findById_WithNonExistingCamera_ShouldReturnEmpty() {
        // Act
        var found = camerasRepository.findById(UUID.randomUUID());

        // Assert
        assertTrue(found.isEmpty());
    }
} 