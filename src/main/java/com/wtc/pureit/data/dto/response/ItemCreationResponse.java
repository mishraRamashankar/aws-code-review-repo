package com.wtc.pureit.data.dto.response;

import com.wtc.pureit.data.dto.CustomAttributes;

import java.sql.Timestamp;
import java.util.List;

public class ItemCreationResponse {

    private Integer id;

    private String sku;

    private String name;

    private Integer attributeSetId;

    private String price;

    private Integer status;

    private Integer visibility;

    private String typeId;

    private Integer weight;

    private Timestamp createdTs;

    private Timestamp updateTs;

    private List<CustomAttributes> customAttributes;
}
