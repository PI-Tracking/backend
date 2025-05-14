package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.CreateUserDTO;
import com.github.pi_tracking.backend.dto.LoginDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.service.AuthService;
import com.github.pi_tracking.backend.service.EmailService;
import com.github.pi_tracking.backend.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {
    private final UsersService usersService;
    private final AuthService authService;
    private final EmailService emailService;

    public UsersController(UsersService usersService, AuthService authService, EmailService emailService) {
        this.usersService = usersService;
        this.authService = authService;
        this.emailService = emailService;
    }

    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin()")
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody @Valid CreateUserDTO dto) {
        LoginDTO login = authService.createUser(dto);
        try {
            emailService.sendEmail(usersService.getUserByUsername(login.getUsername()).getEmail(), "Credenciais de Acesso", "Username: " + login.getUsername() + "\nPassword: " + login.getPassword());
            return new ResponseEntity<>(login, HttpStatus.CREATED);
        } catch (Exception e) {
            authService.removeUser(dto);
            return new ResponseEntity<>(e.toString(), HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin()")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {return ResponseEntity.ok(usersService.getAllUsers());}

    @GetMapping("/self")
    public ResponseEntity<User> getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin()")
    @GetMapping("/{badgeId}")
    public ResponseEntity<User> getUserByBadgeId(@PathVariable("badgeId") String badgeId) {
        User user = usersService.getUserByBadgeId(badgeId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin() and @usersService.getCurrentUser(authentication).getBadgeId() != #badgeId")
    @PatchMapping("/{badgeId}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable("badgeId") String badgeId) {
        User user = usersService.getUserByBadgeId(badgeId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        usersService.toggleActive(badgeId);
        return ResponseEntity.noContent().build();
    }


}
