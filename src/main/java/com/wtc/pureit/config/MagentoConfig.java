package com.wtc.pureit.config;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MagentoConfig {

    // Magento generic
    private final String threadWaitTime = System.getenv("threadWaitTime");
    private final String mediaType = System.getenv("mediaType");
    private final String magnetoAccessToken = System.getenv("magnetoAccessToken");

    // Item/Product
    private final String itemCreationMagentoUrl = System.getenv("itemCreationMagentoUrl");
    private final String bulkItemCreationMagentoUrl = System.getenv("bulkItemCreationMagentoUrl");
    private final String sendCustomerResponseUrl = System.getenv("customerResponse2MagentoUrl");

    // LSP
    private final String lspCreationUrl = System.getenv("magentoLspCreationUrl");
    private final String lspDetailCreationUrl = System.getenv("magentoLspDetailCreationUrl");
    private final String lspStockMapperUrl = System.getenv("magentoLspStockMapperUrl");

    // LSP-Stock
    private final String lspStockLinkageUrl = System.getenv("magentoLspStockLinkageUrl");
    private final String lspStockBulkLimitVal = System.getenv("magentoLspStockBulkLimitVal");

    // Order
    private final String orderStatusRetryCount = System.getenv("orderStatusLookUpCount");
    private final String fetchOrderUrl = System.getenv("fetchOrderMagentoUrl");
    private final String orderUpdateStatusUrl = System.getenv("magentoOrderUpdateStatusUrl");

    // Installation
    private final String installationStatusUrl = System.getenv("magentoInstallationStatusUrl");

    // Lead
    private final String fetchLeadDetailUrl = System.getenv("magentoFetchLeadDetailsUrl");

    // Complaint
    private final String fetchComplaintDetailUrl = System.getenv("magentoFetchComplaintDetailUrl");
    private final String updateComplaintIdToMagentoUrl = System.getenv("magentoUpdateNetsuiteComplainIdUrl");
    private final String complaintStatusUpdateUrl = System.getenv("magentoUpdateComplaintStatusUrl");

}
