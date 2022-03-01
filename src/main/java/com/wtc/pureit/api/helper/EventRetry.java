package com.wtc.pureit.api.helper;

import com.wtc.pureit.config.AwsConfig;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Objects;

public class EventRetry {

    public RetryTemplate retryTemplate() {
        AwsConfig awsConfig = new AwsConfig();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();

        String backOfPeriod = "2000";
        if (Objects.nonNull(awsConfig.getBackOffPeriod())) {
            backOfPeriod = awsConfig.getBackOffPeriod();
        }
        fixedBackOffPolicy.setBackOffPeriod(Integer.parseInt(backOfPeriod));

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        String eventMaxRetry = "4";
        if (Objects.nonNull(awsConfig.getMaxRetry())) {
            eventMaxRetry = awsConfig.getBackOffPeriod();
        }
        retryPolicy.setMaxAttempts(Integer.parseInt(eventMaxRetry));

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
