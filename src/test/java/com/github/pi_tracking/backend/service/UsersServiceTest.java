package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private UsersService usersService;

    @BeforeEach
    void setUp() {
        usersService = new UsersService(userRepository);
    }

    @Test
    void toggleActive_ShouldToggleUserActiveStatus() {
        String badgeId = "123";
        User user = User.builder()
                .badgeId(badgeId)
                .active(true)
                .build();

        when(userRepository.findByBadgeId(badgeId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        usersService.toggleActive(badgeId);

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> expectedUsers = Arrays.asList(
            User.builder().badgeId("1").build(),
            User.builder().badgeId("2").build()
        );
        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> result = usersService.getAllUsers();

        assertEquals(expectedUsers.size(), result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserByBadgeId_WithExistingUser_ShouldReturnUser() {
        String badgeId = "123";
        User expectedUser = User.builder().badgeId(badgeId).build();
        when(userRepository.findByBadgeId(badgeId)).thenReturn(Optional.of(expectedUser));

        User result = usersService.getUserByBadgeId(badgeId);

        assertNotNull(result);
        assertEquals(badgeId, result.getBadgeId());
        verify(userRepository).findByBadgeId(badgeId);
    }

    @Test
    void getUserByBadgeId_WithNonExistingUser_ShouldReturnNull() {
        String badgeId = "123";
        when(userRepository.findByBadgeId(badgeId)).thenReturn(Optional.empty());

        User result = usersService.getUserByBadgeId(badgeId);

        assertNull(result);
        verify(userRepository).findByBadgeId(badgeId);
    }

    @Test
    void getUserByUsername_WithExistingUser_ShouldReturnUser() {
        String username = "testuser";
        User expectedUser = User.builder().username(username).build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        User result = usersService.getUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void getCurrentUser_WithValidAuthentication_ShouldReturnUser() {
        String username = "testuser";
        User expectedUser = User.builder().username(username).build();
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        User result = usersService.getCurrentUser(authentication);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository).findByUsername(username);
    }
} 