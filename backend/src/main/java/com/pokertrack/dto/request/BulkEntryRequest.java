package com.pokertrack.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class BulkEntryRequest {
    @NotEmpty
    @Valid
    private List<CreateEntryRequest> entries;

    public List<CreateEntryRequest> getEntries() {
        return entries;
    }

    public void setEntries(List<CreateEntryRequest> entries) {
        this.entries = entries;
    }
}
