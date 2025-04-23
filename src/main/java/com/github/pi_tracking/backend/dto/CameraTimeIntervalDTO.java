package com.github.pi_tracking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CameraTimeIntervalDTO {
    private UUID cameraId;
    private Long initialTimestamp;
    private Long finalTimestamp;
}