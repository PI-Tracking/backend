package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.*;
import com.github.pi_tracking.backend.service.ReportService;
import com.github.pi_tracking.backend.service.AnalysisService;
import com.github.pi_tracking.backend.producer.RabbitMQProducer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final ReportService reportService;
    private final RabbitMQProducer rabbitMQProducer;
    private final AnalysisService analysisService;

    public AnalysisController(ReportService reportService, RabbitMQProducer rabbitMQProducer, AnalysisService analysisService) {
        this.reportService = reportService;
        this.rabbitMQProducer = rabbitMQProducer;
        this.analysisService = analysisService;
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
        String analysisId = UUID.randomUUID().toString();
        rabbitMQProducer.sendFaceDetection(analysisId, reportId.toString(), faceImage);
        
        NewAnalysisDTO response = new NewAnalysisDTO(analysisId);

        // Save suspect image to MinIO using ReportService
        try {
            reportService.saveSuspectImage(reportId, faceImage);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/search-person")
    public SearchPersonResponseDTO searchPerson(@RequestBody @Valid SearchPersonRequestDTO dto) {
        return analysisService.searchPersonInFrame(dto);
    }
}
