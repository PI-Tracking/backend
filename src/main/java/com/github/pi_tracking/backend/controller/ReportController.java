package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.NewReportDTO;
import com.github.pi_tracking.backend.dto.ReportAnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.ReportResponseDTO;
import com.github.pi_tracking.backend.entity.Report;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ReportResponseDTO> createReport(@RequestBody @Valid NewReportDTO dto) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User issuer = (User) auth.getPrincipal();

        ReportResponseDTO report = reportService.create(dto.getName(), dto.getCameras(), issuer);

        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Report>> getReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponseDTO> getReportById(@PathVariable UUID reportId) throws Exception {
        ReportResponseDTO report = reportService.getReportWithUploadUrlById(reportId);

        if (report == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(report);
    }

    @GetMapping("/{reportId}/analysis")
    public ResponseEntity<ReportAnalysisResponseDTO> getReportAnalysis(@PathVariable UUID reportId) {
        return ResponseEntity.ok(reportService.getAnalysisForReport(reportId));
    }
}
