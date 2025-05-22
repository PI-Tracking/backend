package com.github.pi_tracking.backend.producer;

import com.github.pi_tracking.backend.dto.SelectedDTO;

import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

import static com.github.pi_tracking.backend.configuration.RabbitConfig.LIVE_ANALYSIS_QUEUE;
import static com.github.pi_tracking.backend.configuration.RabbitConfig.REQUESTS_QUEUE;

@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendReportToAnalyse(String reportId, String analysisId, SelectedDTO selected) {
        JsonObject json = new JsonObject();
        json.addProperty("analysisId", analysisId);
        json.addProperty("reportId", reportId);

        if (selected != null) {
            json.add("selected", new Gson().toJsonTree(selected));
        }

        rabbitTemplate.convertAndSend(REQUESTS_QUEUE, json.toString());
    }

    public void startLiveAnalysis(List<String> cameras, String analysisId) {
        String message = analysisId + ";" + cameras;
        rabbitTemplate.convertAndSend(LIVE_ANALYSIS_QUEUE, message);
    }

    public void stopLiveAnalysis(String analysisId) {
        String message = "Stop:" + analysisId;
        rabbitTemplate.convertAndSend(LIVE_ANALYSIS_QUEUE, message);
    }

    // To send the face directly to backend and analyze the video and see if the corresponding face is there
    public void sendFaceDetection(String analysisId, String reportId, MultipartFile faceImage) {
        JsonObject json = new JsonObject();
        json.addProperty("analysisId", analysisId);
        json.addProperty("reportId", reportId);
        try {
            json.addProperty("faceId", Base64.getEncoder().encodeToString(faceImage.getBytes()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Uploaded image is not valid!");
        }

        rabbitTemplate.convertAndSend(REQUESTS_QUEUE, json.toString());
    }
}
