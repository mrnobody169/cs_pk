package com.pokertrack.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DayReportRow {
    private LocalDate sessionDate;
    private Long entryCount;
    private BigDecimal totalBuyIn;
    private BigDecimal totalCashOut;
    private BigDecimal totalProfit;
    private Long unpaidCount;

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Long getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(Long entryCount) {
        this.entryCount = entryCount;
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
