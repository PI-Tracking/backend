package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.entity.DetectionModel;
import com.github.pi_tracking.backend.repository.AnalysisRepository;
import com.github.pi_tracking.backend.repository.UploadRepository;
import com.github.pi_tracking.backend.dto.CameraTimeIntervalDTO;
import com.github.pi_tracking.backend.entity.Upload;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    public List<DetectionModel> getResultsByReportId(String reportId) {
        return analysisRepository.findByReportId(reportId);
    }

    public List<DetectionModel> getResultsByAnalysisId(String analysisId) {
        return analysisRepository.findByAnalysisId(analysisId);
    }

    public List<CameraTimeIntervalDTO> getCameraTimeIntervalsByAnalysisId(String analysisId) {
        
        List<DetectionModel> detections = analysisRepository.findByAnalysisId(analysisId);
    
        // Sort by timestamp
        detections.sort(Comparator.comparing(DetectionModel::getTimestamp));
    
        List<CameraTimeIntervalDTO> timeIntervals = new ArrayList<>();
        UUID currentCameraId = null;
        LocalDateTime currentEntryTime = null;
        LocalDateTime currentExitTime = null;
    
        for (DetectionModel detection : detections) {
            UUID cameraId = getCameraIdFromDetection(detection.getVideoId());
    
            if (currentCameraId == null || !currentCameraId.equals(cameraId)) {
                if (currentCameraId != null) {
                    // Set the final timestamp for the previous camera
                    timeIntervals.add(createTimeIntervalDTO(currentCameraId, currentEntryTime, currentExitTime));
                }
                currentCameraId = cameraId;
                currentEntryTime = LocalDateTime.ofEpochSecond(detection.getTimestamp(), 0, ZoneOffset.UTC);
            }
    
            // Update the exit timestamp only when the suspect leaves the camera (camera changes)
            if (!currentCameraId.equals(cameraId)) {
                currentExitTime = LocalDateTime.ofEpochSecond(detection.getTimestamp(), 0, ZoneOffset.UTC);
            }
        }
    
        if (currentCameraId != null) {
            timeIntervals.add(createTimeIntervalDTO(currentCameraId, currentEntryTime, currentExitTime));
        }
    
        return timeIntervals;
    }

    private UUID getCameraIdFromDetection(String videoId) {
        Upload upload = uploadRepository.findByVideoId(videoId);
        return upload.getCamera().getId();
    }

    private CameraTimeIntervalDTO createTimeIntervalDTO(UUID cameraId, LocalDateTime entryTimestamp, LocalDateTime exitTimestamp) {
        return new CameraTimeIntervalDTO(cameraId, entryTimestamp, exitTimestamp);
    }
}