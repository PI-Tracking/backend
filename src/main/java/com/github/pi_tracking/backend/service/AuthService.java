package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.dto.ChangePasswordDTO;
import com.github.pi_tracking.backend.dto.CreateUserDTO;
import com.github.pi_tracking.backend.dto.LoginDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.github.pi_tracking.backend.utils.StringUtils;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }


    public LoginDTO createUser(CreateUserDTO dto) {
        String badgeId = dto.getBadgeId();
        if (userRepository.existsByBadgeId(badgeId)) {
            throw new IllegalArgumentException("A user with that badge id already exists!");
        }

        String username = dto.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("A user with that name already exists!");
        }

        String password = StringUtils.generateRandomString(16);

        User user = User
                .builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .badgeId(dto.getBadgeId())
                .email(dto.getEmail())
                .isAdmin(dto.isAdmin())
                .build();

        userRepository.save(user);



        return LoginDTO
                .builder()
                .username(username)
                .password(password)
                .build();
    }

    public User authenticate(LoginDTO dto){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        return userRepository.findByUsername(dto.getUsername()).orElseThrow();
    }

    public LoginDTO changePassword(ChangePasswordDTO dto) {
        if (!userRepository.existsByUsername(dto.getUsername())){
            throw new IllegalArgumentException("User or password is incorrect!");
        }
        User user = userRepository.findByUsername(dto.getUsername()).orElse(null);

        if (user == null || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("User or password is incorrect!");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        return new LoginDTO(dto.getUsername(), dto.getNewPassword());
    }

}
