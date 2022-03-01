package com.wtc.pureit.data.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusResponse extends NetSuiteResponseBody {

    private String rfsStatus;
}
