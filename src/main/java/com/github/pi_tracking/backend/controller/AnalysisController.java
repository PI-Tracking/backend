package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.AnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.CameraTimeIntervalDTO;
import com.github.pi_tracking.backend.dto.NewAnalysisDTO;
import com.github.pi_tracking.backend.dto.SelectedDTO;
import com.github.pi_tracking.backend.service.ReportService;
import com.github.pi_tracking.backend.service.AnalysisService;
import com.github.pi_tracking.backend.producer.RabbitMQProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final ReportService reportService;
    private final RabbitMQProducer rabbitMQProducer;
    private final AnalysisService analysisService;
    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public AnalysisController(
        ReportService reportService, 
        RabbitMQProducer rabbitMQProducer, 
        AnalysisService analysisService,
        MinioClient minioClient
    ) {
        this.reportService = reportService;
        this.rabbitMQProducer = rabbitMQProducer;
        this.analysisService = analysisService;
        this.minioClient = minioClient;
    }

    @PostMapping("/{reportId}")
    public ResponseEntity<NewAnalysisDTO> analyseReport(
        @PathVariable UUID reportId,
        @RequestBody(required = false) SelectedDTO selected
    ) {
        if (!reportService.reportExistsById(reportId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String analysisId = UUID.randomUUID().toString();
        rabbitMQProducer.sendReportToAnalyse(reportId.toString(), analysisId, selected);

        // Only send the analysisId, as the analysis itself is processed asynchronously
        NewAnalysisDTO response = new NewAnalysisDTO(analysisId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/live")
    public ResponseEntity<NewAnalysisDTO> startLiveAnalysis(
        @RequestParam List<String> camerasId
    ) {
        String analysisId = UUID.randomUUID().toString();
        rabbitMQProducer.startLiveAnalysis(camerasId, analysisId);
        
        NewAnalysisDTO response = new NewAnalysisDTO(analysisId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/live/{analysisId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stopLiveAnalysis(@PathVariable String analysisId) {
        rabbitMQProducer.stopLiveAnalysis(analysisId);
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<AnalysisResponseDTO> getAnalysisResultsByAnalysisId(@PathVariable String analysisId) {
        AnalysisResponseDTO response = analysisService.createAnalysisResponseDTO(analysisId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{analysisId}/timestamps")
    public ResponseEntity<List<CameraTimeIntervalDTO>> getCameraTimeIntervals(@PathVariable String analysisId) {
        List<CameraTimeIntervalDTO> timeIntervals = analysisService.getCameraTimeIntervalsByAnalysisId(analysisId);
        if (timeIntervals.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(timeIntervals, HttpStatus.OK);
    }

    @PostMapping("/face-detection/{reportId}")
    public ResponseEntity<NewAnalysisDTO> startFaceDetection(
        @PathVariable UUID reportId,
        @RequestPart(required = true, name = "faceImage") MultipartFile faceImage
    ) {
        try {
            // Store the face image in MinIO
            String objectPath = String.format("%s/suspect-image", reportId);
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .stream(faceImage.getInputStream(), faceImage.getSize(), -1)
                .contentType(faceImage.getContentType())
                .build());

            // Send to RabbitMQ for analysis
            String analysisId = UUID.randomUUID().toString();
            rabbitMQProducer.sendFaceDetection(analysisId, reportId.toString(), faceImage);
            
            NewAnalysisDTO response = new NewAnalysisDTO(analysisId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
