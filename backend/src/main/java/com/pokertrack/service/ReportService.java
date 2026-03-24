package com.pokertrack.service;

import com.pokertrack.dto.response.*;
import com.pokertrack.exception.ForbiddenException;
import com.pokertrack.mapper.ReportMapper;
import com.pokertrack.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private CurrentUserProvider currentUser;

    public ReportSummaryResponse getSummary(Long groupId, LocalDate from, LocalDate to) {
        requireAccess(groupId);
        return reportMapper.getSummary(groupId, from, to);
    }

    public List<PlayerReportRow> getByPlayer(Long groupId, LocalDate from, LocalDate to) {
        requireAccess(groupId);
        return reportMapper.getByPlayer(groupId, from, to);
    }

    public List<DayReportRow> getByDay(Long groupId, LocalDate from, LocalDate to) {
        requireAccess(groupId);
        return reportMapper.getByDay(groupId, from, to);
    }

    // ---- Dashboard & Transaction Log ----

    public List<MemberProfitRow> getMemberProfit(Long groupId, LocalDate from, LocalDate to) {
        if (groupId != null) {
            requireAccess(groupId);
            return reportMapper.getMemberProfitByGroup(groupId, from, to);
        }
        return reportMapper.getMemberProfitAllGroups(currentUser.getCurrentUserId(), from, to);
    }

    public List<TransactionRow> getTransactions(Long groupId, LocalDate from, LocalDate to) {
        if (groupId != null) {
            requireAccess(groupId);
            return reportMapper.getTransactionsByGroup(groupId, from, to);
        }
        return reportMapper.getTransactionsAllGroups(currentUser.getCurrentUserId(), from, to);
    }

    private void requireAccess(Long groupId) {
        if (!currentUser.hasAccessTo(groupId))
            throw new ForbiddenException("Access denied to this group");
    }
}
