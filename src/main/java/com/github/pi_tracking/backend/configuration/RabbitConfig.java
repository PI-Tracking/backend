package com.github.pi_tracking.backend.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String REQUESTS_QUEUE = "Requests";
    public static final String LIVE_ANALYSIS_QUEUE = "Cameras";

    @Bean
    public Queue requestsQueue() {
        return new Queue(REQUESTS_QUEUE, true);
    }

    @Bean
    public Queue liveQueue() {
        return new Queue(LIVE_ANALYSIS_QUEUE, true);
    }
}
