package com.github.pi_tracking.backend.dto;

import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^[a-z0-9.]+@[a-z0-9]+\\.[a-z]+(\\.[a-z]+)?$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;
    private String username;
    @Builder.Default
    private boolean isAdmin = false;
}
