package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.AnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.ReportResponseDTO;
import com.github.pi_tracking.backend.dto.SelectedDTO;
import com.github.pi_tracking.backend.dto.UploadDTO;
import com.github.pi_tracking.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.pi_tracking.backend.producer.RabbitMQProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final ReportService reportService;
    private final RabbitMQProducer rabbitMQProducer;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucket.name}")
    private String minioBucket;

    public AnalysisController(ReportService reportService, RabbitMQProducer rabbitMQProducer) {
        this.reportService = reportService;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @PostMapping("/{reportId}")
    public ResponseEntity<AnalysisResponseDTO> analyseReport(
        @PathVariable UUID reportId,
        @RequestBody(required = false) SelectedDTO selected
    ) throws Exception {
        ReportResponseDTO report = reportService.getReportById(reportId);

        if (report == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<UploadDTO> uploads = report.getUploads();
        List<String> videos = new ArrayList<>();
        String analysisId = UUID.randomUUID().toString();

        for (UploadDTO upload : uploads) {
            if (upload.isUploaded()) {
                String url = String.format("%s/%s/%s/%s", minioUrl, minioBucket, reportId, upload.getId());
                videos.add(url);
            }
        }

        if (selected != null) {
            rabbitMQProducer.sendReportToAnalyseWithSuspect(reportId.toString(), videos, analysisId, selected);
        } else {
            rabbitMQProducer.sendReportToAnalyse(reportId.toString(), videos, analysisId);
        }

        AnalysisResponseDTO response = new AnalysisResponseDTO(analysisId);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
