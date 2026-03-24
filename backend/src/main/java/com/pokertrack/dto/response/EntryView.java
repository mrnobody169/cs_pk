package com.pokertrack.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Full entry view returned by GET /api/sessions/{sessionId}/entries.
 * Joins member display_name and updated-by/settled-by display names.
 */
public class EntryView {
    private Long id;
    private Long dailySessionId;
    private Long memberUserId;
    private String memberName; // users.display_name

    private BigDecimal buyInTotal;
    private BigDecimal cashOutTotal;
    private BigDecimal profit;
    private String note;

    private String settlementStatus;
    private BigDecimal settledAmount;
    private String settlementNote;
    private OffsetDateTime settledAt;
    private String settledByName;

    private OffsetDateTime updatedAt;
    private String updatedByName;

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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
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

    public OffsetDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(OffsetDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public String getSettledByName() {
        return settledByName;
    }

    public void setSettledByName(String settledByName) {
        this.settledByName = settledByName;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }
}
