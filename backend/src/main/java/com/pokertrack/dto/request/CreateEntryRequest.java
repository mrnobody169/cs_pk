package com.pokertrack.dto.request;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateEntryRequest {
    @NotNull
    private Long memberUserId;

    @NotNull
    @DecimalMin("0")
    private BigDecimal buyInTotal;

    @NotNull
    @DecimalMin("0")
    private BigDecimal cashOutTotal;

    private String note;

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
