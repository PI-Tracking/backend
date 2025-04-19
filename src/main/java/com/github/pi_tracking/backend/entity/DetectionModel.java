package com.github.pi_tracking.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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
    private String analysisId;  

    private int timestamp; 

    private String type; 

    @Field("detection_box")
    private List<Point> detectionBox;  

    @Field("segmentation_mask")
    private List<List<Float>> segmentationMask;

    @Data  
    public static class Point {

        private float x; 
        private float y; 
    }

}