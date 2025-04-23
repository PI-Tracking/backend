package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.entity.DetectionModel;
import com.github.pi_tracking.backend.repository.AnalysisRepository;
import com.github.pi_tracking.backend.dto.AnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.CameraTimeIntervalDTO;
import com.github.pi_tracking.backend.dto.DetectionDTO;
import com.github.pi_tracking.backend.dto.SegmentationDTO;
import com.github.pi_tracking.backend.entity.Upload;
import com.github.pi_tracking.backend.repository.UploadRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final UploadRepository uploadRepository;

    public AnalysisService(AnalysisRepository analysisRepository, UploadRepository uploadRepository) {
        this.analysisRepository = analysisRepository;
        this.uploadRepository = uploadRepository;
    }

    public List<CameraTimeIntervalDTO> getCameraTimeIntervalsByAnalysisId(String analysisId) {
        
        List<DetectionModel> detections = analysisRepository.findByAnalysisId(analysisId);
        
        // Sort by timestamp
        detections.sort(Comparator.comparing(DetectionModel::getTimestamp));
        
        List<CameraTimeIntervalDTO> timeIntervals = new ArrayList<>();
        UUID currentCameraId = null;
        Long currentEntryTime = null;
        Long currentExitTime = null;
        
        for (DetectionModel detection : detections) {
            UUID cameraId = getCameraIdFromDetection(detection.getVideoId());
        
            if (currentCameraId == null || !currentCameraId.equals(cameraId)) {
                if (currentCameraId != null) {
                    // Set the final timestamp for the previous camera
                    timeIntervals.add(createTimeIntervalDTO(currentCameraId, currentEntryTime, currentExitTime));
                }
                currentCameraId = cameraId;
                currentEntryTime = detection.getTimestamp(); // Use Long for timestamp
            }
        
            currentExitTime = detection.getTimestamp();
        }
        
        if (currentCameraId != null) {
            timeIntervals.add(createTimeIntervalDTO(currentCameraId, currentEntryTime, currentExitTime));
        }
        
        return timeIntervals;
    }

    public AnalysisResponseDTO createAnalysisResponseDTO(String analysisId) {
        if (!analysisRepository.existsByAnalysisId(analysisId)) return null;

        List<DetectionDTO> detections = getDetectionsByAnalysisId(analysisId);
        List<SegmentationDTO> segmentations = getSegmentationsByAnalysisId(analysisId);

        return AnalysisResponseDTO.builder()
                .analysisId(analysisId)
                .detections(detections)
                .segmentations(segmentations)
                .build();
}

    private List<DetectionDTO> getDetectionsByAnalysisId(String analysisId) {
        List<DetectionModel> detectionModels = analysisRepository.findByAnalysisIdAndDetectionBoxIsNotNull(analysisId);

        List<DetectionDTO> detections = new ArrayList<>();
        for (DetectionModel detectionModel : detectionModels) {
            DetectionDTO detectionDTO = DetectionDTO.builder()
                    .className(detectionModel.getType())  // "knife", "gun", "suspect"
                    .confidence(detectionModel.getConfidence())
                    .coordinates(convertDetectionBoxToPointDTO(detectionModel.getDetectionBox())) 
                    .videoId(detectionModel.getVideoId())
                    .timestamp(detectionModel.getTimestamp())
                    .build();
            detections.add(detectionDTO);
        }

        return detections;
    }


    private List<SegmentationDTO> getSegmentationsByAnalysisId(String analysisId) {
        List<DetectionModel> detectionModels = analysisRepository.findByAnalysisIdAndSegmentationMaskIsNotNull(analysisId);
        
        List<SegmentationDTO> segmentationDTOs = new ArrayList<>();
        for (DetectionModel detection : detectionModels) {
            if (detection.getSegmentationMask() != null) {
                SegmentationDTO segmentationDTO = SegmentationDTO.builder()
                        .id(1)  // TBD: Change to the actual suspect id
                        .polygon(detection.getSegmentationMask()) 
                        .videoId(detection.getVideoId())
                        .timestamp(detection.getTimestamp())
                        .build();
                segmentationDTOs.add(segmentationDTO);
            }
        }
        
        return segmentationDTOs;
    }

    private UUID getCameraIdFromDetection(String videoId) {
        Upload upload = uploadRepository.findById(UUID.fromString(videoId))
                .orElseThrow(() -> new RuntimeException("Upload not found for videoId: " + videoId));
    
        return upload.getCamera().getId();
    }
    private CameraTimeIntervalDTO createTimeIntervalDTO(UUID cameraId, Long entryTimestamp, Long exitTimestamp) {
        return new CameraTimeIntervalDTO(cameraId, entryTimestamp, exitTimestamp);
    }

    private List<DetectionDTO.PointDTO> convertDetectionBoxToPointDTO(List<DetectionModel.Point> detectionBox) {
        if (detectionBox == null) {
            return new ArrayList<>();
        }
    
        List<DetectionDTO.PointDTO> points = new ArrayList<>();
        
        for (DetectionModel.Point point : detectionBox) {
            DetectionDTO.PointDTO pointDTO = new DetectionDTO.PointDTO(point.getX(), point.getY());
            points.add(pointDTO);
        }
        return points;
    }
}