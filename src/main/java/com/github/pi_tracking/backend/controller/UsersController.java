package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.CreateUserDTO;
import com.github.pi_tracking.backend.dto.LoginDTO;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.service.AuthService;
import com.github.pi_tracking.backend.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {
    private final UsersService usersService;
    private final AuthService authService;

    public UsersController(UsersService usersService, AuthService authService) {
        this.usersService = usersService;
        this.authService = authService;
    }

    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin() and @usersService.getCurrentUser(authentication).isActive()")
    @PostMapping
    public ResponseEntity<LoginDTO> createUser(@RequestBody @Valid CreateUserDTO dto) throws Exception {
        LoginDTO login = authService.createUser(dto);
        return new ResponseEntity<>(login, HttpStatus.CREATED);
    }

    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin() and @usersService.getCurrentUser(authentication).isActive()")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {return ResponseEntity.ok(usersService.getAllUsers());}

    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin() and @usersService.getCurrentUser(authentication).isActive()")
    @GetMapping("/{badgeId}")
    public ResponseEntity<User> getUserByBadgeId(@PathVariable("badgeId") String badgeId) {
        User user = usersService.getUserByBadgeId(badgeId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("@usersService.getCurrentUser(authentication).isAdmin() and @usersService.getCurrentUser(authentication).isActive() and @usersService.getCurrentUser(authentication).getBadgeId() != #badgeId")
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
