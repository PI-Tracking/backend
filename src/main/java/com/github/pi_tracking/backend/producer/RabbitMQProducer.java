package com.github.pi_tracking.backend.producer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import io.swagger.v3.core.util.Json;

import com.github.pi_tracking.backend.dto.SelectedDTO;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.checkerframework.checker.units.qual.m;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;



@Service
public class RabbitMQProducer {

    
    private final static String requests_queue = "Requests";
    private final static String live_analysis_queue = "Cameras";

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public void sendReportToAnalyse(String reportId, List<String> videos, String analysisId) {
        JsonObject message = buildJsonMessage(reportId, analysisId, videos, null);
        rabbitTemplate.convertAndSend(requests_queue, message);
        System.out.println("[x] Sent report: " + message);
    }

    public void sendReportToAnalyseWithSuspect(String reportId, List<String> videos, String analysisId, SelectedDTO selected) {
        JsonObject message = buildJsonMessage(reportId, analysisId, videos, selected);
        rabbitTemplate.convertAndSend(requests_queue, message);
        System.out.println("[x] Sent report with suspect: " + message);
    }


    public void startLiveAnalysis(List<String> cameras, String analysisId) {
        String message =analysisId + ";" + cameras;
        rabbitTemplate.convertAndSend(live_analysis_queue, message);
        System.out.println("[x] Sent live analysis request: " + message);
    }

    public void stopLiveAnalysis(String analysisId) {
        String message = "Stop:" + analysisId;
        rabbitTemplate.convertAndSend(live_analysis_queue, message);
        System.out.println("[x] Sent stop live analysis request: " + message);
    }

    private JsonObject buildJsonMessage(String reportId, String analysisId, List<String> videos, SelectedDTO selected) {
        JsonObject json = new JsonObject();
        json.addProperty("reportId", reportId);
        json.addProperty("analysisId", analysisId);

        JsonArray videosArray = new JsonArray();
        videos.forEach(videosArray::add);
        json.add("videos", videosArray);

        if (selected != null) {
            json.add("selected", new Gson().toJsonTree(selected));
        }

        return json;
    }


}
