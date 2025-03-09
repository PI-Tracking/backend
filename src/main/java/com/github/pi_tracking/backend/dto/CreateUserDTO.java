package com.github.pi_tracking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserDTO {
    private String cc;
    private String email;
    private String username;
    private boolean isAdmin = false;
}
