package com.github.pi_tracking.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CameraDTO {
    @NotBlank
    @Size(max = 64)
    private String name;

    @NotNull
    @DecimalMin(value="-90")
    @DecimalMax(value="90")
    private Double latitude;

    @NotNull
    @DecimalMin(value="-180")
    @DecimalMax(value="180")
    private Double longitude;
}
