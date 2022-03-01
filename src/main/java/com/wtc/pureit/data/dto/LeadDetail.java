package com.wtc.pureit.data.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LeadDetail {

    private Integer statusCode;
    private String status;
    private JsonNode bodyAsNode;

    @Override
    public String toString() {
        return "LeadDetail {" +
                "statusCode=" + statusCode +
                ", status='" + status + '\'' +
                ", bodyAsNode=" + bodyAsNode +
                '}';
    }
}
