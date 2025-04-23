package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.DetectionModel;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRepository extends MongoRepository<DetectionModel, String> {

    List<DetectionModel> findByAnalysisId(String analysisId);

    @Aggregation(pipeline = { "{ '$match': { 'report_id': ?0 } }", "{ '$group': { '_id': '$analysis_id' } }" })
    List<String> findByReportId(String reportId);

    boolean existsByAnalysisId(String analysisId);

    List<DetectionModel> findByAnalysisIdAndDetectionBoxIsNotNull(String analysisId);

    List<DetectionModel> findByAnalysisIdAndSegmentationMaskIsNotNull(String analysisId);
}