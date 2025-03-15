package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsersService {
    private final UserRepository userRepository;

    public UsersService(UserRepository userRepository) {this.userRepository = userRepository;}

    public void toggleActive(String badgeId) {
        User user = userRepository.findByBadgeId(badgeId).orElseThrow();
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByBadgeId(String badgeId) {
        return userRepository.findByBadgeId(badgeId).orElse(null);
    }

    public User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user;
    }
}
