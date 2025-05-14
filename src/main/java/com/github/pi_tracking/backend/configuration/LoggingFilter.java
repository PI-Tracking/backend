package com.github.pi_tracking.backend.configuration;

import com.github.pi_tracking.backend.service.JWTService;
import com.github.pi_tracking.backend.service.LogsService;
import com.github.pi_tracking.backend.service.UsersService;
import com.github.pi_tracking.backend.utils.Actions;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.entity.Log;
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
import java.util.List;

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

        // Obter o cookie de accessToken
        Cookie cookie = WebUtils.getCookie(request, "accessToken");

        if (cookie != null) {
            final String jwt = cookie.getValue();
            String username = jwtService.extractUsername(jwt); // Extrair o username do JWT

            User user = usersService.getUserByUsername(username);  // Buscar o user na base de dados
            
            if (user != null) {
                String userBadge = user.getBadgeId();

                List<Log> userLogs = logsService.getLogsByUserBadge(userBadge);
                if (!userLogs.isEmpty()) {
                    Log lastLog = userLogs.get(userLogs.size() - 1);
                    Actions lastAction = lastLog.getAction();

                    logsService.saveActionLog(userBadge, username, lastAction);
                }
            }
        }

        // ResponseWrapper para intercetar a resposta
        ResponseWrapper responseWrapper = new ResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        String responseBody = new String(responseWrapper.getData());
        Actions action = determineAction(request, responseBody);

        if (action != null) {
            logsService.saveActionLog(getUserBadge(request), getUsername(request), action);
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
            if (responseBody.contains("minio")) {
                return Actions.Upload_video; 
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
