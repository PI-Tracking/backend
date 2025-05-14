package com.github.pi_tracking.backend.configuration;

import com.github.pi_tracking.backend.service.JWTService;
import com.github.pi_tracking.backend.service.LogsService;
import com.github.pi_tracking.backend.service.UsersService;
import com.github.pi_tracking.backend.utils.Actions;
import com.github.pi_tracking.backend.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import org.springframework.core.annotation.Order;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

@Component
@Order(2)
public class LoggingFilter extends OncePerRequestFilter {

    private final LogsService logsService;
    private final JWTService jwtService;
    private final UsersService usersService;

    public LoggingFilter(LogsService logsService, JWTService jwtService, UsersService usersService) {
        this.logsService = logsService;
        this.jwtService = jwtService;
        this.usersService = usersService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Obter cookie de accessToken
        Cookie cookie = WebUtils.getCookie(request, "accessToken");

        if (cookie != null) {
            final String jwt = cookie.getValue();
            String username = jwtService.extractUsername(jwt); 

            User user = usersService.getUserByUsername(username);  
            if (user != null) {
                String userBadge = user.getBadgeId();
                Actions action = determineAction(request, null); 

                if (action != null) {
                    logsService.saveActionLog(userBadge, username, action);
                }
            }
        }

        // ResponseWrapper para intercetar a resposta
        ResponseWrapper responseWrapper = new ResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        String responseBody = new String(responseWrapper.getData());
        Actions responseAction = determineAction(request, responseBody);

        if (responseAction != null) {
            logsService.saveActionLog(getUserBadge(request), getUsername(request), responseAction);
        }

        response.getOutputStream().write(responseWrapper.getData());
    }

    private String getUsername(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "accessToken");
        if (cookie != null) {
            final String jwt = cookie.getValue();
            return jwtService.extractUsername(jwt); 
        }
        return "Unknown User"; 
    }

    private String getUserBadge(HttpServletRequest request) {
        return getUsername(request);
    }

    @Override
    public void destroy() {
    }

    private Actions determineAction(HttpServletRequest request, String responseBody) {
        String path = request.getRequestURI();

        if (path.contains("/api/v1/login")) {
            return Actions.Login;
        }
        
        else if (path.contains("/api/v1/logout")) {
            return Actions.Logout;
        }
        
        else if (path.contains("/api/v1/analysis")) {
            // Se houver links para MinIO na resposta, regista a ação de upload de vídeo
            if (responseBody != null && responseBody.contains("minio")) {
                return Actions.Upload_video; 
            }

            // Verifica conforme o parâmetro "selected" na requisição
            if (request.getParameter("selected") == null) {
                return Actions.Start_detection;  
            } else {
                return Actions.Select_Suspect;  
            }
        }

        else if (path.contains("/api/v1/userlogs")) {
            return Actions.Access_logs;
        }

        return null; 
    }
}

class ResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream outputStream;
    private PrintWriter writer;

    public ResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        outputStream = new ByteArrayOutputStream();
        writer = new PrintWriter(outputStream);
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    public byte[] getData() {
        return outputStream.toByteArray();
    }
}