package com.pokertrack.dto.response;

import java.math.BigDecimal;

public class PlayerReportRow {
    private Long playerId;
    private String playerName;
    private String playerAlias;
    private Long daysPlayed;
    private BigDecimal totalBuyIn;
    private BigDecimal totalCashOut;
    private BigDecimal totalProfit;
    private Long unpaidCount;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerAlias() {
        return playerAlias;
    }

    public void setPlayerAlias(String playerAlias) {
        this.playerAlias = playerAlias;
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
