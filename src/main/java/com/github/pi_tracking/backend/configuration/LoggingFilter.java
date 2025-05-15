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

import java.io.IOException;

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
                Actions action = determineAction(request); 

                if (action != null) {
                    logsService.saveActionLog(userBadge, username, action);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private Actions determineAction(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.contains("/api/v1/login")) {
            return Actions.Login;
        }
        
        if (path.contains("/api/v1/logout")) {
            return Actions.Logout;
        }
        
        if (path.contains("/api/v1/analysis")) {
            // Se for um pedido GET, regista a ação de upload de video
            if (request.getMethod().equals("GET")) {
                return Actions.Upload_video; 
            }

            // Se for um pedido sem suspeito selecionado, regista ação de start detection
            if (request.getParameter("selected") == null) {
                return Actions.Start_detection;
            }
            
            // Se tiver suspeito selecionado, regista açao de select suspect
            return Actions.Select_Suspect;  
        }

        if (path.contains("/api/v1/userlogs")) {
            return Actions.Access_logs;
        }

        // Ação sem log
        return null;
    }
}
