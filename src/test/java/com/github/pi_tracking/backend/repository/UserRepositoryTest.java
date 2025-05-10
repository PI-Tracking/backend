package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.config.TestContainersConfig;
import com.github.pi_tracking.backend.entity.User;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByBadgeId_WithExistingUser_ShouldReturnUser() {
        // Arrange
        User user = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByBadgeId(user.getBadgeId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(user.getBadgeId(), found.get().getBadgeId());
    }

    @Test
    void findByBadgeId_WithNonExistingUser_ShouldReturnEmpty() {
        // Act
        Optional<User> found = userRepository.findByBadgeId("nonexistent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void findByUsername_WithExistingUser_ShouldReturnUser() {
        // Arrange
        User user = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByUsername(user.getUsername());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(user.getUsername(), found.get().getUsername());
    }

    @Test
    void findByUsername_WithNonExistingUser_ShouldReturnEmpty() {
        // Act
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_WithExistingUser_ShouldReturnUser() {
        // Arrange
        User user = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByEmail(user.getEmail());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(user.getEmail(), found.get().getEmail());
    }

    @Test
    void findByEmail_WithNonExistingUser_ShouldReturnEmpty() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void existsByBadgeId_WithExistingUser_ShouldReturnTrue() {
        // Arrange
        User user = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByBadgeId(user.getBadgeId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByBadgeId_WithNonExistingUser_ShouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByBadgeId("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByUsername_WithExistingUser_ShouldReturnTrue() {
        // Arrange
        User user = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByUsername(user.getUsername());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByUsername_WithNonExistingUser_ShouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByEmail_WithExistingUser_ShouldReturnTrue() {
        // Arrange
        User user = User.builder()
                .badgeId("123")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByEmail(user.getEmail());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_WithNonExistingUser_ShouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }
} 