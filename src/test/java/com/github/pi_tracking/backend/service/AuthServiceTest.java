package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.dto.ChangePasswordDTO;
import com.github.pi_tracking.backend.dto.CreateUserDTO;
import com.github.pi_tracking.backend.dto.LoginDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .badgeId("123")
                .email("test@example.com")
                .isAdmin(false)
                .build();

        when(userRepository.existsByBadgeId(dto.getBadgeId())).thenReturn(false);
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        LoginDTO result = authService.createUser(dto);

        assertNotNull(result);
        assertEquals(dto.getUsername(), result.getUsername());
        assertNotNull(result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithExistingBadgeId_ShouldThrowException() {
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .badgeId("123")
                .email("test@example.com")
                .isAdmin(false)
                .build();

        when(userRepository.existsByBadgeId(dto.getBadgeId())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.createUser(dto));
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnUser() {
        LoginDTO dto = new LoginDTO("testuser", "password");
        User expectedUser = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .build();

        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(expectedUser));

        User result = authService.authenticate(dto);

        assertNotNull(result);
        assertEquals(dto.getUsername(), result.getUsername());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void changePassword_WithValidData_ShouldChangePassword() {
        ChangePasswordDTO dto = new ChangePasswordDTO("testuser", "oldPass", "newPass");
        User user = User.builder()
                .username("testuser")
                .password("encodedOldPass")
                .build();

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getOldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(dto.getNewPassword())).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        LoginDTO result = authService.changePassword(dto);

        assertNotNull(result);
        assertEquals(dto.getUsername(), result.getUsername());
        assertEquals(dto.getNewPassword(), result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_WithInvalidOldPassword_ShouldThrowException() {
        ChangePasswordDTO dto = new ChangePasswordDTO("testuser", "wrongPass", "newPass");
        User user = User.builder()
                .username("testuser")
                .password("encodedOldPass")
                .build();

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getOldPassword(), user.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(dto));
    }
} 