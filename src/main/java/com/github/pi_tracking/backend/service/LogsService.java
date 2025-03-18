package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.repository.ActionLogsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import com.github.pi_tracking.backend.entity.Log;
import com.github.pi_tracking.backend.utils.Actions;

@Service
public class LogsService {
    private final ActionLogsRepository actionLogsRepository;

    public LogsService(ActionLogsRepository actionLogsRepository) {
        this.actionLogsRepository = actionLogsRepository;
    }


    //To access logs

    public List<Log> getLogsByUserBadge(String userBadge) {
        return actionLogsRepository.findByUser_badge(userBadge);
    }

    public List<Log> getAllLogs() {
        return actionLogsRepository.findAll();
    }

    public Log getLogById(String id) {
        Log log = actionLogsRepository.findById(id);
        if (log == null) {
            throw new IllegalArgumentException("Log not found");
        }
        return log;
    }

    public List<Log> getLogsAfterTimestamp(Long timestamp) {
        return actionLogsRepository.findByTimestampGreaterThan(timestamp);
    }

    //To store logs

    public void saveLoginLog(String userBadge, String userName) {
        Log log = new Log();
        log.setUser_badge(userBadge);
        log.setUser_name(userName);
        log.setAction(Actions.Login);
        actionLogsRepository.save(log);
    }

    public void saveLogoutLog(String userBadge, String userName) {
        Log log = new Log();
        log.setUser_badge(userBadge);
        log.setUser_name(userName);
        log.setAction(Actions.Logout);
        actionLogsRepository.save(log);
    }

    public void saveActionLog(String userBadge, String userName, Actions action) {
        Log log = new Log();
        log.setUser_badge(userBadge);
        log.setUser_name(userName);
        log.setAction(action);
        actionLogsRepository.save(log);
    }

    public void saveAccessLog(String userBadge, String userName, UUID logAccessed) {
        Log log = new Log();
        log.setUser_badge(userBadge);
        log.setUser_name(userName);
        log.setAction(Actions.Access_logs);
        log.setLog_accessed(logAccessed);
        actionLogsRepository.save(log);
    }

}
