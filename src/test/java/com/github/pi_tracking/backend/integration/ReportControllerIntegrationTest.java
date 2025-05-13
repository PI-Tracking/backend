package com.github.pi_tracking.backend.integration;

import com.github.pi_tracking.backend.dto.NewReportDTO;
import com.github.pi_tracking.backend.dto.ReportAnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.ReportResponseDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.entity.Report;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.entity.Upload;
import com.github.pi_tracking.backend.repository.CamerasRepository;
import com.github.pi_tracking.backend.repository.ReportRepository;
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

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
public class ReportControllerIntegrationTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UploadRepository uploadRepository;

    private User testUser;
    private String userToken;
    private Camera testCamera;

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
        reportRepository.deleteAll();
        userRepository.deleteAll();
        camerasRepository.deleteAll();
        uploadRepository.deleteAll();
        
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

        // Login to get token
        MvcResult loginResult = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.github.pi_tracking.backend.dto.LoginDTO("testuser", "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        userToken = loginResult.getResponse().getCookie("accessToken").getValue();
    }

    @Test
    void testCreateReport() throws Exception {
        NewReportDTO reportDTO = new NewReportDTO(
                "Test Report",
                List.of(testCamera.getId())
        );

        MvcResult result = mockMvc.perform(post("/api/v1/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportDTO))
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isCreated())
                .andReturn();

        ReportResponseDTO createdReport = objectMapper.readValue(result.getResponse().getContentAsString(), ReportResponseDTO.class);
        assertNotNull(createdReport);
        assertEquals("Test Report", createdReport.getName());
        assertEquals(1, createdReport.getUploads().size());
        assertEquals(testCamera.getId(), createdReport.getUploads().get(0).getCameraId());
    }

    @Test
    void testGetAllReports() throws Exception {
        // Create some test reports
        Report report1 = Report.builder()
                .name("Report 1")
                .creator(testUser)
                .build();
        
        Report report2 = Report.builder()
                .name("Report 2")
                .creator(testUser)
                .build();
        
        reportRepository.save(report1);
        reportRepository.save(report2);

        MvcResult result = mockMvc.perform(get("/api/v1/reports")
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        Report[] reports = objectMapper.readValue(result.getResponse().getContentAsString(), Report[].class);
        assertEquals(2, reports.length);
    }

    @Test
    void testGetReportById() throws Exception {
        // Create a test report
        Report report = Report.builder()
                .name("Test Report")
                .creator(testUser)
                .build();
        
        report = reportRepository.save(report);

        MvcResult result = mockMvc.perform(get("/api/v1/reports/" + report.getId())
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        ReportResponseDTO foundReport = objectMapper.readValue(result.getResponse().getContentAsString(), ReportResponseDTO.class);
        assertEquals(report.getId(), foundReport.getId());
        assertEquals("Test Report", foundReport.getName());
    }

    @Test
    void testGetReportAnalysis() throws Exception {
        // Create a test report
        Report report = Report.builder()
                .name("Test Report")
                .creator(testUser)
                .build();
        
        report = reportRepository.save(report);

        // Create test upload
        Upload testUpload = Upload.builder()
                .camera(testCamera)
                .report(report)
                .build();
        testUpload = uploadRepository.save(testUpload);

        MvcResult result = mockMvc.perform(get("/api/v1/reports/" + report.getId() + "/analysis")
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        ReportAnalysisResponseDTO analysis = objectMapper.readValue(result.getResponse().getContentAsString(), ReportAnalysisResponseDTO.class);
        assertNotNull(analysis);
    }

    @Test
    void testGetNonExistentReport() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/reports/" + nonExistentId)
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isNotFound());
    }
} 