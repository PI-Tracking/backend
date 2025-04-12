package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.AnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.SelectedDTO;
import com.github.pi_tracking.backend.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.pi_tracking.backend.producer.RabbitMQProducer;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final ReportService reportService;
    private final RabbitMQProducer rabbitMQProducer;

    public AnalysisController(ReportService reportService, RabbitMQProducer rabbitMQProducer) {
        this.reportService = reportService;
        this.rabbitMQProducer = rabbitMQProducer;
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

        return new ResponseEntity<>(new AnalysisResponseDTO(analysisId), HttpStatus.OK);
    }

    @GetMapping("/live")
    public ResponseEntity<AnalysisResponseDTO> startLiveAnalysis(
        @RequestParam List<String> camerasId
    ) {
        String analysisId = UUID.randomUUID().toString();
        rabbitMQProducer.startLiveAnalysis(camerasId, analysisId);
        AnalysisResponseDTO response = new AnalysisResponseDTO(analysisId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/live/{analysisId}")
    public ResponseEntity<AnalysisResponseDTO> stopLiveAnalysis(@PathVariable String analysisId) {
        rabbitMQProducer.stopLiveAnalysis(analysisId);
        AnalysisResponseDTO response = new AnalysisResponseDTO(analysisId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
