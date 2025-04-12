package com.github.pi_tracking.backend.producer;

import com.github.pi_tracking.backend.dto.SelectedDTO;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.google.gson.JsonArray;
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

    public void sendReportToAnalyse(String reportId, List<String> videos, String analysisId, SelectedDTO selected) {
        String message = buildJsonMessage(reportId, analysisId, videos, selected);
        rabbitTemplate.convertAndSend(REQUESTS_QUEUE, message);
    }

    public void startLiveAnalysis(List<String> cameras, String analysisId) {
        String message = analysisId + ";" + cameras;
        rabbitTemplate.convertAndSend(LIVE_ANALYSIS_QUEUE, message);
    }

    public void stopLiveAnalysis(String analysisId) {
        String message = "Stop:" + analysisId;
        rabbitTemplate.convertAndSend(LIVE_ANALYSIS_QUEUE, message);
    }

    private String buildJsonMessage(String reportId, String analysisId, List<String> videos, SelectedDTO selected) {
        JsonObject json = new JsonObject();
        json.addProperty("reportId", reportId);
        json.addProperty("analysisId", analysisId);

        JsonArray videosArray = new JsonArray();
        videos.forEach(videosArray::add);
        json.add("videos", videosArray);

        if (selected != null) {
            json.add("selected", new Gson().toJsonTree(selected));
        }

        return json.toString();
    }
}
