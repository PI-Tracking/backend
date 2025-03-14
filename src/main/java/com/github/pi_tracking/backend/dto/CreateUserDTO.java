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
    private String badgeId;
    private String email;
    private String username;
    @Builder.Default
    private boolean isAdmin = false;
}
