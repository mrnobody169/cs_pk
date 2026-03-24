package com.pokertrack.service;

import com.pokertrack.dto.request.BulkSettlementRequest;
import com.pokertrack.dto.request.SettlementRequest;
import com.pokertrack.exception.ForbiddenException;
import com.pokertrack.exception.ResourceNotFoundException;
import com.pokertrack.mapper.DailySessionMapper;
import com.pokertrack.mapper.EntryMapper;
import com.pokertrack.model.DailySession;
import com.pokertrack.model.Entry;
import com.pokertrack.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class SettlementService {

    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private DailySessionMapper sessionMapper;
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private CurrentUserProvider currentUser;

    @Transactional
    public Entry settle(Long entryId, SettlementRequest req) {
        Entry entry = requireEntry(entryId);
        DailySession session = requireSession(entry.getDailySessionId());
        Long groupId = session.getGroupId();

        // Settlement is ADMIN-only
        if (!currentUser.isAdminOf(groupId)) {
            throw new ForbiddenException("Only ADMIN can update settlement");
        }

        Entry before = copySettlement(entry);
        applySettlementRules(entry, req.getStatus(), req.getSettledAmount(), req.getSettlementNote());
        entry.setUpdatedBy(currentUser.getCurrentUserId());

        entryMapper.updateSettlement(entry);
        auditLogService.log(groupId, "ENTRY", entryId, "SETTLEMENT", before, copySettlement(entry),
                currentUser.getCurrentUserId());

        return entryMapper.findById(entryId);
    }

    @Transactional
    public void bulkSettle(Long sessionId, BulkSettlementRequest req) {
        DailySession session = requireSession(sessionId);
        Long groupId = session.getGroupId();

        if (!currentUser.isAdminOf(groupId)) {
            throw new ForbiddenException("Only ADMIN can update settlement");
        }

        if (req.getEntryIds() == null || req.getEntryIds().isEmpty()) {
            throw new IllegalArgumentException("entryIds must not be empty");
        }

        Long actorId = currentUser.getCurrentUserId();

        for (Long entryId : req.getEntryIds()) {
            Entry entry = entryMapper.findById(entryId);
            if (entry == null)
                continue;

            // Security: ensure the entry belongs to this session
            if (!entry.getDailySessionId().equals(sessionId)) {
                throw new ForbiddenException("Entry " + entryId + " does not belong to this session");
            }

            Entry before = copySettlement(entry);
            applySettlementRules(entry, req.getStatus(), req.getSettledAmount(), req.getSettlementNote());
            entry.setUpdatedBy(actorId);

            entryMapper.updateSettlement(entry);
            auditLogService.log(groupId, "ENTRY", entryId, "SETTLEMENT", before, copySettlement(entry), actorId);
        }
    }

    /**
     * Applies all business rules for settlement status transitions.
     * Business Rules (from spec):
     * - UNPAID: clear settled_by, settled_at, settled_amount, settlement_note
     * - PAID: set settled_by=actor, settled_at=now; settledAmount defaults to
     * ABS(profit); must be <= ABS(profit)
     * - PARTIAL: settledAmount required, > 0, and <= ABS(profit)
     * - DISPUTED: settlementNote required (non-empty)
     */
    private void applySettlementRules(Entry entry, String status, BigDecimal settledAmount, String settlementNote) {
        BigDecimal absProfit = entry.getProfit() != null
                ? entry.getProfit().abs()
                : BigDecimal.ZERO;

        switch (status) {
            case "UNPAID":
                entry.setSettlementStatus("UNPAID");
                entry.setSettledAmount(null);
                entry.setSettlementNote(null);
                entry.setSettledBy(null);
                entry.setSettledAt(null);
                break;

            case "PAID":
                BigDecimal amount = (settledAmount != null) ? settledAmount : absProfit;
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("settledAmount must be >= 0");
                }
                if (amount.compareTo(absProfit) > 0) {
                    throw new IllegalArgumentException("settledAmount cannot exceed ABS(profit): " + absProfit);
                }
                entry.setSettlementStatus("PAID");
                entry.setSettledAmount(amount);
                entry.setSettledBy(currentUser.getCurrentUserId());
                entry.setSettledAt(OffsetDateTime.now());
                entry.setSettlementNote(settlementNote);
                break;

            case "PARTIAL":
                if (settledAmount == null || settledAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("settledAmount is required and must be > 0 for PARTIAL status");
                }
                if (settledAmount.compareTo(absProfit) > 0) {
                    throw new IllegalArgumentException("settledAmount cannot exceed ABS(profit): " + absProfit);
                }
                entry.setSettlementStatus("PARTIAL");
                entry.setSettledAmount(settledAmount);
                entry.setSettlementNote(settlementNote);
                entry.setSettledBy(null);
                entry.setSettledAt(null);
                break;

            case "DISPUTED":
                if (settlementNote == null || settlementNote.trim().isEmpty()) {
                    throw new IllegalArgumentException("settlementNote is required for DISPUTED status");
                }
                entry.setSettlementStatus("DISPUTED");
                entry.setSettlementNote(settlementNote);
                entry.setSettledAmount(null);
                entry.setSettledBy(null);
                entry.setSettledAt(null);
                break;

            default:
                throw new IllegalArgumentException("Invalid settlement status: " + status);
        }
    }

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

    private Entry copySettlement(Entry e) {
        Entry copy = new Entry();
        copy.setId(e.getId());
        copy.setSettlementStatus(e.getSettlementStatus());
        copy.setSettledAmount(e.getSettledAmount());
        copy.setSettlementNote(e.getSettlementNote());
        copy.setSettledBy(e.getSettledBy());
        copy.setSettledAt(e.getSettledAt());
        return copy;
    }
}
