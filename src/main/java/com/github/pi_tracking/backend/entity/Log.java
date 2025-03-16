package com.github.pi_tracking.backend.entity;

import java.util.UUID;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.github.pi_tracking.backend.utils.Actions;


@Document(collection = "action_logs")  
public class Log {

    @Id
    private String id;
    private String user_badge;
    private String user_name;
    private Actions action;
    private UUID log_accessed;
    
    @CreationTimestamp
    private LocalDateTime timestamp;

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_badge() {
        return user_badge;
    }

    public void setUser_badge(String user_badge) {
        this.user_badge = user_badge;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public Actions getAction() {
        return action;
    }

    public void setAction(Actions action) {
        this.action = action;
    }

    public UUID getLog_accessed() {
        return log_accessed;
    }

    public void setLog_accessed(UUID log_accessed) {
        this.log_accessed = log_accessed;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    


}
