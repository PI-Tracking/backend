package com.github.pi_tracking.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewReportDTO {
    @NotBlank
    private String name;

    @NotNull
    @NotEmpty
    private List<UUID> cameras;

    @NotNull
    private Boolean hasSuspect;
}
