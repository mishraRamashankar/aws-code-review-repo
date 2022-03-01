package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceCreationRequest {

    @JsonProperty(value = "rfsId", access = Access.READ_ONLY)
    private String rfsId;

    public static InvoiceCreationRequest creationRequest(String transactionId) {
        return InvoiceCreationRequest.builder()
                .rfsId(transactionId)
                .build();
    }
}
