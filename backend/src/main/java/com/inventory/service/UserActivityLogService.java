package com.inventory.service;

import com.inventory.entity.User;
import com.inventory.entity.UserActivityLog;
import com.inventory.repository.UserActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserActivityLogService {

    private final UserActivityLogRepository logRepository;

    @Autowired
    public UserActivityLogService(UserActivityLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void log(User user, String action, String entityType, Long entityId, String details) {
        UserActivityLog entry = new UserActivityLog();
        entry.setUser(user);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        logRepository.save(entry);
    }

    public List<UserActivityLog> getAllLogs() {
        return logRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<UserActivityLog> getLogsForUser(Long userId) {
        return logRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}