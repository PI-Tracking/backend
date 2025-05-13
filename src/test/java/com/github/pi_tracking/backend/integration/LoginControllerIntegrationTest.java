package com.github.pi_tracking.backend.integration;

import com.github.pi_tracking.backend.dto.LoginDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.UserRepository;
import com.github.pi_tracking.backend.config.TestContainersConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
public class LoginControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> postgres = TestContainersConfig.getPostgres();
        MongoDBContainer mongo = TestContainersConfig.getMongo();
        GenericContainer<?> minio = TestContainersConfig.getMinio();

        // PostgreSQL configuration
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.database", () -> "testdb");     
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");

        // MongoDB configuration
        String mongoUri = String.format("mongodb://%s:%d/testdb", mongo.getHost(), mongo.getMappedPort(27017));
        registry.add("spring.data.mongodb.uri", () -> mongoUri);        
        registry.add("security.jwt.secret-key", () -> "cdc38dee898b2800d5a129226bfc5fa30c167c4e75eb8940b0142e8def11fd2bc584b3b208e600a97d1291b1e9bd25f2b79bc2dfb81f9a4f21e9d023a232b1b2c0a65292fbcfdc95f8dfcbbd82a98b1e874934222a8e87b3adc8a9566267459b3a366480f8ee95dbac7d21fcaae9f7e74e59bd067c919e1341b19baa502d7fa0ea634676ae2e1eee220acff9b852f3f9e5578450db8574b22fd314ef0afccbfa048cfa30ef7335001b1aa23ac3a4557ac7c2c6c538c2b839b67da6613b948937da3dea259e93f20641ba4e3cc22af976e391bd343c5f532411ef7186c22f4adebd86ebe91c4e0556c34fdd943df2851ec0e740a27f2008de29b4fb11a4b9f074");
        registry.add("security.jwt.expiration-time", () -> 3600000);

        // MinIO configuration
        String minioUrl = String.format("http://%s:%d", minio.getHost(), minio.getMappedPort(9000));
        registry.add("minio.url", () -> minioUrl);
        registry.add("minio.access.name", () -> "tracking");    
        registry.add("minio.access.secret", () -> "password");
        registry.add("minio.bucket.name", () -> "videos");
        registry.add("web.url", () -> "http://localhost:5173");
        registry.add("web.port", () -> 5173);

        // Rabbitmq
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> 5672);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");

        // Mail
        registry.add("spring.mail.host", () -> "smtp.gmail.com");
        registry.add("spring.mail.port", () -> 587);
        registry.add("spring.mail.username", () -> "trackingtrackingpi@gmail.com");
        registry.add("spring.mail.password", () -> "lrtmlutmmyxkardz");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> true);
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> true);

        registry.add("springdoc.api-docs.path", () -> "/api/v1/docs");
        registry.add("serverUrl", () -> "http://localhost:8080");

        registry.add("admin.badgeid", () -> "123456789");
        registry.add("admin.email", () -> "admin@admin.com");
        registry.add("admin.username", () -> "admin");
        registry.add("admin.password", () -> "admin");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Create test user
        testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .badgeId("TEST123")
                .active(true)
                .isAdmin(false)
                .build();
        
        userRepository.save(testUser);
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        LoginDTO loginDTO = new LoginDTO("testuser", "password123");

        MvcResult result = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andReturn();

        Cookie accessToken = result.getResponse().getCookie("accessToken");
        assertNotNull(accessToken);
        assertTrue(accessToken.getValue().length() > 0);
    }

    @Test
    void testFailedLoginWithWrongPassword() throws Exception {
        LoginDTO loginDTO = new LoginDTO("testuser", "wrongpassword");

        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testFailedLoginWithNonExistentUser() throws Exception {
        LoginDTO loginDTO = new LoginDTO("nonexistentuser", "password123");

        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginWithInactiveUser() throws Exception {
        // Create an inactive user
        User inactiveUser = User.builder()
                .username("inactiveuser")
                .password(passwordEncoder.encode("password123"))
                .email("inactive@example.com")
                .badgeId("INACTIVE123")
                .active(false)
                .isAdmin(false)
                .build();
        
        userRepository.save(inactiveUser);

        LoginDTO loginDTO = new LoginDTO("inactiveuser", "password123");

        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLoginWithAdminUser() throws Exception {
        // Create admin user
        User adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@example.com")
                .badgeId("ADMIN123")
                .active(true)
                .isAdmin(true)
                .build();
        
        userRepository.save(adminUser);

        LoginDTO loginDTO = new LoginDTO("admin", "admin123");

        MvcResult result = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andReturn();

        Cookie accessToken = result.getResponse().getCookie("accessToken");
        assertNotNull(accessToken);
        assertTrue(accessToken.getValue().length() > 0);
    }

    @Test
    void testLoginWithInvalidRequest() throws Exception {
        // Test with missing username
        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\": \"password123\"}"))
                .andExpect(status().isBadRequest());

        // Test with missing password
        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"testuser\"}"))
                .andExpect(status().isBadRequest());

        // Test with empty username
        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"\", \"password\": \"password123\"}"))
                .andExpect(status().isBadRequest());

        // Test with empty password
        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"testuser\", \"password\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
} 