package com.github.pi_tracking.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration
@Testcontainers
public class TestContainersConfig {

    private static final Logger logger = LoggerFactory.getLogger(TestContainersConfig.class);

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.2")
            .withExposedPorts(27017);

    @Container
    static final GenericContainer<?> minio = new GenericContainer<>("minio/minio:latest")
            .withExposedPorts(9000, 9001)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server /data --console-address ':9001'");

    static {
        try {
            postgres.start();
            mongo.start();
            minio.start();
            String mongoUri = String.format("mongodb://%s:%d", mongo.getHost(), mongo.getMappedPort(27017));
            logger.info("MongoDB URI: {}", mongoUri);
            Thread.sleep(5000); // Add a delay to ensure MongoDB is ready
        } catch (Exception e) {
            logger.error("Failed to start test containers", e);
            throw new RuntimeException("Failed to start test containers", e);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.hikari.max-lifetime", () -> 30000); // 30 seconds
        
        String mongoUri = String.format("mongodb://%s:%d", mongo.getHost(), mongo.getMappedPort(27017));
        logger.info("Configuring MongoDB URI: {}", mongoUri);
        registry.add("spring.data.mongodb.uri", () -> mongoUri);
        registry.add("spring.data.mongodb.auto-index-creation", () -> true);

        // Configure MinIO properties
        String minioUrl = String.format("http://%s:%d", minio.getHost(), minio.getMappedPort(9000));
        logger.info("Configuring MinIO URL: {}", minioUrl);
        registry.add("minio.url", () -> minioUrl);
        registry.add("minio.access.name", () -> "minioadmin");
        registry.add("minio.access.secret", () -> "minioadmin");
        registry.add("minio.bucket.name", () -> "videos");
    }
} 