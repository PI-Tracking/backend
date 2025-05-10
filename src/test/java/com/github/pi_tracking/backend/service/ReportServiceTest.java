package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.dto.ReportAnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.ReportResponseDTO;
import com.github.pi_tracking.backend.entity.*;
import com.github.pi_tracking.backend.repository.AnalysisRepository;
import com.github.pi_tracking.backend.repository.CamerasRepository;
import com.github.pi_tracking.backend.repository.ReportRepository;
import com.github.pi_tracking.backend.repository.UploadRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private CamerasRepository camerasRepository;
    @Mock
    private MinioClient minioClient;
    @Mock
    private UploadRepository uploadRepository;
    @Mock
    private AnalysisRepository analysisRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository, camerasRepository, minioClient, uploadRepository, analysisRepository);
        ReflectionTestUtils.setField(reportService, "bucketName", "test-bucket");
    }

    @Test
    void create_WithValidData_ShouldCreateReport() throws Exception {
        String name = "Test Report";
        List<UUID> cameras = Arrays.asList(UUID.randomUUID());
        User creator = User.builder().badgeId("123").build();
        Camera camera = Camera.builder().id(cameras.get(0)).active(true).build();
        Report report = Report.builder().id(UUID.randomUUID()).name(name).creator(creator).build();
        Upload upload = Upload.builder().id(UUID.randomUUID()).camera(camera).report(report).build();

        when(camerasRepository.findAll()).thenReturn(Collections.singletonList(camera));
        when(reportRepository.save(any(Report.class))).thenReturn(report);
        when(uploadRepository.saveAll(any())).thenReturn(Collections.singletonList(upload));
        when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://test-url");

        ReportResponseDTO result = reportService.create(name, cameras, creator);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(1, result.getUploads().size());
        verify(reportRepository).save(any(Report.class));
        verify(uploadRepository).saveAll(any());
    }

    @Test
    void create_WithInvalidCamera_ShouldThrowException() {
        String name = "Test Report";
        List<UUID> cameras = Arrays.asList(UUID.randomUUID());
        User creator = User.builder().badgeId("123").build();
        Camera camera = Camera.builder().id(UUID.randomUUID()).active(true).build();

        when(camerasRepository.findAll()).thenReturn(Collections.singletonList(camera));

        assertThrows(IllegalArgumentException.class, () -> 
            reportService.create(name, cameras, creator)
        );
    }

    @Test
    void getAllReports_ShouldReturnAllReports() {
        List<Report> expectedReports = Arrays.asList(
            Report.builder().id(UUID.randomUUID()).name("Report 1").build(),
            Report.builder().id(UUID.randomUUID()).name("Report 2").build()
        );
        when(reportRepository.findAll()).thenReturn(expectedReports);

        List<Report> result = reportService.getAllReports();

        assertEquals(expectedReports.size(), result.size());
        verify(reportRepository).findAll();
    }

    @Test
    void getAnalysisForReport_WithValidId_ShouldReturnAnalysis() {
        UUID reportId = UUID.randomUUID();
        List<String> expectedAnalysisIds = Arrays.asList("analysis1", "analysis2");
        when(analysisRepository.findByReportId(reportId.toString())).thenReturn(expectedAnalysisIds);

        ReportAnalysisResponseDTO result = reportService.getAnalysisForReport(reportId);

        assertNotNull(result);
        assertEquals(expectedAnalysisIds, result.getAnalysisIds());
        verify(analysisRepository).findByReportId(reportId.toString());
    }
} 