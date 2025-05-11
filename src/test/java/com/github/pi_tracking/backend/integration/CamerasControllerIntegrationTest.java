package com.github.pi_tracking.backend.integration;

import com.github.pi_tracking.backend.dto.CameraDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.CamerasRepository;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
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

    private User adminUser;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        camerasRepository.deleteAll();
        userRepository.deleteAll();
        
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