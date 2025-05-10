package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.dto.AnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.CameraTimeIntervalDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.entity.DetectionModel;
import com.github.pi_tracking.backend.entity.Upload;
import com.github.pi_tracking.backend.repository.AnalysisRepository;
import com.github.pi_tracking.backend.repository.UploadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private UploadRepository uploadRepository;

    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService(analysisRepository, uploadRepository);
    }

    @Test
    void getCameraTimeIntervalsByAnalysisId_ShouldReturnCorrectIntervals() {
        String analysisId = "test-analysis";
        UUID cameraId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        
        List<DetectionModel> detections = Arrays.asList(
            createDetectionModel(analysisId, uploadId.toString(), 1000L),
            createDetectionModel(analysisId, uploadId.toString(), 2000L)
        );

        Upload upload = Upload.builder()
                .id(uploadId)
                .camera(Camera.builder().id(cameraId).build())
                .build();

        when(analysisRepository.findByAnalysisId(analysisId)).thenReturn(detections);
        when(uploadRepository.findById(uploadId)).thenReturn(Optional.of(upload));

        List<CameraTimeIntervalDTO> result = analysisService.getCameraTimeIntervalsByAnalysisId(analysisId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cameraId, result.get(0).getCameraId());
        assertEquals(1000L, result.get(0).getInitialTimestamp());
        assertEquals(2000L, result.get(0).getFinalTimestamp());
    }

    @Test
    void createAnalysisResponseDTO_WithValidData_ShouldReturnDTO() {
        String analysisId = "test-analysis";
        UUID uploadId = UUID.randomUUID();
        
        List<DetectionModel> detections = Arrays.asList(
            createDetectionModel(analysisId, uploadId.toString(), 1000L)
        );

        when(analysisRepository.existsByAnalysisId(analysisId)).thenReturn(true);
        when(analysisRepository.findByAnalysisIdAndDetectionBoxIsNotNull(analysisId)).thenReturn(detections);
        when(analysisRepository.findByAnalysisIdAndSegmentationMaskIsNotNull(analysisId)).thenReturn(new ArrayList<>());

        AnalysisResponseDTO result = analysisService.createAnalysisResponseDTO(analysisId);

        assertNotNull(result);
        assertEquals(analysisId, result.getAnalysisId());
        assertEquals(1, result.getDetections().size());
        assertTrue(result.getSegmentations().isEmpty());
    }

    private DetectionModel createDetectionModel(String analysisId, String videoId, Long timestamp) {
        DetectionModel model = new DetectionModel();
        model.setAnalysisId(analysisId);
        model.setVideoId(videoId);
        model.setTimestamp(timestamp);
        model.setType("test-type");
        model.setConfidence(0.95);
        return model;
    }
} 