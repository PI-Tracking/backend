package com.github.pi_tracking.backend.integration;

import com.github.pi_tracking.backend.dto.CreateUserDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.UserRepository;
import com.github.pi_tracking.backend.service.AuthService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
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

    @BeforeEach
    void setUp() throws Exception {
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
    void testCreateUser() throws Exception {
        CreateUserDTO newUser = new CreateUserDTO(
                "newuser",
                "newuser@example.com",
                "NEW123",
                false
        );

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))
                .cookie(new Cookie("accessToken", adminToken)))
                .andExpect(status().isCreated())
                .andReturn();

        // Verify user was created
        User createdUser = userRepository.findByUsername("newuser").orElse(null);
        assertNotNull(createdUser);
        assertEquals("NEW123", createdUser.getBadgeId());
        assertEquals("newuser@example.com", createdUser.getEmail());
        assertFalse(createdUser.isAdmin());
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