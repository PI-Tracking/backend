// package com.github.pi_tracking.backend.repository;

// import com.github.pi_tracking.backend.entity.Report;
// import com.github.pi_tracking.backend.entity.User;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest
// class ReportRepositoryTest {

//     @Autowired
//     private TestEntityManager entityManager;

//     @Autowired
//     private ReportRepository reportRepository;

//     @Test
//     void save_WithValidReport_ShouldPersistReport() {
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

//         // Act
//         Report saved = reportRepository.save(report);

//         // Assert
//         assertNotNull(saved.getId());
//         assertEquals(report.getName(), saved.getName());
//         assertEquals(creator.getBadgeId(), saved.getCreator().getBadgeId());
//     }

//     @Test
//     void findById_WithExistingReport_ShouldReturnReport() {
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

//         // Act
//         Report found = reportRepository.findById(report.getId()).orElse(null);

//         // Assert
//         assertNotNull(found);
//         assertEquals(report.getId(), found.getId());
//         assertEquals(report.getName(), found.getName());
//         assertEquals(creator.getBadgeId(), found.getCreator().getBadgeId());
//     }

//     @Test
//     void findById_WithNonExistingReport_ShouldReturnEmpty() {
//         // Act
//         var found = reportRepository.findById(UUID.randomUUID());

//         // Assert
//         assertTrue(found.isEmpty());
//     }

//     @Test
//     void delete_WithExistingReport_ShouldRemoveReport() {
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

//         // Act
//         reportRepository.delete(report);
//         var found = reportRepository.findById(report.getId());

//         // Assert
//         assertTrue(found.isEmpty());
//     }
// } 