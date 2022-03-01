package com.wtc.pureit.data.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RfsCreationResponse {

    @JsonProperty(value = "message")
    private String message;

    @JsonProperty(value = "responseCode")
    private String responseCode;

    @JsonProperty(value = "transaction_id")
    private String transactionId;

}
