package com.github.pi_tracking.backend.seed;

import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminSeed implements CommandLineRunner {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${admin.badgeid}")
    private String badgeId;

    @Value("${admin.email}")
    private String email;

    @Value("${admin.username}")
    private String username;

    @Value("${admin.password}")
    private String password;

    public AdminSeed(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        loadAdmin();
    }

    private void loadAdmin() {
        if (userRepository.count() == 0) {
            log.info("User repository is empty, creating a new admin account with credentials {} {}", username, password);

            User admin = User
                    .builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .badgeId(badgeId)
                    .email(email)
                    .isAdmin(true)
                    .build();

            userRepository.save(admin);
        }
    }
}
