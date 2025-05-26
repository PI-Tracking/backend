package com.github.pi_tracking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SegmentationPolygonDTO {
    private List<List<Double>> polygon;
}
