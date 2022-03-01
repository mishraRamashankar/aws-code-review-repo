package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonNode;
import com.wtc.pureit.api.helper.CustomerDetailExtractor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCustomerMasterAddressRequest {

    @Valid
    @NotEmpty(message = "Customer name cannot be blank")
    @JsonProperty(value = "customerId", access = Access.READ_ONLY)
    private String customerId;

    @JsonProperty(value = "addressArry", access = Access.READ_ONLY)
    private List<Address> address;

    /**
     * convert incoming jsonNode into  {@link UpdateCustomerMasterAddressRequest } object
     *
     * @param orderDetailNode    Order detail node
     * @param netsuiteCustomerId Netsuite Customer Id
     * @return
     */
    public static UpdateCustomerMasterAddressRequest updateCustomer(JsonNode orderDetailNode, String netsuiteCustomerId) {
        String billingAddress = CustomerDetailExtractor.getBillingAddress(orderDetailNode);

        return UpdateCustomerMasterAddressRequest.builder()
                .customerId(netsuiteCustomerId)
                .address(Collections.singletonList(Address.builder()
                        .address1(billingAddress)
                        .city(orderDetailNode.path("billing_address").path("city").textValue())
                        .state(orderDetailNode.path("billing_address").path("region").textValue())
                        .pinCode(orderDetailNode.path("billing_address").path("postcode").textValue())
                        .build()))
                .build();
    }

    private static String getValue(JsonNode jsonNode, String key) {
        return Objects.nonNull(jsonNode.get(key)) ? jsonNode.get(key).textValue().trim() : null;
    }

    @Builder
    @JsonRootName(value = "addressArry")
    static class Address {

        @JsonProperty(value = "address1", access = Access.READ_ONLY)
        private String address1;

        @JsonProperty(value = "address2", access = Access.READ_ONLY)
        private String address2;

        private String city;
        private String state;

        @JsonProperty(value = "pincode", access = Access.READ_ONLY)
        private String pinCode;
    }
}

