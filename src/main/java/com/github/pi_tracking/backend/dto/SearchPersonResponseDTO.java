package com.github.pi_tracking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchPersonResponseDTO {
    private List<SegmentationPolygonDTO> segmentations;
}
