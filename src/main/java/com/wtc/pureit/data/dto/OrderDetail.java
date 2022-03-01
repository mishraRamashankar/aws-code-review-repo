package com.wtc.pureit.data.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    private Integer statusCode;
    private String status;
    private JsonNode bodyAsNode;

    @Override
    public String toString() {
        return "OrderDetail {" +
                "statusCode=" + statusCode +
                ", status='" + status + '\'' +
                ", bodyAsNode=" + bodyAsNode +
                '}';
    }
}
