package com.pokertrack.dto.request;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class UpdateEntryRequest {
    @DecimalMin("0")
    private BigDecimal buyInTotal;

    @DecimalMin("0")
    private BigDecimal cashOutTotal;

    private String note;

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
