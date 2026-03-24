package com.pokertrack.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Entry {
    private Long id;
    private Long dailySessionId;
    private Long memberUserId; // References users.id (group member)
    private BigDecimal buyInTotal;
    private BigDecimal cashOutTotal;
    private BigDecimal profit; // Computed in DB (GENERATED ALWAYS AS), do NOT set manually
    private String note;

    // Settlement
    private String settlementStatus; // UNPAID | PAID | PARTIAL | DISPUTED
    private BigDecimal settledAmount;
    private String settlementNote;
    private Long settledBy;
    private OffsetDateTime settledAt;

    // Audit
    private Long createdBy;
    private Long updatedBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDailySessionId() {
        return dailySessionId;
    }

    public void setDailySessionId(Long dailySessionId) {
        this.dailySessionId = dailySessionId;
    }

    public Long getMemberUserId() {
        return memberUserId;
    }

    public void setMemberUserId(Long memberUserId) {
        this.memberUserId = memberUserId;
    }

    public BigDecimal getBuyInTotal() {
        return buyInTotal;
    }

    public void setBuyInTotal(BigDecimal buyInTotal) {
        this.buyInTotal = buyInTotal;
    }

    public BigDecimal getCashOutTotal() {
        return cashOutTotal;
    }

    public void setCashOutTotal(BigDecimal cashOutTotal) {
        this.cashOutTotal = cashOutTotal;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    public BigDecimal getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(BigDecimal settledAmount) {
        this.settledAmount = settledAmount;
    }

    public String getSettlementNote() {
        return settlementNote;
    }

    public void setSettlementNote(String settlementNote) {
        this.settlementNote = settlementNote;
    }

    public Long getSettledBy() {
        return settledBy;
    }

    public void setSettledBy(Long settledBy) {
        this.settledBy = settledBy;
    }

    public OffsetDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(OffsetDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
