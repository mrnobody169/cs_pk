package com.pokertrack.service;

import com.pokertrack.exception.ForbiddenException;
import com.pokertrack.exception.ResourceNotFoundException;
import com.pokertrack.mapper.DailySessionMapper;
import com.pokertrack.model.DailySession;
import com.pokertrack.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DailySessionService {

    @Autowired
    private DailySessionMapper sessionMapper;
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private CurrentUserProvider currentUser;

    /**
     * Creates the session for the given date if it doesn't exist yet.
     * Idempotent: returns existing session if already there.
     */
    @Transactional
    public DailySession getOrCreate(Long groupId, LocalDate sessionDate) {
        requireSystemAdmin();
        DailySession existing = sessionMapper.findByGroupAndDate(groupId, sessionDate);
        if (existing != null)
            return existing;

        DailySession session = new DailySession();
        session.setGroupId(groupId);
        session.setSessionDate(sessionDate);
        session.setDataLocked(false);
        session.setCreatedBy(currentUser.getCurrentUserId());
        sessionMapper.insert(session);
        return session;
    }

    public List<DailySession> listSessions(Long groupId, LocalDate from, LocalDate to) {
        requireAccess(groupId);
        return sessionMapper.findByGroupIdAndDateRange(groupId, from, to);
    }

    public DailySession getSession(Long sessionId) {
        DailySession session = sessionMapper.findById(sessionId);
        if (session == null)
            throw new ResourceNotFoundException("Session", sessionId);
        requireAccess(session.getGroupId());
        return session;
    }

    @Transactional
    public DailySession lockSession(Long sessionId, boolean lock) {
        DailySession session = sessionMapper.findById(sessionId);
        if (session == null)
            throw new ResourceNotFoundException("Session", sessionId);
        Long groupId = session.getGroupId();
        requireSystemAdmin();

        Long actorId = currentUser.getCurrentUserId();
        session.setDataLocked(lock);

        if (lock) {
            sessionMapper.updateLock(sessionId, true, actorId, OffsetDateTime.now());
        } else {
            sessionMapper.updateLock(sessionId, false, null, null);
        }

        auditLogService.log(groupId, "SESSION", sessionId, "LOCK",
                java.util.Collections.singletonMap("dataLocked", !lock),
                java.util.Collections.singletonMap("dataLocked", lock),
                actorId);

        return sessionMapper.findById(sessionId);
    }

    private void requireAccess(Long groupId) {
        if (!currentUser.hasAccessTo(groupId))
            throw new ForbiddenException("Access denied to this group");
    }

    private void requireSystemAdmin() {
        if (!currentUser.isSystemAdmin())
            throw new ForbiddenException("Chỉ admin mới có quyền thực hiện");
    }
}
