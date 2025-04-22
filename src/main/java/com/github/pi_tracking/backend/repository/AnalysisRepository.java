package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.DetectionModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRepository extends MongoRepository<DetectionModel, String> {

    List<DetectionModel> findByAnalysisId(String analysisId);

    List<DetectionModel> findByReportId(String reportId);
}