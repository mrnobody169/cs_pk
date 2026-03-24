package com.pokertrack.controller;

import com.pokertrack.dto.request.BulkEntryRequest;
import com.pokertrack.dto.request.BulkSettlementRequest;
import com.pokertrack.dto.request.CreateEntryRequest;
import com.pokertrack.dto.request.SettlementRequest;
import com.pokertrack.dto.request.UpdateEntryRequest;
import com.pokertrack.dto.response.EntryView;
import com.pokertrack.model.Entry;
import com.pokertrack.service.EntryService;
import com.pokertrack.service.SettlementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "Entries & Settlement", description = "Ghi chép dòng tiền (buy-in/cash-out) và cập nhật thanh toán")
@RestController
@RequestMapping("/api")
public class EntryController {

    @Autowired
    private EntryService entryService;

    @Autowired
    private SettlementService settlementService;

    // --- Entries ---

    @GetMapping("/sessions/{sessionId}/entries")
    public ResponseEntity<List<EntryView>> listEntries(@PathVariable Long sessionId) {
        return ResponseEntity.ok(entryService.listEntries(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/entries")
    public ResponseEntity<EntryView> createEntry(@PathVariable Long sessionId,
            @Valid @RequestBody CreateEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(entryService.createEntry(sessionId, request));
    }

    @PatchMapping("/entries/{entryId}")
    public ResponseEntity<EntryView> updateEntry(@PathVariable Long entryId,
            @Valid @RequestBody UpdateEntryRequest request) {
        return ResponseEntity.ok(entryService.updateEntry(entryId, request));
    }

    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long entryId) {
        entryService.deleteEntry(entryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sessions/{sessionId}/entries/bulk")
    public ResponseEntity<List<EntryView>> bulkCreateEntries(@PathVariable Long sessionId,
            @Valid @RequestBody BulkEntryRequest request) {
        List<EntryView> results = new java.util.ArrayList<>();
        for (CreateEntryRequest entry : request.getEntries()) {
            results.add(entryService.createEntry(sessionId, entry));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    // --- Settlement ---

    @PostMapping("/entries/{entryId}/settlement")
    public ResponseEntity<Entry> settle(@PathVariable Long entryId,
            @Valid @RequestBody SettlementRequest request) {
        return ResponseEntity.ok(settlementService.settle(entryId, request));
    }

    @PostMapping("/sessions/{sessionId}/settlement/bulk")
    public ResponseEntity<Void> bulkSettle(@PathVariable Long sessionId,
            @Valid @RequestBody BulkSettlementRequest request) {
        settlementService.bulkSettle(sessionId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions/{sessionId}/unpaid")
    public ResponseEntity<List<EntryView>> getUnpaid(@PathVariable Long sessionId) {
        return ResponseEntity.ok(entryService.listUnpaid(sessionId));
    }
}
