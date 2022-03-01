package com.wtc.pureit.data.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeadCreationResponse extends NetSuiteResponseBody {

    private String deviceCategory;

    private String customerId;

    @Override
    public String toString() {
        return "{" +
                "deviceCategory='" + deviceCategory + '\'' +
                ", customerId='" + customerId + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
