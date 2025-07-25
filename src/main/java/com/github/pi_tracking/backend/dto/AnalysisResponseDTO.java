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
public class AnalysisResponseDTO {
    private String analysisId;
    private List<DetectionDTO> detections;
    private List<SegmentationDTO> segmentations;
}
