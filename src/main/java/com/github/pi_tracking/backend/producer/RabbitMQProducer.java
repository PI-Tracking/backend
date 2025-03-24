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

    private final static String QUEUE_NAME = "Requests";
    private final static String HOST = "localhost";

    public void sendReportToAnalyse(String reportId, List<String> videos) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            
            String message = buildMessage(reportId, videos);
            
            channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent report to analyse: " + message);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
    
    private String buildMessage(String reportId, List<String> videos) {
        return "ReportID: " + reportId + ", Videos: " + String.join(", ", videos);
    }
    
}
