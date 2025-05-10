// package com.github.pi_tracking.backend.repository;

// import com.github.pi_tracking.backend.entity.Camera;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest
// class CamerasRepositoryTest {

//     @Autowired
//     private TestEntityManager entityManager;

//     @Autowired
//     private CamerasRepository camerasRepository;

//     @Test
//     void existsByName_WithExistingCamera_ShouldReturnTrue() {
//         // Arrange
//         Camera camera = Camera.builder()
//                 .name("Test Camera")
//                 .latitude(10.0)
//                 .longitude(20.0)
//                 .build();
//         entityManager.persist(camera);
//         entityManager.flush();

//         // Act
//         boolean exists = camerasRepository.existsByName(camera.getName());

//         // Assert
//         assertTrue(exists);
//     }

//     @Test
//     void existsByName_WithNonExistingCamera_ShouldReturnFalse() {
//         // Act
//         boolean exists = camerasRepository.existsByName("nonexistent");

//         // Assert
//         assertFalse(exists);
//     }

//     @Test
//     void save_WithValidCamera_ShouldPersistCamera() {
//         // Arrange
//         Camera camera = Camera.builder()
//                 .name("Test Camera")
//                 .latitude(10.0)
//                 .longitude(20.0)
//                 .build();

//         // Act
//         Camera saved = camerasRepository.save(camera);

//         // Assert
//         assertNotNull(saved.getId());
//         assertEquals(camera.getName(), saved.getName());
//         assertEquals(camera.getLatitude(), saved.getLatitude());
//         assertEquals(camera.getLongitude(), saved.getLongitude());
//     }

//     @Test
//     void findById_WithExistingCamera_ShouldReturnCamera() {
//         // Arrange
//         Camera camera = Camera.builder()
//                 .name("Test Camera")
//                 .latitude(10.0)
//                 .longitude(20.0)
//                 .build();
//         entityManager.persist(camera);
//         entityManager.flush();

//         // Act
//         Camera found = camerasRepository.findById(camera.getId()).orElse(null);

//         // Assert
//         assertNotNull(found);
//         assertEquals(camera.getId(), found.getId());
//         assertEquals(camera.getName(), found.getName());
//     }

//     @Test
//     void findById_WithNonExistingCamera_ShouldReturnEmpty() {
//         // Act
//         var found = camerasRepository.findById(UUID.randomUUID());

//         // Assert
//         assertTrue(found.isEmpty());
//     }
// } 