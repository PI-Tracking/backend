package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.NewReportDTO;
import com.github.pi_tracking.backend.dto.ReportAnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.ReportResponseDTO;
import com.github.pi_tracking.backend.entity.Report;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.service.ReportService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;
    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public ReportController(ReportService reportService, MinioClient minioClient) {
        this.reportService = reportService;
        this.minioClient = minioClient;
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

    @PostMapping("/{reportId}/suspect-image")
    public ResponseEntity<Void> saveSuspectImage(
        @PathVariable UUID reportId,
        @RequestPart(required = true, name = "suspectImage") MultipartFile suspectImage
    ) {
        try {
            reportService.saveSuspectImage(reportId, suspectImage);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{reportId}/suspect-image")
    public ResponseEntity<String> getSuspectImage(@PathVariable UUID reportId) {
        try {
            String objectPath = String.format("%s/suspect-image", reportId);
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .build());
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            return ResponseEntity.ok(base64Image);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{reportId}/uploads/{uploadId}/video")
    public ResponseEntity<byte[]> getVideo(@PathVariable UUID reportId, @PathVariable UUID uploadId) {
        try {
            String objectPath = String.format("%s/%s", reportId, uploadId);
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .build());
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
