package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "source")
public class LspCreationRequest {

    @JsonProperty(value = "Internal ID", access = Access.WRITE_ONLY)
    private String internalId;

    private String phone;

    private String lspName;

    private String address1;

    private String country;

    private String city;

    private String province;

    private String zipCode;

    private String sourceCode;

    private Boolean enabled;

    private String contactName;

    @JsonProperty(value = "region_id", access = Access.READ_ONLY)
    private Integer regionId;

    private String latitude;

    private String longitude;

    //<editor-fold desc="getter/s">
    @JsonProperty(value = "name", access = Access.READ_ONLY)
    public String getLspName() {
        return lspName + "-" + internalId;
    }

    @JsonProperty(value = "street", access = Access.READ_ONLY)
    public String getAddress1() {
        return address1;
    }

    @JsonProperty(value = "country_id", access = Access.READ_ONLY)
    public String getCountry() {
        return country;
    }

    @JsonProperty(value = "city", access = Access.READ_ONLY)
    public String getCity() {
        return city;
    }

    @JsonProperty(value = "region", access = Access.READ_ONLY)
    public String getProvince() {
        return province;
    }

    @JsonProperty(value = "postcode", access = Access.READ_ONLY)
    public String getZipCode() {
        return zipCode;
    }

    @JsonProperty(value = "source_code", access = Access.READ_ONLY)
    public String getSourceCode() {
        return sourceCode.replaceAll(" ", "").toLowerCase();
    }

    @JsonProperty(value = "enabled", access = Access.READ_ONLY)
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty(value = "contact_name", access = Access.READ_ONLY)
    public String getContactName() {
        return contactName;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
    //</editor-fold>

    //<editor-fold desc="setter/s">
    @JsonProperty(value = "Phone", access = Access.WRITE_ONLY)
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonProperty(value = "Name", access = Access.WRITE_ONLY)
    public void setLspName(String lspName) {
        this.lspName = lspName;
        this.contactName = lspName;
    }

    @JsonProperty(value = "Address 1", access = Access.WRITE_ONLY)
    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    @JsonProperty(value = "Country", access = Access.WRITE_ONLY)
    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty(value = "City", access = Access.WRITE_ONLY)
    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty(value = "Province", access = Access.WRITE_ONLY)
    public void setProvince(String province) {
        this.province = province;
    }

    @JsonProperty(value = "Zip Code", access = Access.WRITE_ONLY)
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @JsonProperty(value = "Location", access = Access.WRITE_ONLY)
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    @JsonProperty(value = "Partner Inactive", access = Access.WRITE_ONLY)
    public void setEnabled(String isPartnerInactive) {
        this.enabled = "No".equalsIgnoreCase(isPartnerInactive);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    //</editor-fold>

    /**
     * convert incoming json into  list of {@link LspCreationRequest } object
     *
     * @param json
     * @return
     */
    public static List<LspCreationRequest> prepareLspDetailReq(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json,
                    TypeFactory.defaultInstance()
                            .constructCollectionType(List.class, LspCreationRequest.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
