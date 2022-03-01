package com.wtc.pureit.config;

import lombok.Getter;

@Getter
public class NetSuiteConfig {

    // Generic
    private final String mediaType = System.getenv("mediaType");
    private final String cookie = System.getenv("cookie");

    // Oauth
    private final String realm = System.getenv("netSuiteOauthRealm");
    private final String consumerKey = System.getenv("netSuiteOauthConsumerKey");
    private final String token = System.getenv("netSuiteOauthAccessToken");
    private final String signatureMethod = System.getenv("netSuiteOauthSignatureMethod");
    private final String version = System.getenv("netSuiteOauthVersion");
    private final String consumerSecret = System.getenv("netSuiteOauthConsumerSecret");
    private final String tokenSecret = System.getenv("netSuiteOauthTokenSecret");
    private final String deploy = System.getenv("netSuiteDeploy");
    private final String netSuiteUri = System.getenv("netSuiteUri");

    // Lead script
    private final String leadCreationScript = System.getenv("netSuiteLeadCreationScript");

    // Order script
    private final String orderCreationScript = System.getenv("netSuiteOrderCreationScript");
    private final String orderStatusScript = System.getenv("netSuiteOrderStatusScript");
    private final String orderCancellationScript = System.getenv("netSuiteOrderCancellationScript");
    private final String salesOrderCreationScript = System.getenv("netSuiteSalesOrderCreationScript");
    private final String invoiceCreationScript = System.getenv("netSuiteInvoiceCreationScript");

    // Customer script
    private final String customerCreationScript = System.getenv("netSuiteCustomerCreationScript");
    private final String customerAddressUpdateScript = System.getenv("netSuiteCustomerAddressUpdateScript");
    private final String customerDetailScript = System.getenv("netSuiteCustomerDetailScript");

    // Complaint script
    private final String complaintCreationScript = System.getenv("netSuiteComplaintCreationScript");
    private final String complaintStatusScript = System.getenv("netSuiteComplaintStatusScript");

}
