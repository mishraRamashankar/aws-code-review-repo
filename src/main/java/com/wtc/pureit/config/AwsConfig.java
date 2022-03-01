package com.wtc.pureit.config;

import lombok.Getter;

@Getter
public class AwsConfig {

    // access key
    private final String accessKeyId = System.getenv("awsAccessKeyId");

    // secret access key
    private final String secretAccessKey = System.getenv("awsSecretAccessKey");

    // region
    private final String region = System.getenv("awsRegion");

    // Event fail back
    private final String maxRetry = System.getenv("eventFailOverMaxRetry");
    private final String backOffPeriod = System.getenv("eventFailOverBackOffPeriod");
}
