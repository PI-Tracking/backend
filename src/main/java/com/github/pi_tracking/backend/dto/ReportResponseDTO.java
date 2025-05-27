package com.github.pi_tracking.backend.dto;

import com.github.pi_tracking.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportResponseDTO {
    private UUID id;
    private String name;
    private List<UploadDTO> uploads;
    private LocalDateTime createdAt;
    private User creator;
}
