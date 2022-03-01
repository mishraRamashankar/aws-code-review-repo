package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.wtc.pureit.data.dto.helper.NetSuiteUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Objects;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadCreationRequest {

    @Valid
    @NotEmpty(message = "Name cannot be blank")
    private String name;

    @Valid
    @Size(min = 10, max = 10, message = "Phone number must have Ten digits only..")
    private String mobile;

    @Valid
    @NotEmpty(message = "Email cannot be blank")
    @Email(message = "The email address is invalid.", flags = {Flag.CASE_INSENSITIVE})
    private String email;

    @Valid
    @Size(min = 6, max = 6, message = "Pin-code must be of 6 digit")
    @NotEmpty(message = "Pin-code cannot be blank")
    private String pinCode;

    private String leadSource;
    private String airPurifier;
    private String demoDate;
    private String deviceType;
    private String deviceCategory;
    private String itemInternalId;
    private String timeStamp;

    public static LeadCreationRequest createLead(JsonNode m2LeadDetailNode) {
        String demoDate = getValue(m2LeadDetailNode, "demo_date");

        return LeadCreationRequest.builder()
                .name(getValue(m2LeadDetailNode, "name"))
                .mobile(getValue(m2LeadDetailNode, "mobile"))
                .email(getValue(m2LeadDetailNode, "email"))
                .pinCode(getValue(m2LeadDetailNode, "pincode"))
                .leadSource(getValue(m2LeadDetailNode,"leadSource"))
//                .leadSource("DTC-WEB")
                .airPurifier(getValue(m2LeadDetailNode, "airPurifier"))
                .demoDate(demoDate.isEmpty() ? demoDate : NetSuiteUtil.convertDate(Timestamp.valueOf(demoDate)))
                .deviceType(getValue(m2LeadDetailNode, "deviceType"))
                .deviceCategory(getValue(m2LeadDetailNode, "deviceCategory"))
                .itemInternalId(getValue(m2LeadDetailNode, "itemInternalId"))
                .timeStamp(getValue(m2LeadDetailNode, "timeStamp"))
                .build();
    }

    private static String getValue(JsonNode jsonNode, String key) {
        return Objects.nonNull(jsonNode.get(key)) ? jsonNode.get(key).textValue() : "";
    }

}
