package com.github.pi_tracking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetectionDTO {
    private String className;
    private double confidence;
    private List<PointDTO> coordinates;
    private String videoId;
    private long timestamp;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PointDTO {
        private double x;
        private double y;
    }
}
