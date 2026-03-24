package com.pokertrack.dto.response;

import java.math.BigDecimal;

/**
 * Profit summary per member across groups.
 */
public class MemberProfitRow {
    private Long memberUserId;
    private String memberName;
    private Long daysPlayed;
    private BigDecimal totalBuyIn;
    private BigDecimal totalCashOut;
    private BigDecimal totalProfit;
    private Long unpaidCount;

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

    public Long getDaysPlayed() {
        return daysPlayed;
    }

    public void setDaysPlayed(Long daysPlayed) {
        this.daysPlayed = daysPlayed;
    }

    public BigDecimal getTotalBuyIn() {
        return totalBuyIn;
    }

    public void setTotalBuyIn(BigDecimal totalBuyIn) {
        this.totalBuyIn = totalBuyIn;
    }

    public BigDecimal getTotalCashOut() {
        return totalCashOut;
    }

    public void setTotalCashOut(BigDecimal totalCashOut) {
        this.totalCashOut = totalCashOut;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Long getUnpaidCount() {
        return unpaidCount;
    }

    public void setUnpaidCount(Long unpaidCount) {
        this.unpaidCount = unpaidCount;
    }
}
