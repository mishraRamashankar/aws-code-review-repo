package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wtc.pureit.data.dto.CustomAttributes;
import com.wtc.pureit.data.dto.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "product")
public class ItemCreationRequest {

    /**
     * unique id which will act as a link b/w the custom attributes and the product.
     */
    @JsonProperty(value = "Internal ID", access = Access.WRITE_ONLY)
    private String internalId;

    private String sku;

    private String name;

    private Integer attributeSetId;

    private String price;

    @JsonProperty(value = "Inactive", access = Access.WRITE_ONLY)
    private String inactive;

    private Integer status;

    private Integer visibility;

    private String typeId;

    private Integer weight;

    @JsonProperty(value = "IN - CBU Code", access = Access.WRITE_ONLY)
    private String cbuCode;

    private List<CustomAttributes> customAttributes;

    //<editor-fold desc="getter">
    @JsonProperty(value = "sku", access = Access.READ_ONLY)
    public String getSku() {
        return sku;
    }

    @JsonProperty(value = "name", access = Access.READ_ONLY)
    public String getName() {
        return name;
    }

    @JsonProperty(value = "price", access = Access.READ_ONLY)
    public String getPrice() {
        return price;
    }

    @JsonProperty(value = "type_id", access = Access.READ_ONLY)
    public String getTypeId() {
        return typeId;
    }

    @JsonProperty(value = "attribute_set_id", access = Access.READ_ONLY)
    public Integer getAttributeSetId() {
        return attributeSetId;
    }
    //</editor-fold>

    //<editor-fold desc="setter">
    @JsonProperty(value = "Name", access = Access.WRITE_ONLY)
    public void setSku(String sku) {
        this.sku = sku;
    }

    @JsonProperty(value = "Display Name", access = Access.WRITE_ONLY)
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty(value = "Base price", access = Access.WRITE_ONLY)
    public void setPrice(String price) {
        this.price = price;
    }
    //</editor-fold>

    @JsonIgnore
    private String itemCategory;

    @JsonProperty(value = "Device category name", access = Access.WRITE_ONLY)
    private String deviceCategoryName;

    @JsonIgnore
    private String itemDeviceCategory;

    @JsonIgnore
    private Boolean isGkk;

    /**
     * <>
     * 1- No Visible
     * 2- Catalog
     * 3- Search
     * 4- Catalog/Search
     * </>
     */
    @JsonProperty(value = "IN - Category", access = Access.WRITE_ONLY)
    public void setTypeAndVisibility(String typeId) {
        //<editor-fold desc="set type-id">
        if (typeId.equalsIgnoreCase(ItemCategory.CONTRACTS.name())) {
            this.typeId = "virtual";
        } else {
            this.typeId = "simple";
        }
        //</editor-fold>

        //<editor-fold desc="set visibility">
        if (typeId.equalsIgnoreCase(ItemCategory.COMPONENTS.name())) {
            this.visibility = 1;
        } else {
            this.visibility = 4;
        }

        this.itemCategory = typeId;
        //</editor-fold>
    }
    //</editor-fold>

    /**
     * convert incoming json into  list of {@link ItemCreationRequest } object
     *
     * @param json
     * @return
     */
    public static List<ItemCreationRequest> prepareItemReq(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        try {
            Map<String, List<CustomAttributes>> customAttributeMap = ItemCustomAttributes.getCustomAttr(json);

            List<ItemCreationRequest> itemCreationRequests = objectMapper.readValue(json,
                    TypeFactory.defaultInstance()
                            .constructCollectionType(List.class, ItemCreationRequest.class));

            for (ItemCreationRequest itemCreationRequest : itemCreationRequests) {
                itemCreationRequest.setAttributeSetId(4);
                itemCreationRequest.setStatus("yes".equalsIgnoreCase(itemCreationRequest.getInactive()) ? 0 : 1);

                List<CustomAttributes> customAttributes = customAttributeMap.get(itemCreationRequest.getInternalId());
                customAttributes.add(CustomAttributes.builder()
                        .attributeCode("url_key")
                        .value(itemCreationRequest.getName() + "-" + itemCreationRequest.getSku())
                        .build());

                customAttributes.add(CustomAttributes.builder()
                        .attributeCode("item_category")
                        .value(itemCreationRequest.getItemCategory())
                        .build());

                customAttributes.add(CustomAttributes.builder()
                        .attributeCode("cbu_code")
                        .value(itemCreationRequest.getCbuCode())
                        .build());

                for (CustomAttributes attributes : customAttributes) {
                    if ("item_device_category".equalsIgnoreCase(attributes.getAttributeCode())) {
                        itemCreationRequest.setItemDeviceCategory(attributes.getValue());
                    }
                    //set is_gkk
                    if ("gkk_products".equalsIgnoreCase(attributes.getAttributeCode())) {
                        attributes.setValue(itemCreationRequest.getItemCategory()
                                .equalsIgnoreCase(ItemCategory.CONSUMABLE.name()) ? "1" : "0");
                    }
                }

                itemCreationRequest.setCustomAttributes(customAttributes);
            }
            return itemCreationRequests;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
