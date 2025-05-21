package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.AnalysisResponseDTO;
import com.github.pi_tracking.backend.dto.CameraTimeIntervalDTO;
import com.github.pi_tracking.backend.dto.NewAnalysisDTO;
import com.github.pi_tracking.backend.dto.SelectedDTO;
import com.github.pi_tracking.backend.service.ReportService;
import com.github.pi_tracking.backend.service.AnalysisService;
import com.github.pi_tracking.backend.producer.RabbitMQProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
        @RequestPart(required = false, name = "selectedSuspect") SelectedDTO selected,
        @RequestPart(required = false, name = "faceImage") MultipartFile             faceImage
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

    @PostMapping("/face-detection")
    public ResponseEntity<NewAnalysisDTO> startFaceDetection(@PathVariable UUID reportId) {
        String analysisId = UUID.randomUUID().toString();
        rabbitMQProducer.sendFaceDetection(analysisId, reportId.toString());
        
        NewAnalysisDTO response = new NewAnalysisDTO(analysisId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    
    @PostMapping("/has-face")
    public ResponseEntity<String> hasFace(@RequestParam("file") MultipartFile file,
                                          @RequestParam("reportId") UUID reportId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
    
        String analysisId = UUID.randomUUID().toString(); // Generate a unique ID
    
        try {
            rabbitMQProducer.hasFaceDetection(analysisId, reportId.toString(), file);
            return ResponseEntity.ok("Image sent for face validation with analysisId: " + analysisId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process image");
        }
    }
    


    
    // @PostMapping("/has-face")
    // public ResponseEntity<String> handleImageUpload(@RequestParam("file") MultipartFile file) {
    //     if (file.isEmpty()) {
    //         return ResponseEntity.badRequest().body("File is empty");
    //     }

    //     try {
    //         byte[] imageData = file.getBytes();
    //         // Process the image data as needed

    //         return ResponseEntity.ok("Image uploaded successfully: " + file.getOriginalFilename());
    //     } catch (IOException e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process image");
    //     }
    // }
    
}
