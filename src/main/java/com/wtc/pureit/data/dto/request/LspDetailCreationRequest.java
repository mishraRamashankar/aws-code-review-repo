package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "lspdetail")
public class LspDetailCreationRequest {

    private String internalId;

    private String areaInternalId;

    private String areaName;

    private String city;

    private String pinCode;

    private String lspName;

    private String isLsp;

    private String installationCdo;

    private String multiSkillingLeadLsp;

    private String cdoAreaFieldUpdate;

    private String cdoMassUpdate;

    //<editor-fold desc="getter/s">
    @JsonProperty(value = "lsp_internal_id", access = Access.READ_ONLY)
    public String getInternalId() {
        return internalId;
    }

    @JsonProperty(value = "area_internal_id", access = Access.READ_ONLY)
    public String getAreaInternalId() {
        return areaInternalId;
    }

    @JsonProperty(value = "area_name", access = Access.READ_ONLY)
    public String getAreaName() {
        return areaName;
    }

    @JsonProperty(value = "city", access = Access.READ_ONLY)
    public String getCity() {
        return city;
    }

    @JsonProperty(value = "pincode", access = Access.READ_ONLY)
    public String getPinCode() {
        return pinCode;
    }

    @JsonProperty(value = "name", access = Access.READ_ONLY)
    public String getLspName() {
        return lspName;
    }

    @JsonProperty(value = "lsp", access = Access.READ_ONLY)
    public String getIsLsp() {
        return isLsp;
    }

    @JsonProperty(value = "installation_cdo", access = Access.READ_ONLY)
    public String getInstallationCdo() {
        return installationCdo;
    }

    @JsonProperty(value = "multiskilling_lead_lsp", access = Access.READ_ONLY)
    public String getMultiSkillingLeadLsp() {
        return multiSkillingLeadLsp;
    }

    @JsonProperty(value = "cdo_area_field_update", access = Access.READ_ONLY)
    public String getCdoAreaFieldUpdate() {
        return cdoAreaFieldUpdate;
    }

    @JsonProperty(value = "cdo_mass_update", access = Access.READ_ONLY)
    public String getCdoMassUpdate() {
        return cdoMassUpdate;
    }
    //</editor-fold>

    //<editor-fold desc="setter/s">
    @JsonProperty(value = "LSP", access = Access.WRITE_ONLY)
    public void setIsLsp(String lsp) {
        this.isLsp = "Yes".equalsIgnoreCase(lsp) ? "Yes" : "No";
    }

    @JsonProperty(value = "Internal ID", access = Access.WRITE_ONLY)
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    @JsonProperty(value = "Area Internal ID", access = Access.WRITE_ONLY)
    public void setAreaInternalId(String areaInternalId) {
        this.areaInternalId = areaInternalId;
    }

    @JsonProperty(value = "Area Name", access = Access.WRITE_ONLY)
    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    @JsonProperty(value = "City", access = Access.WRITE_ONLY)
    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty(value = "Pincode", access = Access.WRITE_ONLY)
    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    @JsonProperty(value = "LSP Name", access = Access.WRITE_ONLY)
    public void setLspName(String lspName) {
        this.lspName = lspName;
    }

    @JsonProperty(value = "Installation CDO", access = Access.WRITE_ONLY)
    public void setInstallationCdo(String installationCdo) {
        this.installationCdo = installationCdo;
    }

    @JsonProperty(value = "Multiskilling Lead LSP", access = Access.WRITE_ONLY)
    public void setMultiSkillingLeadLsp(String multiSkillingLeadLsp) {
        this.multiSkillingLeadLsp = multiSkillingLeadLsp;
    }

    @JsonProperty(value = "CDO AREA FIELD UPDATE", access = Access.WRITE_ONLY)
    public void setCdoAreaFieldUpdate(String cdoAreaFieldUpdate) {
        this.cdoAreaFieldUpdate = cdoAreaFieldUpdate;
    }

    @JsonProperty(value = "CDO MASS UPDATE", access = Access.WRITE_ONLY)
    public void setCdoMassUpdate(String cdoMassUpdate) {
        this.cdoMassUpdate = cdoMassUpdate;
    }
    //</editor-fold>

}
