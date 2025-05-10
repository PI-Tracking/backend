package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.config.TestContainersConfig;
import com.github.pi_tracking.backend.entity.Report;
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
class ReportRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_WithValidReport_ShouldPersistReport() {
        // Arrange
        User creator = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(creator);

        Report report = Report.builder()
                .name("Test Report")
                .creator(creator)
                .build();

        // Act
        Report saved = reportRepository.save(report);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(report.getName(), saved.getName());
        assertEquals(creator.getBadgeId(), saved.getCreator().getBadgeId());
    }

    @Test
    void findById_WithExistingReport_ShouldReturnReport() {
        // Arrange
        User creator = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(creator);

        Report report = Report.builder()
                .name("Test Report")
                .creator(creator)
                .build();
        reportRepository.save(report);

        // Act
        Report found = reportRepository.findById(report.getId()).orElse(null);

        // Assert
        assertNotNull(found);
        assertEquals(report.getId(), found.getId());
        assertEquals(report.getName(), found.getName());
        assertEquals(creator.getBadgeId(), found.getCreator().getBadgeId());
    }

    @Test
    void findById_WithNonExistingReport_ShouldReturnEmpty() {
        // Act
        var found = reportRepository.findById(UUID.randomUUID());

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void delete_WithExistingReport_ShouldRemoveReport() {
        // Arrange
        User creator = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(creator);

        Report report = Report.builder()
                .name("Test Report")
                .creator(creator)
                .build();
        reportRepository.save(report);

        // Act
        reportRepository.delete(report);
        var found = reportRepository.findById(report.getId());

        // Assert
        assertTrue(found.isEmpty());
    }
} 