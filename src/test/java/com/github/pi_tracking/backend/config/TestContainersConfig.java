package com.github.pi_tracking.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

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
    public static final MongoDBContainer mongo = new MongoDBContainer("mongo:latest")
            .withExposedPorts(27017)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofSeconds(60));

    // @Container
    // static final GenericContainer<?> minio = new GenericContainer<>("minio/minio:latest")
    //         .withExposedPorts(9000, 9001)
    //         .withEnv("MINIO_ROOT_USER", "tracking")  // Matching your prod config
    //         .withEnv("MINIO_ROOT_PASSWORD", "password")  // Matching your prod config
    //                 .withCommand("server /data --console-address 'localhost:9001'")
    //         .waitingFor(Wait.forLogMessage(".*MinIO Object Storage Server.*", 1))
    //         .withStartupTimeout(Duration.ofSeconds(30));
    @Container
    static final MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withExposedPorts(9000, 9001)
            .withEnv("MINIO_ROOT_USER", "tracking")  // Matching your prod config
            .withEnv("MINIO_ROOT_PASSWORD", "password")  // Matching your prod config
            .withStartupTimeout(Duration.ofSeconds(30));
    static {
        try {
            postgres.start();
            mongo.start();
            minio.start();
            logger.info("Started PostgreSQL container at: {}", postgres.getJdbcUrl());
            logger.info("Started MongoDB container at: mongodb://{}:{}", mongo.getHost(), mongo.getMappedPort(27017));
            logger.info("Started MinIO container at: {}", minio.getHost ());
            } catch (Exception e) {
            logger.error("Failed to start test containers", e);
            throw new RuntimeException("Failed to start test containers", e);
        }
    }       

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.datasource.hikari.max-lifetime", () -> 30000);
        
        // MongoDB configuration
        String mongoUri = String.format("mongodb://%s:%d/testdb", mongo.getHost(), mongo.getMappedPort(27017));
        logger.info("Configuring MongoDB URI: {}", mongoUri);
        registry.add("spring.data.mongodb.uri", () -> mongoUri);
        registry.add("spring.data.mongodb.auto-index-creation", () -> true);

        // MinIO configuration
        String minioUrl = String.format("http://%s:%d", minio.getHost(), minio.getMappedPort(9000));
        logger.info("MinIO URL: {}", minioUrl);
        registry.add("minio.url", () -> minioUrl);
        registry.add("minio.access.name", () -> "tracking");
        registry.add("minio.access.secret", () -> "password");
        registry.add("minio.bucket.name", () -> "videos");
        
        // Add serverUrl for your application
        registry.add("serverUrl", () -> "http://localhost:8080");
    }

    public static MongoDBContainer getMongo() {
        return mongo;
    }
    
    public static PostgreSQLContainer<?> getPostgres() {
        return postgres;
    }
    
    public static GenericContainer<?> getMinio() {
        return minio;
    }
}   