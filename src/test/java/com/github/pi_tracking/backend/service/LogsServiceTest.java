package com.github.pi_tracking.backend.service;

import com.github.pi_tracking.backend.entity.Log;
import com.github.pi_tracking.backend.repository.ActionLogsRepository;
import com.github.pi_tracking.backend.utils.Actions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogsServiceTest {

    @Mock
    private ActionLogsRepository actionLogsRepository;

    private LogsService logsService;

    @BeforeEach
    void setUp() {
        logsService = new LogsService(actionLogsRepository);
    }

    @Test
    void createLog_ShouldSaveLog() {
        Log log = Log.builder()
                .userBadge("123")
                .userName("testuser")
                .action(Actions.Login)
                .build();

        when(actionLogsRepository.save(any(Log.class))).thenReturn(log);

        Log result = logsService.createLog(log);

        assertNotNull(result);
        assertEquals(log.getUserBadge(), result.getUserBadge());
        assertEquals(log.getUserName(), result.getUserName());
        assertEquals(log.getAction(), result.getAction());
        verify(actionLogsRepository).save(log);
    }

    @Test
    void getLogsByUserBadge_ShouldReturnUserLogs() {
        String userBadge = "123";
        List<Log> expectedLogs = Arrays.asList(
            Log.builder().userBadge(userBadge).action(Actions.Login).build(),
            Log.builder().userBadge(userBadge).action(Actions.Logout).build()
        );
        when(actionLogsRepository.findByuserBadge(userBadge)).thenReturn(expectedLogs);

        List<Log> result = logsService.getLogsByUserBadge(userBadge);

        assertEquals(expectedLogs.size(), result.size());
        verify(actionLogsRepository).findByuserBadge(userBadge);
    }

    @Test
    void getAllLogs_ShouldReturnAllLogs() {
        List<Log> expectedLogs = Arrays.asList(
            Log.builder().userBadge("123").action(Actions.Login).build(),
            Log.builder().userBadge("456").action(Actions.Logout).build()
        );
        when(actionLogsRepository.findAll()).thenReturn(expectedLogs);

        List<Log> result = logsService.getAllLogs();

        assertEquals(expectedLogs.size(), result.size());
        verify(actionLogsRepository).findAll();
    }

    @Test
    void getLogById_WithExistingLog_ShouldReturnLog() {
        String logId = "123";
        Log expectedLog = Log.builder()
                .id(logId)
                .userBadge("123")
                .action(Actions.Login)
                .build();
        when(actionLogsRepository.findById(logId)).thenReturn(expectedLog);

        Log result = logsService.getLogById(logId);

        assertNotNull(result);
        assertEquals(logId, result.getId());
        verify(actionLogsRepository).findById(logId);
    }

    @Test
    void getLogById_WithNonExistingLog_ShouldThrowException() {
        String logId = "123";
        when(actionLogsRepository.findById(logId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> logsService.getLogById(logId));
    }

    @Test
    void saveLoginLog_ShouldSaveLoginLog() {
        String userBadge = "123";
        String userName = "testuser";

        logsService.saveLoginLog(userBadge, userName);

        verify(actionLogsRepository).save(argThat(log ->
            log.getUserBadge().equals(userBadge) &&
            log.getUserName().equals(userName) &&
            log.getAction() == Actions.Login
        ));
    }

    @Test
    void saveLogoutLog_ShouldSaveLogoutLog() {
        String userBadge = "123";
        String userName = "testuser";

        logsService.saveLogoutLog(userBadge, userName);

        verify(actionLogsRepository).save(argThat(log ->
            log.getUserBadge().equals(userBadge) &&
            log.getUserName().equals(userName) &&
            log.getAction() == Actions.Logout
        ));
    }

    @Test
    void saveActionLog_ShouldSaveActionLog() {
        String userBadge = "123";
        String userName = "testuser";
        Actions action = Actions.Access_logs;

        logsService.saveActionLog(userBadge, userName, action);

        verify(actionLogsRepository).save(argThat(log ->
            log.getUserBadge().equals(userBadge) &&
            log.getUserName().equals(userName) &&
            log.getAction() == action
        ));
    }

    @Test
    void saveAccessLog_ShouldSaveAccessLog() {
        String userBadge = "123";
        String userName = "testuser";
        UUID logAccessed = UUID.randomUUID();

        logsService.saveAccessLog(userBadge, userName, logAccessed);

        verify(actionLogsRepository).save(argThat(log ->
            log.getUserBadge().equals(userBadge) &&
            log.getUserName().equals(userName) &&
            log.getAction() == Actions.Access_logs &&
            log.getLogAccessed().equals(logAccessed)
        ));
    }
} 