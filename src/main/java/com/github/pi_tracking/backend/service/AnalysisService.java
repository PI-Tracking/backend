package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.entity.DetectionModel;
import com.github.pi_tracking.backend.repository.AnalysisRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;

    public AnalysisService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    // Buscar todos os resultados de uma análise com base no report_id
    public List<DetectionModel> getResultsByReportId(String reportId) {
        return analysisRepository.findByReportId(reportId);  // A consulta será feita no MongoDB
    }

    // Buscar todos os resultados de uma análise com base no analysis_id
    public List<DetectionModel> getResultsByAnalysisId(String analysisId) {
        return analysisRepository.findByAnalysisId(analysisId);
    }
}