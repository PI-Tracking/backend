package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.dto.ReportResponseDTO;
import com.github.pi_tracking.backend.dto.UploadDTO;
import com.github.pi_tracking.backend.entity.Camera;
import com.github.pi_tracking.backend.entity.Report;
import com.github.pi_tracking.backend.entity.Upload;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.repository.CamerasRepository;
import com.github.pi_tracking.backend.repository.ReportRepository;
import com.github.pi_tracking.backend.repository.UploadRepository;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final CamerasRepository camerasRepository;
    private final MinioClient minioClient;
    private final UploadRepository uploadRepository;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public ReportService(ReportRepository reportRepository, CamerasRepository camerasRepository, MinioClient minioClient, UploadRepository uploadRepository) {
        this.reportRepository = reportRepository;
        this.camerasRepository = camerasRepository;
        this.minioClient = minioClient;
        this.uploadRepository = uploadRepository;
    }

    @PostConstruct
    public void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName).build());

                String policy = "{"
                        + "\"Version\":\"2012-10-17\","
                        + "\"Statement\":[{"
                        + "\"Effect\":\"Allow\","
                        + "\"Principal\":\"*\","
                        + "\"Action\":\"s3:GetObject\","
                        + "\"Resource\":\"arn:aws:s3:::" + bucketName + "/*\""
                        + "}]"
                        + "}";

                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                                .bucket(bucketName).config(policy).build());
                log.info("Bucket '{}' created successfully.", bucketName);
            } else {
                log.info("Bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO bucket: " + e.getMessage(), e);
        }
    }

    public ReportResponseDTO create(String name, List<UUID> cameras, User creator) throws Exception {
        Report report = Report.builder()
                .creator(creator)
                .name(name)
                .build();

        List<Upload> uploads = new LinkedList<>();
        List<UploadDTO> uploadDTOs = new LinkedList<>();

        Set<UUID> allCameras = camerasRepository.findAll().stream().filter(Camera::isActive).map(Camera::getId).collect(Collectors.toSet());

        if (!allCameras.containsAll(cameras)) {
            throw new IllegalArgumentException("Invalid camera ID(s)");
        }

        for (UUID camId : cameras) {
            Upload upload = Upload.builder()
                    .report(report)
                    .camera(camerasRepository.getReferenceById(camId))
                    .build();

            uploads.add(upload);
        }

        report.setUploads(uploads);
        report = reportRepository.save(report);

        uploads = uploadRepository.saveAll(uploads);

        for (Upload upload : uploads) {
            String uploadUrl = generateMinioPreSignedUrl(report.getId().toString(), upload.getId().toString());

            UploadDTO dto = UploadDTO.builder()
                    .id(upload.getId())
                    .cameraId(upload.getCamera().getId())
                    .uploadUrl(uploadUrl)
                    .build();

            uploadDTOs.add(dto);
        }

        return ReportResponseDTO.builder()
                .id(report.getId())
                .name(report.getName())
                .uploads(uploadDTOs)
                .build();
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public ReportResponseDTO getReportById(UUID id) throws Exception {
        Report report = reportRepository.findById(id).orElse(null);

        if (report == null) return null;

        List<UploadDTO> uploads = new LinkedList<>();

        for (Upload upload : report.getUploads()) {
            String objectPath = String.format("%s/%s", report.getId(), upload.getId());
            boolean uploaded = minioObjectExists(objectPath);

            UploadDTO dto = UploadDTO.builder()
                    .id(upload.getId())
                    .cameraId(upload.getCamera().getId())
                    .uploaded(uploaded)
                    .uploadUrl(!uploaded ? generateMinioPreSignedUrl(report.getId().toString(), upload.getId().toString()) : null)
                    .build();

            uploads.add(dto);
        }

        return ReportResponseDTO.builder()
                .id(report.getId())
                .name(report.getName())
                .uploads(uploads)
                .build();
    }

    private String generateMinioPreSignedUrl(String reportId, String uploadId) throws Exception {
        String objectPath = String.format("%s/%s", reportId, uploadId);

        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucketName)
                .object(objectPath)
                .expiry(12, TimeUnit.HOURS)
                .build();

        return minioClient.getPresignedObjectUrl(args);
    }

    private boolean minioObjectExists(String objectPath) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectPath).build());
        } catch (Exception ex) {
            return false;
        }

        return true;
    }
}
