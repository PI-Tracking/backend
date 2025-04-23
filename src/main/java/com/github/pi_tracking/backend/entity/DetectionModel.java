package com.github.pi_tracking.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "detections")  
@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class DetectionModel {

    @Id
    private String id;  

    @Field("video_id")
    private String videoId; 

    @Field("analysis_id")
    @Indexed
    private String analysisId;  

    @Field("report_id")
    @Indexed
    private String reportId;

    private Double confidence;

    private long timestamp;

    private String type; 

    @Field("detection_box")
    private List<Point> detectionBox;  

    @Field("segmentation_mask")
    private List<List<Double>> segmentationMask;

    @Data  
    public static class Point {

        private double x;
        private double y;
    }

}