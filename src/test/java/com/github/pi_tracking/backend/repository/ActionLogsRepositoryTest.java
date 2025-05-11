// package com.github.pi_tracking.backend.repository;

// import com.github.pi_tracking.backend.config.TestContainersConfig;
// import com.github.pi_tracking.backend.config.MongoTestConfig;
// import com.github.pi_tracking.backend.entity.Log;
// import com.github.pi_tracking.backend.utils.Actions;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Import;
// import org.springframework.data.mongodb.core.MongoTemplate;
// import com.mongodb.client.MongoClient;
// import com.mongodb.client.MongoClients;
// import org.testcontainers.containers.MongoDBContainer;
// import org.testcontainers.junit.jupiter.Container;

// import java.time.LocalDateTime;
// import java.time.ZoneOffset;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// @DataMongoTest
// @Import({TestContainersConfig.class, MongoTestConfig.class})
// class ActionLogsRepositoryTest {

//     @Container
//     static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.2");

//     @Autowired
//     private MongoTemplate mongoTemplate;

//     @Autowired
//     private ActionLogsRepository actionLogsRepository;

//     @Test
//     void findByuserBadge_WithExistingLogs_ShouldReturnList() {
//         // Arrange
//         Log log1 = Log.builder()
//                 .userBadge("123")
//                 .userName("testuser1")
//                 .action(Actions.Login)
//                 .build();
//         Log log2 = Log.builder()
//                 .userBadge("123")
//                 .userName("testuser1")
//                 .action(Actions.Logout)
//                 .build();
//         mongoTemplate.save(log1);
//         mongoTemplate.save(log2);

//         // Act
//         List<Log> found = actionLogsRepository.findByuserBadge("123");

//         // Assert
//         assertEquals(2, found.size());
//         assertTrue(found.stream().allMatch(l -> l.getUserBadge().equals("123")));
//     }

//     @Test
//     void findByuserBadge_WithNonExistingLogs_ShouldReturnEmptyList() {
//         // Act
//         List<Log> found = actionLogsRepository.findByuserBadge("nonexistent");

//         // Assert
//         assertTrue(found.isEmpty());
//     }

//     @Test
//     void findById_WithExistingLog_ShouldReturnLog() {
//         // Arrange
//         Log log = Log.builder()
//                 .userBadge("123")
//                 .userName("testuser")
//                 .action(Actions.Login)
//                 .build();
//         mongoTemplate.save(log);

//         // Act
//         Log found = actionLogsRepository.findById(log.getId());

//         // Assert
//         assertNotNull(found);
//         assertEquals(log.getId(), found.getId());
//         assertEquals(log.getUserBadge(), found.getUserBadge());
//     }

//     @Test
//     void findById_WithNonExistingLog_ShouldReturnNull() {
//         // Act
//         Log found = actionLogsRepository.findById("nonexistent");

//         // Assert
//         assertNull(found);
//     }

//     @Test
//     void findAll_ShouldReturnAllLogs() {
//         // Arrange
//         Log log1 = Log.builder()
//                 .userBadge("123")
//                 .userName("testuser1")
//                 .action(Actions.Login)
//                 .build();
//         Log log2 = Log.builder()
//                 .userBadge("456")
//                 .userName("testuser2")
//                 .action(Actions.Logout)
//                 .build();
//         mongoTemplate.save(log1);
//         mongoTemplate.save(log2);

//         // Act
//         List<Log> found = actionLogsRepository.findAll();

//         // Assert
//         assertEquals(2, found.size());
//     }

//     @Test
//     void findByTimestampGreaterThan_WithValidTimestamp_ShouldReturnList() {
//         // Arrange
//         LocalDateTime now = LocalDateTime.now();
//         long timestamp1 = now.minusMinutes(2).toEpochSecond(ZoneOffset.UTC) * 1000;
//         long timestamp2 = now.toEpochSecond(ZoneOffset.UTC) * 1000;
//         long queryTimestamp = now.minusMinutes(1).toEpochSecond(ZoneOffset.UTC) * 1000;

//         Log log1 = Log.builder()
//                 .userBadge("123")
//                 .userName("testuser1")
//                 .action(Actions.Login)
//                 .timestamp(now.minusMinutes(2))
//                 .build();
//         Log log2 = Log.builder()
//                 .userBadge("456")
//                 .userName("testuser2")
//                 .action(Actions.Logout)
//                 .timestamp(now)
//                 .build();
//         mongoTemplate.save(log1);
//         mongoTemplate.save(log2);

//         // Act
//         List<Log> found = actionLogsRepository.findByTimestampGreaterThan(queryTimestamp);

//         // Assert
//         assertEquals(1, found.size());
//         assertEquals(now, found.get(0).getTimestamp());
//     }

//     @Test
//     void findByTimestampGreaterThan_WithNoMatchingLogs_ShouldReturnEmptyList() {
//         // Arrange
//         LocalDateTime now = LocalDateTime.now();
//         long queryTimestamp = now.minusMinutes(1).toEpochSecond(ZoneOffset.UTC) * 1000;

//         Log log = Log.builder()
//                 .userBadge("123")
//                 .userName("testuser")
//                 .action(Actions.Login)
//                 .timestamp(now.minusMinutes(2))
//                 .build();
//         mongoTemplate.save(log);

//         // Act
//         List<Log> found = actionLogsRepository.findByTimestampGreaterThan(queryTimestamp);

//         // Assert
//         assertTrue(found.isEmpty());
//     }

//     @Bean
//     public MongoTemplate mongoTemplate() {
//         String mongoUri = String.format("mongodb://%s:%d", mongo.getHost(), mongo.getMappedPort(27017));
//         MongoClient mongoClient = MongoClients.create(mongoUri);
//         return new MongoTemplate(mongoClient, "test");
//     }
// } 