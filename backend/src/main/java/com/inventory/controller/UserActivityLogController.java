package com.inventory.controller;

import com.inventory.entity.UserActivityLog;
import com.inventory.service.UserActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@PreAuthorize("hasRole('ADMIN')")
public class UserActivityLogController {

    private final UserActivityLogService userActivityLogService;

    @Autowired
    public UserActivityLogController(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }

    @GetMapping
    public ResponseEntity<List<UserActivityLog>> getAllLogs() {
        return ResponseEntity.ok(userActivityLogService.getAllLogs());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserActivityLog>> getLogsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userActivityLogService.getLogsForUser(userId));
    }
}