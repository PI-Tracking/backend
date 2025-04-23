package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.AnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.CameraTimeIntervalDTO;
import com.github.pi_tracking.backend.dto.SelectedDTO;
import com.github.pi_tracking.backend.service.ReportService;
import com.github.pi_tracking.backend.service.AnalysisService;
import com.github.pi_tracking.backend.producer.RabbitMQProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<AnalysisResponseDTO> analyseReport(
        @PathVariable UUID reportId,
        @RequestBody(required = false) SelectedDTO selected
    ) {
        if (!reportService.reportExistsById(reportId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String analysisId = UUID.randomUUID().toString();
        rabbitMQProducer.sendReportToAnalyse(reportId.toString(), analysisId, selected);

        // Preenche o DTO com as deteções e segmentações
        AnalysisResponseDTO response = analysisService.createAnalysisResponseDTO(analysisId, reportId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/live")
    public ResponseEntity<AnalysisResponseDTO> startLiveAnalysis(
        @RequestParam List<String> camerasId
    ) {
        String analysisId = UUID.randomUUID().toString();
        rabbitMQProducer.startLiveAnalysis(camerasId, analysisId);
        
        AnalysisResponseDTO response = analysisService.createAnalysisResponseDTO(analysisId, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/live/{analysisId}")
    public ResponseEntity<AnalysisResponseDTO> stopLiveAnalysis(@PathVariable String analysisId) {
        rabbitMQProducer.stopLiveAnalysis(analysisId);
        
        AnalysisResponseDTO response = analysisService.createAnalysisResponseDTO(analysisId, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<AnalysisResponseDTO> getAnalysisResultsByReportId(@PathVariable String reportId) {
        AnalysisResponseDTO response = analysisService.createAnalysisResponseDTO(reportId, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
}

    @GetMapping("/{analysisId}")
    public ResponseEntity<AnalysisResponseDTO> getAnalysisResultsByAnalysisId(@PathVariable String analysisId) {
        AnalysisResponseDTO response = analysisService.createAnalysisResponseDTO(analysisId, null);
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
}
