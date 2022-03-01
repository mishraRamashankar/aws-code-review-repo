package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesOrderCreationRequest {

    @JsonProperty(value = "rfsId")
    private String rfsId;

    @JsonProperty(value = "shippingAddress")
    private String shippingAddress;

    @JsonProperty(value = "billingAddress")
    private String billingAddress;

    /**
     * create sales order creation request for the given params
     *
     * @param rfsId           Order rfsId
     * @param shippingAddress Shipping Address of the order
     * @param billingAddress  Billing Address of the order
     * @return
     */
    public static SalesOrderCreationRequest createReq(String rfsId, String shippingAddress, String billingAddress) {
        return SalesOrderCreationRequest.builder()
                .rfsId(rfsId)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .build();
    }
}
