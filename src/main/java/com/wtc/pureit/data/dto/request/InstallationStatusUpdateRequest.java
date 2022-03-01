package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("serviceRequest")
public class InstallationStatusUpdateRequest {

    @JsonProperty(value = "rfsId")
    String rfsId;
    @JsonProperty(value = "itemInternalId")
    String itemInternalId;
    @JsonProperty(value = "complaintTitle")
    String complaintTitle;
    @JsonProperty(value = "netsuiteCustomerId")
    String netsuiteCustomerId;
    @JsonProperty(value = "installationStatus")
    String installationStatus;
    @JsonProperty(value = "installationId")
    String installationId;
    @JsonProperty(value = "installationDate")
    String installationDate;
    @JsonProperty(value = "installationTime")
    String installationTime;
    @JsonProperty(value = "serialNo")
    String serialNo;
    @JsonProperty(value = "qtyPos")
    String qtyPos;

}
