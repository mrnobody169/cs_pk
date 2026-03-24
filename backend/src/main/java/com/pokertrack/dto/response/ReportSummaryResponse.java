package com.pokertrack.dto.response;

import java.math.BigDecimal;

public class ReportSummaryResponse {
    private BigDecimal totalBuyIn;
    private BigDecimal totalCashOut;
    private BigDecimal totalProfit;
    private Long totalEntries;
    private Long unpaidCount;
    private Long paidCount;
    private Long partialCount;
    private Long disputedCount;

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

    public Long getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(Long totalEntries) {
        this.totalEntries = totalEntries;
    }

    public Long getUnpaidCount() {
        return unpaidCount;
    }

    public void setUnpaidCount(Long unpaidCount) {
        this.unpaidCount = unpaidCount;
    }

    public Long getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(Long paidCount) {
        this.paidCount = paidCount;
    }

    public Long getPartialCount() {
        return partialCount;
    }

    public void setPartialCount(Long partialCount) {
        this.partialCount = partialCount;
    }

    public Long getDisputedCount() {
        return disputedCount;
    }

    public void setDisputedCount(Long disputedCount) {
        this.disputedCount = disputedCount;
    }
}
