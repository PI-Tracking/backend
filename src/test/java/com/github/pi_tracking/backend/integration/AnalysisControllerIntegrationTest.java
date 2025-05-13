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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
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

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String userToken;
    private Camera testCamera;
    private Report testReport;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/tracking_dev");
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
        // First start an analysis
        SelectedDTO selected = SelectedDTO.builder()
            .videoId(testCamera.getId().toString())
            .build();

        MvcResult startResult = mockMvc.perform(post("/api/v1/analysis/" + testReport.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selected))
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        NewAnalysisDTO startResponse = objectMapper.readValue(startResult.getResponse().getContentAsString(), NewAnalysisDTO.class);

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
        // First start an analysis
        SelectedDTO selected = SelectedDTO.builder()
            .videoId(testCamera.getId().toString())
            .build();

        MvcResult startResult = mockMvc.perform(post("/api/v1/analysis/" + testReport.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selected))
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        NewAnalysisDTO startResponse = objectMapper.readValue(startResult.getResponse().getContentAsString(), NewAnalysisDTO.class);

        // Then get the time intervals
        MvcResult result = mockMvc.perform(get("/api/v1/analysis/" + startResponse.getAnalysisId() + "/timestamps")
                .cookie(new Cookie("accessToken", userToken)))
                .andExpect(status().isOk())
                .andReturn();

        CameraTimeIntervalDTO[] intervals = objectMapper.readValue(result.getResponse().getContentAsString(), CameraTimeIntervalDTO[].class);
        assertNotNull(intervals);
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