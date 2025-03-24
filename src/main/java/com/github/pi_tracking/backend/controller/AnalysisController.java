package com.github.pi_tracking.backend.controller;


import com.github.pi_tracking.backend.dto.NewReportDTO;
import com.github.pi_tracking.backend.dto.ReportResponseDTO;
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

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponseDTO> getReport(@PathVariable UUID reportId) {
        try{
            ReportResponseDTO report = reportService.getReportById(reportId);
            List<UploadDTO> uploads = report.getUploads();
            List<String> videos=new ArrayList<String>();
            for (UploadDTO upload : uploads) {
                videos.add(upload.getUploadUrl());
            }
            rabbitMQProducer.sendReportToAnalyse(reportId.toString(), videos);
            return new ResponseEntity<>(report, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }



}
