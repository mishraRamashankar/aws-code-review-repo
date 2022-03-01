package com.wtc.pureit.data.dto.response;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class NetSuiteResponseBody {
    private String status;
    private String message;
    private String data;
    private String billingAddressId;
    private String shippingAddressId;
    private String responseCode;

    @Override
    public String toString() {
        return "{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data='" + data + '\'' +
                ", billingAddressId='" + billingAddressId + '\'' +
                ", responseCode='" + responseCode + '\'' +
                '}';
    }
}
