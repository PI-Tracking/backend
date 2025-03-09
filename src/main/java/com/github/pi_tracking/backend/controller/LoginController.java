package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.LoginDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.service.AuthService;
import com.github.pi_tracking.backend.service.JWTService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LoginController {

    private final AuthService authService;

    private final JWTService jwtService;

    public LoginController(AuthService authService, JWTService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }


    @PostMapping("/login")
    public ResponseEntity<User> login(
            @Valid @RequestBody final LoginDTO dto, HttpServletResponse response) throws Exception {
        User user = authService.authenticate(dto);
        String jwtToken = jwtService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtService.getExpirationTime() / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ResponseEntity.noContent().build();
    }

}