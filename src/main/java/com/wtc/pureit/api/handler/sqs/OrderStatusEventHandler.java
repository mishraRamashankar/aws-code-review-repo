package com.wtc.pureit.api.handler.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.LambdaInMemoryCache;
import com.wtc.pureit.api.helper.NetSuiteCaller;
import com.wtc.pureit.data.dto.response.OrderStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class OrderStatusEventHandler extends LambdaInMemoryCache {

    private final NetSuiteCaller netSuiteCaller;

    public OrderStatusEventHandler() {
        this.netSuiteCaller = new NetSuiteCaller();
    }

    public String process(JsonNode messageBody) throws Exception {
        log.info("Message body: {}", messageBody.textValue());

        EventRetry eventRetry = new EventRetry();
        RetryTemplate retryTemplate = eventRetry.retryTemplate();

        String message = retryTemplate.execute((RetryCallback<String, Exception>) retryContext -> {
            ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);
            try {
                String rfsId = Objects.nonNull(messageBody.get("orderId")) ?
                        messageBody.get("orderId").textValue() : null;

                Map<String, String> param = new HashMap<>();
                param.put("rfsId", rfsId);

                OrderStatusResponse orderStatusResponse = netSuiteCaller.getOrderStatus(param);

                return objectMapper.writeValueAsString(orderStatusResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        });

        clearInMemory();

        return message;
    }

}
