package com.github.pi_tracking.backend.controller;


import com.github.pi_tracking.backend.dto.NewReportDTO;
import com.github.pi_tracking.backend.dto.ReportResponseDTO;
import com.github.pi_tracking.backend.dto.SelectedDTO;
import com.github.pi_tracking.backend.dto.UploadDTO;
import com.github.pi_tracking.backend.entity.Report;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public AnalysisController(ReportService reportService, RabbitMQProducer rabbitMQProducer) {
        this.reportService = reportService;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @PostMapping("/{reportId}")
    public ResponseEntity<List<String>> analyseReport(
        @PathVariable UUID reportId,
        @RequestBody(required = false) SelectedDTO selected)
    {
        try{
            ReportResponseDTO report = reportService.getReportById(reportId);
            List<UploadDTO> uploads = report.getUploads();
            List<String> videos=new ArrayList<String>();
            String analysisId = UUID.randomUUID().toString();
            for (UploadDTO upload : uploads) {
                String url="http://localhost:9000/videos/"+reportId+"/"+upload.getId();
                videos.add(url);
            }
            if (selected != null) {
                rabbitMQProducer.sendReportToAnalyseWithSuspect(reportId.toString(), videos, analysisId, selected);
            } else {
                rabbitMQProducer.sendReportToAnalyse(reportId.toString(), videos, analysisId);
            }
            List<String> response = new ArrayList<>();
            response.add(analysisId);
            response.add(reportId.toString());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/live")
    public ResponseEntity<String> startLiveAnalysis(
        @RequestParam List<String> camerasId
    ) {
        String analysisId = UUID.randomUUID().toString();
        rabbitMQProducer.startLiveAnalysis(camerasId, analysisId);
        return new ResponseEntity<>(analysisId, HttpStatus.OK);
    }

    @PostMapping("/live/{analysisId}")
    public ResponseEntity<String> stopLiveAnalysis(@PathVariable String analysisId) {
        rabbitMQProducer.stopLiveAnalysis(analysisId);
        return new ResponseEntity<>(analysisId, HttpStatus.OK);
    }

}
