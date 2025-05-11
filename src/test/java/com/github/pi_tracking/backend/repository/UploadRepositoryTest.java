package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.config.TestContainersConfig;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.entity.Report;
import com.github.pi_tracking.backend.entity.Upload;
import com.github.pi_tracking.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
class UploadRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UploadRepository uploadRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CamerasRepository camerasRepository;

    @Test
    void save_WithValidUpload_ShouldPersistUpload() {
        // Arrange
        User creator = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        creator = userRepository.save(creator);

        Report report = Report.builder()
                .name("Test Report")
                .creator(creator)
                .build();
        reportRepository.save(report);

        Camera camera = Camera.builder()
                .name("Test Camera")
                .latitude(10.0)
                .longitude(20.0)
                .build();
        camerasRepository.save(camera);

        Upload upload = Upload.builder()
                .report(report)
                .camera(camera)
                .build();

        // Act
        Upload saved = uploadRepository.save(upload);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(report.getId(), saved.getReport().getId());
        assertEquals(camera.getId(), saved.getCamera().getId());
    }

    @Test
    void findById_WithExistingUpload_ShouldReturnUpload() {
        // Arrange
        User creator = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        creator = userRepository.save(creator);

        Report report = Report.builder()
                .name("Test Report")
                .creator(creator)
                .build();
        reportRepository.save(report);

        Camera camera = Camera.builder()
                .name("Test Camera")
                .latitude(10.0)
                .longitude(20.0)
                .build();
        camerasRepository.save(camera);

        Upload upload = Upload.builder()
                .report(report)
                .camera(camera)
                .build();
        uploadRepository.save(upload);

        // Act
        Upload found = uploadRepository.findById(upload.getId()).orElse(null);

        // Assert
        assertNotNull(found);
        assertEquals(upload.getId(), found.getId());
        assertEquals(report.getId(), found.getReport().getId());
        assertEquals(camera.getId(), found.getCamera().getId());
    }

    @Test
    void findById_WithNonExistingUpload_ShouldReturnEmpty() {
        // Act
        var found = uploadRepository.findById(UUID.randomUUID());

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void delete_WithExistingUpload_ShouldRemoveUpload() {
        // Arrange
        User creator = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        creator = userRepository.save(creator);

        Report report = Report.builder()
                .name("Test Report")
                .creator(creator)
                .build();
        reportRepository.save(report);

        Camera camera = Camera.builder()
                .name("Test Camera")
                .latitude(10.0)
                .longitude(20.0)
                .build();
        camerasRepository.save(camera);

        Upload upload = Upload.builder()
                .report(report)
                .camera(camera)
                .build();
        uploadRepository.save(upload);

        // Act
        uploadRepository.delete(upload);
        var found = uploadRepository.findById(upload.getId());

        // Assert
        assertTrue(found.isEmpty());
    }
} 