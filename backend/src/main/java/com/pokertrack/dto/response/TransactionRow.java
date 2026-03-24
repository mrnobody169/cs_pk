package com.pokertrack.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row in the transaction log — represents a single entry with session date,
 * group name, member name, and payment status.
 */
public class TransactionRow {
    private Long entryId;
    private LocalDate sessionDate;
    private Long groupId;
    private String groupName;
    private Long memberUserId;
    private String memberName;
    private BigDecimal buyInTotal;
    private BigDecimal cashOutTotal;
    private BigDecimal profit;
    private String settlementStatus;
    private String note;

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
