package com.github.pi_tracking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchPersonRequestDTO {
    private String reportId;
    private String videoId;
    private long timestamp;
}
