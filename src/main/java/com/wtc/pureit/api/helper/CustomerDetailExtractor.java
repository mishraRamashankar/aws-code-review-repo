package com.wtc.pureit.api.helper;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomerDetailExtractor {

    /**
     * Extract shipping address
     *
     * @param m2OrderDetailNode Order Detail node
     * @return
     */
    public static String getShippingAddress(JsonNode m2OrderDetailNode) {
        JsonNode shippingAddressNode = getShippingAddressNode(m2OrderDetailNode);
        log.info("Shipping Address: {}", shippingAddressNode);

        String street = "";
        for (JsonNode streetArr : shippingAddressNode.get("street")) {
            street = street.concat(" ").concat(streetArr.textValue()).concat(" ");
        }

        return street.concat(", ")
                .concat(shippingAddressNode.get("city").textValue()).concat(", ")
                .concat(shippingAddressNode.get("region").textValue()).concat(", ")
                .concat(shippingAddressNode.get("country_id").textValue());
    }

    /**
     * Extract shipping address node
     *
     * @param m2OrderDetailNode Order Detail node
     * @return
     */
    public static JsonNode getShippingAddressNode(JsonNode m2OrderDetailNode) {
        JsonNode shippingAssignmentsNode = m2OrderDetailNode.path("extension_attributes").path("shipping_assignments");
        return shippingAssignmentsNode.get(0).path("shipping").path("address");
    }

    /**
     * Extract billing address
     *
     * @param m2OrderDetailNode Order Detail node
     * @return
     */
    public static String getBillingAddress(JsonNode m2OrderDetailNode) {
        JsonNode billingAddressNode = m2OrderDetailNode.path("billing_address");
        log.info("Billing Address: {}", billingAddressNode);

        String street = "";
        for (JsonNode streetArr : billingAddressNode.get("street")) {
            street = street.concat(" ").concat(streetArr.textValue()).concat(" ");
        }

        return street.concat(", ")
                .concat(billingAddressNode.get("city").textValue()).concat(", ")
                .concat(billingAddressNode.get("region").textValue()).concat(", ")
                .concat(billingAddressNode.get("country_id").textValue());
    }

}