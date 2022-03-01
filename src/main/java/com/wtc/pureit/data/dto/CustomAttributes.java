package com.wtc.pureit.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomAttributes {

    @JsonProperty(value = "attribute_code")
    private String attributeCode;

    @JsonProperty(value = "value")
    private String value;

}
