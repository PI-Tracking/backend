package com.github.pi_tracking.backend.integration;

import com.github.pi_tracking.backend.dto.CreateUserDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.UserRepository;
import com.github.pi_tracking.backend.service.AuthService;
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
public class UsersControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private String adminToken;

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
    void setUp() throws Exception {
        userRepository.deleteAll();
        
        // Create admin user
        adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .email("admin@example.com")
                .badgeId("ADMIN123")
                .active(true)
                .isAdmin(true)
                .build();
        
        userRepository.save(adminUser);

        // Login as admin to get token
        MvcResult loginResult = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.github.pi_tracking.backend.dto.LoginDTO("admin", "admin"))))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = loginResult.getResponse().getCookie("accessToken").getValue();
        assertNotNull(adminToken, "Admin token should not be null");
    }

    @Test
    void testCreateUser() throws Exception {
        // First verify admin user exists and is properly set up
        User admin = userRepository.findByUsername("admin").orElse(null);
        assertNotNull(admin, "Admin user should exist");
        assertTrue(admin.isAdmin(), "Admin user should have admin privileges");
        assertTrue(admin.isActive(), "Admin user should be active");

        CreateUserDTO newUser = new CreateUserDTO(
                "badge",
                "newuser@example.com",
                "NEW123",
                false
        );

        // Create the user with admin token
        MvcResult result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isCreated())
                .andReturn();

        // Print response for debugging
        System.out.println("Response status: " + result.getResponse().getStatus());
        System.out.println("Response content: " + result.getResponse().getContentAsString());

        // Verify user was created in the database
        User createdUser = userRepository.findByUsername("NEW123").orElse(null);
        assertNotNull(createdUser, "User should be created in the database");
        assertEquals("badge", createdUser.getBadgeId());
        assertEquals("newuser@example.com", createdUser.getEmail());
        assertFalse(createdUser.isAdmin());
        assertTrue(createdUser.isActive());
    }

    @Test
    void testGetAllUsers() throws Exception {
        // Create a regular user
        User regularUser = User.builder()
                .username("regular")
                .password(passwordEncoder.encode("regular123"))
                .email("regular@example.com")
                .badgeId("REG123")
                .active(true)
                .isAdmin(false)
                .build();
        
        userRepository.save(regularUser);

        MvcResult result = mockMvc.perform(get("/api/v1/users")
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isOk())
                .andReturn();

        User[] users = objectMapper.readValue(result.getResponse().getContentAsString(), User[].class);
        assertEquals(2, users.length); // Admin + regular user
    }

    @Test
    void testGetUserByBadgeId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/users/ADMIN123")
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isOk())
                .andReturn();

        User user = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        assertEquals("admin", user.getUsername());
        assertEquals("ADMIN123", user.getBadgeId());
    }

    @Test
    void testToggleUserActive() throws Exception {
        // Create a regular user
        User regularUser = User.builder()
                .username("regular")
                .password(passwordEncoder.encode("regular123"))
                .email("regular@example.com")
                .badgeId("REG123")
                .active(true)
                .isAdmin(false)
                .build();
        
        userRepository.save(regularUser);

        // Toggle user active status
        mockMvc.perform(patch("/api/v1/users/REG123/toggle-active")
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isNoContent());

        // Verify user is now inactive
        User updatedUser = userRepository.findByBadgeId("REG123").orElse(null);
        assertNotNull(updatedUser);
        assertFalse(updatedUser.isActive());
    }

    @Test
    void testNonAdminAccess() throws Exception {
        // Create a regular user
        User regularUser = User.builder()
                .username("regular")
                .password(passwordEncoder.encode("regular123"))
                .email("regular@example.com")
                .badgeId("REG123")
                .active(true)
                .isAdmin(false)
                .build();
        
        userRepository.save(regularUser);

        // Login as regular user
        MvcResult loginResult = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.github.pi_tracking.backend.dto.LoginDTO("regular", "regular123"))))
                .andExpect(status().isOk())
                .andReturn();

        String regularToken = loginResult.getResponse().getCookie("accessToken").getValue();

        // Try to access admin endpoints
        mockMvc.perform(get("/api/v1/users")
                .cookie(new Cookie("accessToken", regularToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateUserDTO("newuser", "newuser@example.com", "NEW123", false)))
                .cookie(new Cookie("accessToken", regularToken)))
                .andExpect(status().isForbidden());
    }
} 