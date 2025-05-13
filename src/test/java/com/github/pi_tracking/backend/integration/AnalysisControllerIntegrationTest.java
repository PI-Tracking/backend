package com.github.pi_tracking.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pi_tracking.backend.config.TestContainersConfig;
import com.github.pi_tracking.backend.dto.AnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.CameraTimeIntervalDTO;
import com.github.pi_tracking.backend.dto.NewAnalysisDTO;
import com.github.pi_tracking.backend.dto.SelectedDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.entity.Report;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.CamerasRepository;
import com.github.pi_tracking.backend.repository.ReportRepository;
import com.github.pi_tracking.backend.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import jakarta.servlet.http.Cookie;
import org.mockito.Mockito;
import com.github.pi_tracking.backend.entity.DetectionModel;
import com.github.pi_tracking.backend.repository.AnalysisRepository;
import com.github.pi_tracking.backend.entity.Upload;
import com.github.pi_tracking.backend.repository.UploadRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
public class AnalysisControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CamerasRepository camerasRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // mock rabbitmq
    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private UploadRepository uploadRepository;

    private User testUser;
    private String userToken;
    private Camera testCamera;
    private Report testReport;

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
        registry.add("spring.mail.properties.mail.smtp.aut  h", () -> true                );
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
        reportRepository.deleteAll();
        userRepository.deleteAll();
        camerasRepository.deleteAll();
        
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

        // Create test camera
        testCamera = Camera.builder()
                .name("Test Camera")
                .latitude(40.7128)
                .longitude(-74.0060)
                .active(true)
                .build();
        
        testCamera = camerasRepository.save(testCamera);

        // Create test report
        testReport = Report.builder()
                .name("Test Report")
                .creator(testUser)
                .build();
        
        testReport = reportRepository.save(testReport);

        // Login to get token
        MvcResult loginResult = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.github.pi_tracking.backend.dto.LoginDTO("testuser", "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        userToken = loginResult.getResponse().getCookie("accessToken").getValue();
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    void testAnalyseReport() throws Exception {
        SelectedDTO selected = SelectedDTO.builder()
            .videoId(testCamera.getId().toString())
            .build();

        MvcResult result = mockMvc.perform(post("/api/v1/analysis/" + testReport.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selected))
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        NewAnalysisDTO response = objectMapper.readValue(result.getResponse().getContentAsString(), NewAnalysisDTO.class);
        assertNotNull(response);
        assertNotNull(response.getAnalysisId());
    }

    @Test
    void testStartLiveAnalysis() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/analysis/live")
                .param("camerasId", testCamera.getId().toString())
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        NewAnalysisDTO response = objectMapper.readValue(result.getResponse().getContentAsString(), NewAnalysisDTO.class);
        assertNotNull(response);
        assertNotNull(response.getAnalysisId());
    }

    @Test
    void testStopLiveAnalysis() throws Exception {
        // First start a live analysis
        MvcResult startResult = mockMvc.perform(get("/api/v1/analysis/live")
                .param("camerasId", testCamera.getId().toString())
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        NewAnalysisDTO startResponse = objectMapper.readValue(startResult.getResponse().getContentAsString(), NewAnalysisDTO.class);

        // Then stop it
        mockMvc.perform(post("/api/v1/analysis/live/" + startResponse.getAnalysisId())
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAnalysisResults() throws Exception {
        // Create test upload
        Upload testUpload = Upload.builder()
                .camera(testCamera)
                .report(testReport)
                .build();
        testUpload = uploadRepository.save(testUpload);

        // First start an analysis
        SelectedDTO selected = SelectedDTO.builder()
            .videoId(testUpload.getId().toString())
            .build();

        // Mock RabbitMQ interaction
        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.anyString(), Mockito.anyString());

        MvcResult startResult = mockMvc.perform(post("/api/v1/analysis/" + testReport.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selected))
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        NewAnalysisDTO startResponse = objectMapper.readValue(startResult.getResponse().getContentAsString(), NewAnalysisDTO.class);

        // Create test detection data
        DetectionModel detection = new DetectionModel();
        detection.setAnalysisId(startResponse.getAnalysisId());
        detection.setVideoId(testUpload.getId().toString());
        detection.setTimestamp(System.currentTimeMillis());
        detection.setType("test-type");
        detection.setConfidence(0.95);
        analysisRepository.save(detection);

        // Then get the results
        MvcResult result = mockMvc.perform(get("/api/v1/analysis/" + startResponse.getAnalysisId())
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        AnalysisResponseDTO analysis = objectMapper.readValue(result.getResponse().getContentAsString(), AnalysisResponseDTO.class);
        assertNotNull(analysis);
        assertEquals(startResponse.getAnalysisId(), analysis.getAnalysisId());
    }

    @Test
    void testGetCameraTimeIntervals() throws Exception {
        // Create test upload
        Upload testUpload = Upload.builder()
                .camera(testCamera)
                .report(testReport)
                .build();
        testUpload = uploadRepository.save(testUpload);

        // First start an analysis
        SelectedDTO selected = SelectedDTO.builder()
            .videoId(testUpload.getId().toString())
            .build();

        // Mock RabbitMQ interaction
        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.anyString(), Mockito.anyString());

        MvcResult startResult = mockMvc.perform(post("/api/v1/analysis/" + testReport.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selected))
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        NewAnalysisDTO startResponse = objectMapper.readValue(startResult.getResponse().getContentAsString(), NewAnalysisDTO.class);

        // Create test detection data
        DetectionModel detection = new DetectionModel();
        detection.setAnalysisId(startResponse.getAnalysisId());
        detection.setVideoId(testUpload.getId().toString());
        detection.setTimestamp(System.currentTimeMillis());
        detection.setType("test-type");
        detection.setConfidence(0.95);
        analysisRepository.save(detection);

        // Then get the time intervals
        MvcResult result = mockMvc.perform(get("/api/v1/analysis/" + startResponse.getAnalysisId() + "/timestamps")
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        CameraTimeIntervalDTO[] intervals = objectMapper.readValue(result.getResponse().getContentAsString(), CameraTimeIntervalDTO[].class);
        assertNotNull(intervals);
        assertEquals(1, intervals.length);
        assertEquals(testCamera.getId(), intervals[0].getCameraId());
    }

    @Test
    void testAnalyseNonExistentReport() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        SelectedDTO selected = SelectedDTO.builder()
            .videoId(testCamera.getId().toString())
            .build();

        mockMvc.perform(post("/api/v1/analysis/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selected))
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isNotFound());
    }
} 