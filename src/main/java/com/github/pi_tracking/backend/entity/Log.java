package com.github.pi_tracking.backend.entity;

import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.annotation.CreatedDate;

import com.github.pi_tracking.backend.utils.Actions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "action_logs")  
public class Log {

    @Id
    private String id;  

    @Field("userBadge")
    private String userBadge;

    @Field("user_name")
    private String userName;  

    private Actions action;
    private UUID logAccessed;

    @CreatedDate
    private LocalDateTime timestamp;  
}
