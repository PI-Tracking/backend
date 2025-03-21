package com.github.pi_tracking.backend.controller;

import com.github.pi_tracking.backend.dto.CreateUserDTO;
import com.github.pi_tracking.backend.dto.LoginDTO;
import com.github.pi_tracking.backend.entity.Log;
import com.github.pi_tracking.backend.entity.User;
import com.github.pi_tracking.backend.service.AuthService;
import com.github.pi_tracking.backend.service.LogsService;
import com.github.pi_tracking.backend.service.UsersService;
import jakarta.validation.Valid;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;


//To get access to user activity logs. Not to mistake with analysis logs aka reports.
@RestController
@RequestMapping("/api/v1/userlogs")
public class LogsController {
    
    private final LogsService logsService;

    public LogsController(LogsService logsService) {
        this.logsService = logsService;
    }

    // For temporary testing purposes
    @PostMapping
    public ResponseEntity<Log> createLog(@RequestBody Log log) {
        return ResponseEntity.ok(logsService.createLog(log));
    }
    //

    @GetMapping
    public ResponseEntity<List<Log>> allLogs(@RequestParam(required = false) Long timestamp) {

        if(timestamp != null){
            return ResponseEntity.ok(logsService.getLogsAfterTimestamp(timestamp));
        }

        return ResponseEntity.ok(logsService.getAllLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Log> getLogById(@PathVariable String id) {

        Log log = logsService.getLogById(id);

        if (log == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(log);
    }

    @GetMapping("/user/{userBadge}")
    public ResponseEntity<List<Log>> getLogsByUserBadge(@PathVariable String userBadge, @RequestParam(required = false) Long timestamp) {

        if(timestamp != null){
            return ResponseEntity.ok(logsService.getLogsAfterTimestamp(timestamp));
        }

        List<Log> log = logsService.getLogsByUserBadge(userBadge);

        if (log == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(log);
    }


}
