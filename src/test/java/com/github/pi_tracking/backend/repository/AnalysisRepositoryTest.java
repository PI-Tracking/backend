// package com.github.pi_tracking.backend.repository;

// import com.github.pi_tracking.backend.config.TestContainersConfig;
// import com.github.pi_tracking.backend.entity.DetectionModel;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.data.mongodb.core.MongoTemplate;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.testcontainers.containers.MongoDBContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// @DataMongoTest
// @Testcontainers
// @Import(TestContainersConfig.class)
// class AnalysisRepositoryTest {

//     @Container
//     static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.2");

//     @DynamicPropertySource
//     static void configureProperties(DynamicPropertyRegistry registry) {
//         String mongoUri = String.format("mongodb://%s:%d", mongo.getHost(), mongo.getMappedPort(27017));
//         registry.add("spring.data.mongodb.uri", () -> mongoUri);
//         registry.add("spring.data.mongodb.auto-index-creation", () -> true);
//     }

//     @Autowired
//     private MongoTemplate mongoTemplate;

//     @Autowired
//     private AnalysisRepository analysisRepository;

//     @Test
//     void findByAnalysisId_WithExistingAnalysis_ShouldReturnList() {
//         // Arrange
//         DetectionModel model1 = DetectionModel.builder()
//                 .analysisId("test-analysis")
//                 .build();
//         DetectionModel model2 = DetectionModel.builder()
//                 .analysisId("test-analysis")
//                 .build();
//         mongoTemplate.save(model1);
//         mongoTemplate.save(model2);

//         // Act
//         List<DetectionModel> found = analysisRepository.findByAnalysisId("test-analysis");

//         // Assert
//         assertEquals(2, found.size());
//         assertTrue(found.stream().allMatch(m -> m.getAnalysisId().equals("test-analysis")));
//     }

//     @Test
//     void findByAnalysisId_WithNonExistingAnalysis_ShouldReturnEmptyList() {
//         // Act
//         List<DetectionModel> found = analysisRepository.findByAnalysisId("nonexistent");

//         // Assert
//         assertTrue(found.isEmpty());
//     }

//     @Test
//     void findByReportId_WithExistingReport_ShouldReturnList() {
//         // Arrange
//         DetectionModel model1 = DetectionModel.builder()
//                 .analysisId("test-analysis")
//                 .reportId("test-report")
//                 .build();
//         DetectionModel model2 = DetectionModel.builder()
//                 .analysisId("test-analysis")
//                 .reportId("test-report")
//                 .build();
//         mongoTemplate.save(model1);
//         mongoTemplate.save(model2);

//         // Act
//         List<String> found = analysisRepository.findByReportId("test-report");

//         // Assert
//         assertEquals(1, found.size());
//         assertEquals("test-analysis", found.get(0));
//     }

//     @Test
//     void findByReportId_WithNonExistingReport_ShouldReturnEmptyList() {
//         // Act
//         List<String> found = analysisRepository.findByReportId("nonexistent");

//         // Assert
//         assertTrue(found.isEmpty());
//     }

//     @Test
//     void existsByAnalysisId_WithExistingAnalysis_ShouldReturnTrue() {
//         // Arrange
//         DetectionModel model = DetectionModel.builder()
//                 .analysisId("test-analysis")
//                 .build();
//         mongoTemplate.save(model);

//         // Act
//         boolean exists = analysisRepository.existsByAnalysisId("test-analysis");

//         // Assert
//         assertTrue(exists);
//     }

//     @Test
//     void existsByAnalysisId_WithNonExistingAnalysis_ShouldReturnFalse() {
//         // Act
//         boolean exists = analysisRepository.existsByAnalysisId("nonexistent");

//         // Assert
//         assertFalse(exists);
//     }

//     @Test
//     void findByAnalysisIdAndDetectionBoxIsNotNull_WithValidData_ShouldReturnList() {
//         // Arrange
//         List<DetectionModel.Point> points = new ArrayList<>();
//         DetectionModel.Point point1 = new DetectionModel.Point();
//         point1.setX(0);
//         point1.setY(0);
//         DetectionModel.Point point2 = new DetectionModel.Point();
//         point2.setX(100);
//         point2.setY(100);
//         points.add(point1);
//         points.add(point2);

//         DetectionModel model1 = DetectionModel.builder()
//                 .analysisId("test-analysis")
//                 .detectionBox(points)
//                 .build();
//         DetectionModel model2 = DetectionModel.builder()
//                 .analysisId("test-analysis")
//                 .build();
//         mongoTemplate.save(model1);
//         mongoTemplate.save(model2);

//         // Act
//         List<DetectionModel> found = analysisRepository.findByAnalysisIdAndDetectionBoxIsNotNull("test-analysis");

//         // Assert
//         assertEquals(1, found.size());
//         assertNotNull(found.get(0).getDetectionBox());
//     }

//     @Test
//     void findByAnalysisIdAndSegmentationMaskIsNotNull_WithValidData_ShouldReturnList() {
//         // Arrange
//         List<List<Double>> mask = new ArrayList<>();
//         mask.add(Arrays.asList(0.0, 0.0));
//         mask.add(Arrays.asList(100.0, 100.0));

//         DetectionModel model1 = DetectionModel.builder()
//                 .analysisId("test-analysis")
//                 .segmentationMask(mask)
//                 .build();
//         DetectionModel model2 = DetectionModel.builder()
//                 .analysisId("test-analysis")
//                 .build();
//         mongoTemplate.save(model1);
//         mongoTemplate.save(model2);

//         // Act
//         List<DetectionModel> found = analysisRepository.findByAnalysisIdAndSegmentationMaskIsNotNull("test-analysis");

//         // Assert
//         assertEquals(1, found.size());
//         assertNotNull(found.get(0).getSegmentationMask());
//     }
// } 