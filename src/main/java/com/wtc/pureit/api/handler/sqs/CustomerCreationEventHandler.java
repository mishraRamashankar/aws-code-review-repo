package com.wtc.pureit.api.handler.sqs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.NetSuiteCaller;
import com.wtc.pureit.api.helper.SqsMessageExtractor;
import com.wtc.pureit.data.dto.request.CustomerCreationRequest;
import com.wtc.pureit.data.dto.response.CustomerCreationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.util.Objects;

@Slf4j
public class CustomerCreationEventHandler implements RequestHandler<SQSEvent, String> {

    private final NetSuiteCaller netSuiteCaller;

    public CustomerCreationEventHandler() {
        this.netSuiteCaller = new NetSuiteCaller();
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        log.info("SQS event handler: {}", sqsEvent);
        String resp = "";

        for (SQSEvent.SQSMessage sqsMessage : sqsEvent.getRecords()) {
            JsonNode messageBody = SqsMessageExtractor.extract(sqsMessage);

            log.info("Message body: {}", messageBody.textValue());

            EventRetry eventRetry = new EventRetry();
            RetryTemplate retryTemplate = eventRetry.retryTemplate();

            try {
                resp = retryTemplate.execute((RetryCallback<String, Exception>) retryContext -> {
                    CustomerCreationRequest customerCreationRequest = CustomerCreationRequest.createCustomer(messageBody);

                    CustomerCreationResponse customerCreationResponse = netSuiteCaller.createCustomer(customerCreationRequest);

                    return Objects.nonNull(customerCreationResponse) ? (customerCreationResponse.getMessage().equalsIgnoreCase("Record added") ?
                            customerCreationResponse.getMessage() : null) : null;
                });
            } catch (Exception e) {
                log.error("Exception: ", e);
                throw new RuntimeException(e);
            }
        }

        return resp;
    }
}
