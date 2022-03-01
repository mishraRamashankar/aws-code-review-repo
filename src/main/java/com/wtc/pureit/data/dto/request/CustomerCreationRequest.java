package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.JsonNode;
import com.wtc.pureit.data.dto.helper.NetSuiteUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerCreationRequest {

    @Valid
    @NotEmpty(message = "First name cannot be blank")
    @JsonProperty(value = "first_name", access = Access.READ_ONLY)
    private String firstName;

    @Valid
    @NotEmpty(message = "Last name cannot be blank")
    @JsonProperty(value = "last_name", access = Access.READ_ONLY)
    private String lastName;

    private String deviceCategory;

    @Valid
    @Size(min = 10, max = 10, message = "Phone number must have Ten digits only..")
    private String phone;
    private String landline;

    @Valid
    @NotEmpty(message = "Email cannot be blank")
    @Email(message = "The email address is invalid.", flags = {Flag.CASE_INSENSITIVE})
    private String email;

    private String gender;

    @JsonProperty(value = "address_1", access = Access.READ_ONLY)
    private String address1;

    @JsonProperty(value = "address_2", access = Access.READ_ONLY)
    private String address2;
    private String landmark;
    private String city;
    private String state;

    @JsonProperty(value = "pincode", access = Access.READ_ONLY)
    private String pinCode;

    @JsonProperty(value = "devicepurchasedate", access = Access.READ_ONLY)
    private String devicePurchaseDate;
    private String deviceSerialNo;
    private String itemInternalId;

    /**
     * convert incoming orderDetailNode into  {@link CustomerCreationRequest } object
     *
     * @param orderDetailNode Order Detailss
     * @return
     */
    public static CustomerCreationRequest createCustomer(JsonNode orderDetailNode) throws ParseException {
        JsonNode shippingAssignmentsNode = orderDetailNode.path("extension_attributes").path("shipping_assignments");
        if (Objects.isNull(shippingAssignmentsNode)) {
            throw new IllegalArgumentException("Shipping Assignment not present for order id" + orderDetailNode.get("entity_id"));
        }

        JsonNode shippingAddressNode = shippingAssignmentsNode.get(0).path("shipping").path("address");

        List<String> address = new ArrayList<>();
        if (shippingAssignmentsNode.isArray()) {
            log.info("Shipping assignment is array");

            for (JsonNode node : shippingAssignmentsNode) {

                shippingAddressNode = node.path("shipping").path("address");
                log.info("Shipping Address: {}", shippingAddressNode);

                String street = "";
                for (JsonNode streetArr : shippingAddressNode.get("street")) {
                    street = street.concat(" ").concat(streetArr.textValue()).concat(" ");
                }

                String shippingAddress = street.concat(", ")
                        .concat(shippingAddressNode.get("city").textValue()).concat(", ")
                        .concat(shippingAddressNode.get("region").textValue()).concat(", ")
                        .concat(shippingAddressNode.get("country_id").textValue());

                address.add(shippingAddress);
            }
            log.info("Shipping Address: {}", Arrays.toString(address.toArray()));
        }

        JsonNode itemsNode = getItems(orderDetailNode);
        if (Objects.isNull(itemsNode)) {
            throw new IllegalArgumentException("Items not present for order id" + orderDetailNode.get("entity_id"));
        }

        JsonNode itemNode;
        if (itemsNode.isArray()) {
            itemNode = itemsNode.get(0);
        } else {
            itemNode = itemsNode;
        }

        JsonNode extensionAttributes = itemNode.get("extension_attributes");

        return CustomerCreationRequest.builder()
                .firstName(getValue(orderDetailNode, "customer_firstname"))
                .lastName(getValue(orderDetailNode, "customer_lastname"))
                .deviceCategory(getValue(extensionAttributes, "item_device_category_internal_id"))
                .phone(getValue(shippingAddressNode, "telephone"))
                .landline(getValue(orderDetailNode, ""))
                .email(getValue(orderDetailNode, "customer_email"))
                .gender(getValue(orderDetailNode, ""))
                .address1(address.get(0))
                .address2(address.size() > 1 ? address.get(1) : "")
                .landmark(getValue(orderDetailNode, ""))
                .city(getValue(shippingAddressNode, "city"))
                .state(getValue(shippingAddressNode, "region"))
                .pinCode(getValue(shippingAddressNode, "postcode"))
                .devicePurchaseDate(getDevicePurchaseDate(itemNode))
                .deviceSerialNo(getValue(extensionAttributes, "serial_number"))
                .itemInternalId(getValue(extensionAttributes, "item_internal_id"))
                .build();
    }

    @NotNull
    private static String getDevicePurchaseDate(JsonNode itemNode) throws ParseException {
        Timestamp ts = Timestamp.valueOf(getValue(itemNode, "created_at"));

        return NetSuiteUtil.convertDate(ts);
    }

    /**
     * @param orderDetailNode Order  detail information
     * @return
     */
    private static JsonNode getItems(JsonNode orderDetailNode) {
        return Objects.nonNull(orderDetailNode.get("items")) ? orderDetailNode.get("items") : null;
    }

    private static String getValue(JsonNode jsonNode, String key) {
        return Objects.nonNull(jsonNode.get(key)) ? jsonNode.get(key).textValue() : "";
    }

}
