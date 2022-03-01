package com.wtc.pureit.data.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Data
@Builder
@JsonRootName(value = "customer")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerCreationResponse {

    @JsonProperty(value = "id", access = Access.READ_ONLY)
    private Integer customerId;

    @JsonProperty(value = "customAttributes", access = Access.READ_ONLY)
    private List<CustomerCustomAttributes> customerCustomAttributes;

    @JsonIgnore
    private String shippingAddressId;

    @JsonIgnore
    private String message;

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public static List<CustomerCustomAttributes> customerCustomAttributes(String val) {
        return Arrays.asList(CustomerCustomAttributes.builder()
                .attributeCode("netsuite_customer_id")
                .value(val)
                .build());
    }

    @Builder
    @Getter
    public static class CustomerCustomAttributes {
        @JsonProperty(value = "attributeCode", access = Access.READ_ONLY)
        private String attributeCode;

        @JsonProperty(value = "value", access = Access.READ_ONLY)
        private String value;
    }
}
