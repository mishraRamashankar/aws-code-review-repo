package com.wtc.pureit.api.handler.sqs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.NetSuiteCaller;
import com.wtc.pureit.api.helper.SqsMessageExtractor;
import com.wtc.pureit.data.dto.request.UpdateCustomerMasterAddressRequest;
import com.wtc.pureit.data.dto.response.UpdateCustomerAddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.util.Objects;

@Slf4j
public class UpdateCustomerAddressEventHandler implements RequestHandler<SQSEvent, String> {

    private final NetSuiteCaller netSuiteCaller;

    public UpdateCustomerAddressEventHandler() {
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
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                resp = retryTemplate.execute((RetryCallback<String, Exception>) retryContext -> {
                    JsonNode orderBody = objectMapper.readTree(sqsMessage.getBody());
                    UpdateCustomerMasterAddressRequest addressRequest = UpdateCustomerMasterAddressRequest.updateCustomer(orderBody, null);
                    log.info("UpdateCustomerMasterAddressRequest: {}", objectMapper.writeValueAsString(addressRequest));

                    UpdateCustomerAddressResponse updateCustomerAddressResponse = netSuiteCaller.updateCustomerMasterAddress(addressRequest);
                    log.info("updateCustomerAddressResponse: {}", objectMapper.writeValueAsString(updateCustomerAddressResponse));

                    return Objects.nonNull(updateCustomerAddressResponse) ? (updateCustomerAddressResponse.getMessage().equalsIgnoreCase("Record added") ?
                            updateCustomerAddressResponse.getMessage() : null) : null;
                });
            } catch (Exception e) {
                log.error("Exception: ", e);
                return "Unable to update the customer address";
            }
        }
        return resp;
    }
}
