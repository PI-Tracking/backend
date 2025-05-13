package com.github.pi_tracking.backend.integration;

import com.github.pi_tracking.backend.dto.CameraDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.entity.Upload;
import com.github.pi_tracking.backend.repository.CamerasRepository;
import com.github.pi_tracking.backend.repository.UserRepository;
import com.github.pi_tracking.backend.repository.UploadRepository;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
public class CamerasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CamerasRepository camerasRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UploadRepository uploadRepository;

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
        camerasRepository.deleteAll();
        userRepository.deleteAll();
        uploadRepository.deleteAll();
        
        // Create admin user
        adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@example.com")
                .badgeId("ADMIN123")
                .active(true)
                .isAdmin(true)
                .build();
        
        userRepository.save(adminUser);

        // Login as admin to get token
        MvcResult loginResult = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.github.pi_tracking.backend.dto.LoginDTO("admin", "admin123"))))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = loginResult.getResponse().getCookie("accessToken").getValue();
    }

    @Test
    void testCreateCamera() throws Exception {
        CameraDTO cameraDTO = new CameraDTO("Test Camera", 40.7128, -74.0060);

        MvcResult result = mockMvc.perform(post("/api/v1/cameras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cameraDTO))
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isCreated())
                .andReturn();

        Camera createdCamera = objectMapper.readValue(result.getResponse().getContentAsString(), Camera.class);
        assertNotNull(createdCamera);
        assertEquals("Test Camera", createdCamera.getName());
        assertEquals(40.7128, createdCamera.getLatitude());
        assertEquals(-74.0060, createdCamera.getLongitude());
    }

    @Test
    void testUpdateCamera() throws Exception {
        // First create a camera
        Camera camera = Camera.builder()
                .name("Original Camera")
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();
        
        camera = camerasRepository.save(camera);

        // Update the camera
        CameraDTO updateDTO = new CameraDTO("Updated Camera", 41.8781, -87.6298);

        MvcResult result = mockMvc.perform(put("/api/v1/cameras/" + camera.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isOk())
                .andReturn();

        Camera updatedCamera = objectMapper.readValue(result.getResponse().getContentAsString(), Camera.class);
        assertEquals("Updated Camera", updatedCamera.getName());
        assertEquals(41.8781, updatedCamera.getLatitude());
        assertEquals(-87.6298, updatedCamera.getLongitude());
    }

    @Test
    void testGetAllCameras() throws Exception {
        // Create some test cameras
        Camera camera1 = Camera.builder()
                .name("Camera 1")
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();
        
        Camera camera2 = Camera.builder()
                .name("Camera 2")
                .latitude(41.8781)
                .longitude(-87.6298)
                .build();
        
        camerasRepository.save(camera1);
        camerasRepository.save(camera2);

        MvcResult result = mockMvc.perform(get("/api/v1/cameras")
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isOk())
                .andReturn();

        Camera[] cameras = objectMapper.readValue(result.getResponse().getContentAsString(), Camera[].class);
        assertEquals(2, cameras.length);
    }

    @Test
    void testGetCameraById() throws Exception {
        // Create a test camera
        Camera camera = Camera.builder()
                .name("Test Camera")
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();
        
        camera = camerasRepository.save(camera);

        MvcResult result = mockMvc.perform(get("/api/v1/cameras/" + camera.getId())
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isOk())
                .andReturn();

        Camera foundCamera = objectMapper.readValue(result.getResponse().getContentAsString(), Camera.class);
        assertEquals(camera.getId(), foundCamera.getId());
        assertEquals("Test Camera", foundCamera.getName());
    }

    @Test
    void testToggleCameraActive() throws Exception {
        // Create a test camera
        Camera camera = Camera.builder()
                .name("Test Camera")
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();
        
        camera = camerasRepository.save(camera);

        // Toggle camera active status
        mockMvc.perform(patch("/api/v1/cameras/" + camera.getId() + "/toggle-active")
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isNoContent());

        // Verify camera is now inactive
        Camera updatedCamera = camerasRepository.findById(camera.getId()).orElse(null);
        assertNotNull(updatedCamera);
        assertFalse(updatedCamera.isActive());
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
        CameraDTO cameraDTO = new CameraDTO("Test Camera", 40.7128, -74.0060);

        mockMvc.perform(post("/api/v1/cameras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cameraDTO))
                .cookie(new Cookie("accessToken", regularToken)))
                .andExpect(status().isForbidden());
    }
} 