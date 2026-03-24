package com.pokertrack.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public class SettlementRequest {
    /**
     * Required: one of UNPAID | PAID | PARTIAL | DISPUTED
     */
    @NotBlank
    @Pattern(regexp = "^(UNPAID|PAID|PARTIAL|DISPUTED)$", message = "status must be UNPAID, PAID, PARTIAL, or DISPUTED")
    private String status;

    /** Required if status = PARTIAL; must be > 0 and <= ABS(profit) */
    private BigDecimal settledAmount;

    /** Required if status = DISPUTED */
    private String settlementNote;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
