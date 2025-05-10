// package com.github.pi_tracking.backend.repository;

// import com.github.pi_tracking.backend.entity.Camera;
// import com.github.pi_tracking.backend.entity.Report;
// import com.github.pi_tracking.backend.entity.Upload;
// import com.github.pi_tracking.backend.entity.User;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest
// class UploadRepositoryTest {

//     @Autowired
//     private TestEntityManager entityManager;

//     @Autowired
//     private UploadRepository uploadRepository;

//     @Test
//     void save_WithValidUpload_ShouldPersistUpload() {
//         // Arrange
//         User creator = User.builder()
//                 .badgeId("123")
//                 .username("testuser")
//                 .email("test@example.com")
//                 .password("password")
//                 .build();
//         entityManager.persist(creator);
//         entityManager.flush();

//         Report report = Report.builder()
//                 .name("Test Report")
//                 .creator(creator)
//                 .build();
//         entityManager.persist(report);
//         entityManager.flush();

//         Camera camera = Camera.builder()
//                 .name("Test Camera")
//                 .latitude(10.0)
//                 .longitude(20.0)
//                 .build();
//         entityManager.persist(camera);
//         entityManager.flush();

//         Upload upload = Upload.builder()
//                 .report(report)
//                 .camera(camera)
//                 .build();

//         // Act
//         Upload saved = uploadRepository.save(upload);

//         // Assert
//         assertNotNull(saved.getId());
//         assertEquals(report.getId(), saved.getReport().getId());
//         assertEquals(camera.getId(), saved.getCamera().getId());
//     }

//     @Test
//     void findById_WithExistingUpload_ShouldReturnUpload() {
//         // Arrange
//         User creator = User.builder()
//                 .badgeId("123")
//                 .username("testuser")
//                 .email("test@example.com")
//                 .password("password")
//                 .build();
//         entityManager.persist(creator);
//         entityManager.flush();

//         Report report = Report.builder()
//                 .name("Test Report")
//                 .creator(creator)
//                 .build();
//         entityManager.persist(report);
//         entityManager.flush();

//         Camera camera = Camera.builder()
//                 .name("Test Camera")
//                 .latitude(10.0)
//                 .longitude(20.0)
//                 .build();
//         entityManager.persist(camera);
//         entityManager.flush();

//         Upload upload = Upload.builder()
//                 .report(report)
//                 .camera(camera)
//                 .build();
//         entityManager.persist(upload);
//         entityManager.flush();

//         // Act
//         Upload found = uploadRepository.findById(upload.getId()).orElse(null);

//         // Assert
//         assertNotNull(found);
//         assertEquals(upload.getId(), found.getId());
//         assertEquals(report.getId(), found.getReport().getId());
//         assertEquals(camera.getId(), found.getCamera().getId());
//     }

//     @Test
//     void findById_WithNonExistingUpload_ShouldReturnEmpty() {
//         // Act
//         var found = uploadRepository.findById(UUID.randomUUID());

//         // Assert
//         assertTrue(found.isEmpty());
//     }

//     @Test
//     void delete_WithExistingUpload_ShouldRemoveUpload() {
//         // Arrange
//         User creator = User.builder()
//                 .badgeId("123")
//                 .username("testuser")
//                 .email("test@example.com")
//                 .password("password")
//                 .build();
//         entityManager.persist(creator);
//         entityManager.flush();

//         Report report = Report.builder()
//                 .name("Test Report")
//                 .creator(creator)
//                 .build();
//         entityManager.persist(report);
//         entityManager.flush();

//         Camera camera = Camera.builder()
//                 .name("Test Camera")
//                 .latitude(10.0)
//                 .longitude(20.0)
//                 .build();
//         entityManager.persist(camera);
//         entityManager.flush();

//         Upload upload = Upload.builder()
//                 .report(report)
//                 .camera(camera)
//                 .build();
//         entityManager.persist(upload);
//         entityManager.flush();

//         // Act
//         uploadRepository.delete(upload);
//         var found = uploadRepository.findById(upload.getId());

//         // Assert
//         assertTrue(found.isEmpty());
//     }
// } 