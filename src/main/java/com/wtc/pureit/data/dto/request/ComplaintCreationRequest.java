package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplaintCreationRequest {

    private String complainTitle;

    private String preferredDate;

    private String remark;

    private String preferredTime;

    private String deviceCategory;

    private String customerId;

    //<editor-fold desc="Getter/s">
    @JsonProperty(value = "items", access = Access.READ_ONLY)
    public String getComplainTitle() {
        return complainTitle;
    }

    @JsonProperty(value = "items", access = Access.READ_ONLY)
    public String getPreferredDate() {
        return preferredDate;
    }

    @JsonProperty(value = "items", access = Access.READ_ONLY)
    public String getRemark() {
        return remark;
    }

    @JsonProperty(value = "items", access = Access.READ_ONLY)
    public String getPreferredTime() {
        return preferredTime;
    }

    @JsonProperty(value = "items", access = Access.READ_ONLY)
    public String getDeviceCategory() {
        return deviceCategory;
    }

    @JsonProperty(value = "items", access = Access.READ_ONLY)
    public String getCustomerId() {
        return customerId;
    }
    //</editor-fold>

    //<editor-fold desc="Setter/s">
    @JsonProperty(value = "items", access = Access.WRITE_ONLY)
    public void setComplainTitle(String complainTitle) {
        this.complainTitle = complainTitle;
    }

    @JsonProperty(value = "items", access = Access.WRITE_ONLY)
    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    @JsonProperty(value = "items", access = Access.WRITE_ONLY)
    public void setRemark(String remark) {
        this.remark = remark;
    }

    @JsonProperty(value = "items", access = Access.WRITE_ONLY)
    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }

    @JsonProperty(value = "items", access = Access.WRITE_ONLY)
    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    @JsonProperty(value = "items", access = Access.WRITE_ONLY)
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    //</editor-fold>


    @Builder
    public static class UpdateNetsuiteComplaintDetailsToMagentoReq {
        @JsonProperty(value = "netsuite_complain_id")
        String netsuiteComplaintId;
    }
}