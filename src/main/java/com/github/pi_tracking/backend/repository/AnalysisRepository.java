package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.DetectionModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRepository extends MongoRepository<DetectionModel, String> {

    // Buscar as detecções de uma análise com base no analysis_id
    List<DetectionModel> findByAnalysisId(String analysisId);

    // Buscar as detecções associadas a um report_id
    List<DetectionModel> findByReportId(String reportId);
}