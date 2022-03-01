package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    String rfsId;

    String status;

    @JsonProperty(value = "rfsId", access = Access.READ_ONLY)
    public String getRfsId() {
        return rfsId;
    }

    @JsonProperty(value = "status", access = Access.READ_ONLY)
    public String getStatus() {
        return status;
    }

    @JsonProperty(value = "rfsId", access = Access.WRITE_ONLY)
    public void setRfsId(String rfsId) {
        this.rfsId = rfsId;
    }

    @JsonProperty(value = "orderStatus", access = Access.WRITE_ONLY)
    public void setStatus(String status) {
        this.status = status;
    }
}
