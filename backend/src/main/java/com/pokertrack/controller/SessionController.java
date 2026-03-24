package com.pokertrack.controller;

import com.pokertrack.model.DailySession;
import com.pokertrack.service.DailySessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Daily Sessions", description = "Tạo và quản lý phiên chơi theo ngày, lock/unlock session")
@RestController
@RequestMapping("/api")
public class SessionController {

    @Autowired
    private DailySessionService sessionService;

    @PostMapping("/groups/{groupId}/sessions")
    public ResponseEntity<DailySession> getOrCreateSession(
            @PathVariable Long groupId,
            @RequestBody Map<String, String> body) {
        LocalDate date = LocalDate.parse(body.getOrDefault("sessionDate", LocalDate.now().toString()));
        return ResponseEntity.ok(sessionService.getOrCreate(groupId, date));
    }

    @GetMapping("/groups/{groupId}/sessions")
    public ResponseEntity<List<DailySession>> listSessions(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(sessionService.listSessions(groupId, from, to));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<DailySession> getSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getSession(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/lock")
    public ResponseEntity<DailySession> lockSession(@PathVariable Long sessionId,
            @RequestBody Map<String, Boolean> body) {
        Boolean lock = body.get("locked");
        if (lock == null)
            throw new IllegalArgumentException("'locked' field is required");
        return ResponseEntity.ok(sessionService.lockSession(sessionId, lock));
    }
}
