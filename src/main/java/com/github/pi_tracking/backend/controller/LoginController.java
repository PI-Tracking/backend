package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.ChangePasswordDTO;
import com.github.pi_tracking.backend.dto.LoginDTO;
import com.github.pi_tracking.backend.dto.ResetDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.service.AuthService;
import com.github.pi_tracking.backend.service.EmailService;
import com.github.pi_tracking.backend.service.JWTService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class LoginController {

    private final AuthService authService;
    private final JWTService jwtService;
    private final EmailService emailService;

    public LoginController(AuthService authService, JWTService jwtService, EmailService emailService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(
            @Valid @RequestBody final LoginDTO dto, HttpServletResponse response) {
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

    @PatchMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        LoginDTO login = authService.changePassword(dto);
        if (login == null) {
            return new ResponseEntity<>("Not working",HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }

    @PatchMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetDTO dto) {
        LoginDTO login = authService.resetPassword(dto.getEmail());
        if (login == null) {
            return new ResponseEntity<>("Not working",HttpStatus.BAD_REQUEST);
        }
        try {
            emailService.sendEmail(dto.getEmail(), "Credenciais de Acesso", "Username: " + login.getUsername() + "\nPassword: " + login.getPassword());
            return new ResponseEntity<>("New credentials have been sent successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.NOT_FOUND);
        }
    }


}