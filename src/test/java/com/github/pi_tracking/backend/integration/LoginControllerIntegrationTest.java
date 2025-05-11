package com.github.pi_tracking.backend.integration;

import com.github.pi_tracking.backend.config.TestContainersConfig;
import com.github.pi_tracking.backend.dto.ChangePasswordDTO;
import com.github.pi_tracking.backend.dto.LoginDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.UserRepository;
import com.github.pi_tracking.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
@Testcontainers
public class LoginControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
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
    void testLoginSuccess() throws Exception {
        LoginDTO loginDTO = new LoginDTO("testuser", "password123");

        MvcResult result = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andReturn();

        User responseUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        assertEquals(testUser.getUsername(), responseUser.getUsername());
        assertEquals(testUser.getBadgeId(), responseUser.getBadgeId());
    }

    @Test
    void testLoginFailure() throws Exception {
        LoginDTO loginDTO = new LoginDTO("testuser", "wrongpassword");

        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogout() throws Exception {
        // First login to get the token
        LoginDTO loginDTO = new LoginDTO("testuser", "password123");
        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());

        // Then test logout
        mockMvc.perform(post("/api/v1/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("accessToken", 0));
    }

    @Test
    void testChangePassword() throws Exception {
        // First login to get the token
        LoginDTO loginDTO = new LoginDTO("testuser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String token = loginResult.getResponse().getCookie("accessToken").getValue();

        // Test password change
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "testuser",
                "password123",
                "newpassword123"
        );

        mockMvc.perform(patch("/api/v1/changePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordDTO))
                .cookie(loginResult.getResponse().getCookie("accessToken")))
                .andExpect(status().isOk());

        // Verify new password works
        LoginDTO newLoginDTO = new LoginDTO("testuser", "newpassword123");
        mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLoginDTO)))
                .andExpect(status().isOk());
    }
} 