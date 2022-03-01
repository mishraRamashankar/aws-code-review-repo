package com.wtc.pureit.api.handler.gateway;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.api.helper.ApiMapper;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.MagentoCaller;
import com.wtc.pureit.data.dto.request.OrderStatusUpdateRequest;
import com.wtc.pureit.data.dto.response.OrderUpdateStatusResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.io.InputStream;

@Slf4j
public class OrderUpdateStatusHandler implements RequestHandler<InputStream, String> {

    @SneakyThrows
    @Override
    public String handleRequest(InputStream inputStream, Context context) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode node = objectMapper.readTree(inputStream);
            log.info("InputStream: {}", objectMapper.writeValueAsString(node));

            EventRetry eventRetry = new EventRetry();
            RetryTemplate retryTemplate = eventRetry.retryTemplate();

            OrderUpdateStatusResponse orderUpdateStatusResponse = retryTemplate.execute((RetryCallback<OrderUpdateStatusResponse, Exception>) retryContext -> {
                OrderStatusUpdateRequest orderStatusUpdateRequest = objectMapper.treeToValue(node, OrderStatusUpdateRequest.class);

                String requestBody = objectMapper.writer().withRootName("data").writeValueAsString(orderStatusUpdateRequest);
                log.info("Order status update req: {}", requestBody);

                MagentoCaller magentoCaller = new MagentoCaller();
                String response = magentoCaller.call(magentoCaller.getMagentoConfig().getOrderUpdateStatusUrl(),
                        requestBody, ApiMapper.POST, null);
                log.info("Order status update response: {}", response);

                return OrderUpdateStatusResponse.builder().message(response).build();
            });

            String formattedResponse = objectMapper.writeValueAsString(orderUpdateStatusResponse);
            log.info("OrderUpdateStatusResponse: {}", formattedResponse);

            return formattedResponse;
        } catch (JsonProcessingException e) {
            log.info("Error: ", e);
            return e.getMessage();
        }
    }
}
