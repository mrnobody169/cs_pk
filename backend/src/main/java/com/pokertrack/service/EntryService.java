package com.pokertrack.service;

import com.pokertrack.dto.request.CreateEntryRequest;
import com.pokertrack.dto.request.UpdateEntryRequest;
import com.pokertrack.dto.response.EntryView;
import com.pokertrack.exception.ConflictException;
import com.pokertrack.exception.ForbiddenException;
import com.pokertrack.exception.ResourceNotFoundException;
import com.pokertrack.exception.SessionLockedException;
import com.pokertrack.mapper.DailySessionMapper;
import com.pokertrack.mapper.EntryMapper;
import com.pokertrack.mapper.UserMapper;
import com.pokertrack.model.DailySession;
import com.pokertrack.model.Entry;
import com.pokertrack.model.User;
import com.pokertrack.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EntryService {

    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private DailySessionMapper sessionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private CurrentUserProvider currentUser;

    @Value("${app.member-can-edit-any-entry:true}")
    private boolean memberCanEditAnyEntry;

    public List<EntryView> listEntries(Long sessionId) {
        DailySession session = requireSession(sessionId);
        requireGroupAccess(session.getGroupId());
        return entryMapper.findViewsBySessionId(sessionId);
    }

    public List<EntryView> listUnpaid(Long sessionId) {
        DailySession session = requireSession(sessionId);
        requireGroupAccess(session.getGroupId());
        return entryMapper.findUnpaidBySessionId(sessionId);
    }

    @Transactional
    public EntryView createEntry(Long sessionId, CreateEntryRequest req) {
        DailySession session = requireSession(sessionId);
        Long groupId = session.getGroupId();
        requireSystemAdmin();

        // Business Rule: block creation when session is locked
        if (Boolean.TRUE.equals(session.getDataLocked())) {
            throw new SessionLockedException("Cannot add entries to a locked session");
        }

        // Business Rule: member must be a registered user
        User member = userMapper.findById(req.getMemberUserId());
        if (member == null) {
            throw new ResourceNotFoundException("User not found with id: " + req.getMemberUserId());
        }

        // Business Rule: prevent duplicate entry for same member+session
        Entry existing = entryMapper.findBySessionAndMember(sessionId, req.getMemberUserId());
        if (existing != null) {
            throw new ConflictException("Member already has an entry for this session");
        }

        Long actorId = currentUser.getCurrentUserId();
        Entry entry = new Entry();
        entry.setDailySessionId(sessionId);
        entry.setMemberUserId(req.getMemberUserId());
        entry.setBuyInTotal(req.getBuyInTotal());
        entry.setCashOutTotal(req.getCashOutTotal());
        entry.setNote(req.getNote());
        entry.setSettlementStatus("UNPAID");
        entry.setCreatedBy(actorId);

        entryMapper.insert(entry);

        // Audit
        auditLogService.log(groupId, "ENTRY", entry.getId(), "CREATE", null, entry, actorId);

        // Return the view (re-fetch with joins)
        return entryMapper.findViewsBySessionId(sessionId)
                .stream().filter(v -> v.getId().equals(entry.getId())).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Entry", entry.getId()));
    }

    @Transactional
    public EntryView updateEntry(Long entryId, UpdateEntryRequest req) {
        Entry entry = requireEntry(entryId);
        DailySession session = requireSession(entry.getDailySessionId());
        Long groupId = session.getGroupId();

        // Authorization: only system admin can edit
        requireSystemAdmin();

        // Business Rule: block when locked
        if (Boolean.TRUE.equals(session.getDataLocked())) {
            throw new SessionLockedException();
        }

        Long actorId = currentUser.getCurrentUserId();
        Entry before = copyEntry(entry);

        if (req.getBuyInTotal() != null)
            entry.setBuyInTotal(req.getBuyInTotal());
        if (req.getCashOutTotal() != null)
            entry.setCashOutTotal(req.getCashOutTotal());
        if (req.getNote() != null)
            entry.setNote(req.getNote());
        entry.setUpdatedBy(actorId);

        entryMapper.update(entry);

        auditLogService.log(groupId, "ENTRY", entryId, "UPDATE", before, entry, actorId);

        return entryMapper.findViewsBySessionId(entry.getDailySessionId())
                .stream().filter(v -> v.getId().equals(entryId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Entry", entryId));
    }

    @Transactional
    public void deleteEntry(Long entryId) {
        Entry entry = requireEntry(entryId);
        DailySession session = requireSession(entry.getDailySessionId());
        Long groupId = session.getGroupId();

        requireSystemAdmin();

        if (Boolean.TRUE.equals(session.getDataLocked())) {
            throw new SessionLockedException("Cannot delete entries from a locked session");
        }

        Long actorId = currentUser.getCurrentUserId();
        auditLogService.log(groupId, "ENTRY", entryId, "DELETE", entry, null, actorId);
        entryMapper.delete(entryId);
    }

    // ---- Helpers ----

    private DailySession requireSession(Long sessionId) {
        DailySession s = sessionMapper.findById(sessionId);
        if (s == null)
            throw new ResourceNotFoundException("Session", sessionId);
        return s;
    }

    private Entry requireEntry(Long entryId) {
        Entry e = entryMapper.findById(entryId);
        if (e == null)
            throw new ResourceNotFoundException("Entry", entryId);
        return e;
    }

    private void requireGroupAccess(Long groupId) {
        if (!currentUser.hasAccessTo(groupId))
            throw new ForbiddenException("Access denied to this group");
    }

    private void requireSystemAdmin() {
        if (!currentUser.isSystemAdmin())
            throw new ForbiddenException("Chỉ admin mới có quyền thực hiện");
    }

    private Entry copyEntry(Entry e) {
        Entry copy = new Entry();
        copy.setId(e.getId());
        copy.setDailySessionId(e.getDailySessionId());
        copy.setMemberUserId(e.getMemberUserId());
        copy.setBuyInTotal(e.getBuyInTotal());
        copy.setCashOutTotal(e.getCashOutTotal());
        copy.setProfit(e.getProfit());
        copy.setNote(e.getNote());
        copy.setSettlementStatus(e.getSettlementStatus());
        copy.setSettledAmount(e.getSettledAmount());
        copy.setSettlementNote(e.getSettlementNote());
        return copy;
    }
}
