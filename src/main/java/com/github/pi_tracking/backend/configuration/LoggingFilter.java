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

import java.io.IOException;
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
            
            if (user!= null) {
                String userBadge = user.getBadgeId();

                List<Log> userLogs = logsService.getLogsByUserBadge(userBadge);
                if (!userLogs.isEmpty()) {
                    Log lastLog = userLogs.get(userLogs.size() - 1);
                    Actions lastAction = lastLog.getAction();
                }
                
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}