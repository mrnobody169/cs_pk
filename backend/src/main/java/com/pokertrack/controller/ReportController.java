package com.pokertrack.controller;

import com.pokertrack.dto.response.*;
import com.pokertrack.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Reports", description = "Báo cáo tổng hợp, theo player và theo ngày")
@RestController
@RequestMapping("/api")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // ---- Existing per-group reports ----

    @GetMapping("/groups/{groupId}/reports/summary")
    public ResponseEntity<ReportSummaryResponse> getSummary(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getSummary(groupId, from, to));
    }

    @GetMapping("/groups/{groupId}/reports/by-player")
    public ResponseEntity<List<PlayerReportRow>> getByPlayer(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getByPlayer(groupId, from, to));
    }

    @GetMapping("/groups/{groupId}/reports/by-day")
    public ResponseEntity<List<DayReportRow>> getByDay(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getByDay(groupId, from, to));
    }

    // ---- Dashboard: member profit ----

    @GetMapping("/dashboard/member-profit")
    public ResponseEntity<List<MemberProfitRow>> getMemberProfit(
            @RequestParam(required = false) Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getMemberProfit(groupId, from, to));
    }

    // ---- Transaction tracking log ----

    @GetMapping("/dashboard/transactions")
    public ResponseEntity<List<TransactionRow>> getTransactions(
            @RequestParam(required = false) Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getTransactions(groupId, from, to));
    }
}
