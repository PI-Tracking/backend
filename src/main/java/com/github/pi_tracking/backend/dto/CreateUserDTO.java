package com.github.pi_tracking.backend.dto;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String badgeId;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9.]+@[a-z0-9]+\\.[a-z]+(\\.[a-z]+)?$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;

    @NotBlank
    private String username;

    @Builder.Default
    private boolean isAdmin = false;
}
