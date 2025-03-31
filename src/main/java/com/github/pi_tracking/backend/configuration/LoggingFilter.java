package com.github.pi_tracking.backend.configuration;

import com.github.pi_tracking.backend.service.LogsService;
import com.github.pi_tracking.backend.utils.Actions;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private final LogsService logsService;

    public LoggingFilter(LogsService logsService) {
        this.logsService = logsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userBadge = request.getHeader("userBadge"); 
        String userName = request.getHeader("userName");  
        String path = request.getRequestURI(); 

        // endpoint based action
        Actions action = determineAction(path);

        if (userBadge != null && userName != null) {
            logsService.saveActionLog(userBadge, userName, action);
        }

        filterChain.doFilter(request, response);
    }

    private Actions determineAction(String path) {
        if (path.contains("/login")) {
            return Actions.Login;
        } else if (path.contains("/logout")) {
            return Actions.Logout;
        } else if (path.contains("/upload")) {
            return Actions.Upload_video;
        } else if (path.contains("/start-detection")) {
            return Actions.Start_detection;
        } else if (path.contains("/access-logs")) {
            return Actions.Access_logs;
        } else if (path.contains("/select-suspect")) {
            return Actions.Select_Suspect;
        }
        return Actions.Access_logs;  
    }

    @Override
    public void destroy() {
        // Limpeza, se necessário
    }
}