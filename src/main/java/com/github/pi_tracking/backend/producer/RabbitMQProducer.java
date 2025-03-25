package com.github.pi_tracking.backend.producer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.io.IOException;


public class RabbitMQProducer {

    private final static String requests_queue = "Requests";
    private final static String live_analysis_queue = "Cameras";
    private final static String HOST = "rabbitmq";

    public void sendReportToAnalyse(String reportId, List<String> videos, String analysisId) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {

            channel.queueDeclare(requests_queue, true, false, false, null);
            
            String message = buildMessage(reportId,analysisId, videos);
            
            channel.basicPublish("", requests_queue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent report to analyse: " + message);
            System.out.println(" [x] Analysis ID: " + analysisId);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void startLiveAnalysis(List<String> cameras, String analysisId) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {

            channel.queueDeclare(live_analysis_queue, true, false, false, null);
            
            String message = buildMessage("", analysisId, cameras);
            
            channel.basicPublish("", live_analysis_queue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent live analysis request: " + message);
            System.out.println(" [x] Analysis ID: " + analysisId);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void stopLiveAnalysis(String analysisId) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {

            channel.queueDeclare(live_analysis_queue, true, false, false, null);
            
            String message = "Stop: " + analysisId;
            
            channel.basicPublish("", live_analysis_queue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent stop live analysis request: " + message);
            System.out.println(" [x] Analysis ID: " + analysisId);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private String buildMessage(String reportId, String analysisId, List<String> videos) {
        if (reportId == "") {
            return analysisId + ";" + String.join(" ", videos);
        }
        return reportId + ";" + analysisId + ";" + String.join(" ", videos);
    }
    
}
