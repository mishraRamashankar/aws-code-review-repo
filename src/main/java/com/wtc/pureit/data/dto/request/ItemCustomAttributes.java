package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wtc.pureit.data.dto.CustomAttributes;
import lombok.Getter;
import org.springframework.util.ReflectionUtils;

import java.util.*;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemCustomAttributes {

    /**
     * unique id which will act as a link b/w the custom attributes and the product.
     */
    @JsonProperty("Internal ID")
    private String item_internal_id;

    @JsonProperty("IN - Category")
    private String device_type;

    @JsonProperty("Device category name")
    private String device_category;

    @JsonProperty("Device category id")
    private String device_category_internalid;

    @JsonProperty("IN - Sub-Category")
    private String device_subcategory;

    @JsonProperty("IN - Item Device Category")
    private String item_device_category;

    @JsonProperty("IN - Brand Code")
    private String ns_brand;

    @JsonProperty("IN - Brand Variant Code")
    private String brand_variant_code;

    @JsonProperty("IN - HSN/SAC Code")
    private String hsn_code;

    //    @JsonProperty("IN - HHT Active") json annotation added on setter method
    private Integer hht_active;

    @JsonProperty("IN - Base Pack")
    private String base_pack;

    @JsonProperty("IN - TUR Value")
    private String tur_value;

    @JsonProperty("IN - Landing Price")
    private String landing_price;

    //    @JsonProperty("IN - PTL") json annotation added on setter method
    private Integer it_ptl;

    @JsonProperty("IN - Item Type")
    private String item_type;

    private Boolean gkk_products;

    /**
     * get custom attributes of the items.
     *
     * @param json
     * @return
     * @throws JsonProcessingException
     */
    public static Map<String, List<CustomAttributes>> getCustomAttr(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        List<ItemCustomAttributes> itemCustomAttributes = objectMapper.readValue(json,
                TypeFactory.defaultInstance()
                        .constructCollectionType(List.class, ItemCustomAttributes.class));

        Map<String, List<CustomAttributes>> map = new HashMap<>();

        for (ItemCustomAttributes itemCustomAttribute : itemCustomAttributes) {
            List<CustomAttributes> customAttributes = new ArrayList<>();

            ReflectionUtils.doWithFields(itemCustomAttribute.getClass(), field -> {
                field.setAccessible(true);

                customAttributes.add(CustomAttributes.builder()
                        .attributeCode(field.getName())
                        .value(Optional.ofNullable(field.get(itemCustomAttribute)).isPresent()
                                ? field.get(itemCustomAttribute).toString() : "")
                        .build());
            });

            map.put(itemCustomAttribute.getItem_internal_id(), customAttributes);
        }

        return map;
    }

    @JsonProperty(value = "IN - HHT Active")
    public void setHht_active(String hht_active) {
        this.hht_active = hht_active.equalsIgnoreCase("yes") ? 1 : 0;
    }


    @JsonProperty(value = "IN - PTL")
    public void setIt_ptl(String it_ptl) {
        this.it_ptl = it_ptl.equalsIgnoreCase("yes") ? 1 : 0;
    }
}
