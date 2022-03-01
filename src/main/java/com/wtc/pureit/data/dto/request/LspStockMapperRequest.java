package com.wtc.pureit.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "sourceItems")
public class LspStockMapperRequest {

    private String sku;

    private String sourceCode;

    private String quantity;

    private String status;

    private String swzType;

    @JsonProperty(value = "IN - CBU Code", access = Access.WRITE_ONLY)
    private String cbuCode;

    @JsonIgnore
    private Boolean isActive;

    //<editor-fold desc="getter/s">
    @JsonProperty(value = "sku", access = Access.READ_ONLY)
    public String getSku() {
        return sku;
    }

    @JsonProperty(value = "source_code", access = Access.READ_ONLY)
    public String getSourceCode() {
        return sourceCode.replaceAll(" ", "").toLowerCase();
    }

    @JsonProperty(value = "quantity", access = Access.READ_ONLY)
    public String getQuantity() {
        return quantity;
    }

    @JsonProperty(value = "status", access = Access.READ_ONLY)
    public String getStatus() {
        return status;
    }
    //</editor-fold>

    //<editor-fold desc="setter/s">
    @JsonProperty(value = "Name", access = Access.WRITE_ONLY)
    public void setSku(String sku) {
        this.sku = sku;
    }

    @JsonProperty(value = "location name", access = Access.WRITE_ONLY)
    public void setSourceCode(String sourceCode) {
        this.sourceCode = Objects.nonNull(sourceCode) ? sourceCode.trim() : null;
    }

    @JsonProperty(value = "Location On Hand", access = Access.WRITE_ONLY)
    public void setQuantity(String quantity) {
        this.quantity = !quantity.isEmpty() ? quantity : "0";
        this.status = !quantity.isEmpty() ? "1" : "0";
    }

    @JsonProperty(value = "IN - SWZ Type", access = Access.WRITE_ONLY)
    public void setSwzType(String swzType) {
        this.swzType = swzType;
    }

    @JsonProperty(value = "location inactive", access = Access.WRITE_ONLY)
    public void setActive(String locationIsInactive) {
        isActive = "no".equalsIgnoreCase(locationIsInactive);
    }
    //</editor-fold>

    /**
     * convert incoming json into  list of {@link LspStockMapperRequest } object
     *
     * @param json
     * @return
     */
    public static List<LspStockMapperRequest> prepareReq(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json,
                    TypeFactory.defaultInstance()
                            .constructCollectionType(List.class, LspStockMapperRequest.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
