package com.github.pi_tracking.backend.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${serverUrl}")
    private String serverUrl;

    @Bean
    public OpenAPI defineOpenAPI() {
        Server server = new Server();
        server.setUrl(serverUrl);

        Info information = new Info()
                .title("Tracking REST API")
                .version("1.0")
                .description("REST API for the Tracking demonstrator");

        return new OpenAPI().info(information).servers(List.of(server));
    }
}
