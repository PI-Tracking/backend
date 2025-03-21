package com.github.pi_tracking.backend.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import com.github.pi_tracking.backend.entity.Log;

public interface ActionLogsRepository extends MongoRepository<Log, ObjectId> {
    List<Log> findByuserBadge(String user_badge);
    Log findById(String id);
    List<Log> findAll();
    List<Log> findByTimestampGreaterThan(long timestamp);
}

