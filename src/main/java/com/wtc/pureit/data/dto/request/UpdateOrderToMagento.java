package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonRootName("data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateOrderToMagento {

    @JsonProperty(value = "entity_id")
    private String entityId;

    @JsonProperty(value = "rfs_id")
    private String rfsId;

    @JsonProperty(value = "netsuite_invoice_url")
    private String invoiceUrl;

    @JsonProperty(value = "netsuite_customer_id")
    private String netsuiteCustomerId;

    @JsonIgnore
    private Boolean isInvoiceCreationSuccess;
}
