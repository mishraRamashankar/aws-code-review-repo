package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public class OrderCancellationRequest {

    @JsonProperty(value = "rfsId")
    String rfsId;
}
