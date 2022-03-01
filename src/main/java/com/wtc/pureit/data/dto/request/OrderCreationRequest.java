package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.JsonNode;
import com.wtc.pureit.api.helper.CustomerDetailExtractor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCreationRequest {


    @Valid
    @Size(min = 6, max = 6, message = "Pin-code must be of 6 digit")
    @NotEmpty(message = "Pin-code cannot be blank")
    @JsonProperty("deliveryPinCode")
    private String deliveryPinCode;

    @Valid
    @NotEmpty(message = "Customer internal-id cannot be empty!!")
    @JsonProperty("customerInternalId")
    private String customerInternalId;

    @Valid
    @NotEmpty(message = "Delivery address cannot be empty!!")
    @JsonProperty("deliveryAddress")
    private String deliveryAddress;

    /**
     *
     */
    @JsonProperty(value = "items", access = Access.READ_ONLY)
    private List<Item> items;

    /**
     * convert incoming params into  {@link OrderCreationRequest } object
     *
     * @param customerInternalId, m2OrderDetailNode
     * @return
     */
    public static OrderCreationRequest createOrder(JsonNode m2OrderDetailNode, String customerInternalId, String shippingAddressId) {
        List<Item> items = new ArrayList<>();
        JsonNode itemsNodes = m2OrderDetailNode.get("items");

        for (JsonNode itemNode : itemsNodes) {
            log.info("ItemNode: {}", itemNode);

            JsonNode extensionAttributes = itemNode.get("extension_attributes");

            Item item = Item.builder()
                    .deviceCategory(extensionAttributes.get("item_device_category_internal_id").textValue())
                    .itemInternalId(extensionAttributes.get("item_internal_id").textValue())
                    .quantity(itemNode.get("qty_ordered").toString())
                    .itemPrice(itemNode.get("price").toString())
                    .description(itemNode.get("name").toString())
                    .taxRate(itemNode.get("tax_percent").toString() + "%")
                    .taxAmount(itemNode.get("tax_amount").toString())
                    .expectedTime("")
                    .build();
            items.add(item);

            log.info("Item: {}", item.toString());
        }

        JsonNode shippingAddressNode = CustomerDetailExtractor.getShippingAddressNode(m2OrderDetailNode);

        return OrderCreationRequest.builder()
                .customerInternalId(customerInternalId)
                .items(items)
                .deliveryAddress(shippingAddressId)
                .deliveryPinCode(shippingAddressNode.get("postcode").textValue())
                .build();
    }

    private static String getValue(JsonNode jsonNode, String key) {
        return Objects.nonNull(jsonNode.get(key)) ? jsonNode.get(key).textValue() : "";
    }

    @Builder
    static class Item {
        @JsonProperty("devicecategory")
        private String deviceCategory;

        @JsonProperty("itemInternalId")
        String itemInternalId;

        @JsonProperty("quantity")
        String quantity;

        @JsonProperty("itemPrice")
        String itemPrice;

        @JsonProperty("description")
        String description;

        @JsonProperty("taxRate")
        String taxRate;

        @JsonProperty("taxAmount")
        String taxAmount;

        @JsonProperty("expectedTime")
        String expectedTime;

        @Override
        public String toString() {
            return "{" +
                    "deviceCategory: '" + deviceCategory + '\'' +
                    ", itemInternalId: '" + itemInternalId + '\'' +
                    ", quantity: '" + quantity + '\'' +
                    ", itemPrice: '" + itemPrice + '\'' +
                    ", description: '" + description + '\'' +
                    '}';
        }
    }
}