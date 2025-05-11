package com.github.pi_tracking.backend.integration;

import com.github.pi_tracking.backend.dto.NewReportDTO;
import com.github.pi_tracking.backend.dto.ReportAnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.ReportResponseDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.entity.Report;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.CamerasRepository;
import com.github.pi_tracking.backend.repository.ReportRepository;
import com.github.pi_tracking.backend.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
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

    private User testUser;
    private String userToken;
    private Camera testCamera;

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