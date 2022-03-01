package com.wtc.pureit.data.dto.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "links")
public class LspInventoryLinker {

    private String sourceCode;

    private String stockId;

    private Integer priority;

    //<editor-fold desc="getter/s">
    @JsonProperty(value = "source_code", access = Access.READ_ONLY)
    public String getSourceCode() {
        return sourceCode;
    }

    @JsonProperty(value = "stock_id", access = Access.READ_ONLY)
    public String getStockId() {
        return stockId;
    }

    @JsonProperty(value = "priority", access = Access.READ_ONLY)
    public Integer getPriority() {
        return priority;
    }
    //</editor-fold>
}
